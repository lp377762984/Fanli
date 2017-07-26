package com.ascba.rebate.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ascba.rebate.R;
import com.ascba.rebate.activities.base.BaseNetActivity;
import com.ascba.rebate.activities.shop.order.DeliverDetailsActivity;
import com.ascba.rebate.activities.shop.MyOrderActivity;
import com.ascba.rebate.activities.shop.order.PayDetailsActivity;
import com.ascba.rebate.adapter.ConfirmOrderAdapter;
import com.ascba.rebate.appconfig.AppConfig;
import com.ascba.rebate.application.MyApplication;
import com.ascba.rebate.beans.Goods;
import com.ascba.rebate.beans.ReceiveAddressBean;
import com.ascba.rebate.utils.DialogHome;
import com.ascba.rebate.utils.PayUtils;
import com.ascba.rebate.utils.StringUtils;
import com.ascba.rebate.utils.UrlEncodeUtils;
import com.ascba.rebate.utils.UrlUtils;
import com.ascba.rebate.utils.ViewUtils;
import com.ascba.rebate.view.ShopABarText;
import com.yanzhenjie.nohttp.rest.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 李鹏 on 2017/03/15 0015.
 * 立即购买——确认订单
 */

public class ConfirmBuyOrderActivity extends BaseNetActivity implements View.OnClickListener {

    private static final int REQUEST_SETTING_PAY_PSD = 0;
    private Context context;
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
    private PayUtils pay;
    private String balance;//账户余额
    private String orderId;//订单id
    private ConfirmOrderAdapter confirmOrderAdapter;
    private String pay_total_fee;
    private int is_level_pwd;

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

        //总金额
        tvTotal = ((TextView) findViewById(R.id.confir_order_text_total_price));

        //提交订单
        findViewById(R.id.confir_order_btn_commit).setOnClickListener(this);
        //导航栏
        ShopABarText shopABarText = (ShopABarText) findViewById(R.id.shopbar);
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


        //recyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        confirmOrderAdapter = new ConfirmOrderAdapter(context, getData());
        initHeadView();
        initTailView();
        recyclerView.setAdapter(confirmOrderAdapter);
        //买家留言
        confirmOrderAdapter.setEditTextString(new ConfirmOrderAdapter.editTextString() {
            @Override
            public void getString(String content, int storeId, String mesaagesCartId) {
                try {
                    jsonMessage.put("message", content);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void initTailView() {
        View tailView= ViewUtils.getView(this, R.layout.confirm_order_footer);
        TextView tailTicket = ((TextView) tailView.findViewById(R.id.tv_ticket));
        TextView tailZongyouhui = ((TextView) tailView.findViewById(R.id.tv_zongyouhui));
        TextView tailShijiyouhui = ((TextView) tailView.findViewById(R.id.tv_shijiyouhui));
        TextView tailZengzhijifen = ((TextView) tailView.findViewById(R.id.tv_zengzhijifen));
        try {
            JSONObject dataObj = new JSONObject(json_data);
            JSONObject checkObj = dataObj.optJSONObject("checkout_data");
            tailTicket.setText(checkObj.optString("member_coupon"));
            tailZongyouhui.setText("￥"+checkObj.optString("total_coupon_money"));
            tailShijiyouhui.setText("￥"+checkObj.optString("total_employ_coupon_money"));
            tailZengzhijifen.setText(checkObj.optString("increment_score"));
            pay_total_fee = checkObj.optString("pay_total_fee");
            tvTotal.setText("￥"+ checkObj.optString("pay_total_fee"));
            confirmOrderAdapter.addFooterView(tailView);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initHeadView() {
        View headView = ViewUtils.getView(this, R.layout.confirm_order_header_address);
        //收货人信息
        receiveAddress = (RelativeLayout) headView.findViewById(R.id.confirm_order_addrss_rl);
        receiveAddress.setOnClickListener(this);
        noReceiveAddress = (RelativeLayout) headView.findViewById(R.id.confirm_order_addrss_rl2);
        noReceiveAddress.setOnClickListener(this);
        username = (TextView) headView.findViewById(R.id.confirm_order_username);
        userPhone = (TextView) headView.findViewById(R.id.confirm_order_phone);
        userAddress = (TextView) headView.findViewById(R.id.confirm_order_address);
        try {
            JSONObject dataObj = new JSONObject(json_data);
            JSONObject checkObj = dataObj.optJSONObject("member_default_address");
            username.setText(checkObj.optString("consignee"));
            userPhone.setText(checkObj.optString("mobile"));
            userAddress.setText(checkObj.optString("address_detail"));
            confirmOrderAdapter.addHeaderView(headView);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<Goods> getData() {
        try {
            if (goodsList.size() != 0) {
                goodsList.clear();
            }
            JSONObject dataObj = new JSONObject(json_data);
            //用户信息
            JSONObject member_info = dataObj.optJSONObject("member_info");
            balance = member_info.optString("money");//余额
            //商品店铺信息
            JSONArray storeList = dataObj.optJSONArray("order_store_list");
            if (storeList != null && storeList.length() != 0) {
                for (int i = 0; i < storeList.length(); i++) {
                    JSONObject storeObj = storeList.optJSONObject(i);
                    if (storeObj != null) {
                        JSONObject titleObj = storeObj.optJSONObject("store_info");
                        String store_id = titleObj.optString("id");
                        jsonMessage.put("store_id", store_id);
                        goodsList.add(new Goods(ConfirmOrderAdapter.TYPE1, R.layout.item_store, titleObj.optString("store_name")));
                        JSONArray goodsArray = storeObj.optJSONArray("goods_list");
                        if (goodsArray != null && goodsArray.length() != 0) {
                            for (int j = 0; j < goodsArray.length(); j++) {
                                JSONObject obj = goodsArray.optJSONObject(j);
                                String goods_price = obj.optString("goods_price");
                                String goods_num = obj.optString("goods_num");
                                jsonMessage.put("goods_num", goods_num);
                                String goods_id = obj.optString("goods_id");
                                jsonMessage.put("goods_id", goods_id);
                                String goods_spec_id = obj.optString("goods_spec_id");
                                jsonMessage.put("goods_spec_id", goods_spec_id);
                                //商品信息
                                Goods goods = new Goods(ConfirmOrderAdapter.TYPE2, R.layout.item_goods, obj.optString("goods_img"),
                                        obj.optString("goods_name"), obj.optString("spec_names"), goods_price,
                                        "no_old_price", Integer.parseInt(goods_num));
                                goods.setTeiHui(obj.optString("promotion_text"));
                                goods.setUseTicketToReduce(obj.optString("promotion_mark"));
                                goodsList.add(goods);
                            }
                            jsonMessage.put("message", "");
                            //礼品券一些信息 共几件商品，合计多少金额，运费多少
                            JSONObject exeObj = storeObj.optJSONObject("extra_data");
                            Goods goods = new Goods(ConfirmOrderAdapter.TYPE3, R.layout.item_cost, exeObj.optString("shipping_fee"), exeObj.optInt("total_num"), exeObj.optString("total_fee"), 0, null);
                            goods.setSubtract(exeObj.optString("employ_coupon_money"));
                            goods.setSubDesc(exeObj.optString("coupon_info"));
                            goodsList.add(goods);
                        }
                    }
                }
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
                        addressBean.setAddressDetl(object.optString("address_detail"));
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
            }

            @Override
            public void handleNoNetWork() {
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
            userAddress.setText(defaultAddressBean.getAddressDetl());
        }
    }

    /*
     * 创建订单
     */
    private void creatOrder(String receiveId, String message, final String payType) {
        Request<JSONObject> jsonRequest = buildNetRequest(UrlUtils.createNowBuyOrder, 0, true);
        jsonRequest.add("member_id", AppConfig.getInstance().getInt("uuid", -1000));
        jsonRequest.add("extra_data", message);
        jsonRequest.add("member_address_id", receiveId);//用户收货地址id
        jsonRequest.add("payment_type", payType);//支付方式(余额支付：balance，支付宝：alipay，微信：wxpay)
        executeNetWork(jsonRequest, "请稍后");
        setCallback(new Callback() {
            @Override
            public void handle200Data(JSONObject dataObj, String message) {
                //创建并支付订单成功,开始支付
                payOrder(dataObj, payType,message);
            }

            @Override
            public void handle404(String message) {
                PayUtils.onPayCallBack payCallBack = pay.getPayCallBack();
                if (payCallBack != null) {
                    pay.getPayCallBack().onFinish(payType);
                    pay.getPayCallBack().onCancel(payType);
                }
                getDm().buildAlertDialog(message);
            }

            @Override
            public void handleNoNetWork() {

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
                try {
                    JSONObject dataObj = new JSONObject(json_data);
                    JSONObject object = dataObj.optJSONObject("checkout_data");
                    is_level_pwd = object.optInt("is_level_pwd");
                    if (defaultAddressBean != null && !StringUtils.isEmpty(defaultAddressBean.getId())) {
                        pay = new PayUtils(this, tvTotal.getText().toString(), balance);
                        pay.showDialog(new PayUtils.OnCreatOrder() {
                            @Override
                            public void onCreatOrder(String payType) {
                                if(StringUtils.isEmpty(balance)){
                                    balance="0";
                                }
                                if(StringUtils.isEmpty(pay_total_fee)){
                                    pay_total_fee="0";
                                }
                                if("balance".equals(payType) && Double.parseDouble(balance) < Double.parseDouble(pay_total_fee)){
                                    showToast("余额不足");
                                    return;
                                }
                                //检测用户是否设置了支付密码
                                if("balance".equals(payType) && is_level_pwd==0){
                                    getDm().buildAlertDialogSure("您还未设置支付密码，是否去设置？", new DialogHome.Callback() {
                                        @Override
                                        public void handleSure() {
                                            Intent intent1=new Intent(ConfirmBuyOrderActivity.this,PayPsdSettingActivity.class);
                                            startActivityForResult(intent1,REQUEST_SETTING_PAY_PSD);
                                        }
                                    });
                                    return;
                                }
                                MyApplication.isLoadCartData = true;//需要刷新购物车数据
                                creatOrder(defaultAddressBean.getId(), jsonMessage.toString(), payType);
                            }
                        });
                        //支付结果回调
                        pay.setPayCallBack(new PayUtils.onPayCallBack() {
                            @Override
                            public void onFinish(String payStype) {
                            }

                            @Override
                            public void onSuccess(String payStype) {
                                showToast("支付成功");
                                if (StringUtils.isEmpty(orderId)) {
                                    //跳转待付款列表
                                    MyOrderActivity.startIntent(context, 2);
                                } else {
                                    Intent intent = new Intent(context, DeliverDetailsActivity.class);
                                    intent.putExtra("order_id", orderId);
                                    startActivity(intent);
                                }
                                finish();
                            }

                            @Override
                            public void onCancel(String payStype) {
                                showToast("支付取消");
                                if (StringUtils.isEmpty(orderId)) {
                                    //跳转待付款列表
                                    MyOrderActivity.startIntent(context, 1);
                                } else {
                                    Intent intent = new Intent(context, PayDetailsActivity.class);
                                    intent.putExtra("order_id", orderId);
                                    startActivity(intent);
                                }
                                finish();
                            }

                            @Override
                            public void onFailed(String payStype, String msg) {
                                showToast(msg);
                                if (StringUtils.isEmpty(orderId)) {
                                    //跳转待付款列表
                                    MyOrderActivity.startIntent(context, 1);
                                } else {
                                    Intent intent = new Intent(context, PayDetailsActivity.class);
                                    intent.putExtra("order_id", orderId);
                                    startActivity(intent);
                                }
                                finish();
                            }

                            @Override
                            public void onNetProblem(String payStype) {
                                showToast("手机网络出现问题");
                            }
                        });
                    } else {
                        showToast("请先填写收货地址");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * 接受选择地址回调结果
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
        }else if(requestCode==REQUEST_SETTING_PAY_PSD){
            if(resultCode==RESULT_OK){
                AppConfig.getInstance().putInt("is_level_pwd",1);
                is_level_pwd=1;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.addressId = null;
    }

    //支付
    private void payOrder(JSONObject dataObj, final String payType,String message) {
        orderId = dataObj.optString("order_id", null);
        MyApplication.orderId = orderId;
        JSONObject object = dataObj.optJSONObject("payInfo");
        //调起支付
        if ("balance".equals(payType)) {
            //余额支付
            pay.dismissDialog();
            if (object != null) {
                pay.requestForYuE(dataObj);
            } else {
                //余额不足
                showToast(message);
                //跳转待付款列表
                MyOrderActivity.startIntent(context, 1);
                finish();
            }
        } else if ("alipay".equals(payType)) {
            String payInfo = dataObj.optString("payInfo");
            pay.requestForAli(payInfo);//发起支付宝支付请求
        } else if ("wxpay".equals(payType)) {
            JSONObject wxpay = dataObj.optJSONObject("wxpay");
            pay.requestForWX(wxpay);
        }



    }

}
