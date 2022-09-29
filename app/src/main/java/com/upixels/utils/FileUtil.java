package com.upixels.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;

public class FileUtil {
    private final static String DIR_NAME = "UAVSDK";
    private final static String SUB_DIR_PICTURE = "picture";
    private final static String SUB_DIR_VIDEO = "video";
    private static final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US);

    /**
     * get current date and time as String
     * @return
     */
    public static String getDateTimeString() {
        final GregorianCalendar now = new GregorianCalendar();
        return mDateTimeFormat.format(now.getTime());
    }

    /**
     * generate output file
     * @param type Environment.DIRECTORY_MOVIES / Environment.DIRECTORY_DCIM etc.
     * @param ext .mp4(.m4a for audio) or .png
     * @return return null when this app has no writing permission to external storage.
     */
    public static File getCaptureFile(final String type, final String ext) {
        final File dir = new File(Environment.getExternalStoragePublicDirectory(type), DIR_NAME);
        dir.mkdirs();
        if (dir.canWrite()) { return new File(dir, getDateTimeString() + ext); }
        return null;
    }

    public static File getPictureFile(final String type, final String ext) {
        final File dir = new File(Environment.getExternalStoragePublicDirectory(type).getAbsolutePath()
                                 + File.separator + DIR_NAME + File.separator + SUB_DIR_PICTURE);
        dir.mkdirs();
        if (dir.canWrite()) { return new File(dir, getDateTimeString() + ext); }
        return null;
    }

    public static File getVideoFile(final String type, final String ext) {
        final File dir = new File(Environment.getExternalStoragePublicDirectory(type).getAbsolutePath()
                                 + File.separator + DIR_NAME + File.separator + SUB_DIR_VIDEO);
        dir.mkdirs();
        if (dir.canWrite()) { return new File(dir, getDateTimeString() + ext); }
        return null;
    }

    public static String getThumbnailDir(final String type) {
        final File dir = new File(Environment.getExternalStoragePublicDirectory(type).getAbsolutePath()
                                 + File.separator + DIR_NAME + File.separator + "thumbPhoto");
        dir.mkdirs();
        if (dir.canWrite()) { return dir.getAbsolutePath(); }
        return null;
    }

    public static String writeToCacheFile(Context context, String fileName, String content) {
        File file = new File(context.getCacheDir(), fileName);
        String fileAbsolutePath = file.getAbsolutePath();
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return fileAbsolutePath;
    }

    public static void writeToFile(String fileName, byte[] data) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(fileName);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(File file, byte[] data) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //过滤文件, 选在符合formatSet格式的文件
    private static boolean filterFile(String path, String[] formatSet) {
        for (String f : formatSet){
            if (path.trim().toLowerCase().endsWith(f)){
                return true;
            }
        }
        return false;
    }

    /**
     * 删除文件安全方式：
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.exists() && file.isFile()) {
            deleteFileSafely(file);
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                deleteFileSafely(file);
                return;
            }
            for (File childFile : childFiles) {
                deleteFile(childFile);
            }
            deleteFileSafely(file);
        }
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            deleteFileSafely(file);
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                deleteFileSafely(file);
                return;
            }
            for (File childFile : childFiles) {
                deleteFile(childFile);
            }
            deleteFileSafely(file);
        }
    }

    /**
     * 安全删除文件.
     * @param file
     * @return
     */
    public static boolean deleteFileSafely(File file) {
        if (file != null) {
            String tmpPath = file.getParent() + File.separator + System.currentTimeMillis();
            File tmp = new File(tmpPath);
            file.renameTo(tmp);
            return tmp.delete();
        }
        return false;
    }

    public static void updateMedia(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    public static void updateMedia(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(filePath));
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    /**
     * 针对非系统文件夹下的文件,使用该方法 插入时初始化公共字段
     * @param filePath 文件
     * @return ContentValues
     */
    private static ContentValues initCommonContentValues(String filePath, long createTime) {
        ContentValues values = new ContentValues();
        File saveFile = new File(filePath);
        values.put(MediaStore.MediaColumns.TITLE, saveFile.getName());
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, saveFile.getName());
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, createTime);
        values.put(MediaStore.MediaColumns.DATE_ADDED, createTime);
        values.put(MediaStore.MediaColumns.DATA, saveFile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.SIZE, saveFile.length());
        return values;
    }

    /**
     * 保存到视频到本地，并插入MediaStore以保证相册可以查看到,这是更优化的方法，防止读取的视频获取不到宽高
     * @param context    上下文
     * @param filePath   文件路径
     * @param duration   视频长度 ms
     * @param width      宽度
     * @param height     高度
     */
    public static void insertVideoToMediaStore(Context context, String filePath, int width, int height, long duration) {
        long createTime = System.currentTimeMillis();
        ContentValues values = initCommonContentValues(filePath, createTime);
        values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, createTime);
        if (duration > 0) values.put(MediaStore.Video.VideoColumns.DURATION, duration);
        if (width > 0) values.put(MediaStore.Video.VideoColumns.WIDTH, width);
        if (height > 0) values.put(MediaStore.Video.VideoColumns.HEIGHT, height);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }

    /**
     * 保存到照片到本地，并插入MediaStore以保证相册可以查看到,这是更优化的方法，防止读取的照片获取不到宽高
     * @param context    上下文
     * @param filePath   文件路径
     * @param width      宽度
     * @param height     高度
     */
    public static void insertImageToMediaStore(Context context, String filePath, int width, int height) {
        long createTime = System.currentTimeMillis();
        ContentValues values = initCommonContentValues(filePath, createTime);
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, createTime);
        values.put(MediaStore.Images.ImageColumns.ORIENTATION, 0);
        values.put(MediaStore.Images.ImageColumns.ORIENTATION, 0);
        if (width > 0) values.put(MediaStore.Images.ImageColumns.WIDTH, width);
        if (height > 0) values.put(MediaStore.Images.ImageColumns.HEIGHT, height);
        values.put(MediaStore.MediaColumns.MIME_TYPE,  "image/jpeg");
        context.getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}
