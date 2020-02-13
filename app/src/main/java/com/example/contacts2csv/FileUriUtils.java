package com.example.contacts2csv;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;

/**
 * @author glsite.com
 * @version 1.0.0$
 * @des Get file path from Uri
 * @updateAuthor MinJun$
 * @updateDes Get file path from Uri
 */

public class FileUriUtils {
    /**
     * 专为 大于 19 Android4.4 设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {
        String filePath = "";
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        //System.out.println("uri = " + uri.toString());  //content://com.android.externalstorage.documents/document/
        //System.out.println("Environment.getExternalStoragePublicDirectory = " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
        //storage/sdcard/Documents

        //System.out.println("isKitKat = " + isKitKat);   //true
        //System.out.println("DocumentsContract.isDocumentUri(context, uri)) = " + DocumentsContract.isDocumentUri(context, uri));   //true
        // DocumentProvider
        // ExternalStorageProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            //System.out.println("isExternalStorageDocument(uri) = " + isExternalStorageDocument(uri));   //true
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                //for (int i = 0; i < split.length; i++) {
                //    System.out.println("split[" + i + "] = " + split[i]);
                //}
                //I/System.out: split[0] = 1911-340C
                //I/System.out: split[1] = Android/data/com.example.contacts2csv/files/Download/Group_1.txt
                //我勒个去1113-080E这是个啥呀

                //System.out.println("Environment.getExternalStorageDirectory() = " + Environment.getExternalStorageDirectory());
                //Environment.getExternalStorageDirectory() = /storage/sdcard   //这个方法是获取外部存储
                //if (!TextUtils.isEmpty(type)) {
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {
                    // TODO handle non-primary volumes
                    if (Build.VERSION.SDK_INT > 20) {
                        //getExternalMediaDirs() added in API 21
                        File extenal[] = context.getExternalMediaDirs();
                        //for (int i = 0; i < extenal.length; i++) {
                        //    System.out.println("extenal[" + i + "] = " + extenal[i]);
                        //extenal[0] = /storage/sdcard/Android/media/com.example.contacts2csv
                        //}
                        if (extenal.length > 0) {
                            filePath = extenal[0].getAbsolutePath();
                            filePath = filePath.substring(0, filePath.indexOf("Android")) + split[1];
                        }
                    } else {
                        filePath = "/storage/" + type + "/" + split[1];
                    }
                    return filePath;
                }
            } else if (isDownloadsDocument(uri)) {
                // DownloadsProvider
                final String id = DocumentsContract.getDocumentId(uri);
                if (id.startsWith("raw:")) {
                    final String path = id.replaceFirst("raw:", "");
                    return path;
                }

                Uri contentUri = uri;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                }

                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                // m_ediaProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // MediaStore (and general)
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // File
            return uri.getPath();
        }
        return null;
    }

    //https://stackoverflow.com/questions/42110882/get-real-path-from-uri-of-file-in-sdcard-marshmallow
    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";

        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            if ("primary".equalsIgnoreCase(type)) {
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else {
                // TODO handle non-primary volumes

                if (Build.VERSION.SDK_INT > 20) {
                    //getExternalMediaDirs() added in API 21
                    File extenal[] = context.getExternalMediaDirs();
                    if (extenal.length > 1) {
                        filePath = extenal[1].getAbsolutePath();
                        filePath = filePath.substring(0, filePath.indexOf("Android")) + split[1];
                    }
                } else {
                    filePath = "/storage/" + type + "/" + split[1];
                }
                return filePath;
            }
        } else if (isDownloadsDocument(uri)) {
            // DownloadsProvider
            final String id = DocumentsContract.getDocumentId(uri);
            //final Uri contentUri = ContentUris.withAppendedId(
            // Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

            Cursor cursor = null;
            final String column = "_data";
            final String[] projection = {column};

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int index = cursor.getColumnIndexOrThrow(column);
                    String result = cursor.getString(index);
                    cursor.close();
                    return result;
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (DocumentsContract.isDocumentUri(context, uri)) {
            // MediaProvider
            String wholeID = DocumentsContract.getDocumentId(uri);

            // Split at colon, use second item in the array
            String[] ids = wholeID.split(":");
            String id;
            String type;
            if (ids.length > 1) {
                id = ids[1];
                type = ids[0];
            } else {
                id = ids[0];
                type = ids[0];
            }

            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{id};
            final String column = "_data";
            final String[] projection = {column};
            Cursor cursor = context.getContentResolver().query(contentUri, projection, selection, selectionArgs, null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndex(column);
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
            return filePath;
        } else {
            String[] proj = {MediaStore.Audio.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(column_index);
                }
                cursor.close();
            }

            return filePath;
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file m_sInFilePath.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
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
     * @return Whether the Uri authority is m_ediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    // 小于 19 (Android 4.4) 系统获得 Uri 文件路径的方法
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (null != cursor && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

}
