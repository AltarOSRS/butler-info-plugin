package net.runelite.client.plugins.butlerinfo;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Slf4j
@PluginDescriptor(
        name = "Butler Info"
)
public class ButlerInfoPlugin extends Plugin
{
    private static class Constants {
        // Object IDs used to detect if the player is currently inside a Player-Owned House (POH).
        static final Set<Integer> POH_DETECTION_OBJECT_IDS = ImmutableSet.of(
                13197, 13198, 13199, // GILDED_ALTAR_IDS
                4525, // EXIT_PORTAL_ID
                29146, // TIP JAR

                4529, // Dungeon Entrance
                29241 // Ornate Rejuvination Pool
        );
        static final int SCENE_SIZE = 104;
        static final int MAX_PLANE = 4;
    }

    private static final Pattern ITEM_AMOUNT_MATCHER = Pattern.compile(
            "Master, I have returned with what thou asked me to retrieve\\. As I see thy inventory is full, I shall wait with these (\\d+) items until thou art ready\\."
    );
    private static final Pattern NOT_ENOUGH_IN_BANK_MATCHER = Pattern.compile(
            "Master, I dearly wish that I could perform your instruction in full, but alas, I can only carry (\\d+) items\\."
    );
    private static final String SINGLE_ITEM_TEXT = "Master, I have returned with what thou asked me to retrieve. As I see thy inventory is full, I shall wait with the last item until thou art ready.";
    private static final String NO_EXTRA_ITEMS_TEXT = "Master, I have returned with what you asked me to retrieve.";
    private static final String TAKE_ITEMS_BACK_CONFIRMATION = "Very well, Master.";
    private static final String MONEY_BAG_PAYMENT_TEXT = "Your servant takes some payment from the money bag.";
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
    // Use the FractionalTripCounter for trips until payment
    private FractionalTripCounter tripsUntilPaymentCounter;
    @Getter
    @Setter
    private boolean sendingItemsBack = false;
    @Getter
    @Setter
    private boolean bankTimerReset = false;
    @Getter
    @Setter
    private boolean butlerIsLeaving = false;

    private boolean isPlayerInPOH = false;
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
        servant = null;
        isPlayerInPOH = false;
        butlerIsLeaving = false;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOADING && config.shouldResetSession())
        {
            servant = null;
            removeAll();
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (servant == null)
        {
            return;
        }

        if (event.getType() == ChatMessageType.DIALOG)
        {
            String text = Text.sanitizeMultilineText(event.getMessage());
            final Matcher itemAmountMatcher = ITEM_AMOUNT_MATCHER.matcher(text);
            final Matcher notEnoughInBankMatcher = NOT_ENOUGH_IN_BANK_MATCHER.matcher(text);
            if (text.contains(TAKE_ITEMS_BACK_CONFIRMATION)) {
                if (isSendingItemsBack()) {
                    log.info(">>> SUCCESS: Butler confirmation detected. Awaiting 'continue' action.");
                    setButlerIsLeaving(true);
                    setSendingItemsBack(false);
                    return;
                }
            }

            if (itemAmountMatcher.find()) {
                servant.finishBankTrip(Integer.parseInt(itemAmountMatcher.group(1)));
                return;
            }
            if (notEnoughInBankMatcher.find()) {
                if (!isBankTimerReset()) {
                    setBankTimerReset(true);
                    removeBankTripTimer(false);

                }
                return;
            }
            if (text.contains(SINGLE_ITEM_TEXT)) {
                servant.finishBankTrip(1);
                return;
            }
            if (text.contains(NO_EXTRA_ITEMS_TEXT)) {
                servant.finishBankTrip(0);
            }
        }
        else if (event.getType() == ChatMessageType.GAMEMESSAGE)
        {
            String message = Text.removeTags(event.getMessage());
            if (message.startsWith(MONEY_BAG_PAYMENT_TEXT))
            {
                log.info("[TRIP COUNTER] Money bag payment detected. Resetting trip counter to {}.", Servant.TRIPS_PER_PAYMENT);
                int paymentAmount = servant.getType().getPaymentAmount();
                servant.addPaymentToTotal(paymentAmount);
                servant.setTripsUntilPayment(Servant.TRIPS_PER_PAYMENT);
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick)
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        boolean currentlyInPOH = isInPOH();
        if (!currentlyInPOH && isPlayerInPOH && config.shouldResetSession())
        {
            servant = null;
            removeAll();
        }

        isPlayerInPOH = currentlyInPOH;
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event)
    {
        if (!isPlayerInPOH)
        {
            return;
        }

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

    private boolean isInPOH() {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return false;
        }
        final Set<Integer> detectionIds = Constants.POH_DETECTION_OBJECT_IDS;
        for (int x = 0; x < Constants.SCENE_SIZE; x++) {
            for (int y = 0; y < Constants.SCENE_SIZE; y++) {
                for (int plane = 0; plane < Constants.MAX_PLANE; plane++) {
                    Tile tile = client.getTopLevelWorldView().getScene().getTiles()[plane][x][y];
                    if (tile == null) {
                        continue;
                    }

                    if (tile.getGameObjects() != null) {
                        for (GameObject gameObject : tile.getGameObjects()) {
                            if (gameObject != null && detectionIds.contains(gameObject.getId())) {


                                return true;
                            }
                        }
                    }

                    if (tile.getWallObject() != null) {
                        if (detectionIds.contains(tile.getWallObject().getId())) {


                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void renderItemCounter() {
        if (!config.showItemCount() || (config.onlyInBuildingMode() && !playerOwnedHouse.isBuildingMode())) {
            return;
        }

        removeItemCounter();
        if (servant == null || servant.getItemAmountHeld() <= 0) {
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
        // Only render if trips until payment is greater than 0.0
        if (servant == null || servant.getTripsUntilPayment() <= 0.0) {
            return;
        }

        // Use the FractionalTripCounter for rendering
        tripsUntilPaymentCounter = new FractionalTripCounter(this, servant, itemManager);
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
        removeBankTripTimer(false);
        removeTripsUntilPayment();
    }
}