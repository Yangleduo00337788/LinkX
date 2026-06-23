package com.linkx.server.module.file.controller;

import com.linkx.server.common.AuditLogService;
import com.linkx.server.common.GlobalExceptionHandler;
import com.linkx.server.entity.SysFile;
import com.linkx.server.module.file.service.FileAccessTicketService;
import com.linkx.server.module.file.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileService fileService;

    @Mock
    private FileAccessTicketService fileAccessTicketService;

    @Mock
    private AuditLogService auditLogService;

    @TempDir
    Path tempDir;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FileController controller = new FileController(fileService, fileAccessTicketService, auditLogService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    private static RequestPostProcessor authenticatedUser(long userId) {
        return request -> {
            User user = new User(String.valueOf(userId), "n/a", java.util.List.of());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            return request;
        };
    }

    @Test
    void should_return_not_found_when_access_ticket_is_invalid() throws Exception {
        when(fileAccessTicketService.consumeFile("missing-ticket", 1001L)).thenReturn(null);

        mockMvc.perform(get("/api/file/access/missing-ticket")
                        .with(authenticatedUser(1001L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_return_not_found_when_ticket_user_mismatch() throws Exception {
        when(fileAccessTicketService.consumeFile("other-user-ticket", 1001L)).thenReturn(null);

        mockMvc.perform(get("/api/file/access/other-user-ticket")
                        .with(authenticatedUser(1001L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_return_not_found_when_file_resource_is_missing() throws Exception {
        SysFile sysFile = new SysFile();
        sysFile.setId(101L);
        sysFile.setUserId(1001L);
        sysFile.setOriginalName("report.pdf");
        sysFile.setFileType("application/pdf");
        sysFile.setFilePath(tempDir.resolve("missing-report.pdf").toString());

        when(fileAccessTicketService.consumeFile("missing-file-ticket", 1001L)).thenReturn(sysFile);

        mockMvc.perform(get("/api/file/access/missing-file-ticket")
                        .with(authenticatedUser(1001L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_return_attachment_response_for_non_image_file() throws Exception {
        Path pdfFile = tempDir.resolve("report.pdf");
        Files.writeString(pdfFile, "pdf-content");

        SysFile sysFile = new SysFile();
        sysFile.setId(102L);
        sysFile.setUserId(1001L);
        sysFile.setOriginalName("report.pdf");
        sysFile.setFileType("application/pdf");
        sysFile.setFilePath(pdfFile.toString());

        when(fileAccessTicketService.consumeFile("pdf-ticket", 1001L)).thenReturn(sysFile);

        mockMvc.perform(get("/api/file/access/pdf-ticket")
                        .with(authenticatedUser(1001L)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.pdf\""))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().string("pdf-content"));
    }

    @Test
    void should_return_inline_response_for_image_file() throws Exception {
        Path imageFile = tempDir.resolve("avatar.png");
        Files.writeString(imageFile, "png-content");

        SysFile sysFile = new SysFile();
        sysFile.setId(103L);
        sysFile.setUserId(1001L);
        sysFile.setOriginalName("avatar.png");
        sysFile.setFileType("image/png");
        sysFile.setFilePath(imageFile.toString());

        when(fileAccessTicketService.consumeFile("image-ticket", 1001L)).thenReturn(sysFile);

        mockMvc.perform(get("/api/file/access/image-ticket")
                        .with(authenticatedUser(1001L)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"avatar.png\""))
                .andExpect(content().contentType("image/png"))
                .andExpect(content().string("png-content"));
    }
}