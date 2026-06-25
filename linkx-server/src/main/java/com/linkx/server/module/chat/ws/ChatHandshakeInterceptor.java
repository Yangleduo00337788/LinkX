package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.http.HttpStatus;  // 行注：引入 HttpStatus 类型
import org.springframework.http.server.ServerHttpRequest;  // 行注：引入 ServerHttpRequest 类型
import org.springframework.http.server.ServerHttpResponse;  // 行注：引入 ServerHttpResponse 类型
import org.springframework.http.server.ServletServerHttpRequest;  // 行注：引入 ServletServerHttpRequest 类型
import org.springframework.stereotype.Component;  // 行注：引入 Component 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型
import org.springframework.web.socket.WebSocketHandler;  // 行注：引入 WebSocketHandler 类型
import org.springframework.web.socket.server.HandshakeInterceptor;  // 行注：引入 HandshakeInterceptor 类型

import java.net.URLDecoder;  // 行注：引入 URLDecoder 类型
import java.nio.charset.StandardCharsets;  // 行注：引入 StandardCharsets 类型
import java.util.Map;  // 行注：引入 Map 类型

/**
 * WebSocket 握手：校验 query 中的短期 ticket，将 userId 写入 session 属性 {@link #ATTR_USER_ID}。
 */
@Component  // 行注：应用 @Component 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 ChatHandshakeInterceptor 类
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATTR_USER_ID = "userId";  // 行注：定义ATTR用户ID常量

    private final ChatWebSocketTicketService chatWebSocketTicketService;  // 行注：注入聊天Web连接票据服务依赖

    /** ticket 无效则拒绝握手（401），有效则写入 userId 供 Handler 使用 */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义前置Handshake方法
    public boolean beforeHandshake(
            // 行注：补充当前表达式片段
            ServerHttpRequest request,
            // 行注：补充当前表达式片段
            ServerHttpResponse response,
            // 行注：补充当前表达式片段
            WebSocketHandler wsHandler,
            // 行注：补充当前表达式片段
            Map<String, Object> attributes
    // 行注：开始当前语句对应的代码块
    ) {
        // 握手阶段唯一关心的认证材料就是 query 里的短时 ticket。
        String ticket = extractTicket(request);  // 行注：初始化票据
        Long userId = chatWebSocketTicketService.consumeTicket(ticket);  // 行注：初始化用户ID
        // 行注：判断是否满足当前条件
        if (userId == null) {
            // ticket 无效、过期或已被消费时直接拒绝握手，避免匿名连接进入消息链路。
            response.setStatusCode(HttpStatus.UNAUTHORIZED);  // 行注：调用设置状态验证码
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // userId 写入 attributes 后，真正的 WebSocket Handler 就能按当前登录人处理消息。
        attributes.put(ATTR_USER_ID, userId);  // 行注：调用put
        return true;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 处理afterHandshake。
     *
     * @param request 当前请求或请求对象
     * @param response HTTP 响应
     * @param wsHandler WebSocket处理
     * @param exception 异常
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义afterHandshake方法
    public void afterHandshake(
            // 行注：补充当前表达式片段
            ServerHttpRequest request,
            // 行注：补充当前表达式片段
            ServerHttpResponse response,
            // 行注：补充当前表达式片段
            WebSocketHandler wsHandler,
            // 行注：补充当前表达式片段
            Exception exception
    // 行注：开始当前语句对应的代码块
    ) {
    }  // 行注：结束当前代码块

    // 行注：定义extract票据方法
    private String extractTicket(ServerHttpRequest request) {
        // 行注：判断是否满足当前条件
        if (request instanceof ServletServerHttpRequest servletRequest) {
            // 优先通过 Servlet API 直接读取参数，这是最稳妥的路径。
            String ticket = servletRequest.getServletRequest().getParameter("ticket");  // 行注：初始化票据
            // 行注：判断是否满足当前条件
            if (StringUtils.hasText(ticket)) {
                return ticket;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块

        // 某些场景下再退回到原始 query 手动解析，保证对底层请求实现的兼容性。
        String query = request.getURI().getRawQuery();  // 行注：初始化查询
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(query)) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：遍历当前集合或范围
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);  // 行注：初始化parts
            // 行注：判断是否满足当前条件
            if (parts.length == 2 && "ticket".equals(parts[0])) {
                // ticket 走 URL 传递时可能被编码，这里需要显式解码再交给 Redis 校验。
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);  // 行注：返回处理结果
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        return null;  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
