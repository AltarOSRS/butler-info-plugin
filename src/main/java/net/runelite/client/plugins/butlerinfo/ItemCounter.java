package net.runelite.client.plugins.butlerinfo;

import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.Counter;
public class ItemCounter extends Counter
{
    ItemCounter(Plugin plugin, Servant servant, ItemManager itemManager) {
        // Fallback to plank ID (960) if item is null
        super(itemManager.getImage(servant.getItem() != null ? servant.getItem().getItemId() : 960),
                plugin,
                servant.getItemAmountHeld());
// Provide generic tooltip if item is null
        if (servant.getItem() != null)
        {
            setTooltip(String.format("%s currently has %s %s(s)", servant.getType().getName(), servant.getItemAmountHeld(), servant.getItem().getName()));
        }
        else
        {
            setTooltip(String.format("%s is currently holding %s items.", servant.getType().getName(), servant.getItemAmountHeld()));
        }
    }
}