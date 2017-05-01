package com.ascba.rebate.fragments.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;

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

/**
 * Created by 李鹏 on 2017/04/20 0020.
 */

public abstract class BaseFragmentNet extends BaseFragment {

    protected DialogHome dialogManager;
    protected NetResponseListener netResponseListener = new NetResponseListener();
    protected ProgressDialog dialogProgress;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialogManager = new DialogHome(getActivity());
    }

    //创建进度对话框
    public void buildWaitDialog(String message) {
        if (dialogProgress == null) {
            dialogProgress = new ProgressDialog(getActivity(), R.style.dialog);
            dialogProgress.setCanceledOnTouchOutside(false);//不可点击，返回键可以取消
            dialogProgress.setCancelable(true);//返还键可取消
        }
        if (dialogProgress.isShowing()) {
            dialogProgress.dismiss();
        }
        dialogProgress.setMessage(message);
        dialogProgress.show();
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
     *
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
        boolean netAva = NetUtils.isNetworkAvailable(getActivity());
        isNetWork(netAva);
        if (!netAva) {
            dialogManager.buildAlertDialog(getResources().getString(R.string.no_network));
            return;
        }
        MyApplication.getRequestQueue().add(what, jsonRequest, netResponseListener);
        buildWaitDialog(message);
    }

    //执行网络请求
    protected void executeNetWork(Request<JSONObject> jsonRequest, String message) {

        boolean netAva = NetUtils.isNetworkAvailable(getActivity());
        isNetWork(netAva);
        if (!netAva) {
            dialogManager.buildAlertDialog(getResources().getString(R.string.no_network));
            return;
        }
        MyApplication.getRequestQueue().add(1, jsonRequest, netResponseListener);
        buildWaitDialog(message);
    }


    private class NetResponseListener implements OnResponseListener<JSONObject> {
        @Override
        public void onStart(int what) {

        }

        @Override
        public void onSucceed(int what, Response<JSONObject> response) {
            if (dialogProgress != null && dialogProgress.isShowing()) {
                dialogProgress.dismiss();
            }
            dialogManager.dismissDialog();
            requstSuccess(what, response.get());
        }

        @Override
        public void onFailed(int what, Response<JSONObject> response) {
            if (dialogProgress != null && dialogProgress.isShowing()) {
                dialogProgress.dismiss();
            }
            dialogManager.buildAlertDialog(getString(R.string.no_response));
            requstFailed(what, response.getException());
        }

        @Override
        public void onFinish(int what) {
            if (dialogProgress != null && dialogProgress.isShowing()) {
                dialogProgress.dismiss();
            }
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
     * 取消执行网络请求
     */
    protected void cancelNetWork() {
        MyApplication.getRequestQueue().cancelAll();
    }

    /**
     * 是否启用缓存
     */
    protected boolean hasCache() {
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dialogManager.dismissDialog();
        //cancelNetWork();
        if (dialogProgress != null && dialogProgress.isShowing()) {
            dialogProgress.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        dialogManager.dismissDialog();
        if (dialogProgress != null && dialogProgress.isShowing()) {
            dialogProgress.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dialogProgress != null && dialogProgress.isShowing()) {
            dialogProgress.dismiss();
        }
    }
}