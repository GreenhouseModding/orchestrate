package dev.greenhouseteam.orchestrate.song;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.mib.data.KeyWithOctave;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public record Note(KeyWithOctave key, float volume, int startTime, int duration) {
    public static final Codec<Note> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            KeyWithOctave.CODEC.fieldOf("key").forGetter(Note::key),
            Codec.FLOAT.fieldOf("volume").forGetter(Note::volume),
            Codec.INT.fieldOf("start_time").forGetter(Note::startTime),
            Codec.INT.fieldOf("duration").forGetter(Note::duration)
    ).apply(inst, Note::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Note> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(KeyWithOctave.CODEC),
            Note::key,
            ByteBufCodecs.FLOAT,
            Note::volume,
            ByteBufCodecs.INT,
            Note::startTime,
            ByteBufCodecs.INT,
            Note::duration,
            Note::new
    );

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Note note))
            return false;
        return note.duration == duration && note.startTime == startTime && note.key.equals(key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, startTime, duration);
    }
}
