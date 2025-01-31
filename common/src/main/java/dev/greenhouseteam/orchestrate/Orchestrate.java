package dev.greenhouseteam.orchestrate;

import dev.greenhouseteam.mib.component.ItemInstrument;
import dev.greenhouseteam.mib.item.MibInstrumentItem;
import dev.greenhouseteam.mib.registry.MibDataComponents;
import dev.greenhouseteam.orchestrate.network.clientbound.OrchestrateStartPlayingClientboundPacket;
import dev.greenhouseteam.orchestrate.platform.OrchestratePlatformHelper;
import dev.greenhouseteam.orchestrate.registry.OrchestrateComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Orchestrate {
    public static final String MOD_ID = "orchestrate";
    public static final String MOD_NAME = "Orchestrate";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    private static OrchestratePlatformHelper helper;

    public static void init() {

    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void setHelper(OrchestratePlatformHelper helper) {
        Orchestrate.helper = helper;
    }

    public static OrchestratePlatformHelper getHelper() {
        return helper;
    }

    public static InteractionResultHolder<ItemStack> playSong(LivingEntity living, ItemStack stack, InteractionHand hand) {
        if (!(stack.getItem() instanceof MibInstrumentItem) || !stack.has(MibDataComponents.INSTRUMENT) || !stack.has(OrchestrateComponents.SONG))
            return InteractionResultHolder.pass(stack);

        ItemInstrument instrumentComponent = stack.get(MibDataComponents.INSTRUMENT);
        var instrument = instrumentComponent.sound().unwrap(living.level().registryAccess());
        if (instrument.isEmpty())
            return InteractionResultHolder.pass(stack);

        living.startUsingItem(hand);
        if (!living.level().isClientSide()) {
            Orchestrate.getHelper().sendTrackingClientboundPacket(new OrchestrateStartPlayingClientboundPacket(living.getId(), hand == InteractionHand.OFF_HAND, stack.get(OrchestrateComponents.SONG), instrument.get()), living);
            if (living instanceof Player player)
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        }
        return InteractionResultHolder.consume(stack);
    }
}