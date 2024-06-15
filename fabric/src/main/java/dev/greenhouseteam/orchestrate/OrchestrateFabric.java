package dev.greenhouseteam.orchestrate;

import dev.greenhouseteam.mib.event.MibInstrumentEvents;
import dev.greenhouseteam.orchestrate.network.clientbound.OrchestrateStartPlayingClientboundPacket;
import dev.greenhouseteam.orchestrate.network.clientbound.UpdateSongClientboundPacket;
import dev.greenhouseteam.orchestrate.network.serverbound.OrchestrateStartPlayingServerboundPacket;
import dev.greenhouseteam.orchestrate.network.serverbound.UpdateAuthorServerboundPacket;
import dev.greenhouseteam.orchestrate.network.serverbound.UpdateNameServerboundPacket;
import dev.greenhouseteam.orchestrate.network.serverbound.UpdateNotesServerboundPacket;
import dev.greenhouseteam.orchestrate.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class OrchestrateFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Orchestrate.init();
        registerContents();
        registerEvents();
        registerNetwork();
    }

    public static void registerContents() {
        OrchestrateBlocks.registerAll(Registry::register);
        OrchestrateBlockEntityTypes.registerAll(Registry::register);
        OrchestrateComponents.registerAll(Registry::register);
        OrchestrateItems.registerAll(Registry::register);
        OrchestrateMenuTypes.registerAll(Registry::register);
        OrchestrateSoundEvents.registerAll(Registry::register);
    }

    public static void registerEvents() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries ->
                entries.addAfter(Items.JUKEBOX, OrchestrateItems.COMPOSITION_TABLE));

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
        MibInstrumentEvents.COOLDOWN.register((stack, entity, original) -> stack.has(OrchestrateComponents.SONG) ? 20 : original);
        MibInstrumentEvents.USE_DURATION.register((stack, entity, original) -> stack.has(OrchestrateComponents.SONG) ? (int) stack.get(OrchestrateComponents.SONG).duration() + 20 : original);
    }

    public static void registerNetwork() {
        PayloadTypeRegistry.playS2C().register(OrchestrateStartPlayingClientboundPacket.TYPE, OrchestrateStartPlayingClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateSongClientboundPacket.TYPE, UpdateSongClientboundPacket.STREAM_CODEC);

        PayloadTypeRegistry.playC2S().register(OrchestrateStartPlayingServerboundPacket.TYPE, OrchestrateStartPlayingServerboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateAuthorServerboundPacket.TYPE, UpdateAuthorServerboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateNameServerboundPacket.TYPE, UpdateNameServerboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateNotesServerboundPacket.TYPE, UpdateNotesServerboundPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(OrchestrateStartPlayingServerboundPacket.TYPE, (payload, context) -> payload.handle(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(UpdateAuthorServerboundPacket.TYPE, (payload, context) -> payload.handle(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(UpdateNameServerboundPacket.TYPE, (payload, context) -> payload.handle(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(UpdateNotesServerboundPacket.TYPE, (payload, context) -> payload.handle(context.player()));
    }
}
