package com.butlerinfo;

import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ChatOption
{
    TAKE_ITEMS_BACK(
        "Select an Option",
        "^Take (.+) back to the bank",
        1,
        (event, option) -> event.getPlugin().setSendingItemsBack(true)),

    PAY_SERVANT(
        "Select an Option",
        "^Okay, here's (?<quantity>.+) coins.",
        1,
        (event, option) -> {
            if (event.getPlugin().getServant() == null) {
                return;
            }
            event.getPlugin().getServant().setPaymentAmount(option.getQuantityReferenced(event.getText()));
            event.getPlugin().getServant().setTripsUntilPayment(Servant.TRIPS_PER_PAYMENT);
        }),

    REPEAT_TASK(
        "Repeat last task?",
        "^Fetch from bank: (?<quantity>\\d+) x (?<item>.+)",
        1,
        (event, option) -> {
            if (event.getPlugin().getServant() == null) {
                return;
            }
            String item = option.getItemReferenced(event.getText());
            event.getPlugin().getServant().sendOnBankRun(item);
        }),

    SEND_SERVANT_FOR_ITEM(
        "Select an Option",
        "^(?<item>.+planks|Soft clay|Limestone brick|Steel bar|Cloth|Gold leaf|Marble block|Magic housing stone)",
        Constants.NO_SPECIFIC_ORDER,
        (event, option) -> {
            if (event.getPlugin().getServant() == null) {
                return;
            }
            event.getPlugin().getDialogManager().setEnteringAmount(true);
            String item = option.getItemReferenced(event.getText());
            event.getPlugin().getServant().setItem(item);
        });

    private static class Constants
    {
        public static int NO_SPECIFIC_ORDER = -1;
    }

    @Getter
    private final String optionPrompt;

    @Getter
    private final String text;

    @Getter
    private final int optionOrder;

    @Getter
    private final BiConsumer<ChatOptionEvent, ChatOption> action;

    ChatOption(String optionPrompt, String text, int optionOrder, BiConsumer<ChatOptionEvent, ChatOption> action) {
        this.optionPrompt = optionPrompt;
        this.text = text;
        this.optionOrder = optionOrder;
        this.action = action;
    }

    public Pattern getOptionPromptPattern() {
        return Pattern.compile(optionPrompt);
    }

    public Pattern getTextPattern() {
        return Pattern.compile(text);
    }

    public String getQuantityReferenced(String eventOptionText) {
        Matcher matcher = getTextPattern().matcher(eventOptionText);
        matcher.find();
        return matcher.group("quantity");
    }

    public String getItemReferenced(String eventOptionText) {
        Matcher matcher = getTextPattern().matcher(eventOptionText);
        matcher.find();
        return matcher.group("item");
    }

    public static ChatOption getByEvent(ChatOptionEvent event) {
        for (ChatOption option : ChatOption.values()) {
            if(option.getOptionPromptPattern().matcher(event.getOptionPrompt()).find()
                    && option.getTextPattern().matcher(event.getText()).find()
                    && (option.optionOrder == Constants.NO_SPECIFIC_ORDER || option.optionOrder == event.getOptionOrder())) {
                return option;
            }
        }
        return null;
    }
}
