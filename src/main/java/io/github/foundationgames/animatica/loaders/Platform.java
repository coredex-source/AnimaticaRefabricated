package io.github.foundationgames.animatica.loaders;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Platform {
    private static final Map<Identifier, PreparableReloadListener> RELOAD_LISTENERS = new LinkedHashMap<>();

    private Platform() {
    }

    public static Path getConfigDir() {
        //? if fabric {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
        //?} else if neoforge {
        /*return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
        *///?}
    }

    public static boolean isModLoaded(String modId) {
        //? if fabric {
        return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(modId);
        //?} else if neoforge {
        /*return net.neoforged.fml.ModList.get().isLoaded(modId);
        *///?}
    }

    public static void registerReloadListener(Identifier id, PreparableReloadListener listener) {
        //? if fabric {
        net.fabricmc.fabric.api.resource.v1.ResourceLoader.get(net.minecraft.server.packs.PackType.CLIENT_RESOURCES)
                .registerReloadListener(id, listener);
        //?} else if neoforge {
        /*RELOAD_LISTENERS.put(id, listener);
        *///?}
    }

    public static void registerClientStopping(Runnable handler) {
        //? if fabric {
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING.register(client -> handler.run());
        //?} else if neoforge {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent.class, event -> handler.run());
        *///?}
    }

    public static void registerClientTick(Runnable handler) {
        //? if fabric {
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> handler.run());
        //?} else if neoforge {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.neoforged.neoforge.client.event.ClientTickEvent.Post.class, event -> handler.run());
        *///?}
    }

    //? if neoforge {
    /*public static void registerNeoForgeEvents(net.neoforged.bus.api.IEventBus modEventBus) {
        modEventBus.addListener(Platform::registerNeoForgeReloadListeners);
    }

    private static void registerNeoForgeReloadListeners(net.neoforged.neoforge.client.event.AddClientReloadListenersEvent event) {
        RELOAD_LISTENERS.forEach(event::addListener);
    }
    *///?}
}
