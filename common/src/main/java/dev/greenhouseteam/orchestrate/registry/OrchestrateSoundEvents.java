package dev.greenhouseteam.orchestrate.registry;

import dev.greenhouseteam.mib.registry.internal.RegistrationCallback;
import dev.greenhouseteam.orchestrate.Orchestrate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public class OrchestrateSoundEvents {
    public static final SoundEvent MASTER = SoundEvent.createVariableRangeEvent(Orchestrate.asResource("orchestrate.internal.master"));

    public static void registerAll(RegistrationCallback<SoundEvent> callback) {
        callback.register(BuiltInRegistries.SOUND_EVENT, Orchestrate.asResource("orchestrate.internal.master"), MASTER);
    }
}
