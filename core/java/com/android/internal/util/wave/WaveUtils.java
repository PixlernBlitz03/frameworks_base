/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.util.wave;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.Vibrator;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;

import com.android.internal.R;

import java.util.List;

public class WaveUtils {
    private static OverlayManager mOverlayService;

    public static final String INTENT_SCREENSHOT = "action_handler_screenshot";
    public static final String INTENT_REGION_SCREENSHOT = "action_handler_region_screenshot";

    // Check to see if device is WiFi only
    public static boolean isWifiOnly(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return (cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE) == false);
    }

    // Check to see if device supports the Fingerprint scanner
    public static boolean hasFingerprintSupport(Context context) {
        FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        return context.getApplicationContext().checkSelfPermission(Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED &&
                (fingerprintManager != null && fingerprintManager.isHardwareDetected());
    }

    // Check to see if device not only supports the Fingerprint scanner but also if is enrolled
    public static boolean hasFingerprintEnrolled(Context context) {
        FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        return context.getApplicationContext().checkSelfPermission(Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED &&
                (fingerprintManager != null && fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints());
    }

    // Check to see if device has a camera
    public static boolean hasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    // Check to see if device supports NFC
    public static boolean hasNFC(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
    }

    // Check to see if device supports Wifi
    public static boolean hasWiFi(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI);
    }

    // Check to see if device supports Bluetooth
    public static boolean hasBluetooth(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
    }

    // Check to see if device supports an alterative ambient display package
    public static boolean hasAltAmbientDisplay(Context context) {
        return context.getResources().getBoolean(com.android.internal.R.bool.config_alt_ambient_display);
    }

    // Check to see if device supports A/B (seamless) system updates
    public static boolean isABdevice(Context context) {
        return SystemProperties.getBoolean("ro.build.ab_update", false);
    }

    public static void takeScreenshot(boolean full) {
        IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
        try {
//            wm.sendCustomAction(new Intent(full? INTENT_SCREENSHOT : INTENT_REGION_SCREENSHOT));
            wm.takeAlternativeScreenshot();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // Method to detect whether an overlay is enabled or not
    public static boolean isThemeEnabled(String packageName) {
        mOverlayService = new OverlayManager();
        try {
            List<OverlayInfo> infos = mOverlayService.getOverlayInfosForTarget("android",
                    UserHandle.myUserId());
            for (int i = 0, size = infos.size(); i < size; i++) {
                if (infos.get(i).packageName.equals(packageName)) {
                    return infos.get(i).isEnabled();
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static class OverlayManager {
        private final IOverlayManager mService;

        public OverlayManager() {
            mService = IOverlayManager.Stub.asInterface(
                    ServiceManager.getService(Context.OVERLAY_SERVICE));
        }

        public void setEnabled(String pkg, boolean enabled, int userId)
                throws RemoteException {
            mService.setEnabled(pkg, enabled, userId);
        }

        public List<OverlayInfo> getOverlayInfosForTarget(String target, int userId)
                throws RemoteException {
            return mService.getOverlayInfosForTarget(target, userId);
        }
    }
}
