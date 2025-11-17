package com.cotato.backend.user.controller;



import com.cotato.backend.common.dto.DataResponse;
import com.cotato.backend.domain.user.entity.CustomOAuth2User;
import com.cotato.backend.user.dto.UserInfoResponse;
import com.cotato.backend.user.service.UserService;
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
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DataResponse<UserInfoResponse>> getMe(
            @AuthenticationPrincipal CustomOAuth2User user) {
        UserInfoResponse userInfo = userService.getUserInfo(user.getUserId());
        return ResponseEntity.ok(DataResponse.from(userInfo));
    }
}