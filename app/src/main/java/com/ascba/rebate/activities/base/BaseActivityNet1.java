package com.ascba.rebate.activities.base;

import android.app.Dialog;
import android.os.Bundle;

import com.ascba.rebate.R;
import com.ascba.rebate.appconfig.AppConfig;
import com.ascba.rebate.application.MyApplication;
import com.ascba.rebate.utils.DialogHome;
import com.ascba.rebate.utils.NetUtils;
import com.ascba.rebate.utils.UrlEncodeUtils;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.CacheMode;
import com.yanzhenjie.nohttp.rest.OnResponseListener;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.Response;

import org.json.JSONObject;

public abstract class BaseActivityNet1 extends BaseActivity1 {

    protected DialogHome dialogManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setTheme(R.style.AppTheme);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);*/
        dialogManager = new DialogHome(this);
    }

    //数据请求成功
    protected abstract void requstSuccess(int what, JSONObject object);

    //请求服务器失败
    protected abstract void requstFailed(int what, Exception e);

    //请求完成
    protected abstract void requstFinish(int what);

    protected abstract void isNetWork(boolean isNetWork);

    /**
     * 建立网络请求
     * @param url          请求网址
     * @param method       请求方式 0 post 1 get
     * @param defaultParam 是否有默认请求参数
     */
    protected Request<JSONObject> buildNetRequest(String url, int method, boolean defaultParam) {
        Request<JSONObject> jsonRequest = NoHttp.createJsonObjectRequest(url, method == 0 ? RequestMethod.POST : RequestMethod.GET);

        if (hasCache()) {
            jsonRequest.setCacheMode(CacheMode.REQUEST_NETWORK_FAILED_READ_CACHE);
        }

        int uuid = AppConfig.getInstance().getInt("uuid", -1000);
        if (defaultParam || uuid != -1000) {
            String token = AppConfig.getInstance().getString("token", "");
            long expiring_time = AppConfig.getInstance().getLong("expiring_time", -2000);
            jsonRequest.add("sign", UrlEncodeUtils.createSign(url));
            jsonRequest.add("uuid", uuid);
            jsonRequest.add("token", token);
            jsonRequest.add("expiring_time", expiring_time);
        } else {
            jsonRequest.add("sign", UrlEncodeUtils.createSign(url));
        }
        return jsonRequest;
    }

    //执行网络请求
    protected void executeNetWork(int what, Request<JSONObject> jsonRequest, String message) {
        boolean netAva = NetUtils.isNetworkAvailable(this);
        isNetWork(netAva);
        if (!netAva) {
            dialogManager.buildAlertDialog(getResources().getString(R.string.no_network));
            return;
        }
        MyApplication.getRequestQueue().add(what, jsonRequest, new NetResponseListener(message));
    }

    //执行网络请求
    protected void executeNetWork(Request<JSONObject> jsonRequest, String message) {
        boolean netAva = NetUtils.isNetworkAvailable(this);
        isNetWork(netAva);
        if (!netAva) {
            dialogManager.buildAlertDialog(getResources().getString(R.string.no_network));
            return;
        }
        MyApplication.getRequestQueue().add(0, jsonRequest, new NetResponseListener(message));
    }


    private class NetResponseListener implements OnResponseListener<JSONObject> {
        private String message;
        NetResponseListener(String message) {
            this.message=message;
        }

        private Dialog dialog;

        @Override
        public void onStart(int what) {
            //dialog = dialogManager.buildWaitDialog(message);
        }

        @Override
        public void onSucceed(int what, Response<JSONObject> response) {
            requstSuccess(what, response.get());

        }

        @Override
        public void onFailed(int what, Response<JSONObject> response) {
            showToast(getString(R.string.no_response));
            handler.sendEmptyMessage(LOAD_MORE_ERROR);
            requstFailed(what, response.getException());
        }

        @Override
        public void onFinish(int what) {
            stopRefresh();
            stopLoadMore();
            //dialog.dismiss();
            requstFinish(what);
        }
    }

    /**
     * 获取DialogManager
     */
    public DialogHome getDm() {
        return dialogManager;
    }

    /**
     * 是否启用缓存
     */
    protected boolean hasCache() {
        return false;
    }
}
