package com.mimteam.mimserver.model.entities.chat;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class UserToChatId implements Serializable {
    private Integer userId;
    private Integer chatId;

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
}
