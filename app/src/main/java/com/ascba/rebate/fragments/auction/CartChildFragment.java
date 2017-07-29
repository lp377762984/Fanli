package com.ascba.rebate.fragments.auction;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.ascba.rebate.R;
import com.ascba.rebate.activities.auction.AuctionConfirmOrderActivity;
import com.ascba.rebate.activities.auction.AuctionDetailsActivity;
import com.ascba.rebate.activities.auction.MyAuctionActivity;
import com.ascba.rebate.activities.auction.PayDepositActivity;
import com.ascba.rebate.adapter.CartChildAdapter;
import com.ascba.rebate.application.MyApplication;
import com.ascba.rebate.beans.AcutionGoodsBean;
import com.ascba.rebate.fragments.base.BaseNetFragment;
import com.ascba.rebate.utils.DialogHome;
import com.ascba.rebate.utils.NumberFormatUtils;
import com.ascba.rebate.utils.UrlUtils;
import com.ascba.rebate.utils.ViewUtils;
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
 * 竞拍购物车
 */
public class CartChildFragment extends BaseNetFragment {


    private CartChildAdapter adapter;
    private List<AcutionGoodsBean> beanList;
    private String status;
    private int now_page = 1;
    private int total_page;
    private CustomLoadMoreView loadMoreView;
    private static final int LOAD_MORE_END = 0;
    private static final int LOAD_MORE_ERROR = 1;
    private static final int REDUCE_TIME = 2;
    private static final int REQUEST_PAY_PDEPOSIT = 3;
    private static final int REQUEST_PAY_ORDER = 4;
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
                    setBeanProperty();
                    break;
            }
        }
    };
    private Timer timer;
    private boolean isRefresh = true;
    private CheckBox cbTotal;
    private TextView tvBtmTop;
    private TextView tvBtmBtm;
    private TextView tvApply;
    private View btmView;
    private AcutionGoodsBean selectAGB;
    private boolean isAuction=false;//true 需要刷新竞拍商品接口 false 不需要


    public CartChildFragment() {

    }


    public static CartChildFragment newInstance(String status) {
        CartChildFragment fragment = new CartChildFragment();
        Bundle b = new Bundle();
        b.putString("status", status);
        fragment.setArguments(b);
        return fragment;
    }

    private void setBeanProperty() {
        if (beanList.size() <= 0) {
            return;
        }
        for (int i = 0; i < beanList.size(); i++) {
            AcutionGoodsBean agb = beanList.get(i);
            int currentLeftTime = agb.getCurrentLeftTime();
            int reduceTimes = agb.getReduceTimes();
            Double price = agb.getPrice();
            if (agb.getIntState() == 1 || agb.getIntState() == 3) {
                continue;
            }
            if (currentLeftTime <= 0) {
                reduceTimes++;
                price -= agb.getGapPrice();
                currentLeftTime = agb.getGapTime();
                agb.setReduceTimes(reduceTimes);
                if (agb.getType() == 1) {
                    agb.setPrice(price);
                }
            } else {
                currentLeftTime--;
            }
            agb.setCurrentLeftTime(currentLeftTime);
        }
        caculateAllMoney();
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart_no_sure, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getParams();
        setListener();

        initViews(view);
        requestNetwork(UrlUtils.auctionCard, 0);
    }

    /**
     * 用于监听购物车是否需要刷新
     */
    private void setListener() {
        if("0,1".equals(status)){
            ((AuctionCartFragment) getParentFragment()).setListener(new AuctionCartFragment.UpdateListener() {
                @Override
                public void update() {
                    if(MyApplication.isLoadAuctionCart){
                        Log.d(TAG, "update: ");
                        resetPageAndStatus();
                        requestNetwork(UrlUtils.auctionCard, 0);
                    }
                }
            });
        }else if("2,3".equals(status)){
            AuctionCartFragment.UpdateListener listener = ((AuctionCartFragment) getParentFragment()).getListener();
            if(listener!=null){
                listener.update();
            }
        }
    }

    private void requestNetwork(String url, int what) {
        Request<JSONObject> request = buildNetRequest(url, 0, true);
        if (what == 0) {
            request.add("between_status", status);
            request.add("now_page", now_page);
        } else if (what == 1) {
            request.add("client_str", getAutionIds()[0]);
            request.add("total_price", getAutionIds()[1]);
        }

        executeNetWork(what, request, "请稍后");
    }

    private String[] getClientIds() {
        StringBuilder sb = new StringBuilder();
        Double sureMoney = 0.0;
        for (int i = 0; i < beanList.size(); i++) {
            AcutionGoodsBean agb = beanList.get(i);
            if (agb.isSelect()) {
                sb.append("\"").append(agb.getId()).append("\"").append(":").append("\"").append(agb.getCashDeposit()).append("\"").append(",");
                sureMoney += Double.parseDouble(agb.getCashDeposit());
            }
        }
        String s = sb.toString();
        if (s.endsWith(",")) {
            s = s.substring(0, s.length() - 1);
        }
        String[] ss = new String[2];
        ss[0] = s;
        ss[1] = String.valueOf(sureMoney);
        return ss;
    }

    private String[] getAutionIds() {
        StringBuilder sb = new StringBuilder();
        Double price = 0.0;
        for (int i = 0; i < beanList.size(); i++) {
            AcutionGoodsBean agb = beanList.get(i);
            if (agb.isSelect()) {
                sb.append("\"").append(agb.getId()).append("\"").append(":").append("\"").append(agb.getPrice()).append("\"").append(",");
                price += agb.getPrice();
            }
        }
        String s = sb.toString();
        if (s.endsWith(",")) {
            s = s.substring(0, s.length() - 1);
        }
        String[] ss = new String[2];
        ss[0] = s;
        ss[1] = String.valueOf(price);
        return ss;
    }

    private void getParams() {
        Bundle args = getArguments();
        if (args != null) {
            status = args.getString("status", "0,1");
        }
    }

    private void initViews(View view) {
        initMyRefreshLayout(view);
        initRecyclerview(view);
        initLoadMore();
        initBtmView(view);
    }

    private void initBtmView(View view) {
        btmView = view.findViewById(R.id.cart_clear);
        tvBtmTop = ((TextView) view.findViewById(R.id.tv_btm_top));
        tvBtmBtm = ((TextView) view.findViewById(R.id.tv_btm_btm));
        tvApply = ((TextView) view.findViewById(R.id.tv_apply));
        //点击交保证金或拍
        tvApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasSelect()) {
                    if (status.equals("0,1")) {
                        Intent intent = new Intent(getActivity(), PayDepositActivity.class);
                        intent.putExtra("client_ids", getClientIds()[0]);
                        intent.putExtra("total_price", getClientIds()[1]);
                        startActivityForResult(intent, REQUEST_PAY_PDEPOSIT);
                    } else if (status.equals("2,3")) {
                        requestNetwork(UrlUtils.payAuction, 1);
                    }
                } else {
                    showToast("请先选择竞拍商品");
                }

            }
        });
    }

    private boolean hasSelect() {
        boolean hasSelect = false;
        for (int i = 0; i < beanList.size(); i++) {
            if (beanList.get(i).isSelect()) {
                hasSelect = true;
                break;
            }
        }
        return hasSelect;
    }

    private void initRecyclerview(View view) {
        RecyclerView recyclerView = ((RecyclerView) view.findViewById(R.id.recyclerview));
        beanList = new ArrayList<>();
        cbTotal = ((CheckBox) view.findViewById(R.id.cart_cb_total));
        adapter = new CartChildAdapter(R.layout.auction_list_item, beanList, cbTotal, status);
        adapter.setEmptyView(ViewUtils.getEmptyView(getActivity(), "暂无商品数据"));
        adapter.setCallback(new CartChildAdapter.Callback() {
            @Override
            public void clickCbChild() {
                caculateMoneyAndNum();
            }

            @Override
            public void clickCbTotal() {
                caculateMoneyAndNum();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {

            }

            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                super.onItemChildClick(adapter, view, position);
                selectAGB = beanList.get(position);
                Double gapPrice = selectAGB.getGapPrice();
                Double startPrice = selectAGB.getStartPrice();
                Double endPrice = selectAGB.getEndPrice();
                Double nowPrice = selectAGB.getPrice();
                switch (view.getId()) {
                    case R.id.btn_sub:
                        if (nowPrice < endPrice + gapPrice) {
                            showToast("已经到最低价了");
                        } else {
                            selectAGB.setPrice(nowPrice - gapPrice);
                            adapter.notifyItemChanged(position);
                            caculateAllMoney();
                        }
                        break;
                    case R.id.btn_add:
                        if (nowPrice >= startPrice - gapPrice) {
                            showToast("已经到最高价了");
                        } else {
                            selectAGB.setPrice(nowPrice + gapPrice);
                            adapter.notifyItemChanged(position);
                            caculateAllMoney();
                        }
                        break;
                    case R.id.lat_see_details:
                        AcutionGoodsBean agb = beanList.get(position);
                        AuctionDetailsActivity.startIntent(getActivity(), agb.getId());
                        break;
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PAY_PDEPOSIT && resultCode == Activity.RESULT_OK) {
            resetPageAndStatus();
            requestNetwork(UrlUtils.auctionCard, 0);
            AuctionCartFragment parentFragment = (AuctionCartFragment) getParentFragment();
            ((CartChildFragment) parentFragment.getFragments().get(1)).isAuction=true;
            parentFragment.setTabSelect(1);
        }else if(requestCode == REQUEST_PAY_ORDER){
            resetPageAndStatus();
            requestNetwork(UrlUtils.auctionCard, 0);
        }
    }

    /**
     * 从待交保证金切换到竞拍商品，刷新接口
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && isAuction && status.equals("2,3")){
            resetPageAndStatus();
            requestNetwork(UrlUtils.auctionCard, 0);
            isAuction=false;
        }
    }

    private void initMyRefreshLayout(View view) {
        initRefreshLayout(view);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefresh = true;
                resetPageAndStatus();
                requestNetwork(UrlUtils.auctionCard, 0);
            }
        });
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
                    requestNetwork(UrlUtils.auctionCard, 0);
                }
            }
        });
    }

    @Override
    protected void mhandle200Data(int what, JSONObject object, JSONObject dataObj, String message) {
        if (what == 0) {
            stopLoadMore();
            if (isRefresh) {//下拉刷新
                clearData();
            }
            getPageCount(dataObj);
            refreshGoodsList(dataObj);
            setCbTotal();
            caculateMoneyAndNum();
        } else if (what == 1) {
            if(hasRushAuction()){
                Intent intent=new Intent(getActivity(), AuctionConfirmOrderActivity.class);
                startActivityForResult(intent,REQUEST_PAY_ORDER);
            }else {
                int pay_type = dataObj.optInt("pay_type");
                if(pay_type==2){
                    getDm().buildAlertDialogSure(dataObj.optString("pay_type_msg"), new DialogHome.Callback() {
                        @Override
                        public void handleSure() {
                            Intent intent=new Intent(getActivity(), MyAuctionActivity.class);
                            startActivity(intent);
                        }
                    });
                }
                resetPageAndStatus();
                requestNetwork(UrlUtils.auctionCard, 0);
            }
        }
    }

    private boolean hasRushAuction() {
        boolean has=false;
        for (int i = 0; i < beanList.size(); i++) {
            AcutionGoodsBean agb = beanList.get(i);
            if(agb.isSelect() && agb.getType()==1){
                has=true;
                break;
            }

        }
        return has;
    }

    private void resetPageAndStatus() {
        isRefresh = true;
        now_page = 1;
        total_page = 0;
    }

    private void setCbTotal() {
        if (beanList.size() <= 0) {
            return;
        }
        boolean isAllSame = true;
        for (int i = 0; i < beanList.size(); i++) {
            AcutionGoodsBean agb = beanList.get(i);
            if (agb.isSelect() != cbTotal.isChecked()) {
                isAllSame = false;
                break;
            }
        }
        if (isAllSame) {
            cbTotal.setChecked(beanList.get(0).isSelect());
        } else {
            cbTotal.setChecked(false);
        }
    }

    @Override
    protected void mhandleFailed(int what, Exception e) {
        if (what == 0) {
            handler.sendEmptyMessage(LOAD_MORE_ERROR);
        }
    }

    private void refreshGoodsList(JSONObject dataObj) {
        JSONObject agent = dataObj.optJSONObject("agent");
        JSONArray array = agent.optJSONArray("auction_cart_list");
        if (array != null && array.length() > 0) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                AcutionGoodsBean agb = new AcutionGoodsBean(obj.optInt("id"), obj.optInt("type"), UrlUtils.getNewUrl(obj.optString("imghead")),
                        obj.optString("name"), obj.optDouble("transaction_price"),
                        obj.optString("points"), obj.optString("cash_deposit"), obj.optInt("refresh_count"));
                agb.setGapPrice(obj.optDouble("range"));
                agb.setMaxReduceTimes(obj.optInt("depreciate_count"));
                agb.setCurrentLeftTime(obj.optInt("count_down"));
                agb.setGapTime(obj.optInt("interval_second"));
                agb.setIntState(obj.optInt("is_status"));
                agb.setStrState(obj.optString("auction_tip"));
                agb.setStartPrice(obj.optDouble("begin_price"));
                agb.setEndPrice(obj.optDouble("end_price"));
                beanList.add(agb);
            }
        }
        if (beanList.size() <= 0) {
            btmView.setVisibility(View.GONE);
        } else {
            btmView.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new MyTimerTask(), 0, 1000);
        }
    }

    private void getPageCount(JSONObject dataObj) {
        total_page = dataObj.optInt("total_page");
        now_page++;
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
        boolean isOver = true;
        for (int i = 0; i < beanList.size(); i++) {
            AcutionGoodsBean agb = beanList.get(i);
            int reduceTimes = agb.getReduceTimes();
            int maxReduceTimes = agb.getMaxReduceTimes();
            if (reduceTimes < maxReduceTimes) {
                isOver = false;
                break;
            }
        }
        return isOver;
    }

    //计算保证金总数和选择的商品数量
    private void caculateMoneyAndNum() {
        if (beanList.size() <= 0) {
            return;
        }
        int count = 0;
        Double money = 0.00;
        Double price = 0.00;
        int score = 0;
        for (int i = 0; i < beanList.size(); i++) {
            AcutionGoodsBean agb = beanList.get(i);
            if (agb.isSelect()) {
                count++;
                price += agb.getPrice();
                String cashDeposit = agb.getCashDeposit();
                String scoreS = agb.getScore();
                try {
                    double v = Double.parseDouble(cashDeposit);
                    double f = Double.parseDouble(scoreS);
                    money += v;
                    score += f;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        if (status.equals("0,1")) {//交纳保证金
            tvBtmTop.setText("￥" + NumberFormatUtils.getNewDouble(money));
            tvBtmBtm.setText("（未拍到保证金全额退款）");
            tvApply.setText("交保证金(" + count + ")");
        } else if (status.equals("2,3")) {//拍
            setBtmTop(price);
            tvBtmBtm.setText("礼品分：" + score );
            tvApply.setText("拍(" + count + ")");
        }
    }

    private void setBtmTop(Double money) {
        SpannableString ss=new SpannableString("总金额：￥" + NumberFormatUtils.getNewDouble(money));
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.main_text_normal)),0,4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvBtmTop.setText(ss);
    }

    //计算每秒过后总金额的变化
    private void caculateAllMoney() {
        Double price = 0.00;
        for (int i = 0; i < beanList.size(); i++) {
            AcutionGoodsBean agb = beanList.get(i);
            if (agb.isSelect()) {
                if ("0,1".equals(status)) {
                    price += Double.parseDouble(agb.getCashDeposit());
                } else if ("2,3".equals(status)) {
                    price += agb.getPrice();
                }
            }
        }
        if ("0,1".equals(status)) {
            tvBtmTop.setText("￥" + price);
        } else if ("2,3".equals(status)) {
            setBtmTop(price);
        }


    }

    private void clearData() {
        if (beanList.size() != 0) {
            beanList.clear();
        }
    }

    private void stopLoadMore() {
        if (adapter != null) {
            adapter.loadMoreComplete();
        }
        if (loadMoreView != null) {
            loadMoreView.setLoadMoreStatus(STATUS_DEFAULT);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}
