package net.yeditepemc.bixisnavigator.api;

import com.bixis.navigator.BixisNavigator;
import org.bukkit.entity.Player;

/**
 * Default {@link NavigatorAPI} implementation backed by the plugin's {@code navDisabled}
 * set and navigator-item helpers.
 */
public final class NavigatorAPIImpl implements NavigatorAPI {

    private final BixisNavigator plugin;

    public NavigatorAPIImpl(BixisNavigator plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enableNav(Player player) {
        plugin.getNavDisabled().remove(player.getUniqueId());
        // Restore the four navigator items without wiping the rest of the inventory.
        plugin.installNavItems(player);
    }

    @Override
    public void disableNav(Player player) {
        plugin.getNavDisabled().add(player.getUniqueId());
        plugin.clearNavItems(player);
    }

    @Override
    public boolean isNavEnabled(Player player) {
        return !plugin.getNavDisabled().contains(player.getUniqueId());
    }
}
