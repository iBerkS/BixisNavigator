package com.bixis.navigator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * The navigator items, identified purely by their PersistentDataContainer tag.
 *
 * <p>Every navigator item is tagged with {@link BixisNavigator#getItemKey()}
 * ("bixisnavigator:item") whose value is the enum {@link #id}. Identification and
 * routing of clicks is done exclusively through this tag, never through display names.
 */
public enum NavItem {

    OYUN_MENU(0, "compass", Material.RECOVERY_COMPASS,
            "&bOyun Menüsü", "&7Sunucular arasında gezin!"),
    KOZMETIK(4, "cosmetics", Material.CHEST,
            "&6Kozmetik Menü", "&7Kozmetik eşyalarını özelleştir!"),
    VISIBILITY(7, "visibility", Material.LIME_DYE,
            "&aOyuncuları Göster", "&7Yakındaki oyuncuları gizle/göster"),
    PROFILE(8, "profile", Material.PLAYER_HEAD,
            "&eOyuncu Profili", "&7Profilini görüntüle!");

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();

    private final int slot;
    private final String id;
    private final Material material;
    private final String name;
    private final String lore;

    NavItem(int slot, String id, Material material, String name, String lore) {
        this.slot = slot;
        this.id = id;
        this.material = material;
        this.name = name;
        this.lore = lore;
    }

    public int slot() {
        return slot;
    }

    public String id() {
        return id;
    }

    /**
     * Builds the tagged ItemStack for this navigator item.
     *
     * @param plugin the owning plugin (for the NamespacedKey)
     * @param player the owner, used to apply the player's own skin to the profile head
     */
    public ItemStack build(BixisNavigator plugin, Player player) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(styled(name));
        meta.lore(List.of(styled(lore)));

        if (meta instanceof SkullMeta skull) {
            skull.setOwningPlayer(player);
        }

        meta.getPersistentDataContainer()
                .set(plugin.getItemKey(), PersistentDataType.STRING, id);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Builds the slot-7 visibility item in either state, both carrying the same
     * "visibility" tag so click routing is state-independent.
     *
     * @param hidden {@code true} for the "players hidden" (GRAY_DYE) variant,
     *               {@code false} for the "players shown" (LIME_DYE) variant
     */
    public static ItemStack buildVisibility(BixisNavigator plugin, boolean hidden) {
        Material material = hidden ? Material.GRAY_DYE : Material.LIME_DYE;
        String name = hidden ? "&7Oyuncuları Gizle" : "&aOyuncuları Göster";

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(styled(name));
        meta.lore(List.of(styled(VISIBILITY.lore)));
        meta.getPersistentDataContainer()
                .set(plugin.getItemKey(), PersistentDataType.STRING, VISIBILITY.id);
        item.setItemMeta(meta);
        return item;
    }

    private static Component styled(String legacy) {
        // Disable the default italic decoration that vanilla applies to custom names/lore.
        return LEGACY.deserialize(legacy).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Reads the navigator id stored on an item, or {@code null} if the item is not
     * a tagged navigator item.
     */
    public static String idOf(BixisNavigator plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        NamespacedKey key = plugin.getItemKey();
        return item.getItemMeta().getPersistentDataContainer()
                .get(key, PersistentDataType.STRING);
    }

    /** Whether the given item carries the navigator tag. */
    public static boolean isNavItem(BixisNavigator plugin, ItemStack item) {
        return idOf(plugin, item) != null;
    }
}
