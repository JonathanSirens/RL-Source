package com.runelive.loginserver;

import com.neo.net.packet.PacketSize;

public final class EncoderConstants {
	//Lets not touchthis then
	public static final PacketSize[] PACKET_SIZS = new PacketSize[] {
			PacketSize.FIXED, //if i have fixed its defined LS side too
			PacketSize.SHORT, //Now IF I have short the LS side value = -1 check it out
			PacketSize.SHORT,
			PacketSize.SHORT,
			PacketSize.SHORT,
			PacketSize.SHORT,
			PacketSize.FIXED,
			PacketSize.SHORT,
			PacketSize.SHORT,
			PacketSize.FIXED, // 9
			PacketSize.SHORT,
			PacketSize.FIXED,
			PacketSize.SHORT,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.SHORT,
			PacketSize.SHORT,
			PacketSize.SHORT,
			PacketSize.FIXED,
			PacketSize.SHORT, // yes19
			PacketSize.FIXED,
			PacketSize.SHORT,
			PacketSize.SHORT,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 20
			PacketSize.FIXED,
			PacketSize.FIXED,//null
			PacketSize.SHORT,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 30
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 40
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 50
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 60
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 70
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 80
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 90
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 100
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 120
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 130
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 140
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 150
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 160
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 170
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 180
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 190
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 200
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 210
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 220
			PacketSize.FIXED, PacketSize.FIXED, PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 230
			PacketSize.FIXED, PacketSize.FIXED, PacketSize.FIXED,
			PacketSize.FIXED, PacketSize.FIXED, PacketSize.FIXED,
			PacketSize.FIXED, PacketSize.FIXED,
			PacketSize.FIXED,
			PacketSize.FIXED, // 240
			PacketSize.FIXED, PacketSize.FIXED, PacketSize.FIXED,
			PacketSize.FIXED, PacketSize.FIXED, PacketSize.FIXED // 250
	};
}
