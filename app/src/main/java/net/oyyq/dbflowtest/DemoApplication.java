package net.oyyq.dbflowtest;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.igexin.sdk.IUserLoggerInterface;
import com.igexin.sdk.PushManager;
import net.oyyq.common.BuildConfig;
import net.oyyq.dbflowdemo.db.Account;
import net.oyyq.dbflowdemo.factory.Factory;
import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;


import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DemoApplication extends Application
        implements android.app.Application.ActivityLifecycleCallbacks{

    protected static DemoApplication instance;
    public static DemoApplication getInstance() {
        return instance;
    }


    private static final String TAG = "GetuiSdkDemo";
    public static String isCIDOnLine = "";
    public static String cid = "";
    public static Context appContext;
    public static boolean isSignError = false;
    public static int activeActivityCount = 0;
    private AtomicBoolean PUSH = new AtomicBoolean(false);

    /**
     * 应用未启动, 个推 service已经被唤醒,保存在该时间段内离线消息
     */
    public static StringBuilder payloadData = new StringBuilder();
    private static DemoHandler handler;

    public static void sendMessage(Message msg) {
        handler.sendMessage(msg);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        appContext = this;
        Log.d(TAG, "DemoApplication onCreate");

        Config.init(this);
        if (handler == null) {
            handler = new DemoHandler();
        }

        registerActivityLifecycleCallbacks(this);
        initSdk();
        instance = this;
        Factory.setContext(this);
        Account.setContext(this);
        Factory.setup();
    }


    private void initSdk() {
        Log.d(TAG, "initializing sdk...");
        PushManager.getInstance().initialize(this);
        if (BuildConfig.LOG_ENABLE) {
            //切勿在 release 版本上开启调试日志
            PushManager.getInstance().setDebugLogger(this, new IUserLoggerInterface() {
                @Override
                public void log(String s) {
                    Log.e("PUSH_LOG", s);
                }
            });
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        activeActivityCount++;
        if(PUSH.compareAndSet(false, true)) {
            synchronized (payloadData) {
                String[] messages = payloadData.toString().split("\n");
                payloadData.setLength(0);
                if(messages == null || messages.length == 0) return;
                for(String message : messages) {
                    Factory.dispatchPush(message);
                }
            }
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        activeActivityCount--;
        if(activeActivityCount <= 0) PUSH.compareAndSet(true,false);
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }


    public static class DemoHandler extends Handler {
        public static final int RECEIVE_MESSAGE_DATA = 0; //接收到消息
        public static final int RECEIVE_CLIENT_ID = 1; //cid通知
        public static final int RECEIVE_ONLINE_STATE = 2; //cid在线状态通知消息
        public static final int SHOW_TOAST = 3; //Toast消息


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECEIVE_MESSAGE_DATA:  //接收到消息
                    synchronized (payloadData) {
                        payloadData.append((String) msg.obj);
                        payloadData.append("\n");
                        //前台
                        if (activeActivityCount > 0) {
                            String[] messages = payloadData.toString().split("\n");
                            payloadData.setLength(0);       //清空一个StringBuilder
                            for (String message : messages) {
                                Factory.dispatchPush(message);
                            }
                        }
                    }

                    break;
                case RECEIVE_CLIENT_ID:  //cid通知
                    cid = (String) msg.obj;
                    DemoApplication.cid = cid;
                    Account.setPushId(cid);
                    break;

                case RECEIVE_ONLINE_STATE:  //cid在线状态通知
                    isCIDOnLine = (String) msg.obj;

                    break;

                case SHOW_TOAST:  //需要弹出Toast
                    Toast.makeText(DemoApplication.appContext, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;

            }

        }

    }


    /**
     * 获取缓存文件夹地址 不需要动态申请权限
     *
     * @return 当前APP的缓存文件夹地址
     */
    public static File getCacheDirFile() {
        return instance.getCacheDir();
    }

    /**
     * 获取头像的临时存储文件地址
     *
     * @return 临时文件
     */
    public static File getPortraitTmpFile() {
        // 得到头像目录的缓存地址
        File dir = new File(getCacheDirFile(), "portrait");
        // 创建所有的对应的文件夹
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();

        // 删除旧的一些缓存为文件
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }

        // 返回一个当前时间戳的目录文件地址
        File path = new File(dir, SystemClock.uptimeMillis() + ".jpg");
        return path.getAbsoluteFile();
    }


    /**
     * 获取声音文件的本地地址
     *
     * @param isTmp 是否是缓存文件， True，每次返回的文件地址是一样的
     * @return 录音文件的地址
     */
    public static File getAudioTmpFile(boolean isTmp) {
        File dir = new File(getCacheDirFile(), "audio");
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }

        // aar
        File path = new File(getCacheDirFile(), isTmp ? "tmp.mp3" : SystemClock.uptimeMillis() + ".mp3");
        return path.getAbsoluteFile();
    }



    /**
     * 显示一个Toast
     *
     * @param msg 字符串
     */
    public static void showToast(final String msg) {
        // Toast 只能在主线程中显示，所以需要进行线程转换，
        // 保证一定是在主线程进行的show操作
        Run.onUiAsync(new Action() {
            @Override
            public void call() {
                // 这里进行回调的时候一定就是主线程状态了
                Toast.makeText(instance, msg, Toast.LENGTH_SHORT).show();
            }
        });

    }


    /**
     * 显示一个Toast
     *
     * @param msgId 传递的是字符串的资源
     */
    public static void showToast(@StringRes int msgId) {
        showToast(instance.getString(msgId));
    }


}
