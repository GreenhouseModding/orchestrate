package dev.greenhouseteam.orchestrate.song;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record Song(List<Note> notes, int duration) {
    public static final Codec<Song> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Note.CODEC.listOf().optionalFieldOf("notes", List.of()).forGetter(Song::notes),
            Codec.INT.fieldOf("duration").forGetter(Song::duration)
    ).apply(inst, Song::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Song> STREAM_CODEC = StreamCodec.composite(
            Note.STREAM_CODEC.apply(ByteBufCodecs.list()),
            Song::notes,
            ByteBufCodecs.INT,
            Song::duration,
            Song::new
    );

    public static class Builder {
        private final List<Note> notes = new ArrayList<>();
        private int duration;

        public static Builder fromSong(Song song) {
            Builder builder = new Builder();
            builder.notes.addAll(song.notes);
            builder.duration = song.duration;
            return builder;
        }

        public boolean isEmpty() {
            return notes.isEmpty();
        }

        public Builder add(Note note) {
            notes.add(note);
            duration = Math.max(duration, note.startTime() + note.duration() + 20);
            return this;
        }

        public Builder remove(Note note) {
            notes.remove(note);
            duration = Math.min(duration, notes.stream().map(Note::duration).max(Comparator.comparingInt(value -> value + 20)).orElse(0));
            return this;
        }

        public int getDuration() {
            return duration;
        }

        public Song build() {
            if (isEmpty())
                throw new IllegalStateException("Attempted to create song without any notes.");
            return new Song(notes, duration);
        }
    }
}
