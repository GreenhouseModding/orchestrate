package dev.greenhouseteam.orchestrate;

import dev.greenhouseteam.mib.event.MibInstrumentEvents;
import dev.greenhouseteam.orchestrate.network.clientbound.OrchestrateStartPlayingClientboundPacket;
import dev.greenhouseteam.orchestrate.network.serverbound.OrchestrateStartPlayingServerboundPacket;
import dev.greenhouseteam.orchestrate.registry.OrchestrateSoundEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;

public class OrchestrateFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Orchestrate.init();
        OrchestrateSoundEvents.registerAll(Registry::register);
        registerEvents();
        registerNetwork();
    }

    public static void registerEvents() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (player.getCooldowns().isOnCooldown(stack.getItem()))
                return InteractionResultHolder.pass(stack);
            InteractionResultHolder<ItemStack> result = Orchestrate.playSong(player, stack, hand);
            if (result.getResult().consumesAction())
                Orchestrate.getHelper().sendServerboundPacket(new OrchestrateStartPlayingServerboundPacket(stack, hand == InteractionHand.OFF_HAND));
            return result;
        });

        // TODO: Test code, replace with properly configured code as soon as it's done.
        MibInstrumentEvents.COOLDOWN.register((stack, entity, original) -> 20);
        MibInstrumentEvents.USE_DURATION.register((stack, entity, original) -> Orchestrate.createTestSong().duration());
    }

    public static void registerNetwork() {
        PayloadTypeRegistry.playS2C().register(OrchestrateStartPlayingClientboundPacket.TYPE, OrchestrateStartPlayingClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(OrchestrateStartPlayingServerboundPacket.TYPE, OrchestrateStartPlayingServerboundPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(OrchestrateStartPlayingServerboundPacket.TYPE, (payload, context) -> payload.handle(context.player()));
    }
}
