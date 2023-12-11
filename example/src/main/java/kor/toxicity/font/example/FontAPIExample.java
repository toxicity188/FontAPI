package kor.toxicity.font.example;

import kor.toxicity.font.api.FontAPI;
import kor.toxicity.font.api.renderer.FontAlign;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.plugin.java.JavaPlugin;

public class FontAPIExample extends JavaPlugin {
    @Override
    public void onEnable() {
        var audience = BukkitAudiences.create(this);
        var command = getCommand("testtitle");
        if (command != null) command.setExecutor((sender, command1, label, args) -> {
            var renderer = FontAPI.getRenderer("test_renderer");
            var align = (args.length == 0) ? FontAlign.LEFT : switch (args[0].toLowerCase()) {
                default -> FontAlign.LEFT;
                case "right" -> FontAlign.RIGHT;
                case "center" -> FontAlign.CENTER;
            };
            if (renderer != null) {
                audience.sender(sender).showTitle(
                        Title.title(
                                Component.empty(),
                                renderer.builder()
                                        .align(align)
                                        .append(new String[] {
                                                "Hello world!",
                                                "<yellow>Thank</yellow> you to <aqua>download</aqua> my plugin!",
                                                "The align is " + align + "!",
                                                "Diamond is here! <gold>\\></gold> <image:diamond> <gold>\\<</gold>",
                                        })
                                        .build()
                                        .component()
                        )
                );
            } else sender.sendMessage("Unable to find \"test_renderer\" renderer.");
            return true;
        });
    }
}
