package com.mimteam.mimserver.model.entities.chat;

import com.mimteam.mimserver.events.ChatMembershipEvent;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(schema = "mim", name = "user_to_chat")
public class UserToChatEntity {

    @EmbeddedId
    private UserToChatId id;

    private boolean isAdmin;

    public UserToChatEntity() {}

    public UserToChatEntity(ChatMembershipEvent event) {
        UserToChatId userToChatId = new UserToChatId();
        userToChatId.setChatId(event.getChatId());
        userToChatId.setUserId(event.getUserId());
        this.id = userToChatId;
        this.isAdmin = false;
    }

    public UserToChatId getId() {
        return id;
    }

    public void setId(UserToChatId id) {
        this.id = id;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
