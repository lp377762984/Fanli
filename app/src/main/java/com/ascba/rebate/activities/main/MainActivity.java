package com.ascba.rebate.activities.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Toast;
import com.ascba.rebate.activities.base.BaseNetWorkActivity;
import com.ascba.rebate.appconfig.AppConfig;
import com.ascba.rebate.fragments.CartFragment;
import com.ascba.rebate.fragments.ShopMeFragment;
import com.ascba.rebate.fragments.TypeFragment;
import com.ascba.rebate.handlers.DialogManager2;
import com.ascba.rebate.utils.ExampleUtil;
import com.ascba.rebate.utils.LogUtils;
import com.ascba.rebate.view.AppTabs;
import com.ascba.rebate.R;
import com.ascba.rebate.fragments.main.FirstFragment;
import com.ascba.rebate.fragments.me.FourthFragment;
import com.ascba.rebate.fragments.message.SecondFragment;
import com.ascba.rebate.fragments.shop.ThirdFragment;
import com.jaeger.library.StatusBarUtil;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

/**
 * 主界面
 */
public class MainActivity extends BaseNetWorkActivity implements AppTabs.Callback {
    private static final int MSG_SET_ALIAS = 1001;
    private static final int MSG_SET_TAGS = 1002;
    private List<Fragment> fgts=new ArrayList<>();
    private DialogManager2 dm;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SET_ALIAS:
                    JPushInterface.setAliasAndTags(getApplicationContext(), (String) msg.obj, null, mAliasCallback);
                    break;
                case MSG_SET_TAGS:
                    JPushInterface.setAliasAndTags(getApplicationContext(), null, (Set<String>) msg.obj, mTagsCallback);
                    break;
                default:
                    break;
            }
        }
    };

    public DialogManager2 getDm() {
        return dm;
    }

    public void setDm(DialogManager2 dm) {
        this.dm = dm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarUtil.setColor(this, getResources().getColor(R.color.moneyBarColor));
        findViews();
    }

    private void findViews() {
        dm=new DialogManager2(this);
        initFragments();
        AppTabs appTabs = ((AppTabs) findViewById(R.id.tabs));
        appTabs.setCallback(this);
        init();//设置极光推送用户标识
    }

    private void initFragments() {
        Fragment mFirstFragment = new FirstFragment();
        Fragment mTypeFragment = new TypeFragment();
        Fragment mShopFragment = new ThirdFragment();
        Fragment mCartFragment = new CartFragment();
        Fragment mSettingFragment = new ShopMeFragment();
        Fragment mSecondFragment = new SecondFragment();
        Fragment mFourFragment = new FourthFragment();

        fgts.add(mFirstFragment);
        fgts.add(mTypeFragment);
        fgts.add(mShopFragment);
        fgts.add(mCartFragment);
        fgts.add(mSettingFragment);
        fgts.add(mSecondFragment);
        fgts.add(mFourFragment);


        addAllFrgsToContai();
    }

    private void addAllFrgsToContai() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        for (int i = 0; i < fgts.size(); i++) {
            ft.add(R.id.fl_change,fgts.get(i));
        }
        ft.commit();
        selFrgByPos(2,0);
    }

    private void init() {
        int uuid = AppConfig.getInstance().getInt("uuid", -1000);
        if (uuid != -1000) {
            setAlias(uuid + "");
            boolean appDebug = LogUtils.isAppDebug(this);
            setTag(appDebug);
            if(appDebug){
                LogUtils.PrintLog("123","debug");
            }else {
                LogUtils.PrintLog("123","release");
            }

        }
    }
    //调用JPush API设置Tag
    private void setTag(boolean appDebug) {
        Set<String> tagSet = new LinkedHashSet<String>();
        if(appDebug){
            tagSet.add("debug");
        }else {
            tagSet.add("release");
        }
       /* tagSet.add(getPackageVersionCode()+"");//把版本号传给服务器*/
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_TAGS, tagSet));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setAlias(String alias) {
        //调用JPush API设置Alias
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS, alias));
    }
    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {
        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            switch (code) {
                case 0://成功
                    LogUtils.PrintLog("123","alias设置成功");
                    break;
                case 6002://失败，重试
                    if (ExampleUtil.isConnected(getApplicationContext())) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
                    } else {
                        Toast.makeText(MainActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
            }
        }
    };
    private final TagAliasCallback mTagsCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            switch (code) {
                case 0:
                    LogUtils.PrintLog("123","tag设置成功:");
                    break;

                case 6002:

                    if (ExampleUtil.isConnected(getApplicationContext())) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_TAGS, tags), 1000 * 60);
                    } else {
                        Toast.makeText(MainActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }
                    break;

                default:
            }
        }

    };
    //首页
    @Override
    public void clickZero(View v, int type) {
        selFrgByPos(0,type);
    }
    //分类--消息
    @Override
    public void clickOne(View v, int type) {
        selFrgByPos(1,type);
    }
    //商城
    @Override
    public void clickTwo(View v, int type) {
        selFrgByPos(2,type);
    }
    //购物车
    @Override
    public void clickThree(View v, int type) {
        selFrgByPos(3,type);
    }
    //设置--我
    @Override
    public void clickFour(View v, int type) {
        selFrgByPos(4,type);
    }
    //根据位置切换相应碎片
    public void selFrgByPos(int position,int type){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        switch (position){
            case 0:
                for (int i = 0; i < fgts.size(); i++) {
                    Fragment fragment = fgts.get(i);
                    if(fragment instanceof FirstFragment){
                        ft.show(fragment);
                    }else {
                        ft.hide(fragment);
                    }
                }
                ft.commit();
                break;
            case 1:
                if(type==0){
                    for (int i = 0; i < fgts.size(); i++) {
                        Fragment fragment = fgts.get(i);
                        if(fragment instanceof TypeFragment){
                            ft.show(fragment);
                        }else {
                            ft.hide(fragment);
                        }
                    }
                }else {
                    for (int i = 0; i < fgts.size(); i++) {
                        Fragment fragment = fgts.get(i);
                        if(fragment instanceof SecondFragment){
                            ft.show(fragment);
                        }else {
                            ft.hide(fragment);
                        }
                    }
                }
                ft.commit();
                break;
            case 2:
                for (int i = 0; i < fgts.size(); i++) {
                    Fragment fragment = fgts.get(i);
                    if(fragment instanceof ThirdFragment){
                        ft.show(fragment);
                    }else {
                        ft.hide(fragment);
                    }
                }
                ft.commit();
                break;
            case 3:
                for (int i = 0; i < fgts.size(); i++) {
                    Fragment fragment = fgts.get(i);
                    if(fragment instanceof CartFragment){
                        ft.show(fragment);
                    }else {
                        ft.hide(fragment);
                    }
                }
                ft.commit();
                break;
            case 4:
                if(type==0){
                    for (int i = 0; i < fgts.size(); i++) {
                        Fragment fragment = fgts.get(i);
                        if(fragment instanceof ShopMeFragment){
                            ft.show(fragment);
                        }else {
                            ft.hide(fragment);
                        }
                    }
                }else {
                    for (int i = 0; i < fgts.size(); i++) {
                        Fragment fragment = fgts.get(i);
                        if(fragment instanceof FourthFragment){
                            ft.show(fragment);
                        }else {
                            ft.hide(fragment);
                        }
                    }
                }
                ft.commit();
                break;

        }
    }

}
