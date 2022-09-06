package com.butlerinfo;

import lombok.Getter;

import javax.inject.Inject;

public class ChatOptionEvent
{
    @Getter
    private final ButlerInfoPlugin plugin;

    @Getter
    private final String optionPrompt;

    @Getter
    private final String text;

    @Getter
    private final int optionOrder;

    @Inject
    public ChatOptionEvent(ButlerInfoPlugin plugin, String optionPrompt, String text, int optionOrder) {
        this.plugin = plugin;
        this.optionPrompt = optionPrompt;
        this.text = text;
        this.optionOrder = optionOrder;
    }
}
