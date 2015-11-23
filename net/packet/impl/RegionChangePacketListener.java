package com.strattus.net.packet.impl;

import com.strattus.engine.task.Task;
import com.strattus.engine.task.TaskManager;
import com.strattus.model.RegionInstance.RegionInstanceType;
import com.strattus.net.packet.Packet;
import com.strattus.net.packet.PacketListener;
import com.strattus.world.clip.region.RegionClipping;
import com.strattus.world.content.CustomObjects;
import com.strattus.world.content.Sounds;
import com.strattus.world.entity.impl.GroundItemManager;
import com.strattus.world.entity.impl.npc.NPC;
import com.strattus.world.entity.impl.player.Player;
import com.strattus.world.entity.updating.NPCUpdating;


public class RegionChangePacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		if(player.isAllowRegionChangePacket()) {
			RegionClipping.loadRegion(player.getPosition().getX(), player.getPosition().getY());
			player.getPacketSender().sendMapRegion();
			CustomObjects.handleRegionChange(player);
			GroundItemManager.handleRegionChange(player);
			Sounds.handleRegionChange(player);
			player.getTolerance().reset();
			//Hunter.handleRegionChange(player);
			if(player.getRegionInstance() != null && player.getPosition().getX() != 1 && player.getPosition().getY() != 1) {
				if(player.getRegionInstance().equals(RegionInstanceType.BARROWS) || player.getRegionInstance().equals(RegionInstanceType.WARRIORS_GUILD))
					player.getRegionInstance().destruct();
			}
			
			/** NPC FACING **/
			TaskManager.submit(new Task(1, player, false) {
				@Override
				protected void execute() {
					for(NPC npc : player.getLocalNpcs()) {
						if(npc == null || npc.getMovementCoordinator().getCoordinator().isCoordinate())
							continue;
						NPCUpdating.updateFacing(player, npc);
					}
					stop();
				}
			});
			
			player.setRegionChange(false).setAllowRegionChangePacket(false);
		}
	}
}
