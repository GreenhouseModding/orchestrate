package dev.greenhouseteam.orchestrate;


import dev.greenhouseteam.mib.event.MibInstrumentEvents;
import dev.greenhouseteam.orchestrate.network.clientbound.OrchestrateStartPlayingClientboundPacket;
import dev.greenhouseteam.orchestrate.network.clientbound.UpdateSongClientboundPacket;
import dev.greenhouseteam.orchestrate.network.serverbound.OrchestrateStartPlayingServerboundPacket;
import dev.greenhouseteam.orchestrate.network.serverbound.UpdateAuthorServerboundPacket;
import dev.greenhouseteam.orchestrate.network.serverbound.UpdateNameServerboundPacket;
import dev.greenhouseteam.orchestrate.network.serverbound.UpdateNotesServerboundPacket;
import dev.greenhouseteam.orchestrate.platform.OrchestratePlatformHelperNeoForge;
import dev.greenhouseteam.orchestrate.registry.OrchestrateComponents;
import dev.greenhouseteam.orchestrate.registry.OrchestrateItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.Map;

@Mod(Orchestrate.MOD_ID)
public class OrchestrateNeoForge {
    public OrchestrateNeoForge(IEventBus eventBus) {
        Orchestrate.setHelper(new OrchestratePlatformHelperNeoForge());
    }

    @EventBusSubscriber(modid = Orchestrate.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
            event.registrar("1.0.0")
                    .playToClient(OrchestrateStartPlayingClientboundPacket.TYPE, OrchestrateStartPlayingClientboundPacket.STREAM_CODEC, (packet, context) -> packet.handle())
                    .playToClient(UpdateSongClientboundPacket.TYPE, UpdateSongClientboundPacket.STREAM_CODEC, (packet, context) -> packet.handle())
                    .playToServer(OrchestrateStartPlayingServerboundPacket.TYPE, OrchestrateStartPlayingServerboundPacket.STREAM_CODEC, (packet, context) -> packet.handle((ServerPlayer) context.player()))
                    .playToServer(UpdateAuthorServerboundPacket.TYPE, UpdateAuthorServerboundPacket.STREAM_CODEC, (packet, context) -> packet.handle((ServerPlayer) context.player()))
                    .playToServer(UpdateNameServerboundPacket.TYPE, UpdateNameServerboundPacket.STREAM_CODEC, (packet, context) -> packet.handle((ServerPlayer) context.player()))
                    .playToServer(UpdateNotesServerboundPacket.TYPE, UpdateNotesServerboundPacket.STREAM_CODEC, (packet, context) -> packet.handle((ServerPlayer) context.player()));
        }

        @SubscribeEvent
        public static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
            addAfter(event, Items.JUKEBOX, OrchestrateItems.COMPOSITION_TABLE);
        }

        @SubscribeEvent
        public static void onLivingUse(LivingEntityUseItemEvent.Start event) {
            InteractionResultHolder<ItemStack> result = Orchestrate.playSong(event.getEntity(), event.getEntity().getItemInHand(event.getEntity().getUsedItemHand()), event.getEntity().getUsedItemHand());
            if (result.getResult().consumesAction()) {
                Orchestrate.getHelper().sendServerboundPacket(new OrchestrateStartPlayingServerboundPacket(event.getEntity().getItemInHand(event.getEntity().getUsedItemHand()), event.getEntity().getUsedItemHand() == InteractionHand.OFF_HAND));
                event.setCanceled(true);
            }
        }

        // TODO: Test code, replace with properly configured code as soon as it's done.
        @SubscribeEvent
        public static void setInstrumentCooldown(MibInstrumentEvents.CooldownEvent event) {
            if (event.getStack().has(OrchestrateComponents.SONG))
                event.setValue(20);
        }

        // TODO: Test code, replace with properly configured code as soon as it's done.
        @SubscribeEvent
        public static void setInstrumentUseDuration(MibInstrumentEvents.UseDurationEvent event) {
            if (event.getStack().has(OrchestrateComponents.SONG))
                event.setValue(event.getStack().get(OrchestrateComponents.SONG).duration());
        }

        private static void addAfter(BuildCreativeModeTabContentsEvent event, Item startItem, Item newItem) {
            if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                ItemStack startStack = null;
                for (Map.Entry<ItemStack, CreativeModeTab.TabVisibility> entry : event.getEntries()) {
                    if (entry.getKey().is(startItem)) {
                        startStack = entry.getKey();
                        break;
                    }
                }
                event.getEntries().putAfter(startStack, new ItemStack(newItem), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
    }
}