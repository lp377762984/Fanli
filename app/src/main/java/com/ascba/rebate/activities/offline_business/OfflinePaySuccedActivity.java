package com.ascba.rebate.activities.offline_business;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ascba.rebate.R;
import com.ascba.rebate.activities.base.BaseNetActivity;
import com.ascba.rebate.utils.TimeUtils;
import com.ascba.rebate.utils.UrlUtils;
import com.ascba.rebate.view.RoundImageView;
import com.squareup.picasso.Picasso;

public class OfflinePaySuccedActivity extends BaseNetActivity {
    private RoundImageView userIcon;
    private TextView tvName, tvCost, tvPayType, tvAccount,
            tvScore, tvTime, tvTradeNumber, tv_contact_way,order_status_text,seller_name1;
    private Button pay_succed_close;
    private RelativeLayout pay_succed_waytocontact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_pay_succed);
        initViews();
        setData();
    }


    private void initViews() {
        pay_succed_waytocontact= (RelativeLayout) findViewById(R.id.pay_succed_waytocontact);
        seller_name1= (TextView) findViewById(R.id.pay_succed_member_username);
        userIcon = ((RoundImageView) findViewById(R.id.pay_succed_icon));
        order_status_text= (TextView) findViewById(R.id.pay_succed_pay_type_text);
        tvName = ((TextView) findViewById(R.id.pay_succed_seller_name));
        tvCost = ((TextView) findViewById(R.id.pay_succed_importMoney));
        tvPayType = ((TextView) findViewById(R.id.pay_succed_order_status_text));//交易成功或是确定

        tvScore = ((TextView) findViewById(R.id.pay_succed_accumulate_points));
        tvTime = ((TextView) findViewById(R.id.pay_succed_time));
        tvTradeNumber = ((TextView) findViewById(R.id.pay_succed_order_number));
        tv_contact_way = ((TextView) findViewById(R.id.tv_contact_way));//商家联系方式
        pay_succed_close = (Button) findViewById(R.id.pay_succed_close);
        pay_succed_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(3,getIntent());
                finish();
            }
        });
    }


//    bundle.putString("importMoney",infoBean.getMoney()+"");
//    bundle.putString("seller_cover_logo", seller_cover_logo);
//    bundle.putString("seller_name",infoBean.getName());
//    bundle.putString("order_status_text",infoBean.getOrder_status_text());
//    bundle.putString("pay_type_text",infoBean.getPay_type_text());
// bundle.putString("member_username",infoBean.getMember_username());
//    bundle.putString("accumulate_points",infoBean.getScore()+"");
//    bundle.putLong("create_time",infoBean.getCreate_time());
//    bundle.putString("order_number",infoBean.getOrder_number());

    private void setData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        int pay_type = bundle.getInt("pay_type", 0);

        if(pay_type==1){//记账的支付方式
            pay_succed_waytocontact.setVisibility(View.VISIBLE);
            tv_contact_way.setText(bundle.getString("seller_contact"));
        }else{//余额的支付方式

        }

        tvCost.setText(bundle.getString("importMoney"));//支付的金额问题
        Picasso.with(this).load(UrlUtils.baseWebsite + bundle.getString("seller_cover_logo")).into(userIcon);
        tvName.setText(bundle.getString("seller_name"));
        order_status_text.setText(bundle.getString("pay_type_text"));
        seller_name1.setText(bundle.getString("member_username"));//对方账户
        tvPayType.setText(bundle.getString("order_status_text"));
        //tvAccount.setText(bundle.getString("seller_mobile"));
        tvScore.setText(bundle.getString("accumulate_points"));//积分
        tvTradeNumber.setText(bundle.getString("order_number"));
        //时间戳的问题
      //  seller_order_time.setText(TimeUtils.milliseconds2String(infoBean.getCreate_time() * 1000));
        tvTime.setText(TimeUtils.milliseconds2String(bundle.getLong("create_time") * 1000));

//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
//        String str = sdf.format(curDate);
//        tvTime.setText(str);


    }

}
