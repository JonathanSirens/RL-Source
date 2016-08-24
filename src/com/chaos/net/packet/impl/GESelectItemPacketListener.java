package com.chaos.net.packet.impl;

import com.chaos.GameSettings;
import com.chaos.model.Item;
import com.chaos.model.definitions.ItemDefinition;
import com.chaos.net.packet.Packet;
import com.chaos.net.packet.PacketListener;
import com.chaos.world.content.grandexchange.GrandExchange;
import com.chaos.world.entity.impl.player.Player;

public class GESelectItemPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		int item = packet.readShort();
		if (item <= 0 || item >= ItemDefinition.getMaxAmountOfItems())
			return;
		ItemDefinition def = ItemDefinition.forId(item);
		if (def != null) {
			if (def.getValue() <= 0 || !Item.tradeable(item) || item == 995) {
				player.getPacketSender()
						.sendMessage("This item can currently not be purchased or sold in the Grand Exchange.");
				return;
			}
			if (GameSettings.DEBUG_MODE) {
				// PlayerLogs.log(player,
				// "" + player.getUsername() + " in GESelectItemPacketListener:
				// " + item + "");
			}
			GrandExchange.setSelectedItem(player, item);
		}
	}

}