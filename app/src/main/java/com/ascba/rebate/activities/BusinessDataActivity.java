package com.ascba.rebate.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ascba.rebate.R;
import com.ascba.rebate.activities.base.BaseActivity;
import com.ascba.rebate.activities.base.NetworkBaseActivity;
import com.ascba.rebate.activities.login.LoginActivity;
import com.ascba.rebate.handlers.CheckThread;
import com.ascba.rebate.handlers.PhoneHandler;
import com.ascba.rebate.utils.UrlUtils;
import com.yolanda.nohttp.rest.Request;

import org.json.JSONObject;

public class BusinessDataActivity extends NetworkBaseActivity {
    public static final int REQUEST_BUSINESS_NAME=0;
    public static final int REQUEST_BUSINESS_TAG=1;
    public static final int REQUEST_BUSINESS_LOCATION=2;
    public static final int REQUEST_BUSINESS_PHONE=3;
    public static final int REQUEST_BUSINESS_TIME=4;
    public static final int REQUEST_BUSINESS_RATE=5;
    public static final int REQUEST_BUSINESS_DESC=6;
    private TextView tvName;
    private TextView tvType;
    private TextView tvLocation;
    private TextView tvPhone;
    private TextView tvTime;
    private TextView tvRate;
    private SharedPreferences sf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_data);
        initViews();
        getData();
        //requestForServer();
    }

    private void getData() {
        Intent intent = getIntent();
        if(intent!=null){
            String name = intent.getStringExtra("name");
            String corporate = intent.getStringExtra("corporate");
            String address = intent.getStringExtra("address");
            String tel = intent.getStringExtra("tel");
            tvName.setText(name);
            tvLocation.setText(address);
            tvPhone.setText(tel);
        }
    }


    private void initViews() {
        sf=getSharedPreferences("first_login_success_name_password",MODE_PRIVATE);
        tvName = ((TextView) findViewById(R.id.business_data_name));
        tvType = ((TextView) findViewById(R.id.business_data_type));
        tvLocation = ((TextView) findViewById(R.id.business_data_location));
        tvPhone = ((TextView) findViewById(R.id.business_data_phone));
        tvTime = ((TextView) findViewById(R.id.business_data_time));
        tvRate = ((TextView) findViewById(R.id.business_data_rate));
    }

    /*private void requestForServer() {
        sendMsgToSevr("http://api.qlqwgw.com/v1/getCompany",0);
        CheckThread checkThread = getCheckThread();
        Request<JSONObject> objRequest = checkThread.getObjRequest();
        objRequest.add("company",3);
        PhoneHandler phoneHandler = checkThread.getPhoneHandler();
        final ProgressDialog p=new ProgressDialog(this,R.style.dialog);
        p.setMessage("请稍后");
        phoneHandler.setCallback(phoneHandler.new Callback2(){
            @Override
            public void getMessage(Message msg) {
                p.dismiss();
                super.getMessage(msg);
                JSONObject jObj = (JSONObject) msg.obj;
                int status = jObj.optInt("status");
                if(status==200){
                    JSONObject dataObj = jObj.optJSONObject("data");
                    JSONObject coObj = dataObj.optJSONObject("company");
                    int id = coObj.optInt("id");
                    String name = coObj.optString("name");
                    String corporate = coObj.optString("corporate");
                    String address = coObj.optString("address");
                    String tel = coObj.optString("tel");
                    String licence = coObj.optString("licence");
                    tvName.setText(name);
                    tvLocation.setText(address);
                    tvPhone.setText(tel);
                }
            }
        });
        checkThread.start();
        p.show();
    }*/

    public void goBusinessName(View view) {
        Intent intent=new Intent(this,BusinessNameActivity.class);
        startActivityForResult(intent,REQUEST_BUSINESS_NAME);
    }

    public void goBusinessTag(View view) {
        Intent intent=new Intent(this,BusinessTagActivity.class);
        startActivityForResult(intent,REQUEST_BUSINESS_TAG);
    }

    public void goBusinessLocation(View view) {
        //商家地理位置，此处接入百度地图
        Intent intent=new Intent(this,PoiSearchDemo.class);
        startActivityForResult(intent,REQUEST_BUSINESS_LOCATION);

    }

    public void goBusinessPhone(View view) {
        Intent intent=new Intent(this,BusinessPhoneActivity.class);
        startActivityForResult(intent,REQUEST_BUSINESS_PHONE);
    }

    public void goBusinessTime(View view) {
        Intent intent=new Intent(this,BusinessTimeActivity.class);
        startActivityForResult(intent,REQUEST_BUSINESS_TIME);
    }

    public void goBusinessRate(View view) {
        Intent intent=new Intent(this,EmployeeRateActivity.class);
        startActivityForResult(intent,REQUEST_BUSINESS_RATE);
    }

    public void goBusinessDetail(View view) {
        Intent intent=new Intent(this,BusinessDescriptionActivity.class);
        startActivityForResult(intent,REQUEST_BUSINESS_DESC);
    }

    public void goBusinessPic(View view) {
        Intent intent=new Intent(this,BusinessPicActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data==null){
            return;
        }
        switch (requestCode){
            case REQUEST_BUSINESS_NAME:
                tvName.setText(data.getStringExtra("business_data_name"));
                break;
            case REQUEST_BUSINESS_TAG:
                tvType.setText(data.getStringExtra("business_data_type"));
                break;
            case REQUEST_BUSINESS_LOCATION:
                tvLocation.setText(data.getStringExtra("business_data_location"));
                break;
            case REQUEST_BUSINESS_PHONE:
                tvPhone.setText(data.getStringExtra("business_data_phone"));
                break;
            case REQUEST_BUSINESS_TIME:
                tvTime.setText(data.getStringExtra("business_data_time"));
                break;
            case REQUEST_BUSINESS_RATE:
                tvRate.setText(data.getStringExtra("business_data_rate"));
                break;
            case REQUEST_BUSINESS_DESC:
                String desc = data.getStringExtra("desc");//返回的商家描述
                break;
        }
    }

    //提交商家资料
    public void businessDataGo(View view) {
        sendMsgToSevr(UrlUtils.setCompany,0);
        CheckThread checkThread = getCheckThread();
        if(checkThread!=null){
            Request<JSONObject> objRequest = checkThread.getObjRequest();
            PhoneHandler phoneHandler = checkThread.getPhoneHandler();
            final ProgressDialog p=new ProgressDialog(this,R.style.dialog);
            p.setMessage("请稍后");
            phoneHandler.setCallback(phoneHandler.new Callback2(){
                @Override
                public void getMessage(Message msg) {
                    p.dismiss();
                    super.getMessage(msg);
                    JSONObject jObj = (JSONObject) msg.obj;
                    int status = jObj.optInt("status");
                    String message = jObj.optString("msg");
                    if(status==200){
                        Toast.makeText(BusinessDataActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else if(status==1||status==2||status==3||status == 4||status==5){//缺少sign参数
                        Intent intent = new Intent(BusinessDataActivity.this, LoginActivity.class);
                        sf.edit().putInt("uuid", -1000).apply();
                        startActivity(intent);
                        finish();
                    } else if(status==404){
                        Toast.makeText(BusinessDataActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else if(status==500){
                        Toast.makeText(BusinessDataActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            objRequest.add("seller_name",tvName.getText().toString());
            objRequest.add("seller_taglib",tvType.getText().toString());
            objRequest.add("seller_address",tvLocation.getText().toString());
            objRequest.add("seller_tel",tvPhone.getText().toString());
            objRequest.add("seller_business_hours",tvTime.getText().toString());
            objRequest.add("seller_return_ratio",tvLocation.getText().toString());
            objRequest.add("seller_description","12345654545645654");
            checkThread.start();
            p.show();
        }

    }
}
