package com.chaos.engine.task.impl;

import com.chaos.engine.task.Task;
import com.chaos.model.Position;
import com.chaos.util.Misc;
import com.chaos.world.World;
import com.chaos.world.content.skill.impl.hunter.Hunter;
import com.chaos.world.entity.impl.npc.NPC;

public class NPCRespawnTask extends Task {

	public NPCRespawnTask(NPC npc, int respawn) {
		super(respawn);
		this.npc = npc;
	}

	final NPC npc;

	@Override
	public void execute() {
		NPC npc_ = new NPC(npc.getId(), npc.getDefaultPosition());
		npc_.walkingDistance = npc.walkingDistance;
		if (npc_.getId() == 8022 || npc_.getId() == 8028) { // Desospan, respawn
															// at random
															// locations
			npc_.moveTo(new Position(2595 + Misc.getRandom(12), 4772 + Misc.getRandom(8)));
		} else if (npc_.getId() > 5070 && npc_.getId() < 5081) {
			Hunter.HUNTER_NPC_LIST.add(npc_);
		}

		World.register(npc_);
		stop();
	}

}