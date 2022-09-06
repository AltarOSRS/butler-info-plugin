package com.butlerinfo;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyListener;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.event.KeyEvent;

public class DialogManager implements KeyListener
{
    private static final int MENU_OPTION_HOTKEY_SCRIPT_ID = 2153;

    private static final int CONTINUE_OPTION = -1;

    @Inject
    private Client client;

    @Getter
    private ButlerInfoPlugin plugin;

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
                    plugin.getServant().sendOnBankRun();
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
            Widget npcDialogWidget = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);
            fireChatContinueEvent(npcDialogWidget);
        } else {
            Widget playerDialogueOptionsWidget = client.getWidget(WidgetID.DIALOG_OPTION_GROUP_ID, 1);
            fireChatOptionEvent(playerDialogueOptionsWidget, selectedOption);
        }
    }

    private void fireChatContinueEvent(Widget widget)
    {
        if (widget == null) {
            return;
        }

        String text = Text.sanitizeMultilineText(widget.getText());
        ChatContinueEvent continueEvent = new ChatContinueEvent(plugin, text);

        ChatContinue chatContinue = ChatContinue.getByEvent(continueEvent);
        if(chatContinue != null) {
            chatContinue.getAction().accept(continueEvent, chatContinue);
        }
    }

    private void fireChatOptionEvent(Widget widget, int selectedOption)
    {
        if (widget == null || widget.getChildren() == null) {
            return;
        }

        Widget[] dialogueOptions = widget.getChildren();
        ChatOptionEvent chatOptionEvent = new ChatOptionEvent(
                plugin,
                dialogueOptions[0].getText(),
                dialogueOptions[selectedOption].getText(),
                selectedOption);

        ChatOption chatOption = ChatOption.getByEvent(chatOptionEvent);
        if (chatOption != null) {
            chatOption.getAction().accept(chatOptionEvent, chatOption);
        }
    }
}
