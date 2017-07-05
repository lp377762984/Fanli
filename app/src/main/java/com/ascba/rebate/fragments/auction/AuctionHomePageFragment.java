package com.ascba.rebate.fragments.auction;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ascba.rebate.R;
import com.ascba.rebate.activities.auction.AuctionConfirmOrderActivity;
import com.ascba.rebate.activities.auction.AuctionDetailsActivity;
import com.ascba.rebate.activities.auction.AuctionListActivity;
import com.ascba.rebate.activities.auction.PayDepositActivity;
import com.ascba.rebate.adapter.AcutionHPAdapter;
import com.ascba.rebate.adapter.ShufflingViewPagerAdapter;
import com.ascba.rebate.beans.AcutionGoodsBean;
import com.ascba.rebate.fragments.base.BaseNetFragment;
import com.ascba.rebate.utils.UrlUtils;
import com.ascba.rebate.view.MarqueeTextView;
import com.ascba.rebate.view.loadmore.CustomLoadMoreView;
import com.ascba.rebate.view.pagerWithTurn.ShufflingViewPager;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.yanzhenjie.nohttp.rest.Request;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.chad.library.adapter.base.loadmore.LoadMoreView.STATUS_DEFAULT;

/**
 * Created by 李鹏 on 2017/5/22.
 * 拍卖首页
 */

public class AuctionHomePageFragment extends BaseNetFragment {
    private static final int REQUEST_PAY_DEPOSIT = 3;
    private static final int REQUEST_PAY_ORDER = 4;
    private AcutionHPAdapter adapter;
    private List<AcutionGoodsBean> beanList = new ArrayList<>();
    private int now_page = 1;
    private int total_page;
    private CustomLoadMoreView loadMoreView;
    private static final int LOAD_MORE_END = 0;
    private static final int LOAD_MORE_ERROR = 1;
    private static final int REDUCE_TIME = 2;
    private boolean isRefresh = true;//当前是否是下拉刷新状态
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_MORE_END:
                    if (adapter != null) {
                        adapter.loadMoreEnd(false);
                    }
                    break;
                case LOAD_MORE_ERROR:
                    if (adapter != null) {
                        adapter.loadMoreFail();
                    }
                    break;
                case REDUCE_TIME:
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    private Timer timer;
    private AcutionGoodsBean selectAGB;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auction_homepage, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        requestNetwork(UrlUtils.auction, 0);
    }

    private void requestNetwork(String url, int what) {
        if (what == 0) {
            Request<JSONObject> request = buildNetRequest(url, 0, false);
            request.add("now_page", now_page);
            executeNetWork(what, request, "请稍后");
        } else if (what == 1) {
            Request<JSONObject> request = buildNetRequest(url, 0, true);
            request.add("client_str", getAutionIds());
            request.add("total_price", selectAGB.getEndPrice());
            executeNetWork(what, request, "请稍后");
        }

    }


    private void initView(View view) {
        initRecyclerView(view);
        initRefreshLayoutView(view);
        initLoadMore();
    }

    @Override
    protected void mhandle200Data(int what, JSONObject object, JSONObject dataObj, String message) {
        if (what == 0) {
            getPageCount(dataObj);//分页
            initHeadView(dataObj);//头部数据
            stopLoadMore();
            if (isRefresh) {//下拉刷新
                clearData();
                initAuctionData(dataObj);//列表数据
            } else {//上拉加载
                initAuctionData(dataObj);//列表数据
            }

        } else if (what == 1) {

            if(selectAGB.getType()==1){
                Intent intent=new Intent(getActivity(), AuctionConfirmOrderActivity.class);
                intent.putExtra("goods_id",selectAGB.getId());
                startActivityForResult(intent,REQUEST_PAY_ORDER);
            }else {
                showToast(message);
                resetPageAndStatus();
                requestNetwork(UrlUtils.auction, 0);
            }
        }
    }

    public void resetPageAndStatus(){
        isRefresh =true;
        now_page =1;
        total_page=0;
    }

    private void stopLoadMore() {
        if (adapter != null) {
            adapter.loadMoreComplete();
        }
        if (loadMoreView != null) {
            loadMoreView.setLoadMoreStatus(STATUS_DEFAULT);
        }
    }

    private void initAuctionData(JSONObject dataObj) {
        JSONArray goodsArray = dataObj.optJSONArray("auction_goods");
        if (goodsArray != null && goodsArray.length() > 0) {
            for (int i = 0; i < goodsArray.length(); i++) {
                JSONObject obj = goodsArray.optJSONObject(i);
                AcutionGoodsBean agb = new AcutionGoodsBean(obj.optInt("id"), obj.optInt("type"), UrlUtils.baseWebsite + obj.optString("index_img"),
                        obj.optString("name"), obj.optDouble("transaction_price"),
                        obj.optString("points"), obj.optString("cash_deposit"), obj.optInt("refresh_count"));
                agb.setGapPrice(obj.optDouble("range"));
                agb.setGapTime(obj.optInt("interval_second"));
                agb.setStartPrice(obj.optDouble("begin_price"));
                agb.setEndPrice(obj.optDouble("end_price"));
                agb.setStartTime(obj.optLong("starttime"));
                agb.setEndTime(obj.optLong("endtime"));//modify
                agb.setGoodsEndTime(obj.optLong("price_time"));//add
                agb.setIntState(obj.optInt("is_status"));
                agb.setStrState(obj.optString("auction_tip"));
                agb.setCartStatusTip(obj.optString("cart_status_tip"));
                beanList.add(agb);
            }
        }
        if (beanList.size() > 0) {
            if (timer == null) {
                timer = new Timer();
                timer.schedule(new MyTimerTask(), 0, 1000);
            }
        }
        adapter.notifyDataSetChanged();
    }


    private void initRecyclerView(View view) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AcutionHPAdapter(getActivity(), R.layout.item_auction_hp, beanList);
        adapter.setCallback(new AcutionHPAdapter.Callback() {
            @Override
            public void timeToUpdate() {//时间到主动刷新数据
                resetPageAndStatus();
                requestNetwork(UrlUtils.auction, 0);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(getActivity(), AuctionDetailsActivity.class);
                intent.putExtra("agb", beanList.get(position));
                startActivity(intent);
            }

            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                super.onItemChildClick(adapter, view, position);
                selectAGB = beanList.get(position);
                switch (view.getId()) {
                    case R.id.auction_btn_get:
                        if (selectAGB.getIntState() == 2) {//交保证金
                            Intent intent = new Intent(getActivity(), PayDepositActivity.class);
                            intent.putExtra("client_ids", getClientIds(selectAGB));
                            intent.putExtra("total_price", selectAGB.getCashDeposit());
                            startActivityForResult(intent, REQUEST_PAY_DEPOSIT);
                        } else if (selectAGB.getIntState() == 4) {//立即拍
                            requestNetwork(UrlUtils.payAuction, 1);
                        } else if(selectAGB.getIntState()==6){//支付
                            Intent intent=new Intent(getActivity(),AuctionConfirmOrderActivity.class);
                            intent.putExtra("goods_id",selectAGB.getId());
                            startActivityForResult(intent,REQUEST_PAY_ORDER);
                        }

                        break;
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PAY_DEPOSIT && resultCode == Activity.RESULT_OK) {
            resetPageAndStatus();
            requestNetwork(UrlUtils.auction, 0);
        }else if(requestCode == REQUEST_PAY_ORDER ){
            resetPageAndStatus();
            requestNetwork(UrlUtils.auction, 0);
        }
    }

    private String getClientIds(AcutionGoodsBean selectAGB) {
        return "\"" +
                selectAGB.getId() +
                "\"" +
                ":" +
                "\"" +
                selectAGB.getCashDeposit() +
                "\"";
    }

    private void initHeadView(JSONObject dataObj) {
        View headView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_auction_hp_headview, null, false);
        ShufflingViewPager viewPager = (ShufflingViewPager) headView.findViewById(R.id.shufflingViewPager);
        List<String> banner = new ArrayList<>();
        JSONArray array = dataObj.optJSONArray("banner");
        if (array != null && array.length() > 0) {
            for (int i = 0; i < array.length(); i++) {
                banner.add(UrlUtils.baseWebsite + array.optString(i));
            }
            ShufflingViewPagerAdapter adapter = new ShufflingViewPagerAdapter(getActivity(), banner);
            viewPager.setAdapter(adapter);
            viewPager.start();
        } else {
            viewPager.setVisibility(View.GONE);
        }
        //消息
        JSONArray msgArray = dataObj.optJSONArray("notice_list");
        MarqueeTextView textView = (MarqueeTextView) headView.findViewById(R.id.text_auction_notif);
        View viewMsg = headView.findViewById(R.id.lat_msg);
        if (msgArray != null && msgArray.length() > 0) {
            viewMsg.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < msgArray.length(); i++) {
                sb.append(msgArray.optJSONObject(i).optString("notice"));
                sb.append("            ");
            }
            textView.setText(sb.toString());
        } else {
            viewMsg.setVisibility(View.GONE);
        }

        //抢拍
        headView.findViewById(R.id.lat_rush_auction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AuctionListActivity.class);
                intent.putExtra("type", 1);
                startActivity(intent);
            }
        });
        //盲拍
        headView.findViewById(R.id.lat_blind_auction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AuctionListActivity.class);
                intent.putExtra("type", 2);
                startActivity(intent);
            }
        });
        this.adapter.setHeaderView(headView);
    }


    private void clearData() {
        if (beanList.size() != 0) {
            beanList.clear();
        }
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            if (!isTimerOver()) {
                handler.sendEmptyMessage(REDUCE_TIME);
            }
        }
    }

    //用于判断倒计时是否结束
    private boolean isTimerOver() {
        AcutionGoodsBean agb = beanList.get(0);
        int leftTime = (int) (agb.getEndTime() - System.currentTimeMillis() / 1000);
        return leftTime <= 0;
    }

    private String getAutionIds() {
        return "\"" +
                selectAGB.getId() +
                "\"" +
                ":" +
                "\"" +
                selectAGB.getEndPrice() +
                "\"";
    }

    @Override
    protected void mhandleFailed(int what, Exception e) {
        if (what == 0) {
            handler.sendEmptyMessage(LOAD_MORE_ERROR);
        }
    }

    private void getPageCount(JSONObject dataObj) {
        total_page = dataObj.optInt("total_page");
        now_page++;
    }

    private void initLoadMore() {
        if (loadMoreView == null) {
            loadMoreView = new CustomLoadMoreView();
            adapter.setLoadMoreView(loadMoreView);
        }
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                isRefresh = false;
                if (now_page > total_page && total_page != 0) {
                    handler.sendEmptyMessage(LOAD_MORE_END);
                } else if (total_page == 0) {
                    handler.sendEmptyMessage(LOAD_MORE_END);
                } else {
                    requestNetwork(UrlUtils.auction, 0);
                }
            }
        });
    }

    private void initRefreshLayoutView(View view) {
        initRefreshLayout(view);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetPageAndStatus();
                isRefresh = true;
                requestNetwork(UrlUtils.auction, 0);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        resetPageAndStatus();
        clearData();
        isRefresh = true;
        if (timer != null) {
            timer.cancel();
        }
    }
}
