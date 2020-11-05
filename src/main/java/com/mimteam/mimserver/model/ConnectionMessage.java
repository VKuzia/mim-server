package com.mimteam.mimserver.model;

public class ConnectionMessage {
    private Integer userId;
    private ConnectionType messageType;

    enum ConnectionType {
        CONNECT, DISCONNECT
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public ConnectionType getMessageType() {
        return messageType;
    }

    public void setMessageType(ConnectionType messageType) {
        this.messageType = messageType;
    }
}
