/*
The MIT License (MIT)

Copyright (c) 2015 skwas

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.skwas.cordova.appinfo;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.device.Device;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

public class AppInfo extends CordovaPlugin {

	private Activity _activity;
	private ApplicationInfo _appInfo;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        _activity = cordova.getActivity();
        _appInfo = _activity.getApplicationInfo();
    }

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		if (action.equals("getAppInfo")) {
			cordova.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					JSONObject result = new JSONObject();
					try {
						result.put("name", getApplicationName());
						result.put("version", getVersion());
                        result.put("build", getBuild());
						result.put("identifier", getIdentifer());
						result.put("compileDate", getBuildDate());
						result.put("isHardwareAccelerated", getIsHardwareAccelerated());
						result.put("isDebuggable", getIsDebuggable());
					//							result.put("xapp_key", XtifyCordovaPlugin.getAppKey(activity.getApplicationContext()));
					} catch(JSONException e) {}
					Log.i("AppInfo", getBuildDate());
					callbackContext.success(result);
				   }
				});
			return true;
		}
		return false;
	}

	public String getApplicationName() {
		int stringId = _appInfo.labelRes;
		return _activity.getString(stringId);
	}

	public String getVersion() {
		String versionName = "";
		try {
			versionName = _activity.getPackageManager()
				.getPackageInfo(_activity.getPackageName(), 0)
				.versionName;
		} catch (NameNotFoundException e) {}

		return versionName;
	}

	public String getBuildDate() {
		String buildDate = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS",
			//"dd-MM-yyyy HH:mm:ss",
			Locale.getDefault()
			).format(getBuildDate(_activity)) + "Z";
		return buildDate.replace(" ", "T");
	}

	public Boolean getIsHardwareAccelerated() {
		Boolean isHardwareAccelerated = false;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			try {
				Method _isHardwareAccelerated = CordovaWebView.class.getMethod("isHardwareAccelerated", (Class<?>[])null);
				isHardwareAccelerated = (Boolean)_isHardwareAccelerated.invoke(webView, (Object[])null);
			} catch (Exception e) {}
		}
		return isHardwareAccelerated;

	}

	public boolean getIsDebuggable() {
		return 0 != ( _appInfo.flags &= ApplicationInfo.FLAG_DEBUGGABLE );
	}

	public static long getBuildDate(Context context) {

		ZipFile zf = null;
		try {
			ApplicationInfo ai = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(), 0);
			zf = new ZipFile(ai.sourceDir);
			ZipEntry ze = zf.getEntry("classes.dex");
			long time = ze.getTime();
			return time;

		} catch (Exception e) {
		}

		finally {
			try {
				if (zf != null) zf.close();
			} catch (IOException e) {}
		}

		return 0l;
	}

	public String getBuild() {
  		android.content.pm.PackageManager pm = _activity.getPackageManager();
  		try {
  			android.content.pm.PackageInfo packageInfo = pm.getPackageInfo(getIdentifer(), 0);
  			return Integer.toString(packageInfo.versionCode);
  		} catch (NameNotFoundException e) {
  			return null;
  		}
  	}

  	public String getIdentifer() {
  		return _activity.getPackageName();
  	}
}
