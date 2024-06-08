package dev.greenhouseteam.orchestrate.client;

import dev.greenhouseteam.mib.network.clientbound.StartPlayingClientboundPacket;
import dev.greenhouseteam.orchestrate.network.clientbound.OrchestrateStartPlayingClientboundPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class OrchestrateFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(OrchestrateStartPlayingClientboundPacket.TYPE, (packet, context) -> packet.handle());
    }
}
