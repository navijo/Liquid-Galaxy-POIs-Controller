/*
 * Copyright 2015 Google Inc. All rights reserved.
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

package com.gsoc.ijosa.liquidgalaxycontroller.PW;

import android.os.SystemClock;

import com.gsoc.ijosa.liquidgalaxycontroller.PW.collection.UrlDevice;


abstract class UrlDeviceDiscoverer {
  private UrlDeviceDiscoveryCallback mUrlDeviceDiscoveryCallback;
  private long mScanStartTime;

  public abstract void startScanImpl();
  public abstract void stopScanImpl();

  public void startScan() {
    mScanStartTime = SystemClock.elapsedRealtime();
    startScanImpl();
  }

  public void stopScan() {
    stopScanImpl();
  }

  public void restartScan() {
    stopScan();
    startScan();
  }

  public void setCallback(UrlDeviceDiscoveryCallback urlDeviceDiscoveryCallback) {
    mUrlDeviceDiscoveryCallback = urlDeviceDiscoveryCallback;
  }

  protected Utils.UrlDeviceBuilder createUrlDeviceBuilder(String id, String url) {
    return new Utils.UrlDeviceBuilder(id, url)
        .setScanTimeMillis(SystemClock.elapsedRealtime() - mScanStartTime);
  }

  protected void reportUrlDevice(UrlDevice urlDevice) {
    mUrlDeviceDiscoveryCallback.onUrlDeviceDiscovered(urlDevice);
  }

  public interface UrlDeviceDiscoveryCallback {
    void onUrlDeviceDiscovered(UrlDevice urlDevice);
  }
}
