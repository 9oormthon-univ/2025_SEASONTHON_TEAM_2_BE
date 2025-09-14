package com.seasonthon.everflow.app.family.controller;

import com.seasonthon.everflow.app.family.dto.FamilyCreateRequestDto;
import com.seasonthon.everflow.app.family.dto.FamilyEditRequestDto;
import com.seasonthon.everflow.app.family.dto.FamilyInfoResponseDto;
import com.seasonthon.everflow.app.family.dto.FamilyJoinAnswerDto;
import com.seasonthon.everflow.app.family.dto.FamilyJoinRequestDto;
import com.seasonthon.everflow.app.family.dto.FamilyMembersResponseDto;
import com.seasonthon.everflow.app.family.dto.FamilyVerificationResponseDto;
import com.seasonthon.everflow.app.family.dto.JoinAttemptResponseDto;
import com.seasonthon.everflow.app.family.dto.PendingJoinRequestDto;
import com.seasonthon.everflow.app.family.service.FamilyService;
import com.seasonthon.everflow.app.global.code.dto.ApiResponse;
import com.seasonthon.everflow.app.global.code.status.ErrorStatus;
import com.seasonthon.everflow.app.global.oauth.domain.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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

    @PostMapping("/join/complete")
    public ApiResponse<JoinAttemptResponseDto> joinFamilyComplete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FamilyJoinAnswerDto request) {
        if (userDetails == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED.getCode(), ErrorStatus.UNAUTHORIZED.getMessage(), null);
        }
        JoinAttemptResponseDto result = familyService.joinFamily(userDetails.getUserId(), request);
        return ApiResponse.onSuccess(result.getStatus().getCode(), result.getStatus().getMessage(), result);
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

    @Operation(summary = "가입 요청 승인", description = "가족 생성자가 대기중인 가입 요청을 승인합니다.")
    @PostMapping("/pending/{requestId}/approve")
    public ApiResponse<Void> approveJoinRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long requestId) {
        familyService.approveJoinRequest(userDetails.getUserId(), requestId);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "가입 요청 거절", description = "가족 생성자가 대기중인 가입 요청을 거절합니다.")
    @PostMapping("/pending/{requestId}/reject")
    public ApiResponse<Void> rejectJoinRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long requestId) {
        familyService.rejectJoinRequest(userDetails.getUserId(), requestId);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "가족 가입 대기 목록 조회", description = "가족 생성자(리더)가 승인/거절해야 할 가입 요청 목록을 조회합니다.")
    @GetMapping("/pending")
    public ApiResponse<List<PendingJoinRequestDto>> getPendingJoinRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ApiResponse.onFailure("AUTH401", "인증 정보가 없습니다.", null);
        }
        List<PendingJoinRequestDto> response = familyService.getPendingJoinRequests(userDetails.getUserId());
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "가족 프로필 일괄 수정", description = "그룹장이 가족명/검증 질문/검증 답변을 한 번에 수정합니다.")
    @PatchMapping("/edit")
    public ApiResponse<FamilyInfoResponseDto> editFamily(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FamilyEditRequestDto request) {
        if (userDetails == null) {
            return ApiResponse.onFailure("AUTH401", "인증 정보가 없습니다.", null);
        }
        FamilyInfoResponseDto result = familyService.editFamilyProfile(userDetails.getUserId(), request);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "가족 코드 존재 여부 확인", description = "가족 코드가 유효한지 true/false로 반환합니다.")
    @GetMapping("/verify")
    public ApiResponse<Boolean> verifyFamilyCode(@RequestParam("code") String inviteCode) {
        boolean exists = familyService.doesFamilyExistByCode(inviteCode);
        return ApiResponse.onSuccess(exists);
    }
}
