package com.butlerinfo;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Slf4j
public enum ChatOption
{
    TAKE_ITEMS_BACK(
            "Select an option",
            "^Take (.+) back to the bank",
            1,
            (event, option) -> event.getPlugin().setSendingItemsBack(true)),

    PAY_SERVANT(
            "Select an option",
            "^Okay, here's (?<quantity>.+) coins.",

            1,
            (event, option) -> {
                // Reset to the full 8 trips after manual payment.
                log.info("[TRIP COUNTER] Manual payment detected. Resetting trip counter to {}.", Servant.TRIPS_PER_PAYMENT);
                int paymentAmount = option.getQuantityReferenced(event.getText());

                event.getPlugin().getServant().setPaymentAmount(paymentAmount);
                event.getPlugin().getServant().addPaymentToTotal(paymentAmount);
                event.getPlugin().getServant().setTripsUntilPayment(Servant.TRIPS_PER_PAYMENT);
            }),

    REPEAT_TASK(
            "Repeat last task?",
            // Includes "Take to bank" as an option that initiates a trip
            "^(Fetch from bank|Un-note|Take to bank): (?<quantity>\\d+) x (?<item>.+)",
            1,
            (event, option) -> {
                String item = option.getItemReferenced(event.getText());
                event.getPlugin().getServant().sendOnBankTrip(item);
            }),

    SEND_SERVANT_FOR_ITEM(
            "Select an option",
            "^(?<item>.+planks|Soft clay|Limestone brick|Steel bar|Cloth|Gold leaf|Marble block|Magic housing stone|Marrentill)",
            Constants.NO_SPECIFIC_ORDER,
            (event, option) -> {
                event.getPlugin().getDialogManager().setEnteringAmount(true);
                String item =
                        option.getItemReferenced(event.getText());
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

    private final BiConsumer<ChatOptionEvent, ChatOption> action;
    ChatOption(String optionPrompt, String text, int optionOrder, BiConsumer<ChatOptionEvent, ChatOption> action) {
        this.optionPrompt = optionPrompt;
        this.text = text;
        this.optionOrder = optionOrder;
        this.action = action;
    }

    public void executeAction(ChatOptionEvent event, ChatOption option) {
        if (event.getPlugin().getServant() == null) {
            return;
        }
        action.accept(event, option);
    }

    public Pattern getOptionPromptPattern() {
        return Pattern.compile(optionPrompt);
    }

    public Pattern getTextPattern() {
        return Pattern.compile(text);
    }

    public int getQuantityReferenced(String eventOptionText) {
        Matcher matcher = getTextPattern().matcher(eventOptionText);
        if (matcher.find()) {
            String matchedText = matcher.group("quantity");
            try {
                return Integer.parseInt(matchedText.replace(",", ""));
            } catch(NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public String getItemReferenced(String eventOptionText) {
        Matcher matcher = getTextPattern().matcher(eventOptionText);
        if (matcher.find()) {
            return matcher.group("item");
        }
        return "";
    }

    public static ChatOption getByEvent(ChatOptionEvent event) {
        log.info("--- STEP 3: CHECKING FOR REGEX MATCH ---");
        for (ChatOption option : ChatOption.values()) {
            if(option.getOptionPromptPattern().matcher(event.getOptionPrompt()).find()
                    && option.getTextPattern().matcher(event.getText()).find()
                    && (option.optionOrder == Constants.NO_SPECIFIC_ORDER || option.optionOrder == event.getOptionOrder())) {

                log.info(">>> SUCCESS: Matched with enum '{}'", option.name());
                return option;
            }
        }
        log.warn(">>> FAILED: No regex match found for the selected option.");
        return null;
    }
}