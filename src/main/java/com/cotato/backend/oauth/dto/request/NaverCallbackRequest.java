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
@Schema(description = "네이버 로그인 콜백 요청")
public class NaverCallbackRequest {

    @Schema(description = "네이버 인가코드", example = "abc123def456...")
    private String code;

    @Schema(description = "프론트엔드 콜백 URI", example = "http://localhost:3000/callback")
    private String redirectUri;

    @Schema(description = "state 파라미터 (CSRF 방지)", example = "random_state_string")
    private String state;
}
