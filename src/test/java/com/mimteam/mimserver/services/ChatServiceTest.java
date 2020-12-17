package com.mimteam.mimserver.services;

import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatId;
import com.mimteam.mimserver.model.responses.ResponseBuilder;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.repositories.ChatsRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {
    @Mock
    private ChatsRepository chatsRepository;

    @InjectMocks
    private ChatService chatService;

    private static final String chatName = "Chat Name";
    private static final Integer chatId = 1;

    private ResponseEntity<ResponseDTO> errorResponseEntity;
    private ResponseEntity<ResponseDTO> successResponseEntity;

    private ChatEntity chatEntity;

    @BeforeEach
    public void init() {
        errorResponseEntity = ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        successResponseEntity = ResponseEntity.ok().build();

        chatEntity = new ChatEntity(chatName);
        chatEntity.setChatId(chatId);
    }

    @Test
    public void createChatAlreadyExists() {
        Mockito.when(chatsRepository.findByName(Mockito.anyString())).thenReturn(Optional.of(chatEntity));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(() -> ResponseBuilder.buildError(ResponseDTO.ResponseType.CHAT_ALREADY_EXISTS))
                    .thenReturn(errorResponseEntity);

            ResponseEntity<ResponseDTO> response = chatService.createChat(chatName);
            Assertions.assertEquals(errorResponseEntity, response);
        }

        Mockito.verify(chatsRepository).findByName(chatName);
        Mockito.verify(chatsRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void createChatSuccess() {
        Mockito.when(chatsRepository.findByName(Mockito.anyString())).thenReturn(Optional.empty());
        Mockito.when(chatsRepository.save(Mockito.any())).thenAnswer(invocation -> {
            ((ChatEntity) invocation.getArgument(0)).setChatId(chatId);
            return null;
        });

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            ResponseBuilder mockResponseBuilder = createMockSuccessResponseBuilder(chatId);
            responseBuilder.when(ResponseBuilder::builder).thenReturn(mockResponseBuilder);

            ResponseEntity<ResponseDTO> response = chatService.createChat(chatName);
            Assertions.assertEquals(successResponseEntity, response);
        }

        ArgumentCaptor<ChatEntity> chatCaptor = ArgumentCaptor.forClass(ChatEntity.class);
        Mockito.verify(chatsRepository).findByName(chatName);
        Mockito.verify(chatsRepository).save(chatCaptor.capture());

        Assertions.assertEquals(chatName, chatCaptor.getValue().getName());
        Assertions.assertNull(chatCaptor.getValue().getUserList());
    }

    @Test
    public void getChatUserIdListNotExists() {
        Mockito.when(chatsRepository.findById(Mockito.anyInt())).thenReturn(Optional.empty());

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(() -> ResponseBuilder.buildError(ResponseDTO.ResponseType.CHAT_NOT_EXISTS))
                    .thenReturn(errorResponseEntity);

            ResponseEntity<ResponseDTO> response = chatService.getChatUserList(chatId);
            Assertions.assertEquals(errorResponseEntity, response);
        }

        Mockito.verify(chatsRepository).findById(chatId);
    }

    @Test
    public void getChatUserIdListEmpty() {
        ChatEntity spyChatEntity = Mockito.spy(chatEntity);
        Mockito.when(spyChatEntity.getUserList()).thenReturn(new HashSet<>());
        Mockito.when(chatsRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(spyChatEntity));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            ResponseBuilder mockResponseBuilder = createMockSuccessResponseBuilder(new ArrayList<>());
            responseBuilder.when(ResponseBuilder::builder).thenReturn(mockResponseBuilder);

            ResponseEntity<ResponseDTO> response = chatService.getChatUserList(chatId);
            Assertions.assertEquals(successResponseEntity, response);
        }

        Mockito.verify(chatsRepository).findById(chatId);
    }

    @Test
    public void getChatUserIdListNonEmpty() {
        ArrayList<Integer> expectedChatUserIdList = new ArrayList<>(Arrays.asList(3, 1));
        expectedChatUserIdList.sort(Integer::compareTo);
        Set<UserToChatEntity> userToChatIds = getUsersForChat(expectedChatUserIdList);

        ChatEntity spyChatEntity = Mockito.spy(chatEntity);
        Mockito.when(spyChatEntity.getUserList()).thenReturn(userToChatIds);
        Mockito.when(chatsRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(spyChatEntity));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            ResponseBuilder mockResponseBuilder = createMockSuccessResponseBuilder(expectedChatUserIdList);
            responseBuilder.when(ResponseBuilder::builder).thenReturn(mockResponseBuilder);

            ResponseEntity<ResponseDTO> response = chatService.getChatUserList(chatId);
            Assertions.assertEquals(successResponseEntity, response);
        }

        Mockito.verify(chatsRepository).findById(chatId);
    }

    @Test
    public void getChatByIdNotExists() {
        Mockito.when(chatsRepository.findById(Mockito.anyInt())).thenReturn(Optional.empty());

        Optional<ChatEntity> chat = chatService.getChatById(chatId);
        Assertions.assertTrue(chat.isEmpty());

        Mockito.verify(chatsRepository).findById(chatId);
    }

    @Test
    public void getChatByIdSuccess() {
        Mockito.when(chatsRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(chatEntity));

        Optional<ChatEntity> chat = chatService.getChatById(chatId);
        Assertions.assertTrue(chat.isPresent());
        Assertions.assertEquals(chatEntity, chat.get());

        Mockito.verify(chatsRepository).findById(chatId);
    }

    private ResponseBuilder createMockSuccessResponseBuilder(Object body) {
        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        Mockito.when(responseBuilder.responseType(ResponseDTO.ResponseType.OK)).thenReturn(responseBuilder);
        Mockito.when(responseBuilder.body(body)).thenReturn(responseBuilder);
        Mockito.when(responseBuilder.build()).thenReturn(successResponseEntity);
        return responseBuilder;
    }

    private Set<UserToChatEntity> getUsersForChat(ArrayList<Integer> userIdList) {
        HashSet<UserToChatEntity> users = new HashSet<>();
        for (Integer userId : userIdList) {
            UserEntity userEntity = createUserWithId(userId);

            UserToChatEntity userToChatEntity = new UserToChatEntity(new UserToChatId(userId, chatId));
            userToChatEntity.setUserEntity(userEntity);
            users.add(userToChatEntity);
        }
        return users;
    }

    private UserEntity createUserWithId(Integer userId) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(userId);
        return userEntity;
    }
}
