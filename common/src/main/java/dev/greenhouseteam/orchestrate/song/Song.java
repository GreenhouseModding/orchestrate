package dev.greenhouseteam.orchestrate.song;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record Song(Component name, Component author, List<Note> notes, int duration) {
    public static final Song DEFAULT = new Song(Component.literal("Unnamed Song"), Component.literal("Unnamed Author"), List.of(), 0);
    public static final Codec<Song> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ComponentSerialization.CODEC.fieldOf("name").forGetter(Song::name),
            ComponentSerialization.CODEC.fieldOf("author").forGetter(Song::author),
            Note.CODEC.listOf().optionalFieldOf("notes", List.of()).forGetter(Song::notes),
            Codec.INT.fieldOf("duration").forGetter(Song::duration)
    ).apply(inst, Song::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Song> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC,
            Song::name,
            ComponentSerialization.STREAM_CODEC,
            Song::author,
            Note.STREAM_CODEC.apply(ByteBufCodecs.list()),
            Song::notes,
            ByteBufCodecs.INT,
            Song::duration,
            Song::new
    );

    public boolean isEmpty() {
        return !notes.isEmpty();
    }

    public static class Builder {
        private Component name = Component.literal("Unnamed Song");
        private Component author = Component.literal("Unnamed Author");
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

        public Builder named(String string) {
            author = Component.literal(string);
            return this;
        }

        public Builder named(Component component) {
            name = component;
            return this;
        }

        public Builder author(String string) {
            author = Component.literal(string);
            return this;
        }

        public Builder author(Component component) {
            author = component;
            return this;
        }

        public Builder add(Note note) {
            notes.add(note);
            duration = Math.max(duration, note.startTime() + note.duration());
            return this;
        }

        public Builder remove(Note note) {
            notes.remove(note);
            duration = Math.min(duration, notes.stream().map(value -> value.startTime() + value.duration()).max(Comparator.comparingInt(value -> value)).orElse(0));
            return this;
        }

        public int getDuration() {
            return duration;
        }

        public Song build() {
            if (isEmpty())
                throw new IllegalStateException("Attempted to create song without any notes.");
            return new Song(name, author, notes, duration);
        }
    }
}
