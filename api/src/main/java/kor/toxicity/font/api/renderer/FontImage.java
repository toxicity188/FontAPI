package kor.toxicity.font.api.renderer;

import org.jetbrains.annotations.NotNull;

public record FontImage(
        @NotNull String file,
        @NotNull FontRange range
) {
}
