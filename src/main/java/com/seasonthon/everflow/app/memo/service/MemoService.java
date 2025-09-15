package com.seasonthon.everflow.app.memo.service;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;

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

    /* 버전은 클라이언트에서 받지 않고 서버에서 자동 관리 */
    @Transactional
    public MemoDto update(Long userId, String content) {
        Long familyId = resolveFamilyId(userId);
        Memo memo = memoRepository.findByFamilyId(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMO_NOT_FOUND));

        /* 본문 길이 검증 (800자 초과 금지) */
        if (content != null && content.length() > 800) {
            throw new GeneralException(ErrorStatus.MEMO_CONTENT_TOO_LONG);
        }

        /* 내용 반영: @Version으로 버전은 자동 증가됨 */
        /* 최근 수정자(updated_by) 기록 */
        memo.applyContent(content, userId);
        try {
            Memo saved = memoRepository.save(memo);
            return MemoMapper.toDto(saved);
        } catch (jakarta.persistence.OptimisticLockException e) {
            throw new GeneralException(ErrorStatus.VERSION_CONFLICT);
        }
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
