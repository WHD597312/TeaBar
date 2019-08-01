package com.peihou.teabar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.jwenfeng.library.pulltorefresh.BaseRefreshListener;
import com.jwenfeng.library.pulltorefresh.PullToRefreshLayout;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;



public class MainActivity extends BaseActivity {

    private boolean supportAlpha;//颜色是否支持透明度
    private final float[] mCurrentHSV = {0, 0, 0};
    private int mAlpha;
    @BindView(R.id.hue_cursor)
    ImageView hue_cursor;
    @BindView(R.id.img_hue)
    ImageView img_hue;
    @BindView(R.id.color_plate)
    ColorPlateView color_plate;
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.rl_container)
    RelativeLayout rl_container;
    @BindView(R.id.cv1)
    CircleView cv1;//十字灯
    @BindView(R.id.cv2)
    CircleView cv2;//大圆灯
    @BindView(R.id.cv3)
    CircleView cv3;//小圆灯
    @BindView(R.id.cv4)
    CircleView cv4;//大小圈

    @BindView(R.id.rl_lamp1)
    RelativeLayout rl_lamp;
    @BindView(R.id.rl_lamp2)
    RelativeLayout rl_lamp2;
    @BindView(R.id.rl_lamp3)
    RelativeLayout rl_lamp3;
    @BindView(R.id.rl_lamp4)
    RelativeLayout rl_lamp4;
    @BindView(R.id.seekbar)
    RangeSeekBar slide_bar;

    @BindView(R.id.et_temp)
    EditText et_temp;
    @BindView(R.id.et_jp_count)
    EditText et_jp_count;
    @BindView(R.id.et_jp_scconds)
    EditText et_jp_scconds;
    @BindView(R.id.et_bl)
    EditText et_bl;
    @BindView(R.id.btn_mode_breathe)
    Button btn_mode_breathe;//呼吸
    @BindView(R.id.btn_mode_random)
    Button btn_mode_random;//随机
    @BindView(R.id.btn_mode_normal)
    Button btn_mode_normal;//常在
    Device device;
    @BindView(R.id.plate_cursor)
    ImageView plate_cursor;
    @BindView(R.id.image_lamp)
    ImageView image_lamp;
    String topicName;
    String deviceMac;
    @BindView(R.id.tv_light_value)
    TextView tv_light;
    SharedPreferences preferences;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.et_color)
    EditText et_color;
    @BindView(R.id.swipeRefresh)
    PullToRefreshLayout swipeRefresh;
    @BindView(R.id.tv_furnace_value) TextView tv_furnace_value;
    @Override
    public void initParms(Bundle parms) {

    }

    @Override
    public int bindLayout() {
        setSteepStatusBar(true);
        return R.layout.activity_main;
    }

    @Override
    protected void onStart() {
        super.onStart();
        running = true;

        /**
         * 防止
         */
        Log.i(TAG,"-->onStart");

        if (preferences.contains("deviceMac") && result==0) {
            deviceMac = preferences.getString("deviceMac", "");
            if (!TextUtils.isEmpty(deviceMac)) {
                if (mqService!=null){
                    device=mqService.getDevice(deviceMac);
                }else {
                    device = new Device();
                }
                tv_name.setText(deviceMac);
                device.setDeviceMac(deviceMac);
                topicName = "tea/" + deviceMac + "/operate/set";
            }else {
                device=null;
            }
        }

        if (device==null){
            tv_name.setText("");
        }
        if (mqService != null && device != null && result == 0) {
            String topicName2 = "tea/" + deviceMac + "/status/set";
            mqService.getData(topicName2);
            countTimer.start();
        }
        result = 0;

    }


    int value = 0;
    int load2;
    MQTTMessageReveiver messageReveiver;
    @Override
    public void initView(View view) {
        initOnTouchListener();

        messageReveiver = new MQTTMessageReveiver();
        IntentFilter  intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        intentFilter.addAction("mqttmessage2");

//        filter.addAction(Intent.ACTION_TIME_TICK);
        this.registerReceiver(messageReveiver, intentFilter);
        preferences = getSharedPreferences("device", MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        deviceMac="vlinks_testcc50e3ccde8e";
//        editor.putString("deviceMac", deviceMac).commit();
        receiver = new MessageReceiver();
        slide_bar.setIndicatorTextDecimalFormat("0");
//        slide_bar.setValue(20);
//        slide_bar.setIndicatorTextDecimalFormat("0.0");
        Intent service = new Intent(this, MQService.class);
        bind = bindService(service, connection, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter("MainActivityTest");
        filter.addAction("offline");
        registerReceiver(receiver, filter);
        swipeRefresh.setHeaderView(new MyHeadRefreshView(this));
        swipeRefresh.setFooterView(new MyLoadMoreView(this));
        swipeRefresh.setRefreshListener(new BaseRefreshListener() {
            @Override
            public void refresh() {
                load2=1;
                if (mqService != null && device != null) {
                    String deviceMac = device.getDeviceMac();
                    String topicName = "tea/" + deviceMac + "/status/set";
                    int state=device.state;
                    if (state==0xb6 || state==0xb7){
                        ToastUtil.showShort(MainActivity.this,R.string.updating);
                        swipeRefresh.finishRefresh();
                    }else {
                        mqService.getData(topicName);
                        countTimer.start();
                    }

                }else {
                    ToastUtil.showShort(MainActivity.this,R.string.add_device2);
                    swipeRefresh.finishRefresh();
                }
            }

            @Override
            public void loadMore() {
                load2=2;
                if (mqService != null && device != null) {
                    int state=device.state;
                    if (state==0xb6 || state==0xb7){
                        ToastUtil.showShort(MainActivity.this,R.string.updating);
                        swipeRefresh.finishLoadMore();
                    }else {
                        String deviceMac = device.getDeviceMac();
                        String topicName = "tea/" + deviceMac + "/status/set";
                        mqService.getData(topicName);
                        countTimer.start();
                    }

                }else {
                    ToastUtil.showShort(MainActivity.this,R.string.add_device2);
                    swipeRefresh.finishLoadMore();
                }
            }
        });
        slide_bar.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                Log.i("TrackingTouch", "-->" + leftValue + "," + rightValue);
                value = Math.round(leftValue);
//                value=leftValue;
                tv_light.setText("" + value);
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                Log.i("TrackingTouch", "-->停止滑动");

                if (mqService != null && device != null) {
//                    device.setLight(Math.round(value));
                    device.setLight(value);
                    int success = mqService.send(topicName, device, 0x0d);
                    if (success==1) {
                        countTimer.start();
                    }else if (success==0){
                        ToastUtil.showShort(MainActivity.this,R.string.device_offline);
                    }

                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bind) {
            unbindService(connection);
        }
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        if (messageReveiver!=null){
            unregisterReceiver(messageReveiver);
        }
    }

    DialogLoad dialogLoad;
    int load = R.string.loading;

    private void setLoadDialog() {
        if (dialogLoad != null && dialogLoad.isShowing()) {
            return;
        }

        dialogLoad = new DialogLoad(this);
        dialogLoad.setCanceledOnTouchOutside(false);
        dialogLoad.setLoad(load);
        dialogLoad.show();
    }

    CountTimer countTimer = new CountTimer(2000, 1000);

    class CountTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public CountTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);

        }

        @Override
        public void onTick(long millisUntilFinished) {
            setLoadDialog();
        }

        @Override
        public void onFinish() {
            if (dialogLoad != null && dialogLoad.isShowing()) {
                dialogLoad.dismiss();
            }
            if (swipeRefresh!=null){
                if (load2==1){
                    swipeRefresh.finishRefresh();
                }else if (load2==2){
                    swipeRefresh.finishLoadMore();
                }
            }
        }
    }

    @Override
    public void doBusiness(Context mContext) {

    }

    @Override
    public void onBackPressed() {
        if (dialogLoad != null && dialogLoad.isShowing()) {
            dialogLoad.dismiss();
            return;
        }
        super.onBackPressed();

    }

    int lamp = 0;
    double blewTemp;
    int blewCount = 0;
    int blewSeconds = 0;
    double blewBl = 0;
    double water = 0;

    @OnClick({R.id.btn_add, R.id.btn_sleep,R.id.btn_stop, R.id.btn_start_heater, R.id.btn_cp, R.id.image_lamp, R.id.rl_lamp1, R.id.rl_lamp2, R.id.rl_lamp3, R.id.rl_lamp4, R.id.btn_ensure, R.id.color_plate, R.id.btn_mode_breathe, R.id.btn_mode_random, R.id.btn_mode_normal})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add:
                Intent intent = new Intent(this, AddDeviceActivity.class);
                startActivityForResult(intent, 100);
                break;
            case R.id.btn_sleep:
                if (device == null) {
                    ToastUtil.showShort(this, R.string.tip_no_device);
                    break;
                }

                if (dialogLoad != null && dialogLoad.isShowing()) {
                    ToastUtil.showShort(this, R.string.tip_later);
                    break;
                }
                device.setControl(0xc0);//休眠
                if (mqService != null) {
                    int success = mqService.send(topicName, device, 0x04);
                    if (success==1) {
                        countTimer.start();
                    } else if (success==0){
                        ToastUtil.showShort(this, R.string.no_net);
                    }
                }
                break;
            case R.id.btn_start_heater:
                if (device == null) {
                    ToastUtil.showShort(this, R.string.tip_no_device);
                    break;
                }

                if (dialogLoad != null && dialogLoad.isShowing()) {
                    ToastUtil.showShort(this, R.string.tip_later);
                    break;
                }
                device.setControl(0xc1);//开始预热
                if (mqService != null) {
                    int success = mqService.send(topicName, device, 0x04);
                    if (success==1) {
                        countTimer.start();
                    } else if (success==0){
                        ToastUtil.showShort(this, R.string.no_net);
                    }
                }
                break;
            case R.id.btn_stop:
                if (device == null) {
                    ToastUtil.showShort(this, R.string.tip_no_device);
                    break;
                }

                if (dialogLoad != null && dialogLoad.isShowing()) {
                    ToastUtil.showShort(this, R.string.tip_later);
                    break;
                }
                device.setControl(0xc1);//开始预热
                if (mqService != null) {
                    int success = mqService.send(topicName, device, 0x04);
                    if (success==1) {
                        countTimer.start();
                    } else if (success==0){
                        ToastUtil.showShort(this, R.string.no_net);
                    }
                }
                break;
            case R.id.btn_cp:
                if (device == null) {
                    ToastUtil.showShort(this, R.string.tip_no_device);
                    break;
                }

                if (dialogLoad != null && dialogLoad.isShowing()) {
                    ToastUtil.showShort(this, R.string.tip_later);
                    break;
                }
                String temp = et_temp.getText().toString();
                if (TextUtils.isEmpty(temp)) {
                    ToastUtil.showShort(this, R.string.enter_temp);
                    break;
                }
                blewTemp = Double.parseDouble(temp);
                if (blewTemp<65 || blewTemp>95){
                    ToastUtil.showShort(this, R.string.temp_range);
                    break;
                }
                String count = et_jp_count.getText().toString();
                if (TextUtils.isEmpty(count)) {
                    ToastUtil.showShort(this, R.string.enter_count);
                    break;
                }else {
                    blewCount = Integer.parseInt(count);
                    if (blewCount<1){
                        ToastUtil.showShort(this, R.string.blew_time_range);
                        break;
                    }
                }
                blewCount = Integer.parseInt(count);
                String seconds = et_jp_scconds.getText().toString();
                if (TextUtils.isEmpty(seconds)) {
                    ToastUtil.showShort(this, R.string.enter_time);
                    break;
                } else {
                    blewSeconds = Integer.parseInt(seconds);
                    if (blewSeconds < 5 || blewSeconds > 30) {
                        ToastUtil.showShort(this, R.string.enter_time_rango);
                        break;
                    }
                }

                String bl = et_bl.getText().toString();
                if (TextUtils.isEmpty(bl)) {
                    ToastUtil.showShort(this, R.string.enter_bl);
                    break;
                } else {
                    blewBl = Double.parseDouble(bl);
                    if (blewBl < 100 || blewBl > 350) {
                        ToastUtil.showShort(this, R.string.enter_bl_rabge);
                        break;
                    }
                }

                int state = device.getState();
                if (state==0xb2){
                    ToastUtil.showShort(this, R.string.brew2);
                    break;
                }
                device.setBrewTemp(blewTemp);
                device.setBrewCount(blewCount);
                device.setBrewTime(blewSeconds);
                device.setBrewBl(blewBl);
//                water = blewCount * blewBl;
                water = blewBl;
                device.setWater(water);
                if (mqService != null) {
                    device.setControl(0xc2);
                    int success = mqService.send(topicName, device, 0x01);
                    if (success==1) {
                        countTimer.start();
                    } else if (success==0){
                        ToastUtil.showShort(this, R.string.no_net);
                    }
                }
                break;
            case R.id.image_lamp:
                if (device == null) {
                    ToastUtil.showShort(this, R.string.tip_no_device);
                    break;
                }
                if (dialogLoad != null && dialogLoad.isShowing()) {
                    ToastUtil.showShort(this, R.string.tip_later);
                    break;
                }
                int led = device.getLed();
                led = led == 1 ? 0 : 1;
                device.setLed(led);
                if (led == 0) {

                    device.setControl3(0);
                } else if (led == 1) {
                    device.setControl3(128);
                }

                if (mqService != null) {
                    int success = mqService.send(topicName, device, 0x06);
                    if (success==1) {
                        countTimer.start();
                    } else if (success==0){
                        ToastUtil.showShort(this, R.string.no_net);
                    }
                }
                break;
            case R.id.rl_lamp1:


//                if (dialogLoad!=null && dialogLoad.isShowing()){
//                    ToastUtil.showShort(this,"请稍后");
//                    break;
//                }
                lamp = 1;
                setLampBack(lamp);
//                int crossRGB=Color.HSVToColor(mCurrentHSV);
//                grb=intToHex(crossRGB);
//                device.setCrossR(grb[0]);
//                device.setCrossG(grb[1]);
//                device.setCrossB(grb[2]);
//                if (mqService!=null){
//                    boolean success=mqService.send(topicName,device,0x0a);
//                    if (success){
//                        countTimer.start();
//                    }
//                }

                break;
            case R.id.rl_lamp2:
                lamp = 2;
                setLampBack(lamp);
                break;
            case R.id.rl_lamp3:
                lamp = 3;
                setLampBack(lamp);
                break;
            case R.id.rl_lamp4:
                lamp = 4;
                setLampBack(lamp);
                break;
            case R.id.btn_ensure:
                String colorValue = et_color.getText().toString().trim();
                if (TextUtils.isEmpty(colorValue)) {
                    ToastUtil.showShort(this, R.string.is_color);
                    break;
                } else {
                    if (isColorValue(colorValue)) {
                        if (lamp == 0) {
                            ToastUtil.showShort(this, R.string.choose_lamp);
                        } else {

                            setLampColor2(lamp, Color.parseColor(colorValue));
                            sendEditColor(lamp, Color.parseColor(colorValue));
                        }

                    } else {
                        ToastUtil.showShort(this, R.string.is_color);
                        break;
                    }
                }
                break;
            case R.id.color_plate:
                setLampColor(lamp);
                break;
            case R.id.btn_mode_breathe:
                if (device == null) {
                    ToastUtil.showShort(this, R.string.tip_no_device);
                    break;
                }

                if (dialogLoad != null && dialogLoad.isShowing()) {
                    ToastUtil.showShort(this, R.string.tip_later);
                    break;
                }
                setLedMode(1);
                device.setControl3(64);
                if (mqService != null) {
                    int success = mqService.send(topicName, device, 0x05);
                    if (success==1) {
                        countTimer.start();
                    } else if (success==0){
                        ToastUtil.showShort(this, R.string.no_net);
                    }
                }

                break;
            case R.id.btn_mode_random:
                if (device == null) {
                    ToastUtil.showShort(this, R.string.tip_no_device);
                    break;
                }

                if (dialogLoad != null && dialogLoad.isShowing()) {
                    ToastUtil.showShort(this, R.string.tip_later);
                    break;
                }
                setLedMode(2);
                device.setControl3(32);

                if (mqService != null) {
                    int success = mqService.send(topicName, device, 0x05);
                    if (success==1) {
                        countTimer.start();
                    } else if (success==0){
                        ToastUtil.showShort(this, R.string.no_net);
                    }
                }
                break;
            case R.id.btn_mode_normal:
                if (device == null) {
                    ToastUtil.showShort(this, R.string.tip_no_device);
                    break;
                }

                if (dialogLoad != null && dialogLoad.isShowing()) {
                    ToastUtil.showShort(this, R.string.tip_later);
                    break;
                }
                setLedMode(3);
                device.setControl3(16);
                if (mqService != null) {
                    int success = mqService.send(topicName, device, 0x05);
                    if (success==1) {
                        countTimer.start();
                    } else if (success==0){
                        ToastUtil.showShort(this, R.string.no_net);
                    }
                }
                break;
        }
    }

    private boolean isColorValue(String colorValue) {
        try {
            Color.parseColor(colorValue);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void setLedMode(int mode) {
        if (mode == 1) {
            btn_mode_breathe.setBackgroundResource(R.drawable.shape_blue);
            btn_mode_random.setBackgroundResource(R.drawable.shape_gray);
            btn_mode_normal.setBackgroundResource(R.drawable.shape_gray);
            btn_mode_breathe.setTextColor(Color.parseColor("#ffffff"));
            btn_mode_random.setTextColor(Color.parseColor("#333333"));
            btn_mode_normal.setTextColor(Color.parseColor("#333333"));
        } else if (mode == 2) {
            btn_mode_breathe.setBackgroundResource(R.drawable.shape_gray);
            btn_mode_random.setBackgroundResource(R.drawable.shape_blue);
            btn_mode_normal.setBackgroundResource(R.drawable.shape_gray);
            btn_mode_breathe.setTextColor(Color.parseColor("#333333"));
            btn_mode_random.setTextColor(Color.parseColor("#ffffff"));
            btn_mode_normal.setTextColor(Color.parseColor("#333333"));
        } else if (mode == 3) {
            btn_mode_breathe.setBackgroundResource(R.drawable.shape_gray);
            btn_mode_random.setBackgroundResource(R.drawable.shape_gray);
            btn_mode_normal.setBackgroundResource(R.drawable.shape_blue);
            btn_mode_breathe.setTextColor(Color.parseColor("#333333"));
            btn_mode_random.setTextColor(Color.parseColor("#333333"));
            btn_mode_normal.setTextColor(Color.parseColor("#ffffff"));
        }

    }

    int[] grb = new int[3];

    private int[] intToHex(int color) {
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        grb[0] = red;
        grb[1] = green;
        grb[2] = blue;
        return grb;
    }

    private void setLampColor(int lamp) {
        if (lamp == 1) {
            cv1.setRing_color((Color.HSVToColor(mCurrentHSV)));
        } else if (lamp == 2) {
            cv2.setRing_color((Color.HSVToColor(mCurrentHSV)));
        } else if (lamp == 3) {
            cv3.setRing_color((Color.HSVToColor(mCurrentHSV)));
        } else if (lamp == 4) {
            cv4.setRing_color((Color.HSVToColor(mCurrentHSV)));
        }
    }

    private void setLampColor2(int lamp, int color) {
        if (lamp == 1) {
            cv1.setRing_color(color);
        } else if (lamp == 2) {
            cv2.setRing_color(color);
        } else if (lamp == 3) {
            cv3.setRing_color(color);
        } else if (lamp == 4) {
            cv4.setRing_color(color);
        }
    }

    private void setLampBack(int lamp) {
        if (lamp == 1) {
            rl_lamp.setBackground(getResources().getDrawable(R.drawable.shape_lamp));
            rl_lamp2.setBackgroundResource(0);
            rl_lamp3.setBackgroundResource(0);
            rl_lamp4.setBackgroundResource(0);
        } else if (lamp == 2) {
            rl_lamp.setBackgroundResource(0);
            rl_lamp2.setBackground(getResources().getDrawable(R.drawable.shape_lamp));
            rl_lamp3.setBackgroundResource(0);
            rl_lamp4.setBackgroundResource(0);
        } else if (lamp == 3) {
            rl_lamp.setBackgroundResource(0);
            rl_lamp2.setBackgroundResource(0);
            rl_lamp3.setBackground(getResources().getDrawable(R.drawable.shape_lamp));
            rl_lamp4.setBackgroundResource(0);
        } else if (lamp == 4) {
            rl_lamp.setBackgroundResource(0);
            rl_lamp2.setBackgroundResource(0);
            rl_lamp3.setBackgroundResource(0);
            rl_lamp4.setBackground(getResources().getDrawable(R.drawable.shape_lamp));
        }
//        setLampColor(lamp);

    }

    /**
     * 设置色彩
     *
     * @param color
     */
    private void setColorHue(float color) {
        mCurrentHSV[0] = color;
//        setLampColor(lamp);
    }

    private float getColorHue() {
        return mCurrentHSV[0];
    }

    /**
     * 设置颜色深浅
     */
    private void setColorSat(float color) {
        this.mCurrentHSV[1] = color;
    }

    private float getColorSat() {
        return this.mCurrentHSV[1];
    }

    /**
     * 设置颜色明暗
     */
    private void setColorVal(float color) {
        this.mCurrentHSV[2] = color;
    }

    private float getColorVal() {
        return mCurrentHSV[2];
    }

    /**
     * 获取int颜色
     */
    int red;
    int green;
    int blue;


    int first = 0;

    /**
     * 触摸监听
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initOnTouchListener() {
        //色彩板的触摸监听
        img_hue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                float y = event.getY();
                if (y < 0.f) y = 0.f;
                if (y > img_hue.getMeasuredHeight())
                    y = img_hue.getMeasuredHeight() - 0.001f;
                float colorHue = 360.f - 360.f / img_hue.getMeasuredHeight() * y - 1f;
                if (colorHue == 360.f) colorHue = 0.f;
                Log.i("MotionEvent", "--->" + y);
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
//
//                        scrollView.setOnTouchListener(new View.OnTouchListener() {
//                            @Override
//                            public boolean onTouch(View v, MotionEvent event) {
//                                return true;
//                            }
//                        });


                        Log.i("MotionEvent", "-->按下");
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.i("MotionEvent", "-->松开");
                        first = 0;
                        setColorHue(colorHue);
                        color_plate.setHue(colorHue);
                        moveHueCursor((int) y);

//                        scrollView.setOnTouchListener(new View.OnTouchListener() {
//                            @Override
//                            public boolean onTouch(View v, MotionEvent event) {
//                                return false;
//                            }
//                        });


                        break;
                }
                return true;
            }
        });

//        //颜色样板的触摸监听
        //颜色样板的触摸监听
        color_plate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_UP) {
                    float x = event.getX();
                    float y = event.getY();
                    if (x < 0.f) x = 0.f;
                    if (x > color_plate.getMeasuredWidth()) x = color_plate.getMeasuredWidth();
                    if (y < 0.f) y = 0.f;
                    if (y > color_plate.getMeasuredHeight()) y = color_plate.getMeasuredHeight();

                    setColorSat(1.f / color_plate.getMeasuredWidth() * x);//颜色深浅
                    setColorVal(1.f - (1.f / color_plate.getMeasuredHeight() * y));//颜色明暗
                    getColor();
                    movePlateCursor((int) y);
                    setLampColor(lamp);
                    sendPlateColor(lamp);
//                    tv_color.setBackgroundColor(getColor());

//                    GradientDrawable myGrad = (GradientDrawable) tv_light_bj.getBackground();
//                    myGrad.setColor(getColor());
//                    Log.e("DDDDDDDDDDDDDDDDDDDZ", "onTouch: -->"+getColor() );
//                    if (mListener!=null){
//                        mListener.onColorChange(ChooseColorActvity.this,getColor());
//                    }
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 获取int颜色
     */

    private int getColor() {
        int argb = Color.HSVToColor(mCurrentHSV);
        red = ((argb & 0x00FF0000) >> 16);
        green = ((argb & 0x0000FF00) >> 8);
        blue = argb & 0x000000FF;
        Log.e(TAG, "getColor: -->" + red + "," + green + "," + blue);
        return mAlpha << 24 | (argb & 0x00ffffff);
    }

    private void sendEditColor(int lamp, int color) {
        if (device == null) {
            ToastUtil.showShort(this, R.string.tip_no_device);
            return;
        }

        if (dialogLoad != null && dialogLoad.isShowing()) {
            ToastUtil.showShort(this, R.string.tip_later);
            return;
        }
        if (lamp == 1) {
            grb = intToHex(color);
            device.setCrossR(grb[0]);
            device.setCrossG(grb[1]);
            device.setCrossB(grb[2]);
            if (mqService != null) {
                int success = mqService.send(topicName, device, 0x0a);
                if (success==1) {
                    countTimer.start();
                } else if (success==0){
                    ToastUtil.showShort(this, R.string.no_net);
                }
            }
        } else if (lamp == 2) {
            grb = intToHex(color);
            device.setOutR(grb[0]);
            device.setOutG(grb[1]);
            device.setOutB(grb[2]);
            if (mqService != null) {
                int success = mqService.send(topicName, device, 0x02);
                if (success==1) {
                    countTimer.start();
                } else if (success==0){
                    ToastUtil.showShort(this, R.string.no_net);
                }
            }
        } else if (lamp == 3) {
            grb = intToHex(color);
            device.setInR(grb[0]);
            device.setInG(grb[1]);
            device.setInB(grb[2]);
            if (mqService != null) {
                int success = mqService.send(topicName, device, 0x09);
                if (success==1) {
                    countTimer.start();
                } else if (success==0){
                    ToastUtil.showShort(this, R.string.no_net);
                }
            }
        } else if (lamp == 4) {
            grb = intToHex(color);
            device.setOiR(grb[0]);
            device.setOiG(grb[1]);
            device.setOiB(grb[2]);
            if (mqService != null) {
                int success = mqService.send(topicName, device, 0x0b);
                if (success==1) {
                    countTimer.start();
                } else if (success==0){
                    ToastUtil.showShort(this, R.string.no_net);
                }
            }
        }
    }

    private void sendPlateColor(int lamp) {
        if (device == null) {
            ToastUtil.showShort(this, R.string.tip_no_device);
            return;
        }

        if (dialogLoad != null && dialogLoad.isShowing()) {
            ToastUtil.showShort(this, R.string.tip_later);
            return;
        }
        if (lamp == 1) {
            int crossRGB = Color.HSVToColor(mCurrentHSV);
            grb = intToHex(crossRGB);
            device.setCrossR(grb[0]);
            device.setCrossG(grb[1]);
            device.setCrossB(grb[2]);
            if (mqService != null) {
                int success = mqService.send(topicName, device, 0x0a);
                if (success==1) {
                    countTimer.start();
                } else if (success==0){
                    ToastUtil.showShort(this, R.string.no_net);
                }
            }
        } else if (lamp == 2) {
            int outRGB = Color.HSVToColor(mCurrentHSV);
            grb = intToHex(outRGB);
            device.setOutR(grb[0]);
            device.setOutG(grb[1]);
            device.setOutB(grb[2]);
            if (mqService != null) {
                int success = mqService.send(topicName, device, 0x02);
                if (success==1) {
                    countTimer.start();
                } else if (success==0){
                    ToastUtil.showShort(this, R.string.no_net);
                }
            }
        } else if (lamp == 3) {
            int inRGB = Color.HSVToColor(mCurrentHSV);
            grb = intToHex(inRGB);
            device.setInR(grb[0]);
            device.setInG(grb[1]);
            device.setInB(grb[2]);
            if (mqService != null) {
                int success = mqService.send(topicName, device, 0x09);
                if (success==1) {
                    countTimer.start();
                } else if (success==0){
                    ToastUtil.showShort(this, R.string.no_net);
                }
            }
        } else if (lamp == 4) {
            int outinRGB = Color.HSVToColor(mCurrentHSV);
            grb = intToHex(outinRGB);
            device.setOiR(grb[0]);
            device.setOiG(grb[1]);
            device.setOiB(grb[2]);
            if (mqService != null) {
                int success = mqService.send(topicName, device, 0x0b);
                if (success==1) {
                    countTimer.start();
                } else if (success==0){
                    ToastUtil.showShort(this, R.string.no_net);
                }
            }
        }
    }

    /**
     * 移动色彩样板指针
     */
    private void moveHueCursor(int y2) {//ConstraintLayout$LayoutParams
//        int top=color_plate.getPaddingTop();
//        float y = img_hue.getMeasuredHeight() - (getColorHue() * img_hue.getMeasuredHeight() / 360.f);
//        if (y == img_hue.getMeasuredHeight()) y = 0.f;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) hue_cursor.getLayoutParams();
//            layoutParams.leftMargin = (int) (img_hue.getLeft() - Math.floor(hue_cursor.getMeasuredWidth() / 9) - 60);
//        layoutParams.topMargin = (int) (img_hue.getTop() + y - Math.floor(hue_cursor.getMeasuredHeight() / 2) - 60);

        layoutParams.topMargin = y2;

        hue_cursor.setLayoutParams(layoutParams);
    }

    /**
     * 移动最终颜色样板指针
     */
    private void movePlateCursor(int y2) {
        final float x = getColorSat() * color_plate.getMeasuredWidth();
        final float y = (1.f - getColorVal()) * color_plate.getMeasuredHeight();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) plate_cursor.getLayoutParams();
        layoutParams.leftMargin = (int) (color_plate.getLeft() + x - Math.floor(plate_cursor.getMeasuredWidth() / 2));
//        layoutParams.topMargin = (int) (color_plate.getTop() + y - Math.floor(plate_cursor.getMeasuredHeight() / 2) - container.getPaddingTop());
        layoutParams.topMargin = y2;
        plate_cursor.setLayoutParams(layoutParams);

    }

    private String TAG="MainActivity";
    MQService mqService;
    boolean bind;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MQService.LocalBinder binder = (MQService.LocalBinder) service;
            mqService = binder.getService();
            Log.i(TAG,"-->绑定service");
            if (mqService != null && device != null) {
                mqService.setDevice(deviceMac, device);
                String deviceMac = device.getDeviceMac();
                String topicName = "tea/" + deviceMac + "/status/set";
                mqService.getData(topicName);
                countTimer.start();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        result=0;
    }

    int result = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 100) {
            result = 1;
            deviceMac = data.getStringExtra("deviceMac");
            if (!TextUtils.isEmpty(deviceMac) && mqService != null) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("deviceMac", deviceMac);
                editor.commit();
                device = new Device();
                tv_name.setText(deviceMac);
                device.setDeviceMac(deviceMac);
                topicName = "tea/" + deviceMac + "/operate/set";
                mqService.setDevice(deviceMac, device);
                String deviceMac = device.getDeviceMac();
                String topicName2 = "tea/" + deviceMac + "/status/set";
                mqService.getData(topicName2);
                countTimer.start();
            }
        } else if (resultCode == 200) {
            result = 1;
        }
    }

    StringBuilder sb = new StringBuilder();
    @BindView(R.id.tv_line)
    TextView tv_line;
    @BindView(R.id.tv_heater)
    TextView tv_heater;
    @BindView(R.id.tv_error)
    TextView tv_error;

    private void setMode(Device device) {
        boolean online = device.isOnline();
//        double brewTemp = device.getBrewTemp();
//        et_temp.setText(Math.round(brewTemp)+"");
//        int brewCount = device.getBrewCount();
//        et_jp_count.setText(brewCount+"");
//        double water=device.getWater();
//        et_bl.setText(Math.round(water)+"");
//        int brewTime = device.getBrewTime();
//        et_jp_scconds.setText(brewTime+"");
        int furnace=device.getFurnace();

        tv_furnace_value.setText(furnace+"");
        if (online) {
            tv_line.setText(R.string.device_online);
        } else {
            tv_line.setText(R.string.device_offline);
        }
        int state = device.getState();
//        if (state == 0xb0) {
//            tv_heater.setText(R.string.heater_state1);
//        }
//        else if (state == 0xb1) {
//            tv_heater.setText(R.string.heater_state2);
//        }
        if (state == 0xb2) {
            tv_heater.setText(R.string.heater_state3);
        } else if (state == 0xb3) {
            tv_heater.setText(R.string.heater_state4);
        } else if (state == 0xb4) {
            tv_heater.setText(R.string.heater_state5);
        } else if (state == 0xb5) {
            tv_heater.setText(R.string.heater_state6);
        } else if (state == 0xb6) {
            tv_heater.setText(R.string.heater_state7);
        } else if (state == 0xb7) {
            tv_heater.setText(R.string.heater_state8);
        }else if (state==0xb8){
            tv_heater.setText(R.string.heater_state9);
        }else if (state==0xb9){
            tv_heater.setText(R.string.heater_state10);
        }
//        int control4=device.getControl3();


        int error = device.getError();
        int x[] = TenTwoUtil.changeToTwo(error);
        sb.setLength(0);
        if (x[0] == 1) {
            sb.append("NTC开短路");
        }
        if (x[1] == 1) {
            if (sb.length()>0){
                sb.append(",水箱水位过低");
            }else {
                sb.append("水箱水位过低");
            }
        }
        if (x[2] == 1) {
            if (sb.length()>0){
                sb.append(",70秒锅炉小于70度");
            }else {
                sb.append("70秒锅炉小于70度");
            }
        }
        if (x[3] == 1) {
            if (sb.length()>0){
                sb.append(",炉温过高");
            }else {
                sb.append("炉温过高");
            }
        }
        if (x[4] == 1) {
            if (sb.length()>0){
                sb.append(",累计冲泡咖啡50杯标志");
            }else {
                sb.append("累计冲泡咖啡50杯标志");
            }
        }
        if (x[5] == 1) {
            if (sb.length()>0){
                sb.append(",垃圾盒报警");
            }else {
                sb.append("垃圾盒报警");
            }
        }
        if (x[6] == 1) {
            if (sb.length()>0){
                sb.append(",茶饮机滑动盖未关闭");
            }else {
                sb.append("茶饮机滑动盖未关闭");
            }
        }
        if (x[7] == 1) {
            if (sb.length()>0){
                sb.append(",水泵工作15S无流量信号");
            }else {
                sb.append("水泵工作15S无流量信号");
            }
        }
        String errorContent=sb.toString();
        if (TextUtils.isEmpty(errorContent)){
            tv_error.setText(R.string.normal2);
        }else {
            tv_error.setText(errorContent.trim());
        }

        int led = device.getLed();
        if (led == 1) {
            image_lamp.setImageResource(R.mipmap.img_open);
        } else {
            image_lamp.setImageResource(R.mipmap.img_close);
        }
        int crossR = device.getCrossR();
        int crossG = device.getCrossG();
        int crossB = device.getCrossB();

        int crossColor = Color.rgb(crossR, crossG, crossB);
        cv1.setRing_color(crossColor);

        int outR = device.getOutR();
        int outG = device.getOutG();
        int outB = device.getOutB();
        int outColor = Color.rgb(outR, outG, outB);
        cv2.setRing_color(outColor);

        int inR = device.getInR();
        int inG = device.getInG();
        int inB = device.getInB();
        int inColor = Color.rgb(inR, inG, inB);
        cv3.setRing_color(inColor);

        int oiR=device.getOiR();//大小灯光R
        int oiG=device.getOiG();//大小灯光G
        int oiB=device.getOiB();//大小灯光B
        int oiColor = Color.rgb(oiR, oiG, oiB);
        cv4.setRing_color(oiColor);
        int control3 = device.getControl3();
        int[] x2 = TenTwoUtil.changeToTwo(control3);
        int heat=x2[7];
        if (state==0xb0){
            if (heat==1){
                tv_heater.setText(R.string.heater_state2);
            }else if (heat==0){
                tv_heater.setText(R.string.heater_state11);
            }
        }

        if (x2[1] == 1) {
            btn_mode_breathe.setBackgroundResource(R.drawable.shape_blue);
            btn_mode_random.setBackgroundResource(R.drawable.shape_gray);
            btn_mode_normal.setBackgroundResource(R.drawable.shape_gray);
            btn_mode_breathe.setTextColor(Color.parseColor("#ffffff"));
            btn_mode_random.setTextColor(Color.parseColor("#333333"));
            btn_mode_normal.setTextColor(Color.parseColor("#333333"));
        } else if (x2[2] == 1) {
            btn_mode_breathe.setBackgroundResource(R.drawable.shape_gray);
            btn_mode_random.setBackgroundResource(R.drawable.shape_blue);
            btn_mode_normal.setBackgroundResource(R.drawable.shape_gray);
            btn_mode_breathe.setTextColor(Color.parseColor("#333333"));
            btn_mode_random.setTextColor(Color.parseColor("#ffffff"));
            btn_mode_normal.setTextColor(Color.parseColor("#333333"));
        } else if (x2[3] == 1) {
            btn_mode_breathe.setBackgroundResource(R.drawable.shape_gray);
            btn_mode_random.setBackgroundResource(R.drawable.shape_gray);
            btn_mode_normal.setBackgroundResource(R.drawable.shape_blue);
            btn_mode_breathe.setTextColor(Color.parseColor("#333333"));
            btn_mode_random.setTextColor(Color.parseColor("#333333"));
            btn_mode_normal.setTextColor(Color.parseColor("#ffffff"));
        }
        int light = device.getLight();

        tv_light.setText("" + light);
        if (light>0 && light<=10){
            slide_bar.setValue(light);
        }
    }

    private MessageReceiver receiver;
    public static boolean running = false;

    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent!=null && "offline".equals(intent.getAction())){
                if (device!=null){
                    device.setOnline(false);
                    setMode(device);
                    if (mqService!=null){
                        mqService.setDevice(device);
                    }
                }
            }else {
                String deviceMac2 = intent.getStringExtra("deviceMac");
                if (!TextUtils.isEmpty(deviceMac2) && deviceMac2.equals(deviceMac)) {
                    if (intent.hasExtra("device")) {
                        device = (Device) intent.getSerializableExtra("device");
                        setMode(device);
                    } else {
                        device = null;
                        tv_name.setText("");
                        ToastUtil.showShort(MainActivity.this, "设备已重置");
                    }
                }
            }

        }
    }
}
