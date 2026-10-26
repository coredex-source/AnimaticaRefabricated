package io.github.foundationgames.animatica.util;

import net.minecraft.resources.Identifier;

public enum Utilities {;
    public static Identifier processPath(Identifier fileRelativeTo, Identifier path) {
        var pathStr = path.getPath().contains("/") ? path.getPath() : "./" + path.getPath();
        if (pathStr.startsWith("./")) {
            int lInd = fileRelativeTo.getPath().lastIndexOf("/");
            if (lInd > 0) {
                var builder = new StringBuilder(fileRelativeTo.getPath());
                builder.replace(lInd, builder.length(), pathStr.replaceFirst("\\./", "/"));
                return Identifier.fromNamespaceAndPath(fileRelativeTo.getNamespace(), builder.toString());
            }
        } else if (path.getPath().startsWith("~/")) {
            return Identifier.fromNamespaceAndPath(path.getNamespace(), path.getPath().replaceFirst("~/", "optifine/"));
        }
        return path;
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }
}
