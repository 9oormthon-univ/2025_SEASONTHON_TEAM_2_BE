package com.seasonthon.everflow.app.user.service;

import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.exception.GeneralException;
import com.seasonthon.everflow.app.s3.service.S3Service;
import com.seasonthon.everflow.app.user.domain.User;
import com.seasonthon.everflow.app.user.dto.UserNicknameUpdateDto;
import com.seasonthon.everflow.app.user.dto.UserProfileImageResponseDto;
import com.seasonthon.everflow.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    public void updateNickname(Long userId, UserNicknameUpdateDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        String trimmed = request.getNickname() == null ? "" : request.getNickname().trim();
        if (trimmed.isEmpty() || trimmed.length() > 5) {
            throw new GeneralException(ErrorStatus.VALIDATION_FAILED);
        }
        user.updateNickname(trimmed);
        userRepository.save(user);
    }

    public UserProfileImageResponseDto updateProfileImage(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new GeneralException(ErrorStatus.FILE_IS_EMPTY);
        }
        String ct = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        boolean ok = ct.startsWith("image/") &&
                (ct.contains("jpeg") || ct.contains("jpg") || ct.contains("png") || ct.contains("webp"));
        if (!ok) {
            throw new GeneralException(ErrorStatus.INVALID_IMAGE_FORMAT);
        }
        long max = 5L * 1024 * 1024;
        if (file.getSize() > max) {
            throw new GeneralException(ErrorStatus.IMAGE_SIZE_EXCEEDED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        String oldUrl = user.getProfileUrl();
        String keyPrefix = "profiles/" + userId + "/";
        String newUrl = s3Service.uploadWithPrefix(keyPrefix, file);

        if (oldUrl != null && !oldUrl.isBlank()) {
            s3Service.deleteByUrl(oldUrl);
        }

        user.updateProfileUrl(newUrl);
        userRepository.save(user);
        return new UserProfileImageResponseDto(newUrl);
    }
}
