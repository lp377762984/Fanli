package com.ascba.rebate.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.ascba.rebate.R;
import com.ascba.rebate.activities.base.BaseNetWork4Activity;
import com.ascba.rebate.adapter.ConfirmOrderAdapter;
import com.ascba.rebate.adapter.PayTypeAdapter;
import com.ascba.rebate.appconfig.AppConfig;
import com.ascba.rebate.application.MyApplication;
import com.ascba.rebate.beans.Goods;
import com.ascba.rebate.beans.PayType;
import com.ascba.rebate.beans.ReceiveAddressBean;
import com.ascba.rebate.handlers.DialogManager;
import com.ascba.rebate.utils.IDsUtils;
import com.ascba.rebate.utils.StringUtils;
import com.ascba.rebate.utils.UrlEncodeUtils;
import com.ascba.rebate.utils.UrlUtils;
import com.ascba.rebate.view.ShopABarText;
import com.ascba.rebate.view.SuperSwipeRefreshLayout;
import com.ascba.rebate.view.pay.PayResult;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.yolanda.nohttp.rest.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 李鹏 on 2017/03/15 0015.
 * 确认订单
 */

public class ConfirmOrderActivity extends BaseNetWork4Activity implements SuperSwipeRefreshLayout.OnPullRefreshListener, View.OnClickListener {

    private SuperSwipeRefreshLayout refreshLat;
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
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        Toast.makeText(ConfirmOrderActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
                        try {
                            JSONObject jObj = new JSONObject(resultInfo);
                            JSONObject trObj = jObj.optJSONObject("alipay_trade_app_pay_response");
                            String total_amount = trObj.optString("total_amount");
                            /*Intent intent = new Intent(ConfirmOrderActivity.this, RechaSuccActivity.class);
                            intent.putExtra("money", total_amount + "元");
                            startActivityForResult(intent, FourthFragment.REQUEST_PAY);*/

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if(TextUtils.equals(resultStatus, "6002")) {
                        dm.buildAlertDialog("网络有问题");
                    } else if(TextUtils.equals(resultStatus, "6001")) {
                        dm.buildAlertDialog("您已经取消支付");
                    } else {
                        dm.buildAlertDialog("支付失败");
                    }
                    setResult(RESULT_OK,getIntent());
                    finish();
                    if(dialog!=null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                    break;
                }
                default:
                    break;
            }
        }

        ;
    };
    private Context context;
    private ShopABarText shopABarText;
    private RecyclerView recyclerView;
    private DialogManager dm;
    private ArrayList<ReceiveAddressBean> beanList = new ArrayList<>();//收货地址
    private ReceiveAddressBean defaultAddressBean;//默认收货地址
    private RelativeLayout receiveAddress, noReceiveAddress;
    private TextView username;//收货人姓名
    private TextView userPhone;//收货人电话
    private TextView userAddress;//收货人地址
    private String json_data;
    private TextView tvTotal;
    private List<Goods> goodsList = new ArrayList<>();
    private JSONObject jsonMessage = new JSONObject();//留言信息
    private DecimalFormat fnum = new DecimalFormat("##0.00");//格式化，保留两位
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_confirm_order);
        context = this;
        getDataFromIntent();
        initUI();

        //获取收货地址
        getAddress();

    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        json_data = intent.getStringExtra("json_data");
    }

    private void initUI() {
        //刷新
        refreshLat = ((SuperSwipeRefreshLayout) findViewById(R.id.refresh_layout));
        refreshLat.setOnPullRefreshListener(this);
        //总金额
        tvTotal = ((TextView) findViewById(R.id.confir_order_text_total_price));

        //提交订单
        findViewById(R.id.confir_order_btn_commit).setOnClickListener(this);
        //导航栏
        shopABarText = (ShopABarText) findViewById(R.id.shopbar);
        shopABarText.setBtnEnable(false);
        shopABarText.setCallback(new ShopABarText.Callback() {
            @Override
            public void back(View v) {
                finish();
            }

            @Override
            public void clkBtn(View v) {

            }
        });

        /**
         * 收货人信息
         */
        receiveAddress = (RelativeLayout) findViewById(R.id.confirm_order_addrss_rl);
        receiveAddress.setOnClickListener(this);

        noReceiveAddress = (RelativeLayout) findViewById(R.id.confirm_order_addrss_rl2);
        noReceiveAddress.setOnClickListener(this);

        username = (TextView) findViewById(R.id.confirm_order_username);
        userPhone = (TextView) findViewById(R.id.confirm_order_phone);
        userAddress = (TextView) findViewById(R.id.confirm_order_address);

        //recyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        ConfirmOrderAdapter confirmOrderAdapter = new ConfirmOrderAdapter(context, getData());
        recyclerView.setAdapter(confirmOrderAdapter);

        //买家留言
        confirmOrderAdapter.setEditTextString(new ConfirmOrderAdapter.editTextString() {
            @Override
            public void getString(String content, int storeId, String mesaagesCartId) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("cart_ids", mesaagesCartId);
                    jsonObject.put("message", content);
                    jsonMessage.put(String.valueOf(storeId), jsonObject);
                    Log.d("ConfirmOrderActivity", jsonMessage.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private List<Goods> getData() {
        try {
            if (goodsList.size() != 0) {
                goodsList.clear();
            }
            JSONObject dataObj = new JSONObject(json_data);
            JSONArray storeList = dataObj.optJSONArray("order_store_list");
            if (storeList != null && storeList.length() != 0) {
                float totalPrice = 0;
                for (int i = 0; i < storeList.length(); i++) {
                    JSONObject storeObj = storeList.optJSONObject(i);
                    if (storeObj != null) {
                        JSONObject titleObj = storeObj.optJSONObject("store_info");
                        goodsList.add(new Goods(ConfirmOrderAdapter.TYPE1, R.layout.item_store, titleObj.optString("store_name")));
                        JSONArray goodsArray = storeObj.optJSONArray("goods_list");
                        if (goodsArray != null && goodsArray.length() != 0) {
                            float yunfei = 10;//运费
                            int num = 0;
                            float price = 0;
                            int storeId = 0;
                            String cartId = null;
                            StringBuffer mesaagesCartId = new StringBuffer();
                            for (int j = 0; j < goodsArray.length(); j++) {
                                JSONObject obj = goodsArray.optJSONObject(j);
                                String goods_price = obj.optString("goods_price");
                                String goods_num = obj.optString("goods_num");
                                //商品信息
                                goodsList.add(new Goods(ConfirmOrderAdapter.TYPE2, R.layout.item_goods, UrlUtils.baseWebsite + obj.optString("goods_img"),
                                        obj.optString("goods_name"), obj.optString("spec_names"), goods_price,
                                        "no_old_price", Integer.parseInt(goods_num)));

                                num += Integer.parseInt(goods_num);
                                price += Float.parseFloat(goods_price) * Integer.parseInt(goods_num);
                                //店铺id
                                storeId = Integer.valueOf(String.valueOf(obj.opt("store_id")));
                                //购物车id
                                cartId = obj.optString("cart_id");
                                mesaagesCartId.append(cartId + ",");
                            }
                            /**
                             * 拼接空白留言信息
                             */
                            JSONObject jsonObject = new JSONObject();
                            mesaagesCartId.delete(mesaagesCartId.length() - 1, mesaagesCartId.length());
                            jsonObject.put("cart_ids", mesaagesCartId.toString());
                            jsonObject.put("message", "");
                            jsonMessage.put(String.valueOf(storeId), jsonObject);

                            price += yunfei;
                            totalPrice += price;
                            goodsList.add(new Goods(ConfirmOrderAdapter.TYPE3, R.layout.item_cost, fnum.format(yunfei), num, fnum.format(price), storeId, mesaagesCartId.toString()));
                        }
                    }
                }
                tvTotal.setText("￥" + fnum.format(totalPrice));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return goodsList;
    }

    /*
     * 获取收货地址数据
     */
    private void getAddress() {
        dm = new DialogManager(context);
        Request<JSONObject> jsonRequest = buildNetRequest(UrlUtils.getMemberAddress, 0, true);
        jsonRequest.add("sign", UrlEncodeUtils.createSign(UrlUtils.getMemberAddress));
        jsonRequest.add("member_id", AppConfig.getInstance().getInt("uuid", -1000));
        executeNetWork(jsonRequest, "请稍后");
        setCallback(new Callback() {
            @Override
            public void handle200Data(JSONObject dataObj, String message) {
                beanList.clear();
                JSONArray jsonArray = dataObj.optJSONArray("member_address_list");
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject object = jsonArray.getJSONObject(i);
                        ReceiveAddressBean addressBean = new ReceiveAddressBean();
                        addressBean.setId(object.optString("id"));
                        addressBean.setName(object.optString("consignee"));
                        addressBean.setPhone(object.optString("mobile"));
                        addressBean.setAddress(object.optString("address"));
                        addressBean.setProvince(object.optString("province"));
                        addressBean.setCity(object.optString("city"));
                        addressBean.setDistrict(object.optString("district"));
                        addressBean.setTwon(object.optString("twon"));
                        String isSelected = object.optString("default");
                        addressBean.setIsDefault(isSelected);
                        if (isSelected.equals("1")) {
                            beanList.add(0, addressBean);
                        } else {
                            beanList.add(addressBean);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                //如果保存了收货地址id就遍历查找
                if (!StringUtils.isEmpty(MyApplication.addressId)) {
                    for (ReceiveAddressBean bean : beanList) {
                        if (bean.getId().equals(MyApplication.addressId)) {
                            defaultAddressBean = bean;
                        }
                    }
                } else {
                    //如果没有保存数据，就设置默认收货地址为当前地址，并保存
                    if (beanList.size() != 0 && beanList.get(0).getIsDefault().equals("1")) {
                        defaultAddressBean = beanList.get(0);
                        MyApplication.addressId = defaultAddressBean.getId();
                    }
                }
                setReceiveData();
            }

            @Override
            public void handle404(String message) {
                dm.buildAlertDialog(message);
            }

            @Override
            public void handleNoNetWork() {
                dm.buildAlertDialog(getString(R.string.no_network));
            }
        });
    }

    /**
     * set收货地址信息
     */
    private void setReceiveData() {
        if (defaultAddressBean == null) {
            receiveAddress.setVisibility(View.GONE);
            noReceiveAddress.setVisibility(View.VISIBLE);
        } else {
            receiveAddress.setVisibility(View.VISIBLE);
            noReceiveAddress.setVisibility(View.GONE);
            /**
             * 初始化收货地址数据
             */
            username.setText(defaultAddressBean.getName());
            userPhone.setText(defaultAddressBean.getPhone());
            userAddress.setText(defaultAddressBean.getAddress());
        }
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onPullDistance(int distance) {

    }

    @Override
    public void onPullEnable(boolean enable) {

    }

    /*
     * 创建订单
     */
    private void creatOrder(String receiveId, String message, final String payType) {
        dm = new DialogManager(context);
        Request<JSONObject> jsonRequest = buildNetRequest(UrlUtils.createOrder, 0, true);
        jsonRequest.add("member_id", AppConfig.getInstance().getInt("uuid", -1000));
        jsonRequest.add("extra_data", message);
        jsonRequest.add("member_address_id", receiveId);//用户收货地址id
        jsonRequest.add("payment_type", payType);//支付方式(余额支付：balance，支付宝：alipay，微信：wxpay)
        executeNetWork(jsonRequest, "请稍后");
        setCallback(new Callback() {
            @Override
            public void handle200Data(JSONObject dataObj, String message) {
                //创建并支付订单成功
                //showToast(message);
                /*setResult(RESULT_OK,getIntent());
                finish();*/

                if("balance".equals(payType)){
                    showToast("暂未开放");
                }else if("alipay".equals(payType)){
                    requestForAli(dataObj);//发起支付宝支付请求
                }else if("wxpay".equals(payType)){
                    requestForWX(dataObj);
                }

            }

            @Override
            public void handle404(String message) {
                dm.buildAlertDialog(message);
            }

            @Override
            public void handleNoNetWork() {
                dm.buildAlertDialog(getString(R.string.no_network));
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_order_addrss_rl:
                //选择收货地址
                Intent intent = new Intent(context, SelectAddrssActivity.class);
                intent.putParcelableArrayListExtra("address", beanList);
                startActivityForResult(intent, 1);
                break;
            case R.id.confirm_order_addrss_rl2:
                //选择收货地址
                Intent intent2 = new Intent(context, SelectAddrssActivity.class);
                intent2.putParcelableArrayListExtra("address", beanList);
                startActivityForResult(intent2, 1);
                break;
            case R.id.confir_order_btn_commit:
                //提交订单
                if (defaultAddressBean != null && !StringUtils.isEmpty(defaultAddressBean.getId())) {
                    showFinalDialog();
                } else {
                    showToast("请先填写收货地址");
                }
                break;
        }
    }

    /**
     * 接受选择地址回调结果
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            getAddress();//刷新数据
            if (resultCode == 1 && data != null) {
                //更改当前收货地址
                defaultAddressBean = data.getParcelableExtra("address");
                setReceiveData();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.addressId = null;
    }

    private void initPayTypesData(List<PayType> types) {
        types.add(new PayType(true, R.mipmap.pay_left, "账户余额支付", "快捷支付","balance"));
        types.add(new PayType(false, R.mipmap.pay_ali, "支付宝支付", "大额支付，支持银行卡、信用卡","alipay"));
        types.add(new PayType(false, R.mipmap.pay_weixin, "微信支付", "大额支付，支持银行卡、信用卡","wxpay"));
    }
    //选择支付方式页面
    private void showFinalDialog() {
        final String[] type = {"balance"};
        dialog = new Dialog(this, R.style.AlertDialog);
        dialog.setContentView(R.layout.layout_pay_pop);
        ((TextView) dialog.findViewById(R.id.dlg_tv_total_cash)).setText(tvTotal.getText());
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
                creatOrder(defaultAddressBean.getId(), jsonMessage.toString(),type[0]);
            }
        });
        //列表
        RecyclerView rvTypes = (RecyclerView) dialog.findViewById(R.id.pay_type_list);
        List<PayType> types = new ArrayList<>();
        initPayTypesData(types);
        PayTypeAdapter pt = new PayTypeAdapter(R.layout.pay_type_item, types);
        pt.setCallback(new PayTypeAdapter.Callback() {
            @Override
            public void onClicked(String payType) {
                type[0] =payType;
            }
        });
        rvTypes.setLayoutManager(new LinearLayoutManager(this));
        rvTypes.setAdapter(pt);
        //显示对话框
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.goods_profile_anim);
            //window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams wlp = window.getAttributes();
            Display d = window.getWindowManager().getDefaultDisplay();
            wlp.width = d.getWidth();
            wlp.gravity = Gravity.BOTTOM;
            window.setAttributes(wlp);
        }
    }
    //调起支付宝
    private void requestForAli(JSONObject dataObj) {
        JSONObject object = dataObj.optJSONObject("payreturn_data");
        JSONObject object1 = object.optJSONObject("data");
        final String payInfo = object1.optString("payInfo");
        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                PayTask alipay = new PayTask(ConfirmOrderActivity.this);
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
    private void requestForWX(JSONObject dataObj) {
        try {
            JSONObject object = dataObj.optJSONObject("payreturn_data");
            JSONObject object1 = object.optJSONObject("data");
            JSONObject wxpay = object1.getJSONObject("wxpay");
            PayReq req = new PayReq();
            req.appId = wxpay.getString("appid");
            req.nonceStr = wxpay.getString("noncestr");
            req.packageValue = wxpay.getString("package");
            req.partnerId = wxpay.getString("partnerid");
            req.prepayId = wxpay.getString("prepayid");
            req.timeStamp = wxpay.getInt("timestamp")+"";
            req.sign = wxpay.getString("sign");
            // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
            boolean hasWXApp = WXAPIFactory.createWXAPI(this, IDsUtils.WX_PAY_APP_ID).sendReq(req);
            if(!hasWXApp){
                showToast("您可能没有安装微信客户端");
            }
            if(dialog!=null && dialog.isShowing()){
                dialog.dismiss();
            }
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
