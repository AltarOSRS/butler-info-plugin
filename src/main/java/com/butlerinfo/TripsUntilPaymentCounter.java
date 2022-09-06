package com.butlerinfo;

import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.Counter;

public class TripsUntilPaymentCounter extends Counter
{
    TripsUntilPaymentCounter(Plugin plugin, Servant servant, ItemManager itemManager) {
        super(itemManager.getImage(
                    ItemID.COINS_995,
                    Integer.parseInt(servant.getPaymentAmount().replaceAll(",", "")),
                    false),
                plugin,
                servant.getTripsUntilPayment());

        String tooltipText = String.format(
                "%s bank trip(s) before you will have to pay %s gp",
                servant.getTripsUntilPayment(),
                servant.getPaymentAmount());
        setTooltip(tooltipText);
    }
}
