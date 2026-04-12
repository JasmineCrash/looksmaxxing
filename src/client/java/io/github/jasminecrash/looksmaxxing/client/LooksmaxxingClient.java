package io.github.jasminecrash.looksmaxxing.client;

import io.github.jasminecrash.looksmaxxing.client.render.FrustumRenderer;
import net.fabricmc.api.ClientModInitializer;

public class LooksmaxxingClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		new FrustumRenderer();
	}
}