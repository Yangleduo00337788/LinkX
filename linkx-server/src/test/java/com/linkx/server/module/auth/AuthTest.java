package com.linkx.server.module.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.config.SchemaCompatibilityInitializer;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.auth.dto.LoginRequest;
import com.linkx.server.module.auth.dto.RefreshTokenRequest;
import com.linkx.server.module.auth.dto.RegisterRequest;
import com.linkx.server.module.auth.service.AuthSecurityGuard;
import com.linkx.server.module.auth.service.RefreshTokenSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:linkx_auth_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:auth-test-schema.sql",
        "linkx.jwt.secret=TEST_LINKX_JWT_SECRET_FOR_AUTH_CASES_2026",
        "linkx.jwt.expiration=86400000",
        "linkx.jwt.refresh-expiration=604800000"
})
@AutoConfigureMockMvc
class AuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @MockBean
    private AuthSecurityGuard authSecurityGuard;

    @MockBean
    private RefreshTokenSessionService refreshTokenSessionService;

    @MockBean
    private SchemaCompatibilityInitializer schemaCompatibilityInitializer;

    @BeforeEach
    void setUp() {
        when(authSecurityGuard.resolveClientIp(any())).thenReturn("127.0.0.1");
        when(refreshTokenSessionService.matchesActiveToken(anyLong(), anyString())).thenReturn(false);
    }

    private String uniqueUsername(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "@example.com";
    }

    private void register(RegisterRequest request) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private SysUser findUserByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return sysUserMapper.selectOne(wrapper);
    }

    @Test
    void should_register_successfully() throws Exception {
        String username = uniqueUsername("testuser");
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setNickname("Test User");
        request.setPassword("password123");

        register(request);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest(username, "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value(username));
    }

    @Test
    void should_login_successfully() throws Exception {
        String username = uniqueUsername("loginuser");
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(username);
        registerRequest.setNickname("Login User");
        registerRequest.setPassword("password123");
        register(registerRequest);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest(username, "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void should_fail_with_wrong_password() throws Exception {
        String username = uniqueUsername("wrongpwduser");
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(username);
        registerRequest.setNickname("Wrong Pwd User");
        registerRequest.setPassword("password123");
        register(registerRequest);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest(username, "wrongpassword"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1007));
    }

    @Test
    void should_refresh_token_successfully() throws Exception {
        String username = uniqueUsername("refreshuser");
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(username);
        registerRequest.setNickname("Refresh User");
        registerRequest.setPassword("password123");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String registerBody = registerResult.getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(registerBody).path("data").path("refreshToken").asText();
        when(refreshTokenSessionService.matchesActiveToken(anyLong(), eq(refreshToken))).thenReturn(true);

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value(username));
    }

    @Test
    void should_fail_when_refresh_token_is_invalid() throws Exception {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("invalid-refresh-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2001));
    }

    @Test
    void should_fail_when_username_already_exists() throws Exception {
        String username = uniqueUsername("duplicateuser");

        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setNickname("Duplicate User");
        request.setPassword("password123");
        register(request);

        RegisterRequest duplicateRequest = new RegisterRequest();
        duplicateRequest.setUsername(username);
        duplicateRequest.setNickname("Duplicate User 2");
        duplicateRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1002));
    }

    @Test
    void should_fail_when_email_already_exists() throws Exception {
        String email = uniqueEmail("duplicateemail");

        RegisterRequest request = new RegisterRequest();
        request.setUsername(uniqueUsername("emailuser"));
        request.setNickname("Email User");
        request.setPassword("password123");
        request.setEmail(email);
        register(request);

        RegisterRequest duplicateRequest = new RegisterRequest();
        duplicateRequest.setUsername(uniqueUsername("emailuser"));
        duplicateRequest.setNickname("Email User 2");
        duplicateRequest.setPassword("password123");
        duplicateRequest.setEmail(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1004));
    }

    @Test
    void should_fail_when_user_is_disabled() throws Exception {
        String username = uniqueUsername("disableduser");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(username);
        registerRequest.setNickname("Disabled User");
        registerRequest.setPassword("password123");
        register(registerRequest);

        SysUser user = findUserByUsername(username);
        user.setStatus(0);
        sysUserMapper.updateById(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest(username, "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1006));
    }

    private LoginRequest loginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}
