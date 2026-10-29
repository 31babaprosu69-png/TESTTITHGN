# AdBlocker (Forge 1.20.1, server-side)

Blocks Minestrator-style ads: `tellraw`, `say`, `title`, `msg`, `tell`, `w`, `me`,
`teammsg`, `tm` commands (also inside `execute ... run`) and player chat that contain
`minestrator`, `flexcore`, `myboxfree` or `smartbackup` (case-insensitive).

## Get the jar without installing anything
1. Create a free GitHub repository.
2. Upload every file and folder from this project (including `.github`).
3. Open the **Actions** tab, wait for the `build` run to finish (3-5 min).
4. Open the run, download the `adblocker-jar` artifact, unzip it.
5. Put the `.jar` in your server's `mods` folder (Forge 1.20.1, server only).
