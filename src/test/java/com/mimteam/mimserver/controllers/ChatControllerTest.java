package com.mimteam.mimserver.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.ChatMessageEntity;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.repositories.ChatMessagesRepository;
import com.mimteam.mimserver.repositories.ChatsRepository;
import com.mimteam.mimserver.repositories.UsersRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(value = "/reset_sequence.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatMessagesRepository chatMessagesRepository;
    @Autowired
    private ChatsRepository chatsRepository;
    @Autowired
    private UsersRepository usersRepository;

    private int chatId = 1;
    private static final String content1 = "Text message1";
    private static final String content2 = "Text message2";
    private static final String userUuidToken = "00000000-0000-0000-0000-000000000000";
    private static final String authorizationHeaderToken = "Bearer " + userUuidToken;
    private static final int userId = 1;

    private ChatMessageEntity chatMessageEntity1;
    private ChatMessageEntity chatMessageEntity2;
    private ChatEntity chatEntity;

    @BeforeEach
    public void init() {
        chatMessageEntity1 = new ChatMessageEntity();
        chatMessageEntity1.setChatId(chatId);
        chatMessageEntity1.setContent(content1);

        chatMessageEntity2 = new ChatMessageEntity();
        chatMessageEntity2.setChatId(chatId);
        chatMessageEntity2.setContent(content2);

        chatEntity = new ChatEntity();

        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(userId);
        userEntity.setToken(userUuidToken);
        usersRepository.save(userEntity);
    }

    @AfterEach
    public void tearDown() {
        chatMessagesRepository.deleteAll();
        chatsRepository.deleteAll();
        usersRepository.deleteAll();
    }

    @Test
    public void chatMessageListChatNotExists() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/chats/2/messages")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeaderToken))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();

        ResponseDTO responseDto = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.CHAT_NOT_EXISTS, responseDto.getResponseType());
        Assertions.assertNull(responseDto.getResponseMessage());
    }

    @Test
    public void chatMessageListEmpty() throws Exception {
        chatsRepository.save(chatEntity);
        chatId = chatEntity.getChatId();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/chats/" + chatId + "/messages")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeaderToken))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        ResponseDTO responseDto = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDto.getResponseType());
        Assertions.assertNotNull(responseDto.getResponseMessage());
        Assertions.assertEquals("[]", responseDto.getResponseMessage());
    }

    @Test
    void chatMessageListNotEmpty() throws Exception {
        chatsRepository.save(chatEntity);
        chatId = chatEntity.getChatId();
        chatMessageEntity1.setChatId(chatId);
        chatMessageEntity2.setChatId(chatId);
        chatMessagesRepository.save(chatMessageEntity1);
        chatMessagesRepository.save(chatMessageEntity2);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/chats/" + chatId + "/messages")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeaderToken))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDto = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDto.getResponseType());
        Assertions.assertNotNull(responseDto.getResponseMessage());

        String expectedResponseMessage =
                chatMessageEntitiesToString(Arrays.asList(chatMessageEntity1, chatMessageEntity2));
        Assertions.assertEquals(expectedResponseMessage, responseDto.getResponseMessage());
    }

    private ResponseDTO parseResponseDto(MvcResult mvcResult)
            throws UnsupportedEncodingException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseDTO.class);
    }

    private String chatMessageEntitiesToString(List<ChatMessageEntity> chatMessageEntities) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(chatMessageEntities);
    }
}
