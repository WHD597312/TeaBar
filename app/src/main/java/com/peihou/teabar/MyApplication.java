package com.peihou.teabar;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


//import com.peihou.willgood.daemon.DaemonHolder;


/**
 * Created by hongming.wang on 2018/1/23.
 */

public class MyApplication extends Application {
    public static final String APP_NAME = "XXX";
    public static boolean isDebug=true;
    private int count = 0;
    private List<Activity> activities;
    private List<Fragment> fragments;
    private static Context mContext;

    public static int floating=0;
    public static Context getContext(){
        return mContext;

    }
    @Override
    public void onCreate() {
        super.onCreate();
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext

        disableAPIDialog();
//        createNotificationChannel();
        fragments=new ArrayList<>();

        activities=new ArrayList<>();
        fragments=new ArrayList<>();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                count ++;
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                if(count > 0) {
                    count--;
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }
    private static final String PUSH_CHANNEL_ID = "PUSH_NOTIFY_ID2";
    private static final String PUSH_CHANNEL_NAME = "PUSH_NOTIFY_NAME2";
    //需要创建 NotificationChannel
    private void createNotificationChannel(){
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //判断是不是 Android8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    //字符串类型的 Channel id
                    PUSH_CHANNEL_ID,
                    //字符串类型的 Channel name
                    PUSH_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
    }

    public void addActivity(Activity activity){
        if (!activities.contains(activity)){
            activities.add(activity);
        }
    }
    public void addFragment(Fragment fragment){
        if (!fragments.contains(fragment)){
            fragments.add(fragment);
        }
    }

    public List<Fragment> getFragments() {
        return fragments;
    }
    public void removeFragment(Fragment fragment){
        if (fragments.contains(fragment)){
            fragments.remove(fragment);
        }
    }
    public void removeAllFragment(){
        fragments.clear();
    }

    public void removeActivity(Activity activity){
        if (activities.contains(activity)){
            activities.remove(activity);
            activity.finish();
        }
    }
    public void removeActiviies(List<Activity> activities){
        for (Activity activity:activities){
            removeActivity(activity);
        }
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void removeAllActivity(){
        for (Activity activity:activities){
            activity.finish();
        }
    }
    /**
     * 判断app是否在后台
     * @return
     */
    public boolean isBackground(){
        if(count <= 0){
            return true;
        } else {
            return false;
        }
    }


    private void disableAPIDialog(){

        try {
            if (Build.VERSION.SDK_INT >= 28){
                Class clazz = Class.forName("android.app.ActivityThread");
                Method currentActivityThread = clazz.getDeclaredMethod("currentActivityThread");
                currentActivityThread.setAccessible(true);
                Object activityThread = currentActivityThread.invoke(null);
                Field mHiddenApiWarningShown = clazz.getDeclaredField("mHiddenApiWarningShown");
                mHiddenApiWarningShown.setAccessible(true);
                mHiddenApiWarningShown.setBoolean(activityThread, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
