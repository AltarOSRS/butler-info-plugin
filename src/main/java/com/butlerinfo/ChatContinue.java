package com.butlerinfo;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;
import java.util.regex.Pattern;
@Slf4j
public enum ChatContinue
{
    NOT_ENOUGH_IN_BANK(
            "^Master, I dearly wish that I could perform your instruction in full, but alas, I can only carry (\\d+) items.",
            (event, option) -> event.getPlugin().getServant().sendOnBankTrip());

    @Getter
    private final String text;
    private final BiConsumer<ChatContinueEvent, ChatContinue> action;

    ChatContinue(String text, BiConsumer<ChatContinueEvent, ChatContinue> action) {
        this.text = text;
        this.action = action;
    }

    public void executeAction(ChatContinueEvent event, ChatContinue option) {
        if (event.getPlugin().getServant() == null) {
            return;
        }
        action.accept(event, option);
    }

    public Pattern getTextPattern() {
        return Pattern.compile(text);
    }

    public static ChatContinue getByEvent(ChatContinueEvent event) {
        log.info("--- CHECKING ChatContinue MATCH ---");
        log.info("Trying to match text: '{}'", event.getText());
        for (ChatContinue option : ChatContinue.values()) {
            if(option.getTextPattern().matcher(event.getText()).find()) {
                log.info(">>> SUCCESS: Matched ChatContinue enum '{}'", option.name());
                return option;
            }
        }
        log.warn(">>> FAILED: No ChatContinue match found.");
        return null;
    }
}