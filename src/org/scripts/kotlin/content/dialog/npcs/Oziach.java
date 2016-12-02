package org.scripts.kotlin.content.dialog.npcs;

import com.chaos.model.container.impl.Shop;
import com.chaos.model.options.threeoption.ThreeOption;
import com.chaos.model.options.twooption.TwoOption;
import com.chaos.model.player.dialog.Dialog;
import com.chaos.model.player.dialog.DialogHandler;
import com.chaos.model.player.dialog.DialogMessage;
import com.chaos.world.entity.impl.player.Player;

public class Oziach extends Dialog {

    public Dialog dialog = this;

    public Oziach(Player player) {
        super(player);
        setEndState(3);
    }

    @Override
    public DialogMessage getMessage() {
        switch (getState()) {
            case 0:
                return Dialog.createNpc(DialogHandler.PLAIN_EVIL, "Hello Traveler! What shop would you like me to open?");
            case 1:
                return Dialog.createOption(new ThreeOption(
                        "PvP Armours",
                        "Weapons & Misc Supplies",
                        "Barrows Armours") {
                    @Override
                    public void execute(Player player, OptionType option) {
                        switch(option) {
                            case OPTION_1_OF_3:
                                Shop.ShopManager.getShops().get(22).open(player);
                                break;
                            case OPTION_2_OF_3:
                                Shop.ShopManager.getShops().get(21).open(player);
                                break;
                            case OPTION_3_OF_3:
                                Shop.ShopManager.getShops().get(48).open(player);
                                break;
                        }
                    }
                });
            }
        return null;
    }
}
