package com.ikov.engine;

import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ikov.GamePanel;
import com.ikov.GameServer;
import com.ikov.engine.task.TaskManager;
import com.ikov.world.World;
import com.ikov.world.content.clan.ClanChatManager;
import com.ikov.world.content.grandexchange.GrandExchangeOffers;
import com.ikov.world.content.pos.PlayerOwnedShops;

/**
 * 
 * @author lare96
 * @author Gabriel Hannason
 */
public final class GameEngine implements Runnable {
	
	private final GamePanel panel = GameServer.getPanel();

	private final ScheduledExecutorService logicService = GameEngine.createLogicService();   

	//private static final int PROCESS_GAME_TICK = 2;

	//private EngineState engineState = EngineState.PACKET_PROCESSING;
	
	//private int engineTick = 0;
	
	@Override
	public void run() {
		try {
			/*switch(engineState) {
			case PACKET_PROCESSING:
				World.getPlayers().forEach($it -> $it.getSession().handlePrioritizedMessageQueue());
				break;
			case GAME_PROCESSING:
				TaskManager.sequence();
				World.sequence();
				break;
			}
			engineState = next();*/
			long start = System.currentTimeMillis();
			TaskManager.sequence();
			long task_start = System.currentTimeMillis();
			long taskCycle = task_start - start;
			World.sequence();
			long end = System.currentTimeMillis() - start;
			panel.addCycleTime(end);
			panel.addTaskCycle(taskCycle);
			panel.addGeneral();
		} catch (Throwable e) {
			e.printStackTrace();
			World.savePlayers();
			GrandExchangeOffers.save();
			PlayerOwnedShops.save();
			ClanChatManager.save();
		}
	}

	/*private EngineState next() {
		if (engineTick == PROCESS_GAME_TICK) {
			engineTick = 0;
			return EngineState.GAME_PROCESSING;
		}
		engineTick++;
		return EngineState.PACKET_PROCESSING;
	}

	private enum EngineState {
		PACKET_PROCESSING,
		GAME_PROCESSING;
	}*/

	public void submit(Runnable t) {
		try {
			logicService.execute(t);
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}

	/** STATIC **/

	public static ScheduledExecutorService createLogicService() {
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		executor.setRejectedExecutionHandler(new CallerRunsPolicy());
		executor.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("LogicServiceThread").build());
		executor.setKeepAliveTime(45, TimeUnit.SECONDS);
		executor.allowCoreThreadTimeOut(true);
		return Executors.unconfigurableScheduledExecutorService(executor);
	}
}