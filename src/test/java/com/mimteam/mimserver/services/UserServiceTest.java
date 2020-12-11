package com.mimteam.mimserver.services;

import com.mimteam.mimserver.model.entities.UserEntity;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatId;
import com.mimteam.mimserver.model.responses.ResponseBuilder;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.repositories.UsersRepository;
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
public class UserServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private UserService userService;

    private static final String name = "Name";
    private static final String login = "login";
    private static final String password = "password";
    private static final String incorrectPassword = "incorrect_password";
    private static final Integer userId = 1;

    private ResponseEntity<ResponseDTO> errorResponseEntity;
    private ResponseEntity<ResponseDTO> successResponseEntity;

    private final UserEntity userEntity = new UserEntity(name, login, password);

    @BeforeEach
    public void init() {
        errorResponseEntity = ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        successResponseEntity = ResponseEntity.ok().build();

        userEntity.setUserId(userId);
    }

    @Test
    public void signUpUserAlreadyExists() {
        Mockito.when(usersRepository.findByLogin(Mockito.anyString())).thenReturn(Optional.of(userEntity));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(() -> ResponseBuilder.buildError(ResponseDTO.ResponseType.USER_ALREADY_EXISTS))
                    .thenReturn(errorResponseEntity);

            ResponseEntity<ResponseDTO> response = userService.signUpUser(name, login, password);
            Assertions.assertEquals(errorResponseEntity, response);
        }

        Mockito.verify(usersRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void signUpUserSuccess() {
        Mockito.when(usersRepository.findByLogin(Mockito.anyString())).thenReturn(Optional.empty());

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(ResponseBuilder::buildSuccess).thenReturn(successResponseEntity);

            ResponseEntity<ResponseDTO> response = userService.signUpUser(name, login, password);
            Assertions.assertEquals(successResponseEntity, response);
        }

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

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(() -> ResponseBuilder.buildError(ResponseDTO.ResponseType.USER_NOT_EXISTS))
                    .thenReturn(errorResponseEntity);

            ResponseEntity<ResponseDTO> response = userService.loginUser(login, password);
            Assertions.assertEquals(errorResponseEntity, response);
        }

        Mockito.verify(usersRepository).findByLogin(login);
    }

    @Test
    public void loginUserIncorrectPassword() {
        Mockito.when(usersRepository.findByLogin(Mockito.any())).thenReturn(Optional.of(userEntity));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(() -> ResponseBuilder.buildError(ResponseDTO.ResponseType.INCORRECT_PASSWORD))
                    .thenReturn(errorResponseEntity);

            ResponseEntity<ResponseDTO> response = userService.loginUser(login, incorrectPassword);
            Assertions.assertEquals(errorResponseEntity, response);
        }

        Mockito.verify(usersRepository).findByLogin(login);
    }

    @Test
    public void loginUserSuccess() {
        Mockito.when(usersRepository.findByLogin(Mockito.any())).thenReturn(Optional.of(userEntity));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            ResponseBuilder mockResponseBuilder = createAnyStringSuccessResponseBuilder();
            responseBuilder.when(ResponseBuilder::builder).thenReturn(mockResponseBuilder);

            ResponseEntity<ResponseDTO> response = userService.loginUser(login, password);
            Assertions.assertEquals(successResponseEntity, response);
        }

        Mockito.verify(usersRepository).findByLogin(login);
    }

    @Test
    public void getChatIdListUserNotExists() {
        Mockito.when(usersRepository.findById(Mockito.anyInt())).thenReturn(Optional.empty());

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(() -> ResponseBuilder.buildError(ResponseDTO.ResponseType.USER_NOT_EXISTS))
                    .thenReturn(errorResponseEntity);

            ResponseEntity<ResponseDTO> response = userService.getChatIdList(userId);
            Assertions.assertEquals(errorResponseEntity, response);
        }

        Mockito.verify(usersRepository).findById(userId);
    }

    @Test
    public void getChatIdListEmpty() {
        UserEntity spyUserEntity = Mockito.spy(userEntity);
        Mockito.when(spyUserEntity.getChatList()).thenReturn(new HashSet<>());
        Mockito.when(usersRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(spyUserEntity));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            ResponseBuilder mockResponseBuilder = createObjectSuccessResponseBuilder(new ArrayList<>());
            responseBuilder.when(ResponseBuilder::builder).thenReturn(mockResponseBuilder);

            ResponseEntity<ResponseDTO> response = userService.getChatIdList(userId);
            Assertions.assertEquals(successResponseEntity, response);
        }

        Mockito.verify(usersRepository).findById(userId);
    }

    @Test
    public void getChatIdListNonEmpty() {
        ArrayList<Integer> expectedChatIdList = new ArrayList<>(Arrays.asList(4, 1));
        expectedChatIdList.sort(Integer::compareTo);
        Set<UserToChatEntity> userToChatIds = getChatsForUser(expectedChatIdList);

        UserEntity spyUserEntity = Mockito.spy(userEntity);
        Mockito.when(spyUserEntity.getChatList()).thenReturn(userToChatIds);
        Mockito.when(usersRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(spyUserEntity));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            ResponseBuilder mockResponseBuilder = createObjectSuccessResponseBuilder(expectedChatIdList);
            responseBuilder.when(ResponseBuilder::builder).thenReturn(mockResponseBuilder);

            ResponseEntity<ResponseDTO> response = userService.getChatIdList(userId);
            Assertions.assertEquals(successResponseEntity, response);
        }

        Mockito.verify(usersRepository).findById(userId);
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

    private ResponseBuilder createEmptySuccessResponseBuilder() {
        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        Mockito.when(responseBuilder.responseType(ResponseDTO.ResponseType.OK)).thenReturn(responseBuilder);
        Mockito.when(responseBuilder.build()).thenReturn(successResponseEntity);
        return responseBuilder;
    }

    private ResponseBuilder createObjectSuccessResponseBuilder(Object body) {
        ResponseBuilder responseBuilder = createEmptySuccessResponseBuilder();
        Mockito.when(responseBuilder.body(body)).thenReturn(responseBuilder);
        return responseBuilder;
    }

    private ResponseBuilder createAnyStringSuccessResponseBuilder() {
        ResponseBuilder responseBuilder = createEmptySuccessResponseBuilder();
        Mockito.when(responseBuilder.stringBody(Mockito.anyString())).thenReturn(responseBuilder);
        return responseBuilder;
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
}
