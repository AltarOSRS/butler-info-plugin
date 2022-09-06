package com.butlerinfo;

import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.Timer;

import java.time.temporal.ChronoUnit;

public class BankRunTimer extends Timer
{
    BankRunTimer(Plugin plugin, Servant servant, ItemManager itemManager)
    {
        super(servant.getTicks() * 600L, ChronoUnit.MILLIS, itemManager.getImage(ItemID.WATCH), plugin);
        setTooltip("Time left until servant returns.");
    }
}
