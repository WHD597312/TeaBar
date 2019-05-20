package teabar.ph.com.teabar.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.OnClick;
import teabar.ph.com.teabar.R;
import teabar.ph.com.teabar.activity.login.LoginActivity;
import teabar.ph.com.teabar.base.BaseActivity;
import teabar.ph.com.teabar.base.MyApplication;
import teabar.ph.com.teabar.util.HttpUtils;

public class EncourageActivity extends BaseActivity {
    @BindView(R.id.tv_encourage)
    TextView tv_encourage;
    @BindView(R.id.encourage_tv_day)
    TextView encourage_tv_day;
    @BindView(R.id.encourage_tv_year)
    TextView encourage_tv_year;
    MyApplication application;
    int language;
    @Override
    public void initParms(Bundle parms) {

    }

    @Override
    public int bindLayout() {
        setSteepStatusBar(true);
        return R.layout.activity_encourage;
    }

    @Override
    public void initView(View view) {
        if (application == null) {
            application = (MyApplication) getApplication();
        }
        application.addActivity(this);
        language = application.IsEnglish();
        new GetEncourageAsyncTask().execute();
        Calendar c = Calendar.getInstance();
        int  year = c.get(Calendar.YEAR);
        String month = c.get(Calendar.MONTH)+1 <10 ? "0"+(c.get(Calendar.MONTH)+1):(c.get(Calendar.MONTH)+1)+"" ;
        String day = c.get(Calendar.DATE)<10?"0"+c.get(Calendar.DATE):c.get(Calendar.DATE)+"";
        encourage_tv_year.setText(month);
        encourage_tv_day.setText(day);
    }

    @Override
    public void doBusiness(Context mContext) {

    }

    @Override
    public void widgetClick(View v) {

    }
    @OnClick({R.id.encourage_bt_in})
            public void  onClick(View view){
        switch (view.getId()){
            case R.id.encourage_bt_in:
                startActivity(LoginActivity.class);
                break;

        }
    }

    String  encouraging ,message1;
    class GetEncourageAsyncTask extends AsyncTask<Void ,Void ,String>{

        @Override
        protected String doInBackground(Void... voids) {
            String code ="";
            String result = HttpUtils.getOkHpptRequest(HttpUtils.ipAddress+"/app/encouraging?type="+language);
            if (!TextUtils.isEmpty(result)){
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    code = jsonObject.getString("state");
                    message1 = jsonObject .getString("message1");
                    JSONObject data = jsonObject.getJSONObject("data");
                   encouraging  = data.getString("encouraging");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return code;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            switch (s){
                case "200":
                    tv_encourage.setText(encouraging);
                    break;
                    default:

                        break;
            }
        }
    }
}
