package com.runelive.world.content.skill.impl.summoning;

import com.runelive.model.Skill;
import com.runelive.model.definitions.ItemDefinition;
import com.runelive.world.entity.impl.player.Player;

/**
 * Charming imp
 *
 * @author Kova+ Redone by Gabbe
 */
public class CharmingImp {

	public static final int GREEN_CHARM = 12159;
	public static final int GOLD_CHARM = 12158;
	public static final int CRIM_CHARM = 12160;
	public static final int BLUE_CHARM = 12163;

	public static void changeConfig(Player player, int index, int config) {
		/*
		 * player.getSummoning().setCharmImpConfig(index, config);
		 * player.getPacketSender() .sendInterfaceRemoval() .sendMessage(
		 * "<img=5> <col=996633>Your configuration for " +
		 * ItemDefinition.forId(getCharmForIndex(index)) .getName() +
		 * "s has been saved.");
		 */
		player.getPacketSender().sendMessage("You can't change this, this option is useless now!");
	}

	public static boolean handleCharmDrop(Player player, int itemId, int amount) {
		int index = getIndexForCharm(itemId);
		if (index == -1) {
			return false;
		}
		switch (player.getSummoning().getCharmImpConfig(index)) {
		case 0:
			turnIntoXp(player, itemId, amount);
			sendToInvo(player, itemId, amount);
			return true;
		case 1:
			turnIntoXp(player, itemId, amount);
			sendToInvo(player, itemId, amount);
			return true;
		}
		return false;
	}

	private static boolean sendToInvo(Player player, int itemId, int amount) {
		if (!player.getInventory().contains(itemId) && player.getInventory().getFreeSlots() == 0) {
			player.getPacketSender()
					.sendMessage("Your inventory is full, the Charming imp is unable to pick up any charms!");
			return false;
		}
		sendMessage(player, 0, itemId, amount);
		player.getInventory().add(itemId, amount);
		return true;
	}

	private static void turnIntoXp(Player player, int itemId, int amount) {
		switch (itemId) {
		case GOLD_CHARM:
			player.getSkillManager().addExactExperience(Skill.SUMMONING, 438 * amount);
			break;
		case GREEN_CHARM:
			player.getSkillManager().addExactExperience(Skill.SUMMONING, 536 * amount);
			break;
		case CRIM_CHARM:
			player.getSkillManager().addExactExperience(Skill.SUMMONING, 721 * amount);
			break;
		case BLUE_CHARM:
			player.getSkillManager().addExactExperience(Skill.SUMMONING, 913 * amount);
			break;
		}
		sendMessage(player, 1, itemId, amount);
	}

	private static void sendMessage(Player player, int config, int itemId, int amount) {
		String itemName = ItemDefinition.forId(itemId).getName();
		if (amount > 1) {
			itemName += "s";
		}
		switch (config) {
		case 0:
			// player.getPacketSender().sendMessage("Your charming imp loots
			// your charms and grants you
			// some experience too!");
			// player.getPacketSender().sendMessage("Your Charming imp has found
			// <col=ff0000>" + amount+
			// "</col> " + itemName + " and placed it in your inventory.");
			break;
		case 1:
			// player.getPacketSender().sendMessage("Your charming imp loots
			// your charms and grants you
			// some experience too!");
			// player.getPacketSender().sendMessage("Your Charming imp has found
			// <col=ff0000>" + amount
			// + "</col> " + itemName + " and turned it into Summoning exp.");
			break;
		}
	}

	public static void sendConfig(Player player) {
		for (int i = 0; i < 4; i++) {
			int state = player.getSummoning().getCharmImpConfig(i);
			int charm = getCharmForIndex(i);
			switch (state) {
			case 0:
				player.getPacketSender().sendMessage("<img=5> <col=996633>Your Charming imp is placing all "
						+ ItemDefinition.forId(charm).getName() + "s it finds in your inventory.");
				break;
			case 1:
				player.getPacketSender().sendMessage("<img=5> <col=996633>Your Charming imp is turning all "
						+ ItemDefinition.forId(charm).getName() + "s it finds into Summoning exp.");
				break;
			}
		}
	}

	private static int getIndexForCharm(int charm) {
		switch (charm) {
		case GOLD_CHARM:
			return 0;
		case GREEN_CHARM:
			return 1;
		case CRIM_CHARM:
			return 2;
		case BLUE_CHARM:
			return 3;
		}
		return -1;
	}

	private static int getCharmForIndex(int index) {
		switch (index) {
		case 0:
			return GOLD_CHARM;
		case 1:
			return GREEN_CHARM;
		case 2:
			return CRIM_CHARM;
		case 3:
			return BLUE_CHARM;
		}
		return -1;
	}
}
