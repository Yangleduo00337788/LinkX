package com.linkx.server.module.release.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.SysAppRelease;
import com.linkx.server.mapper.SysAppReleaseMapper;
import com.linkx.server.module.release.dto.LatestReleaseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ReleaseService {

    private final SysAppReleaseMapper releaseMapper;

    public LatestReleaseDTO getLatestPublished(String platform) {
        if (!StringUtils.hasText(platform)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "platform 不能为空");
        }
        String normalized = platform.trim().toLowerCase();
        LambdaQueryWrapper<SysAppRelease> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAppRelease::getPlatform, normalized)
                .eq(SysAppRelease::getPublished, 1)
                .orderByDesc(SysAppRelease::getCreateTime)
                .last("LIMIT 1");
        SysAppRelease release = releaseMapper.selectOne(wrapper);
        if (release == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "暂无已发布版本");
        }
        return LatestReleaseDTO.builder()
                .platform(release.getPlatform())
                .version(release.getVersion())
                .downloadUrl(release.getDownloadUrl())
                .releaseNotes(release.getReleaseNotes())
                .forceUpdate(release.getForceUpdate() != null && release.getForceUpdate() == 1)
                .build();
    }
}