package com.linkx.server.module.chat.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkx.server.module.chat.dto.MessageDTO;
import com.linkx.server.module.chat.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketHandlerTest {

    @Mock
    private ChatPresenceService chatPresenceService;

    @Mock
    private ChatService chatService;

    @Mock
    private WebSocketSession session;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private ChatWebSocketHandler handler;
    private Map<String, Object> attributes;
    private List<TextMessage> outboundMessages;

    @BeforeEach
    void setUp() throws Exception {
        handler = new ChatWebSocketHandler(chatPresenceService, chatService, objectMapper);
        attributes = new HashMap<>();
        attributes.put(ChatHandshakeInterceptor.ATTR_USER_ID, 1L);
        outboundMessages = new ArrayList<>();

        lenient().when(session.getAttributes()).thenReturn(attributes);
        lenient().when(session.getId()).thenReturn("ws-test-session");
        lenient().when(session.isOpen()).thenReturn(true);
        lenient().doAnswer(invocation -> {
            outboundMessages.add(invocation.getArgument(0));
            return null;
        }).when(session).sendMessage(any(TextMessage.class));
    }

    @Test
    void should_reply_with_pong_when_receiving_ping() throws Exception {
        handler.handleTextMessage(session, new TextMessage("PING"));

        assertEquals(1, outboundMessages.size());
        assertEquals("PONG", outboundMessages.get(0).getPayload());
        verifyNoInteractions(chatService);
    }

    @Test
    void should_forward_client_message_id_and_return_command_result() throws Exception {
        MessageDTO message = new MessageDTO();
        message.setId(99L);
        message.setClientMessageId("client-msg-1");
        message.setContent("hello");
        message.setMsgType(0);
        message.setStatus(0);
        message.setCreateTime(LocalDateTime.of(2026, 6, 16, 22, 0));

        when(chatService.sendMessage(1L, 2L, "hello", 0, 1, "client-msg-1", false, List.of())).thenReturn(message);

        handler.handleTextMessage(session, new TextMessage("""
                {
                  "requestId": "req-1",
                  "action": "SEND_MESSAGE",
                  "data": {
                    "toUserId": 2,
                    "content": "hello",
                    "msgType": 0,
                    "sessionType": 1,
                    "clientMessageId": "client-msg-1"
                  }
                }
                """));

        verify(chatService).sendMessage(1L, 2L, "hello", 0, 1, "client-msg-1", false, List.of());
        assertEquals(1, outboundMessages.size());

        JsonNode payload = objectMapper.readTree(outboundMessages.get(0).getPayload());
        assertEquals(ChatEventType.COMMAND_RESULT, payload.path("type").asText());
        assertTrue(payload.path("data").path("success").asBoolean());
        assertEquals("req-1", payload.path("data").path("requestId").asText());
        assertEquals("client-msg-1", payload.path("data").path("data").path("clientMessageId").asText());
    }

    @Test
    void should_forward_group_mentions_when_sending_message() throws Exception {
        MessageDTO message = new MessageDTO();
        message.setId(100L);
        message.setContent("@张三 开会");
        message.setMsgType(0);
        message.setMentionAll(true);
        message.setMentionUserIds(List.of(3L, 4L));
        message.setCreateTime(LocalDateTime.of(2026, 6, 17, 10, 30));

        when(chatService.sendMessage(1L, 200L, "@张三 开会", 0, 2, "client-msg-2", true, List.of(3L, 4L))).thenReturn(message);

        handler.handleTextMessage(session, new TextMessage("""
                {
                  "requestId": "req-2",
                  "action": "SEND_MESSAGE",
                  "data": {
                    "toUserId": 200,
                    "content": "@张三 开会",
                    "msgType": 0,
                    "sessionType": 2,
                    "clientMessageId": "client-msg-2",
                    "mentionAll": true,
                    "mentionUserIds": [3, "4"]
                  }
                }
                """));

        verify(chatService).sendMessage(1L, 200L, "@张三 开会", 0, 2, "client-msg-2", true, List.of(3L, 4L));
        JsonNode payload = objectMapper.readTree(outboundMessages.get(0).getPayload());
        assertTrue(payload.path("data").path("success").asBoolean());
        assertTrue(payload.path("data").path("data").path("mentionAll").asBoolean());
        assertEquals(2, payload.path("data").path("data").path("mentionUserIds").size());
    }

    @Test
    void should_rate_limit_after_too_many_commands_in_same_window() throws Exception {
        when(chatService.getSessions(1L)).thenReturn(List.of());

        for (int index = 1; index <= 121; index++) {
            handler.handleTextMessage(session, new TextMessage("""
                    {
                      "requestId": "req-%d",
                      "action": "GET_SESSIONS",
                      "data": {}
                    }
                    """.formatted(index)));
        }

        assertEquals(121, outboundMessages.size());

        JsonNode lastPayload = objectMapper.readTree(outboundMessages.get(outboundMessages.size() - 1).getPayload());
        assertEquals(ChatEventType.COMMAND_RESULT, lastPayload.path("type").asText());
        assertFalse(lastPayload.path("data").path("success").asBoolean());
        assertEquals(429, lastPayload.path("data").path("code").asInt());
        verify(chatService, times(120)).getSessions(1L);
    }
}
