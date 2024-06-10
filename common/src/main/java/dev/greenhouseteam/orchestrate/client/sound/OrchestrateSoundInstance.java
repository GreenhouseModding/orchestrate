package dev.greenhouseteam.orchestrate.client.sound;

import dev.greenhouseteam.mib.client.sound.MibSoundInstance;
import dev.greenhouseteam.mib.data.ExtendedSound;
import dev.greenhouseteam.mib.data.KeyWithOctave;
import dev.greenhouseteam.mib.data.MibSoundSet;
import dev.greenhouseteam.mib.mixin.client.SoundEngineAccessor;
import dev.greenhouseteam.mib.mixin.client.SoundManagerAccessor;
import dev.greenhouseteam.orchestrate.registry.OrchestrateSoundEvents;
import dev.greenhouseteam.orchestrate.song.Note;
import dev.greenhouseteam.orchestrate.song.Song;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class OrchestrateSoundInstance extends MibSoundInstance {
    @Nullable
    private final OrchestrateSoundInstance rootSound;
    private final List<OrchestrateSoundInstance> children = new ArrayList<>();

    private final Song song;
    private final MibSoundSet soundSet;
    private final int duration;
    private int elapsedTicks;

    protected OrchestrateSoundInstance(@Nullable OrchestrateSoundInstance rootSound,
                                       double x, double y, double z, SoundEvent sound,
                                       Song song, MibSoundSet soundSet,
                                       ExtendedSound extendedSound,
                                       float volume, float pitch, int duration, int elapsedDuration,
                                       boolean isLooping, boolean shouldPlayLoopSound, boolean shouldFade) {
        this(rootSound, null, x, y, z, p -> true, sound, song, soundSet, extendedSound, volume, pitch, duration, elapsedDuration, isLooping, shouldPlayLoopSound, shouldFade);
    }

    protected OrchestrateSoundInstance(@Nullable OrchestrateSoundInstance masterSound,
                                       LivingEntity living, ItemStack stack, SoundEvent sound, Song song,
                                       MibSoundSet soundSet, ExtendedSound extendedSound,
                                       float volume, float pitch, int duration, int elapsedDuration,
                                       boolean isLooping, boolean shouldPlayLoopSound, boolean shouldFade) {
        this(masterSound, living, living.getX(), living.getY(), living.getZ(), p -> !p.isUsingItem() || p.getUseItem() != stack, sound, song, soundSet, extendedSound, volume, pitch, duration, elapsedDuration, isLooping, shouldPlayLoopSound, shouldFade);
    }

    protected OrchestrateSoundInstance(@Nullable OrchestrateSoundInstance rootSound,
                                       @Nullable LivingEntity living,
                                       double x, double y, double z,
                                       Predicate<LivingEntity> stopPredicate,
                                       SoundEvent sound,
                                       Song song, MibSoundSet soundSet, ExtendedSound extendedSound,
                                       float volume, float pitch, int duration, int elapsedDuration,
                                       boolean isLooping, boolean shouldPlayLoopSound, boolean shouldFade) {
        super(living, x, y, z, stopPredicate, sound, extendedSound, volume, pitch, isLooping, shouldPlayLoopSound, shouldFade);
        this.rootSound = rootSound;
        if (rootSound != null)
            rootSound.children.add(this);
        this.song = song;
        this.soundSet = soundSet;
        this.duration = duration;
        this.elapsedTicks = elapsedDuration;
    }

    public static OrchestrateSoundInstance createEntityDependentMaster(LivingEntity living, ItemStack stack,
                                                                       Song song, MibSoundSet soundSet) {
        return createEntityDependentMaster(living, stack, song, soundSet, 0);
    }


    public static OrchestrateSoundInstance createEntityDependentMaster(LivingEntity living, ItemStack stack,
                                                                       Song song, MibSoundSet soundSet,
                                                                       int elapsedDuration) {
        return new OrchestrateSoundInstance(null,
                living, stack, OrchestrateSoundEvents.MASTER,
                song, soundSet, soundSet.getSound(KeyWithOctave.DEFAULT, 1.0F),
                1.0F, 1.0F, song.duration(), elapsedDuration, false, false, false);
    }

    public boolean isMaster() {
        return rootSound == null;
    }

    @Override
    public void tick() {
        if (isStopped())
            return;

        if (handleMasterStop())
            return;
        if (handleStop())
            return;

        replaceWithLoopSoundIfNecessary();

        playNewNotes();

        if (living != null) {
            this.x = living.getX();
            this.y = living.getY();
            this.z = living.getZ();
        }

        if (shouldFade && extendedSound.fadeSpeed().isPresent())
            volume = Math.clamp(volume - (extendedSound.fadeSpeed().get().sample(random) * pitch * initialVolume), 0.0F, 1.0F);

        ++elapsedTicks;
    }

    public void playNewNotes() {
        if (isMaster()) {
            for (Note note : song.notes().stream().filter(note -> note.startTime() == elapsedTicks).toList()) {
                ExtendedSound sound = soundSet.getSound(note.key(), volume);
                if (sound == null)
                    return;
                float pitch = note.key().getPitchFromNote();
                Minecraft.getInstance().getSoundManager().queueTickingSound(new OrchestrateSoundInstance(this, living, x, y, z, stopPredicate, sound.sounds().start().value(), song, soundSet, sound, note.volume(), pitch, note.duration(), 0, false, true, false));
            }
        }
    }

    public void replaceWithLoopSoundIfNecessary() {
        SoundEngineAccessor soundEngine = ((SoundEngineAccessor) ((SoundManagerAccessor)Minecraft.getInstance().getSoundManager()).mib$getSoundEngine());
        if (!isMaster() && !hasPlayedLoop && getOrCalculateStartSoundStop() <= soundEngine.mib$getTickCount() && extendedSound.sounds().loop().isPresent()) {
            hasPlayedLoop = true;
            shouldPlayStopSound = false;
            elapsedTicks = 0;
            Minecraft.getInstance().getSoundManager().queueTickingSound(new OrchestrateSoundInstance(rootSound, living, x, y, z, stopPredicate, extendedSound.sounds().loop().orElse(extendedSound.sounds().start()).value(), song, soundSet, extendedSound, volume, pitch, duration, elapsedTicks, true, false, true));
            stop();
        }
    }

    public boolean handleStop() {
        if (elapsedTicks > duration || living != null && stopPredicate.test(living)) {
            if (shouldPlayStopSound && extendedSound.sounds().stop().isPresent())
                Minecraft.getInstance().getSoundManager().play(new MibSoundInstance(living, x, y, z, stopPredicate, extendedSound.sounds().stop().get().value(), extendedSound, this.volume, this.pitch, false, false, false));
            rootSound.children.remove(this);
            stop();
            elapsedTicks = 0;
            return true;
        }
        return false;
    }

    public boolean handleMasterStop() {
        if (isMaster() && (elapsedTicks > duration || living != null && stopPredicate.test(living))) {
            stopMaster();
            return true;
        }
        return false;
    }

    public void stopMaster() {
        for (OrchestrateSoundInstance child : children) {
            child.children.remove(this);
            child.stop();
        }
        stop();
        elapsedTicks = 0;
    }

    @Override
    public boolean canPlaySound() {
        return (elapsedTicks <= duration || living == null || !stopPredicate.test(living));
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
