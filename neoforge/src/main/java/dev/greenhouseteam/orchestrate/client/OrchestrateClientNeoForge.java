package dev.greenhouseteam.orchestrate.client;

import dev.greenhouseteam.orchestrate.Orchestrate;
import dev.greenhouseteam.orchestrate.client.screen.CompositionScreen;
import dev.greenhouseteam.orchestrate.registry.OrchestrateMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class OrchestrateClientNeoForge {
    @EventBusSubscriber(modid = Orchestrate.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEvents {
        public static void registerMenUScreens(RegisterMenuScreensEvent event) {
            event.register(OrchestrateMenuTypes.COMPOSITION_TABLE, CompositionScreen::new);
        }
    }
}
