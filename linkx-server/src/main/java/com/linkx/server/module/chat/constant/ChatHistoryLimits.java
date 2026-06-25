package com.linkx.server.module.chat.constant;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.constant

/** 聊天历史分页上限，防止单次拉取过大。 */
// 行注：定义 ChatHistoryLimits 类
public final class ChatHistoryLimits {

    /** REST 默认每页条数 */
    public static final int DEFAULT_PAGE_SIZE = 50;  // 行注：定义默认分页大小常量
    /** 单页最大条数 */
    public static final int MAX_PAGE_SIZE = 200;  // 行注：定义最大分页大小常量
    /** 允许的最大页码（防止深分页拖垮库） */
    public static final int MAX_PAGE_NUMBER = 100;  // 行注：定义最大分页NUMBER常量

    // 行注：定义聊天历史限制配置方法
    private ChatHistoryLimits() {
    }  // 行注：结束当前代码块

    /** 将页码限制在 [1, {@link #MAX_PAGE_NUMBER}] */
    // 行注：定义clamp分页方法
    public static int clampPage(int page) {
        return Math.min(Math.max(page, 1), MAX_PAGE_NUMBER);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 将每页大小限制在 [1, {@link #MAX_PAGE_SIZE}] */
    // 行注：定义clamp大小方法
    public static int clampSize(int size) {
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
