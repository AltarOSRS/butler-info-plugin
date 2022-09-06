package com.butlerinfo;

import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ChatContinue
{
    NOT_ENOUGH_IN_BANK(
        "^Master, I dearly wish that I could perform your instruction in full, but alas, I can only carry (\\d+) items.",
        (event, option) -> {
            if (event.getPlugin().getServant() == null) {
                return;
            }
            event.getPlugin().getServant().sendOnBankRun();
        }),

    SEND_ITEMS_BACK_CONFIRMATION(
    "Very well, Master.",
        (event, option) -> {
            if (event.getPlugin().getServant() == null) {
                return;
            }
            if(event.getPlugin().isSendingItemsBack()) {
                event.getPlugin().setSendingItemsBack(false);
                event.getPlugin().getServant().setItemAmount(0);
                event.getPlugin().startBankRunTimer();
            }
        }
    );

    @Getter
    private final String text;

    @Getter
    private final BiConsumer<ChatContinueEvent, ChatContinue> action;

    ChatContinue(String text, BiConsumer<ChatContinueEvent, ChatContinue> action) {
        this.text = text;
        this.action = action;
    }
    public Pattern getTextPattern() {
        return Pattern.compile(text);
    }

    public static ChatContinue getByEvent(ChatContinueEvent event) {
        for (ChatContinue option : ChatContinue.values()) {
            if(option.getTextPattern().matcher(event.getText()).find()) {
                return option;
            }
        }
        return null;
    }
}
