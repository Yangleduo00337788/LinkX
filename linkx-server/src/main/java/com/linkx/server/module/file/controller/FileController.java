package com.linkx.server.module.file.controller;  // 行注：声明当前文件所在包 com.linkx.server.module.file.controller

import com.linkx.server.common.AuditLogService;
import com.linkx.server.common.HttpContentDispositionUtils;
import com.linkx.server.common.Result;
import com.linkx.server.entity.SysFile;  // 行注：引入 SysFile 类型
import com.linkx.server.module.file.dto.FileAccessDTO;  // 行注：引入 FileAccessDTO 类型
import com.linkx.server.module.file.dto.FileDTO;  // 行注：引入 FileDTO 类型
import com.linkx.server.module.file.service.FileAccessTicketService;  // 行注：引入 FileAccessTicketService 类型
import com.linkx.server.module.file.service.FileService;  // 行注：引入 FileService 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import lombok.extern.slf4j.Slf4j;  // 行注：引入 Slf4j 类型
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.http.MediaType;  // 行注：引入 MediaType 类型
import org.springframework.http.ResponseEntity;  // 行注：引入 ResponseEntity 类型
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // 行注：引入 AuthenticationPrincipal 类型
import org.springframework.security.core.userdetails.UserDetails;  // 行注：引入 UserDetails 类型
import org.springframework.web.bind.annotation.*;  // 行注：引入 * 类型
import org.springframework.web.multipart.MultipartFile;  // 行注：引入 MultipartFile 类型

import java.util.List;

/**
 * 文件上传与受控访问：上传后元数据入库；读取须先换 ticket 再 GET /access/{ticket}。
 */
@Slf4j  // 行注：应用 @Slf4j 注解
@RestController  // 行注：应用 @RestController 注解
@RequestMapping("/api/file")  // 行注：应用 @RequestMapping 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 FileController 类
public class FileController {

    private final FileService fileService;  // 行注：注入文件服务依赖
    private final FileAccessTicketService fileAccessTicketService;  // 行注：注入文件访问票据服务依赖
    private final AuditLogService auditLogService;  // 行注：注入审计日志服务依赖

    /**
     * 上传头像。
     *
     * @param userDetails 当前登录用户
     * @param file 上传文件
     * @return 统一返回结果
     */
    @PostMapping("/upload/avatar")  // 行注：应用 @PostMapping 注解
    // 行注：定义上传头像方法
    public Result<FileDTO> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @RequestParam("file") MultipartFile file) {  // 行注：应用 @RequestParam 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        FileDTO response = fileService.uploadAvatar(userId, file);  // 行注：初始化response
        // 行注：执行初始化操作
        log.info("File avatar upload success, userId={}, fileId={}, originalName={}", userId, response.getId(), response.getOriginalName());
        // 行注：调用记录Success
        auditLogService.recordSuccess("FILE_UPLOAD_AVATAR", userId, "FILE", response.getId(), response.getOriginalName());
        return Result.success(response);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 上传图片。
     *
     * @param userDetails 当前登录用户
     * @param file 上传文件
     * @return 统一返回结果
     */
    @PostMapping("/upload/image")  // 行注：应用 @PostMapping 注解
    // 行注：定义上传Image方法
    public Result<FileDTO> uploadImage(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @RequestParam("file") MultipartFile file) {  // 行注：应用 @RequestParam 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        FileDTO response = fileService.uploadImage(userId, file);  // 行注：初始化response
        // 行注：执行初始化操作
        log.info("File image upload success, userId={}, fileId={}, originalName={}", userId, response.getId(), response.getOriginalName());
        // 行注：调用记录Success
        auditLogService.recordSuccess("FILE_UPLOAD_IMAGE", userId, "FILE", response.getId(), response.getOriginalName());
        return Result.success(response);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 上传文件。
     *
     * @param userDetails 当前登录用户
     * @param file 上传文件
     * @return 统一返回结果
     */
    @PostMapping("/upload/file")  // 行注：应用 @PostMapping 注解
    // 行注：定义上传文件方法
    public Result<FileDTO> uploadFile(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @RequestParam("file") MultipartFile file) {  // 行注：应用 @RequestParam 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        FileDTO response = fileService.uploadFile(userId, file);  // 行注：初始化response
        // 行注：执行初始化操作
        log.info("File upload success, userId={}, fileId={}, originalName={}", userId, response.getId(), response.getOriginalName());
        // 行注：调用记录Success
        auditLogService.recordSuccess("FILE_UPLOAD_FILE", userId, "FILE", response.getId(), response.getOriginalName());
        return Result.success(response);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 查询文件列表。
     *
     * @param userDetails 当前登录用户
     * @return 统一返回结果
     */
    @GetMapping("/list")  // 行注：应用 @GetMapping 注解
    // 行注：定义列表Files方法
    public Result<List<FileDTO>> listFiles(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @RequestParam(value = "keyword", required = false) String keyword) {  // 行注：应用 @RequestParam 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        return Result.success(fileService.listFiles(userId, keyword));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 创建带票据的文件访问地址。
     *
     * @param userDetails 当前登录用户
     * @param fileUrl 文件 URL
     * @return 统一返回结果
     */
    @GetMapping("/access-url")  // 行注：应用 @GetMapping 注解
    // 行注：定义创建访问URL方法
    public Result<FileAccessDTO> createAccessUrl(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @RequestParam("fileUrl") String fileUrl) {  // 行注：应用 @RequestParam 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        FileAccessDTO response = fileService.createAccessUrl(userId, fileUrl);  // 行注：初始化response
        log.info("File access url created, userId={}, fileUrl={}", userId, fileUrl);  // 行注：执行初始化操作
        auditLogService.recordSuccess("FILE_ACCESS_TICKET_CREATE", userId, "FILE_URL", fileUrl, fileUrl);  // 行注：调用记录Success
        return Result.success(response);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 通过票据访问文件资源。
     *
     * @param ticket 票据字符串
     * @param userDetails 当前登录用户
     * @return 文件响应
     */
    @GetMapping("/access/{ticket}")  // 行注：应用 @GetMapping 注解
    // 行注：定义访问文件方法
    public ResponseEntity<Resource> accessFile(
            @PathVariable("ticket") String ticket,  // 行注：应用 @PathVariable 注解
            @AuthenticationPrincipal UserDetails userDetails  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails 注解
    // 行注：开始当前语句对应的代码块
    ) {
        Long requestUserId = resolveRequestUserId(userDetails);  // 行注：初始化请求用户ID
        SysFile sysFile = fileAccessTicketService.consumeFile(ticket, requestUserId);
        if (sysFile == null) {
            log.warn("File access rejected, reason=invalid_ticket, ticket={}, requestUserId={}", ticket, requestUserId);
            auditLogService.recordFailure("FILE_ACCESS_BY_TICKET", null, "FILE_TICKET", ticket, "invalid_ticket");
            return ResponseEntity.notFound().build();
        }
        var stored = fileService.openStoredContent(sysFile);
        if (stored == null) {
            log.warn("File access rejected, reason=file_unavailable, ticket={}, fileId={}, path={}",
                    ticket, sysFile.getId(), sysFile.getFilePath());
            auditLogService.recordFailure("FILE_ACCESS_BY_TICKET", null, "FILE", sysFile.getId(), "file_unavailable");
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (sysFile.getFileType() != null && !sysFile.getFileType().isBlank()) {
            mediaType = MediaType.parseMediaType(sysFile.getFileType());
        }
        String disposition = mediaType.getType().equalsIgnoreCase("image") ? "inline" : "attachment";
        String displayName = StringUtils.hasText(sysFile.getOriginalName()) ? sysFile.getOriginalName() : stored.filename();
        String asciiName = StringUtils.hasText(sysFile.getStoredName()) ? sysFile.getStoredName() : stored.filename();
        log.info("File access granted, ticket={}, fileId={}, originalName={}", ticket, sysFile.getId(), sysFile.getOriginalName());
        auditLogService.recordSuccess("FILE_ACCESS_BY_TICKET", sysFile.getUserId(), "FILE", sysFile.getId(), sysFile.getOriginalName());
        var body = stored.toResource();
        var builder = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, HttpContentDispositionUtils.build(disposition, displayName, asciiName))
                .contentType(mediaType);
        if (stored.contentLength() > 0) {
            builder.contentLength(stored.contentLength());
        }
        return builder.body(body);
    }  // 行注：结束当前代码块

    /**
     * 删除文件。
     *
     * @param userDetails 当前登录用户
     * @param id 主键 ID
     * @return 统一返回结果
     */
    @DeleteMapping("/{id}")  // 行注：应用 @DeleteMapping 注解
    // 行注：定义删除文件方法
    public Result<Void> deleteFile(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable("id") Long id) {  // 行注：应用 @PathVariable 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        fileService.deleteFile(userId, id);  // 行注：调用删除文件
        log.info("File delete success, userId={}, fileId={}", userId, id);  // 行注：执行初始化操作
        auditLogService.recordSuccess("FILE_DELETE", userId, "FILE", id, "");  // 行注：调用记录Success
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义解析请求用户ID方法
    private static Long resolveRequestUserId(UserDetails userDetails) {
        // 行注：判断是否满足当前条件
        if (userDetails == null || userDetails.getUsername() == null) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：尝试执行可能失败的逻辑
        try {
            return Long.parseLong(userDetails.getUsername());  // 行注：返回处理结果
        // 行注：执行当前方法调用
        } catch (NumberFormatException exception) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
