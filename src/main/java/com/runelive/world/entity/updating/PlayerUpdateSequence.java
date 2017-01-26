package com.runelive.world.entity.updating;

import com.runelive.world.World;
import com.runelive.world.entity.impl.player.Player;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;

public class PlayerUpdateSequence implements UpdateSequence<Player> {

	/**
	 * Used to block the game thread until updating is completed.
	 */
	private final Phaser synchronizer;
	/**
	 * The thread pool that will update players in parallel.
	 */
	private final ExecutorService updateExecutor;

	/**
	 * Create a new {@link PlayerUpdateSequence}.
	 *
	 * @param synchronizer
	 *            used to block the game thread until updating is completed.
	 * @param updateExecutor
	 *            the thread pool that will update players in parallel.
	 */
	public PlayerUpdateSequence(Phaser synchronizer, ExecutorService updateExecutor) {
		this.synchronizer = synchronizer;
		this.updateExecutor = updateExecutor;
	}

	@Override
	public void executePreUpdate(Player t) {
		try {
			t.getSession().handleQueuedMessages();
			t.process();
			if (t.getWalkToTask() != null)
				t.getWalkToTask().tick();
			t.getActionQueue().processActions();
			t.getWalkingQueue().processNextMovement();
		} catch (Exception e) {
			e.printStackTrace();
			World.deregister(t);
		}
	}

	@Override
	public void executeUpdate(Player t) {
		updateExecutor.execute(() -> {
			try {
				synchronized (t) {
					PlayerUpdating.update(t);
					NPCUpdating.update(t);
				}
			} catch (Exception e) {
				e.printStackTrace();
				World.deregister(t);
			} finally {
				synchronizer.arriveAndDeregister();
			}
		});
	}

	@Override
	public void executePostUpdate(Player t) {
		try {
			PlayerUpdating.resetFlags(t);
		} catch (Exception e) {
			e.printStackTrace();
			World.deregister(t);
		}
	}
}