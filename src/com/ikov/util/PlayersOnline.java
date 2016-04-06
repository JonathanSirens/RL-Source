package com.ikov.util;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;
import com.ikov.GameServer;
import com.ikov.world.World;

public class PlayersOnline {

	private static final File FILE_LOCATION = new File("C:/inetpub/wwwroot/online.txt");
	
	public static void update() {
		GameServer.getLoader().getEngine().submit(() -> {
			try {
				PrintWriter printer = new PrintWriter(FILE_LOCATION);
				printer.print(""+World.getPlayers().size()+"");
				printer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
}