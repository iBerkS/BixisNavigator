package com.bixis.navigator;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

public final class NavListener implements Listener {

    /** Radius, in blocks, within which players are hidden/shown by the toggle. */
    private static final double VISIBILITY_RADIUS = 64.0;

    private final BixisNavigator plugin;

    public NavListener(BixisNavigator plugin) {
        this.plugin = plugin;
    }

    // ---------------------------------------------------------------- lifecycle

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Toggle always defaults to ON on join.
        plugin.getNavDisabled().remove(player.getUniqueId());
        // Wait one tick so we act after the vanilla/other-plugin join inventory setup.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                plugin.giveNavItems(player);
            }
        }, 1L);

        // Re-apply hiding: if any online player currently has visibility toggled OFF,
        // hide the freshly joined player from them so the hidden state survives new joins.
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (!online.equals(player) && plugin.getHidden().contains(online.getUniqueId())) {
                hideFrom(online, player);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        plugin.getHidden().remove(id);
        plugin.getNavDisabled().remove(id);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // Restore after respawn processing completes.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            // Respect the toggle: only reinstall for players who have the items enabled.
            // If disabled, there are no navigator items to restore, and we must not wipe
            // whatever the player has placed in those now-free slots.
            if (!plugin.getNavDisabled().contains(player.getUniqueId())) {
                plugin.installNavItems(player);
            }
        }, 1L);
    }

    // ------------------------------------------------------------- interactions

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        String id = NavItem.idOf(plugin, item);
        if (id == null) {
            return;
        }

        Action action = event.getAction();
        boolean rightClick = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        boolean leftClick = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
        if (!rightClick && !leftClick) {
            return;
        }

        Player player = event.getPlayer();
        switch (id) {
            case "compass" -> {
                if (rightClick || leftClick) {
                    event.setCancelled(true);
                    runCommand(player, "/oyunlar");
                }
            }
            case "cosmetics" -> {
                if (rightClick || leftClick) {
                    event.setCancelled(true);
                    runCommand(player, "/gmenu main");
                }
            }
            case "visibility" -> {
                if (rightClick) {
                    event.setCancelled(true);
                    toggleVisibility(player);
                }
            }
            case "profile" -> {
                if (rightClick) {
                    event.setCancelled(true);
                    runCommand(player, "/profil");
                }
            }
            default -> {
                // Unknown navigator id: still cancel to avoid unintended item use.
                event.setCancelled(true);
            }
        }
    }

    /**
     * Runs a command as the player through the full chat/command pipeline.
     *
     * <p>Unlike {@link Player#performCommand(String)}, {@link Player#chat(String)} with a
     * leading slash fires {@link org.bukkit.event.player.PlayerCommandPreprocessEvent},
     * so it also triggers commands that plugins (e.g. DeluxeMenus) implement by listening
     * to that event rather than registering in the server command map. Real command-map
     * commands (e.g. GadgetsMenu's /gmenu) keep working too.
     *
     * @param command the command including its leading slash, e.g. {@code "/oyunlar"}
     */
    private void runCommand(Player player, String command) {
        player.chat(command);
    }

    private void toggleVisibility(Player player) {
        boolean nowHidden = !plugin.getHidden().contains(player.getUniqueId());

        for (Player other : player.getWorld().getPlayers()) {
            if (other.equals(player)) {
                continue;
            }
            if (other.getLocation().distanceSquared(player.getLocation())
                    > VISIBILITY_RADIUS * VISIBILITY_RADIUS) {
                continue;
            }
            if (nowHidden) {
                hideFrom(player, other);
            } else {
                player.showPlayer(plugin, other);
            }
        }

        if (nowHidden) {
            plugin.getHidden().add(player.getUniqueId());
        } else {
            plugin.getHidden().remove(player.getUniqueId());
        }

        player.getInventory().setItem(
                NavItem.VISIBILITY.slot(),
                NavItem.buildVisibility(plugin, nowHidden));

        String message = nowHidden ? "&7Oyuncular gizlendi." : "&7Oyuncular gösterildi.";
        float pitch = nowHidden ? 0.8f : 1.2f;
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, pitch);
    }

    /**
     * Hides {@code target} from {@code viewer} visually. On modern Paper, {@code hidePlayer}
     * already keeps the target on the viewer's TAB list, so no extra TAB handling is needed.
     */
    private void hideFrom(Player viewer, Player target) {
        viewer.hidePlayer(plugin, target);
    }

    // -------------------------------------------------------------- protection

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (NavItem.isNavItem(plugin, event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Directly clicked slot or the carried cursor stack.
        if (NavItem.isNavItem(plugin, event.getCurrentItem())
                || NavItem.isNavItem(plugin, event.getCursor())) {
            event.setCancelled(true);
            return;
        }

        // Hotbar swap (number keys 1-9): the moved item sits in the target hotbar
        // slot, not in the hovered slot, so getCurrentItem/getCursor miss it.
        if (event.getClick() == ClickType.NUMBER_KEY && event.getHotbarButton() >= 0
                && event.getView().getBottomInventory() instanceof PlayerInventory inv
                && NavItem.isNavItem(plugin, inv.getItem(event.getHotbarButton()))) {
            event.setCancelled(true);
            return;
        }

        // Swap-with-offhand (F key) from within a GUI: the source is the off hand,
        // which is likewise not the hovered slot.
        if (event.getClick() == ClickType.SWAP_OFFHAND
                && event.getWhoClicked() instanceof Player clicker
                && NavItem.isNavItem(plugin, clicker.getInventory().getItemInOffHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        for (ItemStack dragged : event.getNewItems().values()) {
            if (NavItem.isNavItem(plugin, dragged)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        // Pressing F during normal gameplay swaps the held item with the off hand
        // and never passes through InventoryClickEvent.
        if (NavItem.isNavItem(plugin, event.getMainHandItem())
                || NavItem.isNavItem(plugin, event.getOffHandItem())) {
            event.setCancelled(true);
        }
    }
}
