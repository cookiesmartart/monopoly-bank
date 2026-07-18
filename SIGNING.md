# Release signing

Every Android APK must be cryptographically signed. For an app distributed outside the Play
Store (GitHub Releases, F-Droid), **you** are responsible for creating and keeping that signing
key — there is no app store managing it for you.

## Why this matters

Android will only let a user install an update over an existing install if it's signed with the
**same key** as the version they already have. If you lose the keystore file or forget its
passwords, you permanently lose the ability to ship updates to anyone who already installed the
app under that key — their only path forward is to uninstall and reinstall a differently-signed
build, losing their local game data in the process (this app stores everything only on-device,
so there is no cloud backup to fall back on).

So: **generate it once, back up the `.jks` file and both passwords somewhere durable and
private (a password manager plus an offline copy), and never lose them.**

## 1. Generate the keystore

Run this once, on your own machine, with a real JDK installed (`keytool` ships with every JDK).
It will prompt you interactively for the passwords and your name/organization details — nothing
here is typed on the command line, so it won't end up in your shell history.

```sh
keytool -genkeypair -v \
  -keystore monopoly-bank-release.jks \
  -alias monopoly-bank \
  -keyalg RSA -keysize 2048 -validity 10000
```

- `-validity 10000` gives the key a ~27 year lifetime — deliberately long, since regenerating a
  release key later breaks updates for existing users (see above).
- Store the resulting `monopoly-bank-release.jks` file **outside** this repository, e.g. in your
  password manager's secure file storage or an encrypted backup drive.

## 2. Configure the project to use it

Copy the template and fill in your real values:

```sh
cp keystore.properties.example keystore.properties
```

Edit `keystore.properties`:

```properties
storeFile=/absolute/path/to/monopoly-bank-release.jks
storePassword=<the keystore password you chose>
keyAlias=monopoly-bank
keyPassword=<the key password you chose>
```

`keystore.properties` is already listed in `.gitignore` — it will never be committed. If it's
missing, `./gradlew assembleRelease` still works and produces an **unsigned** release APK (useful
for local testing); signing only kicks in once this file exists.

## 3. Build a signed release APK

```sh
./gradlew assembleRelease
```

The signed APK lands in `app/build/outputs/apk/release/`.

## CI builds (GitHub Actions)

The release workflow (`.github/workflows/release.yml`) builds and signs a release APK
automatically whenever you push a tag matching `v*` (e.g. `v1.0.0`), and attaches it to a new
GitHub Release. It reads the same four values as `keystore.properties` from GitHub Actions
secrets instead of a local file, so the keystore itself never needs to touch the repository or a
CI log.

In your GitHub repo, go to **Settings → Secrets and variables → Actions** and add:

| Secret name | Value |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | The `.jks` file, base64-encoded (see below) |
| `RELEASE_KEYSTORE_PASSWORD` | The keystore password you chose |
| `RELEASE_KEY_ALIAS` | `monopoly-bank` (or whatever alias you used) |
| `RELEASE_KEY_PASSWORD` | The key password you chose |

To base64-encode the keystore for the first secret:

```sh
base64 -w0 monopoly-bank-release.jks > keystore.b64   # Linux/macOS
certutil -encode monopoly-bank-release.jks keystore.b64  # Windows (strip the BEGIN/END lines after)
```

Paste the resulting text as the value of `RELEASE_KEYSTORE_BASE64`. Once the four secrets are
set, pushing a version tag (`git tag v1.0.0 && git push origin v1.0.0`) triggers the workflow.

Before tagging a new release, bump `versionCode` (always increasing) and `versionName` in
`app/build.gradle.kts` — the tag itself doesn't drive these, they're read from the build config.
