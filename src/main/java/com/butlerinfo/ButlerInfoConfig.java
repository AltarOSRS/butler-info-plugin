package com.butlerinfo;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
@ConfigGroup("butler-info")
public interface ButlerInfoConfig extends Config
{
    @ConfigItem(
            position = 0,
            keyName = "onlyBuildingMode",
            name = "Only show in building mode",
            description = "Only display overlay/infoboxes when in building mode"
    )
    default boolean onlyInBuildingMode()
    {
        return true;
    }

    @ConfigItem(
            position = 1,
            keyName = "shouldResetSession",
            name = "Reset when exiting/entering house",
            description = "Resets the session and all trackers when you exit/enter your house"
    )
    default boolean shouldResetSession()
    {
        return true;
    }

    @ConfigSection(
            name = "InfoBoxes",
            description = "Settings for the butler infoboxes",
            position = 2
    )
    String infoBoxes = "InfoBoxes";
    @ConfigItem(
            position = 3,
            keyName = "showItemCount",
            name = "Show held item count",
            description = "Display the butler's held item count infobox",
            section = infoBoxes
    )
    default boolean showItemCount()
    {
        return true;
    }

    @ConfigItem(
            position = 4,
            keyName = "showBankTripTimer",
            name = "Show bank trip timer",
            description = "Display the butler's bank trip timer infobox",
            section = infoBoxes
    )
    default boolean showBankTripTimer()
    {
        return true;
    }

    @ConfigItem(
            position = 5,
            keyName = "showTripsUntilPayment",
            name = "Show trips until next payment",
            description = "Display the number of trips until another payment is required infobox",
            section = infoBoxes
    )
    default boolean showTripsUntilPayment()
    {
        return true;
    }

    @ConfigSection(
            name = "Overlay",
            description = "Settings for the butler info overlay",
            position = 6
    )
    String overlay = "Overlay";
    @ConfigItem(
            position = 7,
            keyName = "showTotalAmountPayed",
            name = "Show total amount payed",
            description = "Display the total amount payed during session",
            section = overlay
    )
    default boolean showTotalAmountPayed()
    {
        return true;
    }

    @ConfigItem(
            position = 8,
            keyName = "showTotalBankTrips",
            name = "Show total bank trips made",
            description = "Display the total amount of bank trips made by your servant during session",
            section = overlay
    )
    default boolean showTotalBankTrips()
    {
        return true;
    }
}
