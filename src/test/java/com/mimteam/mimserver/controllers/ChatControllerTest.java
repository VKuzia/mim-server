package com.mimteam.mimserver.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.mimteam.mimserver.TestingUtils;
import com.mimteam.mimserver.events.ChatMembershipEvent;
import com.mimteam.mimserver.handlers.EventHandler;
import com.mimteam.mimserver.model.dto.ChatDTO;
import com.mimteam.mimserver.model.dto.MessageDTO;
import com.mimteam.mimserver.model.dto.UserDTO;
import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.ChatMessageEntity;
import com.mimteam.mimserver.model.messages.ChatMembershipMessage;
import com.mimteam.mimserver.model.messages.TextMessage;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.repositories.ChatMessagesRepository;
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
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    private UsersToChatsRepository usersToChatsRepository;

    private int chatId = 1;
    private static final String CONTENT_1 = "Text message1";
    private static final String CONTENT_2 = "Text message2";

    private static final String USER_UUID_TOKEN = "00000000-0000-0000-0000-000000000000";
    private static final String AUTHORIZATION_HEADER_TOKEN = "Bearer " + USER_UUID_TOKEN;

    private static final String CHAT_NAME = "Test Chat";
    private static final String USER_NAME = "Test User";
    private static final String INVITATION_KEY = "abcd1234";

    private static final String LOGIN = "test_user";
    private static final String PASSWORD = "password";

    private ChatMessageEntity chatMessageEntity1;
    private ChatMessageEntity chatMessageEntity2;
    private UserEntity userEntity;
    private ChatEntity chatEntity;

    @SpyBean
    private EventHandler eventHandler;

    @BeforeEach
    public void init() {
        chatMessageEntity1 = new ChatMessageEntity();
        chatMessageEntity1.setChatId(chatId);
        chatMessageEntity1.setContent(CONTENT_1);

        chatMessageEntity2 = new ChatMessageEntity();
        chatMessageEntity2.setChatId(chatId);
        chatMessageEntity2.setContent(CONTENT_2);

        chatEntity = new ChatEntity();
        chatEntity.setName(CHAT_NAME);
        chatEntity.setInvitationKey(INVITATION_KEY);

        userEntity = new UserEntity(USER_NAME, LOGIN, PASSWORD);
        userEntity.setToken(USER_UUID_TOKEN);
        usersRepository.save(userEntity);
    }

    @AfterEach
    public void tearDown() {
        chatMessagesRepository.deleteAll();
        usersToChatsRepository.deleteAll();
        chatsRepository.deleteAll();
        usersRepository.deleteAll();
    }

    @Test
    public void createChatSuccess() throws Exception {
        chatEntity.setChatId(2);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/chats/create")
                .param("chatName", CHAT_NAME)
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_TOKEN))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDto = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDto.getResponseType());
        Assertions.assertNotNull(responseDto.getResponseMessage());
        Assertions.assertEquals(TestingUtils.convertToString(new ChatDTO(chatEntity)),
                responseDto.getResponseMessage());

        Assertions.assertEquals(1, chatsRepository.count());
        Assertions.assertEquals(1, usersToChatsRepository.count());
    }

    @Test
    public void joinChatNotExists() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/chats/join")
                .param("invitationKey", INVITATION_KEY)
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_TOKEN))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();

        ResponseDTO responseDto = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.CHAT_NOT_EXISTS, responseDto.getResponseType());
        Assertions.assertNull(responseDto.getResponseMessage());

        Mockito.verify(eventHandler, Mockito.never()).post(Mockito.any());
        Assertions.assertEquals(0, usersToChatsRepository.count());
    }

    @Test
    public void joinChatUserAlreadyInChat() throws Exception {
        chatsRepository.save(chatEntity);
        usersToChatsRepository.save(TestingUtils.buildUserToChatEntity(userEntity, chatEntity));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/chats/join")
                .param("invitationKey", INVITATION_KEY)
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_TOKEN))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        ResponseDTO responseDto = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.USER_ALREADY_IN_CHAT, responseDto.getResponseType());
        Assertions.assertNull(responseDto.getResponseMessage());

        Mockito.verify(eventHandler, Mockito.never()).post(Mockito.any());
    }

    @Test
    public void joinChatSuccess() throws Exception {
        chatsRepository.save(chatEntity);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/chats/join")
                .param("invitationKey", INVITATION_KEY)
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_TOKEN))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDto = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDto.getResponseType());
        Assertions.assertNotNull(responseDto.getResponseMessage());
        Assertions.assertEquals(TestingUtils.convertToString(new ChatDTO(chatEntity)),
                responseDto.getResponseMessage());

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
        chatsRepository.save(chatEntity);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/chats/" + chatEntity.getChatId() + "/leave")
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_TOKEN))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        ResponseDTO responseDto = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.USER_NOT_IN_CHAT, responseDto.getResponseType());
        Assertions.assertNull(responseDto.getResponseMessage());

        Mockito.verify(eventHandler, Mockito.never()).post(Mockito.any());
        Assertions.assertEquals(0, usersToChatsRepository.count());
    }

    @Test
    public void leaveChatSuccess() throws Exception {
        chatsRepository.save(chatEntity);
        usersToChatsRepository.save(TestingUtils.buildUserToChatEntity(userEntity, chatEntity));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/chats/" + chatEntity.getChatId() + "/leave")
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_TOKEN))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDto = TestingUtils.parseResponseDto(result);

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
    public void getUserListChatNotExists() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/chats/1/userlist")
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_TOKEN))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        ResponseDTO responseDTO = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.CHAT_NOT_EXISTS, responseDTO.getResponseType());
        Assertions.assertNull(responseDTO.getResponseMessage());
    }

    @Test
    public void getUserListEmpty() throws Exception {
        chatsRepository.save(chatEntity);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/chats/" + chatEntity.getChatId() + "/userlist")
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
        usersToChatsRepository.save(TestingUtils.buildUserToChatEntity(userEntity, chatEntity));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/chats/" + chatEntity.getChatId() + "/userlist")
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_TOKEN))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDTO = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDTO.getResponseType());
        Assertions.assertEquals("[" + TestingUtils.convertToString(new UserDTO(userEntity)) + "]",
                responseDTO.getResponseMessage());
    }

    @Test
    public void getInvitationKeyUserNotInChat() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/chats/" + chatId + "/invitation")
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_TOKEN))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();

        ResponseDTO responseDto = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.USER_NOT_IN_CHAT, responseDto.getResponseType());
        Assertions.assertNull(responseDto.getResponseMessage());
    }

    @Test
    public void getInvitationKeySuccess() throws Exception {
        chatsRepository.save(chatEntity);
        usersToChatsRepository.save(TestingUtils.buildUserToChatEntity(userEntity, chatEntity));

        MvcResult result =
                mockMvc.perform(MockMvcRequestBuilders.get("/chats/" + chatEntity.getChatId() + "/invitation")
                        .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_TOKEN))
                        .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                        .andReturn();

        ResponseDTO responseDto = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDto.getResponseType());
        Assertions.assertNotNull(responseDto.getResponseMessage());
    }

    @Test
    public void getMessageHistoryChatNotExists() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/chats/2/messages")
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_TOKEN))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();

        ResponseDTO responseDto = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.CHAT_NOT_EXISTS, responseDto.getResponseType());
        Assertions.assertNull(responseDto.getResponseMessage());
    }

    @Test
    public void getMessageHistoryEmpty() throws Exception {
        chatsRepository.save(chatEntity);
        chatId = chatEntity.getChatId();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/chats/" + chatId + "/messages")
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_TOKEN))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        ResponseDTO responseDto = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDto.getResponseType());
        Assertions.assertNotNull(responseDto.getResponseMessage());
        Assertions.assertEquals("[]", responseDto.getResponseMessage());
    }

    @Test
    public void getMessageHistoryNotEmpty() throws Exception {
        chatsRepository.save(chatEntity);
        chatId = chatEntity.getChatId();
        chatMessageEntity1.setChatId(chatId);
        chatMessageEntity2.setChatId(chatId);
        chatMessagesRepository.save(chatMessageEntity1);
        chatMessagesRepository.save(chatMessageEntity2);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/chats/" + chatId + "/messages")
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_TOKEN))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        ResponseDTO responseDto = TestingUtils.parseResponseDto(result);

        Assertions.assertEquals(ResponseDTO.ResponseType.OK, responseDto.getResponseType());
        Assertions.assertNotNull(responseDto.getResponseMessage());

        String expectedResponseMessage =
                chatMessageEntitiesToDtoString(Arrays.asList(chatMessageEntity1, chatMessageEntity2));
        Assertions.assertEquals(expectedResponseMessage, responseDto.getResponseMessage());
    }

    private String chatMessageEntitiesToDtoString(List<ChatMessageEntity> chatMessageEntities)
            throws JsonProcessingException {
        List<MessageDTO> dtoList = chatMessageEntities.stream()
                .map(TextMessage::new)
                .map(TextMessage::toDataTransferObject)
                .collect(Collectors.toList());
        return TestingUtils.convertToString(dtoList);
    }
}
