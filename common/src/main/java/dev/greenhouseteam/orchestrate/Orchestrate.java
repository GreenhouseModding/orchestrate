package dev.greenhouseteam.orchestrate;

import dev.greenhouseteam.mib.component.ItemInstrument;
import dev.greenhouseteam.mib.data.Key;
import dev.greenhouseteam.mib.data.KeyWithOctave;
import dev.greenhouseteam.mib.item.MibInstrumentItem;
import dev.greenhouseteam.mib.registry.MibComponents;
import dev.greenhouseteam.orchestrate.network.clientbound.OrchestrateStartPlayingClientboundPacket;
import dev.greenhouseteam.orchestrate.platform.OrchestratePlatformHelper;
import dev.greenhouseteam.orchestrate.song.Note;
import dev.greenhouseteam.orchestrate.song.Song;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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

    // TODO: Use new component for storing songs.
    public static InteractionResultHolder<ItemStack> playSong(Player player, ItemStack stack, InteractionHand hand) {
        if (!(stack.getItem() instanceof MibInstrumentItem) && !stack.has(MibComponents.INSTRUMENT))
            return InteractionResultHolder.pass(stack);

        ItemInstrument instrumentComponent = stack.get(MibComponents.INSTRUMENT);
        var instrument = instrumentComponent.sound().unwrap(player.level().registryAccess());
        if (instrument.isEmpty())
            return InteractionResultHolder.pass(stack);

        if (!player.level().isClientSide()) {
            player.startUsingItem(hand);
            Orchestrate.getHelper().sendTrackingClientboundPacket(new OrchestrateStartPlayingClientboundPacket(player.getId(), hand == InteractionHand.OFF_HAND, createTestSong(), instrument.get()), player);
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        }
        return InteractionResultHolder.consume(stack);
    }

    public static Song createTestSong() {
        Song.Builder builder = new Song.Builder();
        for (int i = 0; i < Key.values().length * 5; ++i) {
            int octave = (i / 12) + 1;
            int index = i % 12;
            builder.add(new Note(new KeyWithOctave(Key.values()[index], octave), 0.1F + ((float) i / Key.values().length), i, 3));
        }
        return builder.build();
    }
}