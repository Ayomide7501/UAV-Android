package com.example.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sdk.UAVSDK;
import com.upixels.utils.CommonUtil;
import com.upixels.utils.FileUtil;
import com.upixels.utils.PermissionUtil;

import java.io.File;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MMMainActivity";
    private static final int REQUEST_CODE = 1;
    private final static String[] USER_PERMISSIONS = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, };

    private UAVSDK mUAVSDK;

    private ImageView imageView;
    private TextView tvUpdateInfo;

    private boolean mCaptureTrigger = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUAVSDK = UAVSDK.getInstance();
        mUAVSDK.nativeInit();
        mUAVSDK.setDataListener(mListener);
        requestPermissions();
        // 初始化UI
        imageView = findViewById(R.id.imageView);
        Switch switchCamera = findViewById(R.id.switch1);
        switchCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mUAVSDK.nativeSetCameraIndex(b ? 1 : 0);
            }
        });

        Button btnCapture = findViewById(R.id.btn_capture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCaptureTrigger = true;
            }
        });

        // 测试: 发送固定64字节飞控命令
        findViewById(R.id.btn_ctl).setOnClickListener(new View.OnClickListener() {
            byte num = 1;
            @Override
            public void onClick(View view) {
                byte data[] = {(byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd,
                        (byte)0xee, (byte)0xff, (byte)0x11, (byte)0x22,
                        (byte)0x33, (byte)0x44, (byte)0x55, (byte)0x66,
                        (byte)0x77, (byte)0x88, (byte)0x99, (byte)0xaa,
                        (byte)0xab, (byte)0xac, (byte)0xad, (byte)0xae,
                        (byte)0xaf, (byte)0xff, (byte)0x11, (byte)0x22,
                        (byte)0x33, (byte)0x44, (byte)0x55, (byte)0x66,
                        (byte)0x77, (byte)0x88, (byte)0x99, (byte)0xaa,
                        (byte)0xab, (byte)0xac, (byte)0xad, (byte)0xae,
                        (byte)0xaf, (byte)0xff, (byte)0x11, (byte)0x22,
                        (byte)0x33, (byte)0x44, (byte)0x55, (byte)0x66,
                        (byte)0x77, (byte)0x88, (byte)0x99, (byte)0xaa,
                        (byte)0xab, (byte)0xac, (byte)0xad, (byte)0xae,
                        (byte)0xaf, (byte)0xff, (byte)0x11, (byte)0x22,
                        (byte)0x33, (byte)0x44, (byte)0x55, (byte)0x66,
                        (byte)0x77, (byte)0x88, (byte)0x99, num++,};
                mUAVSDK.nativeSendCtlMsg(data, 64);
            }
        });

        // 测试获取版本
        findViewById(R.id.btn_get_version).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUAVSDK.nativeGetVersion();
            }
        });

        // 测试: 升级固件
        findViewById(R.id.btn_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new File("/sdcard/new.bin.xz.ota").exists()) {
                    mUAVSDK.nativeMcuUpdate("/sdcard/new.bin.xz.ota");
                } else {
                    Toast.makeText(getApplicationContext(), "固件不存在，\n固件路径:/sdcard/new.bin.zx.ota", Toast.LENGTH_LONG).show();
                }
            }
        });

        tvUpdateInfo = findViewById(R.id.tv_update_info);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUAVSDK.nativeStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUAVSDK.nativeStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void requestPermissions() {
        if (!PermissionUtil.hasPermissionsGranted(this, USER_PERMISSIONS)) {
            PermissionUtil.requestPermission(this, USER_PERMISSIONS, REQUEST_CODE, "请打开必要权限，否则无法正常使用!");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "permissions = " + permissions);
        if (requestCode == REQUEST_CODE) {
            for (int index = 0 ; index < permissions.length; index++) {
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    CommonUtil.showToastShort(this, "未授予相关权限，程序退出！");
                    finish();
                }
            }
        }
    }

    private UAVSDK.DataListener mListener = new UAVSDK.DataListener() {
        @Override
        public void handleJpeg(final byte[] jpg, long seq, byte quality) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(BitmapFactory.decodeByteArray(jpg, 0, jpg.length));
                    if (mCaptureTrigger) {
                        mCaptureTrigger = false;
                        File picFile = FileUtil.getPictureFile(Environment.DIRECTORY_DCIM, ".jpg");
                        FileUtil.writeToFile(picFile, jpg);
                        CommonUtil.showToastShort(MainActivity.this, "拍照成功");
                    }
                }
            });
        }

        @Override
        public void handleCtlMsg(byte[] data, long seq) {
            if (data[0] == 0) { // 飞控数据类型

            } else if (data[0] == 1) { // MCU数据类型
                if (data[1] == 0x65) { // 版本信息
                    final String version = new String(data, 2, data.length-2);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvUpdateInfo.setText(version);
                        }
                    });
                }
            }
        }

        @Override
        public void handleUpdateMsg(int status, int percent) {
            if (status == UAVSDK.Update_Status_Connect_Waiting) {
                tvUpdateInfo.setText("等待连接...");
            } else if (status == UAVSDK.Update_Status_Connect_Success) {
                tvUpdateInfo.setText("连接成功");
            } else if (status == UAVSDK.Update_Status_Connect_Failed) {
                tvUpdateInfo.setText("连接失败");
            } else if (status == UAVSDK.Update_Status_Sending_File) {
                tvUpdateInfo.setText(String.format(Locale.getDefault(),"发送文件 %d", percent));
            } else if (status == UAVSDK.Update_Status_Sending_File_Success) {
                tvUpdateInfo.setText("发送文件成功，请等待升级成功...");
            } else if (status == UAVSDK.Update_Status_Sending_File_Failed) {
                tvUpdateInfo.setText("发送文件失败");
            } else if (status == UAVSDK.Update_Status_File_Error) {
                tvUpdateInfo.setText("固件错误");
            } else if (status == UAVSDK.Update_Status_Success) {
                tvUpdateInfo.setText("升级成功");
            } else if (status > 200) {
                tvUpdateInfo.setText(String.format(Locale.getDefault(), "升级失败 %d", status));
            } else {
                tvUpdateInfo.setText("升级失败 未知错误");
            }
        }
    };
}
