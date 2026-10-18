<img width="1001" height="657" alt="QQ_1792311487161" src="https://github.com/user-attachments/assets/4e217efd-fc2d-4d6c-bda4-704ca3d65ed9" />

# Kursh

A save file editor for single-player Unity games on Windows.

Unity keeps most save data as JSON under `%USERPROFILE%\AppData\LocalLow\<company>\<product>`.
Kursh finds that folder, parses the files, and lets you edit numeric fields directly. It never
touches the running game — you edit the save while the game is closed, then start it again.

## Usage

1. Close the game.
2. Run `Kursh.exe`.
3. Paste the game folder (the one containing `GameName_Data`) or click Browse, then **Scan Saves**.
4. Pick a save from the dropdown.
5. Find the field: type part of its name (`pop`, `metal`) or part of its value (`3509`).
   The `int` / `float` / `bool` chips and the operator box (`=`, `>`, `<`, `1000~5000`, `≈`)
   narrow it down further.
6. Double-click the **New value** cell, type the number, press Enter.
7. Click **Save**. A `.kursh.bak` backup is written next to the file first.
8. Start the game and load the save.

## Notes

- Only JSON saves are parsed. Binary or encrypted saves are left alone.
- Values are replaced in place. Indentation, key order and every other field stay exactly as
  they were, so the game still reads the file normally. If something looks off, click
  **Restore** to roll back to the backup.
- Editing a save while the game is running usually gets overwritten when the game writes on
  exit. Close the game first.
- The interface is English or Chinese depending on the system language; the button in the
  title bar switches and remembers your choice.

## Build

Needs JDK 17 and JavaFX 17.

```
gradlew jar copyLibs
jpackage --type app-image --name Kursh ^
  --app-version 1.0.0 ^
  --vendor "RenF Games" ^
  --input build\package-libs ^
  --main-jar Kursh-1.0.0.jar ^
  --main-class yudxx.kursh.KurshApp ^
  --icon k.ico ^
  --dest dist ^
  --java-options "-Xmx1g"
```

The result is `dist\Kursh\`, which contains `Kursh.exe`, a bundled runtime and the app jars.
The folder is self-contained — copy it anywhere and run the exe.

## License

MIT. See [LICENSE](LICENSE).
