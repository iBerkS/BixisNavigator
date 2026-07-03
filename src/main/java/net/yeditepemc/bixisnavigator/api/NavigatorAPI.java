package net.yeditepemc.bixisnavigator.api;

import org.bukkit.entity.Player;

/**
 * Public API for controlling a player's navigator hotbar, exposed through the Bukkit
 * {@link org.bukkit.plugin.ServicesManager}. Other plugins can look up the registered
 * provider and enable/disable the navigator items for a player.
 */
public interface NavigatorAPI {

    /** Enables the navigator hotbar for the player and restores the four navigator items. */
    void enableNav(Player player);

    /** Disables the navigator hotbar for the player and clears the four navigator slots. */
    void disableNav(Player player);

    /** @return {@code true} if the navigator hotbar is currently enabled for the player. */
    boolean isNavEnabled(Player player);
}
