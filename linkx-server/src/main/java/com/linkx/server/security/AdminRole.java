package com.linkx.server.security;

/**
 * 管理后台角色（对应 sys_admin.role 与 Spring ROLE_*）。
 */
public final class AdminRole {

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String OPERATOR = "OPERATOR";
    public static final String AUDITOR = "AUDITOR";
    public static final String VIEWER = "VIEWER";

    private AdminRole() {
    }
}