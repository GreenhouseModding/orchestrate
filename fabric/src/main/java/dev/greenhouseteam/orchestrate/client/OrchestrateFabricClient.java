package dev.greenhouseteam.orchestrate.client;

import dev.greenhouseteam.orchestrate.client.screen.CompositionScreen;
import dev.greenhouseteam.orchestrate.network.clientbound.OrchestrateStartPlayingClientboundPacket;
import dev.greenhouseteam.orchestrate.registry.OrchestrateMenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;

public class OrchestrateFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(OrchestrateMenuTypes.COMPOSITION_TABLE, CompositionScreen::new);
        ClientPlayNetworking.registerGlobalReceiver(OrchestrateStartPlayingClientboundPacket.TYPE, (packet, context) -> packet.handle());
    }
}
