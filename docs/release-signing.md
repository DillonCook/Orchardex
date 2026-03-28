# Release Signing For OrcharDex

Release signing is the step that proves a release build really came from you.

For Google Play, the simplest model is:

- You create one private `upload` keystore on your machine.
- You use that keystore to sign the `.aab` you upload.
- Google Play App Signing manages the final app-signing key that users receive.

## What You Need

- A private upload keystore stored outside this repo
- A local `release-signing.properties` file in the repo root
- Android Studio or the JDK `keytool` command

## Recommended Path: Android Studio

1. Open the project in Android Studio.
2. Go to `Build > Generate Signed Bundle / APK`.
3. Choose `Android App Bundle`.
4. Click `Create new` under the keystore path field.
5. Save the keystore somewhere outside the repo, for example:
   `C:\Users\<you>\keys\orchardex-upload.jks`
6. Pick a key alias such as `orchardex-upload`.
7. Save the keystore password and key password in your password manager.
8. Cancel the wizard after the keystore is created if you only want to prepare signing first.

## Repo Setup

1. Copy [release-signing.properties.example](/C:/Users/Dillo/code/orchardex/release-signing.properties.example) to `release-signing.properties` in the repo root.
2. Fill in the real values.
3. Use an absolute keystore path or a path relative to the repo root.

Example:

```properties
ORCHARDEX_STORE_FILE=C:/Users/you/keys/orchardex-upload.jks
ORCHARDEX_STORE_PASSWORD=replace-me
ORCHARDEX_KEY_ALIAS=orchardex-upload
ORCHARDEX_KEY_PASSWORD=replace-me
```

## Build The Release Bundle

Windows:

```powershell
.\gradlew.bat bundleRelease
```

The signed bundle is written to:

`app/build/outputs/bundle/release/app-release.aab`

## Important Notes

- Do not commit `release-signing.properties`, `.jks`, or `.keystore` files.
- Back up the keystore and passwords. If you lose the upload key, updates get harder.
- When you create the app in Play Console, enroll in Play App Signing.

## If You Prefer Command Line

This creates an upload keystore with the JDK:

```powershell
keytool -genkeypair -v -keystore C:\Users\you\keys\orchardex-upload.jks -alias orchardex-upload -keyalg RSA -keysize 4096 -validity 10000
```
