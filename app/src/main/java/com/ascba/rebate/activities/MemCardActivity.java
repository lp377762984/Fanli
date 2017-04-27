package com.ascba.rebate.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ascba.rebate.R;
import com.ascba.rebate.activities.base.BaseNetActivity;
import com.ascba.rebate.adapter.MemMsgAdapter;
import com.ascba.rebate.beans.MemMsg;
import com.ascba.rebate.view.ShopABarText;

import java.util.ArrayList;
import java.util.List;

public class MemCardActivity extends BaseNetActivity implements SwipeRefreshLayout.OnRefreshListener {

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private RecyclerView msgRV;
    private List<MemMsg> data;
    private MemMsgAdapter adapter;
    private ShopABarText shopBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mem_card);
        initViews();
    }

    private void initViews() {

        shopBar = (ShopABarText) findViewById(R.id.shopBar);
        shopBar.setBtnEnable(false);
        shopBar.setCallback(new ShopABarText.Callback() {
            @Override
            public void back(View v) {
                finish();
            }

            @Override
            public void clkBtn(View v) {

            }
        });

        initRefreshLayout();
        refreshLayout.setOnRefreshListener(this);

        msgRV = ((RecyclerView) findViewById(R.id.mem_msg_list));
        initData();
        adapter = new MemMsgAdapter(R.layout.mem_msg_layout, data);
        //添加头部
        View view = getLayoutInflater().inflate(R.layout.mem_msg_head_layout, null);
        adapter.setHeaderView(view);
        msgRV.setLayoutManager(new LinearLayoutManager(this));
        msgRV.setAdapter(adapter);
    }

    private void initData() {
        data = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            data.add(new MemMsg("课程名称" + i, "第三期ask初级班" + i));
        }
    }

    @Override
    public void onRefresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);
            }
        }, 1000);
    }

}
