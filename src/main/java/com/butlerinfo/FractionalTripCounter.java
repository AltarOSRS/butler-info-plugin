package com.butlerinfo;

import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class FractionalTripCounter extends InfoBox
{
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");
    private final double count;

    FractionalTripCounter(Plugin plugin, Servant servant, ItemManager itemManager)
    {
        super(itemManager.getImage(995, servant.getPaymentAmount(), false), plugin);
        this.count = servant.getTripsUntilPayment();
        setTooltip(String.format(
                "%s bank trip(s) before you will have to pay %s gp",
                DECIMAL_FORMAT.format(count),
                NumberFormat.getNumberInstance(Locale.US).format(servant.getPaymentAmount())
        ));
    }

    @Override
    public String getText()
    {
        return DECIMAL_FORMAT.format(count);
    }

    @Override
    public Color getTextColor()
    {
        // Always return white for consistency.
        return Color.WHITE;
    }
}