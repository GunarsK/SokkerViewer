# SokkerViewer

A desktop app for [sokker.org](https://sokker.org) managers. It downloads your team's data and keeps every week of it, so you can follow your players' and juniors' skills, training and matches.

## Download

Get the zip for your computer from the [Releases page](https://github.com/GunarsK/SokkerViewer/releases):

| Computer | File |
|---|---|
| Windows, 64-bit | `sokker-<version>-win64-with-java.zip` |
| Mac with Apple silicon (M1 or newer) | `sokker-<version>-macos-arm64-with-java.zip` |
| Mac with an Intel processor | `sokker-<version>-macos-x64-with-java.zip` |

Java is included. Macs need macOS 11 Big Sur or newer.

## Install

SokkerViewer keeps all its data in its own folder, so unzip it somewhere you can write to, such as Documents or the Desktop (not Program Files).

### Windows

1. Unzip the file.
2. Run `SokkerViewer.exe` in the `SokkerViewer` folder.
3. If Windows says "Windows protected your PC", click **More info**, then **Run anyway**.

### macOS

1. Unzip the file.
2. Double-click `SokkerViewer.command` in the `SokkerViewer` folder. The first time, macOS blocks it, because the app is not signed: close the message with **Done** or **OK**.
3. Open **System Settings > Privacy & Security** (on macOS 11 and 12: **System Preferences > Security & Privacy**, General tab), scroll down if needed, click **Open Anyway** next to the message about `SokkerViewer.command`, and confirm.
4. If macOS asks whether Terminal may access the folder, click **Allow**.

SokkerViewer starts. A Terminal window opens next to it each time; you can close it. From then on, a double-click on `SokkerViewer.command` starts SokkerViewer.

## First start

Enter your sokker.org login and password. Tick **Update data?** to download your team straight away; later, **File > Update** brings in what has changed.

## Updating

Version 0.15.3 and newer update themselves. At startup SokkerViewer asks when a new version is out, and **Help > Check updates** (F12) checks at any time. It backs up your database, replaces the program files, keeps your data and starts again. To stop the startup question, untick **Check for updates at startup** in **Tools > Preferences > General**.

Older versions don't update themselves: install the new version and move your data over as below.

## Moving from an older install

1. Copy your old `db` folder somewhere safe as a backup.
2. Install the new version and copy the old `db` folder into its `SokkerViewer` folder, replacing the one there.
3. Start SokkerViewer and log in with the same sokker.org login.

## Your data

Everything is in the `SokkerViewer` folder: `db` holds your database, `bak` its backups, `settings` your preferences and `xml` the downloaded files. SokkerViewer backs up the database once a day and before every import and update. **File > Database backup** makes one now, and **File > Database restore** brings one back.

## Importing older data

**File > Import XML files** reads history saved by other apps: the `xml` folder of an older SokkerViewer, Sokker Organizer's files, or Sokker Asistente's files. Weeks your database already has are left as they are.

From Sokker Asistente, download these three files into one folder and pick that folder:

| File | Holds |
|---|---|
| `<team id>.properties` | your current players' weeks |
| `<team id>_historico.properties` | your former players' weeks |
| `_<your login>.properties` | the training types; only its training lines are read |

The junior files are not needed.
