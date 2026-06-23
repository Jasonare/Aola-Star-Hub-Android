# Aola Star Hub Android

Android Studio project for the mobile WebView client.

## Runtime Flow

1. The app starts with a local loading page.
2. It downloads `resource.zip` from Aliyun OSS into the phone's Downloads location:
   `https://oss-bucket-aola-hub.oss-cn-beijing.aliyuncs.com/aola/assets/resource.zip`
3. It extracts static resource folders into the app-specific Downloads directory:
   `<external app files>/Download/resources`
4. It loads the remote app page:
   `http://110.40.157.248:3030/aola-star.html`
5. WebView intercepts `/resource/...` requests and serves matched files from the local resource directory.

If the current `.resource-ready.json` already exists, the app skips download/extract and loads the remote page directly. If the ready marker is missing or was written by an older resource extraction format, but `Download/resource.zip` already exists, the app reuses that zip and only extracts it again.

## Build With Android Studio

1. Open `C:\AolaStarHub\Aola-Star-Hub-Android` in Android Studio.
2. Wait for Gradle sync to finish.
3. Use `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
4. APK output is under `app/build/outputs/apk/`.

## Notes

- The APK does not embed the 7GB `resource.zip`.
- The first launch needs network access and enough app storage for the zip plus extracted resources.
- `resource.zip` is kept after extraction so the user can inspect or reuse it later.
- Zip entry names are decoded with GBK to keep Chinese resource folders readable on Android.
- To force re-download, remove both `.resource-ready.json` and `Download/resource.zip`.
