package com.runelive.model.action.distance;

import com.runelive.model.Direction;
import com.runelive.model.Position;
import com.runelive.model.action.PlayerAction;
import com.runelive.model.movement.PathFinder;
import com.runelive.model.movement.WalkingQueue;
import com.runelive.world.World;
import com.runelive.world.clip.region.Region;
import com.runelive.world.content.Area;
import com.runelive.world.content.combat.CombatFactory;
import com.runelive.world.entity.impl.Character;
import com.runelive.world.entity.impl.player.Player;

public final class CombatFollowMobileAction extends PlayerAction {
	private Character following;
	private int containsCount;

	public CombatFollowMobileAction(Player player, Character following) {
		super(player);
		this.following = following;
	}

	@Override
	public ActionPolicy getActionPolicy() {
		return ActionPolicy.CLEAR;
	}

	@Override
	public void initialize() {
		if (player.distance(following) == 0) {
			player.getWalkingQueue().clear();
		} else {
			player.getWalkingQueue().deleteSteps(following);
		}
	}

	@Override
	public int execute() {
		WalkingQueue walkingQueue = player.getWalkingQueue();
		Area destination = Area.create(following.getLastPosition(), following.getSize() - 1);
		if (walkingQueue.deleteSteps(destination)) {
			return 1;
		}
		Position position = player.getPosition();
		Direction walk = this.getNextFollowPoint(position, destination, CombatFactory.getNewDistance(player));
		if (walk == null) {
			return STOP;
		}
		if (walk == Direction.NONE) {
			return 1;
		}
		position = position.transform(walk.getX(), walk.getY());
		walkingQueue.addStepInternal(position.getX(), position.getY());
		if (walkingQueue.isRunning()) {
			Direction run = this.getNextFollowPoint(position, destination, CombatFactory.getNewDistance(player));
			//System.out.println("Direction: "+run);
			if (run == null) {
				return STOP;
			}
			if (run == Direction.NONE) {
				return 1;
			}
			walkingQueue.addStepInternal(position.getX() + run.getX(), position.getY() + run.getY());
		}
		return 1;
	}

	@Override
	public void stop() {
		super.stop();
	}

	@Override
	public void dispose() {
		following = null;
	}

	private Direction getNextFollowPoint(Position position, Area destination, int distance) {
		int x = position.getX();
		int y = position.getY();
		int lastX = destination.getX();
		int lastY = destination.getY();
		if (destination.outsideMapRange(x, y)) {
			return null;
		}
		/*if (destination.contains(x, y)) {//Cause of dancing.
			Direction direction = position.moveAwayFrom(destination, containsCount % 4);
			if (direction == Direction.NONE || destination.getSize() == 1 || destination.getX() + direction.getX() == player.getLastPosition().getX() && destination.getY() + direction.getY() == player.getLastPosition().getY()) {
				containsCount += new Random().nextInt(3) + 1;
			}
			return direction;
		}*/
		containsCount = 0;
		int distanceTo = player.distance(following, true);
		if (distanceTo == 1) {
			return Direction.NONE;
		}
		if (distance > 1) {
			if (distanceTo <= distance && Region.canMagicAttack(player, following) && Region.canMagicAttack(following, player)) {
				return Direction.NONE;
			}
			if (player.isFrozen()) {
				player.getPacketSender().sendMessage("You can't reach that!");
				this.stop();
				return Direction.NONE;
			}
		}
		int diffX = lastX - (x = position.getX());
		int diffY = lastY - (y = position.getY());
		Direction dirX = Direction.direction(diffX, 0);
		Direction dirY = Direction.direction(0, diffY);
		if (dirX == Direction.NONE && dirY == Direction.NONE) {
			return Direction.NONE;
		}
		int z = position.getZ() % 4;
		if (World.directionBlocked(dirX, z, x, y, following.getSize())) {
			dirX = Direction.NONE;
		}
		if (World.directionBlocked(dirY, z, x, y, following.getSize())) {
			dirY = Direction.NONE;
		}
		if (dirX == Direction.NONE || dirY == Direction.NONE) {
			PathFinder.calculatePath(player, following.getPosition().getX(), following.getPosition().getY(), 1, 1, true);
		}
		if (dirX == Direction.NONE) {
			return dirY;
		}
		if (dirY == Direction.NONE) {
			return dirX;
		}
		Direction direction = Direction.direction(dirX.getX(), dirY.getY());
		if (World.directionBlocked(direction, z, x, y, following.getSize())) {
			if (Math.abs(diffX) >= Math.abs(diffY)) {
				return dirX;
			} else {
				return dirY;
			}
		}
		return direction;
	}
}