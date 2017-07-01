package com.ascba.rebate.activities.offline_business;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ascba.rebate.R;
import com.ascba.rebate.activities.PayPsdSettingActivity;
import com.ascba.rebate.activities.base.BaseNetActivity;
import com.ascba.rebate.activities.me_page.AccountRechargeActivity;
import com.ascba.rebate.appconfig.AppConfig;
import com.ascba.rebate.handlers.OnPasswordInput;
import com.ascba.rebate.utils.DialogHome;
import com.ascba.rebate.utils.PsdUtils;
import com.ascba.rebate.utils.UrlUtils;
import com.ascba.rebate.view.EditTextWithCustomHint;
import com.ascba.rebate.view.PsdDialog;
import com.ascba.rebate.view.RoundImageView;
import com.squareup.picasso.Picasso;
import com.yanzhenjie.nohttp.rest.Request;

import org.json.JSONObject;

/**
 * 扫一扫-付款
 */
public class OfflinePayActivity extends BaseNetActivity implements View.OnClickListener, TextWatcher {

    private RoundImageView busiIcon;
    private TextView tvBusiName, sweepRemainder;
    private EditTextWithCustomHint etMoney;
    private Button btnPay;
    private PsdDialog psdDialog;
    private BottomSheetDialog payTypeDialog;
    private boolean isReminderPay = true;
    private RadioButton rbReminder, rbOther;
    private int payType = 0;
    private double self_money;
    private double importMoney;
    private int seller;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_pay);
        initViews();

    }


    private void initViews() {
        busiIcon = ((RoundImageView) findViewById(R.id.im_busi_icon));
        tvBusiName = ((TextView) findViewById(R.id.tv_busi_name));
        sweepRemainder = (TextView) findViewById(R.id.sweep_remainder);
        etMoney = ((EditTextWithCustomHint) findViewById(R.id.et_busi_money));
        btnPay = ((Button) findViewById(R.id.btn_pay));
        etMoney.addTextChangedListener(this);
        setBtnStatus(R.drawable.ticket_no_shop_bg, false);
        rbReminder = (RadioButton) findViewById(R.id.rb_offline_reminder);
        rbOther = (RadioButton) findViewById(R.id.rb_offline_other);
        rbReminder.setOnClickListener(this);
        rbOther.setOnClickListener(this);
        rbReminder.setChecked(true);
        requestNetwork(UrlUtils.submit, 0);

        Intent intent = getIntent();
        String seller_cover_logo = intent.getStringExtra("seller_cover_logo");
        String seller_name = intent.getStringExtra("seller_name");
        seller = intent.getIntExtra("seller",0);
        self_money = Double.parseDouble(intent.getStringExtra("self_money"));//余额
        String seller_logo = UrlUtils.baseWebsite + seller_cover_logo;
        Picasso.with(this).load(seller_logo).into(busiIcon);
        tvBusiName.setText("向" + seller_name + "付款");
        sweepRemainder.setText("可用余额" + self_money + "元");
    }





    //点击更换支付方式
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.rb_offline_reminder) {//余额支付
            isReminderPay = true;
            rbReminder.setChecked(true);
            rbOther.setChecked(false);
            payType = 2;
        } else if (v.getId() == R.id.rb_offline_other) {//其他支付方式
            isReminderPay = false;
            rbReminder.setChecked(false);
            rbOther.setChecked(true);
            payType = 1;
        }
    }


    @Override
    public void afterTextChanged(Editable s) {
        String finalString = s.toString();
        if ("".equals(finalString)) {//未输入金额的情况
            setBtnStatus(R.drawable.ticket_no_shop_bg, false);
            return;
        }
        double v = Double.parseDouble(finalString);
        if (v == 0) {//输入金额为0的情况
            setBtnStatus(R.drawable.ticket_no_shop_bg, false);
        } else{
            setBtnStatus(R.drawable.register_btn_bg, true);
        }
    }


    //设置button状态
    private void setBtnStatus(int id, boolean enable) {
        btnPay.setBackgroundDrawable(getResources().getDrawable(id));
        btnPay.setEnabled(enable);
    }

    //点击去付款
    public void goPay(View view) {
        showPsdDialog();


    }

    public void requestNetwork(String url, int what) {
        Request<JSONObject> request = buildNetRequest(url, 0, true);
        request.add("seller", seller);
        request.add("money", importMoney);
        request.add("pay_type", payType);
        request.add("pay_password",password);
        Log.d("fanxi",seller+"------seller");
        Log.d("fanxi",seller+"------seller");
        Log.d("fanxi",seller+"------seller");
        Log.d("fanxi",seller+"------seller");
        executeNetWork(what, request, "请稍后");

    }
    @Override
    protected void mhandle200Data(int what, JSONObject object, JSONObject dataObj, String message) {
        super.mhandle200Data(what, object, dataObj, message);
        Log.d("fanxi", "------"+dataObj.toString());

    }

    //显示密码框
    private void showPsdDialog() {
        psdDialog = new PsdDialog(this, R.style.AlertDialog);
        psdDialog.setOnPasswordInputFinish(new OnPasswordInput() {
            @Override
            public void inputFinish(String number) {
                judgeRemainder();
                password= PsdUtils.getPayPsd(number);
                psdDialog.dismiss();
            }
            //支付取消
            @Override
            public void inputCancel() {
                psdDialog.dismiss();
                showToast("支付取消");
            }
            //忘记密码
            @Override
            public void forgetPsd() {
                AppConfig.getInstance().putInt("is_level_pwd", 0);
                Intent intent = new Intent(OfflinePayActivity.this, PayPsdSettingActivity.class);
                intent.putExtra("type", 1);
                startActivity(intent);
            }
        });
        psdDialog.showMyDialog();
    }


    //判断是否余额充足
    private void judgeRemainder() {           //凡茜
        if (isReminderPay) {//如果余额支付
            importMoney = Double.parseDouble(etMoney.getText().toString());//输入的金额
            if (importMoney < self_money) {
                requestNetwork(UrlUtils.submit,0);
              //  startActivity(new Intent(OfflinePayActivity.this, OfflinePaySuccedActivity.class));
              //  finish();
            } else {
                getDm().buildAlertDialog3("账户余额不足，请先充值，再进行支付！", "去充值", new DialogHome.Callback() {
                    @Override
                    public void handleSure() {
                        startActivity(new Intent(OfflinePayActivity.this, AccountRechargeActivity.class));
                        finish();
                    }
                }).show();
            }
        } else {//线下交易的方式支付,等待商家确定，
            startActivity(new Intent(OfflinePayActivity.this, OfflinePaySureOrderActivity.class));
            finish();
        }

    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }


}

//    //支付方式dialog
//    private void showPayTypeDialog() {
//        payTypeDialog = new BottomSheetDialog(this, R.style.AlertDialog);
//        payTypeDialog.setContentView(R.layout.layout_pay_pop_offline);
//        //列表
//        RecyclerView rvTypes = (RecyclerView) payTypeDialog.findViewById(R.id.pay_type_list);
//        List<PayType> types = new ArrayList<>();
//        initPayTypesData(types);
//        PayTypeAdapter pt = new PayTypeAdapter(R.layout.pay_type_item, types);
//        pt.setCallback(new PayTypeAdapter.Callback() {
//            @Override
//            public void onClicked(String payType) {
//                payTypeDialog.dismiss();
//                OfflinePayActivity.this.payType = payType;
//                if ("balance".equals(payType)) {
//                    setSuperString("账户余额支付方式");
//                } else if ("cash".equals(payType)) {
//                    setSuperString("现金支付方式");
//                }
//            }
//        });
//        rvTypes.setLayoutManager(new LinearLayoutManager(this));
//        rvTypes.setAdapter(pt);
//        //显示对话框
//        payTypeDialog.show();
//    }

//    private void initPayTypesData(List<PayType> types) {
//        types.add(new PayType(true, R.mipmap.pay_left, "账户余额支付方式", "快捷支付 账户余额￥" + 28, "balance"));
//        types.add(new PayType(false, R.mipmap.pay_cash, "现金支付方式", "通过app使用现金支付可以返积分喔！", "cash"));
//        for (int i = 0; i < types.size(); i++) {
//            PayType payType = types.get(i);
//            String type = payType.getType();
//            if (this.payType.equals(type)) {
//                payType.setSelect(true);
//            } else {
//                payType.setSelect(false);
//            }
////        }
//    }
