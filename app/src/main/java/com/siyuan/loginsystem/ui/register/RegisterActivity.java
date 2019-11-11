package com.siyuan.loginsystem.ui.register;


import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextUtils;
import android.widget.EditText;
import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.siyuan.loginsystem.R;
import com.siyuan.loginsystem.ui.HttpUtilities;
import com.siyuan.loginsystem.ui.login.LoginActivity;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText, passwordComfirmEditText;

    private String username, password, passwordComfirm;
    private static final String REGISTER_URL = "http://13.59.244.178/Netease/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置页面布局 ,注册界面
        setContentView(R.layout.activity_register);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        init();
    }

    private void init() {

        ////从activity_register.xml 页面中获取对应的UI控件
        Button backLogin = findViewById(R.id.back_to_login);
        Button registerButton = findViewById(R.id.register);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        passwordComfirmEditText = findViewById(R.id.repeat_password);

        //跳回登陆界面
        backLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getEditTextString();

                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(RegisterActivity.this, R.string.empty_input, Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterActivity.this, R.string.empty_password, Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(passwordComfirm)) {
                    Toast.makeText(RegisterActivity.this, R.string.empty_password, Toast.LENGTH_LONG).show();
                } else if (username.length() < 6 || username.length() > 18) {
                    Toast.makeText(RegisterActivity.this, "用户名长度不符合要求，请重新输入！", Toast.LENGTH_LONG).show();
                } else if (!username.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                    Toast.makeText(RegisterActivity.this, R.string.invalid_username, Toast.LENGTH_LONG).show();
                } else if (!password.equals(passwordComfirm)) {
                    Toast.makeText(RegisterActivity.this, R.string.not_same_password, Toast.LENGTH_LONG).show();
                } else if (password.length() < 8 || password.length() > 18) {
                    Toast.makeText(RegisterActivity.this, "密码长度不符合规范，请重新输入！", Toast.LENGTH_LONG).show();
                } else if (password.equals(username)) {
                    Toast.makeText(RegisterActivity.this, R.string.same_with_username, Toast.LENGTH_LONG).show();
                } else if (!checkPassword(password)) {
                    Toast.makeText(RegisterActivity.this, R.string.invalid_password, Toast.LENGTH_LONG).show();
                } else {
//                    Thread thread = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Looper.prepare();// call looper.prepare()
//                            String result = null;
//                            try {
//                                result = HttpUtilities.getHttpPostResult(username, password, REGISTER_URL);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                            final String finalResult = result;
//                            Handler handler = new Handler() {
//                                public void handleMessage(Message msg) {
//                                    Toast.makeText(RegisterActivity.this, finalResult, Toast.LENGTH_LONG).show();
//                                }
//                            };
//                            Looper.loop();
//                        }
//                    });
//                    thread.start();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject obj = null;
                            try {
                                obj = HttpUtilities.getHttpPostResult(username,password,REGISTER_URL);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String result = null;
                            if (obj != null) {
                                try {
                                    result = obj.getString("status");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (result.equals("OK")) {
                                Toast.makeText(RegisterActivity.this, R.string.register_success, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, R.string.exist_username, Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                }
            }
        });
    }

//    public String getPostResult(String username, String password, String url) {
//        try {
//            HttpURLConnection conn = HttpUtilities.makeHttpPostConnection(username, password, url);
//            conn.connect();
//            String result = HttpUtilities.parseResponse(conn);
//            System.out.println("result"+result);
//            return result;
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return "";
//    }

    /**
     * 得到editText的内容（string）
     */
    private void getEditTextString() {
        username = usernameEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();
        passwordComfirm = passwordComfirmEditText.getText().toString().trim();
    }

    private boolean checkPassword(String password) {
        return password.matches("((^(?=.*[a-z])(?=.*[A-Z])(?=.*\\W)[\\da-zA-Z\\W]{8,16}$)|(^(?=.*\\d)(?=.*[A-Z])(?=.*\\W)[\\da-zA-Z\\W]{8,16}$)|(^(?=.*\\d)(?=.*[a-z])(?=.*\\W)[\\da-zA-Z\\W]{8,16}$)|(^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])[\\da-zA-Z\\W]{8,16}$))");
    }

}
