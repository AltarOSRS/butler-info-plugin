package com.butlerinfo;

import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.Counter;

import java.text.NumberFormat;
import java.util.Locale;

public class TripsUntilPaymentCounter extends Counter
{
    TripsUntilPaymentCounter(Plugin plugin, Servant servant, ItemManager itemManager) {
        super(itemManager.getImage(
                    ItemID.COINS_995,
                    servant.getPaymentAmount(),
                    false),
                plugin,
                servant.getTripsUntilPayment());

        String tooltipText = String.format(
                "%s bank trip(s) before you will have to pay %s gp",
                servant.getTripsUntilPayment(),
                NumberFormat.getNumberInstance(Locale.US).format(servant.getPaymentAmount()));
        setTooltip(tooltipText);
    }
}
