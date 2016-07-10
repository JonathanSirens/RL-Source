package org.scripts.kotlin.content.commands.player

import com.runelive.GameSettings
import com.runelive.model.GameMode
import com.runelive.model.Locations.Location
import com.runelive.model.MagicSpellbook
import com.runelive.model.Position
import com.runelive.model.Prayerbook
import com.runelive.model.Skill
import com.runelive.util.Misc
import com.runelive.world.World
import com.runelive.world.content.PlayerPunishment
import com.runelive.world.content.combat.magic.Autocasting
import com.runelive.world.content.combat.prayer.CurseHandler
import com.runelive.world.content.combat.prayer.PrayerHandler
import com.runelive.world.content.skill.impl.dungeoneering.Dungeoneering
import com.runelive.world.content.transportation.TeleportHandler
import com.runelive.world.entity.impl.player.Player

object YouTubers {

    /**
     * @Author Jonathan Sirens Initiates Command
     */

    @JvmStatic fun initiate_command(player: Player, command: Array<String>, wholeCommand: String) {
        if (command[0] == "bank") {
            if (player.location === Location.WILDERNESS) {
                player.packetSender.sendMessage("You can't do this inside the wilderness!")
                return
            }
            if (player.combatBuilder.isBeingAttacked || player.combatBuilder.isAttacking) {
                player.packetSender.sendMessage("You cannot use this command while in combat!")
                return
            }
            player.getBank(player.currentBankTab).open()
            player.packetSender.sendMessage("You have opened your bank!")
        }
        if (player.isJailed) {
            player.packetSender.sendMessage("You cannot use commands in jail... You're in jail.")
            return
        }
        if (wholeCommand.toLowerCase().startsWith("yell")) {
            if (World.isGlobalYell() == false) {
                player.packetSender.sendMessage("An admin has temporarily disabled the global yell channel.")
                return
            }
            if (PlayerPunishment.isMuted(player.username) || PlayerPunishment.isIpMuted(player.hostAddress)) {
                player.packetSender.sendMessage("You are muted and cannot yell.")
                return
            }
            if (player.isYellMute) {
                player.packetSender.sendMessage("You are muted from yelling and cannot yell.")
                return
            }
            if (!GameSettings.YELL_STATUS) {
                player.packetSender.sendMessage("Yell is currently turned off, please try again in 30 minutes!")
                return
            }
            val yellmessage = wholeCommand.substring(4, wholeCommand.length)
            if (yellmessage.contains("<img=") || yellmessage.contains("<col=") || yellmessage.contains("<shad=")) {
                player.packetSender.sendMessage("You are not allowed to put these symbols in your yell message.")
                return
            }
            if (player.gameMode == GameMode.IRONMAN) {
                World.sendYell("<img=12> [<col=808080><shad=0>Ironman</col></shad>] " + player.username + ": "
                        + yellmessage, player)
                return
            }
            if (player.gameMode == GameMode.HARDCORE_IRONMAN) {
                World.sendYell("<img=13> [<col=ffffff><shad=0>Hardcore</col></shad>] " + player.username + ": "
                        + yellmessage, player)
                return
            }
            if (player.yellTag != "invalid_yell_tag_set") {
                World.sendYell("<img=11> <col=0>[<col=ffff00><shad=0>" + player.yellTag + "<col=0></shad>] "
                        + player.username + ": " + yellmessage, player)
                player.yellTimer.reset()
                return
            }
            World.sendYell("<img=5> <col=0>[<col=ff0000><shad=620000>YouTuber</shad><col=0>] " + player.username
                    + ": " + yellmessage, player)
        }
        if (command[0] == "ezone") {
            if (Dungeoneering.doingDungeoneering(player)) {
                player.packetSender.sendMessage("You can't use this command in a dungeon.")
                return
            }
            if (player.location != null && player.wildernessLevel > 20) {
                player.packetSender.sendMessage("You cannot do this at the moment.")
                return
            }
            var position = Position(3362, 9640, 0)
            val ran = Misc.getRandom(3)
            when (ran) {
                0 -> position = Position(3363, 9641, 0)
                1 -> position = Position(3364, 9640, 0)
                2 -> position = Position(3363, 9639, 0)
                3 -> position = Position(3362, 9640, 0)
            }
            TeleportHandler.teleportPlayer(player, position, player.spellbook.teleportType)
            player.packetSender.sendMessage("<img=9><col=00ff00><shad=0> Welcome to the Extreme Donator Zone!")
        }
        if (command[0] == "dzone") {
            val position = Position(2514, 3860, 0)
            TeleportHandler.teleportPlayer(player, position, player.spellbook.teleportType)
            player.packetSender.sendMessage("[<col=ff0000>Donator Zone</col>] Welcome to the donator zone.")
        }
        if (command[0] == "ancients") {
            if (player.location != null && player.location === Location.WILDERNESS) {
                player.packetSender.sendMessage("You cannot do this at the moment.")
                return
            }
            player.spellbook = MagicSpellbook.ANCIENT
            player.packetSender.sendTabInterface(GameSettings.MAGIC_TAB, player.spellbook.interfaceId).sendMessage("Your magic spellbook is changed to ancients..")
            Autocasting.resetAutocast(player, true)
        }
        if (command[0] == "togglepray") {
            if (player.skillManager.getMaxLevel(Skill.DEFENCE) < 30) {
                player.packetSender.sendMessage("You need a Defence level of at least 30 to use this altar.")
                return
            }
            if (player.prayerbook == Prayerbook.NORMAL) {
                player.packetSender.sendMessage("You sense a surge of power flow through your body!")
                player.prayerbook = Prayerbook.CURSES
            } else {
                player.packetSender.sendMessage("You sense a surge of purity flow through your body!")
                player.prayerbook = Prayerbook.NORMAL
            }
            player.packetSender.sendTabInterface(GameSettings.PRAYER_TAB, player.prayerbook.interfaceId)
            PrayerHandler.deactivateAll(player)
            CurseHandler.deactivateAll(player)
        }
        if (command[0] == "moderns") {
            if (player.location != null && player.location === Location.WILDERNESS) {
                player.packetSender.sendMessage("You cannot do this at the moment.")
                return
            }
            player.spellbook = MagicSpellbook.NORMAL
            player.packetSender.sendTabInterface(GameSettings.MAGIC_TAB, player.spellbook.interfaceId).sendMessage("Your magic spellbook is changed to moderns..")
            Autocasting.resetAutocast(player, true)
        }
        if (command[0] == "lunars") {
            if (player.location != null && player.location === Location.WILDERNESS) {
                player.packetSender.sendMessage("You cannot do this at the moment.")
                return
            }
            player.spellbook = MagicSpellbook.LUNAR
            player.packetSender.sendTabInterface(GameSettings.MAGIC_TAB, player.spellbook.interfaceId).sendMessage("Your magic spellbook is changed to lunars..")
            Autocasting.resetAutocast(player, true)
        }
    }

}