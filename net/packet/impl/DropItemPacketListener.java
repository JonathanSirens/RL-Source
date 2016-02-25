package com.ikov.net.packet.impl;

import com.ikov.model.CombatIcon;
import com.ikov.model.Graphic;
import com.ikov.model.GroundItem;
import com.ikov.model.Hit;
import com.ikov.model.Hitmask;
import com.ikov.model.Item;
import com.ikov.net.packet.Packet;
import com.ikov.net.packet.PacketListener;
import com.ikov.world.content.BankPin;
import com.ikov.world.content.PlayerLogs;
import com.ikov.world.content.Sounds;
import com.ikov.world.content.Sounds.Sound;
import com.ikov.world.content.skill.impl.dungeoneering.ItemBinding;
import com.ikov.world.entity.impl.GroundItemManager;
import com.ikov.world.entity.impl.player.Player;
import com.ikov.world.content.skill.impl.dungeoneering.Dungeoneering;
import com.ikov.model.Locations.Location;

/**
 * This packet listener is called when a player drops an item they
 * have placed in their inventory.
 * 
 * @author relex lawl
 */

public class DropItemPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		int id = packet.readUnsignedShortA();
		@SuppressWarnings("unused")
		int interfaceIndex = packet.readUnsignedShort();
		int itemSlot = packet.readUnsignedShortA();
		if (player.getConstitution() <= 0 || player.getInterfaceId() > 0)
			return;
		if(itemSlot < 0 || itemSlot > player.getInventory().capacity())
			return;
		if(player.getConstitution() <= 0 || player.isTeleporting())
			return;
		Item item = player.getInventory().getItems()[itemSlot];
		if(item.getId() != id) {
			return;
		}
		if(player.getBankPinAttributes().hasBankPin() && !player.getBankPinAttributes().hasEnteredBankPin() && player.getBankPinAttributes().onDifferent(player)) {
			BankPin.init(player, false);
			return;
		}
		player.getPacketSender().sendInterfaceRemoval();
		player.getCombatBuilder().cooldown(false);
		if (item != null && item.getId() != -1 && item.getAmount() >= 1) {
			if(item.tradeable() && !ItemBinding.isBoundItem(item.getId())) {
				player.getInventory().setItem(itemSlot, new Item(-1, 0)).refreshItems();
				if(item.getId() == 4045) {
					player.dealDamage(new Hit((player.getConstitution() - 1) == 0 ? 1 : player.getConstitution() - 1, Hitmask.CRITICAL, CombatIcon.BLUE_SHIELD));
					player.performGraphic(new Graphic(1750));
					player.getPacketSender().sendMessage("The potion explodes in your face as you drop it!");
				} else {
					if(player.getLocation() != Location.DUNGEONEERING) {
						if(item.getDefinition().getName().contains("Primal")) {
							player.getInventory().setItem(itemSlot, new Item(-1, 0)).refreshItems();
							player.getPacketSender().sendMessage("You cannot have primal outside of dungeoneering...");
							return;
						}
					}
					if(Dungeoneering.doingDungeoneering(player) && player.getLocation() != Location.DUNGEONEERING) {
						player.getPacketSender().sendMessage("You can't drop this item outside of a dungeon!");
						return;
					}
					GroundItemManager.spawnGroundItem(player, new GroundItem(item, player.getPosition().copy(), player.getUsername(), player.getHostAddress(), false, 80, player.getPosition().getZ() >= 0 && player.getPosition().getZ() < 4 ? true : false, 80));
					PlayerLogs.log(player.getUsername(), "Player dropping item: "+item.getDefinition().getName()+" ("+item.getId()+"), amount: "+item.getAmount() + " from the computer address: " + player.getLastComputerAddress());
					player.save();
				}
				Sounds.sendSound(player, Sound.DROP_ITEM);
			} else
				destroyItemInterface(player, item);
		}
	}

	public static void destroyItemInterface(Player player, Item item) {//Destroy item created by Remco
		player.setUntradeableDropItem(item);
		String[][] info = {//The info the dialogue gives
				{ "Are you sure you want to discard this item?", "14174" },
				{ "Yes.", "14175" }, { "No.", "14176" }, { "", "14177" },
				{"This item will vanish once it hits the floor.", "14182" }, {"You cannot get it back if discarded.", "14183" },
				{ item.getDefinition().getName(), "14184" } };
		player.getPacketSender().sendItemOnInterface(14171, item.getId(), 0, item.getAmount());
		for (int i = 0; i < info.length; i++)
			player.getPacketSender().sendString(Integer.parseInt(info[i][1]), info[i][0]);
		player.getPacketSender().sendChatboxInterface(14170);
	}
}
