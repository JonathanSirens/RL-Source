package com.chaos.world.content.minigames.impl;

import java.util.concurrent.CopyOnWriteArrayList;

import com.chaos.engine.task.Task;
import com.chaos.engine.task.TaskManager;
import com.chaos.model.Flag;
import com.chaos.model.Item;
import com.chaos.model.Locations;
import com.chaos.model.Locations.Location;
import com.chaos.model.StaffRights;
import com.chaos.model.Position;
import com.chaos.model.container.impl.Equipment;
import com.chaos.model.container.impl.Inventory;
import com.chaos.model.definitions.ItemDefinition;
import com.chaos.util.Misc;
import com.chaos.world.World;
import com.chaos.world.content.BankPin;
import com.chaos.world.content.BonusManager;
import com.chaos.world.content.PlayerLogs;
import com.chaos.world.entity.impl.player.Player;

public class Dueling {

	Player player;

	public Dueling(Player player) {
		this.player = player;
	}

	public void challengePlayer(Player playerToDuel) {
		if (player.getLocation() != Location.DUEL_ARENA)
			return;
		if (player.getBankPinAttributes().hasBankPin() && !player.getBankPinAttributes().hasEnteredBankPin()
				&& player.getBankPinAttributes().onDifferent(player)) {
			BankPin.init(player, false);
			return;
		}
		if (player.getInterfaceId() > 0) {
			player.getPacketSender()
					.sendMessage("Please close the interface you have open before trying to open a new one.");
			return;
		}
		if (!Locations.goodDistance(player.getPosition().getX(), player.getPosition().getY(),
				playerToDuel.getPosition().getX(), playerToDuel.getPosition().getY(), 2)) {
			player.getPacketSender().sendMessage("Please get closer to request a duel.");
			return;
		}
		if (!checkDuel(player, 0)) {
			player.getPacketSender()
					.sendMessage("Unable to request duel. Please try logging out and then logging back in.");
			return;
		}
		if (!checkDuel(playerToDuel, 0) || playerToDuel.getInterfaceId() > 0) {
			player.getPacketSender().sendMessage("The other player is currently busy.");
			return;
		}
		if (player.getDueling().duelingStatus == 5) {
			player.getPacketSender().sendMessage("You can only challenge people outside the arena.");
			return;
		}
		if (player.getSummoning().getFamiliar() != null) {
			player.getPacketSender()
					.sendMessage("You must dismiss your familiar before being allowed to start a duel.");
			return;
		}
		if (inDuelScreen)
			return;
		if (player.getTrading().inTrade())
			player.getTrading().declineTrade(true);
		duelingWith = playerToDuel.getIndex();
		if (duelingWith == player.getIndex())
			return;
		duelRequested = true;
		boolean challenged = playerToDuel.getDueling().duelingStatus == 0 && duelRequested
				|| playerToDuel.getDueling().duelRequested;
		if (duelingStatus == 0 && challenged && duelingWith == playerToDuel.getIndex()
				&& playerToDuel.getDueling().duelingWith == player.getIndex()) {
			if (duelingStatus == 0) {
				openDuel();
				playerToDuel.getDueling().openDuel();
			} else {
				player.getPacketSender().sendMessage("You must decline this duel before accepting another one!");
			}
		} else if (duelingStatus == 0) {
			playerToDuel.getPacketSender().sendMessage(player.getUsername() + ":duelreq:");
			player.getPacketSender().sendMessage("You have sent a duel request to " + playerToDuel.getUsername() + ".");
		}
	}

	public void openDuel() {
		Player playerToDuel = World.getPlayers().get(duelingWith);
		if (playerToDuel == null)
			return;
		player.getPacketSender().sendClientRightClickRemoval();
		inDuelWith = playerToDuel.getIndex();
		stakedItems.clear();
		inDuelScreen = true;
		duelingStatus = 1;
		if (!checkDuel(player, 1))
			return;
		for (int i = 0; i < selectedDuelRules.length; i++)
			selectedDuelRules[i] = false;
		player.getPacketSender().sendConfig(286, 0);
		for (int i = 643; i <= 656; i++)
			player.getPacketSender().sendToggle(i, 0);

		player.getTrading().setCanOffer(true);
		player.getPacketSender().sendDuelEquipment();
		player.getPacketSender().sendString(6671, "Dueling with: " + playerToDuel.getUsername());
		player.getPacketSender()
				.sendMessage("Dueling with: " + playerToDuel.getUsername() + ", Level: "
						+ playerToDuel.getSkillManager().getCombatLevel() + ", Duel victories: "
						+ playerToDuel.getDueling().arenaStats[0] + ", Duel losses: "
						+ playerToDuel.getDueling().arenaStats[1]);
		player.getPacketSender().sendString(6684, "").sendString(669, "@yel@Same Weapon").sendString(8278,
				"Neither player is allowed to change weapon.");
		player.getPacketSender().sendInterfaceSet(6575, 3321);
		player.getPacketSender().sendItemContainer(player.getInventory(), 3322);
		player.getPacketSender().sendInterfaceItems(6670, playerToDuel.getDueling().stakedItems);
		player.getPacketSender().sendInterfaceItems(6669, player.getDueling().stakedItems);
		canOffer = true;
	}

	public void declineDuel(boolean tellOther) {
		Player playerToDuel = duelingWith >= 0 ? World.getPlayers().get(duelingWith) : null;
		if (tellOther) {
			if (playerToDuel == null)
				return;
			if (playerToDuel == null || playerToDuel.getDueling().duelingStatus == 6) {
				return;
			}
			playerToDuel.getDueling().declineDuel(false);
		}
		for (Item item : stakedItems) {
			if (item.getAmount() < 1)
				continue;
			player.getInventory().add(item);
		}
		reset();
		player.getPacketSender().sendInterfaceRemoval();
	}

	public void resetAcceptedStake() {
		Player playerToDuel = World.getPlayers().get(duelingWith);
		if (playerToDuel == null)
			return;
		if (player.getDueling().duelingStatus == 2 || playerToDuel.getDueling().duelingStatus == 2) {
			player.getDueling().duelingStatus = 1;
			player.getPacketSender().sendString(6684, "");
			playerToDuel.getPacketSender().sendString(6684, "");
			playerToDuel.getDueling().duelingStatus = 1;
		}
	}

	public void stakeItem(int itemId, int amount, int slot) {
		if (slot < 0)
			return;
		/*
		 * if(Misc.getMinutesPlayed(player) < 15) {
		 * player.getPacketSender().sendMessage(
		 * "You must have played for at least 15 minutes in order to stake items."
		 * ); return; }
		 */
		if (!getCanOffer())
			return;
		resetAcceptedStake();
		if (!player.getInventory().contains(itemId) || !inDuelScreen)
			return;
		Player playerToDuel = World.getPlayers().get(duelingWith);
		if (playerToDuel == null)
			return;
		int amountInPlayerInv = player.getInventory().getAmount(itemId);
		int amountInOtherInv = playerToDuel.getInventory().getAmount(itemId);
		Item itemAmount = new Item(itemId, amount);
		if (player.getStaffRights() != StaffRights.OWNER && player.getStaffRights() != StaffRights.MANAGER) {
			if (!new Item(itemId).tradeable(player)) {
				player.getPacketSender().sendMessage("This item is currently untradeable and cannot be traded.");
				return;
			}
		}
		if (player.getGameModeAssistant().isIronMan()) {
			player.getPacketSender().sendMessage("You're dumb for trying to stake an Ironman.");
			return;
		}
		if (playerToDuel.getGameModeAssistant().isIronMan()) {
			player.getPacketSender().sendMessage("You're dumb for trying to stake an Ironman.");
			return;
		}

		if (!checkDuel(player, 1) || !checkDuel(playerToDuel, 1) || slot >= player.getInventory().capacity()
				|| player.getInventory().getItems()[slot].getId() != itemId
				|| player.getInventory().getItems()[slot].getAmount() <= 0) {
			declineDuel(false);
			playerToDuel.getDueling().declineDuel(false);
			return;
		}
		if (player.getInventory().forSlot(slot).getAmount() < amount) {
			amount = player.getInventory().forSlot(slot).getAmount();
			if (amount == 0 || player.getInventory().forSlot(slot).getAmount() < amount) {
				return;
			}
		}
		if (!ItemDefinition.forId(itemId).isStackable()) {
			for (int a = 0; a < amount; a++) {
				if (player.getInventory().contains(itemId)) {
					stakedItems.add(new Item(itemId, 1));
					player.getInventory().delete(new Item(itemId));
				}
			}
		} else {
			if (amount <= 0 || player.getInventory().getItems()[slot].getAmount() <= 0)
				return;
			boolean itemInScreen = false;
			for (Item item : stakedItems) {
				if (item.getId() == itemId) {
					itemInScreen = true;
					item.setAmount(item.getAmount() + amount);
					player.getInventory().delete(new Item(itemId).setAmount(amount), slot);
					break;
				}
			}
			if (!itemInScreen) {
				player.getInventory().delete(new Item(itemId, amount), slot);
				stakedItems.add(new Item(itemId, amount));
			}
		}
		player.getPacketSender().sendClientRightClickRemoval();
		player.getPacketSender().sendInterfaceItems(6670, playerToDuel.getDueling().stakedItems);
		player.getPacketSender().sendInterfaceItems(6669, player.getDueling().stakedItems);
		playerToDuel.getPacketSender().sendInterfaceItems(6670, player.getDueling().stakedItems);
		player.getPacketSender().sendString(6684, "");
		playerToDuel.getPacketSender().sendString(6684, "");
		duelingStatus = 1;
		playerToDuel.getDueling().duelingStatus = 1;
		player.getInventory().refreshItems();
		player.getPacketSender().sendItemContainer(player.getInventory(), 3322);
	}

	public void removeStakedItem(int itemId, int amount) {
		if (!inDuelScreen || !getCanOffer())
			return;
		Player playerToDuel = World.getPlayers().get(duelingWith);
		if (playerToDuel == null)
			return;
		resetAcceptedStake();
		if (!checkDuel(player, 1) || !checkDuel(playerToDuel, 1)) {
			declineDuel(false);
			playerToDuel.getDueling().declineDuel(false);
			return;
		}
		/*
		 * if (Item.itemStackable[itemID]) { if
		 * (playerToDuel.getInventory().getFreeSlots() - 1 < (c.duelSpaceReq)) {
		 * c.sendMessage("You have too many rules set to remove that item.");
		 * return false; } }
		 */
		player.getPacketSender().sendClientRightClickRemoval();
		if (!ItemDefinition.forId(itemId).isStackable()) {
			if (amount > 28)
				amount = 28;
			for (int a = 0; a < amount; a++) {
				for (Item item : stakedItems) {
					if (item.getId() == itemId) {
						if (!item.getDefinition().isStackable()) {
							if (!checkDuel(player, 1) || !checkDuel(playerToDuel, 1)) {
								declineDuel(false);
								playerToDuel.getDueling().declineDuel(false);
								return;
							}
							stakedItems.remove(item);
							player.getInventory().add(item);
						}
						break;
					}
				}
			}
		} else
			for (Item item : stakedItems) {
				if (item.getId() == itemId) {
					if (item.getDefinition().isStackable()) {
						if (item.getAmount() > amount) {
							item.setAmount(item.getAmount() - amount);
							player.getInventory().add(itemId, amount);
						} else {
							amount = item.getAmount();
							stakedItems.remove(item);
							player.getInventory().add(item.getId(), amount);
						}
					}
					break;
				}
			}
		player.getDuelTimer().reset(); // reset timer if item removed
		playerToDuel.getPacketSender()
				.sendMessage("@red@Your opponent has removed an item from the stake please review the staked items.");
		player.getPacketSender().sendInterfaceItems(6670, playerToDuel.getDueling().stakedItems);
		player.getPacketSender().sendInterfaceItems(6669, player.getDueling().stakedItems);
		playerToDuel.getPacketSender().sendInterfaceItems(6670, player.getDueling().stakedItems);
		playerToDuel.getPacketSender().sendString(6684, "");
		player.getPacketSender().sendString(6684, "");
		duelingStatus = 1;
		playerToDuel.getDueling().duelingStatus = 1;
		player.getInventory().refreshItems();
		player.getPacketSender().sendItemContainer(player.getInventory(), 3322);
	}

	public void selectRule(DuelRule duelRule) {
		if (duelingWith < 0)
			return;
		final Player playerToDuel = World.getPlayers().get(duelingWith);
		if (playerToDuel == null)
			return;
		if (player.getInterfaceId() != 6575)
			return;
		if (duelRule == DuelRule.LOCK_WEAPON) {
			if (player.getEquipment().get(Equipment.WEAPON_SLOT).getId() != playerToDuel.getEquipment()
					.get(Equipment.WEAPON_SLOT).getId()) {
				player.getPacketSender().sendMessage(
						"@red@This rule requires you and your duel partner to have the same weapon equipped.");
				return;
			}
		}
		int index = duelRule.ordinal();
		boolean alreadySet = selectedDuelRules[duelRule.ordinal()];
		boolean slotOccupied = duelRule.getEquipmentSlot() > 0
				? player.getEquipment().getItems()[duelRule.getEquipmentSlot()].getId() > 0
						|| playerToDuel.getEquipment().getItems()[duelRule.getEquipmentSlot()].getId() > 0
				: false;
		if (duelRule == DuelRule.NO_SHIELD) {
			if (player.getEquipment().getItems()[Equipment.WEAPON_SLOT].getId() > 0
					&& ItemDefinition.forId(player.getEquipment().getItems()[Equipment.WEAPON_SLOT].getId())
							.isTwoHanded()
					|| ItemDefinition.forId(playerToDuel.getEquipment().getItems()[Equipment.WEAPON_SLOT].getId())
							.isTwoHanded())
				slotOccupied = true;
		}
		int spaceRequired = slotOccupied ? duelRule.getInventorySpaceReq() : 0;
		for (int i = 10; i < this.selectedDuelRules.length; i++) {
			if (selectedDuelRules[i]) {
				DuelRule rule = DuelRule.forId(i);
				if (rule.getEquipmentSlot() > 0)
					if (player.getEquipment().getItems()[rule.getEquipmentSlot()].getId() > 0
							|| playerToDuel.getEquipment().getItems()[rule.getEquipmentSlot()].getId() > 0)
						spaceRequired += rule.getInventorySpaceReq();
			}
		}
		if (!alreadySet && player.getInventory().getFreeSlots() < spaceRequired) {
			player.getPacketSender().sendMessage("You do not have enough free inventory space to set this rule.");
			return;
		}
		if (!alreadySet && playerToDuel.getInventory().getFreeSlots() < spaceRequired) {
			player.getPacketSender().sendMessage(
					"" + playerToDuel.getUsername() + " does not have enough inventory space for this rule to be set.");
			return;
		}
		if (index > 10) { // Equipment
			if (!player.getDueling().selectedDuelRules[index]) {
				player.getDueling().selectedDuelRules[index] = true;
				player.getPacketSender().sendToggle(duelRule.getConfigId(),
						player.getDueling().selectedDuelRules[index] ? 1 : 0);
				playerToDuel.getPacketSender().sendToggle(duelRule.getConfigId(),
						player.getDueling().selectedDuelRules[index] ? 1 : 0);
			} else {
				player.getDueling().selectedDuelRules[index] = false;
				player.getPacketSender().sendToggle(duelRule.getConfigId(),
						player.getDueling().selectedDuelRules[index] ? 1 : 0);
				playerToDuel.getPacketSender().sendToggle(duelRule.getConfigId(),
						player.getDueling().selectedDuelRules[index] ? 1 : 0);
			}
		} else { // Other
			if (!player.getDueling().selectedDuelRules[index]) {
				player.getDueling().selectedDuelRules[index] = true;
				playerToDuel.getDueling().selectedDuelRules[index] = true;

				player.getDueling().duelConfig += duelRule.getConfigId();
				playerToDuel.getDueling().duelConfig += duelRule.getConfigId();

			} else {
				player.getDueling().selectedDuelRules[index] = false;
				playerToDuel.getDueling().selectedDuelRules[index] = false;

				player.getDueling().duelConfig -= duelRule.getConfigId();
				playerToDuel.getDueling().duelConfig -= duelRule.getConfigId();

			}
		}
		if (index <= 10) {
			player.getPacketSender().sendToggle(286, player.getDueling().duelConfig);
			playerToDuel.getPacketSender().sendToggle(286, player.getDueling().duelConfig);
		}
		// playerToDuel.getDueling().duelConfig =
		// player.getDueling().duelConfig;
		playerToDuel.getDueling().selectedDuelRules[index] = player.getDueling().selectedDuelRules[index];

		// playerToDuel.getPacketSender().sendToggle(286,
		// playerToDuel.getDueling().duelConfig);

		player.getPacketSender().sendString(6684, "");
		resetAcceptedStake();
		if (selectedDuelRules[DuelRule.OBSTACLES.ordinal()]) {
			if (selectedDuelRules[DuelRule.NO_MOVEMENT.ordinal()]) {
				Position duelTele = new Position(3366 + Misc.getRandom(12), 3246 + Misc.getRandom(6), 0);
				player.getDueling().duelTelePos = duelTele;
				playerToDuel.getDueling().duelTelePos = player.getDueling().duelTelePos.copy();
				playerToDuel.getDueling().duelTelePos.setX(player.getDueling().duelTelePos.getX() - 1);
			}
		} else {
			if (selectedDuelRules[DuelRule.NO_MOVEMENT.ordinal()]) {
				Position duelTele = new Position(3335 + Misc.getRandom(12), 3246 + Misc.getRandom(6), 0);
				player.getDueling().duelTelePos = duelTele;
				playerToDuel.getDueling().duelTelePos = player.getDueling().duelTelePos.copy();
				playerToDuel.getDueling().duelTelePos.setX(player.getDueling().duelTelePos.getX() - 1);
			}
		}

		if (duelRule == DuelRule.LOCK_WEAPON && selectedDuelRules[duelRule.ordinal()]) {
			player.getPacketSender()
					.sendMessage(
							"@red@Warning! The rule 'Same Weapon' has been enabled. You will not be able to change")
					.sendMessage("@red@weapon during the duel!");
			playerToDuel.getPacketSender()
					.sendMessage(
							"@red@Warning! The rule 'Same Weapon' has been enabled. You will not be able to change")
					.sendMessage("@red@weapon during the duel!");
		}
	}

	/**
	 * Checks if two players are the only ones in a duel.
	 * 
	 * @param p1
	 *            Player1 to check if he's 1/2 player in trade.
	 * @param p2
	 *            Player2 to check if he's 2/2 player in trade.
	 * @return true if only two people are in the duel.
	 */
	public static boolean twoDuelers(Player p1, Player p2) {
		int count = 0;
		for (Player player : World.getPlayers()) {
			if (player == null)
				continue;
			if (player.getDueling().inDuelWith == p1.getIndex() || player.getDueling().inDuelWith == p2.getIndex()) {
				count++;
			}
		}
		return count == 2;
	}

	public void confirmDuel() {
		final Player playerToDuel = World.getPlayers().get(duelingWith);
		if (playerToDuel == null)
			return;
		else {
			if (!twoDuelers(player, playerToDuel)) {
				player.getPacketSender().sendMessage("An error has occured. Please try requesting a new duel.");
				return;
			}
		}
		String itemId = "";
		for (Item item : player.getDueling().stakedItems) {
			ItemDefinition def = item.getDefinition();
			if (def.isStackable()) {
				itemId += def.getName() + " x " + Misc.format(item.getAmount()) + "\\n";
			} else {
				itemId += def.getName() + "\\n";
			}
		}
		player.getPacketSender().sendString(6516, itemId);
		itemId = "";
		for (Item item : playerToDuel.getDueling().stakedItems) {
			ItemDefinition def = item.getDefinition();
			if (def.isStackable()) {
				itemId += def.getName() + " x " + Misc.format(item.getAmount()) + "\\n";
			} else {
				itemId += def.getName() + "\\n";
			}
		}
		canOffer = false;
		player.getPacketSender().sendString(6517, itemId);
		player.getPacketSender().sendString(8242, "");
		for (int i = 8238; i <= 8253; i++)
			player.getPacketSender().sendString(i, "");
		player.getPacketSender().sendString(8250, "Hitpoints will be restored.");
		player.getPacketSender().sendString(8238, "Boosted stats will be restored.");
		if (selectedDuelRules[DuelRule.OBSTACLES.ordinal()])
			player.getPacketSender().sendString(8239, "@red@There will be obstacles in the arena.");
		player.getPacketSender().sendString(8240, "");
		player.getPacketSender().sendString(8241, "");
		int lineNumber = 8242;
		for (int i = 0; i < DuelRule.values().length; i++) {
			if (i == DuelRule.OBSTACLES.ordinal())
				continue;
			if (selectedDuelRules[i]) {
				player.getPacketSender().sendString(lineNumber, "" + DuelRule.forId(i).toString());
				lineNumber++;
			}
		}
		player.getPacketSender().sendString(6571, "");
		player.getPacketSender().sendInterfaceSet(6412, Inventory.INTERFACE_ID);
		player.getPacketSender().sendItemContainer(player.getInventory(), 3322);
	}

	public static boolean handleDuelingButtons(final Player player, int button) {
		if (DuelRule.forButtonId(button) != null) {
			player.getDueling().selectRule(DuelRule.forButtonId(button));
			return true;
		} else {
			if (player.getDueling().duelingWith < 0)
				return false;
			final Player playerToDuel = World.getPlayers().get(player.getDueling().duelingWith);
			switch (button) {
			case 6674:
				if (!player.getDueling().inDuelScreen)
					return true;
				if (playerToDuel == null)
					return true;
				if (!checkDuel(player, 1) && !checkDuel(player, 2))
					return true;
				if (player.getDueling().selectedDuelRules[DuelRule.NO_MELEE.ordinal()]
						&& player.getDueling().selectedDuelRules[DuelRule.NO_RANGED.ordinal()]
						&& player.getDueling().selectedDuelRules[DuelRule.NO_MAGIC.ordinal()]) {
					player.getPacketSender()
							.sendMessage("You won't be able to attack the other player with the current rules.");
					return true;
				}
				if (!playerToDuel.getDuelTimer().elapsed(5000)) {
					player.getPacketSender().sendMessage("Your opponent recently removed an item from their stake.");
					return false;
				}
				player.getDueling().duelingStatus = 2;
				if (player.getDueling().duelingStatus == 2) {
					player.getPacketSender().sendString(6684, "Waiting for other player...");
					playerToDuel.getPacketSender().sendString(6684, "Other player has accepted.");
				}
				if (playerToDuel.getDueling().duelingStatus == 2) {
					playerToDuel.getPacketSender().sendString(6684, "Waiting for other player...");
					player.getPacketSender().sendString(6684, "Other player has accepted.");
				}
				if (player.getDueling().duelingStatus == 2 && playerToDuel.getDueling().duelingStatus == 2) {
					player.getDueling().duelingStatus = 3;
					playerToDuel.getDueling().duelingStatus = 3;
					playerToDuel.getDueling().confirmDuel();
					player.getDueling().confirmDuel();
				}
				return true;
			case 6520:
				if (!player.getDueling().inDuelScreen || (!checkDuel(player, 3) && !checkDuel(player, 4))
						|| playerToDuel == null)
					return true;
				player.getDueling().duelingStatus = 4;
				if (playerToDuel.getDueling().duelingStatus == 4 && player.getDueling().duelingStatus == 4) {
					player.getDueling().startDuel();
					playerToDuel.getDueling().startDuel();
				} else {
					player.getPacketSender().sendString(6571, "Waiting for other player...");
					playerToDuel.getPacketSender().sendString(6571, "Other player has accepted");
				}
				return true;
			}
		}
		return false;
	}

	public void startDuel() {
		player.getSession().clearMessages();
		inDuelScreen = false;
		final Player playerToDuel = World.getPlayers().get(duelingWith);
		if (playerToDuel == null) {
			duelVictory();
			return;
		}
		player.getTrading().offeredItems.clear();
		duelingData[0] = playerToDuel != null ? playerToDuel.getUsername() : "Disconnected";
		duelingData[1] = playerToDuel != null ? playerToDuel.getSkillManager().getCombatLevel() : 3;
		Item equipItem;
		for (int i = 10; i < selectedDuelRules.length; i++) {
			DuelRule rule = DuelRule.forId(i);
			if (selectedDuelRules[i]) {
				if (rule.getEquipmentSlot() < 0)
					continue;
				if (player.getEquipment().getItems()[rule.getEquipmentSlot()].getId() > 0) {
					equipItem = new Item(player.getEquipment().getItems()[rule.getEquipmentSlot()].getId(),
							player.getEquipment().getItems()[rule.getEquipmentSlot()].getAmount());
					player.getEquipment().delete(equipItem);
					player.getInventory().add(equipItem);
				}
			}
		}
		if (selectedDuelRules[DuelRule.NO_WEAPON.ordinal()] || selectedDuelRules[DuelRule.NO_SHIELD.ordinal()]) {
			if (player.getEquipment().getItems()[Equipment.WEAPON_SLOT].getId() > 0) {
				if (ItemDefinition.forId(player.getEquipment().getItems()[Equipment.WEAPON_SLOT].getId())
						.isTwoHanded()) {
					equipItem = new Item(player.getEquipment().getItems()[Equipment.WEAPON_SLOT].getId(),
							player.getEquipment().getItems()[Equipment.WEAPON_SLOT].getAmount());
					player.getEquipment().delete(equipItem);
					player.getInventory().add(equipItem);
				}
			}
		}
		equipItem = null;
		player.getInventory().refreshItems();
		player.getEquipment().refreshItems();
		duelingStatus = 5;
		player.getWalkingQueue().clear();
		player.getWalkingQueue().setLockMovement(true);
		player.getPacketSender().sendInterfaceRemoval();
		int INSTANCED_DUEL_LEAVEL = player.getIndex() + playerToDuel.getIndex() * 4;
		for (int i = 0; i < selectedDuelRules.length; i++) {
			player.lastDuelRules[i] = selectedDuelRules[i];
			playerToDuel.lastDuelRules[i] = selectedDuelRules[i];
		}
		if (selectedDuelRules[DuelRule.OBSTACLES.ordinal()]) {
			if (selectedDuelRules[DuelRule.NO_MOVEMENT.ordinal()]) {
				player.moveTo(duelTelePos);
			} else {
				player.moveTo(new Position(3366 + Misc.getRandom(12), 3246 + Misc.getRandom(6), 0));
			}
		} else {
			if (selectedDuelRules[DuelRule.NO_MOVEMENT.ordinal()]) {
				player.moveTo(duelTelePos);
			} else {
				player.moveTo(new Position(3335 + Misc.getRandom(12), 3246 + Misc.getRandom(6), 0));
			}
		}
		player.restart();
		player.getPacketSender().sendPositionalHint(playerToDuel.getPosition().copy(), 10);
		player.getPacketSender().sendEntityHint(playerToDuel);
		TaskManager.submit(new Task(2, player, false) {
			@Override
			public void execute() {
				if (player.getLocation() != Location.DUEL_ARENA) {
					player.getWalkingQueue().setLockMovement(false);
					stop();
					return;
				}
				if (timer == 3 || timer == 2 || timer == 1)
					player.forceChat("" + timer + "..");
				else {
					player.forceChat("FIGHT!!");
					player.getWalkingQueue().setLockMovement(false);
					timer = -1;
					stop();
					return;
				}
				timer--;
			}
		});
		player.getUpdateFlag().flag(Flag.APPEARANCE);
		BonusManager.update(player);
	}

	public void duelVictory() {
		boolean doubleDeath = player.getConstitution() == 0;
		duelingStatus = 6;
		player.restart();
		player.getWalkingQueue().clear();
		player.getWalkingQueue().setLockMovement(false);
		if (duelingWith > 0) {
			Player playerDuel = World.getPlayers().get(duelingWith);
			if (playerDuel != null && playerDuel.getDueling().stakedItems.size() > 0) {
				for (Item item : playerDuel.getDueling().stakedItems) {
					if (item.getId() > 0 && item.getAmount() > 0) {
						if(doubleDeath) {
							playerDuel.getInventory().add(item);
						} else {
							stakedItems.add(item);
							PlayerLogs.stake(player, playerDuel, item, playerDuel.getUsername());
						}
					}
				}
				if(doubleDeath) {
					playerDuel.getPacketSender().sendMessage("Your staked items have been returned because of a tie.");
					player.getPacketSender().sendMessage("Your staked items have been returned because of a tie.");
				} else {
					player.getPacketSender().sendMessage("You won the duel! Congratulations!");
					playerDuel.getPacketSender().sendMessage("You've lost the duel.");
				}
			}
		}
		player.getPacketSender().sendInterfaceItems(6822, stakedItems);
		player.getPacketSender().sendString(6840, "" + duelingData[0]);
		player.getPacketSender().sendString(6839, "" + duelingData[1]);
		player.getSession().clearMessages();
		player.moveTo(new Position(3368 + Misc.getRandom(5), 3267 + Misc.getRandom(3), 0));
		for (Item item : stakedItems) {
			if (item.getId() > 0 && item.getAmount() > 0) {
				if (item.getId() == 18016) { // shards will always be converted
												// to gp...
					player.setMoneyInPouch((player.getMoneyInPouch() + ((long) item.getAmount() * 25)));
					player.getPacketSender().sendString(8135, "" + player.getMoneyInPouch());
					player.getPacketSender().sendString(1, ":moneypouchearning:" + ((long) item.getAmount() * 25) + "");
					player.getPacketSender().sendMessage("The stack of " + Misc.formatAmount(item.getAmount())
							+ " shards has been converted into gp. " + Misc.formatAmount((long) item.getAmount() * 25)
							+ " has been added to your pouch.");
				} else
					player.getInventory().add(item);
				Player playerDuel = World.getPlayers().get(duelingWith);
				PlayerLogs.stake(player, playerDuel, item, player.getUsername());
			}
		}
		reset();
		arenaStats[0]++;
		player.setEntityInteraction(null);
		player.getWalkingQueue().clear();
		player.getPacketSender().sendInterface(6733);
		player.getPointsHandler().refreshPanel();
	}

	public static boolean checkDuel(Player playerToDuel, int statusReq) {
		boolean goodInterfaceId = playerToDuel.getInterfaceId() == -1 || playerToDuel.getInterfaceId() == 6575
				|| playerToDuel.getInterfaceId() == 6412;
		if (playerToDuel.getDueling().duelingStatus != statusReq || playerToDuel.isBanking()
				|| playerToDuel.isShopping() || playerToDuel.getConstitution() <= 0 || playerToDuel.isResting()
				|| !goodInterfaceId)
			return false;
		return true;
	}

	public static boolean checkRule(Player player, DuelRule rule) {
		if (player.getLocation() == Location.DUEL_ARENA && player.getDueling().duelingStatus == 5) {
			if (player.getDueling().selectedDuelRules[rule.ordinal()])
				return true;
		}
		return false;
	}

	public void setLastDuelRules(Player player) {
		for (int i = 0; i < player.lastDuelRules.length; i++) {
			if (player.lastDuelRules[i] == true)
				player.getDueling().selectRule(DuelRule.forId(i));
		}
	}

	public void reset() {
		inDuelWith = -1;
		duelingStatus = 0;
		inDuelScreen = false;
		duelRequested = false;
		canOffer = false;
		for (int i = 0; i < selectedDuelRules.length; i++) {
			selectedDuelRules[i] = false;
		}
		player.getTrading().setCanOffer(true);
		player.getPacketSender().sendConfig(286, 0);
		stakedItems.clear();
		if (duelingWith >= 0) {
			Player playerToDuel = World.getPlayers().get(duelingWith);
			if (playerToDuel != null) {
				player.getPacketSender().sendInterfaceItems(6670, playerToDuel.getDueling().stakedItems);
				playerToDuel.getPacketSender().sendInterfaceItems(6670, player.getDueling().stakedItems);
			}
			player.getPacketSender().sendInterfaceItems(6669, player.getDueling().stakedItems);
		}
		duelingWith = -1;
		duelConfig = 0;
		duelTelePos = null;
		timer = 3;
		player.getCombatBuilder().reset(true);
		player.getWalkingQueue().clear();
		player.getPacketSender().sendEntityHintRemoval(true);
	}

	public boolean getCanOffer() {
		return canOffer && player.getInterfaceId() == 6575 && !player.isBanking() && !player.getPriceChecker().isOpen();
	}

	public int duelingStatus = 0;
	public int duelingWith = -1;
	public boolean inDuelScreen = false;
	public boolean duelRequested = false;
	public boolean[] selectedDuelRules = new boolean[DuelRule.values().length];
	public CopyOnWriteArrayList<Item> stakedItems = new CopyOnWriteArrayList<Item>();
	public int arenaStats[] = { 0, 0 };
	public int spaceReq = 0;
	public int duelConfig;
	public int timer = 3;
	public int inDuelWith = -1;
	private boolean canOffer;

	public Object[] duelingData = new Object[2];
	protected Position duelTelePos = null;

	public static enum DuelRule {
		NO_RANGED(16, 6725, -1, -1), NO_MELEE(32, 6726, -1, -1), NO_MAGIC(64, 6727, -1, -1), NO_SPECIAL_ATTACKS(8192,
				7816, -1, -1), LOCK_WEAPON(4096, 670, -1, -1), NO_FORFEIT(1, 6721, -1, -1), NO_POTIONS(128, 6728, -1,
						-1), NO_FOOD(256, 6729, -1, -1), NO_PRAYER(512, 6730, -1,
								-1), NO_MOVEMENT(2, 6722, -1, -1), OBSTACLES(1024, 6732, -1, -1),

		NO_HELM(/* 16384 */643, 13813, 1, Equipment.HEAD_SLOT), NO_CAPE(/* 32768 */644, 13814, 1,
				Equipment.CAPE_SLOT), NO_AMULET(/* 65536 */645, 13815, 1, Equipment.AMULET_SLOT), NO_AMMUNITION(
						/* 134217728 */656, 13816, 1, Equipment.AMMUNITION_SLOT), NO_WEAPON(/* 131072 */646, 13817, 1,
								Equipment.WEAPON_SLOT), NO_BODY(/* 262144 */647, 13818, 1,
										Equipment.BODY_SLOT), NO_SHIELD(/* 524288 */648, 13819, 1,
												Equipment.SHIELD_SLOT), NO_LEGS(/* 2097152 */650, 13820, 1,
														Equipment.LEG_SLOT), NO_RING(/* 67108864 */655, 13821, 1,
																Equipment.RING_SLOT), NO_BOOTS(/* 16777216 */653, 13822,
																		1, Equipment.FEET_SLOT), NO_GLOVES(
																				/* 8388608 */652, 13823, 1,
																				Equipment.HANDS_SLOT);

		DuelRule(int configId, int buttonId, int inventorySpaceReq, int equipmentSlot) {
			this.configId = configId;
			this.buttonId = buttonId;
			this.inventorySpaceReq = inventorySpaceReq;
			this.equipmentSlot = equipmentSlot;
		}

		private int configId;
		private int buttonId;
		private int inventorySpaceReq;
		private int equipmentSlot;

		public int getConfigId() {
			return configId;
		}

		public int getButtonId() {
			return this.buttonId;
		}

		public int getInventorySpaceReq() {
			return this.inventorySpaceReq;
		}

		public int getEquipmentSlot() {
			return this.equipmentSlot;
		}

		public static DuelRule forId(int i) {
			for (DuelRule r : DuelRule.values()) {
				if (r.ordinal() == i)
					return r;
			}
			return null;
		}

		static DuelRule forButtonId(int buttonId) {
			for (DuelRule r : DuelRule.values()) {
				if (r.getButtonId() == buttonId)
					return r;
			}
			return null;
		}

		@Override
		public String toString() {
			return Misc.formatText(this.name().toLowerCase());
		}
	}

	public static final int INTERFACE_REMOVAL_ID = 6669;
}
