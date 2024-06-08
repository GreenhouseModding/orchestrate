package dev.greenhouseteam.orchestrate.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.greenhouseteam.orchestrate.client.sound.OrchestrateSoundInstance;
import dev.greenhouseteam.orchestrate.client.util.OrchestrateClientUtil;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @ModifyExpressionValue(method = "play", at = @At(value = "FIELD", target = "Lnet/minecraft/client/sounds/SoundManager;EMPTY_SOUND:Lnet/minecraft/client/resources/sounds/Sound;"))
    private Sound orchestrate$soundEngine(Sound original, SoundInstance instance) {
        if (instance instanceof OrchestrateSoundInstance soundInstance && soundInstance.isMaster())
            return OrchestrateClientUtil.EMPTY_SOUND_BUT_NOT_ACTUALLY;
        return original;
    }
}
