package com.seasonthon.everflow.app.notification.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
@RequiredArgsConstructor
public class EmitterRepository {

    // 모든 Emitter를 저장하는 ConcurrentHashMap
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Emitter를 저장한다.
     */
    public void save(String emitterId, SseEmitter sseEmitter) {
        emitters.put(emitterId, sseEmitter);
    }

    /**
     * Emitter를 삭제한다.
     */
    public void deleteById(String emitterId) {
        emitters.remove(emitterId);
    }

    /**
     * 해당 사용자 ID와 관련된 모든 Emitter를 찾는다.
     */
    public Map<String, SseEmitter> findAllByUserId(String userId) {
        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(userId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
