package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

import com.fasterxml.jackson.core.JsonProcessingException;  // 行注：引入 JsonProcessingException 类型
import com.fasterxml.jackson.databind.JsonNode;  // 行注：引入 JsonNode 类型
import com.fasterxml.jackson.databind.ObjectMapper;  // 行注：引入 ObjectMapper 类型
import com.linkx.server.common.BusinessException;  // 行注：引入 BusinessException 类型
import com.linkx.server.common.ErrorCode;  // 行注：引入 ErrorCode 类型
import com.linkx.server.module.chat.constant.ChatConstants;  // 行注：引入 ChatConstants 类型
import com.linkx.server.module.chat.constant.ChatHistoryLimits;  // 行注：引入 ChatHistoryLimits 类型
import com.linkx.server.module.chat.service.ChatService;  // 行注：引入 ChatService 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import lombok.extern.slf4j.Slf4j;  // 行注：引入 Slf4j 类型
import org.springframework.stereotype.Component;  // 行注：引入 Component 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型
import org.springframework.web.socket.CloseStatus;  // 行注：引入 CloseStatus 类型
import org.springframework.web.socket.PingMessage;  // 行注：引入 PingMessage 类型
import org.springframework.web.socket.PongMessage;  // 行注：引入 PongMessage 类型
import org.springframework.web.socket.TextMessage;  // 行注：引入 TextMessage 类型
import org.springframework.web.socket.WebSocketSession;  // 行注：引入 WebSocketSession 类型
import org.springframework.web.socket.handler.TextWebSocketHandler;  // 行注：引入 TextWebSocketHandler 类型

import java.io.IOException;  // 行注：引入 IOException 类型
import java.util.ArrayList;  // 行注：引入 ArrayList 类型
import java.util.List;  // 行注：引入 List 类型

/**
 * 聊天 WebSocket 文本帧处理：心跳、客户端命令（发消息/已读等）与下行事件推送入口。
 * <p>连接建立后 userId 由握手拦截器写入 session 属性。</p>
 */
@Slf4j  // 行注：应用 @Slf4j 注解
@Component  // 行注：应用 @Component 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 ChatWebSocketHandler 类
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final int MAX_TEXT_PAYLOAD_LENGTH = 16 * 1024;  // 行注：定义最大文本载荷长度常量
    private static final int COMMAND_RATE_WINDOW_MILLIS = 10_000;  // 行注：定义COMMANDRATE窗口MILLIS常量
    private static final int COMMAND_RATE_LIMIT = 120;  // 行注：定义COMMANDRATE限制常量

    private static final String ATTR_COMMAND_WINDOW_START = "chat:commandWindowStart";  // 行注：定义ATTRCOMMAND窗口START常量
    private static final String ATTR_COMMAND_WINDOW_COUNT = "chat:commandWindowCount";  // 行注：定义ATTRCOMMAND窗口数量常量

    private final ChatPresenceService chatPresenceService;  // 行注：注入聊天在线状态服务依赖
    private final ChatService chatService;  // 行注：注入聊天服务依赖
    private final ObjectMapper objectMapper;  // 行注：注入objectMapper依赖

    /** 握手成功后注册在线状态并向本端推送 CONNECTED 事件 */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义afterConnectionEstablished方法
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);  // 行注：初始化用户ID
        // 行注：判断是否满足当前条件
        if (userId == null) {
            log.warn("WebSocket connection rejected, sessionId={}, reason=missing_user_identity", session.getId());  // 行注：执行初始化操作
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing user identity"));  // 行注：调用close
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        chatPresenceService.onConnected(userId, session);  // 行注：调用on已连接
        log.info("WebSocket connected, sessionId={}, userId={}", session.getId(), userId);  // 行注：执行初始化操作
    }  // 行注：结束当前代码块

    /**
     * 处理 PING/PONG、JSON 命令帧；每连接有简单滑动窗口限流。
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义handle文本消息方法
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();  // 行注：初始化载荷
        // 行注：判断是否满足当前条件
        if ("PING".equalsIgnoreCase(payload)) {
            // 使用轻量文本心跳维持连接活性，避免进入完整 JSON 解析链路。
            session.sendMessage(new TextMessage("PONG"));  // 行注：调用发送消息
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 单帧大小先做硬限制，避免异常大包拖垮内存或 JSON 解析。
        // 行注：判断是否满足当前条件
        if (payload != null && payload.length() > MAX_TEXT_PAYLOAD_LENGTH) {
            sendCommandError(session, "", "", ErrorCode.BAD_REQUEST.getCode(), "WebSocket 消息过大");  // 行注：调用获取验证码
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        Long userId = getUserId(session);  // 行注：初始化用户ID
        // 行注：判断是否满足当前条件
        if (userId == null) {
            sendCommandError(session, "", "", ErrorCode.UNAUTHORIZED.getCode(), "用户未认证");  // 行注：调用获取验证码
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 这里做的是连接级滑动窗口限流，防止单连接短时间内疯狂刷指令。
        // 行注：判断是否满足当前条件
        if (!allowCommand(session)) {
            sendCommandError(session, "", "", ErrorCode.TOO_MANY_REQUESTS.getCode(), "请求过于频繁，请稍后重试");  // 行注：调用获取验证码
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        JsonNode requestNode;  // 行注：完成当前语句
        // 行注：尝试执行可能失败的逻辑
        try {
            requestNode = objectMapper.readTree(payload);  // 行注：初始化请求Node
        // 行注：执行当前方法调用
        } catch (JsonProcessingException exception) {
            sendCommandError(session, "", "", ErrorCode.BAD_REQUEST.getCode(), "WebSocket 消息格式错误");  // 行注：调用获取验证码
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        String requestId = readText(requestNode, "requestId");  // 行注：初始化请求ID
        String action = readText(requestNode, "action");  // 行注：初始化操作
        JsonNode dataNode = requestNode.path("data");  // 行注：初始化dataNode
        // requestId 是前端建立 request-response 对应关系的关键字段，不能为空。
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(requestId) || !StringUtils.hasText(action)) {
            sendCommandError(session, requestId, action, ErrorCode.BAD_REQUEST.getCode(), "请求缺少 requestId 或 action");  // 行注：调用获取验证码
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        // 行注：尝试执行可能失败的逻辑
        try {
            Object result = handleAction(userId, action, dataNode);  // 行注：初始化结果
            // 行注：补充当前表达式片段
            log.info("WebSocket command handled, sessionId={}, userId={}, requestId={}, action={}, success=true",
                    session.getId(), userId, requestId, action);  // 行注：调用获取ID
            sendCommandSuccess(session, requestId, action, result);  // 行注：调用发送CommandSuccess
        // 行注：执行当前方法调用
        } catch (BusinessException exception) {
            // 行注：补充当前表达式片段
            log.warn("WebSocket command rejected, sessionId={}, userId={}, requestId={}, action={}, code={}, message={}",
                    session.getId(), userId, requestId, action, exception.getErrorCode().getCode(), exception.getMessage());  // 行注：调用获取ID
            // 行注：补充当前表达式片段
            sendCommandError(
                    // 行注：补充当前表达式片段
                    session,
                    // 行注：补充当前表达式片段
                    requestId,
                    // 行注：补充当前表达式片段
                    action,
                    // 行注：调用获取错误验证码
                    exception.getErrorCode().getCode(),
                    // 行注：调用获取消息
                    exception.getMessage()
            );  // 行注：结束当前参数配置
        // 行注：执行当前方法调用
        } catch (Exception exception) {
            log.error("Handle websocket command failed, action={}, sessionId={}", action, session.getId(), exception);  // 行注：执行初始化操作
            sendCommandError(session, requestId, action, ErrorCode.INTERNAL_ERROR.getCode(), "服务器内部错误");  // 行注：调用获取验证码
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /**
     * 处理Pong消息。
     *
     * @param session 会话
     * @param message 附言内容
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义handlePong消息方法
    protected void handlePongMessage(WebSocketSession session, PongMessage message) {
        log.debug("Received pong, sessionId={}", session.getId());  // 行注：执行初始化操作
    }  // 行注：结束当前代码块

    /**
     * 处理TransportError。
     *
     * @param session 会话
     * @param exception 异常
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义handleTransport错误方法
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("WebSocket transport error, sessionId={}", session.getId(), exception);  // 行注：执行初始化操作
        session.close(CloseStatus.SERVER_ERROR);  // 行注：调用close
    }  // 行注：结束当前代码块

    /** 连接关闭时更新在线状态并通知好友 */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义afterConnectionClosed方法
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserId(session);  // 行注：初始化用户ID
        // 行注：判断是否满足当前条件
        if (userId != null) {
            chatPresenceService.onDisconnected(userId, session);  // 行注：调用onDisconnected
        }  // 行注：结束当前代码块
        log.info("WebSocket disconnected, sessionId={}, userId={}, status={}", session.getId(), userId, status);  // 行注：执行初始化操作
    }  // 行注：结束当前代码块

    /**
     * 处理supportsPartialMessages。
     *
     * @return 是否满足条件
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义supportsPartial消息方法
    public boolean supportsPartialMessages() {
        return false;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义获取用户ID方法
    private Long getUserId(WebSocketSession session) {
        // userId 在握手阶段写入 attributes，这里统一做 Number 到 Long 的兼容读取。
        Object rawUserId = session.getAttributes().get(ChatHandshakeInterceptor.ATTR_USER_ID);  // 行注：初始化raw用户ID
        // 行注：判断是否满足当前条件
        if (rawUserId instanceof Long userId) {
            return userId;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (rawUserId instanceof Number number) {
            return number.longValue();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return null;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义handle操作方法
    private Object handleAction(Long userId, String action, JsonNode dataNode) {
        // WebSocket 只负责协议层分发，真正业务校验与持久化全部下沉到 ChatService。
        return switch (action) {  // 行注：返回处理结果
            case ChatSocketAction.GET_SESSIONS -> chatService.getSessions(userId);  // 行注：处理当前分支
            // 行注：处理当前分支
            case ChatSocketAction.GET_HISTORY -> chatService.getChatHistory(
                    // 行注：补充当前表达式片段
                    userId,
                    // 行注：调用已读必填Long
                    readRequiredLong(dataNode, "targetId"),
                    // 行注：调用已读Int
                    readInt(dataNode, "sessionType", ChatConstants.SESSION_TYPE_SINGLE),
                    // 行注：调用已读PositiveInt
                    readPositiveInt(dataNode, "page", 1, ChatHistoryLimits.MAX_PAGE_NUMBER),
                    // 行注：调用已读PositiveInt
                    readPositiveInt(dataNode, "size", ChatHistoryLimits.DEFAULT_PAGE_SIZE, ChatHistoryLimits.MAX_PAGE_SIZE)
            );  // 行注：结束当前参数配置
            // 行注：处理当前分支
            case ChatSocketAction.SEND_MESSAGE -> chatService.sendMessage(
                    // 行注：补充当前表达式片段
                    userId,
                    // 行注：调用已读必填Long
                    readRequiredLong(dataNode, "toUserId"),
                    // 行注：调用已读必填文本
                    readRequiredText(dataNode, "content"),
                    // 行注：调用已读Int
                    readInt(dataNode, "msgType", ChatConstants.MESSAGE_TYPE_TEXT),
                    // 行注：调用已读Int
                    readInt(dataNode, "sessionType", ChatConstants.SESSION_TYPE_SINGLE),
                    // 行注：调用已读客户端消息ID
                    readClientMessageId(dataNode),
                    // 行注：调用已读Boolean
                    readBoolean(dataNode, "mentionAll", false),
                    // 行注：调用已读Long列表
                    readLongList(dataNode, "mentionUserIds")
            );  // 行注：结束当前参数配置
            // 行注：处理当前分支
            case ChatSocketAction.SEND_FILE_MESSAGE -> chatService.sendFileMessage(
                    // 行注：补充当前表达式片段
                    userId,
                    // 行注：调用已读必填Long
                    readRequiredLong(dataNode, "toUserId"),
                    // 行注：调用已读必填Long
                    readRequiredLong(dataNode, "fileId"),
                    // 行注：调用已读Int
                    readInt(dataNode, "msgType", ChatConstants.MESSAGE_TYPE_FILE),
                    // 行注：调用已读Int
                    readInt(dataNode, "sessionType", ChatConstants.SESSION_TYPE_SINGLE),
                    // 行注：调用已读客户端消息ID
                    readClientMessageId(dataNode)
            );  // 行注：结束当前参数配置
            // 行注：处理当前分支
            case ChatSocketAction.MARK_READ -> {
                // 行注：补充当前表达式片段
                chatService.markAsRead(
                        // 行注：补充当前表达式片段
                        userId,
                        // 行注：调用已读必填Long
                        readRequiredLong(dataNode, "targetId"),
                        // 行注：调用已读Int
                        readInt(dataNode, "sessionType", ChatConstants.SESSION_TYPE_SINGLE)
                );  // 行注：结束当前参数配置
                yield null;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            // 行注：处理当前分支
            case ChatSocketAction.RECALL_MESSAGE -> {
                chatService.recallMessage(userId, readRequiredLong(dataNode, "messageId"));  // 行注：调用recall消息
                yield null;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的聊天指令: " + action);  // 行注：处理默认分支
        };  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义已读必填Long方法
    private long readRequiredLong(JsonNode dataNode, String fieldName) {
        JsonNode fieldNode = dataNode.get(fieldName);  // 行注：初始化字段Node
        // 行注：判断是否满足当前条件
        if (fieldNode == null || fieldNode.isNull()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 不能为空");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (fieldNode.isNumber()) {
            return fieldNode.longValue();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (fieldNode.isTextual()) {
            String rawValue = fieldNode.textValue().trim();  // 行注：初始化raw值
            // 行注：判断是否满足当前条件
            if (!StringUtils.hasText(rawValue)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 不能为空");  // 行注：抛出异常并中断当前流程
            }  // 行注：结束当前代码块
            // 行注：尝试执行可能失败的逻辑
            try {
                return Long.parseLong(rawValue);  // 行注：返回处理结果
            // 行注：执行当前方法调用
            } catch (NumberFormatException exception) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式错误");  // 行注：抛出异常并中断当前流程
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式错误");  // 行注：抛出异常并中断当前流程
    }  // 行注：结束当前代码块

    // 行注：定义已读Int方法
    private int readInt(JsonNode dataNode, String fieldName, int defaultValue) {
        JsonNode fieldNode = dataNode.get(fieldName);  // 行注：初始化字段Node
        // 行注：判断是否满足当前条件
        if (fieldNode == null || fieldNode.isNull()) {
            return defaultValue;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (fieldNode.isNumber()) {
            return fieldNode.intValue();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (fieldNode.isTextual()) {
            String rawValue = fieldNode.textValue().trim();  // 行注：初始化raw值
            // 行注：判断是否满足当前条件
            if (!StringUtils.hasText(rawValue)) {
                return defaultValue;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            // 行注：尝试执行可能失败的逻辑
            try {
                return Integer.parseInt(rawValue);  // 行注：返回处理结果
            // 行注：执行当前方法调用
            } catch (NumberFormatException exception) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式错误");  // 行注：抛出异常并中断当前流程
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式错误");  // 行注：抛出异常并中断当前流程
    }  // 行注：结束当前代码块

    // 行注：定义已读PositiveInt方法
    private int readPositiveInt(JsonNode dataNode, String fieldName, int defaultValue, int maxValue) {
        int value = readInt(dataNode, fieldName, defaultValue);  // 行注：初始化值
        // 行注：判断是否满足当前条件
        if (value < 1) {
            return defaultValue;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return Math.min(value, maxValue);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义已读必填文本方法
    private String readRequiredText(JsonNode dataNode, String fieldName) {
        String value = readText(dataNode, fieldName);  // 行注：初始化值
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 不能为空");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return value;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义已读文本方法
    private String readText(JsonNode dataNode, String fieldName) {
        JsonNode fieldNode = dataNode.get(fieldName);  // 行注：初始化字段Node
        // 行注：判断是否满足当前条件
        if (fieldNode == null || fieldNode.isNull()) {
            return "";  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return fieldNode.asText("");  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义已读客户端消息ID方法
    private String readClientMessageId(JsonNode dataNode) {
        String clientMessageId = readText(dataNode, "clientMessageId").trim();  // 行注：初始化客户端消息ID
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(clientMessageId)) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 限制 clientMessageId 长度，既保护数据库唯一索引，也避免异常请求膨胀。
        // 行注：判断是否满足当前条件
        if (clientMessageId.length() > 64) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "clientMessageId 长度不能超过64");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return clientMessageId;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义已读Boolean方法
    private boolean readBoolean(JsonNode dataNode, String fieldName, boolean defaultValue) {
        JsonNode fieldNode = dataNode.get(fieldName);  // 行注：初始化字段Node
        // 行注：判断是否满足当前条件
        if (fieldNode == null || fieldNode.isNull()) {
            return defaultValue;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (fieldNode.isBoolean()) {
            return fieldNode.booleanValue();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (fieldNode.isTextual()) {
            String rawValue = fieldNode.textValue().trim();  // 行注：初始化raw值
            // 行注：判断是否满足当前条件
            if (!StringUtils.hasText(rawValue)) {
                return defaultValue;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if ("true".equalsIgnoreCase(rawValue) || "1".equals(rawValue)) {
                return true;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if ("false".equalsIgnoreCase(rawValue) || "0".equals(rawValue)) {
                return false;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式错误");  // 行注：抛出异常并中断当前流程
    }  // 行注：结束当前代码块

    // 行注：定义已读Long列表方法
    private List<Long> readLongList(JsonNode dataNode, String fieldName) {
        JsonNode fieldNode = dataNode.get(fieldName);  // 行注：初始化字段Node
        // 行注：判断是否满足当前条件
        if (fieldNode == null || fieldNode.isNull()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (!fieldNode.isArray()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式错误");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        List<Long> values = new ArrayList<>();  // 行注：初始化values
        // 行注：遍历当前集合或范围
        for (JsonNode itemNode : fieldNode) {
            // 行注：判断是否满足当前条件
            if (itemNode == null || itemNode.isNull()) {
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (itemNode.isNumber()) {
                values.add(itemNode.longValue());  // 行注：调用添加
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (itemNode.isTextual()) {
                String rawValue = itemNode.textValue().trim();  // 行注：初始化raw值
                // 行注：判断是否满足当前条件
                if (!StringUtils.hasText(rawValue)) {
                    continue;  // 行注：完成当前语句
                }  // 行注：结束当前代码块
                // 行注：尝试执行可能失败的逻辑
                try {
                    values.add(Long.parseLong(rawValue));  // 行注：调用添加
                    continue;  // 行注：完成当前语句
                // 行注：执行当前方法调用
                } catch (NumberFormatException exception) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式错误");  // 行注：抛出异常并中断当前流程
                }  // 行注：结束当前代码块
            }  // 行注：结束当前代码块
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式错误");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return values;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义allowCommand方法
    private boolean allowCommand(WebSocketSession session) {
        long now = System.currentTimeMillis();  // 行注：初始化now
        Object rawWindowStart = session.getAttributes().get(ATTR_COMMAND_WINDOW_START);  // 行注：初始化raw窗口Start
        long windowStart = rawWindowStart instanceof Number number ? number.longValue() : now;  // 行注：初始化窗口Start
        // 行注：判断是否满足当前条件
        if (now - windowStart >= COMMAND_RATE_WINDOW_MILLIS) {
            // 窗口过期后重置计数，新窗口中的第一条命令直接放行。
            session.getAttributes().put(ATTR_COMMAND_WINDOW_START, now);  // 行注：调用获取Attributes
            session.getAttributes().put(ATTR_COMMAND_WINDOW_COUNT, 1);  // 行注：调用获取Attributes
            return true;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        Object rawWindowCount = session.getAttributes().get(ATTR_COMMAND_WINDOW_COUNT);  // 行注：初始化raw窗口数量
        int count = rawWindowCount instanceof Number number ? number.intValue() : 0;  // 行注：初始化数量
        // 行注：判断是否满足当前条件
        if (count >= COMMAND_RATE_LIMIT) {
            log.warn("WebSocket command rate limited, sessionId={}, userId={}", session.getId(), getUserId(session));  // 行注：执行初始化操作
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        session.getAttributes().put(ATTR_COMMAND_WINDOW_START, windowStart);  // 行注：调用获取Attributes
        session.getAttributes().put(ATTR_COMMAND_WINDOW_COUNT, count + 1);  // 行注：调用获取Attributes
        return true;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义发送CommandSuccess方法
    private void sendCommandSuccess(WebSocketSession session, String requestId, String action, Object data) {
        // 行注：补充当前表达式片段
        sendCommandResult(session, new ChatCommandResultPayload(
                // 行注：补充当前表达式片段
                requestId,
                // 行注：补充当前表达式片段
                action,
                // 行注：补充当前表达式片段
                true,
                // 行注：调用获取验证码
                ErrorCode.SUCCESS.getCode(),
                // 行注：调用获取消息
                ErrorCode.SUCCESS.getMessage(),
                // 行注：继续基于 data 配置处理流程
                data
        ));  // 行注：完成当前语句
    }  // 行注：结束当前代码块

    // 行注：定义发送Command错误方法
    private void sendCommandError(WebSocketSession session, String requestId, String action, int code, String message) {
        // 行注：补充当前表达式片段
        sendCommandResult(session, new ChatCommandResultPayload(
                // 行注：补充当前表达式片段
                requestId,
                // 行注：补充当前表达式片段
                action,
                // 行注：补充当前表达式片段
                false,
                // 行注：补充当前表达式片段
                code,
                // 行注：补充当前表达式片段
                message,
                // 行注：继续基于 null 配置处理流程
                null
        ));  // 行注：完成当前语句
    }  // 行注：结束当前代码块

    // 行注：定义发送Command结果方法
    private void sendCommandResult(WebSocketSession session, ChatCommandResultPayload payload) {
        // 行注：判断是否满足当前条件
        if (session == null || !session.isOpen()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：尝试执行可能失败的逻辑
        try {
            // 所有命令结果统一包成 COMMAND_RESULT 事件，前端只需实现一套回包处理逻辑。
            String text = objectMapper.writeValueAsString(new ChatRealtimeEvent(ChatEventType.COMMAND_RESULT, payload));  // 行注：初始化文本
            // 行注：调用synchronized
            synchronized (session) {
                session.sendMessage(new TextMessage(text));  // 行注：调用发送消息
            }  // 行注：结束当前代码块
        // 行注：执行当前方法调用
        } catch (IOException exception) {
            log.warn("Send websocket command result failed, sessionId={}", session.getId(), exception);  // 行注：执行初始化操作
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
