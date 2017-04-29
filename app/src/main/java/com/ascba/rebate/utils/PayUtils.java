package com.ascba.rebate.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.ascba.rebate.R;
import com.ascba.rebate.activities.login.LoginActivity;
import com.ascba.rebate.adapter.PayTypeAdapter;
import com.ascba.rebate.appconfig.AppConfig;
import com.ascba.rebate.application.MyApplication;
import com.ascba.rebate.beans.PayType;
import com.ascba.rebate.handlers.OnPasswordInput;
import com.ascba.rebate.view.PayPopWindow;
import com.ascba.rebate.view.pay.PayResult;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.OnResponseListener;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ascba.rebate.activities.base.BaseNetActivity.REQUEST_LOGIN;

/**
 * Created by 李鹏 on 2017/04/25 0025.
 */

public class PayUtils {

    private Dialog dialog;
    private Activity context;
    private String price;//价格
    private DialogHome dialogHome;
    private onPayCallBack payCallBack;
    private String payType = "balance";//默认值
    private PayPopWindow popWindow;
    private String balance;//账户余额

    private static final int SDK_PAY_FLAG = 1;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    //对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {

                        if (payCallBack != null) {
                            payCallBack.onSuccess(payType, resultStatus);
                        }
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        try {
                            JSONObject jObj = new JSONObject(resultInfo);
                            JSONObject trObj = jObj.optJSONObject("alipay_trade_app_pay_response");
                            String total_amount = trObj.optString("total_amount");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (TextUtils.equals(resultStatus, "6002")) {
                        if (payCallBack != null) {
                            payCallBack.onNetProblem(payType, resultStatus);
                        }
                    } else if (TextUtils.equals(resultStatus, "6001")) {
                        if (payCallBack != null) {
                            payCallBack.onCancel(payType, resultStatus);
                        }
                    } else {
                        if (payCallBack != null) {
                            payCallBack.onFailed(payType, resultStatus);
                        }
                    }
                    if (payCallBack != null) {
                        payCallBack.onFinish(payType);
                    }
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    break;
                }
            }
        }

    };

    public PayUtils(Activity activity, String price, String balance) {
        this.context = activity;
        this.price = price;
        this.balance = balance;
        dialogHome = new DialogHome(context);
    }

    public interface OnCreatOrder {
        void onCreatOrder(String payType);
    }

    public void setPayCallBack(onPayCallBack payCallBack) {
        this.payCallBack = payCallBack;
    }

    public static abstract class onPayCallBack {
        public void onSuccess(String payStype, String resultStatus) {
        }

        public void onFailed(String payStype, String resultStatus) {
        }

        public abstract void onFinish(String payStype);

        public void onCancel(String payStype, String resultStatus) {
        }

        public void onNetProblem(String payStype, String resultStatus) {
        }

    }


    //选择支付方式页面
    public void showDialog(final OnCreatOrder onCreatOrder) {
        dialog = new Dialog(context, R.style.AlertDialog);
        dialog.setContentView(R.layout.layout_pay_pop);
        ((TextView) dialog.findViewById(R.id.dlg_tv_total_cash)).setText(price);
        //关闭对话框
        dialog.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        //去付款
        dialog.findViewById(R.id.go_pay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreatOrder.onCreatOrder(payType);
            }
        });
        //列表
        RecyclerView rvTypes = (RecyclerView) dialog.findViewById(R.id.pay_type_list);
        List<PayType> types = new ArrayList<>();
        initPayTypesData(types);
        PayTypeAdapter pt = new PayTypeAdapter(R.layout.pay_type_item, types);
        pt.setCallback(new PayTypeAdapter.Callback() {
            @Override
            public void onClicked(String arg0) {
                payType = arg0;
            }
        });
        rvTypes.setLayoutManager(new LinearLayoutManager(context));
        rvTypes.setAdapter(pt);
        //显示对话框
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.goods_profile_anim);
            WindowManager.LayoutParams wlp = window.getAttributes();
            Display d = window.getWindowManager().getDefaultDisplay();
            wlp.width = d.getWidth();
            wlp.gravity = Gravity.BOTTOM;
            window.setAttributes(wlp);
        }
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void initPayTypesData(List<PayType> types) {
        types.add(new PayType(true, R.mipmap.pay_left, "账户余额支付", "快捷支付 账户余额￥" + balance, "balance"));
        types.add(new PayType(false, R.mipmap.pay_ali, "支付宝支付", "大额支付，支持银行卡、信用卡", "alipay"));
        types.add(new PayType(false, R.mipmap.pay_weixin, "微信支付", "大额支付，支持银行卡、信用卡", "wxpay"));
    }

    //调起支付宝
    public void requestForAli(final String payInfo) {
        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                PayTask alipay = new PayTask(context);
                Map<String, String> result = alipay.payV2(payInfo, true);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    //调起微信
    public void requestForWX(JSONObject wxpay) {
        MyApplication.payType = 1;
        try {
            PayReq req = new PayReq();
            req.appId = wxpay.getString("appid");
            req.nonceStr = wxpay.getString("noncestr");
            req.packageValue = wxpay.getString("package");
            req.partnerId = wxpay.getString("partnerid");
            req.prepayId = wxpay.getString("prepayid");
            req.timeStamp = String.valueOf(wxpay.getLong("timestamp"));
            req.sign = wxpay.getString("sign");
            // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
            boolean hasWXApp = WXAPIFactory.createWXAPI(context, IDsUtils.WX_PAY_APP_ID).sendReq(req);
            if (!hasWXApp) {
                Toast.makeText(context, "您可能没有安装微信客户端", Toast.LENGTH_SHORT).show();
            }
            context.finish();
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            if (payCallBack != null) {
                payCallBack.onFinish(payType);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //余额支付
    public void requestForYuE(final JSONObject object1) {
        //输入密码
        showPassWordView(new InputPsdCallBack() {
            @Override
            public void onSuccess() {
                requstYuEResult(object1);
            }

            @Override
            public void onFailed() {
                //余额支付
                if (payCallBack != null) {
                    payCallBack.onFailed(payType, "支付失败");
                    payCallBack.onFinish(payType);
                }
            }

            @Override
            public void onCancel() {
                //余额支付
                if (payCallBack != null) {
                    payCallBack.onCancel(payType, "支付取消");
                    payCallBack.onFinish(payType);
                }
            }
        });

    }

    /**
     * 输入密码
     */
    private void showPassWordView(final InputPsdCallBack psdCallBack) {
        View view = context.getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        View background = new View(context);
        background.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popWindow = new PayPopWindow(context, background);
        popWindow.showAsDropDown(background);
        popWindow.setOnPasswordInputFinish(new OnPasswordInput() {
            @Override
            public void inputFinish() {
                popWindow.onDismiss();
                if (!StringUtils.isEmpty(popWindow.getStrPassword())) {
                    psdCallBack.onSuccess();
                }
            }

            @Override
            public void inputCancel() {
                popWindow.onDismiss();
                psdCallBack.onCancel();
            }

            @Override
            public void forgetPsd() {

            }
        });
    }

    //输入密码回调
    public interface InputPsdCallBack {
        void onSuccess();

        void onFailed();

        void onCancel();
    }

    //请求余额支付验证
    private void requstYuEResult(JSONObject object) {
        String url = object.optString("notify_url");//请求地址
        JSONObject payInfoObject = object.optJSONObject("payInfo");
        String orderId = payInfoObject.optString("out_trade_no");//订单号
        double price = payInfoObject.optDouble("total_fee");//价格
        int receiveId = payInfoObject.optInt("member_id");//收货地址id
        int type = payInfoObject.optInt("type");//类型：0——结算，1——我的订单\

        Request<JSONObject> jsonRequest = buildNetRequest(url);

        jsonRequest.add("notify_url", url);
        jsonRequest.add("out_trade_no", orderId);
        jsonRequest.add("total_fee", price);
        jsonRequest.add("member_id", receiveId);
        jsonRequest.add("type", type);

        executeNetWork(1, jsonRequest, "正在支付，请稍后");
    }

    public Request<JSONObject> buildNetRequest(String url) {
        Request<JSONObject> jsonRequest = NoHttp.createJsonObjectRequest(url, RequestMethod.POST);
        int uuid = AppConfig.getInstance().getInt("uuid", -1000);
        String token = AppConfig.getInstance().getString("token", "");
        long expiring_time = AppConfig.getInstance().getLong("expiring_time", -2000);
        jsonRequest.add("sign", UrlEncodeUtils.createSign(url));
        jsonRequest.add("uuid", uuid);
        jsonRequest.add("token", token);
        jsonRequest.add("expiring_time", expiring_time);
        return jsonRequest;
    }

    //执行网络请求
    public void executeNetWork(int what, Request<JSONObject> jsonRequest, String message) {
        boolean netAva = NetUtils.isNetworkAvailable(context);
        if (!netAva) {
            dialogHome.buildAlertDialog(context.getResources().getString(R.string.no_network));
            return;
        }
        MyApplication.getRequestQueue().add(what, jsonRequest, new NetResponseListener());
        dialogHome.buildWaitDialog(message);
    }

    public class NetResponseListener implements OnResponseListener<JSONObject> {
        @Override
        public void onStart(int what) {
        }

        @Override
        public void onSucceed(int what, Response<JSONObject> response) {
            dialogHome.dismissDialog();
            requstSuccess(what, response.get());
        }

        @Override
        public void onFailed(int what, Response<JSONObject> response) {
            dialogHome.dismissDialog();
            switch (what) {
                case 1:
                    //余额支付
                    dialogHome.buildAlertDialog("支付失败");
                    break;
            }
        }

        @Override
        public void onFinish(int what) {

            switch (what) {
                case 1:
                    //余额支付
                    if (payCallBack != null) {
                        payCallBack.onFinish(payType);
                    }
                    break;
            }
        }
    }

    public void requstSuccess(int what, JSONObject jObj) {
        try {
            int status = jObj.optInt("status");
            String message = jObj.optString("msg");
            JSONObject dataObj = jObj.optJSONObject("data");
            if (status == 200) {
                switch (what) {
                    case 1:
                        //余额支付
                        if (payCallBack != null) {
                            payCallBack.onSuccess(payType, "支付成功");
                            payCallBack.onFinish(payType);
                        }
                        break;
                }

            } else if (status == 1 || status == 2 || status == 3 || status == 4 || status == 5) {//缺少sign参数
                Intent intent = new Intent(context, LoginActivity.class);
                AppConfig.getInstance().putInt("uuid", -1000);
                context.startActivityForResult(intent, REQUEST_LOGIN);
                ((MyApplication) context.getApplication()).exit();
            } else {
                switch (what) {
                    case 1:
                        //余额支付
                        if (payCallBack != null) {
                            payCallBack.onFailed(payType, "支付失败");
                            payCallBack.onFinish(payType);
                        }
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
