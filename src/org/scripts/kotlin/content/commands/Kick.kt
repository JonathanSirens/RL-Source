package org.scripts.kotlin.content.commands

import com.runelive.model.Locations
import com.runelive.model.PlayerRights
import com.runelive.model.player.command.Command
import com.runelive.world.World
import com.runelive.world.entity.impl.player.Player

/**
 * "The digital revolution is far more significant than the invention of writing or even of printing." - Douglas
 * Engelbart
 * Created on 8/6/2016.

 * @author Seba
 */
class Kick(playerRights: PlayerRights) : Command(playerRights) {

    override fun execute(player: Player, args: Array<String>?, privilege: PlayerRights) {
        if (args == null) {
            player.packetSender.sendMessage("Example usage: ::kick-playername")
        } else {
            val kick = World.getPlayerByName(args[0])
            if (kick == null) {
                player.packetSender.sendMessage("We are unable to find that player.")
                return
            }
            if (kick.location === Locations.Location.DUEL_ARENA) {
                player.packetSender.sendMessage("You cannot kick someone in the duel arena")
                return
            }
            if (kick.location === Locations.Location.WILDERNESS && player.rights.rights < PlayerRights.ADMINISTRATOR.rights) {
                player.packetSender.sendMessage("You cannot kick someone who is in the wilderness")
            }
            kick.forceOffline = true
            World.deregister(kick)
            player.packetSender.sendMessage("You have successfully kicked " + kick.username + ".")
        }
    }
}