package org.scripts.kotlin.content.dialog.npcs;

import com.chaos.model.container.impl.Shop;
import com.chaos.model.options.fouroption.FourOption;
import com.chaos.model.player.GameMode;
import com.chaos.model.player.dialog.Dialog;
import com.chaos.model.player.dialog.DialogHandler;
import com.chaos.model.player.dialog.DialogMessage;
import com.chaos.world.entity.impl.player.Player;

public class David extends Dialog {

    public David(Player player) {
        super(player);
        setEndState(1);
    }

    @Override
    public DialogMessage getMessage() {
        switch (getState()) {
            case 0:
                return Dialog.createNpc(DialogHandler.CALM, "Hello! Would you like to see what skilling items I have for sale?");
            case 1:
            return Dialog.createOption(new FourOption(
                    "Show me your skilling supplies",
                    "Fishing Shop",
                    "Hunter Shop",
                    "Cancel") {
                @Override
                public void execute(Player player, OptionType option) {
                    switch(option) {
                        case OPTION_1_OF_4:
                            if (player.getGameModeAssistant().getGameMode() == GameMode.IRONMAN) {
                                Shop.ShopManager.getShops().get(40).open(player);
                            } else {
                                Shop.ShopManager.getShops().get(30).open(player);
                            }
                            break;
                        case OPTION_2_OF_4:
                            Shop.ShopManager.getShops().get(38).open(player);
                            break;
                        case OPTION_3_OF_4:
                            Shop.ShopManager.getShops().get(39).open(player);
                            break;
                        case OPTION_4_OF_4:
                            player.getPacketSender().sendInterfaceRemoval();
                            break;
                    }
                }
            });
            }
        return null;
    }
}
