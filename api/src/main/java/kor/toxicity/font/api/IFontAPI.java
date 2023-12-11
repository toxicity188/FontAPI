package kor.toxicity.font.api;

import kor.toxicity.font.api.manager.ResourcePackManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface IFontAPI {
    void reload();
    default @NotNull CompletableFuture<Void> reloadAsync() {
        return CompletableFuture.runAsync(this::reload);
    }
    @NotNull ResourcePackManager getResourcePackManager();
}
