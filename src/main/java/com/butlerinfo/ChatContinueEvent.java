package com.butlerinfo;

import lombok.Getter;

import javax.inject.Inject;
public class ChatContinueEvent
{
    @Getter
    private final ButlerInfoPlugin plugin;
    @Getter
    private final String text;

    @Inject
    public ChatContinueEvent(ButlerInfoPlugin plugin, String text) {
        this.plugin = plugin;
        this.text = text;
    }
}