package com.mimteam.mimserver.services;

import com.mimteam.mimserver.TestingUtils;
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

    private static final String CHAT_NAME = "Chat Name";
    private static final Integer CHAT_ID = 1;
    private static final String INVITATION_KEY = "abc9";

    private ResponseEntity<ResponseDTO> errorResponseEntity;
    private ResponseEntity<ResponseDTO> successResponseEntity;

    private ChatEntity chatEntity;

    @BeforeEach
    public void init() {
        errorResponseEntity = ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        successResponseEntity = ResponseEntity.ok().build();

        chatEntity = new ChatEntity(CHAT_NAME);
        chatEntity.setInvitationKey(INVITATION_KEY);
        chatEntity.setChatId(CHAT_ID);
    }

    @Test
    public void createChatSuccess() {
        Mockito.when(chatsRepository.save(Mockito.any())).thenAnswer(invocation -> {
            ((ChatEntity) invocation.getArgument(0)).setChatId(CHAT_ID);
            return null;
        });

        Assertions.assertNotNull(chatService.createChat(CHAT_NAME));

        ArgumentCaptor<ChatEntity> chatCaptor = ArgumentCaptor.forClass(ChatEntity.class);
        Mockito.verify(chatsRepository).save(chatCaptor.capture());

        Assertions.assertEquals(CHAT_NAME, chatCaptor.getValue().getName());
        Assertions.assertNull(chatCaptor.getValue().getUserList());
    }

    @Test
    public void getChatUserIdListNotExists() {
        Mockito.when(chatsRepository.findById(Mockito.anyInt())).thenReturn(Optional.empty());

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(() -> ResponseBuilder.buildError(ResponseDTO.ResponseType.CHAT_NOT_EXISTS))
                    .thenReturn(errorResponseEntity);

            ResponseEntity<ResponseDTO> response = chatService.getChatUserList(CHAT_ID);
            Assertions.assertEquals(errorResponseEntity, response);
        }

        Mockito.verify(chatsRepository).findById(CHAT_ID);
    }

    @Test
    public void getChatUserIdListEmpty() {
        ChatEntity spyChatEntity = Mockito.spy(chatEntity);
        Mockito.when(spyChatEntity.getUserList()).thenReturn(new HashSet<>());
        Mockito.when(chatsRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(spyChatEntity));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            ResponseBuilder mockResponseBuilder = TestingUtils.createMockSuccessResponseBuilder(new ArrayList<>());
            responseBuilder.when(ResponseBuilder::builder).thenReturn(mockResponseBuilder);

            ResponseEntity<ResponseDTO> response = chatService.getChatUserList(CHAT_ID);
            Assertions.assertEquals(successResponseEntity, response);
        }

        Mockito.verify(chatsRepository).findById(CHAT_ID);
    }

    @Test
    public void getChatUserIdListNonEmpty() {
        ArrayList<Integer> chatUserIdList = new ArrayList<>(Arrays.asList(3, 1));
        chatUserIdList.sort(Integer::compareTo);
        Set<UserToChatEntity> userToChatIds = getUsersForChat(chatUserIdList);

        ChatEntity spyChatEntity = Mockito.spy(chatEntity);
        Mockito.when(spyChatEntity.getUserList()).thenReturn(userToChatIds);
        Mockito.when(chatsRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(spyChatEntity));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            ResponseBuilder mockResponseBuilder = TestingUtils.createMockSuccessResponseBuilder();
            responseBuilder.when(ResponseBuilder::builder).thenReturn(mockResponseBuilder);

            ResponseEntity<ResponseDTO> response = chatService.getChatUserList(CHAT_ID);
            Assertions.assertEquals(successResponseEntity, response);
        }

        Mockito.verify(chatsRepository).findById(CHAT_ID);
    }

    @Test
    public void getChatInvitationKeyChatNotExists() {
        Mockito.when(chatsRepository.findById(Mockito.anyInt())).thenReturn(Optional.empty());

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(() -> ResponseBuilder.buildError(ResponseDTO.ResponseType.CHAT_NOT_EXISTS))
                    .thenReturn(errorResponseEntity);

            ResponseEntity<ResponseDTO> response = chatService.getChatInvitationKey(CHAT_ID);
            Assertions.assertEquals(errorResponseEntity, response);
        }

        Mockito.verify(chatsRepository).findById(CHAT_ID);
    }

    @Test
    public void getChatInvitationKeySuccess() {
        Mockito.when(chatsRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(chatEntity));
        Mockito.when(chatsRepository.save(Mockito.any())).thenAnswer(invocation -> {
            ((ChatEntity) invocation.getArgument(0)).setInvitationKey(INVITATION_KEY);
            return null;
        });

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            ResponseBuilder mockResponseBuilder = Mockito.mock(ResponseBuilder.class);

            Mockito.when(mockResponseBuilder.responseType(ResponseDTO.ResponseType.OK)).thenReturn(mockResponseBuilder);
            Mockito.when(mockResponseBuilder.body(Mockito.anyString())).thenReturn(mockResponseBuilder);
            Mockito.when(mockResponseBuilder.build()).thenReturn(successResponseEntity);

            responseBuilder.when(ResponseBuilder::builder).thenReturn(mockResponseBuilder);
            ResponseEntity<ResponseDTO> response = chatService.getChatInvitationKey(CHAT_ID);
            Assertions.assertEquals(successResponseEntity, response);
        }

        ArgumentCaptor<ChatEntity> chatCaptor = ArgumentCaptor.forClass(ChatEntity.class);
        Mockito.verify(chatsRepository).save(chatCaptor.capture());
        Mockito.verify(chatsRepository).findById(CHAT_ID);

        Assertions.assertEquals(INVITATION_KEY, chatCaptor.getValue().getInvitationKey());
    }

    @Test
    public void getChatByIdNotExists() {
        Mockito.when(chatsRepository.findByInvitationKey(Mockito.anyString())).thenReturn(Optional.empty());

        Optional<ChatEntity> chat = chatService.getChatByInvitationKey(INVITATION_KEY);
        Assertions.assertTrue(chat.isEmpty());

        Mockito.verify(chatsRepository).findByInvitationKey(INVITATION_KEY);
    }

    @Test
    public void getChatByIdSuccess() {
        Mockito.when(chatsRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(chatEntity));

        Optional<ChatEntity> chat = chatService.getChatById(CHAT_ID);
        Assertions.assertTrue(chat.isPresent());
        Assertions.assertEquals(chatEntity, chat.get());

        Mockito.verify(chatsRepository).findById(CHAT_ID);
    }

    @Test
    public void getChatByInvitationKeyNotExists() {
        Mockito.when(chatsRepository.findByInvitationKey(Mockito.anyString())).thenReturn(Optional.empty());

        Optional<ChatEntity> chat = chatService.getChatByInvitationKey(INVITATION_KEY);
        Assertions.assertTrue(chat.isEmpty());

        Mockito.verify(chatsRepository).findByInvitationKey(INVITATION_KEY);
    }

    @Test
    public void getChatByInvitationKeySuccess() {
        Mockito.when(chatsRepository.findByInvitationKey(Mockito.anyString())).thenReturn(Optional.of(chatEntity));

        Optional<ChatEntity> chat = chatService.getChatByInvitationKey(INVITATION_KEY);
        Assertions.assertTrue(chat.isPresent());
        Assertions.assertEquals(chatEntity, chat.get());

        Mockito.verify(chatsRepository).findByInvitationKey(INVITATION_KEY);
    }

    private Set<UserToChatEntity> getUsersForChat(ArrayList<Integer> userIdList) {
        HashSet<UserToChatEntity> users = new HashSet<>();
        for (Integer userId : userIdList) {
            UserEntity userEntity = createUserWithId(userId);

            UserToChatEntity userToChatEntity = new UserToChatEntity(new UserToChatId(userId, CHAT_ID));
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
