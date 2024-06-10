package dev.greenhouseteam.orchestrate.client.util;

import dev.greenhouseteam.mib.data.MibSoundSet;
import dev.greenhouseteam.orchestrate.client.sound.OrchestrateSoundInstance;
import dev.greenhouseteam.orchestrate.song.Song;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class OrchestrateClientUtil {
    public static final Sound EMPTY_SOUND_BUT_NOT_ACTUALLY = new Sound(SoundManager.INTENTIONALLY_EMPTY_SOUND_LOCATION, ConstantFloat.of(0.5F), ConstantFloat.of(0.5F), 1, Sound.Type.FILE, false, false, 1);

    public static void playSong(Player player, InteractionHand hand, Song song, MibSoundSet set) {
        Minecraft.getInstance().getSoundManager().play(
                OrchestrateSoundInstance.createEntityDependentMaster(player, player.getItemInHand(hand), song, set)
        );
    }
}
