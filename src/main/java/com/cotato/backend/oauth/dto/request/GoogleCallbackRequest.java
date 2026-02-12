package com.cotato.backend.oauth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "구글 로그인 콜백 요청")
public class GoogleCallbackRequest {

    @Schema(description = "구글 인가코드", example = "abc123def456...")
    private String code;

    @Schema(description = "프론트엔드 콜백 URI", example = "http://localhost:3000/callback")
    private String redirectUri;
}
