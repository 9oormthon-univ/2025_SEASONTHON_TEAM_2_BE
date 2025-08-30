package com.seasonthon.everflow.app.user.repository;

import com.seasonthon.everflow.app.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 이메일로 사용자 정보를 조회합니다.
     * @param email 조회할 이메일
     * @return 이메일에 해당하는 User 객체 (Optional)
     */
    Optional<User> findByEmail(String email);

    /**
     * 닉네임이 존재하는지 확인합니다.
     * @param nickname 확인할 닉네임
     * @return 닉네임이 존재하면 true
     */
    boolean existsByNickname(String nickname);

    Optional<User> findByRefreshToken(String refreshToken);
}