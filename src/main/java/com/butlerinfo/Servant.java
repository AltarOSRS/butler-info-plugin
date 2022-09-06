package com.butlerinfo;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.NpcID;

import javax.inject.Inject;
import java.util.Optional;

public enum Servant
{
    RICK(NpcID.RICK, "Rick", 100),
    MAID(NpcID.MAID, "Maid", 50),
    COOK(NpcID.COOK, "Cook", 28),
    BUTLER(NpcID.BUTLER, "Butler", 20),
    DEMON_BUTLER(NpcID.DEMON_BUTLER, "Demon butler", 12);

    public static final int TRIPS_PER_PAYMENT = 8;

    @Setter
    private ButlerInfoPlugin plugin;

    @Getter
    private final int npcId;

    @Getter
    private final String name;

    @Getter
    private final int ticks;

    @Getter
    private ConstructionItem item;

    @Getter
    private int itemAmount;

    @Getter
    private int tripsUntilPayment;

    @Getter
    @Setter
    private int prevTripsUntilPayment;

    @Getter
    @Setter
    private String paymentAmount;

    Servant(int npcId, String name, int ticks)
    {
        this.npcId = npcId;
        this.name = name;
        this.ticks = ticks;
        this.tripsUntilPayment = 0;
        this.prevTripsUntilPayment = 0;
        this.itemAmount = 0;
    }

    public void setItem(String itemName)
    {
        ConstructionItem.getByName(singularize(itemName)).ifPresent(item -> this.item = item);
    }

    public void setItemAmount(int value)
    {
        itemAmount = value;
        plugin.renderItemCounter();
    }

    public void setTripsUntilPayment(int value)
    {
        setPrevTripsUntilPayment(tripsUntilPayment);
        tripsUntilPayment = Math.max(value, 0);
        plugin.renderTripsUntilPayment();
    }

    public void sendOnBankRun()
    {
        plugin.startBankRunTimer();
        setTripsUntilPayment(tripsUntilPayment - 1);
    }

    public void sendOnBankRun(String item)
    {
        setItem(item);
        plugin.startBankRunTimer();
        setTripsUntilPayment(tripsUntilPayment - 1);
    }

    public void finishBankRun(int itemAmount) {
        plugin.setBankTimerReset(false);
        setItemAmount(itemAmount);
    }

    public static Optional<Servant> getByNpcId(int npcId)
    {
        for (Servant servant : Servant.values()) {
            if (servant.npcId == npcId) {
                return Optional.of(servant);
            }
        }
        return Optional.empty();
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
