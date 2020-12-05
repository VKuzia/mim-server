package com.mimteam.mimserver.services;

import com.mimteam.mimserver.model.MessageDTO;
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
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceTest {

    @Mock
    private ChatMessagesRepository chatMessagesRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private static final int chatId = 1;
    private static final int userId = 1;
    private static final String content = "Message Text";
    private static final String chatMessage1 = "Message text 1";
    private static final String chatMessage2 = "Message text 2";

    private ResponseEntity<ResponseDTO> successResponseEntity;
    private TextMessage textMessage;

    @BeforeEach
    public void init() {
        successResponseEntity = ResponseEntity.ok().build();

        MessageDTO testMessageDto = new MessageDTO();
        testMessageDto.setChatId(chatId);
        testMessageDto.setUserId(userId);
        testMessageDto.setContent(content);
        textMessage = new TextMessage(testMessageDto);
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

        Assertions.assertEquals(chatId, messageCaptor.getValue().getChatId());
        Assertions.assertEquals(userId, messageCaptor.getValue().getSenderId());
        Assertions.assertEquals(content, messageCaptor.getValue().getContent());
    }

    @Test
    public void getMessagesEmpty() {
        Mockito.when(chatMessagesRepository.findByChatId(Mockito.anyInt())).thenReturn(new ArrayList<>());

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            ResponseBuilder mockResponseBuilder = createMockSuccessResponseBuilder(new ArrayList<>());
            responseBuilder.when(ResponseBuilder::builder).thenReturn(mockResponseBuilder);

            ResponseEntity<ResponseDTO> response = chatMessageService.getMessageList(chatId);
            Assertions.assertEquals(successResponseEntity, response);
        }

        Mockito.verify(chatMessagesRepository).findByChatId(Mockito.anyInt());
    }

    @Test
    public void getMessagesNotEmpty() {
        ArrayList<String> expectedMessageList = new ArrayList<>(Arrays.asList(chatMessage1, chatMessage2));
        List<ChatMessageEntity> spyChatMessageEntities = createSpyChatMessageEntitiesList();
        Mockito.when(chatMessagesRepository.findByChatId(Mockito.anyInt())).thenReturn(spyChatMessageEntities);

        try (MockedStatic<ResponseBuilder> responseBuilder = Mockito.mockStatic(ResponseBuilder.class)) {
            ResponseBuilder mockResponseBuilder = createMockSuccessResponseBuilder(expectedMessageList);
            responseBuilder.when(ResponseBuilder::builder).thenReturn(mockResponseBuilder);

            ResponseEntity<ResponseDTO> response = chatMessageService.getMessageList(chatId);
            Assertions.assertEquals(successResponseEntity, response);
        }

        Mockito.verify(chatMessagesRepository).findByChatId(Mockito.anyInt());
    }

    private ResponseBuilder createMockSuccessResponseBuilder(Object body) {
        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        Mockito.when(responseBuilder.responseType(ResponseDTO.ResponseType.OK)).thenReturn(responseBuilder);
        Mockito.when(responseBuilder.body(body)).thenReturn(responseBuilder);
        Mockito.when(responseBuilder.build()).thenReturn(successResponseEntity);
        return responseBuilder;
    }

    private List<ChatMessageEntity> createSpyChatMessageEntitiesList() {
        ChatMessageEntity spyChatMessageEntity1 = Mockito.spy(ChatMessageEntity.class);
        spyChatMessageEntity1.setChatId(chatId);
        spyChatMessageEntity1.setContent(chatMessage1);

        ChatMessageEntity spyChatMessageEntity2 = Mockito.spy(ChatMessageEntity.class);
        spyChatMessageEntity2.setChatId(chatId);
        spyChatMessageEntity2.setContent(chatMessage2);

        return new ArrayList<>(Arrays.asList(spyChatMessageEntity1, spyChatMessageEntity2));
    }
}
