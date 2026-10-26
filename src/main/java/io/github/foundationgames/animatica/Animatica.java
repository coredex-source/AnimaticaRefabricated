package io.github.foundationgames.animatica;

import io.github.foundationgames.animatica.animation.AnimationLoader;
import io.github.foundationgames.animatica.animation.AnimatedTexture;
import io.github.foundationgames.animatica.compat.IrisCompat;
import io.github.foundationgames.animatica.config.AnimaticaConfig;
import io.github.foundationgames.animatica.loaders.Platform;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Animatica {
    public static final Logger LOG = LogManager.getLogger("Animatica");
    public static final String NAMESPACE = "animatica";

    public static final AnimaticaConfig CONFIG = new AnimaticaConfig();

    public static boolean IRIS_COMPAT = false;

    public static void initializeClient() {
        Platform.registerReloadListener(AnimationLoader.ID, AnimationLoader.INSTANCE);
        Platform.registerClientStopping(AnimatedTexture::shutdownExecutor);

        if (Platform.isModLoaded("iris")) {
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
