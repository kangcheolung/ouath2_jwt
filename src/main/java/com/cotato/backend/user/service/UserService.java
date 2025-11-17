package com.cotato.backend.user.service;

import com.cotato.backend.common.exception.EntityNotFoundException;
import com.cotato.backend.common.exception.ErrorCode;
import com.cotato.backend.domain.user.entity.User;
import com.cotato.backend.domain.user.repository.UserRepository;
import com.cotato.backend.user.dto.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<User> getList() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        return UserInfoResponse.from(user);
    }
}