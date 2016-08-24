package com.chaos.net.packet.impl;

import com.chaos.GameSettings;
import com.chaos.model.Gender;
import com.chaos.model.StaffRights;
import com.chaos.model.options.Option;
import com.chaos.model.Locations.Location;
import com.chaos.model.Position;
import com.chaos.model.container.impl.Bank;
import com.chaos.model.container.impl.Bank.BankSearchAttributes;
import com.chaos.model.definitions.WeaponInterfaces.WeaponInterface;
import com.chaos.model.input.impl.BuyDungExperience;
import com.chaos.model.input.impl.EnterClanChatToJoin;
import com.chaos.model.input.impl.EnterSyntaxToBankSearchFor;
import com.chaos.model.input.impl.InviteToDungeoneering;
import com.chaos.model.input.impl.PosItemSearch;
import com.chaos.net.packet.Packet;
import com.chaos.net.packet.PacketListener;
import com.chaos.world.World;
import com.chaos.world.content.Achievements;
import com.chaos.world.content.BankPin;
import com.chaos.world.content.BonusManager;
import com.chaos.world.content.CompletionistCapes;
import com.chaos.world.content.Consumables;
import com.chaos.world.content.DropLog;
import com.chaos.world.content.Emotes;
import com.chaos.world.content.EnergyHandler;
import com.chaos.world.content.ExperienceLamps;
import com.chaos.world.content.ItemsKeptOnDeath;
import com.chaos.world.content.KillsTracker;
import com.chaos.world.content.LoyaltyProgramme;
import com.chaos.world.content.MoneyPouch;
import com.chaos.world.content.PlayerPanel;
import com.chaos.world.content.PlayerPunishment;
import com.chaos.world.content.PlayersOnlineInterface;
import com.chaos.world.content.Sounds;
import com.chaos.world.content.Sounds.Sound;
import com.chaos.world.content.clan.ClanChat;
import com.chaos.world.content.clan.ClanChatManager;
import com.chaos.world.content.combat.magic.Autocasting;
import com.chaos.world.content.combat.magic.MagicSpells;
import com.chaos.world.content.combat.prayer.CurseHandler;
import com.chaos.world.content.combat.prayer.PrayerHandler;
import com.chaos.world.content.combat.weapon.CombatSpecial;
import com.chaos.world.content.combat.weapon.FightType;
import com.chaos.world.content.dialogue.DialogueManager;
import com.chaos.world.content.dialogue.DialogueOptions;
import com.chaos.world.content.grandexchange.GrandExchange;
import com.chaos.world.content.minigames.impl.ClawQuest;
import com.chaos.world.content.minigames.impl.Dueling;
import com.chaos.world.content.minigames.impl.FarmingQuest;
import com.chaos.world.content.minigames.impl.Nomad;
import com.chaos.world.content.minigames.impl.PestControl;
import com.chaos.world.content.minigames.impl.RecipeForDisaster;
import com.chaos.world.content.pos.PlayerOwnedShops;
import com.chaos.world.content.pos.PosDetails;
import com.chaos.world.content.skill.ChatboxInterfaceSkillAction;
import com.chaos.world.content.skill.Enchanting;
import com.chaos.world.content.skill.impl.construction.Construction;
import com.chaos.world.content.skill.impl.construction.sawmill.SawmillOperator;
import com.chaos.world.content.skill.impl.crafting.LeatherMaking;
import com.chaos.world.content.skill.impl.crafting.Tanning;
import com.chaos.world.content.skill.impl.dungeoneering.Dungeoneering;
import com.chaos.world.content.skill.impl.dungeoneering.DungeoneeringParty;
import com.chaos.world.content.skill.impl.dungeoneering.ItemBinding;
import com.chaos.world.content.skill.impl.fletching.Fletching;
import com.chaos.world.content.skill.impl.herblore.IngridientsBook;
import com.chaos.world.content.skill.impl.slayer.Slayer;
import com.chaos.world.content.skill.impl.smithing.SmithingData;
import com.chaos.world.content.skill.impl.summoning.PouchMaking;
import com.chaos.world.content.skill.impl.summoning.SummoningTab;
import com.chaos.world.content.transportation.TeleportHandler;
import com.chaos.world.entity.impl.player.Player;

/**
 * This packet listener manages a button that the player has clicked upon.
 * 
 * @author Gabriel Hannason
 */

public class ButtonClickPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {

		int id = packet.readShort();

		if (player.getStaffRights().isDeveloper(player))
			player.getPacketSender().sendMessage("Clicked button: " + id);

		if (checkOptionContainer(player, id)) {
			return;
		}

		if (checkHandlers(player, id))
			return;
		if (GameSettings.DEBUG_MODE) {
			// PlayerLogs.log(player, "" + player.getUsername()
			// + " has clicked button in ButtonClickPacketListener " + id + "");
		}


		if (Enchanting.enchantButtons(player, id)) {
			return;
		}

		if (id >= -24102 && id <= -23706) {
			PosDetails pd = PosItemSearch.forId(id, player);
			if (pd != null) {
				if (PlayerPunishment.isPlayerBanned(pd.getOwner())) {
					player.getPacketSender().sendMessage("This player is banned!");
				} else {
					player.getPacketSender().sendString(41900, "Back to Search Selection");
					PlayerOwnedShops.openShop(pd.getOwner(), player);
				}
			} else {
				player.getPacketSender().sendMessage("This result didn't return a offer!");
			}
		}

		switch (id) {
		case -10425:
			// player.setMusicActive(!player.musicActive());
			DialogueManager.sendStatement(player, "You can adjust the music volume in the settings tab.");
			player.setDialogueActionId(-1);
			player.getPacketSender().sendTab(GameSettings.OPTIONS_TAB);
			PlayerPanel.refreshPanel(player);
			break;
		case -10423:
			player.setTourneyToggle(!player.tourney_toggle);
			PlayerPanel.refreshPanel(player);
			break;
		case -24136:
			player.getDueling().setLastDuelRules(player);
			break;
		case -10424:
			// player.setSoundsActive(!player.soundsActive());
			DialogueManager.sendStatement(player, "You can adjust the sound volume in the settings tab.");
			player.setDialogueActionId(-1);
			player.getPacketSender().sendTab(GameSettings.OPTIONS_TAB);
			PlayerPanel.refreshPanel(player);
			break;
		case -26370:
			player.getPacketSender().sendMessage("Coming soon...");
			break;
		case -10415:
			DropLog.open(player);
			break;
		case -10417:
			KillsTracker.open(player);
			break;
		case -10465:
			player.getPacketSender().sendTabInterface(GameSettings.QUESTS_TAB, 55200);
			break;
		case -27454:
		case -27534:
		case 5384:
			player.getPacketSender().sendInterfaceRemoval();
			break;
		case -20152:
			for (int j = 0; j < player.getInventory().getItems().length; j++) {
				player.getTrading().tradeItem(player.getInventory().get(j).getId(),
						player.getInventory().get(j).getAmount(), j);
			}
			break;
		case 1036:
			EnergyHandler.rest(player);
			break;
		case -26376:
		case -10463:
			int tlNeeded = 149;
			if (player.getSkillManager().getTotalLevel() < tlNeeded) {
				player.getPacketSender().sendMessage(
						"You cannot view the players online until you have over " + (tlNeeded + 1) + " total level.");
			} else {
				PlayersOnlineInterface.showInterface(player);
			}
			break;
		case 27229:
			DungeoneeringParty.create(player);
			break;
		case 26226:
		case 26229:
			if (Dungeoneering.doingDungeoneering(player)) {
				DialogueManager.start(player, 114);
				player.setDialogueActionId(71);
			} else {
				Dungeoneering.leave(player, false, true);
			}
			break;
		case 26250:
			player.setInputHandling(new InviteToDungeoneering());
			player.getPacketSender().sendEnterInputPrompt("Enter the name of the player to invite:");
			break;
		case 26244:
		case 26247:
			if (player.getMinigameAttributes().getDungeoneeringAttributes().getParty() != null) {
				if (player.getMinigameAttributes().getDungeoneeringAttributes().getParty().getOwner().getUsername()
						.equals(player.getUsername())) {
					DialogueManager.start(player, id == 26247 ? 106 : 105);
					player.setDialogueActionId(id == 26247 ? 68 : 67);
				} else {
					player.getPacketSender().sendMessage("Only the party owner can change this setting.");
				}
			}
			break;
		case 28180:
			TeleportHandler.teleportPlayer(player, new Position(3450, 3715), player.getSpellbook().getTeleportType());
			break;
		case 14012:
			player.getPacketSender().sendInterfaceRemoval();
			break;
		case 14176:
			player.setUntradeableDropItem(null);
			player.getPacketSender().sendInterfaceRemoval();
			break;
		case 14175:
			player.getPacketSender().sendInterfaceRemoval();
			if (player.getUntradeableDropItem() != null
					&& player.getInventory().contains(player.getUntradeableDropItem().getId())) {
				ItemBinding.unbindItem(player, player.getUntradeableDropItem().getId());
				player.getInventory().delete(player.getUntradeableDropItem());
				player.getPacketSender().sendMessage("Your item vanishes as it hits the floor.");
				Sounds.sendSound(player, Sound.DROP_ITEM);
			}
			player.setUntradeableDropItem(null);
			break;
		case 1013:
			player.getSkillManager().setTotalGainedExp(0);
			break;
		case -10426:
			player.setYellToggle(!player.yell_toggle);
			PlayerPanel.refreshPanel(player);
			break;
		case -10416:
			if (player.getPlayerKillingAttributes().getTarget() != null) {
				int my_x = player.getPosition().getX();
				int other_x = player.getPlayerKillingAttributes().getTarget().getPosition().getX();
				int wild_lvl = player.getPlayerKillingAttributes().getTarget().getWildernessLevel();
				int steps_x = 0;
				String direction = "";
				boolean on_eachother = false;
				if (my_x > other_x) {
					steps_x = my_x - other_x;
					direction = "West";
				} else if (my_x == other_x) {
					on_eachother = true;
				} else {
					steps_x = other_x - my_x;
					direction = "East";
				}
				if (player.getPlayerKillingAttributes().getTarget().getLocation() == Location.WILDERNESS) {
					if (on_eachother) {
						player.getPacketSender().sendMessage(
								"Your target '" + player.getPlayerKillingAttributes().getTarget().getUsername()
										+ "' is directly on you in level " + wild_lvl + " wilderness.");
					} else {
						player.getPacketSender()
								.sendMessage("Your target '"
										+ player.getPlayerKillingAttributes().getTarget().getUsername() + "' is "
										+ steps_x + " steps " + direction + " in level " + wild_lvl + " wilderness.");
					}
				} else {
					player.getPacketSender().sendMessage("Your target is not in the wilderness!");
				}
			} else {
				player.getPacketSender().sendMessage("You currently do not have a target!");
			}
			break;
		case -10531:
			if (player.isKillsTrackerOpen()) {
				player.setKillsTrackerOpen(false);
				player.getPacketSender().sendTabInterface(GameSettings.QUESTS_TAB, 55065);
				PlayerPanel.refreshPanel(player);
			}
			break;
		case -10330:
			player.getPacketSender().sendTabInterface(GameSettings.QUESTS_TAB, 55065);
			break;
		case -10329:
			RecipeForDisaster.openQuestLog(player);
			break;
		case -10328:
			Nomad.openQuestLog(player);
			break;
		case -10327:
			ClawQuest.openQuestLog(player);
			break;
		case -23636:
			if (player.getShop() != null && player.getShop().id == 44 && player.isShopping()) {
				player.setInputHandling(new BuyDungExperience());
				player.getPacketSender()
						.sendEnterAmountPrompt("How much experience would you like to buy? 1 token = 3 exp");
			} else {
				if (player.getGameModeAssistant().isIronMan()) {
					player.getPacketSender().sendMessage("Ironmen can't use the player owned shops!");
					return;
				}
				PlayerOwnedShops.openItemSearch(player, false);
			}
			break;
		case -10326:
			FarmingQuest.openQuestLog(player);
			break;
		case 350:
			player.getPacketSender()
					.sendMessage("To autocast a spell, please right-click it and choose the autocast option.")
					.sendTab(GameSettings.MAGIC_TAB).sendConfig(108, player.getAutocastSpell() == null ? 3 : 1);
			break;
		case 29335:
			if (player.getInterfaceId() > 0) {
				player.getPacketSender()
						.sendMessage("Please close the interface you have open before opening another one.");
				return;
			}
			DialogueManager.start(player, 60);
			player.setDialogueActionId(27);
			break;
		case 29455:
			if (player.getInterfaceId() > 0) {
				player.getPacketSender()
						.sendMessage("Please close the interface you have open before opening another one.");
				return;
			}
			ClanChatManager.toggleLootShare(player);
			break;
		case 30000:
		case 1195:
			if (player.homeLocation == 0) {
				TeleportHandler.teleportPlayer(player, GameSettings.DEFAULT_POSITION_VARROCK.copy(),
						player.getSpellbook().getTeleportType());
			} else {
				TeleportHandler.teleportPlayer(player, GameSettings.DEFAULT_POSITION_EDGEVILLE.copy(),
						player.getSpellbook().getTeleportType());
			}
			break;
		case 1159: // Bones to Bananas
		case 15877:// Bones to peaches
		case 30306:
			MagicSpells.handleMagicSpells(player, id);
			break;
		case 10001:
			if (player.getInterfaceId() == -1) {
				Consumables.handleHealAction(player);
			} else {
				player.getPacketSender().sendMessage("You cannot heal yourself right now.");
			}
			break;
		case 18025:
			if (PrayerHandler.isActivated(player, PrayerHandler.AUGURY)) {
				PrayerHandler.deactivatePrayer(player, PrayerHandler.AUGURY);
			} else {
				PrayerHandler.activatePrayer(player, PrayerHandler.AUGURY);
			}
			break;
		case 18018:
			if (PrayerHandler.isActivated(player, PrayerHandler.RIGOUR)) {
				PrayerHandler.deactivatePrayer(player, PrayerHandler.RIGOUR);
			} else {
				PrayerHandler.activatePrayer(player, PrayerHandler.RIGOUR);
			}
			break;
		case 10000:
		case 950:
			if (player.getInterfaceId() < 0)
				player.getPacketSender().sendInterface(40030);
			else
				player.getPacketSender().sendMessage("Please close the interface you have open before doing this.");
			break;
		case 3546:
		case 3420:
			if (System.currentTimeMillis() - player.getTrading().lastAction <= 300)
				return;
			player.getTrading().lastAction = System.currentTimeMillis();
			if (player.getTrading().inTrade()) {
				player.getTrading().acceptTrade(id == 3546 ? 2 : 1);
			} else {
				player.getPacketSender().sendInterfaceRemoval();
			}
			break;
		case 26003:
		case 10162:
		case -18269:
		case 15210:
		case 26300:
			player.getPacketSender().sendInterfaceRemoval();
			break;
		case 841:
			IngridientsBook.readBook(player, player.getCurrentBookPage() + 2, true);
			break;
		case 839:
			IngridientsBook.readBook(player, player.getCurrentBookPage() - 2, true);
			break;
		case 14922:
			player.getPacketSender().sendClientRightClickRemoval().sendInterfaceRemoval();
			break;
		case 14921:
			player.getPacketSender().sendMessage("Please visit the forums and ask for help in the support section.");
			break;
		case 5294:
			player.getPacketSender().sendClientRightClickRemoval().sendInterfaceRemoval();
			player.setDialogueActionId(player.getBankPinAttributes().hasBankPin() ? 8 : 7);
			DialogueManager.start(player,
					DialogueManager.getDialogues().get(player.getBankPinAttributes().hasBankPin() ? 12 : 9));
			break;
		case 15002:
		case 27653:
			if (!player.busy() && !player.getCombatBuilder().isBeingAttacked()
					&& !Dungeoneering.doingDungeoneering(player)) {
				player.getSkillManager().stopSkilling();
				player.getPriceChecker().open();
			} else {
				player.getPacketSender().sendMessage("You cannot open this right now.");
			}
			break;
		case 2735:
		case 1511:
			if (player.getSummoning().getBeastOfBurden() != null) {
				player.getSummoning().toInventory();
				player.getPacketSender().sendInterfaceRemoval();
			} else {
				player.getPacketSender().sendMessage("You do not have a familiar who can hold items.");
			}
			break;
		case -11501:
		case -11504:
		case -11498:
		case -11507:
		case 1020:
		case 1021:
		case 1019:
		case 1018:
			if (id == 1020 || id == -11504)
				SummoningTab.renewFamiliar(player);
			else if (id == 1019 || id == -11501)
				SummoningTab.callFollower(player);
			else if (id == 1021 || id == -11498)
				SummoningTab.handleDismiss(player, false);
			else if (id == -11507)
				player.getSummoning().toInventory();
			else if (id == 1018)
				player.getSummoning().toInventory();
			break;
		case 2799:
		case 2798:
		case 1747:
		case 1748:
		case 8890:
		case 8886:
		case 8875:
		case 8871:
		case 8894:
			ChatboxInterfaceSkillAction.handleChatboxInterfaceButtons(player, id);
			break;
		case 14873:
		case 14874:
		case 14875:
		case 14876:
		case 14877:
		case 14878:
		case 14879:
		case 14880:
		case 14881:
		case 14882:
			BankPin.clickedButton(player, id);
			break;
		case 27005:
		case 22012:
			if (!player.isBanking() || player.getInterfaceId() != 5292)
				return;
			Bank.depositItems(player, id == 27005 ? player.getEquipment() : player.getInventory(), false);
			break;
		case 27023:
			if (!player.isBanking() || player.getInterfaceId() != 5292)
				return;
			if (player.getSummoning().getBeastOfBurden() == null) {
				player.getPacketSender().sendMessage("You do not have a familiar which can hold items.");
				return;
			}
			Bank.depositItems(player, player.getSummoning().getBeastOfBurden(), false);
			break;
		case 22008:
			if (!player.isBanking() || player.getInterfaceId() != 5292)
				return;
			player.setNoteWithdrawal(!player.withdrawAsNote());
			break;
		case 21000:
			if (!player.isBanking() || player.getInterfaceId() != 5292)
				return;
			player.setSwapMode(false);
			player.getPacketSender().sendConfig(304, 0).sendMessage("This feature is coming soon!");
			// player.setSwapMode(!player.swapMode());
			break;
		case 27009:
			MoneyPouch.toBank(player);
			break;
		case 27014:
		case 27015:
		case 27016:
		case 27017:
		case 27018:
		case 27019:
		case 27020:
		case 27021:
		case 27022:
			if (!player.isBanking())
				return;
			if (player.getBankSearchingAttributes().isSearchingBank())
				BankSearchAttributes.stopSearch(player, true);
			int bankId = id - 27014;
			boolean empty = bankId > 0 ? Bank.isEmpty(player.getBank(bankId)) : false;
			if (!empty || bankId == 0) {
				player.setCurrentBankTab(bankId);
				player.getPacketSender().sendString(5385, "scrollreset");
				player.getPacketSender().sendString(27002, Integer.toString(player.getCurrentBankTab()));
				player.getPacketSender().sendString(27000, "1");
				player.getBank(bankId).open();
			} else
				player.getPacketSender().sendMessage("To create a new tab, please drag an item here.");
			break;
		case 22004:
			if (!player.isBanking())
				return;
			if (!player.getBankSearchingAttributes().isSearchingBank()) {
				player.getBankSearchingAttributes().setSearchingBank(true);
				player.setInputHandling(new EnterSyntaxToBankSearchFor());
				player.getPacketSender().sendEnterInputPrompt("What would you like to search for?");
			} else {
				BankSearchAttributes.stopSearch(player, true);
			}
			break;
		case 14002:
		case 14003:
		case 14004:
		case 14005:
		case 14006:
		case 14007:
		case 14008:
		case 14009:
		case 14010:
		case 14011:
			CompletionistCapes.handleButton(player, id);
			break;
		case 22845:
		case 24115:
		case 24010:
		case 24041:
		case 150:
			player.setAutoRetaliate(!player.isAutoRetaliate());
			break;
		case 29332:
			ClanChat clan = player.getCurrentClanChat();
			if (clan == null) {
				player.getPacketSender().sendMessage("You are not in a clanchat channel.");
				return;
			}
			ClanChatManager.leave(player, false);
			player.setClanChatName(null);
			break;
		case 29329:
			if (player.getInterfaceId() > 0) {
				player.getPacketSender()
						.sendMessage("Please close the interface you have open before opening another one.");
				return;
			}
			player.setInputHandling(new EnterClanChatToJoin());
			player.getPacketSender().sendEnterInputPrompt("Enter the name of the clanchat channel you wish to join:");
			break;
		case 19158:
		case 152:
			if (player.getRunEnergy() <= 1) {
				player.getPacketSender().sendMessage("You do not have enough energy to do this.");
				player.getWalkingQueue().setRunningToggled(false);
			} else {
				player.getWalkingQueue().setRunningToggled(!player.getWalkingQueue().isRunning());
			}
			player.getPacketSender().sendRunStatus();
			break;
		case -10422:
		case -26369:
		case 27658:
		case 15004:
			player.setExperienceLocked(!player.experienceLocked());
			String type = player.experienceLocked() ? "locked" : "unlocked";
			player.getPacketSender().sendMessage("Your experience is now " + type + ".");
			PlayerPanel.refreshPanel(player);
			break;
		case 27651:
		case 21341:
		case 15001:
			if (player.getInterfaceId() == -1) {
				player.getSkillManager().stopSkilling();
				BonusManager.update(player);
				player.getPacketSender().sendInterface(21172);
			} else
				player.getPacketSender().sendMessage("Please close the interface you have open before doing this.");
			break;
		case 15003:
		case 27654:
			if (player.getInterfaceId() > 0) {
				player.getPacketSender()
						.sendMessage("Please close the interface you have open before opening another one.");
				return;
			}
			player.getSkillManager().stopSkilling();
			ItemsKeptOnDeath.sendInterface(player);
			break;
		case 2458: // Logout
			if (player.logout()) {
				World.getPlayers().remove(player);
			}
			break;
		// case 10003:
		case 29138:
		case 29038:
		case 29063:
		case 29113:
		case 29163:
		case 29188:
		case 29213:
		case 29238:
		case 30007:
		case 48023:
		case 33033:
		case 30108:
		case 7473:
		case 7562:
		case 7487:
		case 7788:
		case 8481:
		case 7612:
		case 7587:
		case 7662:
		case 7462:
		case 7548:
		case 7687:
		case 7537:
		case 12322:
		case 7637:
		case 12311:
		case 10003:
		case 8003:
			CombatSpecial.activate(player);
			break;
		case 1772: // shortbow & longbow
			if (player.getWeapon() == WeaponInterface.SHORTBOW) {
				player.setFightType(FightType.SHORTBOW_ACCURATE);
			} else if (player.getWeapon() == WeaponInterface.LONGBOW) {
				player.setFightType(FightType.LONGBOW_ACCURATE);
			} else if (player.getWeapon() == WeaponInterface.CROSSBOW) {
				player.setFightType(FightType.CROSSBOW_ACCURATE);
			}
			break;
		case 1771:
			if (player.getWeapon() == WeaponInterface.SHORTBOW) {
				player.setFightType(FightType.SHORTBOW_RAPID);
			} else if (player.getWeapon() == WeaponInterface.LONGBOW) {
				player.setFightType(FightType.LONGBOW_RAPID);
			} else if (player.getWeapon() == WeaponInterface.CROSSBOW) {
				player.setFightType(FightType.CROSSBOW_RAPID);
			}
			break;
		case 1770:
			if (player.getWeapon() == WeaponInterface.SHORTBOW) {
				player.setFightType(FightType.SHORTBOW_LONGRANGE);
			} else if (player.getWeapon() == WeaponInterface.LONGBOW) {
				player.setFightType(FightType.LONGBOW_LONGRANGE);
			} else if (player.getWeapon() == WeaponInterface.CROSSBOW) {
				player.setFightType(FightType.CROSSBOW_LONGRANGE);
			}
			break;
		case 2282: // dagger & sword
			if (player.getWeapon() == WeaponInterface.DAGGER) {
				player.setFightType(FightType.DAGGER_STAB);
			} else if (player.getWeapon() == WeaponInterface.SWORD) {
				player.setFightType(FightType.SWORD_STAB);
			}
			break;
		case 2285:
			if (player.getWeapon() == WeaponInterface.DAGGER) {
				player.setFightType(FightType.DAGGER_LUNGE);
			} else if (player.getWeapon() == WeaponInterface.SWORD) {
				player.setFightType(FightType.SWORD_LUNGE);
			}
			break;
		case 2284:
			if (player.getWeapon() == WeaponInterface.DAGGER) {
				player.setFightType(FightType.DAGGER_SLASH);
			} else if (player.getWeapon() == WeaponInterface.SWORD) {
				player.setFightType(FightType.SWORD_SLASH);
			}
			break;
		case 2283:
			if (player.getWeapon() == WeaponInterface.DAGGER) {
				player.setFightType(FightType.DAGGER_BLOCK);
			} else if (player.getWeapon() == WeaponInterface.SWORD) {
				player.setFightType(FightType.SWORD_BLOCK);
			}
			break;
		case 2429: // scimitar & longsword
			if (player.getWeapon() == WeaponInterface.SCIMITAR) {
				player.setFightType(FightType.SCIMITAR_CHOP);
			} else if (player.getWeapon() == WeaponInterface.LONGSWORD) {
				player.setFightType(FightType.LONGSWORD_CHOP);
			}
			break;
		case 2432:
			if (player.getWeapon() == WeaponInterface.SCIMITAR) {
				player.setFightType(FightType.SCIMITAR_SLASH);
			} else if (player.getWeapon() == WeaponInterface.LONGSWORD) {
				player.setFightType(FightType.LONGSWORD_SLASH);
			}
			break;
		case 2431:
			if (player.getWeapon() == WeaponInterface.SCIMITAR) {
				player.setFightType(FightType.SCIMITAR_LUNGE);
			} else if (player.getWeapon() == WeaponInterface.LONGSWORD) {
				player.setFightType(FightType.LONGSWORD_LUNGE);
			}
			break;
		case 2430:
			if (player.getWeapon() == WeaponInterface.SCIMITAR) {
				player.setFightType(FightType.SCIMITAR_BLOCK);
			} else if (player.getWeapon() == WeaponInterface.LONGSWORD) {
				player.setFightType(FightType.LONGSWORD_BLOCK);
			}
			break;
		case 3802: // mace
			player.setFightType(FightType.MACE_POUND);
			break;
		case 3805:
			player.setFightType(FightType.MACE_PUMMEL);
			break;
		case 3804:
			player.setFightType(FightType.MACE_SPIKE);
			break;
		case 3699:
			player.getAppearance().setGender(Gender.FEMALE);
			break;
		case 3698:
			player.getAppearance().setGender(Gender.MALE);
			break;
		case 3803:
			player.setFightType(FightType.MACE_BLOCK);
			break;
		case 4454: // knife, thrownaxe, dart & javelin
			if (player.getWeapon() == WeaponInterface.KNIFE) {
				player.setFightType(FightType.KNIFE_ACCURATE);
			} else if (player.getWeapon() == WeaponInterface.THROWNAXE) {
				player.setFightType(FightType.THROWNAXE_ACCURATE);
			} else if (player.getWeapon() == WeaponInterface.DART) {
				player.setFightType(FightType.DART_ACCURATE);
			} else if (player.getWeapon() == WeaponInterface.JAVELIN) {
				player.setFightType(FightType.JAVELIN_ACCURATE);
			}
			break;
		case 4453:
			if (player.getWeapon() == WeaponInterface.KNIFE) {
				player.setFightType(FightType.KNIFE_RAPID);
			} else if (player.getWeapon() == WeaponInterface.THROWNAXE) {
				player.setFightType(FightType.THROWNAXE_RAPID);
			} else if (player.getWeapon() == WeaponInterface.DART) {
				player.setFightType(FightType.DART_RAPID);
			} else if (player.getWeapon() == WeaponInterface.JAVELIN) {
				player.setFightType(FightType.JAVELIN_RAPID);
			}
			break;
		case 4452:
			if (player.getWeapon() == WeaponInterface.KNIFE) {
				player.setFightType(FightType.KNIFE_LONGRANGE);
			} else if (player.getWeapon() == WeaponInterface.THROWNAXE) {
				player.setFightType(FightType.THROWNAXE_LONGRANGE);
			} else if (player.getWeapon() == WeaponInterface.DART) {
				player.setFightType(FightType.DART_LONGRANGE);
			} else if (player.getWeapon() == WeaponInterface.JAVELIN) {
				player.setFightType(FightType.JAVELIN_LONGRANGE);
			}
			break;
		case 4685: // spear
			player.setFightType(FightType.SPEAR_LUNGE);
			break;
		case 4688:
			player.setFightType(FightType.SPEAR_SWIPE);
			break;
		case 4687:
			player.setFightType(FightType.SPEAR_POUND);
			break;
		case 4686:
			player.setFightType(FightType.SPEAR_BLOCK);
			break;
		case 4711: // 2h sword
			player.setFightType(FightType.TWOHANDEDSWORD_CHOP);
			break;
		case 4714:
			player.setFightType(FightType.TWOHANDEDSWORD_SLASH);
			break;
		case 4713:
			player.setFightType(FightType.TWOHANDEDSWORD_SMASH);
			break;
		case 4712:
			player.setFightType(FightType.TWOHANDEDSWORD_BLOCK);
			break;
		case 5576: // pickaxe
			player.setFightType(FightType.PICKAXE_SPIKE);
			break;
		case 5579:
			player.setFightType(FightType.PICKAXE_IMPALE);
			break;
		case 5578:
			player.setFightType(FightType.PICKAXE_SMASH);
			break;
		case 5577:
			player.setFightType(FightType.PICKAXE_BLOCK);
			break;
		case 7768: // claws
			player.setFightType(FightType.CLAWS_CHOP);
			break;
		case 7771:
			player.setFightType(FightType.CLAWS_SLASH);
			break;
		case 7770:
			player.setFightType(FightType.CLAWS_LUNGE);
			break;
		case 7769:
			player.setFightType(FightType.CLAWS_BLOCK);
			break;
		case 8466: // halberd
			player.setFightType(FightType.HALBERD_JAB);
			break;
		case 8468:
			player.setFightType(FightType.HALBERD_SWIPE);
			break;
		case 8467:
			player.setFightType(FightType.HALBERD_FEND);
			break;
		case 5862: // unarmed
			player.setFightType(FightType.UNARMED_PUNCH);
			break;
		case 5861:
			player.setFightType(FightType.UNARMED_KICK);
			break;
		case 5860:
			player.setFightType(FightType.UNARMED_BLOCK);
			break;
		case 12298: // whip
			player.setFightType(FightType.WHIP_FLICK);
			break;
		case 12297:
			player.setFightType(FightType.WHIP_LASH);
			break;
		case 12296:
			player.setFightType(FightType.WHIP_DEFLECT);
			break;
		case 336: // staff
			player.setFightType(FightType.STAFF_BASH);
			break;
		case 335:
			player.setFightType(FightType.STAFF_POUND);
			break;
		case 334:
			player.setFightType(FightType.STAFF_FOCUS);
			break;
		case 433: // warhammer
			player.setFightType(FightType.WARHAMMER_POUND);
			break;
		case 432:
			player.setFightType(FightType.WARHAMMER_PUMMEL);
			break;
		case 431:
			player.setFightType(FightType.WARHAMMER_BLOCK);
			break;
		case 782: // scythe
			player.setFightType(FightType.SCYTHE_REAP);
			break;
		case 784:
			player.setFightType(FightType.SCYTHE_CHOP);
			break;
		case 785:
			player.setFightType(FightType.SCYTHE_JAB);
			break;
		case 783:
			player.setFightType(FightType.SCYTHE_BLOCK);
			break;
		case 1704: // battle axe
			player.setFightType(FightType.BATTLEAXE_CHOP);
			break;
		case 1707:
			player.setFightType(FightType.BATTLEAXE_HACK);
			break;
		case 1706:
			player.setFightType(FightType.BATTLEAXE_SMASH);
			break;
		case 1705:
			player.setFightType(FightType.BATTLEAXE_BLOCK);
			break;
		}
	}

	private boolean checkOptionContainer(Player player, int id) {
		switch (id) {
			case 2461: {
				if (player.getOptionContainer().handle(Option.OptionType.OPTION_1_OF_2))
					return true;
				break;
			}
			case 2462: {
				if (player.getOptionContainer().handle(Option.OptionType.OPTION_2_OF_2))
					return true;
				break;
			}
			case 2471: {
				if (player.getOptionContainer().handle(Option.OptionType.OPTION_1_OF_3))
					return true;
				break;
			}
			case 2472: {
				if (player.getOptionContainer().handle(Option.OptionType.OPTION_2_OF_3))
					return true;
				break;
			}
			case 2473: {
				if (player.getOptionContainer().handle(Option.OptionType.OPTION_3_OF_3))
					return true;
				break;
			}
			case 2482: {
				if (player.getOptionContainer().handle(Option.OptionType.OPTION_1_OF_4))
					return true;
				break;
			}
			case 2483: {
				if (player.getOptionContainer().handle(Option.OptionType.OPTION_2_OF_4))
					return true;
				break;
			}
			case 2484: {
				if (player.getOptionContainer().handle(Option.OptionType.OPTION_3_OF_4))
					return true;
				break;
			}
			case 2485: {
				if (player.getOptionContainer().handle(Option.OptionType.OPTION_4_OF_4))
					return true;
				break;
			}
			case 2494: {
				if (player.getOptionContainer().handle(Option.OptionType.OPTION_1_OF_5))
					return true;
				break;
			}
			case 2495: {
				if (player.getOptionContainer().handle(Option.OptionType.OPTION_2_OF_5))
					return true;
				break;
			}
			case 2496: {
				if (player.getOptionContainer().handle(Option.OptionType.OPTION_3_OF_5))
					return true;
				break;
			}
			case 2497: {
				if (player.getOptionContainer().handle(Option.OptionType.OPTION_4_OF_5))
					return true;
				break;
			}
			case 2498: {
				if (player.getOptionContainer().handle(Option.OptionType.OPTION_5_OF_5))
					return true;
				break;
			}

		}

		return false;
	}

	private boolean checkHandlers(Player player, int id) {
		if (Construction.handleButtonClick(id, player)) {
			return true;
		}
		if (SawmillOperator.handleButtonClick(id, player))
			return true;
		switch (id) {
		case 2494:
		case 2495:
		case 2496:
		case 2497:
		case 2498:
		case 2471:
		case 2472:
		case 2473:
		case 2461:
		case 2462:
		case 2482:
		case 2483:
		case 2484:
		case 2485:
			DialogueOptions.handle(player, id);
			return true;
		}
		if (player.isPlayerLocked() && id != 2458 && (id < 14873 && id > 14882)) {
			return true;
		}
		if (Achievements.handleButton(player, id)) {
			return true;
		}
		if (Sounds.handleButton(player, id)) {
			return true;
		}
		if (PrayerHandler.isButton(id)) {
			PrayerHandler.togglePrayerWithActionButton(player, id);
			return true;
		}
		if (CurseHandler.isButton(player, id)) {
			return true;
		}
		if (Autocasting.handleAutocast(player, id)) {
			return true;
		}
		if (SmithingData.handleButtons(player, id)) {
			return true;
		}
		if (PouchMaking.pouchInterface(player, id)) {
			return true;
		}
		if (LoyaltyProgramme.handleButton(player, id)) {
			return true;
		}
		if (Fletching.fletchingButton(player, id)) {
			return true;
		}
		if (LeatherMaking.handleButton(player, id) || Tanning.handleButton(player, id)) {
			return true;
		}
		if (Emotes.doEmote(player, id)) {
			return true;
		}
		if (PestControl.handleInterface(player, id)) {
			return true;
		}
		if (player.getLocation() == Location.DUEL_ARENA && Dueling.handleDuelingButtons(player, id)) {
			return true;
		}
		if (Slayer.handleRewardsInterface(player, id)) {
			return true;
		}
		if (ExperienceLamps.handleButton(player, id)) {
			return true;
		}
		if (PlayersOnlineInterface.handleButton(player, id)) {
			return true;
		}
		if (GrandExchange.handleButton(player, id)) {
			return true;
		}
		if (PlayerOwnedShops.posButtons(player, id)) {
			return true;
		}
		if (ClanChatManager.handleClanChatSetupButton(player, id)) {
			return true;
		}
		return false;
	}

	public static final int OPCODE = 185;
}