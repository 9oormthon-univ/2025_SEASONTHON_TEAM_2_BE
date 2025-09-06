package com.seasonthon.everflow.app.user.service;

import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.user.domain.User;
import com.seasonthon.everflow.app.user.dto.UserNicknameUpdateDto;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public void updateNickname(Long userId, UserNicknameUpdateDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        String newNickname = request.getNickname().trim();
        if (newNickname.isEmpty() || newNickname.length() > 5) {
            throw new GeneralException(ErrorStatus.VALIDATION_FAILED);
        }

        user.updateNickname(newNickname);
        userRepository.save(user);
    }
}
