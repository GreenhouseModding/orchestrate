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
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class OrchestrateSoundInstance extends MibSoundInstance {
    @Nullable
    private final OrchestrateSoundInstance rootSound;
    private final List<OrchestrateSoundInstance> children = new ArrayList<>();

    private final Song song;
    private final List<Note> remainingNotes;
    private final MibSoundSet soundSet;
    private final float duration;
    private float elapsedTicks;

    protected OrchestrateSoundInstance(@Nullable OrchestrateSoundInstance rootSound,
                                       double x, double y, double z, SoundEvent sound,
                                       Song song, MibSoundSet soundSet,
                                       ExtendedSound extendedSound,
                                       float volume, float pitch, float duration, float elapsedDuration,
                                       boolean isLooping, boolean shouldPlayLoopSound) {
        this(rootSound, null, x, y, z, p -> false, sound, song, soundSet, extendedSound, volume, pitch, duration, elapsedDuration, isLooping, shouldPlayLoopSound);
    }

    protected OrchestrateSoundInstance(@Nullable OrchestrateSoundInstance masterSound,
                                       LivingEntity living, ItemStack stack, SoundEvent sound, Song song,
                                       MibSoundSet soundSet, ExtendedSound extendedSound,
                                       float volume, float pitch, float duration, float elapsedDuration,
                                       boolean isLooping, boolean shouldPlayLoopSound) {
        this(masterSound, living, living.getX(), living.getY(), living.getZ(),
                p -> !p.isUsingItem() || p.getUseItem() != stack, sound, song, soundSet, extendedSound, volume, pitch, duration, elapsedDuration, isLooping, shouldPlayLoopSound);
    }

    protected OrchestrateSoundInstance(@Nullable OrchestrateSoundInstance rootSound,
                                       @Nullable LivingEntity living,
                                       double x, double y, double z,
                                       Predicate<LivingEntity> stopPredicate,
                                       SoundEvent sound,
                                       Song song, MibSoundSet soundSet, ExtendedSound extendedSound,
                                       float volume, float pitch, float duration, float elapsedDuration,
                                       boolean isLooping, boolean shouldPlayLoopSound) {
        super(living, x, y, z, stopPredicate, sound, extendedSound, volume, pitch, isLooping, shouldPlayLoopSound);
        this.rootSound = rootSound;
        if (rootSound != null)
            rootSound.children.add(this);
        this.song = song;
        this.remainingNotes = new ArrayList<>(song.notes());
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
                1.0F, 1.0F, song.duration(), elapsedDuration, false, false);
    }

    public boolean isMaster() {
        return rootSound == null;
    }

    @Override
    public void bypassingTick(long ticks, DeltaTracker delta) {
        if (handleMasterStop())
            return;

        replaceWithLoopSoundIfNecessary(ticks, delta);

        playNewNotes();

        if (handleStop())
            return;

        if (living != null) {
            this.x = living.getX();
            this.y = living.getY();
            this.z = living.getZ();
        }

        if (shouldFade && extendedSound.fadeSpeed().isPresent())
            volume = Math.clamp(volume - (extendedSound.fadeSpeed().get().sample(random) * pitch * initialVolume), 0.0F, 1.0F);

        elapsedTicks += delta.getGameTimeDeltaTicks();
    }

    public void playNewNotes() {
        if (isMaster()) {
            for (Note note : remainingNotes.stream().filter(note -> note.startTime() <= elapsedTicks).toList()) {
                remainingNotes.remove(note);
                ExtendedSound sound = soundSet.getSound(note.key(), volume);
                if (sound == null)
                    return;
                float pitch = note.key().getPitchFromNote();
                Minecraft.getInstance().getSoundManager().queueTickingSound(new OrchestrateSoundInstance(this, living, x, y, z, stopPredicate, sound.sounds().start().value(), song, soundSet, sound, note.volume(), pitch, note.duration(), 0, false, true));

            }
        }
    }

    public void replaceWithLoopSoundIfNecessary(long ticks, DeltaTracker delta) {
        if (!isMaster() && !hasPlayedLoop && getTickDuration(ticks, delta) - 0.2 <= ((float)ticks + delta.getGameTimeDeltaTicks()) && extendedSound.sounds().loop().isPresent()) {
            hasPlayedLoop = true;
            shouldPlayStopSound = false;
            elapsedTicks = 0;
            Minecraft.getInstance().getSoundManager().queueTickingSound(new OrchestrateSoundInstance(rootSound, living, x, y, z, stopPredicate, extendedSound.sounds().loop().orElse(extendedSound.sounds().start()).value(), song, soundSet, extendedSound, volume, pitch, duration, elapsedTicks, true, false));
            stopAndClear();
        }
    }

    public boolean handleStop() {
        if (elapsedTicks > duration || living != null && stopPredicate.test(living)) {
            if (shouldPlayStopSound && extendedSound.sounds().stop().isPresent()) {
                if (living != null)
                    Minecraft.getInstance().getSoundManager().play(MibSoundInstance.createEntityDependentStopSound(living, stopPredicate, extendedSound, volume, pitch));
                else
                    Minecraft.getInstance().getSoundManager().play(MibSoundInstance.createPosDependentStopSound(new Vec3(x, y, z), extendedSound, volume, pitch));
            }
            if (rootSound != null)
                rootSound.children.remove(this);
            stopAndClear();
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
            child.stopAndClear();
        }
        stopAndClear();
        ((SoundEngineAccessor)((SoundManagerAccessor)Minecraft.getInstance().getSoundManager()).mib$getSoundEngine()).mib$getInstanceToChannel().remove(this);

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

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof OrchestrateSoundInstance inst))
            return false;
        return inst.isMaster() == isMaster() && inst.song.equals(song) && super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(song, sound, extendedSound, pitch);
    }
}
