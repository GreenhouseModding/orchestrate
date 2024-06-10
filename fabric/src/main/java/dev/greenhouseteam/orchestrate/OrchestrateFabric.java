package dev.greenhouseteam.orchestrate;

import dev.greenhouseteam.mib.event.MibInstrumentEvents;
import dev.greenhouseteam.orchestrate.network.clientbound.OrchestrateStartPlayingClientboundPacket;
import dev.greenhouseteam.orchestrate.network.serverbound.OrchestrateStartPlayingServerboundPacket;
import dev.greenhouseteam.orchestrate.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
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
        MibInstrumentEvents.COOLDOWN.register((stack, entity, original) -> 20);
        MibInstrumentEvents.USE_DURATION.register((stack, entity, original) -> Orchestrate.createParticleAccelerator().duration() + 20);
    }

    public static void registerNetwork() {
        PayloadTypeRegistry.playS2C().register(OrchestrateStartPlayingClientboundPacket.TYPE, OrchestrateStartPlayingClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(OrchestrateStartPlayingServerboundPacket.TYPE, OrchestrateStartPlayingServerboundPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(OrchestrateStartPlayingServerboundPacket.TYPE, (payload, context) -> payload.handle(context.player()));
    }
}
