package com.butlerinfo;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class ServantOverlay extends OverlayPanel {

    private final ButlerInfoPlugin plugin;

    private final ButlerInfoConfig config;
    @Inject
    private ServantOverlay(ButlerInfoPlugin plugin, ButlerInfoConfig config)
    {
        this.plugin = plugin;
        this.config = config;
        this.setPreferredPosition(OverlayPosition.BOTTOM_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (plugin.getServant() == null || (config.onlyInBuildingMode() && !plugin.getPlayerOwnedHouse().isBuildingMode())) {
            return null;
        }

        if (config.showTotalAmountPayed()) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Total gp paid:")
                    .right(NumberFormat.getNumberInstance(Locale.US).format(plugin.getServant().getTotalPayed()))
                    .build());
        }

        if (config.showTotalBankTrips()) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Bank trips made:")
                    .right(Integer.toString(plugin.getServant().getTotalBankTripsMade()))
                    .build());
        }

        return super.render(graphics);
    }
}