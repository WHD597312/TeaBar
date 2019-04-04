package teabar.ph.com.teabar.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.zlpolygonview.ZLPolygonView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import me.jessyan.autosize.utils.ScreenUtils;
import teabar.ph.com.teabar.R;
import teabar.ph.com.teabar.adpter.FriendListAdapter;
import teabar.ph.com.teabar.base.BaseActivity;
import teabar.ph.com.teabar.base.MyApplication;


public class FriendListActivity extends BaseActivity {

    @BindView(R.id.tv_main_1)
    TextView tv_main_1;
    @BindView(R.id.iv_power_fh)
    ImageView iv_power_fh;
    @BindView(R.id.iv_power_add)
   ImageView iv_power_add;
    @BindView(R.id.rv_friend_inform)
    RecyclerView rv_friend_inform;
    MyApplication application;
    FriendListAdapter friendListAdapter ;
    List<String> list =new ArrayList<>() ;
    @Override
    public void initParms(Bundle parms) {

    }

    @Override
    public int bindLayout() {
        setSteepStatusBar(true);
        return R.layout.activity_friendlist;
    }

    @Override
    public void initView(View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                ScreenUtils.getStatusBarHeight());
        tv_main_1.setLayoutParams(params);

        if (application == null) {
            application = (MyApplication) getApplication();
        }
        application.addActivity(this);
        for (int i=0;i<5;i++){
            list.add(i+"");
        }
        friendListAdapter = new FriendListAdapter(this,list);
        rv_friend_inform.setLayoutManager(new LinearLayoutManager(this));
        rv_friend_inform.setAdapter(friendListAdapter);

    }

    @Override
    public void doBusiness(Context mContext) {

    }

    @Override
    public void widgetClick(View v) {

    }

    @OnClick({ R.id.iv_power_fh,R.id.iv_power_add})
    public void onClick(View view){
        switch (view.getId()){

            case R.id.iv_power_fh:
                finish();
                break;

            case R.id.iv_power_add:
                startActivity(AddFriendActivity.class);
                break;

        }
    }
}
