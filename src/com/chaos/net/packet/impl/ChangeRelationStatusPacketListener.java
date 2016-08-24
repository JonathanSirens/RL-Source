package com.chaos.net.packet.impl;

import com.chaos.GameSettings;
import com.chaos.model.PlayerRelations.PrivateChatStatus;
import com.chaos.net.packet.Packet;
import com.chaos.net.packet.PacketListener;
import com.chaos.world.entity.impl.player.Player;

public class ChangeRelationStatusPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		int actionId = packet.readInt();
		if (GameSettings.DEBUG_MODE) {
			// PlayerLogs.log(player, "" + player.getUsername()
			// + " has changed their private status in
			// ChangeRelationStatusPacketListener");
		}
		player.getRelations().setStatus(PrivateChatStatus.forActionId(actionId), true);
	}

}