package dev.greenhouseteam.orchestrate.registry;

import dev.greenhouseteam.mib.Mib;
import dev.greenhouseteam.mib.registry.internal.RegistrationCallback;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Consumer;

@EventBusSubscriber(modid = Mib.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class OrchestrateRegistryEvents {
    @SubscribeEvent
    public static void registerContent(RegisterEvent event) {
        register(event, OrchestrateBlocks::registerAll);
        register(event, OrchestrateBlockEntityTypes::registerAll);
        register(event, OrchestrateComponents::registerAll);
        register(event, OrchestrateItems::registerAll);
        register(event, OrchestrateMenuTypes::registerAll);
        register(event, OrchestrateSoundEvents::registerAll);
    }

    private static <T> void register(RegisterEvent event, Consumer<RegistrationCallback<T>> consumer) {
        consumer.accept((registry, id, value) ->
                event.register(registry.key(), id, () -> value));
    }
}
