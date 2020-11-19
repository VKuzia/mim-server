package com.mimteam.mimserver.model.messages;

import com.mimteam.mimserver.model.MessageDTO;
import com.mimteam.mimserver.model.Transferable;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class TextMessage implements Transferable {
    private Integer userId;
    private Integer chatId;
    private String content;
    private LocalDateTime dateTime;

    public TextMessage(MessageDTO messageDto) {
        this.fromDataTransferObject(messageDto);
    }

    @Override
    public MessageDTO toDataTransferObject() {
        MessageDTO dto = new MessageDTO();
        dto.setMessageType(MessageDTO.MessageType.TEXT_MESSAGE);
        dto.setUserId(userId);
        dto.setChatId(chatId);
        dto.setContent(content);
        dto.setDateTime(dateTime);
        return dto;
    }

    @Override
    public void fromDataTransferObject(@NotNull MessageDTO dto) {
        this.userId = dto.getUserId();
        this.chatId = dto.getChatId();
        this.content = dto.getContent();
        this.dateTime = dto.getDateTime();
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
