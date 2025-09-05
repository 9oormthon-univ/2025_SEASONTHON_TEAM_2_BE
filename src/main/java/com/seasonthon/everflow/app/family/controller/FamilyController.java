package com.seasonthon.everflow.app.family.controller;

import com.seasonthon.everflow.app.family.dto.FamilyCreateRequestDto;
import com.seasonthon.everflow.app.family.dto.FamilyInfoResponseDto;
import com.seasonthon.everflow.app.family.dto.FamilyJoinAnswerDto;
import com.seasonthon.everflow.app.family.dto.FamilyJoinRequestDto;
import com.seasonthon.everflow.app.family.dto.FamilyMembersResponseDto;
import com.seasonthon.everflow.app.family.dto.FamilyVerificationResponseDto;
import com.seasonthon.everflow.app.family.service.FamilyService;
import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Family API", description = "가족 관리 API")
@RestController
@RequestMapping("/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @Operation(summary = "가족 생성", description = "최초 로그인(GUEST) 사용자가 가족을 생성합니다.")
    @PostMapping("/create")
    public ApiResponse<Void> createFamily(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FamilyCreateRequestDto request) {
        if (userDetails == null) {
            return ApiResponse.onFailure("AUTH401", "인증 정보가 없습니다.", null);
        }
        familyService.createFamily(userDetails.getUserId(), request);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "가족 가입 요청", description = "가족 코드로 검증 질문을 조회합니다. (1단계)")
    @PostMapping("/join/request")
    public ApiResponse<FamilyVerificationResponseDto> joinFamilyRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FamilyJoinRequestDto request) {
        if (userDetails == null) {
            return ApiResponse.onFailure("AUTH401", "인증 정보가 없습니다.", null);
        }
        FamilyVerificationResponseDto response = familyService.getVerificationQuestion(userDetails.getUserId(), request);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "가족 가입 완료", description = "가족 검증 질문에 답변하여 가입을 완료합니다. (2단계)")
    @PostMapping("/join/complete")
    public ApiResponse<Void> joinFamilyComplete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FamilyJoinAnswerDto request) {
        if (userDetails == null) {
            return ApiResponse.onFailure("AUTH401", "인증 정보가 없습니다.", null);
        }
        familyService.joinFamily(userDetails.getUserId(), request);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "내 가족 정보 조회", description = "현재 로그인한 사용자의 가족 정보를 조회합니다.")
    @GetMapping("/my")
    public ApiResponse<FamilyInfoResponseDto> getMyFamily(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ApiResponse.onFailure("AUTH401", "인증 정보가 없습니다.", null);
        }
        FamilyInfoResponseDto response = familyService.getMyFamily(userDetails.getUserId());
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "내 가족 구성원 정보 조회", description = "현재 로그인한 사용자의 가족 구성원 정보를 조회합니다.")
    @GetMapping("/my/members")
    public ApiResponse<FamilyMembersResponseDto> getFamilyMembers(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ApiResponse.onFailure("AUTH401", "인증 정보가 없습니다.", null);
        }
        FamilyMembersResponseDto response = familyService.getFamilyMembers(userDetails.getUserId());
        return ApiResponse.onSuccess(response);
    }
}
