package io.github.jasminecrash.looksmaxxing;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Looksmaxxing implements ModInitializer {
	public static final String MOD_ID = "looksmaxxing";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[" + MOD_ID + "] *winks at u*");
	}
}