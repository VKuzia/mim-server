package com.mimteam.mimserver.repositories;

import com.mimteam.mimserver.model.entities.chat.ChatEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ChatsRepository extends CrudRepository<ChatEntity, Integer> {
    Optional<ChatEntity> findByInvitationKey(String invitationKey);
}
