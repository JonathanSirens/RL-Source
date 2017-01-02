package org.scripts.kotlin.content.dialog;

import com.chaos.model.options.threeoption.ThreeOption;
import com.chaos.model.player.dialog.Dialog;
import com.chaos.model.player.dialog.DialogMessage;
import com.chaos.world.content.Scoreboard;
import com.chaos.world.entity.impl.player.Player;

public class ChooseScoreboard extends Dialog {

    public ChooseScoreboard(Player player) {
        super(player);
        setEndState(0);
    }

    @Override
    public DialogMessage getMessage() {
        switch (getState()) {
            case 0:
                return Dialog.createOption(new ThreeOption(
                        "Top Pkers",
                        "Top Killstreaks",
                        "Top Skillers") {
                    @Override
                    public void execute(Player player, OptionType option) {
                        switch (option) {
                            case OPTION_1_OF_3:
                                player.getPacketSender().sendInterfaceRemoval();
                                Scoreboard.open(player, 1);
                                break;
                            case OPTION_2_OF_3:
                                player.getPacketSender().sendInterfaceRemoval();
                                Scoreboard.open(player, 2);
                                break;
                            case OPTION_3_OF_3:
                                player.getPacketSender().sendInterfaceRemoval();
                                Scoreboard.open(player, 3);
                                break;
                        }
                    }
                });
            }
        return null;
    }
}