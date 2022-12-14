package com.upixels.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CommonUtil {
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Toast toast;

    public static void showToast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            toast.setText(msg);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public static void showToastShort(final Activity activity, final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast == null) {
                    toast = Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    toast.setText(msg);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    public static void showToastLong(final Activity activity, String msg) {
        if (toast == null) {
            toast = Toast.makeText(activity, msg, Toast.LENGTH_LONG);
            toast.show();
        } else {
            toast.setText(msg);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public static void setFullScreen(final Window window) {
        int uiOptions = window.getDecorView().getSystemUiVisibility();
        uiOptions |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
        window.getDecorView().setSystemUiVisibility(uiOptions);
        final int finalUiOptions = uiOptions;
        window.getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.VISIBLE) {
                    window.getDecorView().setSystemUiVisibility(finalUiOptions);
                }
            }
        });
    }

    /**
     * ?????????????????????1:20:30????????????
     * @param timeMs
     * @return
     */
    public static String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.US, "%d:%02d", minutes, seconds);
        }
    }

    /**
     * ??????????????????apk?????????
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            //??????????????????????????????AndroidManifest.xml???android:versionCode
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * ?????????????????????
     * @param context ?????????
     */
    public static String getVersionName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }

    public static String getUniqueID(Context context) {
        //??????????????????????????????????????????????????????64????????????????????????????????????16???????????????????????????????????????
        String ANDROID_ID = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return ANDROID_ID;
    }

    public static String toMD5(String text) {
        try {
            //??????????????? MessageDigest
            MessageDigest messageDigest = null;
            messageDigest = MessageDigest.getInstance("MD5");
            //?????????????????????????????????????????????????????????hash??????
            byte[] digest = messageDigest.digest(text.getBytes());

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digest.length; i++) {
                //?????????????????? ?????????????????????????????????;
                int digestInt = digest[i] & 0xff;
                //???10????????????????????????16??????
                String hexString = Integer.toHexString(digestInt);
                //???????????????????????????????????????0,??????????????????0
                if (hexString.length() < 2) {
                    sb.append(0);
                }
                //?????????????????????????????????
                sb.append(hexString);
            }
            //??????????????????
            return sb.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * ??????????????????
     */
    public static String getBrand() {
        return Build.BRAND;
    }

    /**
     * ??????SDK?????????
     */
    public static int getSDK() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * ??????APP????????????
     */
    public static boolean isApkExist(Context context, String packageName){
        if (TextUtils.isEmpty(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isWechatExist(Context context) {
        return isApkExist(context, "com.tencent.mm");
    }

    public static boolean isWeiboExist(Context context) {
        return isApkExist(context, "com.sina.weibo");
    }

    public static boolean isDouyinExist(Context context) {
        return isApkExist(context, "com.ss.android.ugc.aweme");
    }

}
