package org.scripts.kotlin.content.dialog.Well;

import com.chaos.model.options.Option;
import com.chaos.model.options.threeoption.ThreeOption;
import com.chaos.model.player.dialog.Dialog;
import com.chaos.model.player.dialog.DialogHandler;
import com.chaos.model.player.dialog.DialogMessage;
import com.chaos.util.Misc;
import com.chaos.world.content.Well.WellOfGoodness;
import com.chaos.world.content.Well.WellOfGoodwill;
import com.chaos.world.entity.impl.player.Player;

public class LookDownWell extends Dialog {

    public Dialog dialog = this;

    String wellName = "";
    String wellNameActual = "";

    public LookDownWell(Player player, String well) {
        super(player);
        setEndState(1);
        wellName = well;
    }

    @Override
    public DialogMessage getMessage() {
        switch (getState()) {
            case 0:
                return Dialog.createOption(new ThreeOption(
                        "Check Bonus Xp Well", //exp
                        "Check Well of Wealth", //drops
                        "Check Well of Execution") { //pkp
                    @Override
                    public void execute(Player player, OptionType option) {
                        switch (option) {
                            case OPTION_1_OF_3:
                                wellName = "exp";
                                wellNameActual = "exp";
                                setState(1);
                                player.getDialog().sendDialog(dialog);
                                break;
                            case OPTION_2_OF_3:
                                wellName = "drops";
                                wellNameActual = "wealth";
                                setState(1);
                                player.getDialog().sendDialog(dialog);
                                break;
                            case OPTION_3_OF_3:
                                wellName = "pkp";
                                wellNameActual = "execution";
                                setState(1);
                                player.getDialog().sendDialog(dialog);
                                break;
                        }
                    }
                });
            case 1:
                return Dialog.createNpc(DialogHandler.CALM, "The well of " + wellNameActual + " needs another " + Misc.insertCommasToNumber("" + WellOfGoodness.getMissingAmount(wellName) + "") + " coins before it is active.");
        }
        return null;
    }
}