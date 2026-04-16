package io.github.jasminecrash.looksmaxxing;

import io.github.jasminecrash.looksmaxxing.networking.ServerPacketReceivers;
import io.github.jasminecrash.looksmaxxing.networking.ModPackets;
import io.github.jasminecrash.looksmaxxing.rts_mechanics.RTSCommandTypes;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Looksmaxxing implements ModInitializer {
	public static final String MOD_ID = "looksmaxxing";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	//TODO: need to do networking :(
	//TODO: figure out how to re-prioritize mob goals (look into mob brain system)
	//TODO: in-world floating HUD? (for issuing commands to enthralled mobs)
	//TODO: look into interface injection
	//TODO: figure out exactly what happens to players that you attempt to enthrall

	@Override
	public void onInitialize() {
		ModDataAttachments.registerAttachments();
		ModPackets.registerPackets();
		ServerPacketReceivers.registerServerReceivers();
		RTSCommandTypes.registerRTSCommandTypes();
		ModCommands.registerCommands();
		ModEvents.registerServerEvents();
		LOGGER.info("[" + MOD_ID + "] *winks at u*");
	}
}