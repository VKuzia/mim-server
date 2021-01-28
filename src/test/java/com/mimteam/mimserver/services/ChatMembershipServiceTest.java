package com.mimteam.mimserver.services;

import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatId;
import com.mimteam.mimserver.model.responses.ResponseBuilder;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.repositories.UsersToChatsRepository;
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

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ChatMembershipServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private ChatService chatService;
    @Mock
    private UsersToChatsRepository usersToChatsRepository;

    @InjectMocks
    private ChatMembershipService chatMembershipService;

    private static final Integer USER_ID = 1;
    private static final Integer CHAT_ID = 2;

    private ResponseEntity<ResponseDTO> errorResponseEntity;
    private ResponseEntity<ResponseDTO> successResponseEntity;

    private UserEntity userEntity;
    private ChatEntity chatEntity;
    private UserToChatEntity userToChatEntity;

    @BeforeEach
    public void init() {
        errorResponseEntity = ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        successResponseEntity = ResponseEntity.ok().build();

        userEntity = new UserEntity();
        userEntity.setUserId(USER_ID);
        chatEntity = new ChatEntity();
        chatEntity.setChatId(CHAT_ID);

        userToChatEntity = new UserToChatEntity(new UserToChatId(USER_ID, CHAT_ID));
        userToChatEntity.setUserEntity(userEntity);
        userToChatEntity.setChatEntity(chatEntity);
    }

    @Test
    public void joinChatUserNotExists() {
        Mockito.when(userService.getUserById(Mockito.anyInt())).thenReturn(Optional.empty());
        Mockito.lenient().when(chatService.getChatById(Mockito.anyInt())).thenReturn(Optional.of(chatEntity));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(() -> ResponseBuilder.buildError(ResponseDTO.ResponseType.USER_NOT_EXISTS))
                    .thenReturn(errorResponseEntity);

            ResponseEntity<ResponseDTO> response = chatMembershipService.joinChat(USER_ID, CHAT_ID);
            Assertions.assertEquals(errorResponseEntity, response);
        }

        Mockito.verify(userService).getUserById(USER_ID);
        Mockito.verify(usersToChatsRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void joinChatNotExists() {
        Mockito.lenient().when(userService.getUserById(Mockito.anyInt())).thenReturn(Optional.of(userEntity));
        Mockito.when(chatService.getChatById(Mockito.anyInt())).thenReturn(Optional.empty());

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(() -> ResponseBuilder.buildError(ResponseDTO.ResponseType.CHAT_NOT_EXISTS))
                    .thenReturn(errorResponseEntity);

            ResponseEntity<ResponseDTO> response = chatMembershipService.joinChat(USER_ID, CHAT_ID);
            Assertions.assertEquals(errorResponseEntity, response);
        }

        Mockito.verify(chatService).getChatById(CHAT_ID);
        Mockito.verify(usersToChatsRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void joinChatUserAlreadyInChat() {
        Mockito.when(userService.getUserById(Mockito.anyInt())).thenReturn(Optional.of(userEntity));
        Mockito.when(chatService.getChatById(Mockito.anyInt())).thenReturn(Optional.of(chatEntity));
        Mockito.when(usersToChatsRepository.findById(Mockito.any(UserToChatId.class)))
                .thenReturn(Optional.of(userToChatEntity));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(() -> ResponseBuilder.buildError(ResponseDTO.ResponseType.USER_ALREADY_IN_CHAT))
                    .thenReturn(errorResponseEntity);

            ResponseEntity<ResponseDTO> response = chatMembershipService.joinChat(USER_ID, CHAT_ID);
            Assertions.assertEquals(errorResponseEntity, response);
        }

        Mockito.verify(userService).getUserById(USER_ID);
        Mockito.verify(chatService).getChatById(CHAT_ID);
        Mockito.verify(usersToChatsRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void joinChatSuccess() {
        Mockito.when(userService.getUserById(Mockito.anyInt())).thenReturn(Optional.of(userEntity));
        Mockito.when(chatService.getChatById(Mockito.anyInt())).thenReturn(Optional.of(chatEntity));
        Mockito.when(usersToChatsRepository.findById(Mockito.any(UserToChatId.class))).thenReturn(Optional.empty());

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            ResponseBuilder mockResponseBuilder = createMockSuccessResponseBuilder();
            responseBuilder.when(ResponseBuilder::builder).thenReturn(mockResponseBuilder);

            ResponseEntity<ResponseDTO> response = chatMembershipService.joinChat(USER_ID, CHAT_ID);
            Assertions.assertEquals(successResponseEntity, response);
        }

        ArgumentCaptor<UserToChatEntity> userToChatCaptor = ArgumentCaptor.forClass(UserToChatEntity.class);
        Mockito.verify(userService).getUserById(USER_ID);
        Mockito.verify(chatService).getChatById(CHAT_ID);
        Mockito.verify(usersToChatsRepository).save(userToChatCaptor.capture());

        Assertions.assertEquals(USER_ID, userToChatCaptor.getValue().getId().getUserId());
        Assertions.assertEquals(CHAT_ID, userToChatCaptor.getValue().getId().getChatId());
        Assertions.assertEquals(userEntity, userToChatCaptor.getValue().getUserEntity());
        Assertions.assertEquals(chatEntity, userToChatCaptor.getValue().getChatEntity());
    }

    @Test
    public void leaveChatUserNotInChat() {
        Mockito.when(usersToChatsRepository.findById(Mockito.any(UserToChatId.class))).thenReturn(Optional.empty());

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(() -> ResponseBuilder.buildError(ResponseDTO.ResponseType.USER_NOT_IN_CHAT))
                    .thenReturn(errorResponseEntity);

            ResponseEntity<ResponseDTO> response = chatMembershipService.leaveChat(USER_ID, CHAT_ID);
            Assertions.assertEquals(errorResponseEntity, response);
        }

        ArgumentCaptor<UserToChatId> idCaptor = ArgumentCaptor.forClass(UserToChatId.class);
        Mockito.verify(usersToChatsRepository).findById(idCaptor.capture());
        Mockito.verify(usersToChatsRepository, Mockito.never()).delete(Mockito.any());

        Assertions.assertEquals(USER_ID, idCaptor.getValue().getUserId());
        Assertions.assertEquals(CHAT_ID, idCaptor.getValue().getChatId());
    }

    @Test
    public void leaveChatSuccess() {
        Mockito.when(usersToChatsRepository.findById(Mockito.any(UserToChatId.class)))
                .thenReturn(Optional.of(userToChatEntity));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(ResponseBuilder::buildSuccess).thenReturn(successResponseEntity);

            ResponseEntity<ResponseDTO> response = chatMembershipService.leaveChat(USER_ID, CHAT_ID);
            Assertions.assertEquals(successResponseEntity, response);
        }

        ArgumentCaptor<UserToChatId> idCaptor = ArgumentCaptor.forClass(UserToChatId.class);
        ArgumentCaptor<UserToChatEntity> userToChatCaptor = ArgumentCaptor.forClass(UserToChatEntity.class);
        Mockito.verify(usersToChatsRepository).findById(idCaptor.capture());
        Mockito.verify(usersToChatsRepository).delete(userToChatCaptor.capture());

        Assertions.assertEquals(USER_ID, idCaptor.getValue().getUserId());
        Assertions.assertEquals(CHAT_ID, idCaptor.getValue().getChatId());
        Assertions.assertEquals(USER_ID, userToChatCaptor.getValue().getId().getUserId());
        Assertions.assertEquals(CHAT_ID, userToChatCaptor.getValue().getId().getChatId());
        Assertions.assertEquals(userEntity, userToChatCaptor.getValue().getUserEntity());
        Assertions.assertEquals(chatEntity, userToChatCaptor.getValue().getChatEntity());
    }

    private ResponseBuilder createMockSuccessResponseBuilder() {
        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        Mockito.when(responseBuilder.responseType(ResponseDTO.ResponseType.OK)).thenReturn(responseBuilder);
        Mockito.when(responseBuilder.body(Mockito.any())).thenReturn(responseBuilder);
        Mockito.when(responseBuilder.build()).thenReturn(successResponseEntity);
        return responseBuilder;
    }
}
