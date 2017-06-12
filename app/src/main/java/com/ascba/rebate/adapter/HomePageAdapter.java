package com.ascba.rebate.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.ascba.rebate.R;
import com.ascba.rebate.activities.PlayVideoActivity;
import com.ascba.rebate.beans.HomePageMultiItemItem;
import com.ascba.rebate.view.pagerWithTurn.ShufflingVideoPager;
import com.ascba.rebate.view.pagerWithTurn.ShufflingViewPager;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by 李鹏 on 2017/03/11 0011.
 */

public class HomePageAdapter extends BaseMultiItemQuickAdapter<HomePageMultiItemItem, BaseViewHolder> {

    private Context context;

    public HomePageAdapter(List<HomePageMultiItemItem> data, Context context) {
        super(data);
        this.context = context;
        for (int i = 0; i < data.size(); i++) {
            HomePageMultiItemItem item = data.get(i);
            addItemType(item.getItemType(), item.getLayout());
        }
    }


    @Override
    protected void convert(BaseViewHolder helper, HomePageMultiItemItem item) {
        switch (helper.getItemViewType()) {
            case HomePageMultiItemItem.TYPE1:
                /**
                 * 新闻轮播
                 */
                ShufflingViewPager viewPager = helper.getView(R.id.homepage_pager);
                ShufflingViewPagerAdapter adapter = new ShufflingViewPagerAdapter(context, item.getList());
                viewPager.setAdapter(adapter);
                if (item.getList().size() != 1) {
                    viewPager.start();
                }
                break;
            case HomePageMultiItemItem.TYPE2:
                /**
                 * 赚钱花钱
                 */
                helper.addOnClickListener(R.id.homepage_btn_speedmon);
                helper.addOnClickListener(R.id.homepage_btn_makemon);
                break;
            case HomePageMultiItemItem.TYPE3:
                /**
                 * ASK商学院、创业扶持
                 */
                helper.addOnClickListener(R.id.homepage_btn_college);

                helper.addOnClickListener(R.id.homepage_btn_policy);
                break;

            case HomePageMultiItemItem.TYPE5:
                /**
                 * 券购商城
                 */
                helper.addOnClickListener(R.id.homepage_text_more_shop);
                break;

            case HomePageMultiItemItem.TYPE6:
                /**
                 * 全球券购
                 */
                helper.addOnClickListener(R.id.homepage_btn_global);
                ImageView btnGlobalImg = helper.getView(R.id.homepage_btn_global_img);
                Picasso.with(context).load("http://image18-c.poco.cn/mypoco/myphoto/20170309/11/18505011120170309114124067_640.jpg").placeholder(R.mipmap.busi_loading).error(R.mipmap.busi_loading).into(btnGlobalImg);

                /**
                 * 天天特价
                 */

                helper.addOnClickListener(R.id.homepage_btn_offer);
                ImageView btnOfferImg = helper.getView(R.id.homepage_btn_offer_img);
                Picasso.with(context).load("http://image18-c.poco.cn/mypoco/myphoto/20170309/11/18505011120170309114124067_640.jpg").placeholder(R.mipmap.busi_loading).error(R.mipmap.busi_loading).into(btnOfferImg);

                /**
                 * 品牌精选
                 */
                helper.addOnClickListener(R.id.homepage_btn_selected);
                ImageView btnSelectedImg = helper.getView(R.id.homepage_btn_selected_img);
                Picasso.with(context).load("http://image18-c.poco.cn/mypoco/myphoto/20170309/11/18505011120170309114124067_640.jpg").placeholder(R.mipmap.busi_loading).error(R.mipmap.busi_loading).into(btnSelectedImg);
                break;

            case HomePageMultiItemItem.TYPE8:
                /**
                 * ASK资讯
                 */
                helper.setText(R.id.homepage_text_title, item.getTitle());
                break;

            case HomePageMultiItemItem.TYPE9:
                /**
                 * 视频
                 */
                initVideo(helper, item);

                break;
            case HomePageMultiItemItem.TYPE10:
                /**
                 * 最新动态
                 */
                helper.setText(R.id.homepage_text_title, item.getTitle());
                helper.addOnClickListener(R.id.homepage_text_more_news);
                break;

            /**
             * 新闻
             */
            case HomePageMultiItemItem.TYPE11:
                helper.setText(R.id.item_news_head_text1, item.getNewsBeen().get(0).getTitle());
                ImageView img1 = helper.getView(R.id.item_news_head_img1);
                Picasso.with(context).load(item.getNewsBeen().get(0).getImg()).placeholder(R.mipmap.loading_rect).error(R.mipmap.loading_rect).into(img1);
                helper.addOnClickListener(R.id.item_news_head_rl1);

                helper.setText(R.id.item_news_head_text2, item.getNewsBeen().get(1).getTitle());
                ImageView img2 = helper.getView(R.id.item_news_head_img2);
                Picasso.with(context).load(item.getNewsBeen().get(1).getImg()).placeholder(R.mipmap.loading_rect).error(R.mipmap.loading_rect).into(img2);
                helper.addOnClickListener(R.id.item_news_head_rl2);

                helper.setText(R.id.item_news_head_text3, item.getNewsBeen().get(2).getTitle());
                ImageView img3 = helper.getView(R.id.item_news_head_img3);
                Picasso.with(context).load(item.getNewsBeen().get(2).getImg()).placeholder(R.mipmap.loading_rect).error(R.mipmap.loading_rect).into(img3);
                helper.addOnClickListener(R.id.item_news_head_rl3);

                helper.setText(R.id.item_news_head_text4, item.getNewsBeen().get(3).getTitle());
                ImageView img4 = helper.getView(R.id.item_news_head_img4);
                Picasso.with(context).load(item.getNewsBeen().get(3).getImg()).placeholder(R.mipmap.loading_rect).error(R.mipmap.loading_rect).into(img4);
                helper.addOnClickListener(R.id.item_news_head_rl4);

                helper.setText(R.id.item_news_head_text5, item.getNewsBeen().get(4).getTitle());
                ImageView img5 = helper.getView(R.id.item_news_head_img5);
                Picasso.with(context).load(item.getNewsBeen().get(4).getImg()).placeholder(R.mipmap.loading_rect).error(R.mipmap.loading_rect).into(img5);
                helper.addOnClickListener(R.id.item_news_head_rl5);
                break;

            case HomePageMultiItemItem.TYPE12:
                ImageView imageView = helper.getView(R.id.item_news_icon);
                if (item.getBean().isIcon()) {
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    imageView.setVisibility(View.INVISIBLE);
                }
                helper.setText(R.id.item_news_title, item.getBean().getTitle());
                helper.setText(R.id.item_news_time, item.getBean().getTime());
                break;
        }
    }

    /**
     * 初始化视频播放
     */
    private void initVideo(BaseViewHolder helper, final HomePageMultiItemItem item) {
        ShufflingVideoPager videoPager = helper.getView(R.id.homepage_pager);

        ShufflingVideoAdapter adapter = new ShufflingVideoAdapter(context, item.getVideoList());
        videoPager.setAdapter(adapter);
        if (item.getVideoList().size() > 1) {
            videoPager.start();
        }
        adapter.addOnClick(new ShufflingVideoAdapter.OnClick() {
            @Override
            public void OnClick(int position) {
                PlayVideoActivity.newIndexIntent(context, item.getVideoList().get(position).getVideoUrl());
            }
        });
    }
}
