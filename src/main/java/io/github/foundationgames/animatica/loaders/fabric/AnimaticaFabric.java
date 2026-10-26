//? if fabric {
package io.github.foundationgames.animatica.loaders.fabric;

import io.github.foundationgames.animatica.Animatica;
import net.fabricmc.api.ClientModInitializer;

public class AnimaticaFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Animatica.initializeClient();
    }
}
//?}
