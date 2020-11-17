package com.mimteam.mimserver.repositories;

import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;
import org.springframework.data.repository.CrudRepository;

public interface UsersToChatsRepository extends CrudRepository<UserToChatEntity, Integer> {
}
