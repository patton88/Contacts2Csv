package com.example.contacts2csv;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import androidx.loader.content.CursorLoader;

import static com.example.contacts2csv.MainActivity.m_Fun;

/**
 * @author glsite.com
 * @version 1.0.0$
 * @des Get file path from Uri
 * @updateAuthor MinJun$
 * @updateDes Get file path from Uri
 */

// 原文链接：https://blog.csdn.net/u011077027/article/details/90052610
public class FileUriUtils {

    //测试显示，在低于API19的系统中，无法通过系统文件管理器获取文本文件路径
    //e:\Users\WinUser01\.android\avd\Pixel_2_API_18.avd\
    //运行成功，导出成功，点击浏览只能选择music和Gallery，无法选择文本文件
    //e:\Users\WinUser01\.android\avd\Pixel_2_API_17.avd\
    //运行成功，导出成功，点击浏览只能选择music和Gallery，无法选择文本文件
    //e:\Users\WinUser01\.android\avd\Pixel_2_API_16.avd\
    //运行成功，导出成功，点击浏览只能选择music和Gallery，无法选择文本文件
    //e:\Users\WinUser01\.android\avd\Pixel_2_API_15.avd\
    //运行成功，导出成功，只能选择music和Gallery，无法选择文本文件，点击浏览app闪退

    //System.out.println("Build.VERSION.SDK_INT = " + Build.VERSION.SDK_INT);
    //Build.VERSION.SDK_INT = 22
    //if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {   // 大于 19 (Android 4.4)

    // 复杂版本处理(适配多种API)
    public static String getRealPathFromUri(Context context, Uri uri) {
        String filepath = "";
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion < Build.VERSION_CODES.HONEYCOMB) {       // 适配 API 1 - 10
            filepath = getRealPathFromUri_BelowApi11(context, uri);
        } else if (sdkVersion < Build.VERSION_CODES.KITKAT) {   // 适配 API 11 - 18
            filepath = getRealPathFromUri_Api11To18(context, uri);
        } else if (sdkVersion < Build.VERSION_CODES.N) {        // 适配 API 19 - 23
            filepath = getRealPathFromUri_AboveApi19(context, uri);
        } else {                                                // 适配 API 24 及以上版本
            filepath = getFilePathForN(context, uri);
        }

        //m_Fun.logString(filepath);
        //String = null
        //String = storage/sdcard/Android/data/com.example.contacts2csv/files/Download/Contacts_2.txt
        return filepath;
    }

    /**
     * 适配 API 19 - 23,根据uri获取图片的绝对路径
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getRealPathFromUri_AboveApi19(Context context, Uri uri) {
        String filePath = "";
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    filePath = Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {
                    // TODO handle non-primary volumes
                    if (Build.VERSION.SDK_INT > 20) {
                        //getExternalMediaDirs() added in API 21
                        File extenal[] = context.getExternalMediaDirs();
                        if (extenal.length > 0) {
                            filePath = extenal[0].getAbsolutePath();
                            filePath = filePath.substring(0, filePath.indexOf("Android")) + split[1];
                        }
                    } else {
                        //filePath = "/storage/" + type + "/" + split[1];
                        filePath = Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                filePath = getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else {
                    contentUri = MediaStore.Files.getContentUri("external");
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                filePath = getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        return filePath;
    }

    /**
     * 适用于 API 24 android7.0 及以上处理方法
     */
    private static String getFilePathForN(Context context, Uri uri) {
        String filePath = "";
        try {
            Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String name = (returnCursor.getString(nameIndex));
            File file = new File(context.getFilesDir(), name);
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            returnCursor.close();
            inputStream.close();
            outputStream.close();
            filePath = file.getPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }

    /**
     * 适配 API 11 -18，根据uri获取图片的绝对路径
     */
    private static String getRealPathFromUri_Api11To18(Context context, Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        //这个有两个包不知道是哪个。。。。不过这个复杂版一般用不到
        CursorLoader loader = new CursorLoader(context, uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();

        if (cursor != null) {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
            cursor.close();
        }
        return filePath;
    }

    /**
     * 适配 API11 以下(不包括 API11),根据uri获取图片的绝对路径
     */
    private static String getRealPathFromUri_BelowApi11(Context context, Uri uri) {
        String filePath = "";
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
            cursor.close();
        }
        return filePath;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String dataColumn = "";
        Cursor cursor = null;
        String column = MediaStore.MediaColumns.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(column);
                dataColumn = cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return dataColumn;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
