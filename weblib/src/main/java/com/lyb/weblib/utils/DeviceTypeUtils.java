package com.lyb.weblib.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;


/**
 * 判断当前手机是什么厂商：
 * 小米 魅族  华为。。。
 * TIPS:网上捡的。。。
 */
public class DeviceTypeUtils {

    private static final String SYS_XIAOMI = "xiaomi";
    private static final String SYS_MEIZU = "meizu";
    private static final String SYS_HUAWEI = "huawei";
    private static final String SYS_VIVO = "vivo";
    private static final String SYS_OPPO = "oppo";
    private static final String SYS_NOKIA = "hmd";
    private static final String SYS_HTC = "htc";

    public static boolean isMeiZuOS() {
        return Build.MANUFACTURER == null ? false : Build.MANUFACTURER.toLowerCase().contains(SYS_MEIZU);
    }

    public static boolean isMIOS() {
        return Build.MANUFACTURER == null ? false : Build.MANUFACTURER.toLowerCase().contains(SYS_XIAOMI);
    }

    public static boolean isHuaWeiOS() {
        return Build.MANUFACTURER == null ? false : Build.MANUFACTURER.toLowerCase().contains(SYS_HUAWEI);
    }

    public static boolean isVivoOS() {
        return Build.MANUFACTURER == null ? false : Build.MANUFACTURER.toLowerCase().contains(SYS_VIVO);
    }

    public static boolean isNOKIA() {
        return Build.MANUFACTURER == null ? false : Build.MANUFACTURER.toLowerCase().contains(SYS_NOKIA);
    }

    public static boolean isOPPO() {
        return Build.MANUFACTURER == null ? false : Build.MANUFACTURER.toLowerCase().contains(SYS_OPPO);
    }

    public static boolean isHTC() {
        return Build.MODEL == null ? false : Build.MODEL.toLowerCase().contains(SYS_HTC);
    }

    /************************************ 跳转应用设置界面  start ************************************************/
    public static void toSystemSettingActivity(Activity activity) {
        if (activity == null) {
            return;
        } else {
            if (isMeiZuOS()) {
                gotoMeizuPermission(activity);
            } else if (isMIOS()) {
                gotoMiuiPermission(activity);
            } else if (isHuaWeiOS()) {
                gotoHuaweiPermission(activity);
            } else {
                gotoAppDetailSettingIntent(activity);
            }
        }

    }

    /**
     * 跳转到魅族的权限管理系统
     */
    private static void gotoMeizuPermission(Activity activity) {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity");
        try {
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            gotoAppDetailSettingIntent(activity);
        }
    }

    /**
     * 跳转到miui的权限管理页面
     */
    private static void gotoMiuiPermission(Activity activity) {
        Intent i = new Intent("miui.intent.action.APP_PERM_EDITOR");
        ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
        i.setComponent(componentName);
        i.putExtra("extra_pkgname", activity.getPackageName());
        try {
            activity.startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
            gotoAppDetailSettingIntent(activity);
        }
    }

    /**
     * 华为的权限管理页面
     */
    private static void gotoHuaweiPermission(Activity activity) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.android.packageinstaller"
                    , "com.android.packageinstaller.permission.ui.ManagePermissionsActivity");//华为权限管理
            intent.putExtra("extra_pkgname", activity.getPackageName());
            intent.setComponent(comp);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            gotoAppDetailSettingIntent(activity);
        }
    }

    /**
     * 默认的跳转应用详情的方法
     *
     * @return
     */
    private static void gotoAppDetailSettingIntent(Activity activity) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {

            localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            localIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", activity.getPackageName());
        }
        activity.startActivity(localIntent);
    }

    /************************************ 跳转应用设置界面  END ************************************************/


    public static String getBrandName() {
        if (isHuaWeiOS()) {
            return "华为";
        } else if (isMeiZuOS()) {
            return "魅族";
        } else if (isOPPO()) {
            return "OPPO";
        } else if (isVivoOS()) {
            return "VIVO";
        } else if (isMIOS()) {
            return "小米";
        }else {
            return Build.BRAND;
        }
    }

    /**
     * manufacture  是否是厂商渠道
     * 1.xiaomi 2.huawei 3.meizu 4.oppo
     * oppo低版本不支持push
     *
     * @return
     */
    public static boolean isPushChannel() {
        if (isOPPO()) {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;
        } else  if(isMIOS()){
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
        } else if(isVivoOS()){
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
        }else {
            return isHuaWeiOS() || isMeiZuOS() ;
        }
    }

    /**
     * manufacture  判断机型是否是五大厂商
     *
     *
     */
    public static boolean is5ChinaRom() {
        return isMeiZuOS()||isHuaWeiOS()||isMIOS()||isOPPO()||isVivoOS();
    }
}
