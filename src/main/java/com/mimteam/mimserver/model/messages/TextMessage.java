package com.mimteam.mimserver.model.messages;

import com.mimteam.mimserver.model.MessageDTO;
import com.mimteam.mimserver.model.Transferable;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TextMessage implements Transferable {
    private Integer userId;
    private Integer chatId;
    private String content;
    private LocalDateTime time;

    @Override
    public MessageDTO toDataTransferObject() {
        MessageDTO dto = new MessageDTO();
        dto.setMessageType(MessageDTO.MessageType.TEXT_MESSAGE);
        dto.setUserId(userId);
        dto.setChatId(chatId);
        dto.setContent(content);
        dto.setTime(time);
        return dto;
    }

    @Override
    public void fromDataTransferObject(@NotNull MessageDTO dto) {
        this.userId = dto.getUserId();
        this.chatId = dto.getChatId();
        this.content = dto.getContent();
        this.time = dto.getTime();
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

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
