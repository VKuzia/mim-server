package com.mimteam.mimserver.model;

import com.mimteam.mimserver.model.dto.MessageDTO;
import org.jetbrains.annotations.NotNull;

public interface Transferable {
    MessageDTO toDataTransferObject();
    void fromDataTransferObject(@NotNull MessageDTO dto);
}
