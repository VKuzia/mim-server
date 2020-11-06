package com.mimteam.mimserver.model;

public class ConnectionMessage {
    private Integer userId;
    private ConnectionMessageType messageType;

    enum ConnectionMessageType {
        CONNECT, DISCONNECT
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public ConnectionMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(ConnectionMessageType messageType) {
        this.messageType = messageType;
    }
}
