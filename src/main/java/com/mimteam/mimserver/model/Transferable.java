package com.mimteam.mimserver.model;

import org.jetbrains.annotations.NotNull;

public interface Transferable {
    MessageDTO toDataTransferObject();
    void fromDataTransferObject(@NotNull MessageDTO dto);
}
