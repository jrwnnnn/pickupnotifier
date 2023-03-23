package fuzs.pickupnotifier.client;

import fuzs.pickupnotifier.PickUpNotifier;
import fuzs.pickupnotifier.client.data.ItemBlacklistManager;
import fuzs.pickupnotifier.client.handler.DrawEntriesHandler;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.event.v1.ClientTickEvents;
import fuzs.puzzleslib.api.client.event.v1.RenderGuiCallback;
import fuzs.puzzleslib.api.core.v1.context.ModLifecycleContext;

public class PickUpNotifierClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerHandlers();
    }

    private static void registerHandlers() {
        ClientTickEvents.END.register(DrawEntriesHandler.INSTANCE::onClientTick);
        RenderGuiCallback.EVENT.register(DrawEntriesHandler.INSTANCE::onRenderGui);
    }

    @Override
    public void onClientSetup(ModLifecycleContext context) {
        ItemBlacklistManager.INSTANCE.loadAll(PickUpNotifier.MOD_ID);
    }
}
