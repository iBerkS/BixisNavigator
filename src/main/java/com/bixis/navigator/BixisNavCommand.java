package com.bixis.navigator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.yeditepemc.bixisnavigator.api.NavigatorAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Handles {@code /bixisnav on|off}, which enables/disables the player's navigator hotbar
 * (builder mode) through the {@link NavigatorAPI}.
 */
public final class BixisNavCommand implements CommandExecutor, TabCompleter {

    private static final String ADMIN_PERMISSION = "bixisnavigator.admin";

    private final NavigatorAPI api;

    public BixisNavCommand(NavigatorAPI api) {
        this.api = api;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Bu komut yalnızca oyuncular tarafından kullanılabilir.",
                    NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission(ADMIN_PERMISSION)) {
            player.sendMessage(Component.text("Bunu yapmak için yetkin yok.", NamedTextColor.RED));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(Component.text("Kullanım: /bixisnav <on|off>", NamedTextColor.YELLOW));
            return true;
        }

        if (args[0].equalsIgnoreCase("on")) {
            api.enableNav(player);
            player.sendMessage(Component.text("Navigator hotbar açıldı.", NamedTextColor.GREEN));
        } else if (args[0].equalsIgnoreCase("off")) {
            api.disableNav(player);
            player.sendMessage(Component.text("Navigator hotbar kapatıldı.", NamedTextColor.GRAY));
        } else {
            player.sendMessage(Component.text("Kullanım: /bixisnav <on|off>", NamedTextColor.YELLOW));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission(ADMIN_PERMISSION)) {
            String prefix = args[0].toLowerCase();
            return Stream.of("on", "off").filter(s -> s.startsWith(prefix)).toList();
        }
        return Collections.emptyList();
    }
}
