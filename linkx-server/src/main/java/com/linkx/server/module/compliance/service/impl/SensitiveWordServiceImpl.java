package com.linkx.server.module.compliance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.SysSensitiveHitLog;
import com.linkx.server.entity.SysSensitiveWord;
import com.linkx.server.mapper.SysSensitiveHitLogMapper;
import com.linkx.server.mapper.SysSensitiveWordMapper;
import com.linkx.server.module.compliance.dto.SensitiveCheckResult;
import com.linkx.server.module.compliance.service.SensitiveWordService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private static final int ACTION_BLOCK = 1;
    private static final int MATCH_CONTAINS = 1;
    private static final int MATCH_WHOLE = 2;

    private final SysSensitiveWordMapper wordMapper;
    private final SysSensitiveHitLogMapper hitLogMapper;

    private volatile List<SysSensitiveWord> cached = List.of();

    @PostConstruct
    @Override
    public void refreshCache() {
        LambdaQueryWrapper<SysSensitiveWord> w = new LambdaQueryWrapper<>();
        w.eq(SysSensitiveWord::getEnabled, 1);
        cached = wordMapper.selectList(w);
    }

    @Override
    public SensitiveCheckResult checkText(Long userId, String text, String source, boolean logHit) {
        if (!StringUtils.hasText(text)) {
            return SensitiveCheckResult.builder().blocked(false).build();
        }
        String normalized = text.toLowerCase(Locale.ROOT);
        SysSensitiveWord hit = null;
        for (SysSensitiveWord word : cached) {
            if (!StringUtils.hasText(word.getWord())) {
                continue;
            }
            String w = word.getWord().toLowerCase(Locale.ROOT);
            boolean matched = word.getMatchMode() != null && word.getMatchMode() == MATCH_WHOLE
                    ? normalized.equals(w)
                    : normalized.contains(w);
            if (matched) {
                hit = word;
                break;
            }
        }
        if (hit == null) {
            return SensitiveCheckResult.builder().blocked(false).build();
        }
        boolean block = hit.getAction() != null && hit.getAction() == ACTION_BLOCK;
        if (logHit && userId != null) {
            String snippet = text.length() > 200 ? text.substring(0, 200) : text;
            recordHit(userId, hit.getId(), hit.getWord(), snippet, source, block, null);
        }
        return SensitiveCheckResult.builder()
                .blocked(block)
                .matchedWord(hit.getWord())
                .wordId(hit.getId())
                .action(hit.getAction() != null ? hit.getAction() : ACTION_BLOCK)
                .build();
    }

    @Override
    public void recordHit(Long userId, Long wordId, String matchedWord, String snippet, String source,
                          boolean blocked, Long messageId) {
        SysSensitiveHitLog log = new SysSensitiveHitLog();
        log.setUserId(userId);
        log.setWordId(wordId);
        log.setMatchedWord(matchedWord);
        log.setContentSnippet(snippet);
        log.setSource(source != null ? source : "CHAT_SEND");
        log.setBlocked(blocked ? 1 : 0);
        log.setMessageId(messageId);
        hitLogMapper.insert(log);
    }

    @Override
    public Page<SysSensitiveWord> pageWords(int page, int size, String keyword, Integer enabled) {
        LambdaQueryWrapper<SysSensitiveWord> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.like(SysSensitiveWord::getWord, keyword.trim());
        }
        if (enabled != null) {
            w.eq(SysSensitiveWord::getEnabled, enabled);
        }
        w.orderByDesc(SysSensitiveWord::getUpdateTime);
        return wordMapper.selectPage(new Page<>(page, size), w);
    }

    @Override
    public void createWord(SysSensitiveWord word) {
        if (!StringUtils.hasText(word.getWord())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "敏感词不能为空");
        }
        word.setWord(word.getWord().trim());
        if (word.getMatchMode() == null) {
            word.setMatchMode(MATCH_CONTAINS);
        }
        if (word.getAction() == null) {
            word.setAction(ACTION_BLOCK);
        }
        if (word.getEnabled() == null) {
            word.setEnabled(1);
        }
        wordMapper.insert(word);
        refreshCache();
    }

    @Override
    public void updateWord(SysSensitiveWord word) {
        if (word.getId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        wordMapper.updateById(word);
        refreshCache();
    }

    @Override
    public void deleteWord(Long id) {
        wordMapper.deleteById(id);
        refreshCache();
    }

    @Override
    public Page<SysSensitiveHitLog> pageHits(int page, int size, Long userId) {
        LambdaQueryWrapper<SysSensitiveHitLog> w = new LambdaQueryWrapper<>();
        if (userId != null) {
            w.eq(SysSensitiveHitLog::getUserId, userId);
        }
        w.orderByDesc(SysSensitiveHitLog::getCreateTime);
        return hitLogMapper.selectPage(new Page<>(page, size), w);
    }
}