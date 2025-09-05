package com.seasonthon.everflow.app.family.domain;

import com.seasonthon.everflow.app.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Entity
@Table(name = "families")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "family_name")
    private String familyName;

    @Column(name = "invite_code", nullable = false, unique = true)
    private String inviteCode;

    @Column(name = "verification_question")
    private String verificationQuestion;

    @Column(name = "verification_answer")
    private String verificationAnswer;

    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> members = new ArrayList<>();

    @Builder
    public Family(String familyName, String verificationQuestion, String verificationAnswer) {
        this.familyName = familyName;
        this.verificationQuestion = verificationQuestion;
        this.verificationAnswer = verificationAnswer;
        this.inviteCode = generateInviteCode();
    }

    public void addMember(User user) {
        this.members.add(user);
        user.setFamily(this);
    }

    private String generateInviteCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
