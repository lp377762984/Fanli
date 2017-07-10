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
import android.widget.TextView;

import com.ascba.rebate.R;
import com.ascba.rebate.activities.BusinessShopActivity;
import com.ascba.rebate.activities.GoodsDetailsActivity;
import com.ascba.rebate.activities.base.BaseNetActivity;
import com.ascba.rebate.activities.base.WebViewBaseActivity;
import com.ascba.rebate.adapter.order.DeliverDetailsAdapter;
import com.ascba.rebate.beans.Goods;
import com.ascba.rebate.utils.DialogHome;
import com.ascba.rebate.utils.StringUtils;
import com.ascba.rebate.utils.TimeUtils;
import com.ascba.rebate.utils.UrlUtils;
import com.ascba.rebate.view.ShopABarText;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.yanzhenjie.nohttp.rest.Request;

import org.json.JSONArray;
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
    private String orderId;
    private DeliverDetailsAdapter adapter;
    private TextView phoneTx, nameTx, addressTx;
    private TextView storeTx, orderSnTx, orderTimeTx;
    private TextView orderAmountTx, shippingFeeTx, vouchersFeeTx, orderPriceTx;
    private int flag = 0;//0-获取数据,1-确认收货
    private String storePhone;
    private TextView tvMsg;
    private View msgView;
    private String store_id;
    private Goods goods;//被点击的商品

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

        phoneTx = (TextView) findViewById(R.id.address_phone);
        nameTx = (TextView) findViewById(R.id.address_name);
        addressTx = (TextView) findViewById(R.id.address_address);
        storeTx = (TextView) findViewById(R.id.store_name);
        orderSnTx = (TextView) findViewById(R.id.order_sn);
        orderTimeTx = (TextView) findViewById(R.id.order_time);
        LinearLayout contactStoreTx = (LinearLayout) findViewById(R.id.contact_store);
        contactStoreTx.setOnClickListener(this);
        orderAmountTx = (TextView) findViewById(R.id.order_amount);
        shippingFeeTx = (TextView) findViewById(R.id.shipping_fee);
        vouchersFeeTx = (TextView) findViewById(R.id.vouchers_fee);
        orderPriceTx = (TextView) findViewById(R.id.order_price);
        TextView btnTake = (TextView) findViewById(R.id.btn_take);
        btnTake.setOnClickListener(this);
        TextView btnRefund = (TextView) findViewById(R.id.btn_refund);
        btnRefund.setOnClickListener(this);
        //recyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.deliver_details_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new DeliverDetailsAdapter(R.layout.item_goods, goodsList, context);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                goods = goodsList.get(position);
                GoodsDetailsActivity.startIntent(TakeDetailsActivity.this, goods.getTitleId());
            }

            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                super.onItemChildClick(adapter, view, position);
                if(view.getId()==R.id.tv_express_flow){
                    goods = goodsList.get(position);
                    requstData(UrlUtils.getAuctionExp, 2);
                }
            }
        });

        tvMsg = ((TextView) findViewById(R.id.tv_left_msg));
        msgView = findViewById(R.id.left_msg_lat);

        findViewById(R.id.store_lat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TakeDetailsActivity.this, BusinessShopActivity.class);
                intent.putExtra("store_id", Integer.parseInt(store_id));
                startActivity(intent);
            }
        });
    }

    private void getOrderId() {
        Intent intent = getIntent();
        if (intent != null) {
            orderId = intent.getStringExtra("order_id");
            if (orderId != null) {
                requstData(UrlUtils.viewOrder, 0);
            } else {
                showToast(getString(R.string.no_data_txt));
                finish();
            }
        }
    }

    private void requstData(String url, int flag) {
        this.flag = flag;
        Request<JSONObject> jsonRequest = buildNetRequest(url, 0, true);
        switch (flag) {
            case 0:
                jsonRequest.add("order_id", orderId);
                break;
            case 1:
                jsonRequest.add("order_id", orderId);
                break;
            case 2:
                jsonRequest.add("ordertraces",goods.getDeliverNum());
                break;
        }
        executeNetWork(jsonRequest, "请稍后");
        setCallback(this);
    }


    @Override
    public void onRefresh() {
        requstData(UrlUtils.viewOrder, 0);
    }


    @Override
    public void handle200Data(JSONObject dataObj, String message) {
        switch (flag) {
            case 0://获取订单数据
                //收货地址
                getAddress(dataObj);

                //订单信息
                getGoodsInfo(dataObj);

                //店铺电话
                JSONObject object = dataObj.optJSONObject("store_info");
                if (object != null) {
                    storePhone = object.optString("store_mobile");
                }
                //买家留言
                String msg = dataObj.optJSONObject("order_info").optString("order_message");
                if (StringUtils.isEmpty(msg)) {
                    msgView.setVisibility(View.GONE);
                } else {
                    msgView.setVisibility(View.VISIBLE);
                    tvMsg.setText(msg);
                }
                break;
            case 1:
                //确认收货
                getDm().buildAlertDialog(message);
                finish();
                MyOrderActivity.setCurrTab(4);
                break;
            case 2:
                String url = dataObj.optJSONObject("auction_exp").optString("exp_url");
                Intent intent = new Intent(this, WebViewBaseActivity.class);
                intent.putExtra("name", "物流信息");
                intent.putExtra("url", url);
                startActivity(intent);
                break;
        }
    }

    private void getAddress(JSONObject dataObject) {
        JSONObject addressObject = dataObject.optJSONObject("order_member_address");
        String name = addressObject.optString("reciver_name");//收货人姓名
        String phone = addressObject.optString("reciver_mobile");//手机号
        String address = addressObject.optString("reciver_address");//收货地址
        phoneTx.setText(phone);
        nameTx.setText(name);
        addressTx.setText(address);
    }


    //订单信息
    private void getGoodsInfo(JSONObject dataObject) {
        JSONObject orderObject = dataObject.optJSONObject("order_info");
        //店铺信息
        String storeName = orderObject.optString("store_name");//店铺
        store_id = orderObject.optString("store_id");
        storeTx.setText(storeName);

        //订单信息
        String shippingFee = orderObject.optString("shipping_fee");//邮费
        String orderSn = orderObject.optString("order_sn");//订单号
        String goodsAmount = orderObject.optString("goods_amount");//商品价格
        String orderAmount = orderObject.optString("order_amount");//订单价格
        String orderTime = orderObject.optString("add_time");//订单时间
        orderTime = TimeUtils.milliseconds2String(Long.parseLong(orderTime) * 1000);

        orderSnTx.setText(orderSn);
        orderTimeTx.setText(orderTime);
        orderPriceTx.setText("￥" + orderAmount);
        orderAmountTx.setText("￥" + goodsAmount);
        shippingFeeTx.setText("￥" + shippingFee);
        vouchersFeeTx.setText("-￥" + orderObject.optString("employ_coupon_money"));//礼品券立减金额

        if (goodsList.size() > 0) {
            goodsList.clear();
        }
        //商品信息
        JSONArray goodsArray = orderObject.optJSONArray("orderGoods");
        if (goodsArray != null && goodsArray.length() > 0) {
            for (int i = 0; i < goodsArray.length(); i++) {
                JSONObject goodObject = goodsArray.optJSONObject(i);
                String goodName = goodObject.optString("goods_name");//商品名
                String goodsPrice = goodObject.optString("goods_price");//商品价格
                String specNames = goodObject.optString("spec_names");//商品规格
                String goodNum = goodObject.optString("goods_num");//数量
                String goodImg = UrlUtils.baseWebsite + goodObject.optString("goods_img");//商品图片
                Goods goods = new Goods(goodImg, goodName, specNames, goodsPrice, Integer.parseInt(goodNum));
                String goods_id = goodObject.optString("goods_id");//商品id
                goods.setTitleId(Integer.parseInt(goods_id));
                goods.setDeliverNum(goodObject.optString("invoice_no"));//运单号
                goodsList.add(goods);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contact_store://联系商家
                if (!StringUtils.isEmpty(storePhone)) {
                    Intent intent1 = new Intent();
                    intent1.setAction(Intent.ACTION_DIAL);
                    intent1.setData(Uri.parse("tel:" + storePhone));
                    startActivity(intent1);
                } else {
                    showToast("没有卖家联系方式");
                }
                break;
            case R.id.btn_take://确认收货
                getDm().buildAlertDialogSure("您确定要确认收货吗？", new DialogHome.Callback() {
                    @Override
                    public void handleSure() {
                        requstData(UrlUtils.orderReceive, 1);
                    }
                });

                break;
            case R.id.btn_refund://退款
                break;
        }
    }
    @Override
    public void handle404(String message) {
    }

    @Override
    public void handleNoNetWork() {
    }
}
