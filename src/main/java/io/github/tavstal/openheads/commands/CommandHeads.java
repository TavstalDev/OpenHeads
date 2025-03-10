package io.github.tavstal.openheads.commands;

import io.github.tavstal.minecorelib.core.PluginLogger;
import io.github.tavstal.minecorelib.models.SubCommandData;
import io.github.tavstal.minecorelib.utils.ChatUtils;
import io.github.tavstal.openheads.OpenHeads;
import io.github.tavstal.openheads.gui.MainGUI;
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
    private final PluginLogger _logger = OpenHeads.Logger().WithModule(CommandHeads.class);

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
            _logger.Info(ChatUtils.translateColors("Commands.ConsoleCaller", true).toString());
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

                    boolean isUpToDate = OpenHeads.Instance.isUpToDate();
                    if (isUpToDate) {
                        OpenHeads.Instance.sendLocalizedMsg(player, "Commands.Version.UpToDate");
                        return true;
                    }

                    parameters = new HashMap<>();
                    parameters.put("link", OpenHeads.Instance.getDownloadUrl());
                    OpenHeads.Instance.sendLocalizedMsg(player, "Commands.Version.Outdated");
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

        MainGUI.open(player);
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

    /**
     * Sends the help message to the specified player, displaying the available subcommands.
     *
     * @param player The player to whom the help message is being sent.
     * @param page   The page number of the help message to display.
     */
    private void help(Player player, int page) {
        int maxPage = 1 + (_subCommands.size() / 15);

        if (page > maxPage)
            page = maxPage;
        if (page < 1)
            page = 1;
        int finalPage = page;

        OpenHeads.Instance.sendLocalizedMsg(player, "Commands.Help.Title", new HashMap<>() {{
            put("current_page", finalPage);
            put("max_page", maxPage);
        }});
        OpenHeads.Instance.sendLocalizedMsg(player, "Commands.Help.Info");

        boolean reachedEnd = false;
        int itemIndex = 0;
        for (int i = 0; i < 15; i++) {
            int index = itemIndex + (page - 1) * 15;
            if (index >= _subCommands.size()) {
                reachedEnd = true;
                break;
            }
            itemIndex++;

            SubCommandData subCommand = _subCommands.get(index);
            if (!subCommand.hasPermission(player)) {
                i--;
                continue;
            }

            subCommand.send(OpenHeads.Instance, player);
        }

        // Bottom message
        String previousBtn = OpenHeads.Instance.Localize(player, "Commands.Help.PrevBtn");
        String nextBtn = OpenHeads.Instance.Localize(player, "Commands.Help.NextBtn");
        String bottomMsg = OpenHeads.Instance.Localize(player, "Commands.Help.Bottom")
                .replace("%current_page%", String.valueOf(page))
                .replace("%max_page%", String.valueOf(maxPage));

        Map<String, Component> bottomParams = new HashMap<>();
        if (page > 1)
            bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true).clickEvent(ClickEvent.runCommand("/kit help " + (page - 1))));
        else
            bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true));

        if (!reachedEnd && maxPage >= page + 1)
            bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true).clickEvent(ClickEvent.runCommand("/kit help " + (page + 1))));
        else
            bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true));

        Component bottomComp = ChatUtils.buildWithButtons(bottomMsg, bottomParams);
        player.sendMessage(bottomComp);
    }
}
