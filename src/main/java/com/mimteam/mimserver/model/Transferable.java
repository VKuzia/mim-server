package com.mimteam.mimserver.model;

import org.jetbrains.annotations.NotNull;

public interface Transferable {
    public MessageDTO toDataTransferObject();
    public void fromDataTransferObject(@NotNull MessageDTO dto);
}
