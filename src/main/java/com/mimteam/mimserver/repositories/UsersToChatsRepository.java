package com.mimteam.mimserver.repositories;

import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatId;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UsersToChatsRepository extends CrudRepository<UserToChatEntity, Integer> {
    Optional<UserToChatEntity> findById(UserToChatId id);
}
