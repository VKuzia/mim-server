package com.mimteam.mimserver.services;

import com.mimteam.mimserver.model.MessageDTO;
import com.mimteam.mimserver.model.entities.chat.ChatMessageEntity;
import com.mimteam.mimserver.model.messages.TextMessage;
import com.mimteam.mimserver.model.responses.ResponseDTO;
import com.mimteam.mimserver.repositories.ChatMessagesRepository;
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

@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceTest {

    @Mock
    private ChatMessagesRepository chatMessagesRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    public static final int chatId = 1;
    public static final int userId = 1;
    public static final String content = "Message Text";

    private TextMessage textMessage;

    @BeforeEach
    public void init() {
        MessageDTO testMessageDto = new MessageDTO();
        testMessageDto.setChatId(chatId);
        testMessageDto.setUserId(userId);
        testMessageDto.setContent(content);
        textMessage = new TextMessage(testMessageDto);
    }

    @Test
    public void textMessageSavedSuccess() {
        ResponseEntity<ResponseDTO> response = chatMessageService.saveTextMessage(textMessage);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());

        ArgumentCaptor<ChatMessageEntity> messageCaptor = ArgumentCaptor.forClass(ChatMessageEntity.class);
        Mockito.verify(chatMessagesRepository).save(messageCaptor.capture());

        Assertions.assertEquals(chatId, messageCaptor.getValue().getChatId());
        Assertions.assertEquals(userId, messageCaptor.getValue().getSenderId());
        Assertions.assertEquals(content, messageCaptor.getValue().getContent());
    }
}
