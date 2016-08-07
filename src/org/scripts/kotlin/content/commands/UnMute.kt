package org.scripts.kotlin.content.commands

import com.runelive.model.PlayerRights
import com.runelive.model.player.command.Command
import com.runelive.world.content.PlayerPunishment
import com.runelive.world.entity.impl.player.Player

/**
 * "The digital revolution is far more significant than the invention of writing or even of printing." - Douglas
 * Engelbart
 * Created on 8/6/2016.

 * @author Seba
 */
class UnMute(playerRights: PlayerRights) : Command(playerRights) {

    override fun execute(player: Player, args: Array<String>?, privilege: PlayerRights) {
        if (args == null) {
            player.packetSender.sendMessage("Example: ::unmute-playername")
        } else {
            if (!PlayerPunishment.isMuted(args[0])) {
                player.packetSender.sendMessage(args[0] + " is currently not muted.")
                return
            }
            PlayerPunishment.unMute(args[0])
            player.packetSender.sendMessage(args[0] + " has been successfully unmuted")
        }
    }
}