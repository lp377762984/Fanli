package com.ascba.rebate.activities.base;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.widget.Toast;

import com.ascba.rebate.R;
import com.ascba.rebate.activities.main.MainActivity;
import com.ascba.rebate.application.MyApplication;
import com.ascba.rebate.view.loadmore.CustomLoadMoreView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.taobao.sophix.PatchStatus;
import com.taobao.sophix.SophixManager;

import static com.chad.library.adapter.base.loadmore.LoadMoreView.STATUS_DEFAULT;


public class BaseActivity1 extends BaseDefaultUIActivity {

    protected String TAG=getClass().getSimpleName();
    protected BaseActivity.PermissionCallback requestPermissionAndBack;
    protected SwipeRefreshLayout refreshLayout;
    protected CustomLoadMoreView loadMoreView;
    protected static final int LOAD_MORE_END = 2017;
    protected static final int LOAD_MORE_ERROR = 2018;
    protected BaseQuickAdapter baseAdapter;
    protected LoadRequestor loadRequestor;//加载器，管理上拉加载，下拉刷新
    protected int now_page = 1;//当前页数 用于分页
    protected int total_page = 0;//总页数
    protected boolean isRefreshing = true;//true 下拉刷新 false 加载更多
    @SuppressLint("HandlerLeak")
    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_MORE_END:
                    if (baseAdapter != null) {
                        baseAdapter.loadMoreEnd(false);
                    }

                    break;
                case LOAD_MORE_ERROR:
                    if (baseAdapter != null) {
                        baseAdapter.loadMoreFail();
                    }
                    break;
            }
        }
    };

    public interface LoadRequestor{
        void loadMore();
        void pullToRefresh();
    }

    //初始化下拉刷新
    protected void initRefreshLayout() {
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        //改变加载显示的颜色
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetPage();
                if(loadRequestor!=null){
                    loadRequestor.pullToRefresh();
                }


            }
        });
    }

    //初始化上拉加载
    protected void initLoadMoreRequest() {
        if (loadMoreView == null) {
            loadMoreView = new CustomLoadMoreView();
            baseAdapter.setLoadMoreView(loadMoreView);
        }
        baseAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                isRefreshing = false;
                if (now_page > total_page && total_page != 0) {
                    handler.sendEmptyMessage(LOAD_MORE_END);
                } else if (total_page == 0) {
                    handler.sendEmptyMessage(LOAD_MORE_END);
                } else {
                    if(loadRequestor!=null){
                        loadRequestor.loadMore();
                    }
                }
            }
        });
    }

    //停止下拉刷新rere
    protected void stopRefresh() {
        if (refreshLayout!=null && refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(false);
        }
    }

    //停止上拉加载
    protected void stopLoadMore() {
        if (baseAdapter != null) {
            baseAdapter.loadMoreComplete();
        }
        if (loadMoreView != null) {
            loadMoreView.setLoadMoreStatus(STATUS_DEFAULT);
        }
    }
    //重置页数
    protected void resetPage(){
        isRefreshing = true;
        now_page=1;
        total_page=0;
    }

    public interface PermissionCallback {
        void requestPermissionAndBack(boolean isOk);
    }

    /**
     * 申请权限
     */
    protected void checkAndRequestAllPermission(String[] permissions, BaseActivity.PermissionCallback requestPermissionAndBack) {
        this.requestPermissionAndBack = requestPermissionAndBack;
        if (permissions == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(permissions, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] per,
                                           @NonNull int[] grantResults) {
        boolean isAll = true;
        for (int i = 0; i < per.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isAll = false;
                break;
            }
        }
        if (!isAll) {
            showToast(getResources().getString(R.string.no_permission));
        }
        if (requestPermissionAndBack != null) {
            requestPermissionAndBack.requestPermissionAndBack(isAll);//isAll 用户是否拥有所有权限
        }
        super.onRequestPermissionsResult(requestCode, per, grantResults);
    }

    protected void showToast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    protected void showToast(int content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mHomeKeyEventReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        /*if(MyApplication.patchStatusCode== PatchStatus.CODE_LOAD_RELAUNCH){
            if(this instanceof MainActivity){
                killProcess(false);
            }
        }*/
        unregisterReceiver(mHomeKeyEventReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
        String SYSTEM_REASON = "reason";
        String SYSTEM_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            if(MyApplication.patchStatusCode== PatchStatus.CODE_LOAD_RELAUNCH){
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                    String reason = intent.getStringExtra(SYSTEM_REASON);
                    if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
                        killProcess(true);
                    }
                }else if(action.equals(Intent.ACTION_SCREEN_OFF)){
                    killProcess(true);
                }
            }
        }
    };
    //杀进程，用于加载补丁
    private void killProcess(boolean needReboot){
        SophixManager.getInstance().killProcessSafely();
        if(needReboot){
            Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage( getBaseContext().getPackageName() );
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }
}
