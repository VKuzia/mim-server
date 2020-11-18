package com.mimteam.mimserver.handlers;

import com.google.common.eventbus.Subscribe;
import com.mimteam.mimserver.events.ChatMembershipEvent;
import com.mimteam.mimserver.events.SendTextMessageEvent;
import com.mimteam.mimserver.model.entities.chat.UserToChatEntity;
import com.mimteam.mimserver.model.entities.chat.UserToChatId;
import com.mimteam.mimserver.repositories.ChatMessagesRepository;
import com.mimteam.mimserver.repositories.ChatsRepository;
import com.mimteam.mimserver.repositories.UsersToChatsRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.mimteam.mimserver.model.entities.chat.ChatMessageEntity;

@Component
public class DatabaseEntityUpdater {
    private final ChatsRepository chatsRepository;
    private final ChatMessagesRepository chatMessagesRepository;
    private final UsersToChatsRepository usersToChatsRepository;

    @Autowired
    public DatabaseEntityUpdater(ChatsRepository chatsRepository,
                                 ChatMessagesRepository chatMessagesRepository,
                                 UsersToChatsRepository usersToChatsRepository) {
        this.chatsRepository = chatsRepository;
        this.chatMessagesRepository = chatMessagesRepository;
        this.usersToChatsRepository = usersToChatsRepository;
    }

    @Subscribe
    public void handleChatMembershipEvent(@NotNull ChatMembershipEvent event) {
        UserToChatId userToChatId = new UserToChatId(event.getUserId(), event.getChatId());
        UserToChatEntity entity = new UserToChatEntity(userToChatId);

        switch (event.getChatMembershipMessageType()) {
            case JOIN:
                usersToChatsRepository.save(entity);
                break;
            case LEAVE:
                usersToChatsRepository.delete(entity);
                break;
        }
    }

    @Subscribe
    public void saveChatMessage(@NotNull SendTextMessageEvent event) {
        ChatMessageEntity entity = new ChatMessageEntity(event);
        chatMessagesRepository.save(entity);
    }
}
