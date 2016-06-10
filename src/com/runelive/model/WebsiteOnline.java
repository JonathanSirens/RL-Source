package com.runelive.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.runelive.GameServer;
import com.runelive.net.mysql.SQLCallback;

public class WebsiteOnline {

	public static void updateOnline(int amountOnline) {
		GameServer.getCharacterPool().executeQuery("UPDATE `online` SET `amount`="+amountOnline+" WHERE 1", new SQLCallback() {
			@Override
			public void queryComplete(ResultSet rs) throws SQLException {
				//Query is complete
			}

			@Override
			public void queryError(SQLException e) {
				e.printStackTrace();
			}
		});
	}
}