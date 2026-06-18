package com.linkx.server.module.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.ImMessage;
import com.linkx.server.entity.ImGroupMember;
import com.linkx.server.entity.SysFile;
import com.linkx.server.mapper.ImMessageMapper;
import com.linkx.server.mapper.ImGroupMemberMapper;
import com.linkx.server.mapper.SysFileMapper;
import com.linkx.server.module.chat.constant.ChatConstants;
import com.linkx.server.module.file.dto.FileAccessDTO;
import com.linkx.server.module.file.dto.FileDTO;
import com.linkx.server.module.file.service.FileAccessTicketService;
import com.linkx.server.module.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final int HEADER_READ_LIMIT = 64;
    private static final int TEXT_VALIDATION_LIMIT = 8192;

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp");
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/bmp"
    );
    private static final Set<String> FILE_EXTENSIONS = Set.of(
            ".pdf", ".docx", ".xlsx", ".pptx",
            ".txt", ".zip", ".rar", ".7z", ".mp3", ".wav", ".mp4", ".avi"
    );
    private static final Set<String> FILE_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "application/zip",
            "application/x-zip-compressed",
            "application/vnd.rar",
            "application/x-rar-compressed",
            "application/x-7z-compressed",
            "audio/mpeg",
            "audio/wav",
            "video/mp4",
            "video/x-msvideo"
    );
    private static final Set<String> OOXML_EXTENSIONS = Set.of(".docx", ".xlsx", ".pptx");

    private final SysFileMapper fileMapper;
    private final ImMessageMapper messageMapper;
    private final ImGroupMemberMapper groupMemberMapper;
    private final FileAccessTicketService fileAccessTicketService;

    @Value("${linkx.upload.path:uploads/}")
    private String uploadPath;

    @Value("${linkx.upload.url:http://localhost:8080/uploads/}")
    private String uploadUrl;

    @Value("${linkx.api-base-url:http://localhost:8080}")
    private String apiBaseUrl;

    @Override
    public FileDTO uploadImage(Long userId, MultipartFile file) {
        return storeFile(userId, file, "image", IMAGE_EXTENSIONS, IMAGE_CONTENT_TYPES, "仅支持上传 JPG、PNG、GIF、WEBP、BMP 图片");
    }

    @Override
    public FileDTO uploadAvatar(Long userId, MultipartFile file) {
        return storeFile(userId, file, "avatar", IMAGE_EXTENSIONS, IMAGE_CONTENT_TYPES, "头像仅支持上传 JPG、PNG、GIF、WEBP、BMP 图片");
    }

    @Override
    public FileDTO uploadFile(Long userId, MultipartFile file) {
        return storeFile(userId, file, "file", FILE_EXTENSIONS, FILE_CONTENT_TYPES, "仅支持上传常见文档、压缩包、音视频和文本文件");
    }

    private FileDTO storeFile(
            Long userId,
            MultipartFile file,
            String type,
            Set<String> allowedExtensions,
            Set<String> allowedContentTypes,
            String invalidTypeMessage
    ) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = extractExtension(originalFilename);
        String contentType = normalizeContentType(file.getContentType());
        if (!allowedExtensions.contains(ext) || !allowedContentTypes.contains(contentType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, invalidTypeMessage);
        }
        validateFileContent(file, ext, invalidTypeMessage);

        String storedName = UUID.randomUUID().toString() + ext;
        String dirPath = buildDirPath(type);
        File dir = new File(dirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "创建上传目录失败");
        }

        try {
            File dest = new File(dir, storedName);
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件上传失败");
        }

        String fileUrl = buildFileUrl(type, storedName);

        SysFile sysFile = new SysFile();
        sysFile.setUserId(userId);
        sysFile.setOriginalName(StringUtils.hasText(originalFilename) ? originalFilename : storedName);
        sysFile.setStoredName(storedName);
        sysFile.setFilePath(new File(dir, storedName).getAbsolutePath());
        sysFile.setFileUrl(fileUrl);
        sysFile.setFileSize(file.getSize());
        sysFile.setFileType(contentType);
        fileMapper.insert(sysFile);

        return toFileDTO(sysFile);
    }

    private void validateFileContent(MultipartFile file, String extension, String invalidTypeMessage) {
        DetectedFileType detectedFileType = detectFileType(file);
        if (!matchesExtension(extension, detectedFileType) || !matchesStructuredFile(file, extension, detectedFileType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, invalidTypeMessage);
        }
    }

    private DetectedFileType detectFileType(MultipartFile file) {
        try {
            byte[] header = readHeader(file, HEADER_READ_LIMIT);
            if (startsWith(header, 0xFF, 0xD8, 0xFF)) {
                return DetectedFileType.JPEG;
            }
            if (startsWith(header, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
                return DetectedFileType.PNG;
            }
            if (hasAsciiAt(header, 0, "GIF87a") || hasAsciiAt(header, 0, "GIF89a")) {
                return DetectedFileType.GIF;
            }
            if (startsWith(header, 0x42, 0x4D)) {
                return DetectedFileType.BMP;
            }
            if (hasAsciiAt(header, 0, "RIFF") && hasAsciiAt(header, 8, "WEBP")) {
                return DetectedFileType.WEBP;
            }
            if (hasAsciiAt(header, 0, "%PDF-")) {
                return DetectedFileType.PDF;
            }
            if (startsWith(header, 0x50, 0x4B, 0x03, 0x04)
                    || startsWith(header, 0x50, 0x4B, 0x05, 0x06)
                    || startsWith(header, 0x50, 0x4B, 0x07, 0x08)) {
                return DetectedFileType.ZIP;
            }
            if (startsWith(header, 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00)
                    || startsWith(header, 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00)) {
                return DetectedFileType.RAR;
            }
            if (startsWith(header, 0x37, 0x7A, 0xBC, 0xAF, 0x27, 0x1C)) {
                return DetectedFileType.SEVEN_Z;
            }
            if (hasAsciiAt(header, 0, "RIFF") && hasAsciiAt(header, 8, "WAVE")) {
                return DetectedFileType.WAV;
            }
            if (hasAsciiAt(header, 0, "RIFF") && hasAsciiAt(header, 8, "AVI ")) {
                return DetectedFileType.AVI;
            }
            if (hasAsciiAt(header, 4, "ftyp")) {
                return DetectedFileType.MP4;
            }
            if (hasAsciiAt(header, 0, "ID3") || isMp3FrameHeader(header)) {
                return DetectedFileType.MP3;
            }
            if (startsWith(header, 0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1)) {
                return DetectedFileType.OLE;
            }
            if (looksLikePlainText(file)) {
                return DetectedFileType.TEXT;
            }
            return DetectedFileType.UNKNOWN;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件校验失败");
        }
    }

    private boolean matchesExtension(String extension, DetectedFileType detectedFileType) {
        return switch (extension) {
            case ".jpg", ".jpeg" -> detectedFileType == DetectedFileType.JPEG;
            case ".png" -> detectedFileType == DetectedFileType.PNG;
            case ".gif" -> detectedFileType == DetectedFileType.GIF;
            case ".webp" -> detectedFileType == DetectedFileType.WEBP;
            case ".bmp" -> detectedFileType == DetectedFileType.BMP;
            case ".pdf" -> detectedFileType == DetectedFileType.PDF;
            case ".zip" -> detectedFileType == DetectedFileType.ZIP;
            case ".rar" -> detectedFileType == DetectedFileType.RAR;
            case ".7z" -> detectedFileType == DetectedFileType.SEVEN_Z;
            case ".mp3" -> detectedFileType == DetectedFileType.MP3;
            case ".wav" -> detectedFileType == DetectedFileType.WAV;
            case ".mp4" -> detectedFileType == DetectedFileType.MP4;
            case ".avi" -> detectedFileType == DetectedFileType.AVI;
            case ".txt" -> detectedFileType == DetectedFileType.TEXT;
            default -> OOXML_EXTENSIONS.contains(extension) && detectedFileType == DetectedFileType.ZIP;
        };
    }

    private boolean matchesStructuredFile(MultipartFile file, String extension, DetectedFileType detectedFileType) {
        if (!OOXML_EXTENSIONS.contains(extension)) {
            return true;
        }
        if (detectedFileType != DetectedFileType.ZIP) {
            return false;
        }
        return switch (extension) {
            case ".docx" -> zipContains(file, "word/");
            case ".xlsx" -> zipContains(file, "xl/");
            case ".pptx" -> zipContains(file, "ppt/");
            default -> false;
        };
    }

    private boolean zipContains(MultipartFile file, String requiredPrefix) {
        try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
            boolean hasContentTypes = false;
            boolean hasRequiredPrefix = false;
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                if ("[Content_Types].xml".equals(entryName)) {
                    hasContentTypes = true;
                }
                if (entryName != null && entryName.startsWith(requiredPrefix)) {
                    hasRequiredPrefix = true;
                }
                if (hasContentTypes && hasRequiredPrefix) {
                    return true;
                }
            }
            return false;
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件校验失败");
        }
    }

    private String buildDirPath(String type) {
        File baseDir = new File(uploadPath);
        return new File(baseDir, type).getAbsolutePath() + File.separator;
    }

    private String buildFileUrl(String type, String storedName) {
        String normalizedBaseUrl = uploadUrl.endsWith("/") ? uploadUrl : uploadUrl + "/";
        return normalizedBaseUrl + type + "/" + storedName;
    }

    private String extractExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase(Locale.ROOT);
    }

    private String normalizeContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return "";
        }
        int separatorIndex = contentType.indexOf(';');
        String normalized = separatorIndex >= 0 ? contentType.substring(0, separatorIndex) : contentType;
        return normalized.trim().toLowerCase(Locale.ROOT);
    }

    private byte[] readHeader(MultipartFile file, int length) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return inputStream.readNBytes(length);
        }
    }

    private boolean startsWith(byte[] source, int... expected) {
        if (source.length < expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if ((source[i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean hasAsciiAt(byte[] source, int offset, String expected) {
        byte[] expectedBytes = expected.getBytes(StandardCharsets.US_ASCII);
        if (source.length < offset + expectedBytes.length) {
            return false;
        }
        for (int i = 0; i < expectedBytes.length; i++) {
            if (source[offset + i] != expectedBytes[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean isMp3FrameHeader(byte[] source) {
        if (source.length < 2) {
            return false;
        }
        return (source[0] & 0xFF) == 0xFF && (source[1] & 0xE0) == 0xE0;
    }

    private boolean looksLikePlainText(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] content = inputStream.readNBytes(TEXT_VALIDATION_LIMIT);
            if (content.length == 0) {
                return true;
            }

            int suspiciousCharCount = 0;
            for (byte currentByte : content) {
                int unsignedByte = currentByte & 0xFF;
                if (unsignedByte == 0) {
                    return false;
                }
                if (unsignedByte < 0x20 && unsignedByte != '\n' && unsignedByte != '\r' && unsignedByte != '\t') {
                    suspiciousCharCount++;
                }
            }
            return suspiciousCharCount * 20 <= content.length;
        }
    }

    private enum DetectedFileType {
        JPEG,
        PNG,
        GIF,
        WEBP,
        BMP,
        PDF,
        ZIP,
        RAR,
        SEVEN_Z,
        MP3,
        WAV,
        MP4,
        AVI,
        OLE,
        TEXT,
        UNKNOWN
    }

    @Override
    public List<FileDTO> listFiles(Long userId, String keyword) {
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFile::getUserId, userId);
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(SysFile::getOriginalName, keyword.trim());
        }
        wrapper.orderByDesc(SysFile::getCreateTime);
        return fileMapper.selectList(wrapper).stream()
                .map(this::toFileDTO)
                .toList();
    }

    @Override
    public FileAccessDTO createAccessUrl(Long userId, String fileUrl) {
        SysFile sysFile = findFileByUrl(fileUrl);
        if (sysFile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        if (!canAccessFile(userId, sysFile)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该文件");
        }
        String ticket = fileAccessTicketService.createTicket(sysFile.getId());
        return new FileAccessDTO(buildAccessUrl(ticket));
    }

    @Override
    public void deleteFile(Long userId, Long fileId) {
        SysFile sysFile = fileMapper.selectById(fileId);
        if (sysFile == null || !sysFile.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        LambdaQueryWrapper<ImMessage> msgWrapper = new LambdaQueryWrapper<>();
        msgWrapper.eq(ImMessage::getContent, sysFile.getFileUrl());
        if (messageMapper.selectCount(msgWrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该文件已被聊天消息引用，无法删除");
        }

        File file = new File(sysFile.getFilePath());
        if (file.exists() && !file.delete()) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件删除失败，请稍后重试");
        }

        fileMapper.deleteById(fileId);
    }

    private SysFile findFileByUrl(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return null;
        }
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFile::getFileUrl, fileUrl.trim()).last("LIMIT 1");
        return fileMapper.selectOne(wrapper);
    }

    private boolean canAccessFile(Long userId, SysFile sysFile) {
        if (sysFile == null) {
            return false;
        }
        if (userId != null && userId.equals(sysFile.getUserId())) {
            return true;
        }
        if (isAvatarUrl(sysFile.getFileUrl())) {
            return true;
        }
        if (userId == null) {
            return false;
        }

        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImMessage::getContent, sysFile.getFileUrl())
                .in(ImMessage::getMsgType, List.of(ChatConstants.MESSAGE_TYPE_IMAGE, ChatConstants.MESSAGE_TYPE_FILE))
                .ne(ImMessage::getStatus, ChatConstants.MESSAGE_STATUS_RECALLED)
                .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                .last("LIMIT 100");
        List<ImMessage> relatedMessages = messageMapper.selectList(wrapper);
        if (relatedMessages.isEmpty()) {
            return false;
        }
        for (ImMessage message : relatedMessages) {
            if (message == null) {
                continue;
            }
            if (!isGroupMessage(message)) {
                if (userId.equals(message.getFromUserId()) || userId.equals(message.getToUserId())) {
                    return true;
                }
                continue;
            }
            if (isGroupMember(message.getToUserId(), userId)) {
                return true;
            }
        }
        return false;
    }

    private boolean isGroupMessage(ImMessage message) {
        return message != null
                && message.getSessionId() != null
                && message.getSessionId().equals(message.getToUserId());
    }

    private boolean isGroupMember(Long groupId, Long userId) {
        if (groupId == null || userId == null) {
            return false;
        }
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupMember::getGroupId, groupId)
                .eq(ImGroupMember::getUserId, userId);
        return groupMemberMapper.selectCount(wrapper) > 0;
    }

    private boolean isAvatarUrl(String fileUrl) {
        return StringUtils.hasText(fileUrl) && fileUrl.contains("/avatar/");
    }

    private String buildAccessUrl(String ticket) {
        String normalizedApiBaseUrl = apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl;
        return normalizedApiBaseUrl + "/api/file/access/" + ticket;
    }

    private FileDTO toFileDTO(SysFile sysFile) {
        FileDTO dto = new FileDTO();
        dto.setId(sysFile.getId());
        dto.setOriginalName(sysFile.getOriginalName());
        dto.setFileUrl(sysFile.getFileUrl());
        dto.setFileSize(sysFile.getFileSize());
        dto.setFileType(sysFile.getFileType());
        dto.setCreateTime(sysFile.getCreateTime());
        return dto;
    }
}
