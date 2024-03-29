package org.scripts.kotlin.content.commands

import com.runelive.GameSettings
import com.runelive.model.Locations
import com.runelive.model.StaffRights
import com.runelive.model.Store
import com.runelive.model.player.command.Command
import com.runelive.world.entity.impl.player.Player

/**
 * "The digital revolution is far more significant than the invention of writing or even of printing." - Douglas
 * Engelbart
 * Created on 7/27/2016.

 * @author Seba
 */
class ClaimStorePurchase(staffRights: StaffRights) : Command(staffRights) {

    override fun execute(player: Player, args: Array<String>?, privilege: StaffRights) {
        if (!GameSettings.STORE_CONNECTIONS) {
            player.packetSender.sendMessage("The store is currently offline! Try again in 30 minutes.")
            return
        }
        if (player.claimingStoreItems) {
            player.packetSender.sendMessage("You already have a active store claim process going...")
            return
        }
        player.packetSender.sendMessage("Checking for any store purchases...")
        Store.claimItem(player)
    }
}
