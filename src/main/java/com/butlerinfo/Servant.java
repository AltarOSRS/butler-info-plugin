package com.butlerinfo;

import lombok.Getter;
import lombok.Setter;

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

    @Getter
    private int tripsUntilPayment;

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
    }

    public void setItem(String itemName)
    {
        ConstructionItem.getByName(singularize(itemName)).ifPresent(item -> this.item = item);
    }

    public void setItemAmountHeld(int value)
    {
        itemAmountHeld = value;
        plugin.renderItemCounter();
    }

    public void setTripsUntilPayment(int value)
    {
        setPrevTripsUntilPayment(tripsUntilPayment);
        tripsUntilPayment = Math.max(value, 0);
        plugin.renderTripsUntilPayment();
    }

    public void sendOnBankTrip()
    {
        plugin.startBankTripTimer();
        incrementTotalBankTripsMade();
        setTripsUntilPayment(tripsUntilPayment - 1);
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
