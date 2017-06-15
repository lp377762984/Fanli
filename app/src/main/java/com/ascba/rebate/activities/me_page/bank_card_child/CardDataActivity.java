package com.ascba.rebate.activities.me_page.bank_card_child;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.ascba.rebate.R;
import com.ascba.rebate.activities.base.BaseNetActivity;
import com.ascba.rebate.activities.me_page.settings.child.real_name_confirm.RealNameSuccessActivity;
import com.ascba.rebate.utils.UrlUtils;
import com.yanzhenjie.nohttp.rest.Request;
import org.json.JSONObject;

public class CardDataActivity extends BaseNetActivity implements BaseNetActivity.Callback {
    private TextView tvCard;
    private TextView tvSex;
    private TextView tvAge;
    private TextView tvLocation;
    private EditText etName;
    private String card;
    private String sex;
    private String age;
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_data);
        initViews();
        getDataFromBefore();
    }

    private void initViews() {
        tvCard = ((TextView) findViewById(R.id.card));
        tvSex = ((TextView) findViewById(R.id.sex));
        tvAge = ((TextView) findViewById(R.id.age));
        tvLocation = ((TextView) findViewById(R.id.location));
        etName = ((EditText) findViewById(R.id.name));

    }

    public void getDataFromBefore() {
        Intent intent = getIntent();
        if(intent!=null){
            card = intent.getStringExtra("card");
            sex = intent.getStringExtra("sex");
            age = intent.getStringExtra("age");
            location = intent.getStringExtra("location");
            tvCard.setText(card);
            tvSex.setText(sex);
            tvAge.setText(age);
            tvLocation.setText(location);
        }
    }

    public void goRealNameConfirm(View view) {
        requestVerifyCard(UrlUtils.verifyCard);
    }

    private void requestVerifyCard(String url) {
        String s = etName.getText().toString();
        if("".equals(s)){
            getDm().buildAlertDialog("请输入姓名");
            return;
        }
        Request<JSONObject> objRequest = buildNetRequest(url, 0, true);
        objRequest.add("cardid", card);
        objRequest.add("realname", s);
        if(sex.equals("男")){
            objRequest.add("sex", 1);
        }else if(sex.equals("女")){
            objRequest.add("sex", 0);
        }else{
            objRequest.add("sex", 2);
        }
        objRequest.add("age", age);
        objRequest.add("location", location);
        executeNetWork(objRequest,"请稍后");
        setCallback(this);
    }

    @Override
    public void handle200Data(JSONObject dataObj, String message) {
        Intent intent=new Intent(CardDataActivity.this,RealNameSuccessActivity.class);
        intent.putExtra("realname",etName.getText().toString());
        intent.putExtra("cardid",tvCard.getText().toString());
        String sexString = tvSex.getText().toString();
        int sex=2;
        if("女".equals(sexString)){
            sex=0;
        }else if("男".equals(sexString)){
            sex=1;
        }
        intent.putExtra("sex",sex);
        String s = tvAge.getText().toString();
        if(!"".equals(s)){
            intent.putExtra("age",Integer.parseInt(s));
        }else {
            getDm().buildAlertDialog("没有年龄参数");
            return;
        }
        intent.putExtra("location",tvLocation.getText().toString());
        startActivity(intent);
        finish();
    }

    @Override
    public void handle404(String message) {
        getDm().buildAlertDialog(message);
    }

    @Override
    public void handleNoNetWork() {

    }
}
