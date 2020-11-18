package com.mimteam.mimserver.repositories;

import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import org.springframework.data.repository.CrudRepository;

public interface ChatsRepository extends CrudRepository<ChatEntity, Integer> {
}
