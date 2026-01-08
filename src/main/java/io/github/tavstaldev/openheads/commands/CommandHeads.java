package io.github.tavstaldev.openheads.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.managers.MenuManager;
import io.github.tavstaldev.minecorelib.models.command.SubCommandData;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openheads.OpenHeads;
import io.github.tavstaldev.openheads.gui.CategoryGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHeads implements CommandExecutor {
    private final PluginLogger _logger = OpenHeads.logger().withModule(CommandHeads.class);
    private final String baseCommand = "heads";

    /**
     * Executes the given command, returning its success.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (sender instanceof ConsoleCommandSender) {
            _logger.info(ChatUtils.translateColors("Commands.ConsoleCaller", true).toString());
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("openheads.commands.heads")) {
            OpenHeads.Instance.sendLocalizedMsg(player, "General.NoPermission");
            return true;
        }

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "help":
                case "?": {
                    int page = 1;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (Exception ex) {
                            OpenHeads.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidPage");
                            return true;
                        }
                    }

                    help(player, page);
                    return true;
                }
                case "version": {
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("version", OpenHeads.Instance.getVersion());
                    OpenHeads.Instance.sendLocalizedMsg(player, "Commands.Version.Current", parameters);

                    OpenHeads.Instance.isUpToDate().thenAccept(upToDate -> {
                        if (upToDate) {
                            OpenHeads.Instance.sendLocalizedMsg(player, "Commands.Version.UpToDate");
                        } else {
                            OpenHeads.Instance.sendLocalizedMsg(player, "Commands.Version.Outdated", Map.of("link", OpenHeads.Instance.getDownloadUrl()));
                        }
                    }).exceptionally(e -> {
                        _logger.error("Failed to determine update status: " + e.getMessage());
                        return null;
                    });
                    return true;
                }
                case "reload": {
                    if (!player.hasPermission("openheads.commands.reload")) {
                        OpenHeads.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    OpenHeads.Instance.reload();
                    OpenHeads.Instance.sendLocalizedMsg(player, "Commands.Reload.Done");
                    return true;
                }
            }

            OpenHeads.Instance.sendLocalizedMsg(player, "Commands.InvalidArguments");
            return true;
        }

        MenuManager manager = OpenHeads.Instance.getMenuManager();
        if (manager == null)
            return true;
        manager.open(player, CategoryGUI.ID);
        return true;
    }

    /**
     * A list of subcommands available for the CommandHeads command.
     */
    private final List<SubCommandData> _subCommands = new ArrayList<>() {
        {
            // HELP
            add(new SubCommandData("help", "", new HashMap<>() {{
                put("syntax", null);
                put("description", "Commands.Help.Desc");
            }}));
            // VERSION
            add(new SubCommandData("version", "", new HashMap<>() {{
                put("syntax", null);
                put("description", "Commands.Version.Desc");
            }}));
            // RELOAD
            add(new SubCommandData("reload", "openheads.commands.reload", new HashMap<>() {{
                put("syntax", null);
                put("description", "Commands.Reload.Desc");
            }}));
            // OPEN
            add(new SubCommandData("", "", new HashMap<>() {{
                put("syntax", null);
                put("description", "Commands.Gui.Desc");
            }}));
        }
    };

    private void help(CommandSender sender, int page) {
        int maxPage = 1 + (_subCommands.size() / 15);

        // Ensure the page number is within valid bounds
        if (page > maxPage)
            page = maxPage;
        if (page < 1)
            page = 1;
        int finalPage = page;

        // Send the help menu title and info
        OpenHeads.Instance.sendCommandReply(sender, "Commands.Help.Title", Map.of(
                        "current_page", finalPage,
                        "max_page", maxPage
                )
        );
        OpenHeads.Instance.sendCommandReply(sender, "Commands.Help.Info");

        boolean reachedEnd = false;
        int itemIndex = 0;

        // Display up to 15 subcommands per page
        for (int i = 0; i < 15; i++) {
            int index = itemIndex + (page - 1) * 15;
            if (index >= _subCommands.size()) {
                reachedEnd = true;
                break;
            }
            itemIndex++;

            SubCommandData subCommand = _subCommands.get(index);
            if (!subCommand.hasPermission(sender)) {
                i--;
                continue;
            }

            subCommand.send(OpenHeads.Instance, sender, baseCommand);
        }

        // Display navigation buttons for the help menu
        String previousBtn, nextBtn, bottomMsg;
        if (sender instanceof Player player)
        {
            previousBtn = OpenHeads.Instance.localize(player, "Commands.Help.PrevBtn");
            nextBtn = OpenHeads.Instance.localize(player, "Commands.Help.NextBtn");
            bottomMsg = OpenHeads.Instance.localize(player, "Commands.Help.Bottom", Map.of(
                    "current_page", page,
                    "max_page", maxPage
            ));
        }
        else {
            previousBtn = OpenHeads.Instance.localize("Commands.Help.PrevBtn");
            nextBtn = OpenHeads.Instance.localize("Commands.Help.NextBtn");
            bottomMsg = OpenHeads.Instance.localize("Commands.Help.Bottom", Map.of(
                    "current_page", page,
                    "max_page", maxPage
            ));
        }

        Map<String, Component> bottomParams = new HashMap<>();
        if (page > 1)
            bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true)
                    .clickEvent(ClickEvent.runCommand(String.format("/%s help %s", baseCommand, page - 1))));
        else
            bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true));

        if (!reachedEnd && maxPage >= page + 1)
            bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true)
                    .clickEvent(ClickEvent.runCommand(String.format("/%s help %s", baseCommand, page + 1))));
        else
            bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true));

        Component bottomComp = ChatUtils.buildWithButtons(bottomMsg, bottomParams);
        sender.sendMessage(bottomComp);
    }
}
