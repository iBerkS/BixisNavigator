package com.bixis.navigator;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class BixisNavigator extends JavaPlugin {

    /** NamespacedKey "bixisnavigator:item" used to tag every navigator item. */
    private NamespacedKey itemKey;

    /** Players who currently have nearby-player visibility toggled OFF (hidden). */
    private final Set<UUID> hidden = Collections.synchronizedSet(new HashSet<>());

    /** Players who have toggled the navigator items OFF via /bixisnav toggle. */
    private final Set<UUID> navDisabled = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void onEnable() {
        this.itemKey = new NamespacedKey(this, "item");
        getServer().getPluginManager().registerEvents(new NavListener(this), this);

        BixisNavCommand command = new BixisNavCommand(this);
        getCommand("bixisnav").setExecutor(command);
        getCommand("bixisnav").setTabCompleter(command);

        getLogger().info("BixisNavigator enabled.");
    }

    @Override
    public void onDisable() {
        hidden.clear();
        navDisabled.clear();
    }

    public NamespacedKey getItemKey() {
        return itemKey;
    }

    public Set<UUID> getHidden() {
        return hidden;
    }

    public Set<UUID> getNavDisabled() {
        return navDisabled;
    }

    /**
     * Installs the four navigator items into their fixed hotbar slots without touching
     * the rest of the inventory. Slot 7 respects the player's current visibility state.
     */
    public void installNavItems(Player player) {
        boolean visibilityHidden = hidden.contains(player.getUniqueId());
        PlayerInventory inv = player.getInventory();
        for (NavItem item : NavItem.values()) {
            ItemStack stack = item == NavItem.VISIBILITY
                    ? NavItem.buildVisibility(this, visibilityHidden)
                    : item.build(this, player);
            inv.setItem(item.slot(), stack);
        }
    }

    /**
     * Clears the player's inventory and installs all four navigator items into their
     * fixed hotbar slots. Used on join.
     */
    public void giveNavItems(Player player) {
        player.getInventory().clear();
        installNavItems(player);
    }

    /** Removes the four navigator items from their slots, leaving the rest untouched. */
    public void clearNavItems(Player player) {
        PlayerInventory inv = player.getInventory();
        for (NavItem item : NavItem.values()) {
            inv.setItem(item.slot(), null);
        }
    }
}
