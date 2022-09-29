package com.example.sdk;

import android.util.Log;

public class UAVSDK {
    static {
        System.loadLibrary("uav_lib");
    }

    private final static String TAG = "UAVSDK";

    //#define Update_Status_Failed                   20X  //升级失败 大于200的错误值
    public final static int Update_Status_Success              =    200;    //升级成功
    public final static int Update_Status_Connect_Waiting      =    100;    //等待连接
    public final static int Update_Status_Connect_Success      =    101;    //连接成功
    public final static int Update_Status_Connect_Failed       =    102;    //连接失败

    public final static int Update_Status_Sending_File         =    103;    //正在发送固件
    public final static int Update_Status_Sending_File_Success =    104;    //发送固件成功
    public final static int Update_Status_Sending_File_Failed  =    105;    //发送固件错误
    public final static int Update_Status_File_Error           =    106;    //固件不存在或无法打开

    public interface DataListener {
        void handleJpeg(byte[] jpg, long seq, byte quality);
        void handleCtlMsg(byte[] data, long seq);
        void handleUpdateMsg(int status, int percent);
    }

    private DataListener mDataListener;

    private static UAVSDK mInstance;
    public static UAVSDK getInstance() {
        if (mInstance == null) {
            mInstance = new UAVSDK();
        }
        return mInstance;
    }

    private UAVSDK() {
    }

    public void setDataListener(DataListener listener) {
        mDataListener = listener;
    }

    /*
     * 本地初始化JNI相关参数
     */
    public native void nativeInit();

    /*
     * 开启APP与MCU通讯，cbJpegFromNative 会回调jpg预览流到Java层
     */
    public native void nativeStart();

    /*
     * 停止APP与MCU通讯
     */
    public native void nativeStop();

    // 设置camera index = 0 为主摄像头; index = 1 为光流摄像头
    public native void nativeSetCameraIndex(int index);

    public native void nativeSetQPara(int q1, int q2, int thres1, int thres2);

    // 发送飞控命令，如果没有更新，重复发送上一条命令 dataLen <= 64
    public native void nativeSendCtlMsg(byte data[], int dataLen);

    // 发送用户自定义命令，只发送一次。不传递给飞控。dataLen <= 64
    public native void nativeSendCustomMsg(byte data[], int dataLen);

    // 获取MCU版本号
    public native void nativeGetVersion();

    //固件升级，填写固件所在路径
    public native void nativeMcuUpdate(String path);

    // MCU 发送来的 mjpeg 数据帧
    private void cbJpegFromNative(byte[] jpg, long seq, byte quality) {
        if (mDataListener == null) {
            return;
        }
        mDataListener.handleJpeg(jpg, seq, quality);
    }

    // MCU 发送到 APP 的命令
    // data[0]的值标记数据类型
    // 当data[0] = 0时，表示接收到飞控透传来的数据。
    // 当data[0] = 1时，表示接收用户自定义数据, data[1] = 0x65时，data[2]之后的数据是版本信息
    private void cbCtlMsgFromNative(byte[] data, long seq) {
        if (mDataListener == null) {
            return;
        }
        if (data[0] == 0) { // 飞控数据类型

        } else if (data[0] == 1) { // MCU数据
            if (data[1] == 0x65) {
                String version = new String(data, 2, data.length-2);
                Log.d(TAG, "MCU Version : " + version);
            }
        }
        mDataListener.handleCtlMsg(data, seq);
    }

    // MCU 升级回调
    private void cbUpdateFromNative(int status, int percent) {
        Log.d(TAG, "status = " + status + " percent = " + percent);
        mDataListener.handleUpdateMsg(status, percent);
    }

}
