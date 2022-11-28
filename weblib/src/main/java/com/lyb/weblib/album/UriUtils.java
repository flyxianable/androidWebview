package com.lyb.weblib.album;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class UriUtils {


    private static String getPath(Context context, Uri uri) {
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIndex);
            return path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getImagePath(Context context, Uri uri) {
        final String authority = uri.getAuthority();
        if ("com.android.providers.media.documents".equals(authority)) {
            // 圖片、影音檔案
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] divide = docId.split(":");
            final String type = divide[0];
            Uri mediaUri = null;
            if ("image".equals(type)) {
                mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            }
            mediaUri = ContentUris.withAppendedId(mediaUri, Long.parseLong(divide[1]));
            String path = queryAbsolutePath(context, mediaUri);
            return path;
        }else{
            return getPath(context, uri);
        }
    }

    private static String queryAbsolutePath(final Context context, final Uri uri) {
        final String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                return cursor.getString(index);
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

}
