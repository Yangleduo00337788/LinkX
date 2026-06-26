package com.linkx.server.module.compliance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.entity.SysSensitiveHitLog;
import com.linkx.server.entity.SysSensitiveWord;
import com.linkx.server.module.compliance.dto.SensitiveCheckResult;

/**
 * 敏感词库与发送前检测。
 */
public interface SensitiveWordService {

    SensitiveCheckResult checkText(Long userId, String text, String source, boolean logHit);

    void recordHit(Long userId, Long wordId, String matchedWord, String snippet, String source, boolean blocked, Long messageId);

    Page<SysSensitiveWord> pageWords(int page, int size, String keyword, Integer enabled);

    void createWord(SysSensitiveWord word);

    void updateWord(SysSensitiveWord word);

    void deleteWord(Long id);

    void refreshCache();

    Page<SysSensitiveHitLog> pageHits(int page, int size, Long userId);
}