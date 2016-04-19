package com.runelive.net.packet.impl;

import com.runelive.engine.task.impl.WalkToTask;
import com.runelive.engine.task.impl.WalkToTask.FinalizedMovementTask;
import com.runelive.model.Locations;
import com.runelive.model.Locations.Location;
import com.runelive.net.packet.Packet;
import com.runelive.net.packet.PacketListener;
import com.runelive.world.World;
import com.runelive.world.entity.impl.player.Player;

/**
 * This packet listener is called when a player accepts a trade offer, whether it's from the chat
 * box or through the trading player menu.
 * 
 * @author relex lawl
 */

public class TradeInvitationPacketListener implements PacketListener {

  @Override
  public void handleMessage(Player player, Packet packet) {
    if (player.getConstitution() <= 0)
      return;
    if (player.isTeleporting())
      return;
    player.getSkillManager().stopSkilling();
    if (player.getLocation() == Location.FIGHT_PITS) {
      player.getPacketSender().sendMessage("You cannot trade other players here.");
      return;
    }
    int index =
        packet.getOpcode() == TRADE_OPCODE ? (packet.readShort() & 0xFF) : packet.readLEShort();
    if (index < 0 || index > World.getPlayers().capacity())
      return;
    Player target = World.getPlayers().get(index);
    if (target == null || !Locations.goodDistance(player.getPosition(), target.getPosition(), 13))
      return;
    player.setWalkToTask(
        new WalkToTask(player, target.getPosition(), target.getSize(), new FinalizedMovementTask() {
          @Override
          public void execute() {
            if (target.getIndex() != player.getIndex())
              player.getTrading().requestTrade(target);
          }
        }));
  }

  public static final int TRADE_OPCODE = 39;
  public static final int CHATBOX_TRADE_OPCODE = 139;
}
