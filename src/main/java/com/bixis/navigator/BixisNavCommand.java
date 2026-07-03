package com.bixis.navigator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Handles {@code /bixisnav toggle}, which shows/hides the player's own navigator items.
 */
public final class BixisNavCommand implements CommandExecutor, TabCompleter {

    private static final String TOGGLE_PERMISSION = "bixisnavigator.toggle";

    private final BixisNavigator plugin;

    public BixisNavCommand(BixisNavigator plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Bu komut yalnızca oyuncular tarafından kullanılabilir.",
                    NamedTextColor.RED));
            return true;
        }

        if (args.length != 1 || !args[0].equalsIgnoreCase("toggle")) {
            player.sendMessage(Component.text("Kullanım: /bixisnav toggle", NamedTextColor.YELLOW));
            return true;
        }

        if (!player.hasPermission(TOGGLE_PERMISSION)) {
            player.sendMessage(Component.text("Bunu yapmak için yetkin yok.", NamedTextColor.RED));
            return true;
        }

        UUID id = player.getUniqueId();
        if (plugin.getNavDisabled().remove(id)) {
            // Was OFF -> turn ON: restore the four navigator items.
            plugin.installNavItems(player);
            player.sendMessage(Component.text("Navigator eşyaları açıldı.", NamedTextColor.GREEN));
        } else {
            // Was ON -> turn OFF: clear the four navigator slots.
            plugin.getNavDisabled().add(id);
            plugin.clearNavItems(player);
            player.sendMessage(Component.text("Navigator eşyaları kapatıldı.", NamedTextColor.GRAY));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission(TOGGLE_PERMISSION)
                && "toggle".startsWith(args[0].toLowerCase())) {
            return List.of("toggle");
        }
        return Collections.emptyList();
    }
}
