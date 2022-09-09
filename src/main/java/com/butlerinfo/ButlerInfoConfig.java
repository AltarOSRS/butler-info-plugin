package com.butlerinfo;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("butler-info")
public interface ButlerInfoConfig extends Config
{
	@ConfigItem(
			position = 1,
			keyName = "onlyBuildingMode",
			name = "Only show in building mode",
			description = "Only display info when in building mode."
	)
	default boolean onlyInBuildingMode()
	{
		return true;
	}

	@ConfigItem(
			position = 2,
			keyName = "showItemCountInfobox",
			name = "Show item count",
			description = "Display the butler's held item count as an infobox."
	)
	default boolean showItemCountInfobox()
	{
		return true;
	}

	@ConfigItem(
			position = 3,
			keyName = "showBankRunTimer",
			name = "Show bank run timer",
			description = "Display the butler's bank run timer as an infobox."
	)
	default boolean showBankRunTimer()
	{
		return true;
	}

	@ConfigItem(
			position = 4,
			keyName = "showTripsUntilPayment",
			name = "Show trips until next payment",
			description = "Display the number of trips the butler will take before requiring another payment."
	)
	default boolean showTripsUntilPayment()
	{
		return true;
	}
}
