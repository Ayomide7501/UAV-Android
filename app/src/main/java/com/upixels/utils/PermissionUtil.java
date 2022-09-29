package com.upixels.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by yuzh on 2017/8/29.
 * 权限操作相关的工具类
 */

public class PermissionUtil {

    //Tells whether permissions are granted to the app.
    public static boolean hasPermissionsGranted(Activity activity, String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void requestPermission(Activity activity, String[] permissions, int requestCode, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRationale(activity, permissions)) {
                PermissionConfirmationDialog.newInstance()
                        .setActivity(activity)
                        .setMessage(message)
                        .setPermissions(permissions, requestCode)
                        .show(activity.getFragmentManager(), "dialog");
            } else {
                activity.requestPermissions(permissions, requestCode);
            }
        }
    }

    /**
     * Gets whether you should show UI with rationale for requesting the permissions.
     * @return True if the UI should be shown.
     */
    public static boolean shouldShowRationale(Activity activity, String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (activity.shouldShowRequestPermissionRationale(permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * A dialog that explains about the necessary permissions.
     */
    public static class PermissionConfirmationDialog extends DialogFragment {
        private Activity activity;
        private String[] permissions;
        private String message;
        private int requestCode;
        public static PermissionConfirmationDialog newInstance() {
            return new PermissionConfirmationDialog();
        }

        public PermissionConfirmationDialog setActivity(Activity activity) {
            this.activity = activity;
            return this;
        }

        public PermissionConfirmationDialog setMessage(String message) {
            this.message = message;
            return this;
        }

        public PermissionConfirmationDialog setPermissions(String[] permissions, int requestCode) {
            this.permissions = permissions;
            this.requestCode = requestCode;
            return this;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                activity.requestPermissions(permissions, requestCode);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.finish();
                        }
                    })
                    .create();
        }


//        @SuppressLint("ClickableViewAccessibility")
//        @Nullable
//        @Override
//        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
//            Window window = getDialog().getWindow();
//            CommonUtil.setFullScreen(window);
//            //对话框内部的背景设为透明
//            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            window.setWindowAnimations(R.style.DialogAnimation);
//            getDialog().setCanceledOnTouchOutside(false);
//            getDialog().setCancelable(false);
//            View view = inflater.inflate(R.layout.dialog_notice, container);
//            ImageView ivImageView = view.findViewById(R.id.iv_notice);
//            LinearLayout.LayoutParams layoutParams;
//            ivImageView.setImageResource(R.drawable.permissions_notice);
//            layoutParams = new LinearLayout.LayoutParams(DensityUtil.dip2px(getActivity(), 250), DensityUtil.dip2px(getActivity(),117));
//            ivImageView.setLayoutParams(layoutParams);
//            ivImageView.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    int w = v.getWidth();
//                    int h = v.getHeight();
//                    int x = (int) event.getX();
//                    int y = (int) event.getY();
//                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                        if (x < w/2 && y>h*2/3) { //Touch_Left
//                            activity.finish();
//                        } else if (x >= w/2 && y>h*2/3) { //Touch_Right
//                            dismiss();
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                activity.requestPermissions(permissions, requestCode);
//                            }
//                        }
//                    }
//                    return true;
//                }
//            });
//            return view;
//        }
    }
}
