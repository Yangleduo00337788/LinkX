package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

/** {@link ChatEventType#COMMAND_RESULT}：客户端 WS 命令的异步执行结果。 */
// 行注：定义 ChatCommandResultPayload 记录类型
public record ChatCommandResultPayload(
        // 行注：补充当前表达式片段
        String requestId,
        // 行注：补充当前表达式片段
        String action,
        // 行注：补充当前表达式片段
        boolean success,
        // 行注：补充当前表达式片段
        int code,
        // 行注：补充当前表达式片段
        String message,
        // 行注：补充当前表达式片段
        Object data
// 行注：开始当前语句对应的代码块
) {
}  // 行注：结束当前代码块
