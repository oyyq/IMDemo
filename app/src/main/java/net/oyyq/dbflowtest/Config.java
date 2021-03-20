package net.oyyq.dbflowtest;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;


public class Config {
    public static final String AUTH_ACTION = "com.action.auth";
    private static final String TAG = Config.class.getSimpleName();
    public static String appid = "";
    public static String appkey = "";
    public static String appName = "";
    public static String packageName = "";
    public static String authToken;

    public static void init(Context context) {
        parseManifests(context);
    }


    private static void parseManifests(Context context) {
        packageName = context.getPackageName();
        try {
            //meta-data在主工程manifest文件里
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            appName = appInfo.loadLabel(context.getPackageManager()).toString();
            if (appInfo.metaData != null) {
                appid = appInfo.metaData.getString("GETUI_APPID");
                appkey = appInfo.metaData.getString("GETUI_APPKEY");
            }
        } catch (Exception e) {
            Log.i(TAG, "parse manifest failed = " + e);
        }
    }
}
