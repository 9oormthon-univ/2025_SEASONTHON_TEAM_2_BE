package com.seasonthon.everflow.app.memo.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "memos",
        uniqueConstraints = @UniqueConstraint(name = "uq_memos_family", columnNames = "family_id"),
        indexes = @Index(name = "idx_memos_updated_at", columnList = "updated_at")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Memo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_id", nullable = false, unique = true)
    private Long familyId;

    /** 메모 본문 (최대 800자) */
    @Column(nullable = false, length = 800)
    private String content;

    /** 낙관적 락 */
    @Version
    @Column(nullable = false)
    private int version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "datetime(6)")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime(6)")
    private LocalDateTime updatedAt;

    private Memo(Long familyId) {
        this.familyId = familyId;
        this.content = "";
    }

    public static Memo create(Long familyId) {
        return new Memo(familyId);
    }

    public void applyContent(String newContent) {
        this.content = (newContent == null) ? "" : newContent;
    }
}