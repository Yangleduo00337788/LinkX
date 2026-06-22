package com.linkx.server.module.file.service.impl;

import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.config.LinkxAppProperties;
import com.linkx.server.entity.SysFile;
import com.linkx.server.mapper.ImGroupMemberMapper;
import com.linkx.server.mapper.ImMessageMapper;
import com.linkx.server.mapper.SysFileMapper;
import com.linkx.server.module.file.dto.FileAccessDTO;
import com.linkx.server.module.file.dto.FileDTO;
import com.linkx.server.module.file.service.FileAccessTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private SysFileMapper fileMapper;

    @Mock
    private ImMessageMapper messageMapper;

    @Mock
    private ImGroupMemberMapper groupMemberMapper;

    @Mock
    private FileAccessTicketService fileAccessTicketService;

    @TempDir
    Path tempDir;

    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        LinkxAppProperties properties = new LinkxAppProperties();
        properties.setApiBaseUrl("http://localhost:8080");
        properties.getUpload().setPath(tempDir.toString());
        properties.getUpload().setUrl("http://localhost:8080/uploads/");

        fileService = new FileServiceImpl(
                fileMapper,
                messageMapper,
                groupMemberMapper,
                fileAccessTicketService,
                properties
        );
    }

    @Test
    void should_store_pdf_file_successfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.pdf",
                "application/pdf",
                "%PDF-1.7\nLinkX test".getBytes()
        );
        doAnswer(invocation -> {
            SysFile sysFile = invocation.getArgument(0);
            sysFile.setId(501L);
            return 1;
        }).when(fileMapper).insert(any(SysFile.class));

        FileDTO response = fileService.uploadFile(1001L, file);

        assertEquals(501L, response.getId());
        assertEquals("report.pdf", response.getOriginalName());
        assertEquals("application/pdf", response.getFileType());
        assertTrue(response.getFileUrl().startsWith("http://localhost:8080/uploads/file/"));

        Path storedDirectory = tempDir.resolve("file");
        assertTrue(Files.exists(storedDirectory));
        assertTrue(Files.list(storedDirectory).findFirst().isPresent());
    }

    @Test
    void should_reject_disguised_image_upload_when_header_does_not_match_extension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "plain text but not png".getBytes()
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> fileService.uploadImage(1001L, file));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        verify(fileMapper, never()).insert(any(SysFile.class));
    }

    @Test
    void should_create_access_url_for_owner_file() {
        SysFile sysFile = new SysFile();
        sysFile.setId(601L);
        sysFile.setUserId(1001L);
        sysFile.setFileUrl("http://localhost:8080/uploads/file/report.pdf");
        when(fileMapper.selectOne(any())).thenReturn(sysFile);
        when(fileAccessTicketService.createTicket(601L)).thenReturn("ticket-601");

        FileAccessDTO response = fileService.createAccessUrl(1001L, sysFile.getFileUrl());

        assertNotNull(response);
        assertEquals("http://localhost:8080/api/file/access/ticket-601", response.getAccessUrl());
        verify(fileAccessTicketService).createTicket(601L);
    }

    @Test
    void should_reject_access_url_for_unrelated_user() {
        SysFile sysFile = new SysFile();
        sysFile.setId(602L);
        sysFile.setUserId(1001L);
        sysFile.setFileUrl("http://localhost:8080/uploads/file/private.pdf");
        when(fileMapper.selectOne(any())).thenReturn(sysFile);
        when(messageMapper.selectList(any())).thenReturn(java.util.List.of());

        BusinessException exception = assertThrows(BusinessException.class, () -> fileService.createAccessUrl(2002L, sysFile.getFileUrl()));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(fileAccessTicketService, never()).createTicket(602L);
    }
}
