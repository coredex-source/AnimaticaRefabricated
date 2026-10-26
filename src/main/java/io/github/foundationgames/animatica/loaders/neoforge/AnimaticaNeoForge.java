//? if neoforge {
/*package io.github.foundationgames.animatica.loaders.neoforge;

import io.github.foundationgames.animatica.Animatica;
import io.github.foundationgames.animatica.loaders.Platform;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = Animatica.NAMESPACE, dist = Dist.CLIENT)
public class AnimaticaNeoForge {
    public AnimaticaNeoForge(IEventBus modEventBus) {
        Platform.registerNeoForgeEvents(modEventBus);
        Animatica.initializeClient();
    }
}
*///?}
