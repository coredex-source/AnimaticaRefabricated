package io.github.foundationgames.animatica;

import io.github.foundationgames.animatica.animation.AnimationLoader;
import io.github.foundationgames.animatica.animation.AnimatedTexture;
import io.github.foundationgames.animatica.compat.IrisCompat;
import io.github.foundationgames.animatica.config.AnimaticaConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Animatica implements ClientModInitializer {
    public static final Logger LOG = LogManager.getLogger("Animatica");
    public static final String NAMESPACE = "animatica";

    public static final AnimaticaConfig CONFIG = new AnimaticaConfig();

    public static boolean IRIS_COMPAT = false;

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(AnimationLoader.INSTANCE);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> AnimatedTexture.shutdownExecutor());

        if (FabricLoader.getInstance().isModLoaded("iris")) {
            try {
                IrisCompat.init();
                IRIS_COMPAT = true;
            } catch (LinkageError e) {
                LOG.warn("Can't init Iris, PBR will not use animated textures.", e);
            }
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(NAMESPACE, path);
    }
}
