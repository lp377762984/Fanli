package com.qlqwgw.fanli.activities.register;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.qlqwgw.fanli.R;
import com.qlqwgw.fanli.activities.base.BaseActivity;
import com.qlqwgw.fanli.activities.main.MainActivity;

public class RegisterAfterReceiveCodeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_after_receive_code);
    }
    //注册成功 跳转到主页
    public void goMain2(View view) {
        Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
