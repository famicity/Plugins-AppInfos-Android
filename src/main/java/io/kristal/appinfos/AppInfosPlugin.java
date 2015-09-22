/**
 *
 * AppInfosPlugin
 * AppInfos
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Kristal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package io.kristal.appinfos;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import java.util.Locale;
import fr.cobaltians.cobalt.Cobalt;
import fr.cobaltians.cobalt.fragments.CobaltFragment;
import fr.cobaltians.cobalt.plugin.CobaltAbstractPlugin;
import fr.cobaltians.cobalt.plugin.CobaltPluginWebContainer;
import org.json.JSONException;
import org.json.JSONObject;

public class AppInfosPlugin extends CobaltAbstractPlugin {

    protected final static String TAG = AppInfosPlugin.class.getSimpleName();

    private static final String GET_APP_INFOS = "getAppInfos";
    private static final String VERSION_NAME = "versionName";
    private static final String VERSION_CODE = "versionCode";
    private static final String LANG = "lang";
    private static final String ANDROID = "android";
    private static final String PLATFORM = "platform";
    private static final String DEVICE_ID = "deviceId";
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";


    /*******************************************************************************************************
     * MEMBERS
     *******************************************************************************************************/

    protected static AppInfosPlugin sInstance;

    /**************************************************************************************
     * CONSTRUCTORS
     **************************************************************************************/

    public static CobaltAbstractPlugin getInstance(CobaltPluginWebContainer webContainer) {
        if (sInstance == null) sInstance = new AppInfosPlugin();
        sInstance.addWebContainer(webContainer);
        return sInstance;
    }

    @Override
    public void onMessage(CobaltPluginWebContainer webContainer, JSONObject message) {
        try {
            String action = message.getString(Cobalt.kJSAction);

            if (action.equals(GET_APP_INFOS)){
                CobaltFragment fragment = webContainer.getFragment();
                Context ctx = webContainer.getActivity();
                try {
                    PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
                    JSONObject data = new JSONObject();
                    data.put(VERSION_NAME, packageInfo.versionName);
                    data.put(VERSION_CODE, packageInfo.versionCode);
                    data.put(LANG, Locale.getDefault().getLanguage());
                    data.put(PLATFORM, ANDROID);
                    data.put(DEVICE_ID, getUniqueId(ctx));
                    fragment.sendCallback(message.getString(Cobalt.kJSCallback),data);
                }
                catch (PackageManager.NameNotFoundException e) {
                    if (Cobalt.DEBUG) {
                        Log.e(TAG, "onMessage: Package Name not found " + message.toString() + ".");
                        e.printStackTrace();
                    }
                }
            }
            else if (Cobalt.DEBUG) Log.e(TAG, "onMessage: invalid action " + action + "in message " + message.toString() + ".");
        }
        catch (JSONException exception) {
            if (Cobalt.DEBUG) {
                Log.e(TAG, "onMessage: missing action key in message " + message.toString() + ".");
                exception.printStackTrace();
            }
        }
    }

    private synchronized static String getUniqueId(Context context) {
        String uniqueID = null;
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_UNIQUE_ID, Context.MODE_PRIVATE);
        uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
        if (uniqueID == null) {
            uniqueID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (uniqueID == null || uniqueID.length() == 0 || "9774d56d682e549c".equals(uniqueID)) {
                // old version of reto meier
                //uniqueID = UUID.randomUUID().toString();
                uniqueID = "35" + //we make this look like a valid IMEI
                        Build.BOARD.length()%10+ Build.BRAND.length()%10 +
                        Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +
                        Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
                        Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
                        Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
                        Build.TAGS.length()%10 + Build.TYPE.length()%10 +
                        Build.USER.length()%10 ; //13 digits
            }
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(PREF_UNIQUE_ID, uniqueID);
            editor.commit();
        }
        return uniqueID;
    }
}
