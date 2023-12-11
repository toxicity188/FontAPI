package kor.toxicity.font.api.manager;

import kor.toxicity.font.api.renderer.FontRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ResourcePackManager extends FontAPIManager {
    @Nullable FontRenderer getRenderer(@NotNull String id);
}
