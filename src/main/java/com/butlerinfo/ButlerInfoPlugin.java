package com.butlerinfo;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.ScriptID;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.Text;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
	name = "Butler Info"
)
public class ButlerInfoPlugin extends Plugin
{
	private static final Pattern ITEM_AMOUNT_MATCHER = Pattern.compile(
			"^Master, I have returned with what thou asked me to retrieve. As I see thy inventory is full, I shall wait with these (\\d+) items until thou art ready."
	);

	private static final Pattern NOT_ENOUGH_IN_BANK_MATCHER = Pattern.compile(
			"^Master, I dearly wish that I could perform your instruction in full, but alas, I can only carry (\\d+) items."
	);

	private static final String SINGLE_ITEM_TEXT = "Master, I have returned with what thou asked me to retrieve. As I see thy inventory is full, I shall wait with the last item until thou art ready.";

	private static final String NO_EXTRA_ITEMS_TEXT = "Master, I have returned with what you asked me to retrieve.";

	@Inject
	private Client client;

	@Inject
	private ButlerInfoConfig config;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private PlayerOwnedHouse playerOwnedHouse;

	@Inject
	private EventBus eventBus;

	@Inject
	private KeyManager keyManager;

	@Inject
	@Getter
	private DialogManager dialogManager;

	@Getter
	@Setter
	private Servant servant;

	private BankRunTimer bankRunTimer;

	private ItemCounter itemCounter;

	private TripsUntilPaymentCounter tripsUntilPaymentCounter;

	@Getter
	@Setter
	private boolean sendingItemsBack = false;

	@Getter
	@Setter
	private boolean bankTimerReset = false;

	@Provides
	ButlerInfoConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ButlerInfoConfig.class);
	}


	@Override
	protected void startUp()
	{
		eventBus.register(playerOwnedHouse);
		eventBus.register(dialogManager);
		keyManager.registerKeyListener(dialogManager);
	}

	@Override
	protected void shutDown()
	{
		eventBus.register(playerOwnedHouse);
		eventBus.unregister(dialogManager);
		keyManager.unregisterKeyListener(dialogManager);
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		Widget npcDialog = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);
		if (npcDialog == null) {
			return;
		}
		String text = Text.sanitizeMultilineText(npcDialog.getText());
		final Matcher itemAmountMatcher = ITEM_AMOUNT_MATCHER.matcher(text);
		final Matcher notEnoughInBankMatcher = NOT_ENOUGH_IN_BANK_MATCHER.matcher(text);
		if (itemAmountMatcher.find()) {
			servant.finishBankRun(Integer.parseInt(itemAmountMatcher.group(1)));
		}
		if (notEnoughInBankMatcher.find()) {
			if (!isBankTimerReset()) {
				setBankTimerReset(true);
				removeBankRunTimer(false);
				servant.setTripsUntilPayment(servant.getPrevTripsUntilPayment());
			}
		}
		if (text.equals(SINGLE_ITEM_TEXT)) {
			servant.finishBankRun(1);
		}
		if (text.equals(NO_EXTRA_ITEMS_TEXT)) {
			servant.finishBankRun(0);
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		if(event.getNpc() == null || servant != null) {
			return;
		}
		Optional<Servant> servantOptional = Servant.getByNpcId(event.getNpc().getId());
		servantOptional.ifPresent(s -> {
			s.setPlugin(this);
			setServant(s);
		});
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		switch (event.getKey()) {
			case "onlyBuildingMode":
				if (config.onlyInBuildingMode() && !playerOwnedHouse.isBuildingMode()) {
					removeAll();
				} else {
					renderAll();
				}
				break;
			case "showItemCountInfobox":
				if (config.showItemCountInfobox()) {
					renderItemCounter();
				} else {
					removeItemCounter();
				}
				break;
			case "showBankRunTimer":
				if (config.showBankRunTimer()) {
					renderBankRunTimer();
				} else {
					removeBankRunTimer(true);
				}
				break;
			case "showTripsUntilPayment":
				if (config.showTripsUntilPayment()) {
					renderTripsUntilPayment();
				} else {
					removeTripsUntilPayment();
				}
				break;
		}
	}

	public void renderItemCounter() {
		if (!config.showItemCountInfobox() || (config.onlyInBuildingMode() && !playerOwnedHouse.isBuildingMode())) {
			return;
		}

		removeItemCounter();
		if (servant.getItemAmount() <= 0) {
			return;
		}

		itemCounter = new ItemCounter(this, servant, itemManager);

		infoBoxManager.addInfoBox(itemCounter);
	}

	private void removeItemCounter()
	{
		if (itemCounter == null)
		{
			return;
		}

		infoBoxManager.removeInfoBox(itemCounter);
		itemCounter = null;
	}

	public void startBankRunTimer() {
		if (servant == null) {
			return;
		}
		bankRunTimer = new BankRunTimer(this, servant, itemManager);
		renderBankRunTimer();
	}

	private void renderBankRunTimer()
	{
		if (!config.showBankRunTimer() || (config.onlyInBuildingMode() && !playerOwnedHouse.isBuildingMode())) {
			return;
		}
		if (bankRunTimer == null)
		{
			return;
		}

		removeBankRunTimer(true);
		infoBoxManager.addInfoBox(bankRunTimer);
	}

	private void removeBankRunTimer(boolean preserveTimer)
	{
		if (bankRunTimer == null)
		{
			return;
		}

		infoBoxManager.removeInfoBox(bankRunTimer);
		if (!preserveTimer) {
			bankRunTimer = null;
		}
	}

	public void renderTripsUntilPayment() {
		if (!config.showTripsUntilPayment() || (config.onlyInBuildingMode() && !playerOwnedHouse.isBuildingMode())) {
			return;
		}

		removeTripsUntilPayment();
		if (servant.getTripsUntilPayment() <= 0) {
			return;
		}

		tripsUntilPaymentCounter = new TripsUntilPaymentCounter(this, servant, itemManager);

		infoBoxManager.addInfoBox(tripsUntilPaymentCounter);
	}

	private void removeTripsUntilPayment()
	{
		if (tripsUntilPaymentCounter == null)
		{
			return;
		}

		infoBoxManager.removeInfoBox(tripsUntilPaymentCounter);
		tripsUntilPaymentCounter = null;
	}

	public void renderAll() {
		renderItemCounter();
		renderBankRunTimer();
		renderTripsUntilPayment();
	}

	private void removeAll() {
		removeItemCounter();
		removeBankRunTimer(true);
		removeTripsUntilPayment();
	}
}
