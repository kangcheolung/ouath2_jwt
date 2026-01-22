package com.cotato.backend.user.controller;

import com.cotato.backend.common.dto.DataResponse;
import com.cotato.backend.domain.user.entity.User;
import com.cotato.backend.user.dto.UserInfoResponse;
import com.cotato.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DataResponse<UserInfoResponse>> getMe(
        @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        UserInfoResponse userInfo = userService.getUserInfo(user.getId());
        return ResponseEntity.ok(DataResponse.from(userInfo));
    }
}