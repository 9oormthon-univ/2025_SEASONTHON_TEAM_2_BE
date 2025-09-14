package com.seasonthon.everflow.app.family.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FamilyVerificationDetailResponseDto {
    private String familyName;
    private String leaderName;
    private List<String> profileImageUrls;
    private int memberCount;
}
