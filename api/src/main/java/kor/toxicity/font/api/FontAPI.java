package kor.toxicity.font.api;

import kor.toxicity.font.api.renderer.FontRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FontAPI {
    private static FontPlatform platform;
    private FontAPI() {
        throw new RuntimeException();
    }
    public static void setPlatform(@NotNull FontPlatform fontPlatform) {
        platform = Objects.requireNonNull(fontPlatform);
        platform.getFontAPI().reload();
    }
    public static @Nullable FontRenderer getRenderer(@NotNull String id) {
        return getPlatform().getFontAPI().getResourcePackManager().getRenderer(id);
    }
    public static @NotNull FontPlatform getPlatform() {
        return Objects.requireNonNull(platform);
    }
}
