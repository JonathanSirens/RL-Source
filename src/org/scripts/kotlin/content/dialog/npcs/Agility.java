package org.scripts.kotlin.content.dialog.npcs;

import com.chaos.model.container.impl.Shop;
import com.chaos.model.input.impl.BuyAgilityExperience;
import com.chaos.model.options.threeoption.ThreeOption;
import com.chaos.model.options.twooption.TwoOption;
import com.chaos.model.player.dialog.Dialog;
import com.chaos.model.player.dialog.DialogHandler;
import com.chaos.model.player.dialog.DialogMessage;
import com.chaos.world.entity.impl.player.Player;

public class Agility extends Dialog {

    public Dialog dialog = this;

    public Agility(Player player) {
        super(player);
        setEndState(2);
    }

    @Override
    public DialogMessage getMessage() {
        switch (getState()) {
            case 0:
                return Dialog.createNpc(DialogHandler.CALM, "Hello! What can I do for you?");
            case 1:
            return Dialog.createOption(new ThreeOption(
                    "I want to buy experience",
                    "I want to buy equipment",
                    "Cancel") {
                @Override
                public void execute(Player player, OptionType option) {
                    switch(option) {
                        case OPTION_1_OF_3:
                            Dialog.createNpc(DialogHandler.CALM, "@bla@Would you like to exchange tickets for experience?" +
                                    " One ticket currently grants @red@7680@bla@ Agility experience.?");
                            setState(2);
                            player.getDialog().sendDialog(dialog);
                            break;
                        case OPTION_2_OF_3:
                            Shop.ShopManager.getShops().get(28).open(player);
                            break;
                        case OPTION_3_OF_3:
                            player.getPacketSender().sendInterfaceRemoval();
                            break;
                    }
                }
            });
            case 2:
                return Dialog.createOption(new TwoOption(
                        "Yes I want to exchange some tickets",
                        "Cancel") {
                    @Override
                    public void execute(Player player, OptionType option) {
                        switch(option) {
                            case OPTION_1_OF_2:
                                player.getPacketSender().sendInterfaceRemoval();
                                player.setInputHandling(new BuyAgilityExperience());
                                player.getPacketSender().sendEnterAmountPrompt("How many tickets would you like to exchange?");
                                break;
                            case OPTION_2_OF_2:
                                player.getPacketSender().sendInterfaceRemoval();
                                break;
                        }
                    }
                });
            }
        return null;
    }
}