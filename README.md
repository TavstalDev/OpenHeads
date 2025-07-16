# OpenHeads

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/TavstalDev/OpenHeads/ci.yml?branch=main&label=build&style=flat-square)](https://github.com/TavstalDev/OpenHeads/actions)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/TavstalDev/OpenHeads?style=flat-square)](https://github.com/TavstalDev/OpenHeads/releases/latest)
[![GitHub All Releases](https://img.shields.io/github/downloads/TavstalDev/OpenHeads/total?style=flat-square)](https://github.com/TavstalDev/OpenHeads/releases)


OpenHeads is a free and open-source heads menu plugin with GUI support for Minecraft servers. It allows players to browse and obtain custom heads through an easy-to-use graphical interface.

## Features

- Browse and obtain custom heads
- GUI support for easy navigation
- Integration with Vault and ProtocolLib
- Permission-based access control

## Installation

1. Download the latest release from the [GitHub releases page](https://github.com/TavstalDev/OpenHeads/releases).
2. Place the downloaded `.jar` file in your server's `plugins` directory.
3. Restart your server to load the plugin.

## Commands

- `/heads` - Opens the heads menu GUI.
    - `/heads help` - Show help for the heads command.
    - `/heads version` - Show the version of the plugin.
    - `/heads reload` - Reload the plugin.

## Permissions

- `openheads.commands.heads` - Allows the player to use the `/heads` command.
- `openheads.commands.reload` - Allows the player to reload the plugin.
- `openheads.commands.version` - Allows the player to view the plugin version.
- `openheads.commands.help` - Allows the player to view help for the heads command.
- `openheads.player` - Collection of player permissions.
- `openheads.admin` - Collection of admin permissions.
- `openheads.*` - Gives all permissions related to the OpenHeads plugin.

## Dependencies

- [Vault](https://www.spigotmc.org/resources/vault.34315/)
- [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)

## Contributing

1. Fork the repository.
2. Create a new branch for your feature or bugfix.
3. Commit your changes.
4. Push your branch and create a pull request.

## License

This project is licensed under the GNU General Public License v3.0. See the `LICENSE` file for more details.

## Contact

For issues or feature requests, please use the [GitHub issue tracker](https://github.com/TavstalDev/OpenHeads/issues).

# Credits & Disclaimer

This plugin uses saved head data from [minecraft-heads.com](https://minecraft-heads.com). We do not own or control their content. All credit for the heads goes to the creators on the [minecraft-heads.com](https://minecraft-heads.com) website.

![OpenHeads Banner](https://images.minecraft-heads.com/banners/minecraft-heads_banner_600x200.png)