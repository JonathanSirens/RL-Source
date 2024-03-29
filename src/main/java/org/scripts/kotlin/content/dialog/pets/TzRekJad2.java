package org.scripts.kotlin.content.dialog.pets;

import com.runelive.model.player.dialog.Dialog;
import com.runelive.model.player.dialog.DialogHandler;
import com.runelive.model.player.dialog.DialogMessage;
import com.runelive.world.entity.impl.player.Player;

public class TzRekJad2 extends Dialog {

    public Dialog dialog = this;

    public TzRekJad2(Player player) {
        super(player);
        setEndState(2);
    }

    @Override
    public DialogMessage getMessage() {
        switch (getState()) {
            case 0:
                return Dialog.createPlayer(DialogHandler.CALM, "Are you hungry?");
            case 1:
                return Dialog.createNpc(DialogHandler.CALM, "Kl-Kra!");
            case 2:
                return Dialog.createPlayer(DialogHandler.CALM, "Ooookay...");
        }
        return null;
    }
}
