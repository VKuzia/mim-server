package com.mimteam.mimserver.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimteam.mimserver.events.ChatMembershipEvent;
import com.mimteam.mimserver.handlers.EventHandler;
import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatId;
import com.mimteam.mimserver.model.messages.ChatMembershipMessage;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.repositories.ChatsRepository;
import com.mimteam.mimserver.repositories.UsersRepository;
import com.mimteam.mimserver.repositories.UsersToChatsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
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
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatsRepository chatsRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private UsersToChatsRepository usersToChatsRepository;

    @SpyBean
    private EventHandler eventHandler;

    private static final String chatName = "Test Chat";
    private static final String userName = "Test User";
    private static final String login = "test_user";
    private static final String password = "password";

    private UserEntity userEntity;
    private ChatEntity chatEntity;

    @BeforeEach
    public void init() {
        userEntity = new UserEntity(userName, login, password);
        chatEntity = new ChatEntity(chatName);
    }

    @AfterEach
    public void tearDown() {
        usersToChatsRepository.deleteAll();
        chatsRepository.deleteAll();
        usersRepository.deleteAll();
    }

    @Test
    public void createChatSuccess() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/chats/create")
                .param("chatName", chatName))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDto = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDto.getResponseType());
        Assertions.assertEquals("1", responseDto.getResponseMessage());
        Assertions.assertTrue(chatsRepository.existsById(1));
    }

    @Test
    public void createChatAlreadyExists() throws Exception {
        chatsRepository.save(new ChatEntity(chatName));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/chats/create")
                .param("chatName", chatName))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        ResponseDTO responseDto = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.CHAT_ALREADY_EXISTS, responseDto.getResponseType());
        Assertions.assertNull(responseDto.getResponseMessage());
    }

    @Test
    public void joinChatUserNotExists() throws Exception {
        chatsRepository.save(chatEntity);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/chats/" + chatEntity.getChatId() + "/join")
                .param("userId", "1"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        ResponseDTO responseDto = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.USER_NOT_EXISTS, responseDto.getResponseType());
        Assertions.assertNull(responseDto.getResponseMessage());

        Mockito.verify(eventHandler, Mockito.never()).post(Mockito.any());
        Assertions.assertEquals(0, usersToChatsRepository.count());
    }

    @Test
    public void joinChatNotExists() throws Exception {
        usersRepository.save(userEntity);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/chats/1/join")
                .param("userId", userEntity.getUserId().toString()))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        ResponseDTO responseDto = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.CHAT_NOT_EXISTS, responseDto.getResponseType());
        Assertions.assertNull(responseDto.getResponseMessage());

        Mockito.verify(eventHandler, Mockito.never()).post(Mockito.any());
        Assertions.assertEquals(0, usersToChatsRepository.count());
    }

    @Test
    public void joinChatUserAlreadyInChat() throws Exception {
        usersRepository.save(userEntity);
        chatsRepository.save(chatEntity);
        usersToChatsRepository.save(buildUserToChatEntity(userEntity, chatEntity));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/chats/" + chatEntity.getChatId() + "/join")
                .param("userId", userEntity.getUserId().toString()))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        ResponseDTO responseDto = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.USER_ALREADY_IN_CHAT, responseDto.getResponseType());
        Assertions.assertNull(responseDto.getResponseMessage());

        Mockito.verify(eventHandler, Mockito.never()).post(Mockito.any());
    }

    @Test
    public void joinChatSuccess() throws Exception {
        usersRepository.save(userEntity);
        chatsRepository.save(chatEntity);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/chats/" + chatEntity.getChatId() + "/join")
                .param("userId", userEntity.getUserId().toString()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDto = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDto.getResponseType());
        Assertions.assertNull(responseDto.getResponseMessage());

        ArgumentCaptor<ChatMembershipEvent> chatEventCaptor = ArgumentCaptor.forClass(ChatMembershipEvent.class);
        Mockito.verify(eventHandler).post(chatEventCaptor.capture());

        Assertions.assertEquals(userEntity.getUserId(), chatEventCaptor.getValue().getUserId());
        Assertions.assertEquals(chatEntity.getChatId(), chatEventCaptor.getValue().getChatId());
        Assertions.assertEquals(ChatMembershipMessage.ChatMembershipMessageType.JOIN,
                chatEventCaptor.getValue().getChatMembershipMessageType());
        Assertions.assertEquals(1, usersToChatsRepository.count());
    }

    @Test
    public void leaveChatUserNotInChat() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/chats/1/leave")
                .param("userId", "1"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        ResponseDTO responseDto = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.USER_NOT_IN_CHAT, responseDto.getResponseType());
        Assertions.assertNull(responseDto.getResponseMessage());

        Mockito.verify(eventHandler, Mockito.never()).post(Mockito.any());
    }

    @Test
    public void leaveChatSuccess() throws Exception {
        usersRepository.save(userEntity);
        chatsRepository.save(chatEntity);
        usersToChatsRepository.save(buildUserToChatEntity(userEntity, chatEntity));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/chats/" + chatEntity.getChatId() + "/leave")
                .param("userId", userEntity.getUserId().toString()))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDto = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDto.getResponseType());
        Assertions.assertNull(responseDto.getResponseMessage());

        ArgumentCaptor<ChatMembershipEvent> chatEventCaptor = ArgumentCaptor.forClass(ChatMembershipEvent.class);
        Mockito.verify(eventHandler).post(chatEventCaptor.capture());

        Assertions.assertEquals(userEntity.getUserId(), chatEventCaptor.getValue().getUserId());
        Assertions.assertEquals(chatEntity.getChatId(), chatEventCaptor.getValue().getChatId());
        Assertions.assertEquals(ChatMembershipMessage.ChatMembershipMessageType.LEAVE,
                chatEventCaptor.getValue().getChatMembershipMessageType());
        Assertions.assertEquals(0, usersToChatsRepository.count());
    }

    @Test
    public void chatUserListChatNotExists() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/chats/1/userlist"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        ResponseDTO responseDto = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.CHAT_NOT_EXISTS, responseDto.getResponseType());
        Assertions.assertNull(responseDto.getResponseMessage());
    }

    @Test
    public void chatUserListEmpty() throws Exception {
        chatsRepository.save(chatEntity);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/chats/" + chatEntity.getChatId() + "/userlist"))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDto = parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDto.getResponseType());
        Assertions.assertNotNull(responseDto.getResponseMessage());
        Assertions.assertEquals("[]", responseDto.getResponseMessage());
    }

    @Test void chatUserListNotEmpty() throws Exception {
        chatsRepository.save(chatEntity);
        usersRepository.save(userEntity);
        usersToChatsRepository.save(buildUserToChatEntity(userEntity, chatEntity));

        UserEntity oneMoreUserEntity = new UserEntity(userName + " copy", login + "_copy", password);
        usersRepository.save(oneMoreUserEntity);
        usersToChatsRepository.save(buildUserToChatEntity(oneMoreUserEntity, chatEntity));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/chats/" + chatEntity.getChatId() + "/userlist"))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDto = parseResponseDto(result);

        String expectedUserList = "[" + userEntity.getUserId() + "," + oneMoreUserEntity.getUserId() + "]";
        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDto.getResponseType());
        Assertions.assertNotNull(responseDto.getResponseMessage());
        Assertions.assertEquals(expectedUserList, responseDto.getResponseMessage());
    }

    private ResponseDTO parseResponseDto(MvcResult mvcResult)
            throws UnsupportedEncodingException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ResponseDTO.class);
    }

    private UserToChatEntity buildUserToChatEntity(UserEntity userEntity, ChatEntity chatEntity) {
        UserToChatId userToChatId = new UserToChatId(userEntity.getUserId(), chatEntity.getChatId());
        UserToChatEntity userToChatEntity = new UserToChatEntity(userToChatId);
        userToChatEntity.setUserEntity(userEntity);
        userToChatEntity.setChatEntity(chatEntity);
        return userToChatEntity;
    }
}
