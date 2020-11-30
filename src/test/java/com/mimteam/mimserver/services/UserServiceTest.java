package com.mimteam.mimserver.services;

import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatId;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.repositories.UsersRepository;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private UserService userService;

    public static final String name = "Name";
    public static final String login = "login";
    public static final String password = "password";
    public static final String incorrectPassword = "incorrect_password";
    public static final Integer userId = 1;

    private final UserEntity userEntity = new UserEntity(name, login, password);

    @BeforeEach
    public void init() {
        userEntity.setUserId(userId);
    }

    @Test
    public void signUpUserAlreadyExists() {
        Mockito.when(usersRepository.findByLogin(Mockito.anyString())).thenReturn(Optional.of(userEntity));

        ResponseEntity<ResponseDTO> response = userService.signUpUser(name, login, password);
        Assertions.assertFalse(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(ResponseDTO.ResponseType.USER_ALREADY_EXISTS, response.getBody().getResponseType());

        Mockito.verify(usersRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void signUpUserSuccess() {
        Mockito.when(usersRepository.findByLogin(Mockito.anyString())).thenReturn(Optional.empty());

        ResponseEntity<ResponseDTO> response = userService.signUpUser(name, login, password);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        Mockito.verify(usersRepository).findByLogin(login);
        Mockito.verify(usersRepository).save(userCaptor.capture());

        Assertions.assertEquals(name, userCaptor.getValue().getName());
        Assertions.assertEquals(login, userCaptor.getValue().getLogin());
        Assertions.assertEquals(password, userCaptor.getValue().getPassword());
        Assertions.assertNull(userEntity.getChatList());
    }

    @Test
    public void loginUserNotExists() {
        Mockito.when(usersRepository.findByLogin(Mockito.any())).thenReturn(Optional.empty());

        ResponseEntity<ResponseDTO> response = userService.loginUser(login, password);
        Assertions.assertFalse(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(ResponseDTO.ResponseType.USER_NOT_EXISTS, response.getBody().getResponseType());

        Mockito.verify(usersRepository).findByLogin(login);
    }

    @Test
    public void loginUserIncorrectPassword() {
        Mockito.when(usersRepository.findByLogin(Mockito.any())).thenReturn(Optional.of(userEntity));

        ResponseEntity<ResponseDTO> response = userService.loginUser(login, incorrectPassword);
        Assertions.assertFalse(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(ResponseDTO.ResponseType.INCORRECT_PASSWORD, response.getBody().getResponseType());

        Mockito.verify(usersRepository).findByLogin(login);
    }

    @Test
    public void loginUserSuccess() {
        Mockito.when(usersRepository.findByLogin(Mockito.any())).thenReturn(Optional.of(userEntity));

        ResponseEntity<ResponseDTO> response = userService.loginUser(login, password);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(userId.toString(), response.getBody().getResponseMessage());

        Mockito.verify(usersRepository).findByLogin(login);
    }

    @Test
    public void getChatIdListUserNotExists() {
        Mockito.when(usersRepository.findById(Mockito.anyInt())).thenReturn(Optional.empty());

        ResponseEntity<ResponseDTO> response = userService.getChatIdList(userId);
        Assertions.assertFalse(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(ResponseDTO.ResponseType.USER_NOT_EXISTS, response.getBody().getResponseType());

        Mockito.verify(usersRepository).findById(userId);
    }

    @Test
    public void getChatIdListEmpty() {
        UserEntity spyUserEntity = Mockito.spy(userEntity);
        Mockito.when(spyUserEntity.getChatList()).thenReturn(new HashSet<>());
        Mockito.when(usersRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(spyUserEntity));

        ResponseEntity<ResponseDTO> response = userService.getChatIdList(userId);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(new ArrayList<>().toString(), response.getBody().getResponseMessage());
    }

    @Test
    public void getChatIdListNonEmpty() {
        ArrayList<Integer> expectedChatIdList = new ArrayList<>();
        expectedChatIdList.add(4);
        expectedChatIdList.add(1);

        Set<UserToChatEntity> userToChatIds = getChatsForUser(expectedChatIdList);

        UserEntity spyUserEntity = Mockito.spy(userEntity);
        Mockito.when(spyUserEntity.getChatList()).thenReturn(userToChatIds);
        Mockito.when(usersRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(spyUserEntity));

        ResponseEntity<ResponseDTO> response = userService.getChatIdList(userId);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());

        List<Integer> actualChatIdList = parseChatIdList(response.getBody().getResponseMessage());
        expectedChatIdList.sort(Integer::compareTo);
        actualChatIdList.sort(Integer::compareTo);
        Assertions.assertEquals(expectedChatIdList, actualChatIdList);
    }

    private List<Integer> parseChatIdList(String list) {
        String[] ids = list.substring(1, list.length() - 1).split(",");
        return Arrays.stream(ids).map(Integer::valueOf).sorted().collect(Collectors.toList());
    }

    private Set<UserToChatEntity> getChatsForUser(ArrayList<Integer> chatIdList) {
        HashSet<UserToChatEntity> chats = new HashSet<>();
        for (Integer chatId : chatIdList) {
            ChatEntity chatEntity = createChatWithId(chatId);

            UserToChatEntity userToChatEntity = new UserToChatEntity(new UserToChatId(userId, chatId));
            userToChatEntity.setChatEntity(chatEntity);
            chats.add(userToChatEntity);
        }
        return chats;
    }

    private ChatEntity createChatWithId(Integer chatId) {
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setChatId(chatId);
        return chatEntity;
    }

    @Test
    public void getUserByIdNotExists() {
        Mockito.when(usersRepository.findById(Mockito.anyInt())).thenReturn(Optional.empty());

        Optional<UserEntity> user = userService.getUserById(userId);
        Assertions.assertTrue(user.isEmpty());

        Mockito.verify(usersRepository).findById(userId);
    }

    @Test
    public void getUserByIdSuccess() {
        Mockito.when(usersRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(userEntity));

        Optional<UserEntity> user = userService.getUserById(userId);
        Assertions.assertTrue(user.isPresent());
        Assertions.assertEquals(userEntity, user.get());

        Mockito.verify(usersRepository).findById(userId);
    }
}