package dev.greenhouseteam.orchestrate;

import dev.greenhouseteam.orchestrate.platform.OrchestratePlatformHelperFabric;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class OrchestrateFabricPreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        Orchestrate.setHelper(new OrchestratePlatformHelperFabric());
    }
}
