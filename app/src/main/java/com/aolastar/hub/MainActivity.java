package com.aolastar.hub;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.app.DownloadManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {

    private static final String REMOTE_APP_URL = "http://110.40.157.248/aola-star.html";
    private static final String RESOURCE_ZIP_FILE_NAME = "resource.zip";
    private static final String RESOURCE_V7_ZIP_FILE_NAME = "resource-v7.zip";
    private static final String COS_RESOURCE_ZIP_URL = "https://aola-star-hub-1350639287.cos.ap-shanghai.myqcloud.com/resource.zip";
    private static final String SERVER_RESOURCE_ZIP_URL = "http://110.40.157.248/resource.zip";
    private static final String COS_RESOURCE_V7_ZIP_URL = "https://aola-star-hub-1350639287.cos.ap-shanghai.myqcloud.com/resource-v7.zip";
    private static final String SERVER_RESOURCE_V7_ZIP_URL = "http://110.40.157.248/resource-v7.zip";
    private static final String[] RESOURCE_ZIP_URLS = new String[] {
        COS_RESOURCE_ZIP_URL,
        SERVER_RESOURCE_ZIP_URL
    };
    private static final String[] RESOURCE_V7_ZIP_URLS = new String[] {
        COS_RESOURCE_V7_ZIP_URL,
        SERVER_RESOURCE_V7_ZIP_URL
    };
    private static final String READY_FILE = ".resource-ready.json";
    private static final String DOWNLOAD_READY_FILE = ".resource-download-ready.json";
    private static final String DOWNLOAD_V7_READY_FILE = ".resource-v7-download-ready.json";
    private static final String EXTRACT_READY_FILE = ".resource-extract-ready.json";
    private static final String EXTRACT_V7_READY_FILE = ".resource-v7-extract-ready.json";
    private static final String PREFS_NAME = "aola_resource_prefs";
    private static final String PREF_DOWNLOAD_ID = "resource_download_id";
    private static final String PREF_DOWNLOAD_URL = "resource_download_url";
    private static final String PREF_V7_DOWNLOAD_ID = "resource_v7_download_id";
    private static final String PREF_V7_DOWNLOAD_URL = "resource_v7_download_url";
    private static final String RESOURCE_ENTRY_CHARSET = "GBK";
    private static final int RESOURCE_READY_VERSION = 4;
    private static final String TAG = "AolaStarHub";
    private static final ResourcePackage BASE_RESOURCE_PACKAGE = new ResourcePackage(
        RESOURCE_ZIP_FILE_NAME,
        RESOURCE_ZIP_URLS,
        DOWNLOAD_READY_FILE,
        EXTRACT_READY_FILE,
        PREF_DOWNLOAD_ID,
        PREF_DOWNLOAD_URL,
        false
    );
    private static final ResourcePackage V7_RESOURCE_PACKAGE = new ResourcePackage(
        RESOURCE_V7_ZIP_FILE_NAME,
        RESOURCE_V7_ZIP_URLS,
        DOWNLOAD_V7_READY_FILE,
        EXTRACT_V7_READY_FILE,
        PREF_V7_DOWNLOAD_ID,
        PREF_V7_DOWNLOAD_URL,
        true
    );
    private static final String ANDROID_WEB_PATCH_CSS =
        "html.android-webview,html.android-webview body{width:auto!important;min-width:100vw!important;height:auto!important;min-height:100vh!important;overflow:auto!important;-webkit-overflow-scrolling:touch!important;}" +
        "html.android-webview #app{width:max-content!important;min-width:100vw!important;min-height:100vh!important;overflow:visible!important;}" +
        "html.android-webview .home-shell{width:max-content!important;min-width:100vw!important;min-height:100vh!important;justify-content:flex-start!important;overflow:visible!important;}" +
        "html.android-webview .home-hub-scene{width:max(calc(2524 * var(--ui-px)),1320px,calc(100vw - 16px))!important;height:max(calc(1315 * var(--ui-px)),688px,calc(100vh - 16px))!important;min-height:max(calc(1315 * var(--ui-px)),688px,calc(100vh - 16px))!important;max-width:none!important;max-height:none!important;flex:0 0 auto!important;background-image:url(\"./resource/ui/%E8%83%8C%E6%99%AF/hub-main.png\"),url(\"./resource/ui/Aola-Star-Hub-bg.png\")!important;background-size:100% 100%,cover!important;background-position:center!important;background-repeat:no-repeat!important;}" +
        "html.android-webview .home-hub-scene::after{z-index:0!important;}" +
        "html.android-webview .home-status-panel,html.android-webview .home-action-grid,html.android-webview .home-settings-btn,html.android-webview .home-double-reward{transform:translateZ(0);}";

    private static final Set<String> STATIC_RESOURCE_SEGMENTS = new HashSet<>(Arrays.asList(
        "BGM",
        "badges",
        "boss-level",
        "fight-ui",
        "hub \u5b88\u62a4\u8005\u8054\u76df\u52cb\u7ae0",
        "pet-action",
        "pet-img",
        "pet-state",
        "scene",
        "skill-effect",
        "skill-effect-fullscreen",
        "time-tunnel-environments",
        "type",
        "type-transparent",
        "\u5730\u53f0boss",
        "\u5c0f\u56fe\u6807",
        "\u52cb\u7ae0",
        "\u80cc\u5305ui",
        "\u88c5\u5907",
        "\u754c\u9762ui",
        "ui"
    ));

    private WebView webView;
    private FrameLayout loadingOverlay;
    private TextView loadingMessage;
    private TextView loadingPercent;
    private ProgressBar loadingProgress;
    private ValueCallback<Uri[]> filePathCallback;
    private ActivityResultLauncher<Intent> fileChooserLauncher;
    private ActivityResultLauncher<Intent> saveImportLauncher;
    private String exportFilename;
    private String exportMimeType;
    private StringBuilder exportBase64Builder;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyFullscreenWebViewWindow();
        setContentView(R.layout.activity_main);
        setupActivityLaunchers();

        webView = findViewById(R.id.webview);
        loadingOverlay = findViewById(R.id.loading_overlay);
        loadingMessage = findViewById(R.id.loading_message);
        loadingPercent = findViewById(R.id.loading_percent);
        loadingProgress = findViewById(R.id.loading_progress);
        webView.setBackgroundColor(Color.BLACK);
        webView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        webView.setHorizontalScrollBarEnabled(true);
        webView.setVerticalScrollBarEnabled(true);
        webView.setScrollbarFadingEnabled(false);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setAllowContentAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.clearCache(true);

        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, false);
        }

        webView.addJavascriptInterface(new AndroidBridge(), "AolaAndroid");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (loadingOverlay != null && loadingOverlay.getVisibility() == View.VISIBLE && newProgress < 100) {
                    showLoading("Loading remote page...", Math.max(1, newProgress), false);
                }
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                try {
                    fileChooserLauncher.launch(params.createIntent());
                } catch (Exception e) {
                    filePathCallback = null;
                    return false;
                }
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return tryOpenLocalResource(request.getUrl());
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return tryOpenLocalResource(Uri.parse(url));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                injectAndroidWebPatch();
                hideLoadingOverlay();
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                injectAndroidWebPatch();
                hideLoadingOverlay();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request != null && request.isForMainFrame()) {
                    String description = error == null ? "Unknown WebView error" : String.valueOf(error.getDescription());
                    showBootstrapError(new IllegalStateException("Remote page failed: " + description));
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                showBootstrapError(new IllegalStateException("Remote page failed: " + description));
            }
        });

        showLoading("Preparing local resources...", 0, false);
        bootstrapResourcesThenLoadRemote();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        });
    }

    private void setupActivityLaunchers() {
        fileChooserLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (filePathCallback == null) return;
            Uri[] uris = null;
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Intent data = result.getData();
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    uris = new Uri[count];
                    for (int i = 0; i < count; i++) uris[i] = data.getClipData().getItemAt(i).getUri();
                } else if (data.getData() != null) {
                    uris = new Uri[] { data.getData() };
                }
            }
            filePathCallback.onReceiveValue(uris);
            filePathCallback = null;
        });

        saveImportLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK || result.getData() == null || result.getData().getData() == null) return;
            try {
                String text = readUriText(result.getData().getData());
                runOnUiThread(() -> webView.evaluateJavascript("window.__aolaAndroidReceiveSaveJson && window.__aolaAndroidReceiveSaveJson(" + JSONObject.quote(text) + ")", null));
            } catch (Exception e) {
                runOnUiThread(() -> webView.evaluateJavascript("window.__aolaAndroidImportFailed && window.__aolaAndroidImportFailed(" + JSONObject.quote(e.getMessage()) + ")", null));
            }
        });
    }

    private void bootstrapResourcesThenLoadRemote() {
        new Thread(() -> {
            try {
                File resourceDir = getResourceDir();
                File readyFile = new File(resourceDir, READY_FILE);
                if (isReadyFileCurrent(readyFile, resourceDir)) {
                    showLoading("Local resources are ready.", 100, true);
                    loadRemoteApp();
                    return;
                }

                if (!isExtractReadyFileCurrent(BASE_RESOURCE_PACKAGE, resourceDir)) {
                    recreateResourceDir(resourceDir);
                } else {
                    ensureResourceDir(resourceDir);
                }

                ensureStaticResourcePackage(BASE_RESOURCE_PACKAGE, resourceDir);
                ensureStaticResourcePackage(V7_RESOURCE_PACKAGE, resourceDir);
                writeReadyFile(readyFile);
                showLoading("Local resources are ready.", 100, true);
                loadRemoteApp();
            } catch (Exception e) {
                showBootstrapError(e);
            }
        }, "aola-resource-bootstrap").start();
    }

    private void recreateResourceDir(File resourceDir) {
        deleteRecursively(resourceDir);
        if (!resourceDir.exists() && !resourceDir.mkdirs()) {
            throw new IllegalStateException("Cannot create resource directory.");
        }
    }

    private void ensureResourceDir(File resourceDir) {
        if (!resourceDir.exists() && !resourceDir.mkdirs()) {
            throw new IllegalStateException("Cannot create resource directory.");
        }
    }

    private void deleteRecursively(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) deleteRecursively(child);
            }
        }
        file.delete();
    }

    private void ensureStaticResourcePackage(ResourcePackage resourcePackage, File resourceDir) throws Exception {
        if (isExtractReadyFileCurrent(resourcePackage, resourceDir)) {
            showLoading(resourcePackage.fileName + " resources are ready.", 90, true);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Uri zipUri = findReusableDownloadZipUri(resourcePackage);
            if (zipUri == null) {
                zipUri = waitForDownloadManagerZip(resourcePackage);
            } else {
                showLoading("Using existing " + resourcePackage.fileName + "...", 84, true);
            }
            unzipStaticResources(zipUri, resourceDir, resourcePackage);
        } else {
            File zipFile = getDownloadZipFile(resourcePackage);
            if (!zipFile.isFile() || !isDownloadReadyFileCurrent(resourcePackage)) {
                if (zipFile.exists()) zipFile.delete();
                deleteDownloadReadyFile(resourcePackage);
                downloadFileWithFallback(resourcePackage, zipFile);
                writeDownloadReadyFile(resourcePackage);
            } else {
                showLoading("Using existing " + resourcePackage.fileName + "...", 84, true);
            }
            unzipStaticResources(zipFile, resourceDir, resourcePackage);
        }
        writeExtractReadyFile(resourcePackage, resourceDir);
    }

    private File getDownloadZipFile(ResourcePackage resourcePackage) throws Exception {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadDir.exists() && !downloadDir.mkdirs()) {
            throw new IllegalStateException("Cannot create Download directory.");
        }
        return new File(downloadDir, resourcePackage.fileName);
    }

    private Uri waitForDownloadManagerZip(ResourcePackage resourcePackage) throws Exception {
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager == null) throw new IllegalStateException("Download service is unavailable.");

        long downloadId = getStoredDownloadId(resourcePackage);
        String downloadUrl = getStoredDownloadUrl(resourcePackage);
        if (downloadId > 0 && !isKnownResourceZipUrl(resourcePackage, downloadUrl)) {
            manager.remove(downloadId);
            clearStoredDownloadId(resourcePackage);
            clearStoredDownloadUrl(resourcePackage);
            downloadId = -1L;
            downloadUrl = "";
        }

        Uri zipUri = getCompletedDownloadUri(manager, downloadId);
        if (zipUri != null) {
            writeDownloadReadyFile(resourcePackage);
            return zipUri;
        }

        DownloadProgress progress = getDownloadProgress(manager, downloadId);
        if (progress == null || progress.status == DownloadManager.STATUS_FAILED) {
            deleteDownloadReadyFile(resourcePackage);
            if (downloadId > 0) manager.remove(downloadId);
            downloadUrl = resourcePackage.urls[0];
            downloadId = enqueueResourceDownload(manager, resourcePackage, downloadUrl);
            storeDownloadId(resourcePackage, downloadId);
            storeDownloadUrl(resourcePackage, downloadUrl);
        }

        showDownloadManagerProgress(manager, downloadId, resourcePackage);
        while (true) {
            zipUri = getCompletedDownloadUri(manager, downloadId);
            if (zipUri != null) {
                writeDownloadReadyFile(resourcePackage);
                return zipUri;
            }
            progress = getDownloadProgress(manager, downloadId);
            if (progress == null || progress.status == DownloadManager.STATUS_FAILED) {
                deleteDownloadReadyFile(resourcePackage);
                clearStoredDownloadId(resourcePackage);
                String fallbackUrl = getFallbackResourceZipUrl(resourcePackage, downloadUrl);
                if (fallbackUrl == null) {
                    clearStoredDownloadUrl(resourcePackage);
                    throw new IllegalStateException("Background download failed. Please reopen the app to retry.");
                }
                manager.remove(downloadId);
                showLoading("Primary download failed, switching download source...", 1, true);
                downloadUrl = fallbackUrl;
                downloadId = enqueueResourceDownload(manager, resourcePackage, downloadUrl);
                storeDownloadId(resourcePackage, downloadId);
                storeDownloadUrl(resourcePackage, downloadUrl);
                showDownloadManagerProgress(manager, downloadId, resourcePackage);
                Thread.sleep(3000);
                continue;
            }
            showDownloadProgress(progress, resourcePackage);
            Thread.sleep(3000);
        }
    }

    private void showDownloadManagerProgress(DownloadManager manager, long downloadId, ResourcePackage resourcePackage) {
        DownloadProgress progress = getDownloadProgress(manager, downloadId);
        if (progress == null) {
            showLoading("Downloading " + resourcePackage.fileName + " in background...", 1, true);
            return;
        }
        showDownloadProgress(progress, resourcePackage);
    }

    private void showDownloadProgress(DownloadProgress progress, ResourcePackage resourcePackage) {
        if (progress.totalBytes > 0 && progress.downloadedBytes >= 0) {
            int percent = Math.max(1, Math.min(82, (int) ((progress.downloadedBytes * 82) / progress.totalBytes)));
            showLoading("Downloading " + resourcePackage.fileName + " " + formatMb(progress.downloadedBytes) + " / " + formatMb(progress.totalBytes), percent, false);
        } else if (progress.downloadedBytes > 0) {
            showLoading("Downloading " + resourcePackage.fileName + ", downloaded " + formatMb(progress.downloadedBytes), 8, true);
        } else {
            showLoading("Downloading " + resourcePackage.fileName + " in background...", 1, true);
        }
    }

    private long enqueueResourceDownload(DownloadManager manager, ResourcePackage resourcePackage, String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle("Aola Star Hub " + resourcePackage.fileName);
        request.setDescription("Downloading game resources");
        request.setMimeType("application/zip");
        request.setAllowedOverMetered(true);
        request.setAllowedOverRoaming(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, resourcePackage.fileName);
        return manager.enqueue(request);
    }

    @Nullable
    private Uri getCompletedDownloadUri(DownloadManager manager, long downloadId) {
        if (downloadId <= 0) return null;
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        try (Cursor cursor = manager.query(query)) {
            if (cursor == null || !cursor.moveToFirst()) return null;
            int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
            if (status != DownloadManager.STATUS_SUCCESSFUL) return null;
            String uri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
            return uri == null ? manager.getUriForDownloadedFile(downloadId) : Uri.parse(uri);
        }
    }

    @Nullable
    private DownloadProgress getDownloadProgress(DownloadManager manager, long downloadId) {
        if (downloadId <= 0) return null;
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        try (Cursor cursor = manager.query(query)) {
            if (cursor == null || !cursor.moveToFirst()) return null;
            int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
            long downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            long total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            return new DownloadProgress(status, downloaded, total);
        }
    }

    private static class DownloadProgress {
        final int status;
        final long downloadedBytes;
        final long totalBytes;

        DownloadProgress(int status, long downloadedBytes, long totalBytes) {
            this.status = status;
            this.downloadedBytes = downloadedBytes;
            this.totalBytes = totalBytes;
        }
    }

    private static class ResourcePackage {
        final String fileName;
        final String[] urls;
        final String downloadReadyFileName;
        final String extractReadyFileName;
        final String prefDownloadId;
        final String prefDownloadUrl;
        final boolean extractAllEntries;

        ResourcePackage(String fileName, String[] urls, String downloadReadyFileName, String extractReadyFileName, String prefDownloadId, String prefDownloadUrl, boolean extractAllEntries) {
            this.fileName = fileName;
            this.urls = urls;
            this.downloadReadyFileName = downloadReadyFileName;
            this.extractReadyFileName = extractReadyFileName;
            this.prefDownloadId = prefDownloadId;
            this.prefDownloadUrl = prefDownloadUrl;
            this.extractAllEntries = extractAllEntries;
        }
    }

    private long getStoredDownloadId(ResourcePackage resourcePackage) {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getLong(resourcePackage.prefDownloadId, -1L);
    }

    private void storeDownloadId(ResourcePackage resourcePackage, long downloadId) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putLong(resourcePackage.prefDownloadId, downloadId).apply();
    }

    private void clearStoredDownloadId(ResourcePackage resourcePackage) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().remove(resourcePackage.prefDownloadId).apply();
    }

    private String getStoredDownloadUrl(ResourcePackage resourcePackage) {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(resourcePackage.prefDownloadUrl, "");
    }

    private void storeDownloadUrl(ResourcePackage resourcePackage, String url) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(resourcePackage.prefDownloadUrl, url).apply();
    }

    private void clearStoredDownloadUrl(ResourcePackage resourcePackage) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().remove(resourcePackage.prefDownloadUrl).apply();
    }

    private boolean isKnownResourceZipUrl(ResourcePackage resourcePackage, String url) {
        if (url == null || url.isEmpty()) return false;
        for (String resourceZipUrl : resourcePackage.urls) {
            if (resourceZipUrl.equals(url)) return true;
        }
        return false;
    }

    @Nullable
    private String getFallbackResourceZipUrl(ResourcePackage resourcePackage, String currentUrl) {
        if (currentUrl == null || currentUrl.isEmpty()) return null;
        for (int i = 0; i < resourcePackage.urls.length - 1; i++) {
            if (resourcePackage.urls[i].equals(currentUrl)) return resourcePackage.urls[i + 1];
        }
        return null;
    }

    @Nullable
    private Uri findReusableDownloadZipUri(ResourcePackage resourcePackage) {
        ContentResolver resolver = getContentResolver();
        String[] projection = new String[] { MediaStore.Downloads._ID };
        String selection = MediaStore.Downloads.DISPLAY_NAME + "=? AND " + MediaStore.Downloads.RELATIVE_PATH + "=?";
        String[] args = new String[] { resourcePackage.fileName, Environment.DIRECTORY_DOWNLOADS + "/" };
        try (Cursor cursor = resolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI, projection, selection, args, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                Uri uri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cursor.getLong(0));
                if (isDownloadReadyFileCurrent(resourcePackage)) return uri;
                resolver.delete(uri, null, null);
            }
            return null;
        }
    }

    private void downloadFile(ResourcePackage resourcePackage, String url, File outFile) throws Exception {
        File parent = outFile.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        try (OutputStream output = new BufferedOutputStream(new FileOutputStream(outFile))) {
            downloadFile(url, output, resourcePackage.fileName);
        }
    }

    private void downloadFileWithFallback(ResourcePackage resourcePackage, File outFile) throws Exception {
        Exception lastError = null;
        for (String url : resourcePackage.urls) {
            try {
                if (outFile.exists()) outFile.delete();
                showLoading("Downloading " + resourcePackage.fileName + "...", 1, false);
                downloadFile(resourcePackage, url, outFile);
                storeDownloadUrl(resourcePackage, url);
                return;
            } catch (Exception e) {
                lastError = e;
                if (outFile.exists()) outFile.delete();
                showLoading("Download source failed, trying fallback...", 1, true);
            }
        }
        throw lastError == null ? new IllegalStateException("No " + resourcePackage.fileName + " download source is available.") : lastError;
    }

    private void downloadFile(String url, OutputStream output, String fileName) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setInstanceFollowRedirects(true);
        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IllegalStateException("Download failed: HTTP " + code);
        }
        long total = conn.getContentLengthLong();
        long downloaded = 0;
        showLoading(total > 0 ? "Downloading " + fileName + "..." : "Downloading " + fileName + ", downloaded 0 MB", 1, false);
        try (InputStream input = new BufferedInputStream(conn.getInputStream());
             OutputStream out = output) {
            byte[] buffer = new byte[1024 * 256];
            int len;
            long lastUpdate = 0;
            while ((len = input.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
                downloaded += len;
                long now = System.currentTimeMillis();
                if (now - lastUpdate > 500) {
                    lastUpdate = now;
                    if (total > 0) {
                        int percent = Math.max(1, Math.min(82, (int) ((downloaded * 82) / total)));
                        showLoading("Downloading " + fileName + " " + formatMb(downloaded) + " / " + formatMb(total), percent, false);
                    } else {
                        showLoading("Downloading " + fileName + ", downloaded " + formatMb(downloaded), 8, true);
                    }
                }
            }
        } finally {
            conn.disconnect();
        }
    }

    private void unzipStaticResources(File zipFile, File resourceDir, ResourcePackage resourcePackage) throws Exception {
        try (InputStream input = new BufferedInputStream(new FileInputStream(zipFile))) {
            unzipStaticResources(input, resourceDir, resourcePackage);
        }
    }

    private void unzipStaticResources(Uri zipUri, File resourceDir, ResourcePackage resourcePackage) throws Exception {
        InputStream rawInput;
        if ("file".equalsIgnoreCase(zipUri.getScheme())) {
            rawInput = new FileInputStream(new File(zipUri.getPath()));
        } else {
            rawInput = getContentResolver().openInputStream(zipUri);
        }
        if (rawInput == null) throw new IllegalStateException("Cannot read Download/" + resourcePackage.fileName + ".");
        try (InputStream input = new BufferedInputStream(rawInput)) {
            unzipStaticResources(input, resourceDir, resourcePackage);
        }
    }

    private void unzipStaticResources(InputStream zipInput, File resourceDir, ResourcePackage resourcePackage) throws Exception {
        showLoading("Extracting " + resourcePackage.fileName + "...", 88, true);
        int extracted = 0;
        try (ZipInputStream zip = new ZipInputStream(zipInput, Charset.forName(RESOURCE_ENTRY_CHARSET))) {
            ZipEntry entry;
            String rootPath = resourceDir.getCanonicalPath() + File.separator;
            Set<String> extractedPaths = new HashSet<>();
            while ((entry = zip.getNextEntry()) != null) {
                String rel = getStaticResourceEntryPath(entry.getName(), resourcePackage);
                if (rel == null) {
                    zip.closeEntry();
                    continue;
                }
                if (!extractedPaths.add(rel)) {
                    zip.closeEntry();
                    continue;
                }
                File target = new File(resourceDir, rel);
                String targetPath = target.getCanonicalPath();
                if (!targetPath.startsWith(rootPath)) {
                    zip.closeEntry();
                    continue;
                }
                if (entry.isDirectory()) {
                    target.mkdirs();
                    zip.closeEntry();
                    continue;
                }
                File parent = target.getParentFile();
                if (parent != null && !parent.exists()) parent.mkdirs();
                try (OutputStream output = new BufferedOutputStream(new FileOutputStream(target))) {
                    byte[] buffer = new byte[1024 * 256];
                    int len;
                    while ((len = zip.read(buffer)) >= 0) {
                        output.write(buffer, 0, len);
                    }
                }
                zip.closeEntry();
                extracted += 1;
                if (extracted % 200 == 0) {
                    showLoading("Extracting " + resourcePackage.fileName + ", extracted " + extracted + " files", 88, true);
                }
            }
        }
        if (extracted == 0) throw new IllegalStateException("No static resources were found in " + resourcePackage.fileName + ".");
    }

    @Nullable
    private String getStaticResourceEntryPath(String entryName, ResourcePackage resourcePackage) {
        String normalized = entryName == null ? "" : entryName.replace('\\', '/');
        while (normalized.startsWith("/")) normalized = normalized.substring(1);
        if (normalized.length() == 0 || normalized.contains("../")) return null;
        String[] parts = normalized.split("/");
        if (resourcePackage.extractAllEntries) {
            String directPath = getDirectResourceEntryPath(parts, getZipRootName(resourcePackage.fileName));
            return directPath == null || directPath.length() == 0 ? null : directPath;
        }
        for (int i = 0; i < parts.length; i++) {
            if (STATIC_RESOURCE_SEGMENTS.contains(parts[i])) {
                StringBuilder out = new StringBuilder();
                for (int j = i; j < parts.length; j++) {
                    if (j > i) out.append('/');
                    out.append(parts[j]);
                }
                return out.toString();
            }
        }
        return null;
    }

    @Nullable
    private String getDirectResourceEntryPath(String[] parts, String packageRootName) {
        if (parts == null || parts.length == 0) return null;
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if ("resource".equalsIgnoreCase(part) || "resources".equalsIgnoreCase(part) || packageRootName.equalsIgnoreCase(part)) {
                return joinPathParts(parts, i + 1);
            }
        }
        return joinPathParts(parts, 0);
    }

    private String joinPathParts(String[] parts, int start) {
        StringBuilder out = new StringBuilder();
        for (int i = start; i < parts.length; i++) {
            if (parts[i].length() == 0) return "";
            if (i > start) out.append('/');
            out.append(parts[i]);
        }
        return out.toString();
    }

    private String getZipRootName(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    @Nullable
    private WebResourceResponse tryOpenLocalResource(Uri uri) {
        try {
            if (uri == null) return null;
            String path = URLDecoder.decode(uri.getPath() == null ? "" : uri.getPath(), StandardCharsets.UTF_8.name());
            while (path.startsWith("/")) path = path.substring(1);
            if (path.toLowerCase(Locale.ROOT).startsWith("resource/")) path = path.substring("resource/".length());
            String rel = getRequestedResourcePath(path);
            if (rel == null) return null;
            File resourceDir = getResourceDir();
            File file = resolveLocalResourceFile(resourceDir, rel);
            String rootPath = resourceDir.getCanonicalPath() + File.separator;
            String filePath = file.getCanonicalPath();
            if (!filePath.startsWith(rootPath) || !file.isFile()) {
                Log.d(TAG, "resource miss: " + uri + " -> " + filePath);
                return null;
            }
            Map<String, String> headers = new HashMap<>();
            headers.put("Access-Control-Allow-Origin", "*");
            headers.put("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.put("Pragma", "no-cache");
            headers.put("Expires", "0");
            Log.d(TAG, "resource hit: " + uri + " -> " + filePath);
            return new WebResourceResponse(getMimeType(file), null, 200, "OK", headers, new FileInputStream(file));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private String getRequestedResourcePath(String path) {
        String normalized = path == null ? "" : path.replace('\\', '/');
        while (normalized.startsWith("/")) normalized = normalized.substring(1);
        if (normalized.length() == 0 || normalized.contains("../")) return null;
        String[] parts = normalized.split("/");
        return joinPathParts(parts, 0);
    }

    private File resolveLocalResourceFile(File resourceDir, String rel) {
        File direct = new File(resourceDir, rel);
        if (direct.isFile()) return direct;
        if (rel != null && rel.endsWith("/hub-main.png")) {
            File fallback = findFileByName(resourceDir, "hub-main.png");
            if (fallback != null) return fallback;
        }
        return direct;
    }

    @Nullable
    private File findFileByName(File dir, String filename) {
        if (dir == null || filename == null || !dir.isDirectory()) return null;
        File[] children = dir.listFiles();
        if (children == null) return null;
        for (File child : children) {
            if (child.isFile() && filename.equals(child.getName())) return child;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                File found = findFileByName(child, filename);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void showLoading(String message, int percent, boolean indeterminate) {
        int safePercent = Math.max(0, Math.min(100, percent));
        runOnUiThread(() -> {
            if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
            if (loadingMessage != null) loadingMessage.setText(message);
            if (loadingProgress != null) {
                loadingProgress.setIndeterminate(indeterminate);
                if (!indeterminate) loadingProgress.setProgress(safePercent);
            }
            if (loadingPercent != null) loadingPercent.setText(indeterminate ? "" : safePercent + "%");
        });
    }

    private void hideLoadingOverlay() {
        runOnUiThread(() -> {
            if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
        });
    }

    private void showBootstrapError(Exception e) {
        String message = e == null || e.getMessage() == null ? "Unknown error" : e.getMessage();
        String html = "<!doctype html><html><head><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">" +
            "<style>html,body{margin:0;width:100%;height:100%;background:#020617;color:#fecaca;font-family:monospace}body{display:flex;align-items:center;justify-content:center;padding:20px;box-sizing:border-box}.box{max-width:720px;white-space:pre-wrap}</style></head><body><div class=\"box\">Resource bootstrap failed:\\n" + escapeHtml(message) + "</div></body></html>";
        runOnUiThread(() -> {
            if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
            webView.loadDataWithBaseURL("https://aola.local/error/", html, "text/html", "UTF-8", null);
        });
    }

    private void loadRemoteApp() {
        showLoading("Connecting to server...", 1, false);
        runOnUiThread(() -> webView.loadUrl(REMOTE_APP_URL));
    }

    private String fetchText(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setInstanceFollowRedirects(true);
        try {
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new IllegalStateException("Remote page failed: HTTP " + code);
            }
            try (InputStream input = new BufferedInputStream(conn.getInputStream());
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = input.read(buffer)) >= 0) output.write(buffer, 0, len);
                return output.toString(StandardCharsets.UTF_8.name());
            }
        } finally {
            conn.disconnect();
        }
    }

    private String injectAndroidWebPatch(String html) {
        String patch = "<style id=\"aola-android-web-patch\">" + ANDROID_WEB_PATCH_CSS + "</style>";
        int headEnd = html == null ? -1 : html.indexOf("</head>");
        if (headEnd >= 0) {
            return html.substring(0, headEnd) + patch + html.substring(headEnd);
        }
        return patch + String.valueOf(html);
    }

    private void injectAndroidWebPatch() {
        String script = "(function(){var id='aola-android-web-patch';if(document.getElementById(id))return;var style=document.createElement('style');style.id=id;style.textContent=" + JSONObject.quote(ANDROID_WEB_PATCH_CSS) + ";document.head.appendChild(style);})();";
        webView.evaluateJavascript(script, null);
    }

    private void writeReadyFile(File readyFile) throws Exception {
        String json = "{\"ok\":true,\"version\":" + RESOURCE_READY_VERSION
            + ",\"resourceZipUrl\":\"" + getStoredDownloadUrl(BASE_RESOURCE_PACKAGE)
            + "\",\"resourceV7ZipUrl\":\"" + getStoredDownloadUrl(V7_RESOURCE_PACKAGE)
            + "\",\"resourceEntryCharset\":\"" + RESOURCE_ENTRY_CHARSET
            + "\",\"finishedAt\":" + System.currentTimeMillis() + "}";
        try (OutputStream output = new FileOutputStream(readyFile)) {
            output.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void writeDownloadReadyFile(ResourcePackage resourcePackage) throws Exception {
        File readyFile = getDownloadReadyFile(resourcePackage);
        String json = "{\"ok\":true,\"fileName\":\"" + resourcePackage.fileName
            + "\",\"resourceZipUrl\":\"" + getStoredDownloadUrl(resourcePackage)
            + "\",\"finishedAt\":" + System.currentTimeMillis() + "}";
        try (OutputStream output = new FileOutputStream(readyFile)) {
            output.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }

    private boolean isDownloadReadyFileCurrent(ResourcePackage resourcePackage) {
        File readyFile = getDownloadReadyFile(resourcePackage);
        if (!readyFile.isFile()) return false;
        try {
            JSONObject json = new JSONObject(readTextFile(readyFile));
            String resourceZipUrl = json.optString("resourceZipUrl", "");
            boolean isCurrent = json.optBoolean("ok", false)
                && resourcePackage.fileName.equals(json.optString("fileName", resourcePackage.fileName))
                && isKnownResourceZipUrl(resourcePackage, resourceZipUrl);
            if (isCurrent) storeDownloadUrl(resourcePackage, resourceZipUrl);
            return isCurrent;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void deleteDownloadReadyFile(ResourcePackage resourcePackage) {
        File readyFile = getDownloadReadyFile(resourcePackage);
        if (readyFile.exists()) readyFile.delete();
    }

    private File getDownloadReadyFile(ResourcePackage resourcePackage) {
        return new File(getResourceBaseDir(), resourcePackage.downloadReadyFileName);
    }

    private void writeExtractReadyFile(ResourcePackage resourcePackage, File resourceDir) throws Exception {
        File readyFile = getExtractReadyFile(resourcePackage, resourceDir);
        String json = "{\"ok\":true,\"fileName\":\"" + resourcePackage.fileName
            + "\",\"version\":" + RESOURCE_READY_VERSION
            + "\",\"resourceZipUrl\":\"" + getStoredDownloadUrl(resourcePackage)
            + "\",\"resourceEntryCharset\":\"" + RESOURCE_ENTRY_CHARSET
            + "\",\"finishedAt\":" + System.currentTimeMillis() + "}";
        try (OutputStream output = new FileOutputStream(readyFile)) {
            output.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }

    private boolean isExtractReadyFileCurrent(ResourcePackage resourcePackage, File resourceDir) {
        File readyFile = getExtractReadyFile(resourcePackage, resourceDir);
        if (!readyFile.isFile()) return false;
        try {
            JSONObject json = new JSONObject(readTextFile(readyFile));
            return json.optBoolean("ok", false)
                && json.optInt("version", 0) == RESOURCE_READY_VERSION
                && resourcePackage.fileName.equals(json.optString("fileName", ""))
                && RESOURCE_ENTRY_CHARSET.equalsIgnoreCase(json.optString("resourceEntryCharset", ""));
        } catch (Exception ignored) {
            return false;
        }
    }

    private File getExtractReadyFile(ResourcePackage resourcePackage, File resourceDir) {
        return new File(resourceDir, resourcePackage.extractReadyFileName);
    }

    private boolean isReadyFileCurrent(File readyFile, File resourceDir) {
        if (readyFile == null || !readyFile.isFile()) return false;
        try {
            JSONObject json = new JSONObject(readTextFile(readyFile));
            return json.optBoolean("ok", false)
                && json.optInt("version", 0) == RESOURCE_READY_VERSION
                && RESOURCE_ENTRY_CHARSET.equalsIgnoreCase(json.optString("resourceEntryCharset", ""))
                && isExtractReadyFileCurrent(BASE_RESOURCE_PACKAGE, resourceDir)
                && isExtractReadyFileCurrent(V7_RESOURCE_PACKAGE, resourceDir);
        } catch (Exception ignored) {
            return false;
        }
    }

    private String readTextFile(File file) throws Exception {
        try (InputStream input = new BufferedInputStream(new FileInputStream(file));
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = input.read(buffer)) >= 0) output.write(buffer, 0, len);
            return output.toString(StandardCharsets.UTF_8.name());
        }
    }

    private File getResourceDir() {
        return new File(getResourceBaseDir(), "resources");
    }

    private File getResourceBaseDir() {
        File downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        return downloadDir == null ? getFilesDir() : downloadDir;
    }

    private String formatMb(long bytes) {
        return String.format(Locale.US, "%.1f MB", bytes / 1024.0 / 1024.0);
    }

    private String escapeHtml(String value) {
        return String.valueOf(value)
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }

    private String getMimeType(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".css")) return "text/css";
        if (name.endsWith(".gif")) return "image/gif";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".js")) return "text/javascript";
        if (name.endsWith(".json")) return "application/json";
        if (name.endsWith(".mp3")) return "audio/mpeg";
        if (name.endsWith(".ogg")) return "audio/ogg";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".svg")) return "image/svg+xml";
        if (name.endsWith(".txt")) return "text/plain";
        if (name.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) applyFullscreenWebViewWindow();
    }

    private void applyFullscreenWebViewWindow() {
        Window window = getWindow();
        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(params);
        }

        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private String readUriText(Uri uri) throws Exception {
        try (InputStream input = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (input == null) throw new IllegalStateException("Cannot read file.");
            byte[] buffer = new byte[8192];
            int len;
            while ((len = input.read(buffer)) >= 0) output.write(buffer, 0, len);
            return output.toString(StandardCharsets.UTF_8.name());
        }
    }

    private String writeDownloadFile(String filename, String mimeType, byte[] data) throws Exception {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
        values.put(MediaStore.Downloads.MIME_TYPE, mimeType);
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Aola Star Hub");
        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) throw new IllegalStateException("Cannot create export file.");
        try (OutputStream output = resolver.openOutputStream(uri)) {
            if (output == null) throw new IllegalStateException("Cannot write export file.");
            output.write(data);
        }
        return uri.toString();
    }

    public class AndroidBridge {
        @JavascriptInterface
        public String exportSaveJson(String filename, String text) {
            try {
                return "OK|" + writeDownloadFile(filename, "application/json", text.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                return "ERR|" + e.getMessage();
            }
        }

        @JavascriptInterface
        public String exportBlobBase64(String filename, String mimeType, String base64) {
            try {
                byte[] data = Base64.decode(base64, Base64.DEFAULT);
                return "OK|" + writeDownloadFile(filename, mimeType, data);
            } catch (Exception e) {
                return "ERR|" + e.getMessage();
            }
        }

        @JavascriptInterface
        public synchronized String beginBlobExport(String filename, String mimeType) {
            exportFilename = filename;
            exportMimeType = mimeType;
            exportBase64Builder = new StringBuilder();
            return "OK";
        }

        @JavascriptInterface
        public synchronized String appendBlobExportBase64(String chunk) {
            if (exportBase64Builder == null) return "ERR|Export has not started.";
            exportBase64Builder.append(chunk);
            return "OK";
        }

        @JavascriptInterface
        public synchronized String finishBlobExportBase64() {
            if (exportBase64Builder == null) return "ERR|Export has not started.";
            try {
                byte[] data = Base64.decode(exportBase64Builder.toString(), Base64.DEFAULT);
                String uri = writeDownloadFile(exportFilename, exportMimeType, data);
                exportFilename = null;
                exportMimeType = null;
                exportBase64Builder = null;
                return "OK|" + uri;
            } catch (Exception e) {
                exportFilename = null;
                exportMimeType = null;
                exportBase64Builder = null;
                return "ERR|" + e.getMessage();
            }
        }

        @JavascriptInterface
        public void importSaveJson() {
            runOnUiThread(() -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] { "application/json", "text/plain", "application/octet-stream" });
                saveImportLauncher.launch(intent);
            });
        }
    }
}
