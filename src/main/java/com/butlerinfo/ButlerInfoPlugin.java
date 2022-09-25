package com.butlerinfo;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
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
	private OverlayManager overlayManager;

	@Inject
	private ServantOverlay servantOverlay;

	@Inject
	private ItemManager itemManager;

	@Inject
	@Getter
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

	private BankTripTimer bankTripTimer;

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
		overlayManager.add(servantOverlay);
	}

	@Override
	protected void shutDown()
	{
		eventBus.unregister(playerOwnedHouse);
		eventBus.unregister(dialogManager);
		keyManager.unregisterKeyListener(dialogManager);
		overlayManager.remove(servantOverlay);
		removeAll();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING && config.shouldResetSession())
		{
			servant = null;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (servant == null) {
			return;
		}
		Widget npcDialog = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);
		if (npcDialog == null) {
			return;
		}
		String text = Text.sanitizeMultilineText(npcDialog.getText());
		final Matcher itemAmountMatcher = ITEM_AMOUNT_MATCHER.matcher(text);
		final Matcher notEnoughInBankMatcher = NOT_ENOUGH_IN_BANK_MATCHER.matcher(text);
		if (itemAmountMatcher.find()) {
			servant.finishBankTrip(Integer.parseInt(itemAmountMatcher.group(1)));
		}
		if (notEnoughInBankMatcher.find()) {
			if (!isBankTimerReset()) {
				setBankTimerReset(true);
				removeBankTripTimer(false);
				servant.setTripsUntilPayment(servant.getPrevTripsUntilPayment());
			}
		}
		if (text.equals(SINGLE_ITEM_TEXT)) {
			servant.finishBankTrip(1);
		}
		if (text.equals(NO_EXTRA_ITEMS_TEXT)) {
			servant.finishBankTrip(0);
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		if(event.getNpc() == null || servant != null) {
			return;
		}
		Optional<ServantType> typeOptional = ServantType.getByNpcId(event.getNpc().getId());
		typeOptional.ifPresent(type -> {
			Servant servant = new Servant(type);
			servant.setPlugin(this);
			setServant(servant);
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
			case "showItemCount":
				if (config.showItemCount()) {
					renderItemCounter();
				} else {
					removeItemCounter();
				}
				break;
			case "showBankTripTimer":
				if (config.showBankTripTimer()) {
					renderBankTripTimer();
				} else {
					removeBankTripTimer(true);
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
		if (!config.showItemCount() || (config.onlyInBuildingMode() && !playerOwnedHouse.isBuildingMode())) {
			return;
		}

		removeItemCounter();
		if (servant.getItemAmountHeld() <= 0) {
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

	public void startBankTripTimer() {
		if (servant == null) {
			return;
		}
		bankTripTimer = new BankTripTimer(this, servant, itemManager);
		renderBankTripTimer();
	}

	private void renderBankTripTimer()
	{
		if (!config.showBankTripTimer() || (config.onlyInBuildingMode() && !playerOwnedHouse.isBuildingMode())) {
			return;
		}
		if (bankTripTimer == null)
		{
			return;
		}

		removeBankTripTimer(true);
		infoBoxManager.addInfoBox(bankTripTimer);
	}

	private void removeBankTripTimer(boolean preserveTimer)
	{
		if (bankTripTimer == null)
		{
			return;
		}

		infoBoxManager.removeInfoBox(bankTripTimer);
		if (!preserveTimer) {
			bankTripTimer = null;
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
		renderBankTripTimer();
		renderTripsUntilPayment();
	}

	private void removeAll() {
		removeItemCounter();
		removeBankTripTimer(true);
		removeTripsUntilPayment();
	}
}
