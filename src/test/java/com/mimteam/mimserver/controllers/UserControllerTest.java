package com.mimteam.mimserver.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.repositories.ChatsRepository;
import com.mimteam.mimserver.repositories.UsersRepository;
import com.mimteam.mimserver.repositories.UsersToChatsRepository;
import org.springframework.http.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.UnsupportedEncodingException;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(value = "/reset_sequence.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private ChatsRepository chatsRepository;
    @Autowired
    private UsersToChatsRepository usersToChatsRepository;

    private static final String userUuidToken = "00000000-0000-0000-0000-000000000000";
    private static final String authorizationHeaderToken = "Bearer " + userUuidToken;

    private static final String chatName = "Test Chat";
    private static final String userName = "Test User";
    private static final String login = "test_user";
    private static final String password = "password";

    private ChatEntity chatEntity;
    private UserEntity userEntity;

    @BeforeEach
    public void init() {
        userEntity = new UserEntity(userName, login, password);
        userEntity.setToken(userUuidToken);

        chatEntity = new ChatEntity(chatName);
    }

    @AfterEach
    public void tearDown() {
        usersToChatsRepository.deleteAll();
        chatsRepository.deleteAll();
        usersRepository.deleteAll();
    }

    @Test
    public void signUpUserSuccess() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users/signup")
                .param("userName", userName)
                .param("login", login)
                .param("password", password))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDTO = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDTO.getResponseType());
        Assertions.assertTrue(usersRepository.existsById(1));
    }

    @Test
    public void signUpUserAlreadyExists() throws Exception {
        usersRepository.save(userEntity);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users/signup")
                .param("userName", userName)
                .param("login", login)
                .param("password", password))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        ResponseDTO responseDTO = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.USER_ALREADY_EXISTS, responseDTO.getResponseType());
        Assertions.assertTrue(usersRepository.existsById(1));
    }

    @Test
    public void loginUserSuccess() throws Exception {
        usersRepository.save(userEntity);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users/login")
                .param("login", login)
                .param("password", password)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeaderToken))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDTO = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDTO.getResponseType());
        Assertions.assertNotNull(responseDTO.getResponseMessage());
    }

    @Test
    public void loginUserIncorrectPassword() throws Exception {
        usersRepository.save(userEntity);

        String incorrectPassword = "pass";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users/login")
                .param("login", login)
                .param("password", incorrectPassword))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        ResponseDTO responseDTO = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.INCORRECT_PASSWORD, responseDTO.getResponseType());
        Assertions.assertTrue(usersRepository.existsById(userEntity.getUserId()));
    }

    @Test
    public void loginUserNotExists() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users/login")
                .param("login", login)
                .param("password", password))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        ResponseDTO responseDTO = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.USER_NOT_EXISTS, responseDTO.getResponseType());
    }

    @Test
    public void getChatIdListEmpty() throws Exception {
        usersRepository.save(userEntity);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(
                "/users/chatlist")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeaderToken))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDTO = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDTO.getResponseType());
        Assertions.assertEquals("[]", responseDTO.getResponseMessage());
    }

    @Test
    public void getChatIdList() throws Exception {
        chatsRepository.save(chatEntity);
        usersRepository.save(userEntity);

        mockMvc.perform(MockMvcRequestBuilders.post("/chats/" + chatEntity.getChatId() + "/join")
                .param("userId", userEntity.getUserId().toString())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeaderToken));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(
                "/users/chatlist")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeaderToken))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDTO = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDTO.getResponseType());
        Assertions.assertEquals("[" + chatEntity.getChatId() + "]", responseDTO.getResponseMessage());
    }

    private ResponseDTO parseResponseDto(MvcResult mvcResult)
            throws UnsupportedEncodingException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseDTO.class);
    }
}
