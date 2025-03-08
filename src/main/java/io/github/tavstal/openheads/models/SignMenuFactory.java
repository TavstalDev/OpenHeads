package io.github.tavstal.openheads.models;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import io.github.tavstal.minecorelib.core.PluginLogger;
import io.github.tavstal.minecorelib.utils.ChatUtils;
import io.github.tavstal.openheads.OpenHeads;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;

// Code from FrostedSnowman on spigotmc.org
/**
 * Factory class for creating and managing sign menus.
 */
public final class SignMenuFactory {
    private static final int ACTION_INDEX = 9;
    private static final int SIGN_LINES = 4;
    private static final String NBT_FORMAT = "{\"text\":\"%s\"}";
    private static final String NBT_BLOCK_ID = "minecraft:sign";

    private final Plugin plugin;

    private final Map<Player, Menu> inputs;

    /**
     * Constructs a new SignMenuFactory.
     *
     * @param plugin the plugin instance
     */
    public SignMenuFactory(Plugin plugin) {
        this.plugin = plugin;
        this.inputs = new HashMap<>();
        this.listen();
    }

    /**
     * Creates a new menu with the specified text.
     *
     * @param text the text to display on the sign
     * @return the new menu
     */
    public Menu newMenu(List<String> text) {
        return new Menu(text);
    }

    /**
     * Listens for the UPDATE_SIGN packet and handles the sign menu interaction.
     */
    private void listen() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this.plugin, PacketType.Play.Client.UPDATE_SIGN) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();

                Menu menu = inputs.remove(player);

                if (menu == null) {
                    return;
                }
                event.setCancelled(true);

                boolean success = menu.response.test(player, event.getPacket().getStringArrays().read(0));

                if (!success && menu.reopenIfFail && !menu.forceClose) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> menu.open(player), 2L);
                }
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        Location location = menu.position.toLocation(player.getWorld());
                        player.sendBlockChange(location, location.getBlock().getBlockData());
                    }
                }, 2L);
            }
        });
    }

    /**
     * Represents a menu that can be displayed on a sign.
     */
    public final class Menu {
        private final PluginLogger _logger = OpenHeads.Logger().WithModule(Menu.class);

        private final List<String> text;

        private BiPredicate<Player, String[]> response;
        private boolean reopenIfFail;

        private BlockPosition position;

        private boolean forceClose;

        /**
         * Constructs a new Menu with the specified text.
         *
         * @param text the text to display on the sign
         */
        Menu(List<String> text) {
            this.text = text;
        }

        /**
         * Sets whether the menu should reopen if the response fails.
         *
         * @param value true to reopen if the response fails, false otherwise
         * @return the menu instance
         */
        public Menu reopenIfFail(boolean value) {
            this.reopenIfFail = value;
            return this;
        }

        /**
         * Sets the response handler for the menu.
         *
         * @param response the response handler
         * @return the menu instance
         */
        public Menu response(BiPredicate<Player, String[]> response) {
            this.response = response;
            return this;
        }

        /**
         * Opens the menu for the specified player.
         *
         * @param player the player to open the menu for
         */
        public void open(Player player) {
            Objects.requireNonNull(player, "player");
            if (!player.isOnline()) {
                return;
            }
            Location location = player.getLocation();
            this.position = new BlockPosition(location.getBlockX(), 255 - location.getBlockY(), location.getBlockZ());

            player.sendBlockChange(this.position.toLocation(location.getWorld()), Material.OAK_SIGN.createBlockData());

            PacketContainer openSign = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);
            PacketContainer signData = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.TILE_ENTITY_DATA);

            openSign.getBlockPositionModifier().write(0, this.position);

            NbtCompound signNBT = (NbtCompound) signData.getNbtModifier().read(0);

            for (int line = 0; line < SIGN_LINES; line++) {
                signNBT.put("Text" + (line + 1), this.text.size() > line ? String.format(NBT_FORMAT, color(this.text.get(line))) : "");
            }

            signNBT.put("x", this.position.getX());
            signNBT.put("y", this.position.getY());
            signNBT.put("z", this.position.getZ());
            signNBT.put("id", NBT_BLOCK_ID);

            signData.getBlockPositionModifier().write(0, this.position);
            signData.getIntegers().write(0, ACTION_INDEX);
            signData.getNbtModifier().write(0, signNBT);

            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, signData);
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, openSign);
            } catch (Exception ex) {
                _logger.Warn("Error while opening sign menu for player: " + player.getName());
                _logger.Error(ex.getMessage());
            }
            inputs.put(player, this);
        }

        /**
         * Closes the menu for the specified player.
         *
         * @param player the player to close the menu for
         * @param force true to force close the menu, false otherwise
         */
        public void close(Player player, boolean force) {
            this.forceClose = force;
            if (player.isOnline()) {
                player.closeInventory();
            }
        }

        /**
         * Closes the menu for the specified player.
         *
         * @param player the player to close the menu for
         */
        public void close(Player player) {
            close(player, false);
        }

        /**
         * Translates color codes in the input string.
         *
         * @param input the input string
         * @return the translated string
         */
        private String color(String input) {
            return ChatUtils.translateColors(input, true).toString();
        }
    }
}
