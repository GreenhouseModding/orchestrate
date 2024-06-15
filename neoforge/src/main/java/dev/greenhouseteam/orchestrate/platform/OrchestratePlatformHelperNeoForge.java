package dev.greenhouseteam.orchestrate.platform;

import dev.greenhouseteam.orchestrate.block.CompositionTableBlockEntity;
import dev.greenhouseteam.orchestrate.menu.CompositionMenu;
import dev.greenhouseteam.orchestrate.util.CompositionExtendedMenuFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;

public class OrchestratePlatformHelperNeoForge implements OrchestratePlatformHelper {

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public MenuType<CompositionMenu> createCompositionMenu() {
        return new MenuType<>(new CompositionExtendedMenuFactory(), FeatureFlags.VANILLA_SET);
    }

    @Override
    public void openCompositionMenu(ServerPlayer player, BlockPos tablePos) {
        player.openMenu((CompositionTableBlockEntity)player.level().getBlockEntity(tablePos), tablePos);
    }

    @Override
    public void sendClientboundPacket(CustomPacketPayload payload, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendTrackingClientboundPacket(CustomPacketPayload payload, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
        if (entity instanceof ServerPlayer player)
            PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendServerboundPacket(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}