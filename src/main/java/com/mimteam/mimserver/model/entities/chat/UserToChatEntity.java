package com.mimteam.mimserver.model.entities.chat;

import com.mimteam.mimserver.model.entities.UserEntity;

import javax.persistence.*;

@Entity
@Table(schema = "mim", name = "user_to_chat")
public class UserToChatEntity {

    @EmbeddedId
    private UserToChatId id;

    private boolean isAdmin;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    UserEntity userEntity;

    @ManyToOne
    @MapsId("chatId")
    @JoinColumn(name = "chat_id")
    ChatEntity chatEntity;

    public UserToChatEntity() {}

    public UserToChatEntity(UserToChatId id) {
        this.id = id;
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

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    public ChatEntity getChatEntity() {
        return chatEntity;
    }

    public void setChatEntity(ChatEntity chatEntity) {
        this.chatEntity = chatEntity;
    }
}
