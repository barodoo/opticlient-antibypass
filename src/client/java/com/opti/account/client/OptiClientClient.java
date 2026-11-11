package com.opti.account.client;

import net.fabricmc.api.ClientModInitializer;

public class OptiClientClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		SignInGate.init();
	}
}