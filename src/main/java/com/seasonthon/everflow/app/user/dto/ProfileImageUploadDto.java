package com.seasonthon.everflow.app.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class ProfileImageUploadDto {
    @Schema(description = "이미지 파일(jpg/png/webp, ≤5MB)", type = "string", format = "binary")
    private MultipartFile file;
}
