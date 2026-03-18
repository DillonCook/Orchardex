# Google Play Release Checklist

Use this list before submitting OrchardDex to Google Play.

## In The Codebase

- Build a signed release Android App Bundle with `bundleRelease`
- Verify the app opens cleanly on a fresh install
- Verify reminder notifications work on Android 13+ after permission is granted
- Verify export/import backups on a physical device
- Verify no developer-only features are visible in release builds
- Verify the in-app privacy policy matches the published privacy policy

## In Play Console

- Upload a signed AAB
- Complete the Data safety form
- Add a public, non-editable privacy policy URL
- Complete the app content questionnaires and content rating
- Add app category, contact details, and store listing copy
- Upload the required app icon, feature graphic, phone screenshots, and any tablet screenshots if applicable
- Confirm the app target API level meets the current Google Play requirement
- Review whether reminder notifications need clear store listing disclosure

## Local Release Assets To Prepare

- Keystore and signing config kept outside the repo
- Final version code and version name
- Privacy policy content
- Short description and full description
- App screenshots
- Feature graphic
