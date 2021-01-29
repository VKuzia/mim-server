package com.mimteam.mimserver.services;

import com.mimteam.mimserver.TestingUtils;
import com.mimteam.mimserver.model.dto.MessageDTO;
import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import com.mimteam.mimserver.model.entities.chat.ChatMessageEntity;
import com.mimteam.mimserver.model.messages.TextMessage;
import com.mimteam.mimserver.model.responses.ResponseBuilder;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.repositories.ChatMessagesRepository;
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
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceTest {

    @Mock
    private ChatMessagesRepository chatMessagesRepository;
    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private static final int CHAT_ID = 1;
    private static final int USER_ID = 1;
    private static final String CONTENT = "Message Text";

    private static final String CONTENT_1 = "Text message1";
    private static final String CONTENT_2 = "Text message2";

    private ChatMessageEntity chatMessageEntity1;
    private ChatMessageEntity chatMessageEntity2;
    private ChatEntity chatEntity;

    private ResponseEntity<ResponseDTO> successResponseEntity;
    private ResponseEntity<ResponseDTO> errorResponseEntity;

    private TextMessage textMessage;

    @BeforeEach
    public void init() {
        successResponseEntity = ResponseEntity.ok().build();
        errorResponseEntity = ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        MessageDTO testMessageDto = new MessageDTO();
        testMessageDto.setChatId(CHAT_ID);
        testMessageDto.setUserId(USER_ID);
        testMessageDto.setContent(CONTENT);

        textMessage = new TextMessage(testMessageDto);

        chatMessageEntity1 = new ChatMessageEntity();
        chatMessageEntity1.setChatId(CHAT_ID);
        chatMessageEntity1.setContent(CONTENT_1);

        chatMessageEntity2 = new ChatMessageEntity();
        chatMessageEntity2.setChatId(CHAT_ID);
        chatMessageEntity2.setContent(CONTENT_2);

        chatEntity = new ChatEntity();
        chatEntity.setChatId(CHAT_ID);
    }

    @Test
    public void textMessageSavedSuccess() {
        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(ResponseBuilder::buildSuccess).thenReturn(successResponseEntity);

            ResponseEntity<ResponseDTO> response = chatMessageService.saveTextMessage(textMessage);
            Assertions.assertEquals(successResponseEntity, response);
        }

        ArgumentCaptor<ChatMessageEntity> messageCaptor = ArgumentCaptor.forClass(ChatMessageEntity.class);
        Mockito.verify(chatMessagesRepository).save(messageCaptor.capture());

        Assertions.assertEquals(CHAT_ID, messageCaptor.getValue().getChatId());
        Assertions.assertEquals(USER_ID, messageCaptor.getValue().getSenderId());
        Assertions.assertEquals(CONTENT, messageCaptor.getValue().getContent());
    }

    @Test
    public void getMessagesChatNotExists() {
        Mockito.when(chatService.getChatById(Mockito.anyInt())).thenReturn(Optional.empty());

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            responseBuilder.when(() -> ResponseBuilder.buildError(ResponseDTO.ResponseType.CHAT_NOT_EXISTS))
                    .thenReturn(errorResponseEntity);

            ResponseEntity<ResponseDTO> response = chatMessageService.getMessageList(CHAT_ID);
            Assertions.assertEquals(errorResponseEntity, response);
        }

        Mockito.verify(chatService).getChatById(Mockito.anyInt());
    }

    @Test
    public void getMessagesEmpty() {
        Mockito.when(chatService.getChatById(Mockito.anyInt())).thenReturn(Optional.of(chatEntity));
        Mockito.when(chatMessagesRepository.findByChatId(Mockito.anyInt())).thenReturn(new ArrayList<>());

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            ResponseBuilder mockResponseBuilder = TestingUtils.createMockSuccessResponseBuilder(new ArrayList<>());
            responseBuilder.when(ResponseBuilder::builder).thenReturn(mockResponseBuilder);

            ResponseEntity<ResponseDTO> response = chatMessageService.getMessageList(CHAT_ID);
            Assertions.assertEquals(successResponseEntity, response);
        }

        Mockito.verify(chatMessagesRepository).findByChatId(Mockito.anyInt());
    }

    @Test
    public void getMessagesNotEmpty() {
        List<ChatMessageEntity> chatMessageList =
                new ArrayList<>(Arrays.asList(chatMessageEntity1, chatMessageEntity2));

        Mockito.when(chatService.getChatById(Mockito.anyInt())).thenReturn(Optional.of(chatEntity));
        Mockito.when(chatMessagesRepository.findByChatId(Mockito.anyInt())).thenReturn(chatMessageList);

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            ResponseBuilder mockResponseBuilder = TestingUtils.createMockSuccessResponseBuilder();
            responseBuilder.when(ResponseBuilder::builder).thenReturn(mockResponseBuilder);

            ResponseEntity<ResponseDTO> response = chatMessageService.getMessageList(CHAT_ID);
            Assertions.assertEquals(successResponseEntity, response);
        }

        Mockito.verify(chatMessagesRepository).findByChatId(Mockito.anyInt());
    }
}
