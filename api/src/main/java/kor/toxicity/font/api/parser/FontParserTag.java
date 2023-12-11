package kor.toxicity.font.api.parser;

import kor.toxicity.font.api.component.WidthComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FontParserTag {
    @NotNull Type getType();
    @Nullable WidthComponent getComponent();
    static @NotNull FontParserTag tag(@Nullable WidthComponent component) {
        return new FontParserTag() {
            @Override
            public @NotNull Type getType() {
                return Type.NONE;
            }

            @Override
            public @Nullable WidthComponent getComponent() {
                return component;
            }
        };
    }
    static @NotNull FontParserTag selfClosingTag(@Nullable WidthComponent component) {
        return new FontParserTag() {
            @Override
            public @NotNull Type getType() {
                return Type.SELF_CLOSING;
            }

            @Override
            public @Nullable WidthComponent getComponent() {
                return component;
            }
        };
    }
    enum Type {
        NONE,
        SELF_CLOSING
    }
}
