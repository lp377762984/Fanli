package com.ascba.rebate.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.ascba.rebate.R;
import com.ascba.rebate.activities.base.BaseNetActivity;
import com.ascba.rebate.adapter.ViewpagerFragmentAdapter;
import com.ascba.rebate.fragments.shop.TypeFragment;
import com.ascba.rebate.fragments.college.AllCurriculumFragment;
import com.ascba.rebate.view.ShopABar;
import com.flyco.tablayout.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 李鹏 on 2017/03/15 0015.
 * ASK商学院
 */

public class ASKCollegeActivity extends BaseNetActivity {

    private ShopABar shopABar;
    private SlidingTabLayout slidingtablayout;
    private ViewPager mViewPager;
    private List<Fragment> fragmentList = new ArrayList<>();//页卡视图集合
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_college);
        context = this;
        initView();
    }

    private void initView() {
        //导航栏
        shopABar = (ShopABar) findViewById(R.id.shopbar);
        shopABar.setImageOtherEnable(false);
        shopABar.setImMsgSta(R.mipmap.icon_person_black);
        shopABar.setCallback(new ShopABar.Callback() {
            @Override
            public void back(View v) {
                finish();
            }

            @Override
            public void clkMsg(View v) {
                /**
                 * 个人中心
                 */
                Intent intent = new Intent(context, MyEvaluationActivity.class);
                startActivity(intent);
            }

            @Override
            public void clkOther(View v) {

            }
        });

        slidingtablayout = (SlidingTabLayout) findViewById(R.id.slidingtablayout);
        mViewPager = (ViewPager) findViewById(R.id.college_rv);
        initViewpager();
    }

    private void initViewpager() {
        //全部课程
        AllCurriculumFragment allCurriculumFragment = new AllCurriculumFragment();
        fragmentList.add(allCurriculumFragment);

        TypeFragment fragment2 = new TypeFragment();
        fragmentList.add(fragment2);

        TypeFragment fragment3 = new TypeFragment();
        fragmentList.add(fragment3);

        TypeFragment fragment4 = new TypeFragment();
        fragmentList.add(fragment4);

        ViewpagerFragmentAdapter viewPager = new ViewpagerFragmentAdapter(getSupportFragmentManager(), fragmentList);
        mViewPager.setAdapter(viewPager);
        String[] title = new String[]{"全部", "初级", "中级", "高级"};
        slidingtablayout.setViewPager(mViewPager, title);

    }
}
