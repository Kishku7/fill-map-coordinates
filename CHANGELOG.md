# Changelog

## 2.0.0

First release of Fill Map Coordinates, a rebuilt and extended version of
[Filled Map Coordinates](https://modrinth.com/mod/2SRiBKfr) by manuelgrabowski
(source: https://github.com/manuelgrabowski/FilledMapCoordinates, licence CC BY-NC-SA 4.0, which this project keeps).

- Shows the corner coordinates of the area a filled map covers in the item tooltip.
- Records the area when a map is filled on the server, and again when it is zoomed out on a cartography table.
  It stores the real centre and zoom of the map, so the corners stay correct after zooming.
- Maps made by the original mod still show their area.
- One config value, `enabled`, in `config/FillMapCoordinates/config.yml`; change it in game with
  `/mapcoordinates enabled true|false`.
- Minecraft 1.21.11: Fabric, Forge, NeoForge. Minecraft 26.1, 26.2, 26.3: Fabric, NeoForge.
  Minecraft 26.4-snapshot-10: Fabric.
- Needs to be installed on both the server and the client.
