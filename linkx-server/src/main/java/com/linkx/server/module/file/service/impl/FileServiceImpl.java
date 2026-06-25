package com.linkx.server.module.file.service.impl;  // 行注：声明当前文件所在包 com.linkx.server.module.file.service.impl

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 行注：引入 LambdaQueryWrapper 类型
import com.linkx.server.common.BusinessException;  // 行注：引入 BusinessException 类型
import com.linkx.server.common.ErrorCode;  // 行注：引入 ErrorCode 类型
import com.linkx.server.common.TextNormalizer;  // 行注：引入 TextNormalizer 类型
import com.linkx.server.config.LinkxAppProperties;
import com.linkx.server.config.LinkxMinioProperties;
import com.linkx.server.module.file.storage.MinioObjectStorageService;
import com.linkx.server.entity.ImMessage;  // 行注：引入 ImMessage 类型
import com.linkx.server.entity.ImGroupMember;  // 行注：引入 ImGroupMember 类型
import com.linkx.server.entity.SysFile;  // 行注：引入 SysFile 类型
import com.linkx.server.mapper.ImMessageMapper;  // 行注：引入 ImMessageMapper 类型
import com.linkx.server.mapper.ImGroupMemberMapper;  // 行注：引入 ImGroupMemberMapper 类型
import com.linkx.server.mapper.SysFileMapper;  // 行注：引入 SysFileMapper 类型
import com.linkx.server.module.chat.constant.ChatConstants;  // 行注：引入 ChatConstants 类型
import com.linkx.server.module.file.dto.FileAccessDTO;
import com.linkx.server.module.file.dto.FileDTO;
import com.linkx.server.module.file.dto.StoredFileContent;
import com.linkx.server.module.file.service.FileAccessTicketService;  // 行注：引入 FileAccessTicketService 类型
import com.linkx.server.module.file.service.FileService;  // 行注：引入 FileService 类型
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型
import org.springframework.web.multipart.MultipartFile;  // 行注：引入 MultipartFile 类型

import java.io.File;  // 行注：引入 File 类型
import java.io.IOException;  // 行注：引入 IOException 类型
import java.io.InputStream;  // 行注：引入 InputStream 类型
import java.nio.charset.StandardCharsets;  // 行注：引入 StandardCharsets 类型
import java.util.List;  // 行注：引入 List 类型
import java.util.Locale;  // 行注：引入 Locale 类型
import java.util.Set;  // 行注：引入 Set 类型
import java.util.UUID;  // 行注：引入 UUID 类型
import java.util.zip.ZipEntry;  // 行注：引入 ZipEntry 类型
import java.util.zip.ZipInputStream;  // 行注：引入 ZipInputStream 类型

/**
 * 文件上传实现：魔数/扩展名校验、ZIP 炸弹防护、落盘与 {@link SysFile} 入库；
 * 访问 URL 经 {@link FileAccessTicketService} 换 ticket。
 */
@Slf4j  // 行注：应用 @Slf4j 注解
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 FileServiceImpl 类
public class FileServiceImpl implements FileService {

    private static final int HEADER_READ_LIMIT = 64;  // 行注：定义HEADER已读限制常量
    private static final int TEXT_VALIDATION_LIMIT = 8192;  // 行注：定义文本VALIDATION限制常量

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp");  // 行注：定义IMAGEEXTENSIONS常量
    // 行注：定义IMAGE内容TYPES常量
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            // 行注：补充当前表达式片段
            "image/jpeg",
            // 行注：补充当前表达式片段
            "image/png",
            // 行注：补充当前表达式片段
            "image/gif",
            // 行注：补充当前表达式片段
            "image/webp",
            // 行注：补充当前表达式片段
            "image/bmp"
    );  // 行注：结束当前参数配置
    // 行注：定义文件EXTENSIONS常量
    private static final Set<String> FILE_EXTENSIONS = Set.of(
            // 行注：补充当前表达式片段
            ".pdf", ".docx", ".xlsx", ".pptx",
            // 行注：补充当前表达式片段
            ".txt", ".zip", ".rar", ".7z", ".mp3", ".wav", ".mp4", ".avi"
    );  // 行注：结束当前参数配置
    // 行注：定义文件内容TYPES常量
    private static final Set<String> FILE_CONTENT_TYPES = Set.of(
            // 行注：补充当前表达式片段
            "application/pdf",
            // 行注：补充当前表达式片段
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            // 行注：补充当前表达式片段
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            // 行注：补充当前表达式片段
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            // 行注：补充当前表达式片段
            "text/plain",
            // 行注：补充当前表达式片段
            "application/zip",
            // 行注：补充当前表达式片段
            "application/x-zip-compressed",
            // 行注：补充当前表达式片段
            "application/vnd.rar",
            // 行注：补充当前表达式片段
            "application/x-rar-compressed",
            // 行注：补充当前表达式片段
            "application/x-7z-compressed",
            // 行注：补充当前表达式片段
            "audio/mpeg",
            // 行注：补充当前表达式片段
            "audio/wav",
            // 行注：补充当前表达式片段
            "video/mp4",
            // 行注：补充当前表达式片段
            "video/x-msvideo"
    );  // 行注：结束当前参数配置
    private static final Set<String> OOXML_EXTENSIONS = Set.of(".docx", ".xlsx", ".pptx");  // 行注：定义OOXMLEXTENSIONS常量

    private final SysFileMapper fileMapper;  // 行注：注入文件Mapper依赖
    private final ImMessageMapper messageMapper;  // 行注：注入消息Mapper依赖
    private final ImGroupMemberMapper groupMemberMapper;  // 行注：注入群成员Mapper依赖
    private final FileAccessTicketService fileAccessTicketService;
    private final LinkxAppProperties linkxAppProperties;
    private final LinkxMinioProperties linkxMinioProperties;
    private final ObjectProvider<MinioObjectStorageService> minioObjectStorageProvider;

    /**
     * 上传图片。
     *
     * @param userId 用户 ID
     * @param file 上传文件
     * @return 文件信息
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义上传Image方法
    public FileDTO uploadImage(Long userId, MultipartFile file) {
        // 行注：返回处理结果
        return storeFile(userId, file, "image", IMAGE_EXTENSIONS, IMAGE_CONTENT_TYPES, "仅支持上传 JPG、PNG、GIF、WEBP、BMP 图片");
    }  // 行注：结束当前代码块

    /**
     * 上传头像。
     *
     * @param userId 用户 ID
     * @param file 上传文件
     * @return 文件信息
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义上传头像方法
    public FileDTO uploadAvatar(Long userId, MultipartFile file) {
        // 行注：返回处理结果
        return storeFile(userId, file, "avatar", IMAGE_EXTENSIONS, IMAGE_CONTENT_TYPES, "头像仅支持上传 JPG、PNG、GIF、WEBP、BMP 图片");
    }  // 行注：结束当前代码块

    /**
     * 上传文件。
     *
     * @param userId 用户 ID
     * @param file 上传文件
     * @return 文件信息
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义上传文件方法
    public FileDTO uploadFile(Long userId, MultipartFile file) {
        return storeFile(userId, file, "file", FILE_EXTENSIONS, FILE_CONTENT_TYPES, "仅支持上传常见文档、压缩包、音视频和文本文件");  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义store文件方法
    private FileDTO storeFile(
            // 行注：补充当前表达式片段
            Long userId,
            // 行注：补充当前表达式片段
            MultipartFile file,
            // 行注：补充当前表达式片段
            String type,
            // 行注：补充当前表达式片段
            Set<String> allowedExtensions,
            // 行注：补充当前表达式片段
            Set<String> allowedContentTypes,
            // 行注：补充当前表达式片段
            String invalidTypeMessage
    // 行注：开始当前语句对应的代码块
    ) {
        // 行注：判断是否满足当前条件
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件不能为空");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        String originalFilename = file.getOriginalFilename();  // 行注：初始化originalFilename
        String ext = extractExtension(originalFilename);  // 行注：初始化ext
        String contentType = normalizeContentType(file.getContentType());  // 行注：初始化内容类型
        // 行注：判断是否满足当前条件
        if (!allowedExtensions.contains(ext) || !allowedContentTypes.contains(contentType)) {
            // 行注：补充当前表达式片段
            log.warn("File upload rejected, userId={}, type={}, originalName={}, extension={}, contentType={}, reason=invalid_type",
                    userId, type, originalFilename, ext, contentType);  // 行注：完成当前语句
            throw new BusinessException(ErrorCode.BAD_REQUEST, invalidTypeMessage);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        validateFileContent(file, ext, invalidTypeMessage);  // 行注：调用validate文件内容

        String storedName = UUID.randomUUID().toString() + ext;
        String objectKey = type + "/" + storedName;
        String fileUrl = buildFileUrl(type, storedName);
        String persistedPath;

        MinioObjectStorageService minioStorage = minioObjectStorageProvider.getIfAvailable();
        if (useMinio(minioStorage)) {
            try {
                minioStorage.putObject(objectKey, file.getInputStream(), file.getSize(), contentType);
            } catch (Exception e) {
                log.error("MinIO upload failed, userId={}, objectKey={}", userId, objectKey, e);
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件上传失败");
            }
            persistedPath = objectKey;
        } else {
            String dirPath = buildDirPath(type);
            File dir = new File(dirPath);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "创建上传目录失败");
            }
            try {
                File dest = new File(dir, storedName);
                file.transferTo(dest);
                persistedPath = dest.getAbsolutePath();
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件上传失败");
            }
        }

        SysFile sysFile = new SysFile();
        sysFile.setUserId(userId);
        String normalizedOriginalName = TextNormalizer.normalizeOptionalSingleLine(originalFilename, 255, "文件名");
        sysFile.setOriginalName(StringUtils.hasText(normalizedOriginalName) ? normalizedOriginalName : storedName);
        sysFile.setStoredName(storedName);
        sysFile.setFilePath(persistedPath);
        sysFile.setFileUrl(fileUrl);
        sysFile.setFileSize(file.getSize());  // 行注：调用设置文件大小
        sysFile.setFileType(contentType);  // 行注：调用设置文件类型
        fileMapper.insert(sysFile);  // 行注：调用insert
        // 行注：补充当前表达式片段
        log.info("File stored, userId={}, fileId={}, type={}, originalName={}, storedName={}, size={}, contentType={}",
                userId, sysFile.getId(), type, sysFile.getOriginalName(), storedName, file.getSize(), contentType);  // 行注：调用获取ID

        return toFileDTO(sysFile);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义validate文件内容方法
    private void validateFileContent(MultipartFile file, String extension, String invalidTypeMessage) {
        DetectedFileType detectedFileType = detectFileType(file);  // 行注：初始化detected文件类型
        // 行注：判断是否满足当前条件
        if (!matchesExtension(extension, detectedFileType) || !matchesStructuredFile(file, extension, detectedFileType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, invalidTypeMessage);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义detect文件类型方法
    private DetectedFileType detectFileType(MultipartFile file) {
        // 行注：尝试执行可能失败的逻辑
        try {
            byte[] header = readHeader(file, HEADER_READ_LIMIT);  // 行注：初始化header
            // 行注：判断是否满足当前条件
            if (startsWith(header, 0xFF, 0xD8, 0xFF)) {
                return DetectedFileType.JPEG;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (startsWith(header, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
                return DetectedFileType.PNG;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (hasAsciiAt(header, 0, "GIF87a") || hasAsciiAt(header, 0, "GIF89a")) {
                return DetectedFileType.GIF;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (startsWith(header, 0x42, 0x4D)) {
                return DetectedFileType.BMP;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (hasAsciiAt(header, 0, "RIFF") && hasAsciiAt(header, 8, "WEBP")) {
                return DetectedFileType.WEBP;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (hasAsciiAt(header, 0, "%PDF-")) {
                return DetectedFileType.PDF;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (startsWith(header, 0x50, 0x4B, 0x03, 0x04)
                    // 行注：调用starts
                    || startsWith(header, 0x50, 0x4B, 0x05, 0x06)
                    // 行注：调用starts
                    || startsWith(header, 0x50, 0x4B, 0x07, 0x08)) {
                return DetectedFileType.ZIP;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (startsWith(header, 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00)
                    // 行注：调用starts
                    || startsWith(header, 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00)) {
                return DetectedFileType.RAR;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (startsWith(header, 0x37, 0x7A, 0xBC, 0xAF, 0x27, 0x1C)) {
                return DetectedFileType.SEVEN_Z;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (hasAsciiAt(header, 0, "RIFF") && hasAsciiAt(header, 8, "WAVE")) {
                return DetectedFileType.WAV;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (hasAsciiAt(header, 0, "RIFF") && hasAsciiAt(header, 8, "AVI ")) {
                return DetectedFileType.AVI;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (hasAsciiAt(header, 4, "ftyp")) {
                return DetectedFileType.MP4;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (hasAsciiAt(header, 0, "ID3") || isMp3FrameHeader(header)) {
                return DetectedFileType.MP3;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (startsWith(header, 0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1)) {
                return DetectedFileType.OLE;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (looksLikePlainText(file)) {
                return DetectedFileType.TEXT;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            return DetectedFileType.UNKNOWN;  // 行注：返回处理结果
        // 行注：执行当前方法调用
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件校验失败");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义matchesExtension方法
    private boolean matchesExtension(String extension, DetectedFileType detectedFileType) {
        return switch (extension) {  // 行注：返回处理结果
            case ".jpg", ".jpeg" -> detectedFileType == DetectedFileType.JPEG;  // 行注：处理当前分支
            case ".png" -> detectedFileType == DetectedFileType.PNG;  // 行注：处理当前分支
            case ".gif" -> detectedFileType == DetectedFileType.GIF;  // 行注：处理当前分支
            case ".webp" -> detectedFileType == DetectedFileType.WEBP;  // 行注：处理当前分支
            case ".bmp" -> detectedFileType == DetectedFileType.BMP;  // 行注：处理当前分支
            case ".pdf" -> detectedFileType == DetectedFileType.PDF;  // 行注：处理当前分支
            case ".zip" -> detectedFileType == DetectedFileType.ZIP;  // 行注：处理当前分支
            case ".rar" -> detectedFileType == DetectedFileType.RAR;  // 行注：处理当前分支
            case ".7z" -> detectedFileType == DetectedFileType.SEVEN_Z;  // 行注：处理当前分支
            case ".mp3" -> detectedFileType == DetectedFileType.MP3;  // 行注：处理当前分支
            case ".wav" -> detectedFileType == DetectedFileType.WAV;  // 行注：处理当前分支
            case ".mp4" -> detectedFileType == DetectedFileType.MP4;  // 行注：处理当前分支
            case ".avi" -> detectedFileType == DetectedFileType.AVI;  // 行注：处理当前分支
            case ".txt" -> detectedFileType == DetectedFileType.TEXT;  // 行注：处理当前分支
            default -> OOXML_EXTENSIONS.contains(extension) && detectedFileType == DetectedFileType.ZIP;  // 行注：处理默认分支
        };  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义matchesStructured文件方法
    private boolean matchesStructuredFile(MultipartFile file, String extension, DetectedFileType detectedFileType) {
        // 行注：判断是否满足当前条件
        if (!OOXML_EXTENSIONS.contains(extension)) {
            return true;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (detectedFileType != DetectedFileType.ZIP) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return switch (extension) {  // 行注：返回处理结果
            case ".docx" -> zipContains(file, "word/");  // 行注：处理当前分支
            case ".xlsx" -> zipContains(file, "xl/");  // 行注：处理当前分支
            case ".pptx" -> zipContains(file, "ppt/");  // 行注：处理当前分支
            default -> false;  // 行注：处理默认分支
        };  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义zipContains方法
    private boolean zipContains(MultipartFile file, String requiredPrefix) {
        // 行注：尝试执行可能失败的逻辑
        try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
            boolean hasContentTypes = false;  // 行注：初始化是否包含内容Types
            boolean hasRequiredPrefix = false;  // 行注：初始化是否包含必填Prefix
            ZipEntry entry;  // 行注：完成当前语句
            // 行注：按条件重复执行当前逻辑
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();  // 行注：初始化入口名称
                // 行注：判断是否满足当前条件
                if ("[Content_Types].xml".equals(entryName)) {
                    hasContentTypes = true;  // 行注：初始化是否包含内容Types
                }  // 行注：结束当前代码块
                // 行注：判断是否满足当前条件
                if (entryName != null && entryName.startsWith(requiredPrefix)) {
                    hasRequiredPrefix = true;  // 行注：初始化是否包含必填Prefix
                }  // 行注：结束当前代码块
                // 行注：判断是否满足当前条件
                if (hasContentTypes && hasRequiredPrefix) {
                    return true;  // 行注：返回处理结果
                }  // 行注：结束当前代码块
            }  // 行注：结束当前代码块
            return false;  // 行注：返回处理结果
        // 行注：执行当前方法调用
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件校验失败");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义构建Dir路径方法
    private String buildDirPath(String type) {
        String uploadPath = linkxAppProperties.getUpload().getPath();  // 行注：初始化上传路径
        File baseDir = new File(uploadPath);  // 行注：初始化基础Dir
        return new File(baseDir, type).getAbsolutePath() + File.separator;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义构建文件URL方法
    private String buildFileUrl(String type, String storedName) {
        String uploadUrl = linkxAppProperties.getUpload().getUrl();  // 行注：初始化上传URL
        String normalizedBaseUrl = uploadUrl.endsWith("/") ? uploadUrl : uploadUrl + "/";  // 行注：初始化规范化后的基础URL
        return normalizedBaseUrl + type + "/" + storedName;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义extractExtension方法
    private String extractExtension(String originalFilename) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            return "";  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase(Locale.ROOT);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义规范化内容类型方法
    private String normalizeContentType(String contentType) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(contentType)) {
            return "";  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        int separatorIndex = contentType.indexOf(';');  // 行注：初始化separatorIndex
        String normalized = separatorIndex >= 0 ? contentType.substring(0, separatorIndex) : contentType;  // 行注：执行初始化操作
        return normalized.trim().toLowerCase(Locale.ROOT);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义已读Header方法
    private byte[] readHeader(MultipartFile file, int length) throws IOException {
        // 行注：尝试执行可能失败的逻辑
        try (InputStream inputStream = file.getInputStream()) {
            return inputStream.readNBytes(length);  // 行注：返回处理结果
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义starts方法
    private boolean startsWith(byte[] source, int... expected) {
        // 行注：判断是否满足当前条件
        if (source.length < expected.length) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：遍历当前集合或范围
        for (int i = 0; i < expected.length; i++) {
            // 行注：判断是否满足当前条件
            if ((source[i] & 0xFF) != expected[i]) {
                return false;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        return true;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义是否包含AsciiAt方法
    private boolean hasAsciiAt(byte[] source, int offset, String expected) {
        byte[] expectedBytes = expected.getBytes(StandardCharsets.US_ASCII);  // 行注：初始化期望值Bytes
        // 行注：判断是否满足当前条件
        if (source.length < offset + expectedBytes.length) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：遍历当前集合或范围
        for (int i = 0; i < expectedBytes.length; i++) {
            // 行注：判断是否满足当前条件
            if (source[offset + i] != expectedBytes[i]) {
                return false;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        return true;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义是否Mp3FrameHeader方法
    private boolean isMp3FrameHeader(byte[] source) {
        // 行注：判断是否满足当前条件
        if (source.length < 2) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return (source[0] & 0xFF) == 0xFF && (source[1] & 0xE0) == 0xE0;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义looksLikePlain文本方法
    private boolean looksLikePlainText(MultipartFile file) throws IOException {
        // 行注：尝试执行可能失败的逻辑
        try (InputStream inputStream = file.getInputStream()) {
            byte[] content = inputStream.readNBytes(TEXT_VALIDATION_LIMIT);  // 行注：初始化内容
            // 行注：判断是否满足当前条件
            if (content.length == 0) {
                return true;  // 行注：返回处理结果
            }  // 行注：结束当前代码块

            int suspiciousCharCount = 0;  // 行注：初始化suspiciousChar数量
            // 行注：遍历当前集合或范围
            for (byte currentByte : content) {
                int unsignedByte = currentByte & 0xFF;  // 行注：初始化unsignedByte
                // 行注：判断是否满足当前条件
                if (unsignedByte == 0) {
                    return false;  // 行注：返回处理结果
                }  // 行注：结束当前代码块
                // 行注：判断是否满足当前条件
                if (unsignedByte < 0x20 && unsignedByte != '\n' && unsignedByte != '\r' && unsignedByte != '\t') {
                    suspiciousCharCount++;  // 行注：完成当前语句
                }  // 行注：结束当前代码块
            }  // 行注：结束当前代码块
            return suspiciousCharCount * 20 <= content.length;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义 DetectedFileType 枚举
    private enum DetectedFileType {
        // 行注：补充当前表达式片段
        JPEG,
        // 行注：补充当前表达式片段
        PNG,
        // 行注：补充当前表达式片段
        GIF,
        // 行注：补充当前表达式片段
        WEBP,
        // 行注：补充当前表达式片段
        BMP,
        // 行注：补充当前表达式片段
        PDF,
        // 行注：补充当前表达式片段
        ZIP,
        // 行注：补充当前表达式片段
        RAR,
        // 行注：补充当前表达式片段
        SEVEN_Z,
        // 行注：补充当前表达式片段
        MP3,
        // 行注：补充当前表达式片段
        WAV,
        // 行注：补充当前表达式片段
        MP4,
        // 行注：补充当前表达式片段
        AVI,
        // 行注：补充当前表达式片段
        OLE,
        // 行注：补充当前表达式片段
        TEXT,
        // 行注：继续基于 UNKNOWN 配置处理流程
        UNKNOWN
    }  // 行注：结束当前代码块

    /**
     * 查询文件列表。
     *
     * @param userId 用户 ID
     * @param keyword 搜索关键词
     * @return 文件信息列表
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义列表Files方法
    public List<FileDTO> listFiles(Long userId, String keyword) {
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.eq(SysFile::getUserId, userId);  // 行注：调用等值条件
        // 行注：判断是否满足当前条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(SysFile::getOriginalName, keyword.trim());  // 行注：调用like
        }  // 行注：结束当前代码块
        wrapper.orderByDesc(SysFile::getCreateTime);  // 行注：调用排序降序
        return fileMapper.selectList(wrapper).stream()  // 行注：返回处理结果
                // 行注：继续调用映射
                .map(this::toFileDTO)
                .toList();  // 行注：继续调用转为列表
    }  // 行注：结束当前代码块

    /**
     * 创建带票据的文件访问地址。
     *
     * @param userId 用户 ID
     * @param fileUrl 文件 URL
     * @return 文件访问地址
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义创建访问URL方法
    public FileAccessDTO createAccessUrl(Long userId, String fileUrl) {
        SysFile sysFile = findFileByUrl(fileUrl);  // 行注：初始化系统文件
        // 行注：判断是否满足当前条件
        if (sysFile == null) {
            log.warn("File access url rejected, userId={}, fileUrl={}, reason=file_not_found", userId, fileUrl);  // 行注：执行初始化操作
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (!canAccessFile(userId, sysFile)) {
            // 行注：执行初始化操作
            log.warn("File access url rejected, userId={}, fileId={}, fileUrl={}, reason=forbidden", userId, sysFile.getId(), fileUrl);
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该文件");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        String ticket = fileAccessTicketService.createTicket(userId, sysFile.getId());  // 行注：初始化票据
        log.info("File access ticket created, userId={}, fileId={}", userId, sysFile.getId());  // 行注：执行初始化操作
        return new FileAccessDTO(buildAccessUrl(ticket));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 删除文件。
     *
     * @param userId 用户 ID
     * @param fileId 文件 ID
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义删除文件方法
    public void deleteFile(Long userId, Long fileId) {
        SysFile sysFile = fileMapper.selectById(fileId);  // 行注：初始化系统文件
        // 行注：判断是否满足当前条件
        if (sysFile == null || !sysFile.getUserId().equals(userId)) {
            log.warn("File delete rejected, userId={}, fileId={}, reason=file_not_found_or_not_owner", userId, fileId);  // 行注：执行初始化操作
            throw new BusinessException(ErrorCode.NOT_FOUND);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        LambdaQueryWrapper<ImMessage> msgWrapper = new LambdaQueryWrapper<>();  // 行注：初始化消息条件封装器
        msgWrapper.eq(ImMessage::getContent, sysFile.getFileUrl());  // 行注：调用等值条件
        // 行注：判断是否满足当前条件
        if (messageMapper.selectCount(msgWrapper) > 0) {
            log.warn("File delete rejected, userId={}, fileId={}, reason=message_referenced", userId, fileId);  // 行注：执行初始化操作
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该文件已被聊天消息引用，无法删除");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        removeStoredObject(sysFile);

        fileMapper.deleteById(fileId);
        log.info("File deleted, userId={}, fileId={}, originalName={}", userId, fileId, sysFile.getOriginalName());  // 行注：执行初始化操作
    }  // 行注：结束当前代码块

    // 行注：定义查找文件URL方法
    private SysFile findFileByUrl(String fileUrl) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(fileUrl)) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        String trimmed = fileUrl.trim();  // 行注：初始化trimmed
        LambdaQueryWrapper<SysFile> exactWrapper = new LambdaQueryWrapper<>();  // 行注：初始化exact条件封装器
        exactWrapper.eq(SysFile::getFileUrl, trimmed).last("LIMIT 1");  // 行注：调用等值条件
        SysFile exact = fileMapper.selectOne(exactWrapper);  // 行注：初始化exact
        // 行注：判断是否满足当前条件
        if (exact != null) {
            return exact;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        String storedName = extractStoredNameFromUrl(trimmed);  // 行注：初始化已存储值名称
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(storedName)) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LambdaQueryWrapper<SysFile> nameWrapper = new LambdaQueryWrapper<>();  // 行注：初始化名称条件封装器
        nameWrapper.eq(SysFile::getStoredName, storedName).last("LIMIT 1");  // 行注：调用等值条件
        return fileMapper.selectOne(nameWrapper);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义extract已存储值名称URL方法
    private String extractStoredNameFromUrl(String fileUrl) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(fileUrl)) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        String path = fileUrl.trim();  // 行注：初始化路径
        int queryIndex = path.indexOf('?');  // 行注：初始化查询Index
        // 行注：判断是否满足当前条件
        if (queryIndex >= 0) {
            path = path.substring(0, queryIndex);  // 行注：初始化路径
        }  // 行注：结束当前代码块
        int slash = path.lastIndexOf('/');  // 行注：初始化slash
        // 行注：判断是否满足当前条件
        if (slash < 0 || slash >= path.length() - 1) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return path.substring(slash + 1);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义can访问文件方法
    private boolean canAccessFile(Long userId, SysFile sysFile) {
        // 行注：判断是否满足当前条件
        if (sysFile == null || userId == null) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (userId.equals(sysFile.getUserId())) {
            return true;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        // 头像文件：检查是否是头像，头像对所有用户可访问
        String filePath = sysFile.getFilePath();
        String fileUrl = sysFile.getFileUrl();
        if (StringUtils.hasText(filePath) && filePath.startsWith("avatar/")) {
            return true;
        }
        if (StringUtils.hasText(fileUrl) && fileUrl.contains("/avatar/")) {
            return true;
        }

        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(fileUrl)) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImMessage::getContent, fileUrl.trim())
                // 行注：继续调用in
                .in(ImMessage::getMsgType, List.of(ChatConstants.MESSAGE_TYPE_IMAGE, ChatConstants.MESSAGE_TYPE_FILE))
                .ne(ImMessage::getStatus, ChatConstants.MESSAGE_STATUS_RECALLED);  // 行注：继续调用ne
        List<ImMessage> relatedMessages = messageMapper.selectList(wrapper);  // 行注：初始化related消息
        // 行注：判断是否满足当前条件
        if (relatedMessages.isEmpty()) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：遍历当前集合或范围
        for (ImMessage message : relatedMessages) {
            // 行注：判断是否满足当前条件
            if (message == null) {
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (!isGroupMessage(message)) {
                // 行注：判断是否满足当前条件
                if (userId.equals(message.getFromUserId()) || userId.equals(message.getToUserId())) {
                    return true;  // 行注：返回处理结果
                }  // 行注：结束当前代码块
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (isGroupMember(message.getToUserId(), userId)) {
                return true;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        return false;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义是否群消息方法
    private boolean isGroupMessage(ImMessage message) {
        return message != null  // 行注：返回处理结果
                // 行注：调用获取会话ID
                && message.getSessionId() != null
                && message.getSessionId().equals(message.getToUserId());  // 行注：调用获取会话ID
    }  // 行注：结束当前代码块

    // 行注：定义是否群成员方法
    private boolean isGroupMember(Long groupId, Long userId) {
        // 行注：判断是否满足当前条件
        if (groupId == null || userId == null) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImGroupMember::getGroupId, groupId)
                .eq(ImGroupMember::getUserId, userId);  // 行注：继续调用等值条件
        return groupMemberMapper.selectCount(wrapper) > 0;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    @Override
    public StoredFileContent openStoredContent(SysFile sysFile) {
        if (sysFile == null) {
            return null;
        }
        String path = sysFile.getFilePath();
        MinioObjectStorageService minioStorage = minioObjectStorageProvider.getIfAvailable();

        if (StringUtils.hasText(path) && !isMinioObjectKey(path)) {
            File local = new File(path);
            if (local.exists() && local.isFile()) {
                try {
                    String filename = StringUtils.hasText(sysFile.getOriginalName()) ? sysFile.getOriginalName() : local.getName();
                    return new StoredFileContent(new java.io.FileInputStream(local), filename, local.length());
                } catch (IOException e) {
                    log.warn("Local file open failed, fileId={}, path={}", sysFile.getId(), path, e);
                }
            }
        }

        if (StringUtils.hasText(path) && isMinioObjectKey(path) && useMinio(minioStorage)) {
            StoredFileContent fromMinio = readFromMinio(minioStorage, sysFile, path.trim());
            if (fromMinio != null) {
                return fromMinio;
            }
        }

        if (useMinio(minioStorage)) {
            String objectKey = resolveMinioObjectKey(sysFile);
            if (StringUtils.hasText(objectKey) && (path == null || !objectKey.equals(path.trim()))) {
                StoredFileContent fromMinio = readFromMinio(minioStorage, sysFile, objectKey);
                if (fromMinio != null) {
                    return fromMinio;
                }
            }
        }
        return null;
    }

    private StoredFileContent readFromMinio(MinioObjectStorageService minioStorage, SysFile sysFile, String objectKey) {
        try {
            var response = minioStorage.getObject(objectKey);
            long length = sysFile.getFileSize() != null ? sysFile.getFileSize() : -1;
            String filename = StringUtils.hasText(sysFile.getOriginalName()) ? sysFile.getOriginalName() : sysFile.getStoredName();
            return new StoredFileContent(response, filename, length);
        } catch (Exception e) {
            log.warn("MinIO getObject failed, fileId={}, objectKey={}", sysFile.getId(), objectKey, e);
            return null;
        }
    }

    /** 从 file_url / stored_name 推断 MinIO object key（兼容库内仍为本地绝对路径的旧记录） */
    private String resolveMinioObjectKey(SysFile sysFile) {
        if (sysFile == null || !StringUtils.hasText(sysFile.getStoredName())) {
            return null;
        }
        String storedName = sysFile.getStoredName().trim();
        String fileUrl = sysFile.getFileUrl();
        if (StringUtils.hasText(fileUrl)) {
            String trimmed = fileUrl.trim();
            int q = trimmed.indexOf('?');
            if (q >= 0) {
                trimmed = trimmed.substring(0, q);
            }
            int uploads = trimmed.indexOf("/uploads/");
            if (uploads >= 0) {
                String after = trimmed.substring(uploads + "/uploads/".length());
                if (StringUtils.hasText(after) && after.endsWith(storedName)) {
                    return after;
                }
            }
            if (trimmed.endsWith("/" + storedName)) {
                int last = trimmed.lastIndexOf('/');
                if (last > 0) {
                    int prev = trimmed.lastIndexOf('/', last - 1);
                    if (prev >= 0) {
                        return trimmed.substring(prev + 1);
                    }
                }
            }
        }
        String path = sysFile.getFilePath();
        if (StringUtils.hasText(path) && isMinioObjectKey(path)) {
            return path.trim();
        }
        if (StringUtils.hasText(path)) {
            String normalized = path.replace('\\', '/');
            int idx = normalized.indexOf("/uploads/");
            if (idx >= 0) {
                return normalized.substring(idx + "/uploads/".length());
            }
        }
        return null;
    }

    private void removeStoredObject(SysFile sysFile) {
        if (sysFile == null || !StringUtils.hasText(sysFile.getFilePath())) {
            return;
        }
        String path = sysFile.getFilePath();
        MinioObjectStorageService minioStorage = minioObjectStorageProvider.getIfAvailable();
        if (useMinio(minioStorage) && isMinioObjectKey(path)) {
            try {
                minioStorage.removeObject(path);
            } catch (Exception e) {
                log.warn("MinIO removeObject failed, fileId={}, objectKey={}", sysFile.getId(), path, e);
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件删除失败，请稍后重试");
            }
            return;
        }
        File file = new File(path);
        if (file.exists() && !file.delete()) {
            log.warn("File delete rejected, fileId={}, reason=file_delete_failed, path={}", sysFile.getId(), path);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件删除失败，请稍后重试");
        }
    }

    private boolean useMinio(MinioObjectStorageService minioStorage) {
        return linkxMinioProperties.isEnabled() && minioStorage != null;
    }

    /** MinIO 模式下 file_path 存 object key，如 image/uuid.png */
    private static boolean isMinioObjectKey(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return false;
        }
        if (filePath.length() >= 2 && filePath.charAt(1) == ':') {
            return false;
        }
        if (filePath.startsWith("/") || filePath.startsWith("\\")) {
            return false;
        }
        return filePath.contains("/") && !filePath.contains("\\");
    }

    // 行注：定义构建访问URL方法
    private String buildAccessUrl(String ticket) {
        String apiBaseUrl = linkxAppProperties.getApiBaseUrl();  // 行注：初始化接口基础URL
        // 行注：初始化规范化后的接口基础URL
        String normalizedApiBaseUrl = apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl;
        return normalizedApiBaseUrl + "/api/file/access/" + ticket;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义转为文件DTO方法
    private FileDTO toFileDTO(SysFile sysFile) {
        FileDTO dto = new FileDTO();  // 行注：初始化DTO
        dto.setId(sysFile.getId());  // 行注：调用设置ID
        dto.setOriginalName(sysFile.getOriginalName());  // 行注：调用设置Original名称
        dto.setFileUrl(sysFile.getFileUrl());  // 行注：调用设置文件URL
        dto.setFileSize(sysFile.getFileSize());  // 行注：调用设置文件大小
        dto.setFileType(sysFile.getFileType());  // 行注：调用设置文件类型
        dto.setCreateTime(sysFile.getCreateTime());  // 行注：调用设置创建时间
        return dto;  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
