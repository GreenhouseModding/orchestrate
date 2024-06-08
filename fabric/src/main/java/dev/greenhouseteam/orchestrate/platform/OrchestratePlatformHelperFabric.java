package dev.greenhouseteam.orchestrate.platform;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

public class OrchestratePlatformHelperFabric implements OrchestratePlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public void sendTrackingClientboundPacket(CustomPacketPayload payload, Entity entity) {
        Collection<ServerPlayer> players = PlayerLookup.tracking(entity);
        for (ServerPlayer other : players) {
            ServerPlayNetworking.send(other, payload);
        }
        if (entity instanceof ServerPlayer player && !players.contains(player))
            ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendServerboundPacket(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
