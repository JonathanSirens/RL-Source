package com.ikov.world.content.skill.impl.summoning;

import com.ikov.world.entity.impl.npc.NPC;
import com.ikov.world.entity.impl.player.Player;

/**
 * This class model acts as an interface for a player's familiar. Class auto generated by Eclipse
 * 
 * @author Gabriel Hannason
 */
public class Familiar {

  public Familiar(Player owner, NPC summonNpc, int deathTimer) {
    this.owner = owner;
    this.summonNpc = summonNpc;
    this.deathTimer = deathTimer;
  }

  public Familiar(Player owner, NPC summonNpc) {
    this.owner = owner;
    this.summonNpc = summonNpc;
    this.petNpc = true;
    this.deathTimer = -1;
  }

  public Player getOwner() {
    return owner;
  }

  public void setOwner(Player owner) {
    this.owner = owner;
  }

  public NPC getSummonNpc() {
    return summonNpc;
  }

  public void setSummonNpc(NPC summonNpc) {
    this.summonNpc = summonNpc;
  }

  public int getDeathTimer() {
    return deathTimer;
  }

  public void setDeathTimer(int deathTimer) {
    this.deathTimer = deathTimer;
  }

  public boolean isRespawnNeeded() {
    return respawnNeeded;
  }

  public void setRespawnNeeded(boolean respawnNeeded) {
    this.respawnNeeded = respawnNeeded;
  }

  public boolean isPet() {
    return petNpc;
  }

  private boolean petNpc;
  private Player owner;
  private NPC summonNpc;
  private int deathTimer;
  private boolean respawnNeeded;
}
