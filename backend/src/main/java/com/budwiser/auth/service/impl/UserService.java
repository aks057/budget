package com.budwiser.auth.service.impl;

import com.budwiser.auth.dto.UpdateProfileRequest;
import com.budwiser.auth.dto.UserDto;
import com.budwiser.auth.entity.User;
import com.budwiser.auth.mapper.IUserMapper;
import com.budwiser.auth.repository.IUserRepository;
import com.budwiser.auth.service.IUserService;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.exception.ResourceNotFoundException;
import com.budwiser.security.service.ICurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
  private final IUserRepository userRepository;
  private final IUserMapper userMapper;
  private final ICurrentUserProvider currentUserProvider;

  @Override
  @Transactional(readOnly = true)
  public UserDto getCurrentUser() {
    return userMapper.toDto(getOwned(currentUserProvider.getUserId()));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public UserDto updateCurrentUser(UpdateProfileRequest request) {
    User user = getOwned(currentUserProvider.getUserId());
    user.setFullName(request.getFullName().trim());
    user.setCurrency(request.getCurrency());
    log.info("[updateCurrentUser] profile updated, userId: {}", user.getId());
    return userMapper.toDto(user);
  }

  private User getOwned(Long userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException(userId, ErrorCode.USER_NOT_FOUND));
  }
}
