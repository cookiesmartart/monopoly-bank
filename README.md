# Monopoly Bank

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Build release](https://github.com/cookiesmartart/monopoly-bank/actions/workflows/release.yml/badge.svg)](https://github.com/cookiesmartart/monopoly-bank/actions/workflows/release.yml)

A digital banker for board-game Monopoly nights. Add players, track balances, and move money
between them or the bank — no more counting paper bills or arguing about who owes what.

## About this project

This app was (largely) built with the help of AI ([Claude Code](https://claude.com/claude-code)).
The code, architecture decisions, and this README were generated through an AI-assisted
development process, with a human reviewing and directing each step. If you're browsing the
source or considering a contribution, you should know that going in.

## Features

- **Fully local, fully private.** No network access, no accounts, no cloud sync. Every balance
  and transaction lives only in a local database on your device.
- **Single device, pass-and-play.** All players share one phone — there's no multi-device
  syncing to set up.
- **Pick a mode per game.** Every new game starts with an explicit choice: **Manual** (on-screen
  keypad) or **NFC** (tap-to-pay). NFC mode is scan-only once chosen — there's no manual fallback
  mid-game, so pick Manual if you'd rather not use tags. Devices without NFC hardware only see the
  Manual option.
- **Manual banking.** Pay the bank, receive from the bank, or transfer between two players, with
  a large on-screen keypad and quick-amount buttons for common Monopoly note values.
- **NFC tap-to-pay.** Link a blank NTAG213/215/216 sticker to every player before the game starts,
  then just tap a tag: pick who it's with (defaults to the bank), enter the amount, confirm — one
  scan per transaction, no second tag or extra screens.
- **Negative balances allowed, with a warning.** Consistent with the real rules — a player can go
  into debt until they sell off property. The app flags it visually instead of blocking it.
- **Bankruptcy handling.** Once a player can't pay and has nothing left to sell or mortgage,
  declare them bankrupt: their remaining debt is written off and they're removed from the active
  game (past transactions stay visible in history). When one player remains, a winner screen
  appears automatically.
- **Transaction history**, both global and per player.
- **Configurable starting balance** (defaults to €1500, per the standard rules).
- **Light and dark mode**, plus optional sound/vibration feedback on transactions.
- **English, Dutch, and Spanish** included out of the box — switch anytime via the language icon
  on the main menu, or Settings → Language. Works on any supported Android version.

## Screenshots

_Coming soon — will be added once the first tagged release is built._

## Installation

Requires Android 7.0 (API 24) or newer.

1. Go to the [Releases page](https://github.com/cookiesmartart/monopoly-bank/releases) and
   download the latest `.apk`.
2. Android will likely warn you about installing from outside the Play Store. You'll need to
   allow "install from unknown sources" for your browser or file manager — Android will prompt
   you for this the first time, or you can enable it in advance under
   **Settings → Apps → Special access → Install unknown apps**.
3. Open the downloaded APK to install.

This app is not published on the Google Play Store. It's distributed only via GitHub Releases,
and optionally F-Droid in the future (see [FDROID.md](FDROID.md)).

## Privacy

This app collects no data, requires no internet connection, and stores nothing outside your own
device.

## How NFC mode works

1. Buy a handful of blank NTAG213/215/216 stickers (cheap and widely available) — one per player,
   or reuse a set across game nights.
2. Starting a new game, pick **NFC** on the main menu (only shown if your device has NFC
   hardware) instead of Manual.
3. Before play begins, you land on a **Link NFC tags** screen. Tap **Link tag** next to each
   player, then hold a blank sticker against the back of the phone. The app reads only the
   sticker's built-in factory ID — it never writes anything to the tag, so any blank sticker works
   immediately. Every player needs a linked tag before you can continue.
4. During play, the app is always listening: tap a tag to identify that player, pick who the money
   is with (defaults to the bank, or choose another player), enter the amount, and confirm — one
   scan per transaction, no second tag and no extra screens. Tags can be relinked or unlinked
   anytime via the NFC icon in the home screen's top bar.

NFC mode is scan-only by design once chosen for a game — there's no manual fallback mid-game. If
you'd rather use the on-screen keypad, pick **Manual** when starting a new game instead.

## Building from source

Requirements: JDK 17, the Android SDK (the Gradle wrapper will fetch a matching Gradle build
automatically).

```sh
./gradlew assembleRelease
```

This produces an **unsigned** release APK unless you've set up a signing key — see
[SIGNING.md](SIGNING.md) for how to generate one and configure it, and how the automated GitHub
Actions release build (`.github/workflows/release.yml`) signs tagged releases.

Only open-source, non-Google-Play-Services Gradle dependencies are used (see
`gradle/libs.versions.toml`) — no Firebase, no `google-services.json`, no analytics or ads SDKs.
This keeps the project buildable from source without any proprietary components, which is a hard
requirement for F-Droid inclusion.

## Running tests

```sh
./gradlew test
```

Unit tests cover the bank/transaction logic (balance calculations and validation) in
`domain/BankMath.kt` — see `app/src/test/java/.../domain/BankMathTest.kt`.

## Contributing

Issues and pull requests are welcome. Since this project was built with heavy AI assistance (see
[About this project](#about-this-project)), please review generated code with the same scrutiny
you'd apply to any other contribution.

## License

[MIT](LICENSE) — see the LICENSE file for the full text.
