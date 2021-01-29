package com.mimteam.mimserver.services;

import com.mimteam.mimserver.TestingUtils;
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

    private static final String NAME = "Name";
    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";
    private static final String INCORRECT_PASSWORD = "incorrect_password";
    private static final Integer USER_ID = 1;

    private ResponseEntity<ResponseDTO> errorResponseEntity;
    private ResponseEntity<ResponseDTO> successResponseEntity;

    private static final UserEntity USER_ENTITY = new UserEntity(NAME, LOGIN, PASSWORD);

    @BeforeEach
    public void init() {
        errorResponseEntity = ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        successResponseEntity = ResponseEntity.ok().build();

        USER_ENTITY.setUserId(USER_ID);
    }

    @Test
    public void signUpUserAlreadyExists() {
        Mockito.when(usersRepository.findByLogin(Mockito.anyString())).thenReturn(Optional.of(USER_ENTITY));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(() -> ResponseBuilder.buildError(ResponseDTO.ResponseType.USER_ALREADY_EXISTS))
                    .thenReturn(errorResponseEntity);

            ResponseEntity<ResponseDTO> response = userService.signUpUser(NAME, LOGIN, PASSWORD);
            Assertions.assertEquals(errorResponseEntity, response);
        }

        Mockito.verify(usersRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void signUpUserSuccess() {
        Mockito.when(usersRepository.findByLogin(Mockito.anyString())).thenReturn(Optional.empty());

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(ResponseBuilder::buildSuccess).thenReturn(successResponseEntity);

            ResponseEntity<ResponseDTO> response = userService.signUpUser(NAME, LOGIN, PASSWORD);
            Assertions.assertEquals(successResponseEntity, response);
        }

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        Mockito.verify(usersRepository).findByLogin(LOGIN);
        Mockito.verify(usersRepository).save(userCaptor.capture());

        Assertions.assertEquals(NAME, userCaptor.getValue().getName());
        Assertions.assertEquals(LOGIN, userCaptor.getValue().getLogin());
        Assertions.assertEquals(PASSWORD, userCaptor.getValue().getPassword());
        Assertions.assertNull(USER_ENTITY.getChatList());
    }

    @Test
    public void loginUserNotExists() {
        Mockito.when(usersRepository.findByLogin(Mockito.any())).thenReturn(Optional.empty());

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(() -> ResponseBuilder.buildError(ResponseDTO.ResponseType.USER_NOT_EXISTS))
                    .thenReturn(errorResponseEntity);

            ResponseEntity<ResponseDTO> response = userService.loginUser(LOGIN, PASSWORD);
            Assertions.assertEquals(errorResponseEntity, response);
        }

        Mockito.verify(usersRepository).findByLogin(LOGIN);
    }

    @Test
    public void loginUserIncorrectPassword() {
        Mockito.when(usersRepository.findByLogin(Mockito.any())).thenReturn(Optional.of(USER_ENTITY));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(() -> ResponseBuilder.buildError(ResponseDTO.ResponseType.INCORRECT_PASSWORD))
                    .thenReturn(errorResponseEntity);

            ResponseEntity<ResponseDTO> response = userService.loginUser(LOGIN, INCORRECT_PASSWORD);
            Assertions.assertEquals(errorResponseEntity, response);
        }

        Mockito.verify(usersRepository).findByLogin(LOGIN);
    }

    @Test
    public void loginUserSuccess() {
        Mockito.when(usersRepository.findByLogin(Mockito.any())).thenReturn(Optional.of(USER_ENTITY));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            ResponseBuilder mockResponseBuilder = TestingUtils.createAnyStringSuccessResponseBuilder();
            responseBuilder.when(ResponseBuilder::builder).thenReturn(mockResponseBuilder);

            ResponseEntity<ResponseDTO> response = userService.loginUser(LOGIN, PASSWORD);
            Assertions.assertEquals(successResponseEntity, response);
        }

        Mockito.verify(usersRepository).findByLogin(LOGIN);
    }

    @Test
    public void getChatIdListUserNotExists() {
        Mockito.when(usersRepository.findById(Mockito.anyInt())).thenReturn(Optional.empty());

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(() -> ResponseBuilder.buildError(ResponseDTO.ResponseType.USER_NOT_EXISTS))
                    .thenReturn(errorResponseEntity);

            ResponseEntity<ResponseDTO> response = userService.getChatList(USER_ID);
            Assertions.assertEquals(errorResponseEntity, response);
        }

        Mockito.verify(usersRepository).findById(USER_ID);
    }

    @Test
    public void getChatIdListEmpty() {
        UserEntity spyUserEntity = Mockito.spy(USER_ENTITY);
        Mockito.when(spyUserEntity.getChatList()).thenReturn(new HashSet<>());
        Mockito.when(usersRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(spyUserEntity));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            ResponseBuilder mockResponseBuilder = TestingUtils.createMockSuccessResponseBuilder(new ArrayList<>());
            responseBuilder.when(ResponseBuilder::builder).thenReturn(mockResponseBuilder);

            ResponseEntity<ResponseDTO> response = userService.getChatList(USER_ID);
            Assertions.assertEquals(successResponseEntity, response);
        }

        Mockito.verify(usersRepository).findById(USER_ID);
    }

    @Test
    public void getChatIdListNonEmpty() {
        ArrayList<Integer> expectedChatIdList = new ArrayList<>(Arrays.asList(4, 1));
        expectedChatIdList.sort(Integer::compareTo);
        Set<UserToChatEntity> userToChatIds = getChatsForUser(expectedChatIdList);

        UserEntity spyUserEntity = Mockito.spy(USER_ENTITY);
        Mockito.when(spyUserEntity.getChatList()).thenReturn(userToChatIds);
        Mockito.when(usersRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(spyUserEntity));

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            ResponseBuilder mockResponseBuilder = TestingUtils.createMockSuccessResponseBuilder();
            responseBuilder.when(ResponseBuilder::builder).thenReturn(mockResponseBuilder);

            ResponseEntity<ResponseDTO> response = userService.getChatList(USER_ID);
            Assertions.assertEquals(successResponseEntity, response);
        }

        Mockito.verify(usersRepository).findById(USER_ID);
    }

    @Test
    public void getUserByIdNotExists() {
        Mockito.when(usersRepository.findById(Mockito.anyInt())).thenReturn(Optional.empty());

        Optional<UserEntity> user = userService.getUserById(USER_ID);
        Assertions.assertTrue(user.isEmpty());

        Mockito.verify(usersRepository).findById(USER_ID);
    }

    @Test
    public void getUserByIdSuccess() {
        Mockito.when(usersRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(USER_ENTITY));

        Optional<UserEntity> user = userService.getUserById(USER_ID);
        Assertions.assertTrue(user.isPresent());
        Assertions.assertEquals(USER_ENTITY, user.get());

        Mockito.verify(usersRepository).findById(USER_ID);
    }

    private Set<UserToChatEntity> getChatsForUser(ArrayList<Integer> chatIdList) {
        HashSet<UserToChatEntity> chats = new HashSet<>();
        for (Integer chatId : chatIdList) {
            ChatEntity chatEntity = createChatWithId(chatId);

            UserToChatEntity userToChatEntity = new UserToChatEntity(new UserToChatId(USER_ID, chatId));
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
