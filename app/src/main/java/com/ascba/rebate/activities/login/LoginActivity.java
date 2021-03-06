package com.ascba.rebate.activities.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ascba.rebate.R;
import com.ascba.rebate.activities.base.BaseActivity;
import com.ascba.rebate.activities.main.MainActivity;
import com.ascba.rebate.activities.password_loss.PasswordLossActivity;
import com.ascba.rebate.activities.register.RegisterAfterReceiveCodeActivity;
import com.ascba.rebate.activities.register.RegisterInputNumberActivity;
import com.ascba.rebate.handlers.CheckThread;
import com.ascba.rebate.handlers.PhoneHandler;
import com.ascba.rebate.utils.EncryptHelper;
import com.ascba.rebate.utils.LogUtils;
import com.ascba.rebate.utils.NetUtils;
import com.ascba.rebate.utils.UrlEncodeUtils;
import com.ascba.rebate.utils.UrlUtils;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.RequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 登录页面
 */

public class LoginActivity extends BaseActivity {
    private EditText edPhone;
    private EditText edPassword;
    private String loginPhone;
    private String loginPassword;
    private PhoneHandler phoneHandler;
    private CheckThread checkThread;
    private RequestQueue requestQueue;
    private SharedPreferences sf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        initViews();
        autoLogin();
        backFirstPhone();//传回注册成功的手机账号
        backLossPhone();//密码找回成功
    }

    private void autoLogin() {
        int uuid = sf.getInt("uuid", -1000);
        if (uuid != -1000) {//如果有登录数据，则自动登录到主界面
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void backLossPhone() {
        String loss_password = getIntent().getStringExtra("loss_password");
        if (loss_password != null) {
            Toast.makeText(this, "密码找回成功", Toast.LENGTH_SHORT).show();
            edPassword.setText("");
            sf.edit()
                    /*.putString("login_password", loss_password)*/
                    .putString("login_password", "")
                    .apply();
        }
    }


    private void backFirstPhone() {

        String phone_number = getIntent().getStringExtra("phone_number");
        if (phone_number != null) {
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
            edPhone.setText(phone_number);
            edPassword.requestFocusFromTouch();
        }
    }

    private void initViews() {
        edPhone = (EditText) findViewById(R.id.login_phone_ed);
        edPassword = (EditText) findViewById(R.id.login_password_ed);
        sf = getSharedPreferences("first_login_success_name_password", MODE_PRIVATE);
        //isFirstLogin=sf.getBoolean("first_login_success",true);
    }


    //点击登录按钮
    public void goMain(View view) {
        sendMsgToSevr(UrlUtils.login);
    }


    //进入密码找回页面
    public void goForgetPassword(View view) {
        Intent intent = new Intent(this, PasswordLossActivity.class);
        startActivity(intent);
        finish();
    }

    //进入注册页面
    public void goRegister(View view) {
        Intent intent = new Intent(this, RegisterInputNumberActivity.class);
        startActivity(intent);
    }

    private void sendMsgToSevr(String baseUrl) {
        boolean netAva = NetUtils.isNetworkAvailable(this);
        if(!netAva){
            Toast.makeText(this, "请打开网络", Toast.LENGTH_SHORT).show();
            return;
        }
        loginPhone = edPhone.getText().toString();
        loginPassword = edPassword.getText().toString();
        if (loginPhone.equals("") || loginPassword.equals("")) {
            Toast.makeText(this, "账号或密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        requestQueue = NoHttp.newRequestQueue();
        final ProgressDialog dialog4 = new ProgressDialog(this,R.style.dialog);
        dialog4.setMessage("登录中");
        Request<JSONObject> objRequest = NoHttp.createJsonObjectRequest(baseUrl + "?", RequestMethod.POST);
        objRequest.add("sign", UrlEncodeUtils.createSign(baseUrl));
        objRequest.add("loginId", loginPhone);
        objRequest.add("password", loginPassword);
        phoneHandler = new PhoneHandler(this);
        phoneHandler.setCallback(new PhoneHandler.Callback() {
            @Override
            public void getMessage(Message msg) {
                dialog4.dismiss();
                JSONObject jObj = (JSONObject) msg.obj;
                LogUtils.PrintLog("123LoginActivity", jObj.toString());
                try {
                    int status = jObj.getInt("status");
                    String message = jObj.getString("msg");
                    if (status == 200) {//服务端返回成功
                        JSONObject dataObj = jObj.getJSONObject("data");
                        int uuid = dataObj.getInt("uuid");
                        String token = dataObj.getString("token");
                        Long exTime = dataObj.getLong("expiring_time");
                        /*//加密密码
                        String enPassword = EncryptHelper.rsaOpration(loginPassword, EncryptHelper.RSA_ENCRYPT);*/
                        sf.edit().putInt("uuid", uuid)
                                .putString("token", token)
                                .putLong("expiring_time", exTime)
                                .putBoolean("first_login_success", false)
                                .putString("login_phone", loginPhone)
                                .putString("login_password", "")
                                .apply();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else if(status==-1){//用户不存在
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else if(status==1){//缺少sign参数
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else if(status==2){//非法请求，sign验证失败
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    } /*else if(status==3){//跳转登录
                        Intent intent=new Intent(LoginActivity.this, LoginActivity.class);
                        intent.putExtra("uuid",-1000);
                        startActivity(intent);
                        finish();
                    } */else if(status==4){//登陆后缺少uuid/token/expiring_time参数
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else if(status==5){//token验证失败
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    } /*else if(status==6){//用户已存在
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    } */else if(status==404){//失败
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else if(status==500){//数据异常，内部错误
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        checkThread = new CheckThread(requestQueue, phoneHandler, objRequest);
        checkThread.start();
        //登录中对话框
        dialog4.show();
    }
}
