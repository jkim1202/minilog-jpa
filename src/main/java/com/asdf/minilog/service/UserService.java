package com.asdf.minilog.service;

import com.asdf.minilog.dto.UserRequestDto;
import com.asdf.minilog.dto.UserResponseDto;
import com.asdf.minilog.entity.Role;
import com.asdf.minilog.entity.User;
import com.asdf.minilog.exception.NotAuthorizedException;
import com.asdf.minilog.exception.UserNotFoundException;
import com.asdf.minilog.repository.UserRepository;
import com.asdf.minilog.security.MinilogUserDetails;
import com.asdf.minilog.util.EntityDtoMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
  private final UserRepository userRepository;

  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public List<UserResponseDto> getUsers() {
    return userRepository.findAll().stream().map(EntityDtoMapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public Optional<UserResponseDto> getUserById(Long userId) {
    return userRepository.findById(userId).map(EntityDtoMapper::toDto);
  }

  @Transactional(readOnly = true)
  public UserResponseDto getUserByUsername(String username) {
    return userRepository
        .findByUsername(username)
        .map(EntityDtoMapper::toDto)
        .orElseThrow(
            () ->
                new UserNotFoundException(
                    String.format("해당 이름(%s)을 가진 사용자를 찾을 수 없습니다.", username)));
  }

  public UserResponseDto createUser(UserRequestDto userRequestDto) {
    if (userRepository.findByUsername(userRequestDto.getUsername()).isPresent()) {
      throw new IllegalArgumentException("이미 존재하는 사용자 이름입니다.");
    }
    // User 권한
    HashSet<Role> roles = new HashSet<>();
    roles.add(Role.ROLE_AUTHOR);
    // Admin 권한
    if (userRequestDto.getUsername().equals("admin")) {
      roles.add(Role.ROLE_ADMIN);
    }

    User savedUser =
        userRepository.save(
            User.builder()
                .username(userRequestDto.getUsername())
                .password(userRequestDto.getPassword())
                .roles(roles)
                .build());
    return EntityDtoMapper.toDto(savedUser);
  }

  public UserResponseDto updateUser(
      MinilogUserDetails userDetails, Long userId, UserRequestDto userRequestDto) {
    if (!userDetails.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(Role.ROLE_ADMIN.name()))
        && !userDetails.getId().equals(userId)) {
      throw new NotAuthorizedException("권한이 없습니다.");
    }
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new UserNotFoundException(
                        String.format("해당 아이디(%d)를 가진 사용자를 찾을 수 없습니다.", userId)));
    user.setUsername(userRequestDto.getUsername());
    user.setPassword(userRequestDto.getPassword());

    var updatedUser = userRepository.save(user);
    return EntityDtoMapper.toDto(user);
  }

  public void deleteUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new UserNotFoundException(
                        String.format("해당 아이디(%d)를 가진 사용자를 찾을 수 없습니다.", userId)));
    userRepository.deleteById(userId);
  }
}
