package com.ascba.rebate.fragments.shop.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ascba.rebate.R;
import com.ascba.rebate.activities.GoodsDetailsActivity;
import com.ascba.rebate.activities.MyOrderActivity;
import com.ascba.rebate.activities.shop.order.DeliverDetailsActivity;
import com.ascba.rebate.activities.shop.order.EvaluateDetailsActivity;
import com.ascba.rebate.activities.shop.order.PayDetailsActivity;
import com.ascba.rebate.activities.shop.order.TakeDetailsActivity;
import com.ascba.rebate.adapter.order.AllOrderAdapter;
import com.ascba.rebate.beans.Goods;
import com.ascba.rebate.beans.OrderBean;
import com.ascba.rebate.fragments.base.BaseNetFragment;
import com.ascba.rebate.fragments.base.LazyLoadFragment;
import com.ascba.rebate.utils.DialogHome;
import com.ascba.rebate.utils.PayUtils;
import com.ascba.rebate.utils.StringUtils;
import com.ascba.rebate.utils.TimeUtils;
import com.ascba.rebate.utils.UrlUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.yanzhenjie.nohttp.rest.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 李鹏 on 2017/03/14 0014.
 * 全部订单
 */

public class AllOrderFragment extends LazyLoadFragment implements BaseNetFragment.Callback {

    private RecyclerView recyclerView;
    private Context context;

    /**
     * 每笔订单中的商品列表
     */
    private List<OrderBean> beanArrayList = new ArrayList<>();
    private AllOrderAdapter adapter;
    private View view;
    private String orderStatus;//订单状态：0(已取消)10(默认):未付款;20:已付款;30:已发货;40:已收货;
    private String orderId;//订单id
    private int flag = 0;//0——获取数据，1——取消订单,2——删除订单
    private View emptyView;
    private String balance;//账户余额
    private PayUtils pay;
    private String payType;

    @Override
    protected int setContentView() {
        return R.layout.fragment_orders;
    }


    @Override
    protected void lazyLoad() {
        requstListData();
    }

    @Override
    protected void stopLoad() {
        cancelNetWork();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getActivity();
        this.view = view;
    }


    /*
      获取列表数据
    */
    private void requstListData() {
        flag = 0;
        Request<JSONObject> jsonRequest = buildNetRequest(UrlUtils.getOrderList, 0, true);
        jsonRequest.add("status", "all");
        executeNetWork(jsonRequest, "请稍后");
        setCallback(this);
    }


    private void requstData(int flag, String url, String order_id) {
        this.flag = flag;
        Request<JSONObject> jsonRequest = null;
        jsonRequest = buildNetRequest(url, 0, true);
        jsonRequest.add("order_id", order_id);
        switch (flag) {
            case 3:
                //付款
                jsonRequest.add("pay_type", payType);
                break;
        }
        executeNetWork(jsonRequest, "请稍后");
        setCallback(this);
    }


    /*
    初始化数据
     */
    private void initData(JSONObject dataObj) {
        if (beanArrayList.size() > 0) {
            beanArrayList.clear();
        }

        //用户信息
        JSONObject member_info = dataObj.optJSONObject("member_info");
        if (member_info != null) {
            balance = member_info.optString("money");//余额
            int white_score = member_info.optInt("white_score");
            int red_score = member_info.optInt("red_score");
        }

        //商品信息
        JSONArray jsonArray = dataObj.optJSONArray("order_list");
        if (jsonArray != null && jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                int totalNum = 0;//购买商品数量
                JSONObject object = jsonArray.optJSONObject(i);

                //订单id
                orderId = object.optString("order_id").trim();
                //订单状态
                orderStatus = object.optString("order_status").trim();

                //头部信息
                String time = object.optString("add_time");//时间
                time = TimeUtils.milliseconds2String((Long.parseLong(time) * 1000));
                OrderBean beanHead = new OrderBean(AllOrderAdapter.TYPE_Head, R.layout.item_order_head, time);
                beanHead.setId(orderId);
                beanHead.setStateCode(orderStatus);

                if (orderStatus.equals("10")) {
                    //等待卖家付款
                    beanHead.setState("等待买家付款");
                } else if (orderStatus.equals("0")) {
                    //交易关闭
                    beanHead.setState("交易关闭");
                } else if (orderStatus.equals("20")) {
                    //等待卖家发货
                    beanHead.setState("等待卖家发货");
                } else if (orderStatus.equals("30")) {
                    //等待买家收货
                    beanHead.setState("等待买家收货");
                } else if (orderStatus.equals("40")) {
                    //交易成功
                    beanHead.setState("交易成功");
                }
                beanArrayList.add(beanHead);

                //商品信息
                JSONArray goodsArray = object.optJSONArray("orderGoods");

                if (goodsArray != null && goodsArray.length() > 0) {

                    for (int j = 0; j < goodsArray.length(); j++) {
                        try {
                            JSONObject goodsObject = goodsArray.getJSONObject(j);
                            Goods good = new Goods();
                            good.setTitleId(Integer.parseInt(goodsObject.optString("id")));//商品id
                            good.setImgUrl(UrlUtils.baseWebsite + goodsObject.optString("goods_img"));//图片
                            good.setGoodsTitle(goodsObject.optString("goods_name"));//商品名
                            good.setTitleId(Integer.parseInt(goodsObject.optString("goods_id")));

                            int num = Integer.parseInt(String.valueOf(goodsObject.opt("goods_num")));
                            totalNum = num + totalNum;

                            good.setUserQuy(num);//购买数量
                            good.setGoodsPrice(goodsObject.optString("goods_pay_price"));//付款价格
                            good.setGoodsPriceOld(goodsObject.optString("goods_price"));//原价

                            OrderBean orderBean = new OrderBean(AllOrderAdapter.TYPE_GOODS, R.layout.item_goods, good);
                            orderBean.setId(orderId);
                            orderBean.setStateCode(orderStatus);
                            beanArrayList.add(orderBean);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //底部信息
                String orderAmount = object.optString("order_amount");//订单总价
                String shippingFee = "(含" + object.optString("shipping_fee") + "元运费)";//运费
                String goodsNum = "共" + totalNum + "件商品";//商品数量

                OrderBean beadFoot = null;
                if (orderStatus.equals("10")) {
                    //等待卖家付款
                    beadFoot = new OrderBean(AllOrderAdapter.TYPE1, R.layout.item_order_pay1_foot, goodsNum, "￥" + orderAmount, shippingFee);
                } else if (orderStatus.equals("0")) {
                    //交易关闭
                    beadFoot = new OrderBean(AllOrderAdapter.TYPE2, R.layout.item_order_pay2_foot, goodsNum, "￥" + orderAmount, shippingFee);
                } else if (orderStatus.equals("20")) {
                    //等待卖家发货
                    beadFoot = new OrderBean(AllOrderAdapter.TYPE3, R.layout.item_order_deliver_foot, goodsNum, "￥" + orderAmount, shippingFee);
                } else if (orderStatus.equals("30")) {
                    //等待买家收货
                    beadFoot = new OrderBean(AllOrderAdapter.TYPE4, R.layout.item_order_take_foot, goodsNum, "￥" + orderAmount, shippingFee);
                } else if (orderStatus.equals("40")) {
                    //交易成功
                    beadFoot = new OrderBean(AllOrderAdapter.TYPE5, R.layout.item_order_evaluate_foot, goodsNum, "￥" + orderAmount, shippingFee);
                }
                beadFoot.setId(orderId);
                beadFoot.setStateCode(orderStatus);
                beanArrayList.add(beadFoot);
            }
        }

        if (adapter == null) {
            initRecylerView();
        } else {
            adapter = new AllOrderAdapter(beanArrayList, context);
            recyclerView.setAdapter(adapter);
        }

        if (beanArrayList.size() > 0) {
            emptyView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void initRecylerView() {

        recyclerView = (RecyclerView) view.findViewById(R.id.list_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        adapter = new AllOrderAdapter(beanArrayList, context);
        recyclerView.setAdapter(adapter);

        emptyView = view.findViewById(R.id.empty_view);

        recyclerView.addOnItemTouchListener(new OnItemChildClickListener() {
            @Override
            public void onSimpleItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                final String orderId = beanArrayList.get(position).getId();
                switch (view.getId()) {
                    case R.id.item_goods_rl:
                        if (orderId != null) {
                            Intent intent = new Intent();
                            //点击商品查看订单详情
                            String orderStatus = beanArrayList.get(position).getStateCode();
                            if (orderStatus.equals("10")) {
                                //等待卖家付款
                                intent.setClass(context, PayDetailsActivity.class);
                            } else if (orderStatus.equals("0")) {
                                //交易关闭,查看商品详情
                                intent = null;
                                int goodId = beanArrayList.get(position).getGoods().getTitleId();
                                GoodsDetailsActivity.startIntent(getActivity(), goodId);
                            } else if (orderStatus.equals("20")) {
                                //等待卖家发货
                                intent.setClass(context, DeliverDetailsActivity.class);
                            } else if (orderStatus.equals("30")) {
                                //等待买家收货
                                intent.setClass(context, TakeDetailsActivity.class);
                            } else if (orderStatus.equals("40")) {
                                //交易成功
                                intent.setClass(context, EvaluateDetailsActivity.class);
                            }

                            if (intent != null) {
                                intent.putExtra("order_id", orderId);
                                startActivityForResult(intent, 1);
                            }
                        }
                        break;
                    case R.id.item_goods_order_total_pay:
                        //付款
                        payPrice(position,orderId);
                        break;
                    case R.id.item_goods_order_total_cancel:
                        //取消订单
                        getDm().buildAlertDialogSure("您确定要取消订单吗？", new DialogHome.Callback() {
                            @Override
                            public void handleSure() {
                                requstData(1, UrlUtils.cancelOrder, orderId);
                            }
                        });
                        //requstData(1, UrlUtils.cancelOrder, orderId);
                        break;
                    case R.id.item_goods_order_total_call:
                        //联系卖家
                        break;
                    case R.id.item_goods_order_total_delete:
                        //删除订单
                        getDm().buildAlertDialogSure("您确定要删除订单吗？", new DialogHome.Callback() {
                            @Override
                            public void handleSure() {
                                requstData(2, UrlUtils.delOrder, orderId);
                            }
                        });
                        //requstData(2, UrlUtils.delOrder, orderId);
                        break;
                    case R.id.item_goods_order_total_refund:
                        //退款
                        break;

                    case R.id.item_goods_order_total_logistics:
                        //查看物流
                        break;
                    case R.id.item_goods_order_total_take:
                        //确认收货
                        break;
                    case R.id.item_goods_order_total_after:
                        //售后
                        break;
                    case R.id.item_goods_order_total_evalute:
                        //评价
                        break;
                }
            }
        });
    }

    @Override
    public void handle200Data(JSONObject dataObj, String message) {
        switch (flag) {
            case 0:
                //获取数据
                initData(dataObj);
                break;
            case 1:
                //取消订单,成功后刷新数据
                requstListData();
                break;
            case 2:
                //删除订单,成功后刷新数据
                requstListData();
                break;
            case 3:
                try {
                    if ("balance".equals(payType)) {
                        //余额支付
                        pay.dismissDialog();
                        pay.requestForYuE(dataObj);
                    } else if ("alipay".equals(payType)) {
                        String payInfo = dataObj.optString("payInfo");
                        pay.requestForAli(payInfo);//发起支付宝支付请求
                    } else if ("wxpay".equals(payType)) {
                        JSONObject wxpay = dataObj.getJSONObject("wxpay");
                        pay.requestForWX(wxpay);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void handleReqFailed() {
    }

    @Override
    public void handle404(String message, JSONObject dataObj) {
        getDm().buildAlertDialog(message);
    }


    @Override
    public void handleReLogin() {

    }

    @Override
    public void handleNoNetWork() {
        getDm().buildAlertDialog(getString(R.string.no_network));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            requstListData();
        }
    }

    /*
      付款
   */
    private void payPrice(int position, final String orderId) {
        String price = beanArrayList.get(position).getOrderPrice();
        if (StringUtils.isEmpty(price)) {
            showToast("正在加载订单信息，请稍后");
        } else {
            //开启支付
            pay = new PayUtils(getActivity(), price, balance);
            pay.showDialog(new PayUtils.OnCreatOrder() {
                @Override
                public void onCreatOrder(String arg) {
                    payType = arg;
                    requstData(3, UrlUtils.orderPay, orderId);
                }
            });

            //支付回调
            pay.setPayCallBack(new PayUtils.onPayCallBack() {
                @Override
                public void onFinish(String payStype) {
                    //刷新数据
                    requstListData();
                }

                @Override
                public void onSuccess(String payStype, String resultStatus) {
                    MyOrderActivity.setCurrTab(2);
                    showToast("成功支付");
                }

                @Override
                public void onCancel(String payStype, String resultStatus) {
                    showToast("取消支付");
                }

                @Override
                public void onFailed(String payStype, String resultStatus) {
                    showToast("支付失败");
                }

                @Override
                public void onNetProblem(String payStype, String resultStatus) {
                    showToast("支付失败");
                }
            });
        }
    }
}