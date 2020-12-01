package com.mimteam.mimserver.services;

import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatId;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.repositories.UsersToChatsRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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

    private static final Integer userId = 1;
    private static final Integer chatId = 2;

    private UserEntity userEntity;
    private ChatEntity chatEntity;
    private UserToChatEntity userToChatEntity;

    @BeforeEach
    public void init() {
        userEntity = new UserEntity();
        userEntity.setUserId(userId);
        chatEntity = new ChatEntity();
        chatEntity.setChatId(chatId);

        userToChatEntity = new UserToChatEntity(new UserToChatId(userId, chatId));
        userToChatEntity.setUserEntity(userEntity);
        userToChatEntity.setChatEntity(chatEntity);
    }

    @Test
    public void joinChatUserNotExists() {
        Mockito.when(userService.getUserById(Mockito.anyInt())).thenReturn(Optional.empty());
        Mockito.lenient().when(chatService.getChatById(Mockito.anyInt())).thenReturn(Optional.of(chatEntity));

        ResponseEntity<ResponseDTO> response = chatMembershipService.joinChat(userId, chatId);
        Assertions.assertFalse(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(ResponseDTO.ResponseType.USER_NOT_EXISTS, response.getBody().getResponseType());

        Mockito.verify(userService).getUserById(userId);
        Mockito.verify(usersToChatsRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void joinChatNotExists() {
        Mockito.lenient().when(userService.getUserById(Mockito.anyInt())).thenReturn(Optional.of(userEntity));
        Mockito.when(chatService.getChatById(Mockito.anyInt())).thenReturn(Optional.empty());

        ResponseEntity<ResponseDTO> response = chatMembershipService.joinChat(userId, chatId);
        Assertions.assertFalse(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(ResponseDTO.ResponseType.CHAT_NOT_EXISTS, response.getBody().getResponseType());

        Mockito.verify(chatService).getChatById(chatId);
        Mockito.verify(usersToChatsRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void joinChatUserAlreadyInChat() {
        Mockito.when(userService.getUserById(Mockito.anyInt())).thenReturn(Optional.of(userEntity));
        Mockito.when(chatService.getChatById(Mockito.anyInt())).thenReturn(Optional.of(chatEntity));
        Mockito.when(usersToChatsRepository.findById(Mockito.any(UserToChatId.class)))
                .thenReturn(Optional.of(userToChatEntity));

        ResponseEntity<ResponseDTO> response = chatMembershipService.joinChat(userId, chatId);
        Assertions.assertFalse(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(ResponseDTO.ResponseType.USER_ALREADY_IN_CHAT, response.getBody().getResponseType());

        Mockito.verify(userService).getUserById(userId);
        Mockito.verify(chatService).getChatById(chatId);
        Mockito.verify(usersToChatsRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void joinChatSuccess() {
        Mockito.when(userService.getUserById(Mockito.anyInt())).thenReturn(Optional.of(userEntity));
        Mockito.when(chatService.getChatById(Mockito.anyInt())).thenReturn(Optional.of(chatEntity));
        Mockito.when(usersToChatsRepository.findById(Mockito.any(UserToChatId.class))).thenReturn(Optional.empty());

        ResponseEntity<ResponseDTO> response = chatMembershipService.joinChat(userId, chatId);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());

        ArgumentCaptor<UserToChatEntity> userToChatCaptor = ArgumentCaptor.forClass(UserToChatEntity.class);
        Mockito.verify(userService).getUserById(userId);
        Mockito.verify(chatService).getChatById(chatId);
        Mockito.verify(usersToChatsRepository).save(userToChatCaptor.capture());

        Assertions.assertEquals(userId, userToChatCaptor.getValue().getId().getUserId());
        Assertions.assertEquals(chatId, userToChatCaptor.getValue().getId().getChatId());
        Assertions.assertEquals(userEntity, userToChatCaptor.getValue().getUserEntity());
        Assertions.assertEquals(chatEntity, userToChatCaptor.getValue().getChatEntity());
    }

    @Test
    public void leaveChatUserNotInChat() {
        Mockito.when(usersToChatsRepository.findById(Mockito.any(UserToChatId.class))).thenReturn(Optional.empty());

        ResponseEntity<ResponseDTO> response = chatMembershipService.leaveChat(userId, chatId);
        Assertions.assertFalse(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(ResponseDTO.ResponseType.USER_NOT_IN_CHAT, response.getBody().getResponseType());

        ArgumentCaptor<UserToChatId> idCaptor = ArgumentCaptor.forClass(UserToChatId.class);
        Mockito.verify(usersToChatsRepository).findById(idCaptor.capture());
        Mockito.verify(usersToChatsRepository, Mockito.never()).delete(Mockito.any());

        Assertions.assertEquals(userId, idCaptor.getValue().getUserId());
        Assertions.assertEquals(chatId, idCaptor.getValue().getChatId());
    }

    @Test
    public void leaveChatSuccess() {
        Mockito.when(usersToChatsRepository.findById(Mockito.any(UserToChatId.class)))
                .thenReturn(Optional.of(userToChatEntity));

        ResponseEntity<ResponseDTO> response = chatMembershipService.leaveChat(userId, chatId);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());

        ArgumentCaptor<UserToChatId> idCaptor = ArgumentCaptor.forClass(UserToChatId.class);
        ArgumentCaptor<UserToChatEntity> userToChatCaptor = ArgumentCaptor.forClass(UserToChatEntity.class);
        Mockito.verify(usersToChatsRepository).findById(idCaptor.capture());
        Mockito.verify(usersToChatsRepository).delete(userToChatCaptor.capture());

        Assertions.assertEquals(userId, idCaptor.getValue().getUserId());
        Assertions.assertEquals(chatId, idCaptor.getValue().getChatId());
        Assertions.assertEquals(userId, userToChatCaptor.getValue().getId().getUserId());
        Assertions.assertEquals(chatId, userToChatCaptor.getValue().getId().getChatId());
        Assertions.assertEquals(userEntity, userToChatCaptor.getValue().getUserEntity());
        Assertions.assertEquals(chatEntity, userToChatCaptor.getValue().getChatEntity());
    }
}
