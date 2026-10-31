# Sinhala Buddhist Books (Android)

The Android app `lk.pitaka.books` on [Google Play](https://play.google.com/store/apps/details?id=lk.pitaka.books): the books
and the Dhammapada of [pitaka.lk](https://pitaka.lk/books/) offline, in a WebView that serves them at
`https://appassets.androidplatform.net/` (`MainActivity.java`).

The pages are not in this repo: `dev/sync-apps.sh` of [pitaka.lk](https://github.com/pathnirvana/pitaka.lk) copies the
built sites into `app/src/main/assets/` and the install-time asset packs (`jathaka_images_asset`: the Jathaka pictures,
`dhammapada_media_asset`: the Dhammapada paintings and recitations). Then `./gradlew bundleRelease` makes the signed
bundle (the key's settings are described in `app/build.gradle`), and `node dev/play/play-release.mjs` of pitaka.lk
uploads it to Google Play.

Privacy policy: https://github.com/pathnirvana/pitaka.lk/blob/master/PRIVACY.md
