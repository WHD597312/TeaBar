package com.peihou.teabar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Entity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;



public class MQService extends Service {

    private String TAG = "MQService";
    private String host = "tcp://47.98.131.11:1883";//mqtt连接服务端ip
    private String userName = "admin";//mqtt连接用户名
    private String passWord = "Xr7891122";//mqtt连接密码


    private MqttClient client;//mqtt客户端

    public String myTopic = "rango/dc4f220aa96e/transfer";
    private LinkedList<String> offlineList = new LinkedList<String>();//离线主题


    private MqttConnectOptions options;

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private LocalBinder binder = new LocalBinder();
    String clientId;
    private int times = 0;
    String reconnect = null;

    SharedPreferences.Editor editor;
    @Nullable
    @Override

    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences("device", MODE_PRIVATE);
        editor=preferences.edit();
        Log.i(TAG, "onCreate");
        clientId = UUID.getUUID(this);
        Log.i("clientId", "-->" + clientId);
//        preferences = getSharedPreferences("my", Context.MODE_PRIVATE);

//        new InitMQttAsync().execute();
//        init();
        new InitMQttAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        String reconnect = "";
        if (intent != null) {
            reconnect = intent.getStringExtra("reconnect");
        }
        connect(0);
//        if (!Utils.isEmpty(reconnect)) {
//            CountTimer countTimer = new CountTimer(2000, 1000);
//            countTimer.start();
//        }
        return super.onStartCommand(intent, flags, startId);
    }

    public class LocalBinder extends Binder {
        public MQService getService() {
            Log.i(TAG, "Binder");
            return MQService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        try {
            Log.i(TAG, "onDestroy");
            scheduler.shutdown();
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean stopService(Intent name) {
        Log.i(TAG, "stopService");
        return super.stopService(name);
    }

    public void connect(int state) {
        try {
            new ConAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class ConAsync extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            int code=0;
            try {
                if (client != null && client.isConnected() == false) {
                    client.connect(options);
                }

//                if (client.isConnected() == false) {
//                    client.connect(options);
//                }
                List<String> topicNames = getTopicNames();
                if (client.isConnected() && !topicNames.isEmpty()) {
                    for (String topicName : topicNames) {
                        if (!TextUtils.isEmpty(topicName)) {
                            client.subscribe(topicName, 1);
                            Log.i("client", "-->" + topicName);
                        }
                    }
                    code=1;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;

        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (integer==1){
                new LoadData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    class LoadData extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            for (Map.Entry<String,Device> entry:deviceMap.entrySet()){
                String deviceMac=entry.getKey();
                String topicName = "tea/" + deviceMac + "/status/set";
                getData(topicName);
            }
            return null;
        }
    }
    class InitMQttAsync extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            init();
            return null;
        }
    }
    private String result;

    /**
     * 初始化MQTT
     */
    private void init() {
        try {
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存

            client = new MqttClient(host, clientId,
                    new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(15);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(60);
            options.setAutomaticReconnect(true);


//            options.setWill("sssssssss","rangossssss".getBytes("UTF-8"),1,false);

            //设置回调
            client.setCallback(new MqttCallback() {

                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    System.out.println("connectionLost----------");
                    startReconnect();
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                    System.out.println("deliveryComplete---------"
                            + token.isComplete());
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message) {
                    try {
                        new LoadAsyncTask().execute(topicName, message.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    int groupPostion = 0;
    int childPosition = 0;
    int timerTaskWeek = 0;
    private int falling = -1;


    NotificationManager mNotificationManager;
    String preMacAddress;/**处理倾倒的设备mac
     如果mqtt接收到当前的macAddress与上一个mac地址相同并且设备没有倾倒，就从通知中自动移除这个设备的倾倒消息，preMacAddress的值为空字符串*/

    /**
     * 处理mqtt接收到的消息
     */
    class LoadAsyncTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... strings) {
            int code=0;
            String topicName = strings[0];
            Log.i(TAG,"-->"+topicName);

            String message = strings[1];
            Log.i(TAG,"-->"+message);
//            String topicName="tea/"+deviceMac+"/status/transfer";
//            String topicName2="tea/"+deviceMac+"/operate/transfer";
//            String topicName3="tea/"+deviceMac+"/extra/transfer";
//            String topicName4="tea/"+deviceMac+"/reset/transfer";
//            String topicName2="tea/"+deviceMac+"/operate/transfer";
            try {
                String stings[] = topicName.split("/");
                String deviceMac = stings[1];
                Device device=null;
                if (topicName.contains("status")){
                    String topicName2 = "tea/" + deviceMac + "/status/set";
                    getData(topicName2);
                }
                else if (topicName.contains("reset")) {
                    if (deviceMap.containsKey(deviceMac)) {
                        editor.clear();
                        editor.commit();
                        deviceMap.remove(deviceMac);
                        Intent intent=new Intent("MainActivityTest");
                        intent.putExtra("deviceMac",deviceMac);
                        sendBroadcast(intent);
                        return 0;
                    }
                }else if (topicName.contains("lwt")){
                    if (deviceMap.containsKey(deviceMac)) {
                        device=deviceMap.get(deviceMac);
                        device.setOnline(false);
                        deviceMap.put(deviceMac,device);
                        Intent intent=new Intent("MainActivityTest");
                        intent.putExtra("deviceMac",deviceMac);
                        intent.putExtra("device",device);
                        sendBroadcast(intent);
                        return 0;
                    }
                }else {
                    if (deviceMap.containsKey(deviceMac)) {
                        device=deviceMap.get(deviceMac);
                        if (isJsonData(message)){
                            JSONObject jsonObject=new JSONObject(message);
                            JSONArray jsonArray= jsonObject.getJSONArray("Coffee");

                            int state=jsonArray.getInt(2);
                            int error=jsonArray.getInt(3);
                            int control3=jsonArray.getInt(4);

                            int inR=jsonArray.getInt(5);
                            int inG=jsonArray.getInt(6);
                            int inB=jsonArray.getInt(7);

                            int outR=jsonArray.getInt(8);
                            int outG=jsonArray.getInt(9);
                            int outB=jsonArray.getInt(10);

                            int crossR=jsonArray.getInt(11);
                            int crossG=jsonArray.getInt(12);
                            int crossB=jsonArray.getInt(13);
                            int waterHigh=jsonArray.getInt(14);
                            int waterLow=jsonArray.getInt(15);
                            int water=waterHigh*256+waterLow;
                            int brewTemp=jsonArray.getInt(17);
                            int brewTime=jsonArray.getInt(18);
                            int furnace=jsonArray.getInt(20);//爐溫
                            int brewCount=jsonArray.getInt(24);
                            int light=jsonArray.getInt(25);
                            int oiR=jsonArray.getInt(19);//大小灯光R
                            int oiG=jsonArray.getInt(26);//大小灯光G
                            int oiB=jsonArray.getInt(27);//大小灯光B

                            int[] x=TenTwoUtil.changeToTwo(control3);
                            int led=x[0];
                            device.setOnline(true);
                            device.setState(state);
                            device.setBrewTime(brewTime);
                            device.setBrewTemp(brewTemp);
                            device.setError(error);
                            device.setLed(led);
                            device.setControl3(control3);
                            device.setWater(water);
                            device.setCrossR(crossR);
                            device.setCrossG(crossG);
                            device.setCrossB(crossB);
                            device.setOutR(outR);
                            device.setOutG(outG);
                            device.setOutB(outB);
                            device.setInR(inR);
                            device.setInG(inG);
                            device.setInB(inB);
                            device.setOiR(oiR);
                            device.setOiG(oiG);
                            device.setOiB(oiB);

                            device.setBrewBl(water);
                            device.setBrewCount(brewCount);
                            device.setLight(light);
                            device.setFurnace(furnace);
                            deviceMap.put(deviceMac,device);
                            Intent intent=new Intent("MainActivityTest");
                            intent.putExtra("deviceMac",deviceMac);
                            intent.putExtra("device",device);
                            sendBroadcast(intent);
                            if (state==0xb7){
                                code=1;
                            }
                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            if (code==1){
                ToastUtil.showShort(MQService.this,R.string.updating);
            }
        }
    }

    private boolean isJsonData(String message){
        boolean flag=false;
        try {
            JSONObject jsonObject=new JSONObject(message);
            jsonObject=null;
            flag=true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return flag;
    }
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    /**
     * 重新连接mqtt，即重连机制
     */
    private void startReconnect() {

        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                if (!client.isConnected()) {
                    connect(1);
                }
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

//    public void insert(DeviceChild deviceChild) {
//        deviceChildDao.insert(deviceChild);
//    }


    /**
     * 发送主题
     *
     * @param topicName 主题名称
     * @param qos       消息发送次数，0为最多发一次，1为至少发送一次，2为只发一次
     * @param payload   发送的内容
     * @return
     */
    public boolean publish(String topicName, int qos, String payload) {
        boolean flag = false;
        try {
            if (client != null && !client.isConnected()) {
                client.connect(options);
                String ss[]=topicName.split("/");
                String deviceMac=ss[1];
                String topicName1 = "tea/" + deviceMac + "/status/transfer";
                String topicName2 = "tea/" + deviceMac + "/operate/transfer";
                String topicName3 = "tea/" + deviceMac + "/extra/transfer";
                String topicName4 = "tea/" + deviceMac + "/reset/transfer";
                String topicName5 = "tea/" + deviceMac + "/lwt";
                subscribe(topicName1,1);
                subscribe(topicName2,1);
                subscribe(topicName3,1);
                subscribe(topicName4,1);
                subscribe(topicName5,1);
            }
            if (client != null && client.isConnected()) {

                MqttMessage message = new MqttMessage(payload.getBytes("utf-8"));
                qos = 1;
                message.setQos(qos);
                client.publish(topicName, message);
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 订阅所有主题
     *
     * @param topicName
     * @param qos
     * @return
     */
    public boolean subscribe(String topicName, int qos) {
        boolean flag = false;
        try {
            if (client != null && !client.isConnected()) {
                client.connect(options);
            }
            if (client != null && client.isConnected()) {

                client.subscribe(topicName, qos);
                flag = true;
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public String getResult() {
        return result;
    }

    SharedPreferences preferences;

    Map<String, Device> deviceMap = new HashMap<>();

    public Map<String, Device> getDeviceMap() {
        return deviceMap;
    }

    public Device getDevice(String deviceMac){
        Device device=deviceMap.get(deviceMac);
        if (device!=null){
            device.setOnline(false);
        }
        return device;
    }

    public void setDevice(Device device){
        if (device!=null){
            String deviceMac=device.getDeviceMac();
            deviceMap.put(deviceMac,device);
        }
    }
    public void setDevice(String deviceMac, Device device) {
        deviceMap.put(deviceMac, device);
        String topicName = "tea/" + deviceMac + "/status/transfer";
        String topicName2 = "tea/" + deviceMac + "/operate/transfer";
        String topicName3 = "tea/" + deviceMac + "/extra/transfer";
        String topicName4 = "tea/" + deviceMac + "/reset/transfer";
        String topicName5 = "tea/" + deviceMac + "/lwt";
        topicNames.add(topicName);
        topicNames.add(topicName2);
        topicNames.add(topicName3);
        topicNames.add(topicName4);
        topicNames.add(topicName5);
        subscribe(topicName, 1);
        subscribe(topicName2, 1);
        subscribe(topicName3, 1);
        subscribe(topicName4, 1);
        subscribe(topicName5,1);
    }

    /**
     * 获取所有主题
     *
     * @return
     */
    List<String> topicNames = new ArrayList<>();

    public List<String> getTopicNames() {
        topicNames.clear();
        for (Map.Entry<String, Device> entry : deviceMap.entrySet()) {
            String deviceMac = entry.getKey();
            String topicName = "tea/" + deviceMac + "/status/transfer";
            String topicName2 = "tea/" + deviceMac + "/operate/transfer";
            String topicName3 = "tea/" + deviceMac + "/extra/transfer";
            String topicName4 = "tea/" + deviceMac + "/reset/transfer";
            String topicName5 = "tea/" + deviceMac + "/lwt";
            topicNames.add(topicName);
            topicNames.add(topicName2);
            topicNames.add(topicName3);
            topicNames.add(topicName4);
            topicNames.add(topicName5);
        }
        return topicNames;
    }


    /**
     * 取消订阅主题
     *
     * @param topicName
     */
    public void unsubscribe(String topicName) {
        if (client != null && client.isConnected()) {
            try {
                client.unsubscribe(topicName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getName() {
        return "ssss";
    }

    public void getData(String topicName) {
        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            jsonArray.put(0,0x32);
            jsonArray.put(1, 0xa1);
            jsonArray.put(2, 0);
            int sum = 0;
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                sum += jsonArray.getInt(i);
            }
            jsonArray.put(3, sum % 256);
            jsonObject.put("Coffee", jsonArray.toString());
            String payLoad = jsonObject.toString();
            boolean success = publish(topicName, 1, payLoad);
            if (!success)
                success = publish(topicName, 1, payLoad);
            Log.i(TAG, "-->" + success + "," + payLoad);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int send(String topicName, Device device, int funCode) {
        boolean online=device.isOnline();
        if (!online){
            String ss[]=topicName.split("/");
            String deviceMac=ss[1];
            String topicName2 = "tea/" + deviceMac + "/status/set";
            getData(topicName2);
            Toast.makeText(getApplicationContext(),R.string.device_offline,Toast.LENGTH_SHORT).show();
            return 1;
        }else {
            if (device.getState()==0xb7 || device.getState()==0xb6){
                Toast.makeText(getApplicationContext(), R.string.updating,Toast.LENGTH_SHORT).show();
                return 3;
            }
        }
        boolean success=false;
        int control = device.getControl();//控制命令 0xc0休眠 0xc1预热 0xc2冲泡
        int control2 = device.getControl2();//控制命令2 0xf0
        int control3 = device.getControl3();//控制命令3 bit7 1：led显示开 0关
        int crossR = device.getCrossR();//十字灯 R
        int crossG = device.getCrossG();//十字灯 G
        int crossB = device.getCrossB();//十字灯 B
        int outR = device.getOutR();//外圈灯光 R
        int outG = device.getOutG();//外圈灯光 G
        int outB = device.getOutB();//外圈灯光 B
        int inR = device.getInR();//内圈灯光 R
        int inG = device.getInG();//内圈灯光 G
        int inB = device.getInB();//内圈灯光 B
        int oiR=device.getOiR();//大小灯光R
        int oiG=device.getOiG();//大小灯光G
        int oiB=device.getOiB();//大小灯光B
        double water = device.getWater();//制作水量
        int waterHigh = (int) (water / 256);
        int waterLow = (int) (water % 256);
        int preTemp = device.getPreTemp();//预热温度
        double brewTemp = device.getBrewTemp();//浸泡温度
        int brewTime = device.getBrewTime();//浸泡时间
        int brewPreStartTime = device.getBrewPreStartTime();//浸泡前水泵启动时间
        int bigFlow = device.getBigFlow();//大杯流量
        int bigFlowHigh = bigFlow / 256;
        int bigFlowLow = bigFlow % 256;
        int smallFlow = device.getSmallFlow();//小杯流量
        int smallFlowHigh = smallFlow / 256;
        int smallFlowLow = smallFlow % 256;
        int clearCount = device.getClearCount();//清洗次数
        int brewCount=device.getBrewCount();
        int light=device.getLight();
        try {
            int len = 31;
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(0, 0x32);
            jsonArray.put(1, funCode);
            jsonArray.put(2, len);
            jsonArray.put(3, control);
            jsonArray.put(4, 0x0f);
            jsonArray.put(5, control3);
            jsonArray.put(6, crossR);
            jsonArray.put(7, crossG);
            jsonArray.put(8, crossB);
            jsonArray.put(9, outR);
            jsonArray.put(10, outG);
            jsonArray.put(11, outB);
            jsonArray.put(12, inR);
            jsonArray.put(13, inG);
            jsonArray.put(14, inB);
            jsonArray.put(15, waterHigh);
            jsonArray.put(16, waterLow);
            jsonArray.put(17, preTemp);
            jsonArray.put(18, brewTemp);
            jsonArray.put(19, brewTime);
            jsonArray.put(20, brewPreStartTime);
            jsonArray.put(21, clearCount);
            jsonArray.put(22, brewCount);
            jsonArray.put(23, light);
            jsonArray.put(24, oiR);
            jsonArray.put(25, oiG);
            jsonArray.put(26, oiB);
            jsonArray.put(27, 0);
            jsonArray.put(28, 0);
            jsonArray.put(29, 0);
            jsonArray.put(30,0);
            jsonArray.put(31,0);
            jsonArray.put(32,0);
            jsonArray.put(33,0);
            int sum = 0;
            for (int i = 0; i < 34; i++) {
                sum += jsonArray.getInt(i);
            }
            jsonArray.put(34, sum % 256);
            jsonObject.put("Coffee", jsonArray.toString());
            String payLoad = jsonObject.toString();
            success = publish(topicName, 1, payLoad);
            if (!success)
                success = publish(topicName, 1, payLoad);

            Log.i(TAG, "-->" + success + "," + payLoad);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success?1:0;
    }
}