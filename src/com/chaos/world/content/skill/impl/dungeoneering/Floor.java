package com.chaos.world.content.skill.impl.dungeoneering;

import com.chaos.world.entity.impl.npc.NPC;

/**
 * Handles the Global Floors
 * @Author Jonny
 */
public interface Floor {

    /**
     * Enters the dungeoneering floor
     */
    public void enterFloor();

    /**
     * Gets all the npc spawns for the floor
     * @return
     */
    public NPC[] getNpcs();

}
