package org.scripts.kotlin.content.commands;

import com.runelive.model.StaffRights;
import com.runelive.model.player.command.Command;
import com.runelive.world.entity.impl.player.Player;
import org.scripts.kotlin.content.dialog.GiveDonatorDialog;

/**
 * "The digital revolution is far more significant than the invention of writing or even of printing." - Douglas
 * Engelbart
 * Created on 8/28/2016.
 *
 * @author Seba
 */
public class GiveDonor extends Command {

    public GiveDonor(StaffRights staffRights) {
        super(staffRights);
    }

    @Override
    public void execute(Player player, String[] args, StaffRights privilege) {
        if(args.length < 1) {
            player.getPacketSender().sendMessage("You must use the command as ::givedonator-name");
            return;
        }
        String name = args[0];
        player.getDialog().sendDialog(new GiveDonatorDialog(player, name));
    }
}
