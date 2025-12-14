package com.butlerinfo;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyListener;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
@Slf4j
public class DialogManager implements KeyListener
{
    private static final int DIALOG_NPC_TEXT_CHILD_ID = 40;
    private static final int DIALOG_OPTION_CONTAINER_CHILD_ID = 1;
    private static final int CONTINUE_OPTION = -1;
    @Inject
    private Client client;

    @Getter
    private final ButlerInfoPlugin plugin;
    @Getter
    @Setter
    private boolean enteringAmount;
    @Inject
    public DialogManager(ButlerInfoPlugin plugin)
    {
        this.plugin = plugin;
        enteringAmount = false;
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
                handleDialogAction(CONTINUE_OPTION);
                break;
            case KeyEvent.VK_1:
                handleDialogAction(1);
                break;
            case KeyEvent.VK_2:
                handleDialogAction(2);
                break;
            case KeyEvent.VK_3:
                handleDialogAction(3);
                break;
            case KeyEvent.VK_4:
                handleDialogAction(4);
                break;
            case KeyEvent.VK_5:
                handleDialogAction(5);
                break;
            case KeyEvent.VK_ENTER:
                if (isEnteringAmount()) {
                    setEnteringAmount(false);
                    plugin.getServant().sendOnBankTrip();
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (event.getMenuAction() == MenuAction.WIDGET_CONTINUE) {
            handleDialogAction(event.getParam0());
        }
    }

    private void handleDialogAction(int selectedOption)
    {
        if (selectedOption == CONTINUE_OPTION) {
            Widget npcDialogWidget = client.getWidget(InterfaceID.CHATBOX, DIALOG_NPC_TEXT_CHILD_ID);
            fireChatContinueEvent(npcDialogWidget);
        } else {
            Widget playerDialogueOptionsWidget = client.getWidget(InterfaceID.CHATMENU, DIALOG_OPTION_CONTAINER_CHILD_ID);
            fireChatOptionEvent(playerDialogueOptionsWidget, selectedOption);
        }
    }

    private void fireChatContinueEvent(Widget widget)
    {
        if (plugin.isButlerIsLeaving())
        {
            plugin.setButlerIsLeaving(false);
// Reset state
            if (plugin.getServant() != null)
            {
                plugin.getServant().setItemAmountHeld(0);
// Handle the return trip as a 0.5 decrement
                plugin.getServant().handleReturnTrip();
                plugin.startBankTripTimer();
            }
            return;
        }

        if (widget == null) {
            return;
        }

        String text = Text.sanitizeMultilineText(widget.getText());
        log.info("Butler Info Debug (NPC Continue): Raw text is \"{}\"", text);

        ChatContinueEvent continueEvent = new ChatContinueEvent(plugin, text);
        ChatContinue chatContinue = ChatContinue.getByEvent(continueEvent);
        if(chatContinue != null) {
            chatContinue.executeAction(continueEvent, chatContinue);
        }
    }

    private void fireChatOptionEvent(Widget widget, int selectedOption)
    {
        if (widget == null || widget.getChildren() == null) {
            return;
        }

        log.info("--- Butler Info Debug (Player Option) ---");
        Widget[] dialogueOptions = widget.getChildren();
        log.info("Option Prompt: \"{}\"", dialogueOptions[0].getText());
        for (int i = 1; i < dialogueOptions.length; i++) {
            if (dialogueOptions[i] != null) {
                String optionText = dialogueOptions[i].getText();
                log.info("Available Option [{}]: \"{}\"", i, optionText);
            }
        }
        log.info("Player Selected Option [{}]: \"{}\"", selectedOption, dialogueOptions[selectedOption].getText());
        ChatOptionEvent chatOptionEvent = new ChatOptionEvent(
                plugin,
                dialogueOptions[0].getText(),
                dialogueOptions[selectedOption].getText(),
                selectedOption);
        ChatOption chatOption = ChatOption.getByEvent(chatOptionEvent);
        if (chatOption != null) {
            chatOption.executeAction(chatOptionEvent, chatOption);
        }
    }
}