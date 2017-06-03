package com.ascba.rebate.fragments.shop.auction;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ascba.rebate.R;
import com.ascba.rebate.activities.shop.auction.BlindShootActivity;
import com.ascba.rebate.activities.shop.auction.GrabShootActivity;
import com.ascba.rebate.adapter.AuctionMainPlaceChildAdapter;
import com.ascba.rebate.beans.AcutionGoodsBean;
import com.ascba.rebate.beans.TittleBean;
import com.ascba.rebate.fragments.base.BaseNetFragment;
import com.ascba.rebate.utils.UrlUtils;
import com.ascba.rebate.view.loadmore.CustomLoadMoreView;
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
 * Created by 李鹏 on 2017/5/24.
 * 主会场——盲拍列表
 */

public class AuctionMainPlaceChildFragment extends BaseNetFragment {

    private List<AcutionGoodsBean> beanList = new ArrayList<>();
    private AuctionMainPlaceChildAdapter adapter;
    private int type = 1;
    private TittleBean tb;
    private int now_page = 1;
    private int total_page;
    private CustomLoadMoreView loadMoreView;
    private static final int LOAD_MORE_END = 0;
    private static final int LOAD_MORE_ERROR = 1;
    private static final int REDUCE_TIME = 2;
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
                    if(beanList.size()==0){
                        return;
                    }
                    setBeanProperty();
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    private Timer timer ;
    private boolean isRefresh=true;

    public static AuctionMainPlaceChildFragment newInstance(int type, TittleBean tb) {
        Bundle b = new Bundle();
        b.putInt("type", type);
        b.putParcelable("title_bean", tb);
        AuctionMainPlaceChildFragment fragment = new AuctionMainPlaceChildFragment();
        fragment.setArguments(b);
        return fragment;
    }
    private void setBeanProperty(){
        for (int i = 0; i < beanList.size(); i++) {
            AcutionGoodsBean agb = beanList.get(i);
            int currentLeftTime = agb.getCurrentLeftTime();
            int reduceTimes = agb.getReduceTimes();
            int maxReduceTimes = agb.getMaxReduceTimes();
            Double price = agb.getPrice();
            if(reduceTimes >= maxReduceTimes ){
                return;
            }
            currentLeftTime--;
            if(currentLeftTime <=0){
                reduceTimes++;
                price -= agb.getGapPrice();
                currentLeftTime=agb.getGapTime();
                agb.setReduceTimes(reduceTimes);
                agb.setPrice(price);
            }
            agb.setCurrentLeftTime(currentLeftTime);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auction_main_place_child, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        getParams();
        requestNetwork(UrlUtils.auctionType, 0);
    }

    private void getParams() {
        Bundle b = getArguments();
        if (b != null) {
            this.type = b.getInt("type");
            this.tb = b.getParcelable("title_bean");
        }
    }


    private void requestNetwork(String url, int what) {
        Request<JSONObject> request = buildNetRequest(url, 0, false);
        request.add("type", type);
        request.add("strat_time", tb.getStartTime());
        request.add("end_time", tb.getEndTime());
        request.add("now_page", now_page);
        executeNetWork(what, request, "请稍后");
    }



    @Override
    protected void mhandle200Data(int what, JSONObject object, JSONObject dataObj, String message) {
        stopLoadMore();
        if(isRefresh){//下拉刷新
            clearData();
        }
        getPageCount(dataObj);
        JSONArray array = dataObj.optJSONArray("auction_list");
        if (array != null && array.length() > 0) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                AcutionGoodsBean agb = new AcutionGoodsBean(obj.optInt("id"), obj.optInt("type"), obj.optString("imghead"),
                        obj.optString("name"), obj.optDouble("transaction_price"),
                        obj.optString("points"), obj.optString("cash_deposit"), obj.optInt("refresh_count"));
                agb.setState(tb.getStatus());
                agb.setGapPrice(obj.optDouble("range"));
                agb.setMaxReduceTimes(obj.optInt("depreciate_count"));
                agb.setCurrentLeftTime(obj.optInt("count_down"));
                agb.setGapTime(obj.optInt("interval_second"));
                agb.setIntState(obj.optInt("is_status"));
                agb.setStrState(obj.optString("auction_tip"));
                beanList.add(agb);
            }
        }
        adapter.notifyDataSetChanged();
    }


    @Override
    protected void mhandleFailed(int what, Exception e) {
        handler.sendEmptyMessage(LOAD_MORE_ERROR);
    }

    private void getPageCount(JSONObject dataObj) {
        total_page = dataObj.optInt("total_page");
        now_page++;
    }

    private void initView(View view) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        if (type == 1) {
            adapter = new AuctionMainPlaceChildAdapter(getActivity(), R.layout.item_auction_goods2, beanList);
        } else if (type == 2) {
            adapter = new AuctionMainPlaceChildAdapter(getActivity(), R.layout.item_auction_goods, beanList);
        }
        recyclerView.setAdapter(adapter);
        timer = new Timer();
        timer.schedule(new MyTimerTask(),0,1000);
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                switch (position) {
                    case 0:
                        BlindShootActivity.startIntent(getActivity(), 0);
                        break;
                    case 1:
                        BlindShootActivity.startIntent(getActivity(), 1);
                        break;
                    case 2:
                        BlindShootActivity.startIntent(getActivity(), 2);
                        break;
                    case 3:
                        GrabShootActivity.startIntent(getActivity(), 0);
                        break;
                    case 4:
                        GrabShootActivity.startIntent(getActivity(), 1);
                        break;
                    case 5:
                        GrabShootActivity.startIntent(getActivity(), 2);
                        break;
                }

            }

            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

                switch (view.getId()) {
                    case R.id.btn_sub:
                        showToast("减号-->" + position);
                        break;
                    case R.id.btn_add:
                        showToast("加号-->" + position);
                        break;
                    case R.id.btn_auction_goods_add_cart:
                        showToast("加入购物车-->" + position);
                        break;
                    case R.id.btn_auction_goods_apply:
                        showToast("立即报名-->" + position);
                        break;
                }

            }
        });

        initRefreshLayout(view);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefresh=true;
                resetPage();
                requestNetwork(UrlUtils.auctionType, 0);
            }
        });

        initLoadMore();
    }

    private void initLoadMore() {
        if (loadMoreView == null) {
            loadMoreView = new CustomLoadMoreView();
            adapter.setLoadMoreView(loadMoreView);
        }
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                isRefresh=false;
                if (now_page > total_page && total_page != 0) {
                    handler.sendEmptyMessage(LOAD_MORE_END);
                } else if(total_page==0){
                    handler.sendEmptyMessage(LOAD_MORE_END);
                } else {
                    requestNetwork(UrlUtils.auction,0);
                }
            }
        });
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            if(!isTimerOver()){
                handler.sendEmptyMessage(REDUCE_TIME);
            }
        }
    }

    private void clearData(){
        if(beanList.size()!=0){
            beanList.clear();
        }
    }

    private void resetPage(){
        now_page=1;
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
    //用于判断倒计时是否结束
    private boolean isTimerOver(){
        boolean isOver=true;
        for (int i = 0; i < beanList.size(); i++) {
            AcutionGoodsBean agb = beanList.get(i);
            int reduceTimes = agb.getReduceTimes();
            int maxReduceTimes = agb.getMaxReduceTimes();
            if(reduceTimes < maxReduceTimes ){
                isOver=false;
                break;
            }
        }
        return isOver;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        resetPage();
        clearData();
        isRefresh=true;
        if(timer!=null){
            timer.cancel();
        }
    }
}
