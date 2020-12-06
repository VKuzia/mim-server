package com.mimteam.mimserver.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimteam.mimserver.model.entities.chat.ChatMessageEntity;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.repositories.ChatMessagesRepository;
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

    private final int chatId = 1;
    private final String content1 = "Text message1";
    private final String content2 = "Text message2";

    private ChatMessageEntity chatMessageEntity1;
    private ChatMessageEntity chatMessageEntity2;

    @BeforeEach
    public void init() {
        chatMessageEntity1 = new ChatMessageEntity();
        chatMessageEntity1.setChatId(chatId);
        chatMessageEntity1.setContent(content1);

        chatMessageEntity2 = new ChatMessageEntity();
        chatMessageEntity2.setChatId(chatId);
        chatMessageEntity2.setContent(content2);
    }

    @AfterEach
    public void tearDown() {
        chatMessagesRepository.deleteAll();
    }

    @Test
    public void chatMessageListEmpty() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/chats/2/messages"))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        ResponseDTO responseDto = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDto.getResponseType());
        Assertions.assertNotNull(responseDto.getResponseMessage());
        Assertions.assertEquals("[]", responseDto.getResponseMessage());
    }

    @Test
    void chatMessageListNotEmpty() throws Exception {
        chatMessagesRepository.save(chatMessageEntity1);
        chatMessagesRepository.save(chatMessageEntity2);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/chats/" + chatId + "/messages"))
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
        String expectedResponseMessage = "[";
        for (int i = 0; i < chatMessageEntities.size(); i++) {
            expectedResponseMessage += objectMapper.writeValueAsString(chatMessageEntities.get(i));
            if (i != chatMessageEntities.size() - 1) {
                expectedResponseMessage += ",";
            }
        }
        expectedResponseMessage += "]";
        return expectedResponseMessage;
    }
}
