package teabar.ph.com.teabar.activity.my;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jmessage.support.google.gson.Gson;
import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import me.jessyan.autosize.utils.ScreenUtils;
import teabar.ph.com.teabar.R;
import teabar.ph.com.teabar.adpter.NearestAdpter;
import teabar.ph.com.teabar.adpter.SocialAdapter;
import teabar.ph.com.teabar.base.BaseActivity;
import teabar.ph.com.teabar.base.MyApplication;
import teabar.ph.com.teabar.pojo.Tea;
import teabar.ph.com.teabar.util.HttpUtils;
import teabar.ph.com.teabar.util.ToastUtil;


public class NearestActivity extends BaseActivity {

    @BindView(R.id.tv_main_1)
    TextView tv_main_1;
    @BindView(R.id.iv_power_fh)
    ImageView iv_power_fh;
    @BindView(R.id.rv_nearest)
    RecyclerView rv_nearest;
    MyApplication application;
    SharedPreferences preferences;
    QMUITipDialog tipDialog;
    List<Tea> list = new ArrayList<>();
    NearestAdpter nearestAdpter ;
    long userId;
    @Override
    public void initParms(Bundle parms) {

    }

    @Override
    public int bindLayout() {
        setSteepStatusBar(true);
        return R.layout.activity_nearest;
    }

    @Override
    public void initView(View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                ScreenUtils.getStatusBarHeight());
        tv_main_1.setLayoutParams(params);

        if (application == null) {
            application = (MyApplication) getApplication();
        }
        application.addActivity(this);
        preferences = getSharedPreferences("my",MODE_PRIVATE);
        userId = preferences.getLong("userId",0);
        nearestAdpter = new NearestAdpter(this,list);
        rv_nearest.setLayoutManager(new LinearLayoutManager(this));
        rv_nearest.setAdapter(nearestAdpter);
        new getNeastTeaAsynctask().execute();
    }

    String returnMsg1,returnMsg2;
    class getNeastTeaAsynctask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... voids) {
            String code = "";
            String result =   HttpUtils.getOkHpptRequest(HttpUtils.ipAddress+"/app/getLatelyTea?userId="+userId );
            Log.e("back", "--->" + result);
            if (!ToastUtil.isEmpty(result)) {
                if (!"4000".equals(result)){
                    try {

                        JSONObject jsonObject = new JSONObject(result);
                        code = jsonObject.getString("state");
                        returnMsg1=jsonObject.getString("message1");
                        if ("200".equals(code)) {
                            list.clear();
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            Gson gson = new Gson();
                            for (int i =0;i<jsonArray.length();i++){
                                Tea tea = gson.fromJson(jsonArray.get(i).toString(),Tea.class);
                                list.add(tea);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }else {
                    code="4000";
                }
            }
            return code;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            switch (s) {

                case "200":
                    nearestAdpter.setmData(list);

                    break;
                case "4000":

                    toast(  "连接超时，请重试");

                    break;
                default:

                    toast( returnMsg1);
                    break;

            }
        }
    }


    @Override
    public void doBusiness(Context mContext) {

    }
    //显示dialog
    public void showProgressDialog() {

        tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("请稍后...")
                .create();
        tipDialog.show();
    }

    @Override
    public void widgetClick(View v) {

    }

    @OnClick({ R.id.iv_power_fh})
    public void onClick(View view){
        switch (view.getId()){

            case R.id.iv_power_fh:
                finish();
                break;


        }
    }

}