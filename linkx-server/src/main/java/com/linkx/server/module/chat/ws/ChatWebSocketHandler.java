package com.linkx.server.module.chat.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.module.chat.constant.ChatConstants;
import com.linkx.server.module.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final int MAX_TEXT_PAYLOAD_LENGTH = 16 * 1024;
    private static final int COMMAND_RATE_WINDOW_MILLIS = 10_000;
    private static final int COMMAND_RATE_LIMIT = 120;
    private static final int MAX_HISTORY_PAGE_SIZE = 200;
    private static final int MAX_HISTORY_PAGE_NUMBER = 100;
    private static final String ATTR_COMMAND_WINDOW_START = "chat:commandWindowStart";
    private static final String ATTR_COMMAND_WINDOW_COUNT = "chat:commandWindowCount";

    private final ChatPresenceService chatPresenceService;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);
        if (userId == null) {
            log.warn("WebSocket connection rejected, sessionId={}, reason=missing_user_identity", session.getId());
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing user identity"));
            return;
        }
        chatPresenceService.onConnected(userId, session);
        log.info("WebSocket connected, sessionId={}, userId={}", session.getId(), userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if ("PING".equalsIgnoreCase(payload)) {
            session.sendMessage(new TextMessage("PONG"));
            return;
        }
        if (payload != null && payload.length() > MAX_TEXT_PAYLOAD_LENGTH) {
            sendCommandError(session, "", "", ErrorCode.BAD_REQUEST.getCode(), "WebSocket 消息过大");
            return;
        }

        Long userId = getUserId(session);
        if (userId == null) {
            sendCommandError(session, "", "", ErrorCode.UNAUTHORIZED.getCode(), "用户未认证");
            return;
        }
        if (!allowCommand(session)) {
            sendCommandError(session, "", "", ErrorCode.TOO_MANY_REQUESTS.getCode(), "请求过于频繁，请稍后重试");
            return;
        }

        JsonNode requestNode;
        try {
            requestNode = objectMapper.readTree(payload);
        } catch (JsonProcessingException exception) {
            sendCommandError(session, "", "", ErrorCode.BAD_REQUEST.getCode(), "WebSocket 消息格式错误");
            return;
        }

        String requestId = readText(requestNode, "requestId");
        String action = readText(requestNode, "action");
        JsonNode dataNode = requestNode.path("data");
        if (!StringUtils.hasText(requestId) || !StringUtils.hasText(action)) {
            sendCommandError(session, requestId, action, ErrorCode.BAD_REQUEST.getCode(), "请求缺少 requestId 或 action");
            return;
        }

        try {
            Object result = handleAction(userId, action, dataNode);
            log.info("WebSocket command handled, sessionId={}, userId={}, requestId={}, action={}, success=true",
                    session.getId(), userId, requestId, action);
            sendCommandSuccess(session, requestId, action, result);
        } catch (BusinessException exception) {
            log.warn("WebSocket command rejected, sessionId={}, userId={}, requestId={}, action={}, code={}, message={}",
                    session.getId(), userId, requestId, action, exception.getErrorCode().getCode(), exception.getMessage());
            sendCommandError(
                    session,
                    requestId,
                    action,
                    exception.getErrorCode().getCode(),
                    exception.getMessage()
            );
        } catch (Exception exception) {
            log.error("Handle websocket command failed, action={}, sessionId={}", action, session.getId(), exception);
            sendCommandError(session, requestId, action, ErrorCode.INTERNAL_ERROR.getCode(), "服务器内部错误");
        }
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) {
        log.debug("Received pong, sessionId={}", session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("WebSocket transport error, sessionId={}", session.getId(), exception);
        session.close(CloseStatus.SERVER_ERROR);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserId(session);
        if (userId != null) {
            chatPresenceService.onDisconnected(userId, session);
        }
        log.info("WebSocket disconnected, sessionId={}, userId={}, status={}", session.getId(), userId, status);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private Long getUserId(WebSocketSession session) {
        Object rawUserId = session.getAttributes().get(ChatHandshakeInterceptor.ATTR_USER_ID);
        if (rawUserId instanceof Long userId) {
            return userId;
        }
        if (rawUserId instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private Object handleAction(Long userId, String action, JsonNode dataNode) {
        return switch (action) {
            case ChatSocketAction.GET_SESSIONS -> chatService.getSessions(userId);
            case ChatSocketAction.GET_HISTORY -> chatService.getChatHistory(
                    userId,
                    readRequiredLong(dataNode, "targetId"),
                    readInt(dataNode, "sessionType", ChatConstants.SESSION_TYPE_SINGLE),
                    readPositiveInt(dataNode, "page", 1, MAX_HISTORY_PAGE_NUMBER),
                    readPositiveInt(dataNode, "size", 50, MAX_HISTORY_PAGE_SIZE)
            );
            case ChatSocketAction.SEND_MESSAGE -> chatService.sendMessage(
                    userId,
                    readRequiredLong(dataNode, "toUserId"),
                    readRequiredText(dataNode, "content"),
                    readInt(dataNode, "msgType", ChatConstants.MESSAGE_TYPE_TEXT),
                    readInt(dataNode, "sessionType", ChatConstants.SESSION_TYPE_SINGLE),
                    readClientMessageId(dataNode),
                    readBoolean(dataNode, "mentionAll", false),
                    readLongList(dataNode, "mentionUserIds")
            );
            case ChatSocketAction.SEND_FILE_MESSAGE -> chatService.sendFileMessage(
                    userId,
                    readRequiredLong(dataNode, "toUserId"),
                    readRequiredLong(dataNode, "fileId"),
                    readInt(dataNode, "msgType", ChatConstants.MESSAGE_TYPE_FILE),
                    readInt(dataNode, "sessionType", ChatConstants.SESSION_TYPE_SINGLE),
                    readClientMessageId(dataNode)
            );
            case ChatSocketAction.MARK_READ -> {
                chatService.markAsRead(
                        userId,
                        readRequiredLong(dataNode, "targetId"),
                        readInt(dataNode, "sessionType", ChatConstants.SESSION_TYPE_SINGLE)
                );
                yield null;
            }
            case ChatSocketAction.RECALL_MESSAGE -> {
                chatService.recallMessage(userId, readRequiredLong(dataNode, "messageId"));
                yield null;
            }
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的聊天指令: " + action);
        };
    }

    private long readRequiredLong(JsonNode dataNode, String fieldName) {
        JsonNode fieldNode = dataNode.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 不能为空");
        }
        if (fieldNode.isNumber()) {
            return fieldNode.longValue();
        }
        if (fieldNode.isTextual()) {
            String rawValue = fieldNode.textValue().trim();
            if (!StringUtils.hasText(rawValue)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 不能为空");
            }
            try {
                return Long.parseLong(rawValue);
            } catch (NumberFormatException exception) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式错误");
            }
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式错误");
    }

    private int readInt(JsonNode dataNode, String fieldName, int defaultValue) {
        JsonNode fieldNode = dataNode.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return defaultValue;
        }
        if (fieldNode.isNumber()) {
            return fieldNode.intValue();
        }
        if (fieldNode.isTextual()) {
            String rawValue = fieldNode.textValue().trim();
            if (!StringUtils.hasText(rawValue)) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(rawValue);
            } catch (NumberFormatException exception) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式错误");
            }
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式错误");
    }

    private int readPositiveInt(JsonNode dataNode, String fieldName, int defaultValue, int maxValue) {
        int value = readInt(dataNode, fieldName, defaultValue);
        if (value < 1) {
            return defaultValue;
        }
        return Math.min(value, maxValue);
    }

    private String readRequiredText(JsonNode dataNode, String fieldName) {
        String value = readText(dataNode, fieldName);
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 不能为空");
        }
        return value;
    }

    private String readText(JsonNode dataNode, String fieldName) {
        JsonNode fieldNode = dataNode.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return "";
        }
        return fieldNode.asText("");
    }

    private String readClientMessageId(JsonNode dataNode) {
        String clientMessageId = readText(dataNode, "clientMessageId").trim();
        if (!StringUtils.hasText(clientMessageId)) {
            return null;
        }
        if (clientMessageId.length() > 64) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "clientMessageId 长度不能超过64");
        }
        return clientMessageId;
    }

    private boolean readBoolean(JsonNode dataNode, String fieldName, boolean defaultValue) {
        JsonNode fieldNode = dataNode.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return defaultValue;
        }
        if (fieldNode.isBoolean()) {
            return fieldNode.booleanValue();
        }
        if (fieldNode.isTextual()) {
            String rawValue = fieldNode.textValue().trim();
            if (!StringUtils.hasText(rawValue)) {
                return defaultValue;
            }
            if ("true".equalsIgnoreCase(rawValue) || "1".equals(rawValue)) {
                return true;
            }
            if ("false".equalsIgnoreCase(rawValue) || "0".equals(rawValue)) {
                return false;
            }
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式错误");
    }

    private List<Long> readLongList(JsonNode dataNode, String fieldName) {
        JsonNode fieldNode = dataNode.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return List.of();
        }
        if (!fieldNode.isArray()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式错误");
        }
        List<Long> values = new ArrayList<>();
        for (JsonNode itemNode : fieldNode) {
            if (itemNode == null || itemNode.isNull()) {
                continue;
            }
            if (itemNode.isNumber()) {
                values.add(itemNode.longValue());
                continue;
            }
            if (itemNode.isTextual()) {
                String rawValue = itemNode.textValue().trim();
                if (!StringUtils.hasText(rawValue)) {
                    continue;
                }
                try {
                    values.add(Long.parseLong(rawValue));
                    continue;
                } catch (NumberFormatException exception) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式错误");
                }
            }
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式错误");
        }
        return values;
    }

    private boolean allowCommand(WebSocketSession session) {
        long now = System.currentTimeMillis();
        Object rawWindowStart = session.getAttributes().get(ATTR_COMMAND_WINDOW_START);
        long windowStart = rawWindowStart instanceof Number number ? number.longValue() : now;
        if (now - windowStart >= COMMAND_RATE_WINDOW_MILLIS) {
            session.getAttributes().put(ATTR_COMMAND_WINDOW_START, now);
            session.getAttributes().put(ATTR_COMMAND_WINDOW_COUNT, 1);
            return true;
        }

        Object rawWindowCount = session.getAttributes().get(ATTR_COMMAND_WINDOW_COUNT);
        int count = rawWindowCount instanceof Number number ? number.intValue() : 0;
        if (count >= COMMAND_RATE_LIMIT) {
            log.warn("WebSocket command rate limited, sessionId={}, userId={}", session.getId(), getUserId(session));
            return false;
        }
        session.getAttributes().put(ATTR_COMMAND_WINDOW_START, windowStart);
        session.getAttributes().put(ATTR_COMMAND_WINDOW_COUNT, count + 1);
        return true;
    }

    private void sendCommandSuccess(WebSocketSession session, String requestId, String action, Object data) {
        sendCommandResult(session, new ChatCommandResultPayload(
                requestId,
                action,
                true,
                ErrorCode.SUCCESS.getCode(),
                ErrorCode.SUCCESS.getMessage(),
                data
        ));
    }

    private void sendCommandError(WebSocketSession session, String requestId, String action, int code, String message) {
        sendCommandResult(session, new ChatCommandResultPayload(
                requestId,
                action,
                false,
                code,
                message,
                null
        ));
    }

    private void sendCommandResult(WebSocketSession session, ChatCommandResultPayload payload) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            String text = objectMapper.writeValueAsString(new ChatRealtimeEvent(ChatEventType.COMMAND_RESULT, payload));
            synchronized (session) {
                session.sendMessage(new TextMessage(text));
            }
        } catch (IOException exception) {
            log.warn("Send websocket command result failed, sessionId={}", session.getId(), exception);
        }
    }
}
