package com.ascba.rebate.activities.shop.order;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ascba.rebate.R;
import com.ascba.rebate.activities.base.BaseNetActivity;
import com.ascba.rebate.adapter.order.DeliverDetailsAdapter;
import com.ascba.rebate.beans.Goods;
import com.ascba.rebate.utils.StringUtils;
import com.ascba.rebate.utils.TimeUtils;
import com.ascba.rebate.utils.UrlUtils;
import com.ascba.rebate.view.ShopABarText;
import com.yanzhenjie.nohttp.rest.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 李鹏 on 2017/03/15 0015.
 * 待收货订单详情
 */

public class TakeDetailsActivity extends BaseNetActivity implements SwipeRefreshLayout.OnRefreshListener, BaseNetActivity.Callback, View.OnClickListener {

    private List<Goods> goodsList = new ArrayList<>();
    private Context context;
    private ShopABarText shopABarText;
    private RecyclerView recyclerView;
    private String orderId;
    private DeliverDetailsAdapter adapter;
    //收货地址
    private RelativeLayout addressView;
    private LinearLayout contactStoreTx, countdownView;
    private TextView phoneTx, nameTx, addressTx;
    private TextView storeTx, orderSnTx, orderTimeTx, addWayTx;
    private TextView orderAmountTx, shippingFeeTx, vouchersFeeTx, orderPriceTx;
    private TextView payTx, deleteTx, countdownTx, closeOrderTx;
    private int flag = 0;//0-获取数据,1-确认收货
    private String storePhone;
    private TextView btnTake, btnRefund;//确认收货、退款

    //倒计时
    private int maxTime = 604800;//单位—秒 7天
    private Handler handler = new Handler();
    private int countdownSecond;
    private boolean isCountdown;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (countdownSecond > 0) {
                countdownTx.setText(countdownSecond / 86400 + "天" + countdownSecond / 3600 + "时");
                countdownSecond--;
                handler.postDelayed(this, 1000);
            } else {
                countdownView.setVisibility(View.INVISIBLE);
                closeOrderTx.setVisibility(View.VISIBLE);
                handler.removeCallbacks(runnable);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_details);
        context = this;
        initView();
        getOrderId();
    }


    private void initView() {
        //刷新
        initRefreshLayout();
        refreshLayout.setOnRefreshListener(this);

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

        phoneTx = (TextView) findViewById(R.id.address_phone);
        nameTx = (TextView) findViewById(R.id.address_name);
        addressTx = (TextView) findViewById(R.id.address_address);
        storeTx = (TextView) findViewById(R.id.store_name);
        orderSnTx = (TextView) findViewById(R.id.order_sn);
        orderTimeTx = (TextView) findViewById(R.id.order_time);
        addWayTx = (TextView) findViewById(R.id.value_add_way);
        contactStoreTx = (LinearLayout) findViewById(R.id.contact_store);
        contactStoreTx.setOnClickListener(this);
        orderAmountTx = (TextView) findViewById(R.id.order_amount);
        shippingFeeTx = (TextView) findViewById(R.id.shipping_fee);
        vouchersFeeTx = (TextView) findViewById(R.id.vouchers_fee);
        orderPriceTx = (TextView) findViewById(R.id.order_price);
        countdownTx = (TextView) findViewById(R.id.tx_countdown);
        closeOrderTx = (TextView) findViewById(R.id.tx_close_order);
        countdownView = (LinearLayout) findViewById(R.id.ll_countdown);
        btnTake = (TextView) findViewById(R.id.btn_take);
        btnTake.setOnClickListener(this);
        btnRefund = (TextView) findViewById(R.id.btn_refund);
        btnRefund.setOnClickListener(this);

        //recyclerView
        recyclerView = (RecyclerView) findViewById(R.id.deliver_details_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new DeliverDetailsAdapter(R.layout.item_goods, goodsList, context);
        recyclerView.setAdapter(adapter);
    }

    private void getOrderId() {
        Intent intent = getIntent();
        if (intent != null) {
            orderId = intent.getStringExtra("order_id");
            if (orderId != null) {
                requstData(UrlUtils.viewOrderGoods, 0);
            } else {
                showToast(getString(R.string.no_data_txt));
                finish();
            }
        }
    }

    /*
      获取列表数据
    */
    private void requstData(String url, int flag) {
        this.flag = flag;
        Request<JSONObject> jsonRequest = buildNetRequest(url, 0, true);
        switch (flag) {
            case 0:
                jsonRequest.add("order_goods_id", orderId);
                break;
            case 1:
                jsonRequest.add("order_goods_id", orderId);
                break;
        }

        executeNetWork(jsonRequest, "请稍后");
        setCallback(this);
    }


    @Override
    public void onRefresh() {
        requstData(UrlUtils.viewOrderGoods, 0);
    }


    @Override
    public void handle200Data(JSONObject dataObj, String message) {
        switch (flag) {
            case 0:
                /*
                获取订单数据
                */
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                }

                //收货地址
                getAddress(dataObj);

                //订单信息
                getGoodsInfo(dataObj);

                //店铺电话
                storePhone = dataObj.optJSONObject("store_info").optString("store_mobile");
                break;
            case 1:
                //确认收货
                getDm().buildAlertDialog("确认收货");
                break;
        }
    }

    @Override
    public void handle404(String message) {
        if (refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(false);
        }
        getDm().buildAlertDialog(message);
    }

    @Override
    public void handleNoNetWork() {
        if (refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(false);
        }
    }

    /*
         收货地址
         "reciver_name": "刘小典",
	     "reciver_mobile": "13400352743",
		 "reciver_address": "河北省张家口市桥东区"

        */
    private void getAddress(JSONObject dataObject) {
        try {
            JSONObject addressObject = dataObject.getJSONObject("order_member_address");
            String name = addressObject.optString("reciver_name");//收货人姓名
            String phone = addressObject.optString("reciver_mobile");//手机号
            String address = addressObject.optString("reciver_address");//收货地址
            phoneTx.setText(phone);
            nameTx.setText(name);
            addressTx.setText(address);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /*
   订单信息
    */
    private void getGoodsInfo(JSONObject dataObject) {
        try {
            JSONObject orderObject = dataObject.getJSONObject("order_info");

            //店铺信息
            String storeName = orderObject.optString("store_name");//店铺
            storeTx.setText(storeName);

            //订单信息
            String shippingFee = orderObject.optString("shipping_fee");//邮费
            String orderSn = orderObject.optString("order_sn");//订单号
            String goodsAmount = orderObject.optString("goods_amount");//商品价格
            String orderAmount = orderObject.optString("order_amount");//订单价格
            String orderTime = orderObject.optString("add_time");//订单时间
            orderTime = TimeUtils.milliseconds2String(Long.parseLong(orderTime) * 1000);

             /*
               开始支付倒计时
             */
            if (!isCountdown) {
                //时间差
                countdownSecond = TimeUtils.countdownTime(maxTime, orderTime);
                isCountdown = handler.postDelayed(runnable, 1000);
            }

            orderSnTx.setText(orderSn);
            orderTimeTx.setText(orderTime);
            orderAmountTx.setText("￥" + orderAmount);
            shippingFeeTx.setText("￥" + shippingFee);

            if (goodsList.size() > 0) {
                goodsList.clear();
            }

            //商品信息
            JSONObject goodObject = dataObject.optJSONObject("order_goods_info");
            String goodName = goodObject.optString("goods_name");//商品名
            String goodsPrice = goodObject.optString("goods_price");//商品价格
            String specNames = goodObject.optString("spec_names");//商品规格
            String goodNum = goodObject.optString("goods_num");//数量
            String goodImg = UrlUtils.baseWebsite + goodObject.optString("goods_img");//商品图片
            goodsList.add(new Goods(goodImg, goodName, specNames, goodsPrice, Integer.parseInt(goodNum)));

            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contact_store:
                //联系商家
                if (!StringUtils.isEmpty(storePhone)) {
                    Intent intent1 = new Intent();
                    intent1.setAction(Intent.ACTION_DIAL);
                    intent1.setData(Uri.parse("tel:" + storePhone));
                    startActivity(intent1);
                }else {
                    showToast("没有卖家联系方式");
                }
                break;
            case R.id.btn_take:
                //确认收货
                requstData(UrlUtils.orderReceive, 1);
                break;
            case R.id.btn_refund:
                //退款
                break;
        }
    }
}
