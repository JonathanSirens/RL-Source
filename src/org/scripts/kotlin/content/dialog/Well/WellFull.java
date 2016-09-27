package org.scripts.kotlin.content.dialog.Well;

import com.chaos.model.player.dialog.Dialog;
import com.chaos.model.player.dialog.DialogHandler;
import com.chaos.model.player.dialog.DialogMessage;
import com.chaos.world.entity.impl.player.Player;

public class WellFull extends Dialog {

    public Dialog dialog = this;

    public WellFull(Player player) {
        super(player);
        setEndState(0);
    }

    @Override
    public DialogMessage getMessage() {
        switch (getState()) {
            case 0:
                return Dialog.createNpc(DialogHandler.CALM, "This well is full and bonus is currently active. ");
        }
        return null;
    }
}