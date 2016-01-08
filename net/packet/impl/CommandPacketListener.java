package com.ikov.net.packet.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ikov.GameServer;
import com.ikov.GameServer;
import com.ikov.GameSettings;
import com.ikov.engine.task.Task;
import com.ikov.model.input.impl.ChangePassword;
import com.ikov.engine.task.TaskManager;
import com.ikov.commands.Commands;
import com.ikov.model.Animation;
import com.ikov.model.Flag;
import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import com.ikov.model.GameObject;
import com.ikov.model.Graphic;
import com.ikov.model.GroundItem;
import com.ikov.model.Item;
import com.ikov.model.Locations.Location;
import com.ikov.model.PlayerRights;
import com.ikov.model.Position;
import com.ikov.model.Skill;
import com.ikov.world.content.minigames.impl.Zulrah;
import com.ikov.model.container.impl.Bank;
import com.ikov.model.container.impl.Equipment;
import com.ikov.model.container.impl.Shop.ShopManager;
import com.ikov.model.definitions.ItemDefinition;
import com.ikov.model.definitions.WeaponAnimations;
import com.ikov.model.definitions.WeaponInterfaces;
import com.ikov.net.packet.Packet;
import com.ikov.net.packet.PacketListener;
import com.ikov.net.security.ConnectionHandler;
import com.ikov.util.Auth;
import com.ikov.util.Misc;
import com.ikov.world.World;
import com.ikov.world.content.BonusManager;
import com.ikov.world.content.BossSystem;
import com.ikov.world.content.MoneyPouch;
import com.ikov.world.content.WellOfGoodwill;
import com.ikov.world.content.Lottery;
import com.ikov.world.content.PlayerLogs;
import com.ikov.world.content.PlayerPunishment;
import com.ikov.world.content.PlayersOnlineInterface;
import com.ikov.world.content.ShootingStar;
import com.ikov.world.content.clan.ClanChatManager;
import com.ikov.world.content.combat.CombatFactory;
import com.ikov.world.content.combat.DesolaceFormulas;
import com.ikov.world.content.combat.weapon.CombatSpecial;
import com.ikov.world.content.dialogue.DialogueManager;
import com.ikov.world.content.grandexchange.GrandExchange;
import com.ikov.world.content.grandexchange.GrandExchangeOffer;
import com.ikov.world.content.grandexchange.GrandExchangeOffers;
import com.ikov.world.content.minigames.impl.WarriorsGuild;
import com.ikov.world.content.skill.SkillManager;
import com.ikov.world.content.skill.impl.slayer.SlayerTasks;
import com.ikov.world.content.transportation.TeleportHandler;
import com.ikov.world.content.transportation.TeleportType;
import com.ikov.world.entity.impl.GroundItemManager;
import com.ikov.world.entity.impl.npc.NPC;
import com.ikov.world.entity.impl.player.Player;
import com.ikov.world.entity.impl.player.PlayerSaving;
import com.ikov.world.clip.stream.ByteStreamExt;
import com.ikov.world.clip.stream.MemoryArchive;
import com.ikov.world.content.skill.impl.dungeoneering.Dungeoneering;

/**
 * This packet listener manages commands a player uses by using the
 * command console prompted by using the "`" char.
 * 
 * @author Gabriel Hannason
 */

public class CommandPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		String command = Misc.readString(packet.getBuffer());
		String[] parts = command.toLowerCase().split(" ");
		if(command.contains("\r") || command.contains("\n")) {
			return;
		}
		try {
			Commands.initiate_commands(player, parts, command);
		} catch (Exception exception) {
			player.getPacketSender().sendMessage("Error executing that command.");
		}
	}
}

