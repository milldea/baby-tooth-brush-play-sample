package com.example.toothbrush;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScreenshotUtils {

    // スクリーンショットを保存するメソッド
    public static void takeScreenshot(Context context, View view) {
        // スクリーンショットを取得するViewのサイズを取得
        int width = view.getWidth();
        int height = view.getHeight();

        // ViewからBitmapを作成
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));

        // スクリーンショットを保存するディレクトリを作成
        File screenshotsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ToothBrushApp");
        if (!screenshotsDir.exists()) {
            screenshotsDir.mkdirs();
        }
        // タイムスタンプ付きのファイル名を作成
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String filename = "screenshot_" + timestamp + ".png";
        File file = new File(screenshotsDir, filename);

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        MediaScannerConnection.scanFile(context,
                new String[]{file.getAbsolutePath()},
                null,
                (path, uri) -> {
                    Log.i("MediaScanner", "Scanned " + path + ":");
                    Log.i("MediaScanner", "-> uri=" + uri);
                });
    }
}