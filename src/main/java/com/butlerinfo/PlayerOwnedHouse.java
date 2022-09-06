package com.butlerinfo;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;

public class PlayerOwnedHouse
{
    public static final int BUILDING_MODE_VARP = 780;

    public static final int BUILDING_MODE_VARBIT = 2176;

    @Inject
    private Client client;

    @Getter
    private ButlerInfoPlugin plugin;

    @Getter
    @Setter
    private boolean buildingMode;

    @Inject
    public PlayerOwnedHouse(ButlerInfoPlugin plugin)
    {
        this.plugin = plugin;
        setBuildingMode(false);
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        if (event.getIndex() == PlayerOwnedHouse.BUILDING_MODE_VARP)
        {
            setBuildingMode(client.getVarbitValue(PlayerOwnedHouse.BUILDING_MODE_VARBIT) == 1);
            if (plugin.getServant() != null) {
                plugin.renderAll();
            }
        }
    }
}
