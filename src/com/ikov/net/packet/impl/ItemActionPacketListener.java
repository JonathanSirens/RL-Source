package com.ikov.net.packet.impl;

import com.ikov.model.GameMode;
import java.text.DecimalFormat;
import com.ikov.model.GameObject;
import com.ikov.model.Item;
import com.ikov.model.Position;
import com.ikov.model.PlayerRights;
import com.ikov.net.packet.Packet;
import com.ikov.commands.ranks.SpecialPlayers;
import com.ikov.net.packet.PacketListener;
import com.ikov.util.Misc;
import com.ikov.world.content.Consumables;
import com.ikov.model.input.impl.EnterAmountToDice;
import com.ikov.model.Skill;
import com.ikov.world.content.Digging;
import com.ikov.world.content.Effigies;
import com.ikov.world.content.ExperienceLamps;
import com.ikov.model.VoteTokens;
import com.ikov.world.content.Gambling;
import com.ikov.world.content.MemberScrolls;
import com.ikov.world.content.MoneyPouch;
import com.ikov.world.content.combat.range.DwarfMultiCannon;
import com.ikov.world.content.dialogue.DialogueManager;
import com.ikov.world.content.skill.impl.construction.Construction;
import com.ikov.world.content.skill.impl.dungeoneering.ItemBinding;
import com.ikov.world.content.skill.impl.herblore.Herblore;
import com.ikov.world.content.skill.impl.herblore.IngridientsBook;
import com.ikov.world.content.skill.impl.hunter.BoxTrap;
import com.ikov.world.content.skill.impl.hunter.Hunter;
import com.ikov.world.content.skill.impl.hunter.JarData;
import com.ikov.world.content.skill.impl.hunter.PuroPuro;
import com.ikov.world.content.skill.impl.hunter.SnareTrap;
import com.ikov.world.content.skill.impl.hunter.Trap.TrapState;
import com.ikov.world.content.skill.impl.prayer.Prayer;
import com.ikov.world.content.skill.impl.runecrafting.Runecrafting;
import com.ikov.world.content.skill.impl.runecrafting.RunecraftingPouches;
import com.ikov.world.content.skill.impl.runecrafting.RunecraftingPouches.RunecraftingPouch;
import com.ikov.world.content.skill.impl.slayer.SlayerDialogues;
import com.ikov.world.content.skill.impl.slayer.SlayerTasks;
import com.ikov.world.content.skill.impl.summoning.CharmingImp;
import com.ikov.world.content.skill.impl.summoning.SummoningData;
import com.ikov.world.content.skill.impl.woodcutting.BirdNests;
import com.ikov.world.content.transportation.JewelryTeleporting;
import com.ikov.world.content.transportation.TeleportHandler;
import com.ikov.world.content.transportation.TeleportType;
import com.ikov.world.entity.impl.player.Player;

import com.ikov.GameSettings;
import com.ikov.world.content.PlayerLogs;



public class ItemActionPacketListener implements PacketListener {

	private static void firstAction(final Player player, Packet packet) {
		int interfaceId = packet.readUnsignedShort();
		int slot = packet.readShort();
		int itemId = packet.readShort();
		if(GameSettings.DEBUG_MODE) {
			PlayerLogs.log(player.getUsername(), ""+player.getUsername()+" in ItemActionPacketListener: "+itemId+"");
		}
		if(interfaceId == 38274) {
			Construction.handleItemClick(itemId, player);
			return;
		}
		if(slot < 0 || slot > player.getInventory().capacity())
			return;
		if(player.getInventory().getItems()[slot].getId() != itemId)
			return;
		player.setInteractingItem(player.getInventory().getItems()[slot]);
		if (Prayer.isBone(itemId)) {
			Prayer.buryBone(player, itemId);
			return;
		}
		if(!player.getDragonSpear().elapsed(3000)) {
			player.getPacketSender().sendMessage("You can't do that, you're stunned!");
			return;
		}
		if (Consumables.isFood(player, itemId, slot))
			return;
		if(Consumables.isPotion(itemId)) {
			Consumables.handlePotion(player, itemId, slot);
			return;
		}
		if(BirdNests.isNest(itemId)) {
			BirdNests.searchNest(player, itemId);
			return;
		}
		if (Herblore.cleanHerb(player, itemId))
			return;
//		if(MemberScrolls.handleScroll(player, itemId))
//			return;
		if(Effigies.isEffigy(itemId)) {
			Effigies.handleEffigy(player, itemId);
			return;
		}
		if(ExperienceLamps.handleLamp(player, itemId)) {
			return;
		}
		switch(itemId) {
		case 10943:
			DialogueManager.start(player, 203);
			player.setDialogueActionId(203);
			break;
		case 10934:
			DialogueManager.start(player, 204);
			player.setDialogueActionId(204);
			break;
		case 10935:
			DialogueManager.start(player, 205);
			player.setDialogueActionId(205);
			break;
		case 7629:
			DialogueManager.start(player, 206);
			player.setDialogueActionId(206);
			break;
		case 739:
			if(player.getInventory().contains(739)) {
				Consumables.handlePotion(player, 739, slot);
			}
			break;
		case 6040:
			Position location = new Position(2036, 4535, 0);
			TeleportHandler.teleportPlayer(player, location, TeleportType.PURO_PURO);
			break;
		case 11211:
			boolean continue_command2 = false;
			for(int i = 0; i < SpecialPlayers.player_names.length; i++) {
				if(SpecialPlayers.player_names[i].toLowerCase().equals(player.getUsername().toLowerCase())) {
					continue_command2 = true;
				}
			}
			if(!continue_command2 && player.getRights() != PlayerRights.OWNER && player.getRights() != PlayerRights.ADMINISTRATOR && player.getRights() != PlayerRights.COMMUNITY_MANAGER) {
				return;
			}
			player.setInputHandling(new EnterAmountToDice(1, 1));
			player.getPacketSender().sendEnterAmountPrompt("What would you like to roll?");
		break;
		case 4490:
			boolean continue_command1 = false;
			for(int i = 0; i < SpecialPlayers.player_names.length; i++) {
				if(SpecialPlayers.player_names[i].toLowerCase().equals(player.getUsername().toLowerCase())) {
					continue_command1 = true;
				}
			}
			if(!continue_command1 && player.getRights() != PlayerRights.OWNER && player.getRights() != PlayerRights.ADMINISTRATOR && player.getRights() != PlayerRights.COMMUNITY_MANAGER) {
				return;
			}
			player.setDialogueActionId(137);
			DialogueManager.start(player, 137);
		break;
		case 4142:
			boolean continue_command = false;
			for(int i = 0; i < SpecialPlayers.player_names.length; i++) {
				if(SpecialPlayers.player_names[i].toLowerCase().equals(player.getUsername().toLowerCase())) {
					continue_command = true;
				}
			}
			if(!continue_command && player.getRights() != PlayerRights.OWNER && player.getRights() != PlayerRights.ADMINISTRATOR && player.getRights() != PlayerRights.COMMUNITY_MANAGER) {
				return;
			}
			if(!player.boost_stats) {
				player.getPacketSender().sendMessage("<col=ff0000><shad=0>You have boosted to 1,000 defence.");
				player.getSkillManager().setCurrentLevel(Skill.DEFENCE, 1000, true);
				player.boost_stats = true;
			} else {
				player.getPacketSender().sendMessage("<col=ff0000><shad=0>You have restored your defence.");
				player.boost_stats = false;
				player.getSkillManager().setCurrentLevel(Skill.DEFENCE, 99, true);
			}
		break;
		case 10944:
			if(player.getInventory().isFull()) {
				player.getPacketSender().sendMessage("You need to have atleast 1 free inventory space.");
				return;
			}
			VoteTokens.open_token(player, 10944);
		break;
		case 13663:
			if(player.getInterfaceId() > 0) {
				player.getPacketSender().sendMessage("Please close the interface you have open before doing this.");
				return;
			}
			player.setUsableObject(new Object[2]).setUsableObject(0, "reset");
			player.getPacketSender().sendString(38006, "Choose stat to reset!").sendMessage("@red@Please select a skill you wish to reset and then click on the 'Confim' button.").sendString(38090, "Which skill would you like to reset?");
			player.getPacketSender().sendInterface(38000);
			break;
		case 19670:
			if(player.busy()) {
				player.getPacketSender().sendMessage("You can not do this right now.");
				return;
			}
			player.setDialogueActionId(70);
			DialogueManager.start(player, player.getGameMode() == GameMode.NORMAL ? 108 : 109);
			break;
		case 7956:
			player.getInventory().delete(7956, 1);
			int[] rewards = 		{200, 202, 204, 206, 208, 210, 212, 214, 216, 218, 220, 2486, 3052, 1624, 1622, 1620, 1618, 1632, 1516, 1514, 454, 448, 450, 452, 378, 372, 7945, 384, 390, 15271, 533, 535, 537, 18831, 556, 558, 555, 554, 557, 559, 564, 562, 566, 9075, 563, 561, 560, 565, 888, 890, 892, 11212, 9142, 9143, 9144, 9341, 9244, 866, 867, 868,  2, 10589, 10564, 6809, 4131, 15126, 4153, 1704, 1149};
			int[] rewardsAmount = 	{50, 50, 50, 30, 20,  30,  30,  30,  30,  20,  10,   5,   4,  70,  40,  25,  10,   10,  100,  50,  100,  80, 25, 25,250, 200,  125, 50, 30,    25, 50, 20,  20,     5,500,500,500,500,500,500,500,500, 200,  200, 200, 200, 200, 200,1000,750, 200,   100, 1200, 1200,  120,  50,   20, 1000,500,100,100,     1,     1,    1,    1,     1,    1,    1,    1};
			int rewardPos = Misc.getRandom(rewards.length-1);
			player.getInventory().add(rewards[rewardPos], (int)((rewardsAmount[rewardPos]*0.5) + (Misc.getRandom(rewardsAmount[rewardPos]))));
			break;
		case 15387:
			player.getInventory().delete(15387, 1);
			rewards = new int[] {1377, 1149, 7158, 3000, 219, 5016, 6293, 6889, 2205, 3051, 269, 329, 3779, 6371, 2442, 347, 247};
			player.getInventory().add(rewards[Misc.getRandom(rewards.length-1)], 1);
			break;
		case 407:
			player.getInventory().delete(407, 1);
			if (Misc.getRandom(3) < 3) {
				player.getInventory().add(409, 1);
			} else if(Misc.getRandom(4) < 4) {
				player.getInventory().add(411, 1);
			} else 
				player.getInventory().add(413, 1);
			break;
		case 405:
			player.getInventory().delete(405, 1);
			if (Misc.getRandom(1) < 1) {
				int coins = Misc.getRandom(30000);
				player.getInventory().add(995, coins);
				player.getPacketSender().sendMessage("The casket contained "+coins+" coins!");
			} else
				player.getPacketSender().sendMessage("The casket was empty.");
			break;
		case 15084:
			if(player.getClanChatName() == null) {
				player.getPacketSender().sendMessage("You need to be in a clanchat channel to roll a dice.");
				return;
			}
			Gambling.rollDice(player);
			break;
		case 299:
			Gambling.plantSeed(player);
			break;
		case 15104:
			player.getPacketSender().sendMessage("Combine this with the three other missing parts...");
			return;
		case 15103:
			player.getPacketSender().sendMessage("Combine this with the three other missing parts...");
			return;
		case 15105:
			player.getPacketSender().sendMessage("Combine this with the three other missing parts...");
			return;
		case 15106:
			player.getPacketSender().sendMessage("Combine this with the three other missing parts...");
			return;
		case 4155:
			if(player.getSlayer().getSlayerTask() == SlayerTasks.NO_TASK) {
				player.getPacketSender().sendInterfaceRemoval();
				player.getPacketSender().sendMessage("Your Enchanted gem will only work if you have a Slayer task.");
				return;
			}
			DialogueManager.start(player, SlayerDialogues.dialogue(player));
			break;
		case 11858:
		case 11860:
		case 11862:
		case 11848:
		case 11856:
		case 11850:
		case 11854:
		case 11852:
		case 11846:
			if(!player.getClickDelay().elapsed(2000) || !player.getInventory().contains(itemId))
				return;
			if(player.busy()) {
				player.getPacketSender().sendMessage("You cannot open this right now.");
				return;
			}

			int[] items = itemId == 11858 ? new int[] {10350, 10348, 10346, 10352} : 
				itemId == 11860 ? new int[]{10334, 10330, 10332, 10336} : 
					itemId == 11862 ? new int[]{10342, 10338, 10340, 10344} : 
						itemId == 11848 ? new int[]{4716, 4720, 4722, 4718} : 
							itemId == 11856 ? new int[]{4753, 4757, 4759, 4755} : 
								itemId == 11850 ? new int[]{4724, 4728, 4730, 4726} : 
									itemId == 11854 ? new int[]{4745, 4749, 4751, 4747} : 
										itemId == 11852 ? new int[]{4732, 4734, 4736, 4738} : 
											itemId == 11846 ? new int[]{4708, 4712, 4714, 4710} :
												new int[]{itemId};

											if(player.getInventory().getFreeSlots() < items.length) {
												player.getPacketSender().sendMessage("You do not have enough space in your inventory.");
												return;
											}
											player.getInventory().delete(itemId, 1);
											for(int i : items) {
												player.getInventory().add(i, 1);
											}
											player.getPacketSender().sendMessage("You open the set and find items inside.");
											player.getClickDelay().reset();
											break;
		case 952:
			Digging.dig(player);
			break;
		case 10006:
			// Hunter.getInstance().laySnare(client);
			Hunter.layTrap(player, new SnareTrap(new GameObject(19175, new Position(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ())), TrapState.SET, 200, player));
			break;
		case 10008:			
			Hunter.layTrap(player, new BoxTrap(new GameObject(19187, new Position(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ())), TrapState.SET, 200, player));
			break;
		case 5509:
		case 5510:
		case 5512:
			RunecraftingPouches.fill(player, RunecraftingPouch.forId(itemId));
			break;
		case 292:
			IngridientsBook.readBook(player, 0, false);
			break;
		case 6199:
			int rewards2[][] = {
					{15501, 15272, 2503, 10499, 6326, 861, 1163, 1201, 6111, 544, 542, 5574, 5575, 5576, 1215, 3105, 13734, 7400, 2572, 11118}, //Common, 0
					{15501, 11133, 15126, 10828, 3751, 3753, 10589, 10564, 6809, 4587, 1249, 3204, 1305, 1377, 1434, 6528, 7158, 4153, 6, 8, 10, 12, 4675, 6914, 6889}, //Uncommon, 1
					{6739, 15259, 15332, 2579, 6920, 6922, 13879, 13883, 15241, 15243} //Rare, 2
			};
			double numGen = Math.random();
			/** Chances
			 *  50% chance of Common Items - cheap gear, high-end consumables
			 *  40% chance of Uncommon Items - various high-end coin-bought gear
			 *  10% chance of Rare Items - Highest-end coin-bought gear, some voting-point/pk-point equipment
			 */
			int rewardGrade = numGen >= 0.5 ? 0 : numGen >= 0.20 ? 1 : 2;
			rewardPos = Misc.getRandom(rewards2[rewardGrade].length-1);
			player.getInventory().delete(6199, 1);
			player.getInventory().add(rewards2[rewardGrade][rewardPos], 1).refreshItems();
			break;
		case 15501:
			int superiorRewards[][] = {
					{11133, 15126, 10828, 3751, 3753, 10589, 10564, 6809, 4587, 1249, 3204, 1305, 1377, 1434, 6528, 7158, 4153, 6, 8, 10, 12, 4675, 6914, 6889}, //Uncommon, 0
					{6739, 15259, 15332, 2579, 6920, 6922, 15241, 11882, 11884, 11906, 20084}, //Rare, 1
					{6570, 15018, 15019, 15020, 15220, 11730, 18349, 18353, 13896, 18357, 13899, 10551, 4151, 2577, }, //Epic, 2
					{11235, 17273, 14484, 11696, 11698, 11700, 13262, 15486, 19336, 19337, 19338, 19339, 19340, 
					14009, 14010, 14008, 14011, 14012, 14013, 14014, 14015, 14016} //Legendary, 3
			};
			double superiorNumGen = Math.random();
			/** Chances
			 *  54% chance of Uncommon Items - various high-end coin-bought gear
			 *  30% chance of Rare Items - Highest-end coin-bought gear, Some poor voting-point/pk-point equipment
			 *  11% chance of Epic Items -Better voting-point/pk-point equipment
			 *  5% chance of Legendary Items - Only top-notch voting-point/pk-point equipment
			 */
			int superiorRewardGrade = superiorNumGen >= 0.46 ? 0 : superiorNumGen >= 0.16 ? 1 : superiorNumGen >= 0.05? 2 : 3;
			int superiorRewardPos = Misc.getRandom(superiorRewards[superiorRewardGrade].length-1);
			player.getInventory().delete(15501, 1);
			player.getInventory().add(superiorRewards[superiorRewardGrade][superiorRewardPos], 1).refreshItems();
			break;
		case 11882:
			player.getInventory().delete(11882, 1);
			player.getInventory().add(2595, 1).refreshItems();
			player.getInventory().add(2591, 1).refreshItems();
			player.getInventory().add(3473, 1).refreshItems();
			player.getInventory().add(2597, 1).refreshItems();
			break;
		case 11884:
			player.getInventory().delete(11884, 1);
			player.getInventory().add(2595, 1).refreshItems();
			player.getInventory().add(2591, 1).refreshItems();
			player.getInventory().add(2593, 1).refreshItems();
			player.getInventory().add(2597, 1).refreshItems();
			break;
		case 11906:
			player.getInventory().delete(11906, 1);
			player.getInventory().add(7394, 1).refreshItems();
			player.getInventory().add(7390, 1).refreshItems();
			player.getInventory().add(7386, 1).refreshItems();
			break;
		case 15262:
			if(!player.getClickDelay().elapsed(1000))
				return;
			player.getInventory().delete(15262, 1);
			player.getInventory().add(18016, 10000).refreshItems();
			player.getClickDelay().reset();
			break;
		case 6:
			DwarfMultiCannon.setupCannon(player);
			break;
		}
	}

	public static void secondAction(Player player, Packet packet) {
		int interfaceId = packet.readLEShortA();
		int slot = packet.readLEShort();
		int itemId = packet.readShortA();
		if(slot < 0 || slot > player.getInventory().capacity())
			return;
		if(player.getInventory().getItems()[slot].getId() != itemId)
			return;
		if (SummoningData.isPouch(player, itemId, 2))
			return;
		switch(itemId) {
		case 6500:
			if(player.getCombatBuilder().isAttacking() || player.getCombatBuilder().isBeingAttacked()) {
				player.getPacketSender().sendMessage("You cannot configure this right now.");
				return;
			}
			player.getPacketSender().sendInterfaceRemoval();
			DialogueManager.start(player, 101);
			player.setDialogueActionId(60);
			break;
		case 1712:
		case 1710:
		case 1708:
		case 1706:
		case 11118:
		case 11120:
		case 11122:
		case 11124:
			JewelryTeleporting.rub(player, itemId);
			break;
		case 1704:
			player.getPacketSender().sendMessage("Your amulet has run out of charges.");
			break;
		case 11126:
			player.getPacketSender().sendMessage("Your bracelet has run out of charges.");
			break;
		case 13281:
		case 13282:
		case 13283:
		case 13284:
		case 13285:
		case 13286:
		case 13287:
		case 13288:
			player.getSlayer().handleSlayerRingTP(itemId);
			break;
		case 5509:
		case 5510:
		case 5512:
			RunecraftingPouches.check(player, RunecraftingPouch.forId(itemId));
			break;
		case 995:
			MoneyPouch.depositMoney(player, player.getInventory().getAmount(995));
			break;
		case 1438:
		case 1448:
		case 1440:
		case 1442:
		case 1444:
		case 1446:
		case 1454:
		case 1452:
		case 1462:
		case 1458:
		case 1456:
		case 1450:
			Runecrafting.handleTalisman(player, itemId);
			break;
		}
	}

	public void thirdClickAction(Player player, Packet packet) {
		int itemId = packet.readShortA();
		int slot = packet.readLEShortA();
		int interfaceId = packet.readLEShortA();
		if(slot < 0 || slot > player.getInventory().capacity())
			return;
		if(player.getInventory().getItems()[slot].getId() != itemId)
			return;
		if(JarData.forJar(itemId) != null) {
			PuroPuro.lootJar(player, new Item(itemId, 1), JarData.forJar(itemId));
			return;
		}
		if (SummoningData.isPouch(player, itemId, 3)) {
			return;
		}
		if(ItemBinding.isBindable(itemId)) {
			ItemBinding.bindItem(player, itemId);
			return;
		}
		switch(itemId) {
		case 19670:
			if(player.busy()) {
				player.getPacketSender().sendMessage("You can not do this right now.");
				return;
			}
			player.setDialogueActionId(71);
			DialogueManager.start(player, player.getGameMode() == GameMode.NORMAL ? 108 : 109);
			break;
		case 6500:
			CharmingImp.sendConfig(player);
			break;
		case 4155:
			player.getPacketSender().sendInterfaceRemoval();
			DialogueManager.start(player, 103);
			player.setDialogueActionId(65);
			break;
		case 13281:
		case 13282:
		case 13283:
		case 13284:
		case 13285:
		case 13286:
		case 13287:
		case 13288:
			player.getPacketSender().sendInterfaceRemoval();
			player.getPacketSender().sendMessage(player.getSlayer().getSlayerTask() == SlayerTasks.NO_TASK ? ("You do not have a Slayer task.") : ("Your current task is to kill another "+(player.getSlayer().getAmountToSlay())+" "+Misc.formatText(player.getSlayer().getSlayerTask().toString().toLowerCase().replaceAll("_", " "))+"s."));
			break;
		case 6570:
			if(player.getInventory().contains(6570) && player.getInventory().getAmount(6529) >= 50000) {
				player.getInventory().delete(6570, 1).delete(6529, 50000).add(19111, 1);
				player.getPacketSender().sendMessage("You have upgraded your Fire cape into a TokHaar-Kal cape!");
			} else {
				player.getPacketSender().sendMessage("You need at least 50.000 Tokkul to upgrade your Fire Cape into a TokHaar-Kal cape.");
			}
			break;
			case 21077:
				double charges = (double)player.getToxicStaffCharges() / 110;
				DecimalFormat double_decimal_format = new DecimalFormat("#.00");
				player.getPacketSender().sendMessage("Your Toxic staff of the dead has "+double_decimal_format.format(charges)+"% of it's charges left.");
			break;
		case 15262:
			if(!player.getClickDelay().elapsed(1300))
				return;
			int amt = player.getInventory().getAmount(15262);
			if(amt > 0)
				player.getInventory().delete(15262, amt).add(18016, 10000 * amt);
			player.getClickDelay().reset();
			break;
		case 5509:
		case 5510:
		case 5512:
			RunecraftingPouches.empty(player, RunecraftingPouch.forId(itemId));
			break;
		case 11283: //DFS
			player.getPacketSender().sendMessage("Your Dragonfire shield has "+player.getDfsCharges()+"/50 dragon-fire charges.");
			break;
		}
	}

	@Override
	public void handleMessage(Player player, Packet packet) {
		if (player.getConstitution() <= 0)
			return;
		switch (packet.getOpcode()) {
		case SECOND_ITEM_ACTION_OPCODE:
			secondAction(player, packet);
			break;
		case FIRST_ITEM_ACTION_OPCODE:
			firstAction(player, packet);
			break;
		case THIRD_ITEM_ACTION_OPCODE:
			thirdClickAction(player, packet);
			break;
		}
	}

	public static final int SECOND_ITEM_ACTION_OPCODE = 75;

	public static final int FIRST_ITEM_ACTION_OPCODE = 122;

	public static final int THIRD_ITEM_ACTION_OPCODE = 16;

}