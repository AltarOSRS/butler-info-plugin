package com.butlerinfo;

import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.Counter;

public class ItemCounter extends Counter
{
    ItemCounter(Plugin plugin, Servant servant, ItemManager itemManager) {
        super(itemManager.getImage(servant.getItem() != null ? servant.getItem().getItemId() : ItemID.PLANK),
                plugin,
                servant.getItemAmountHeld());

        setTooltip(String.format("%s currently has %s %s(s)", servant.getType().getName(), servant.getItemAmountHeld(), servant.getItem().getName()));
    }
}
