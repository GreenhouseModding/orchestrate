package dev.greenhouseteam.orchestrate.registry;

import dev.greenhouseteam.mib.registry.internal.RegistrationCallback;
import dev.greenhouseteam.orchestrate.Orchestrate;
import dev.greenhouseteam.orchestrate.song.Song;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public class OrchestrateComponents {
    public static final DataComponentType<Song> SONG = DataComponentType.<Song>builder()
            .persistent(Song.CODEC)
            .networkSynchronized(Song.STREAM_CODEC)
            .build();

    public static void registerAll(RegistrationCallback<DataComponentType<?>> callback) {
        callback.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Orchestrate.asResource("song"), SONG);
    }
}
