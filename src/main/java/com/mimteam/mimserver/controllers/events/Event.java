package com.mimteam.mimserver.controllers.events;

import java.time.LocalDate;

public class Event {
    private LocalDate creationTime;

    public Event() {
        this.creationTime = LocalDate.now();
    }

    public LocalDate getCreationTime() {
        return creationTime;
    }
}
