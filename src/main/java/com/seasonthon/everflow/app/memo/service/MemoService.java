package com.seasonthon.everflow.app.memo.service;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;

import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.memo.domain.Memo;
import com.seasonthon.everflow.app.memo.dto.MemoDto;
import com.seasonthon.everflow.app.memo.repository.MemoRepository;
import com.seasonthon.everflow.app.memo.dto.MemoMapper;
import com.seasonthon.everflow.app.user.domain.User;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class MemoService {
    private final MemoRepository memoRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MemoDto getOrCreate(Long userId) {
        Long familyId = resolveFamilyId(userId);
        return memoRepository.findByFamilyId(familyId)
                .map(MemoMapper::toDto)
                .orElseGet(() -> {
                    Memo memo = Memo.create(familyId, userId);
                    Memo saved = memoRepository.save(memo);
                    return MemoMapper.toDto(saved);
                });
    }

    @Transactional
    public MemoDto update(Long userId, Integer expectedVersion, String content) {
        Long familyId = resolveFamilyId(userId);
        Memo memo = memoRepository.findByFamilyId(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMO_NOT_FOUND));
        if (memo.getVersion() != expectedVersion) {
            throw new GeneralException(
                ErrorStatus.VERSION_CONFLICT
            );
        }
        if (content != null && content.length() > 800) {
            throw new GeneralException(ErrorStatus.MEMO_CONTENT_TOO_LONG);
        }
        memo.applyContent(content);
        Memo saved = memoRepository.save(memo);
        return MemoMapper.toDto(saved);
    }

    private Long resolveFamilyId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        if (user.getFamily() == null) {
            throw new GeneralException(ErrorStatus.NOT_IN_FAMILY_YET);
        }
        return user.getFamily().getId();
    }
}
