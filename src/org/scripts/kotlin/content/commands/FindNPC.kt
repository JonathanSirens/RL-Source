package org.scripts.kotlin.content.commands

import com.chaos.model.StaffRights
import com.chaos.model.definitions.ItemDefinition
import com.chaos.model.definitions.NpcDefinition
import com.chaos.model.player.command.Command
import com.chaos.world.entity.impl.player.Player

/**
 * "The digital revolution is far more significant than the invention of writing or even of printing." - Douglas
 * Engelbart
 * Created on 8/6/2016.

 * @author Seba
 */
class FindNPC(staffRights: StaffRights) : Command(staffRights) {

    override fun execute(player: Player, args: Array<String>?, privilege: StaffRights) {
        if (args == null) {
            player.packetSender.sendMessage("Example usage: ::findnpc-npcname")
        } else {
            val name = args[0].toLowerCase()
            var found = false
            player.packetSender.sendMessage("Trying to find name id for npc name " + name)
            for (i in 0..NpcDefinition.getMaxAmountOfNpcs() - 1 - 1) {
                if (NpcDefinition.forId(i).name.toLowerCase().contains(name)) {
                    player.packetSender.sendMessage("Found npc with name [" + ItemDefinition.forId(i).getName() + "] - id: " + i)
                    found = true
                }
            }
            if (!found) {
                player.packetSender.sendMessage("No name for the name $name were found.")
            }
        }
    }
}
