package com.mimteam.mimserver.controllers;

import com.mimteam.mimserver.TestingUtils;
import com.mimteam.mimserver.model.dto.ChatDTO;
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

import java.util.UUID;

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

    private static final String USER_UUID_TOKEN = "00000000-0000-0000-0000-000000000000";
    private static final String AUTHORIZATION_HEADER_TOKEN = "Bearer " + USER_UUID_TOKEN;

    private static final String CHAT_NAME = "Test Chat";
    private static final String USER_NAME = "Test User";
    private static final String LOGIN = "test_user";
    private static final String PASSWORD = "password";

    private ChatEntity chatEntity;
    private UserEntity userEntity;

    @BeforeEach
    public void init() {
        userEntity = new UserEntity(USER_NAME, LOGIN, PASSWORD);
        userEntity.setToken(USER_UUID_TOKEN);

        chatEntity = new ChatEntity(CHAT_NAME);
    }

    @AfterEach
    public void tearDown() {
        usersToChatsRepository.deleteAll();
        chatsRepository.deleteAll();
        usersRepository.deleteAll();
    }

    @Test
    public void signUpUserAlreadyExists() throws Exception {
        usersRepository.save(userEntity);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users/signup")
                .param("userName", USER_NAME)
                .param("login", LOGIN)
                .param("password", PASSWORD))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        ResponseDTO responseDTO = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.USER_ALREADY_EXISTS, responseDTO.getResponseType());
        Assertions.assertNull(responseDTO.getResponseMessage());
        Assertions.assertTrue(usersRepository.existsById(userEntity.getUserId()));
    }

    @Test
    public void signUpUserSuccess() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users/signup")
                .param("userName", USER_NAME)
                .param("login", LOGIN)
                .param("password", PASSWORD))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDTO = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDTO.getResponseType());
        Assertions.assertNull(responseDTO.getResponseMessage());
        Assertions.assertTrue(usersRepository.existsById(1));
    }

    @Test
    public void loginUserNotExists() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users/login")
                .param("login", LOGIN)
                .param("password", PASSWORD))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        ResponseDTO responseDTO = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.USER_NOT_EXISTS, responseDTO.getResponseType());
        Assertions.assertNull(responseDTO.getResponseMessage());
    }

    @Test
    public void loginUserIncorrectPassword() throws Exception {
        usersRepository.save(userEntity);

        String incorrectPassword = "pass";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users/login")
                .param("login", LOGIN)
                .param("password", incorrectPassword))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        ResponseDTO responseDTO = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.INCORRECT_PASSWORD, responseDTO.getResponseType());
        Assertions.assertNull(responseDTO.getResponseMessage());
    }

    @Test
    public void loginUserSuccess() throws Exception {
        usersRepository.save(userEntity);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users/login")
                .param("login", LOGIN)
                .param("password", PASSWORD)
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_TOKEN))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDTO = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDTO.getResponseType());
        Assertions.assertNotNull(responseDTO.getResponseMessage());
        Assertions.assertDoesNotThrow(() -> UUID.fromString(responseDTO.getResponseMessage()));
    }

    @Test
    public void getChatListEmpty() throws Exception {
        usersRepository.save(userEntity);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/users/chatlist")
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_TOKEN))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDTO = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDTO.getResponseType());
        Assertions.assertEquals("[]", responseDTO.getResponseMessage());
    }

    @Test
    public void getChatListSuccess() throws Exception {
        chatsRepository.save(chatEntity);
        usersRepository.save(userEntity);
        usersToChatsRepository.save(TestingUtils.buildUserToChatEntity(userEntity, chatEntity));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/users/chatlist")
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_TOKEN))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDTO = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDTO.getResponseType());
        Assertions.assertEquals("[" + TestingUtils.convertToString(new ChatDTO(chatEntity)) + "]",
                responseDTO.getResponseMessage());
    }

    @Test
    public void getUserIdSuccess() throws Exception {
        usersRepository.save(userEntity);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/users/getid")
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_TOKEN))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDTO = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDTO.getResponseType());
        Assertions.assertEquals(userEntity.getUserId().toString(), responseDTO.getResponseMessage());
    }
}
