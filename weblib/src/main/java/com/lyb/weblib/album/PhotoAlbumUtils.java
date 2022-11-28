package com.lyb.weblib.album;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.fragment.app.Fragment;


import com.lyb.weblib.utils.DeviceTypeUtils;

import java.util.ArrayList;


public class PhotoAlbumUtils {

    /**
     * 跳转到系统相册，支持选择多张图片
     *
     * @param activity
     * @param requestCode
     */
    public static void gotoMultiChoiceAlbum(Activity activity, int requestCode) {
        Intent intent ;
        if(DeviceTypeUtils.isVivoOS() || DeviceTypeUtils.isOPPO()) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }else{
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void gotoChoiceAlbum(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void gotoChoiceAlbum(Fragment fragment, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * 相册返回
     *
     * @param context
     * @param data
     * @param remainPhotoCount 本次可添加的最大图片数量
     * @return Image集合，与原相册组件返回保持一致
     */
    public static ArrayList<Image> onAlbumActivityResult(Context context, Intent data, int remainPhotoCount) {
        ClipData imageNames = data.getClipData();
        ArrayList<Image> pictures = new ArrayList<>();
        if (imageNames != null) {
            int count = imageNames.getItemCount();
            if (imageNames.getItemCount() > remainPhotoCount) {
                count = remainPhotoCount;
            }
            for (int i = 0; i < count; i++) {
                Uri imageUri = imageNames.getItemAt(i).getUri();
                Image image = new Image();
                image.path = UriUtils.getImagePath(context, imageUri);
                pictures.add(image);
            }
        } else {
            Uri uri = data.getData();
            if (uri == null) {
                return pictures;
            }
            Image image = new Image();
            image.path = UriUtils.getImagePath(context, uri);
            pictures.add(image);
        }
        return pictures;
    }




    /**
     * 相册返回
     *
     * @param context
     * @param data
     * @param remainPhotoCount
     * @return paths 图片文件路径
     */
    public ArrayList<String> onAlbumPathsActivityResult(Context context, Intent data, int remainPhotoCount) {
        ClipData imageNames = data.getClipData();
        ArrayList<String> paths = new ArrayList<>();
        if (imageNames != null) {
            int count = imageNames.getItemCount();
            if (imageNames.getItemCount() > remainPhotoCount) {
                count = remainPhotoCount;
            }
            for (int i = 0; i < count; i++) {
                Uri imageUri = imageNames.getItemAt(i).getUri();
                paths.add(UriUtils.getImagePath(context, imageUri));
            }
        } else {
            Uri uri = data.getData();
            Image image = new Image();
            paths.add(UriUtils.getImagePath(context, uri));
        }
        return paths;
    }

    /**
     * 获取相册uri集合
     * @param data
     * @return
     */
    public static Uri[] onAlbumUriActivityResult(Intent data){
        Uri[] results;
        ClipData imageNames = data.getClipData();
        if(imageNames != null) {
            int count = imageNames.getItemCount();
            results = new Uri[count];
            for (int i = 0; i < count; i++) {
                Uri imageUri = imageNames.getItemAt(i).getUri();
                results[i] = imageUri;
            }
        }else{
            Uri imageUri = data.getData();
            results = new Uri[1];
            results[0] = imageUri;
        }
        return results;
    }


    public static boolean isOnePlus() {
        return Build.MANUFACTURER == null ? false : Build.MANUFACTURER.toLowerCase().contains("oneplus");
    }

}
