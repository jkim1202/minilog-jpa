package com.asdf.minilog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.asdf.minilog.dto.UserRequestDto;
import com.asdf.minilog.dto.UserResponseDto;
import com.asdf.minilog.exception.UserNotFoundException;
import com.asdf.minilog.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
@MockitoBean(types = JpaMetamodelMappingContext.class)
class UserControllerTest {
  final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired MockMvc mockMvc;
  @MockitoBean UserService userService;

  AutoCloseable closeable;

  @BeforeEach
  void openMocks() {
    closeable = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void releaseMocks() throws Exception {
    closeable.close();
  }

  @Test
  void testGetUsers() throws Exception {
    List<UserResponseDto> userResponseDtoList =
        List.of(
            UserResponseDto.builder().id(1L).username("Test User").build(),
            UserResponseDto.builder().id(2L).username("Test User 2").build());

    UserResponseDto.builder().id(1L).username("Test User").build();
    when(userService.getUsers()).thenReturn(userResponseDtoList);

    mockMvc
        .perform(get("/api/v1/user"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].username").value("Test User"))
        .andExpect(jsonPath("$[1].id").value(2L))
        .andExpect(jsonPath("$[1].username").value("Test User 2"));
  }

  @Test
  void testGetUserById() throws Exception {
    UserResponseDto userResponseDto =
        UserResponseDto.builder().id(1L).username("Test User").build();
    when(userService.getUserById(anyLong())).thenReturn(Optional.of(userResponseDto));

    mockMvc
        .perform(get("/api/v1/user/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.username").value("Test User"));
  }

  @Test
  void testCreateUser() throws Exception {
    UserRequestDto userRequestDto =
        UserRequestDto.builder().username("Test User").password("password").build();
    UserResponseDto userResponseDto =
        UserResponseDto.builder().id(1L).username("Test User").build();
    when(userService.createUser(any(UserRequestDto.class))).thenReturn(userResponseDto);

    mockMvc
        .perform(
            post("/api/v1/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.username").value("Test User"));
  }

  @Test
  void testUpdateUser() throws Exception {
    UserRequestDto userRequestDto =
        UserRequestDto.builder().username("Test User").password("password").build();
    UserResponseDto userResponseDto =
        UserResponseDto.builder().id(1L).username("Test User").build();
    when(userService.updateUser(anyLong(), any(UserRequestDto.class))).thenReturn(userResponseDto);

    mockMvc
        .perform(
            put("/api/v1/user/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.username").value("Test User"));
  }

  @Test
  void testDeleteUser() throws Exception {
    mockMvc.perform(delete("/api/v1/user/1")).andExpect(status().isNoContent());
  }

  @Test
  void testGlobalExceptionHandler() throws Exception {
    when(userService.getUserById(anyLong())).thenThrow(new UserNotFoundException("Test Exception"));

    mockMvc
        .perform(get("/api/v1/user/999"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Test Exception"));
  }
}
