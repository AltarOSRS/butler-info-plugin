package com.butlerinfo;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Servant
{
    public static final int TRIPS_PER_PAYMENT = 8;
    @Getter
    private final ServantType type;

    @Setter
    private ButlerInfoPlugin plugin;
    @Getter
    private ConstructionItem item;

    @Getter
    private int itemAmountHeld;
    // Tracks trips until next payment, using a double to handle half-trips (for returns with a full inventory)
    @Getter
    private double tripsUntilPayment;
    @Getter
    @Setter
    private int prevTripsUntilPayment;
    @Getter
    @Setter
    private int paymentAmount;

    @Getter
    private int totalPayed;
    @Getter
    private int totalBankTripsMade;

    Servant(ServantType type)
    {
        this.type = type;
        this.tripsUntilPayment = 0;
        this.prevTripsUntilPayment = 0;
        this.itemAmountHeld = 0;
        this.totalPayed = 0;
        this.totalBankTripsMade = 0;
        this.paymentAmount = type.getPaymentAmount();
        log.info("[TRIP COUNTER] New Servant created. Trip counter initialized to {}.", this.tripsUntilPayment);
    }

    public void setItem(String itemName)
    {
        log.info("--- SERVANT STATE CHANGE: setItem ---");
        log.info("Attempting to set item to: '{}'", itemName);
        ConstructionItem.getByName(singularize(itemName)).ifPresent(item -> {
            this.item = item;
            log.info(">>> SUCCESS: Item set to {}", this.item.getName());
        });
    }

    public void setItemAmountHeld(int value)
    {
        itemAmountHeld = value;
        plugin.renderItemCounter();
    }

    public void setTripsUntilPayment(double value)
    {
        log.info("[TRIP COUNTER] Setting trips. Old value: {}, New value: {}", this.tripsUntilPayment, value);
        tripsUntilPayment = Math.max(value, 0.0);
        plugin.renderTripsUntilPayment();
    }

    // Method to handle the 2-for-1 logic for return trips when inventory is full
    public void handleReturnTrip()
    {
        log.info("[TRIP COUNTER] Decrementing trips by 0.5 for a return trip.");
        setTripsUntilPayment(this.tripsUntilPayment - 0.5);
    }

    public void sendOnBankTrip()
    {
        log.info("[TRIP COUNTER] A bank trip is starting. Decrementing trip counter from {}.", tripsUntilPayment);
        plugin.startBankTripTimer();
        incrementTotalBankTripsMade();
        setTripsUntilPayment(tripsUntilPayment - 1.0);
    }

    public void sendOnBankTrip(String item)
    {
        setItem(item);
        sendOnBankTrip();
    }

    public void finishBankTrip(int itemAmountHeld) {
        plugin.setBankTimerReset(false);
        setItemAmountHeld(itemAmountHeld);
    }

    public void addPaymentToTotal(int paymentAmount) {
        totalPayed += paymentAmount;
    }

    public void incrementTotalBankTripsMade() {
        totalBankTripsMade++;
    }

    private String singularize(String item)
    {
        if(item.charAt(item.length() - 1) == 's') {
            return item.substring(0, item.length() - 1);
        } else {
            return item;
        }
    }
}