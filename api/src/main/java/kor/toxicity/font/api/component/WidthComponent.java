package kor.toxicity.font.api.component;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record WidthComponent(int width, @NotNull Component component) {
    public static final WidthComponent EMPTY = new WidthComponent(0, Component.empty());
    public @NotNull WidthComponent append(@NotNull WidthComponent widthComponent) {
        return new WidthComponent(
                width + widthComponent.width,
                component.append(widthComponent.component)
        );
    }
}
