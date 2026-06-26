package com.linkx.server.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.SysAppRelease;
import com.linkx.server.mapper.SysAppReleaseMapper;
import com.linkx.server.module.admin.dto.AdminCreateReleaseRequest;
import com.linkx.server.module.admin.dto.AdminReleaseListItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReleaseService {

    private final SysAppReleaseMapper releaseMapper;

    public Page<AdminReleaseListItemDTO> list(int page, int size, String platform) {
        LambdaQueryWrapper<SysAppRelease> wrapper = new LambdaQueryWrapper<>();
        if (platform != null && !platform.isBlank()) {
            wrapper.eq(SysAppRelease::getPlatform, platform.trim());
        }
        wrapper.orderByDesc(SysAppRelease::getCreateTime);
        Page<SysAppRelease> raw = releaseMapper.selectPage(new Page<>(page, size), wrapper);
        Page<AdminReleaseListItemDTO> result = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        result.setRecords(raw.getRecords().stream().map(this::toDto).toList());
        return result;
    }

    @Transactional
    public AdminReleaseListItemDTO create(AdminCreateReleaseRequest req, Long adminId, String adminUsername,
                                          String clientIp, AdminAuditService audit) {
        SysAppRelease r = new SysAppRelease();
        r.setPlatform(req.getPlatform().trim());
        r.setVersion(req.getVersion().trim());
        r.setDownloadUrl(req.getDownloadUrl());
        r.setReleaseNotes(req.getReleaseNotes());
        r.setForceUpdate(Boolean.TRUE.equals(req.getForceUpdate()) ? 1 : 0);
        r.setPublished(Boolean.TRUE.equals(req.getPublished()) ? 1 : 0);
        releaseMapper.insert(r);
        audit.record(adminId, adminUsername, "RELEASE_CREATE", "release", String.valueOf(r.getId()),
                r.getPlatform() + " " + r.getVersion(), clientIp);
        return toDto(r);
    }

    @Transactional
    public void setPublished(Long id, boolean published, Long adminId, String adminUsername, String clientIp,
                             AdminAuditService audit) {
        SysAppRelease r = releaseMapper.selectById(id);
        if (r == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        r.setPublished(published ? 1 : 0);
        releaseMapper.updateById(r);
        audit.record(adminId, adminUsername, published ? "RELEASE_PUBLISH" : "RELEASE_UNPUBLISH",
                "release", String.valueOf(id), null, clientIp);
    }

    private AdminReleaseListItemDTO toDto(SysAppRelease r) {
        return AdminReleaseListItemDTO.builder()
                .id(r.getId())
                .platform(r.getPlatform())
                .version(r.getVersion())
                .downloadUrl(r.getDownloadUrl())
                .releaseNotes(r.getReleaseNotes())
                .forceUpdate(r.getForceUpdate())
                .published(r.getPublished())
                .createTime(r.getCreateTime())
                .build();
    }
}