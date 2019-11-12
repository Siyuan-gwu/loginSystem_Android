package com.siyuan.loginsystem.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.siyuan.loginsystem.R;
import com.siyuan.loginsystem.ui.HttpUtilities;
import com.siyuan.loginsystem.ui.LogOutTimerUtil;
import com.siyuan.loginsystem.ui.register.RegisterActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;

public class LoginActivity extends AppCompatActivity implements LogOutTimerUtil.LogOutListener {

    SharedPreferences sharedPreferences;
    private Timer timer;
    private static final String LOGIN_URL = "http://13.59.244.178/Netease/login";
    private String username, password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogOutTimerUtil.startLogoutTimer(this,this);
        System.out.println("正在计时...");
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        LogOutTimerUtil.startLogoutTimer(this,this);
        System.out.println("用户与屏幕交互...");
    }

    @Override
    protected void onResume() {
        super.onResume();

        System.out.println("onResume()...");
    }

    /**
     * Performing idle time logout
     */
    @Override
    public void doLogout() {
        // write your stuff here
        String user = existlogin();
        if (user != null) {
            clearUser();
            Toast.makeText(LoginActivity.this, R.string.timeout, Toast.LENGTH_LONG).show();
        }
    }

    private void init() {
        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final Button backToRegister = findViewById(R.id.register);
        final Button logoutButton = findViewById(R.id.logout);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearUser();
                Toast.makeText(LoginActivity.this, R.string.logout, Toast.LENGTH_LONG).show();

            }
        });

        backToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = usernameEditText.getText().toString().trim();
                password = passwordEditText.getText().toString().trim();

                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(LoginActivity.this, R.string.empty_input, Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, R.string.empty_password, Toast.LENGTH_LONG).show();
                } else if (checkLoggedIn(username)) {
                    Toast.makeText(LoginActivity.this, R.string.repeat_login, Toast.LENGTH_LONG).show();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String result = null;
                            JSONObject obj = null;
                            String msg = null;
                            String text = null;
                            try {
                                obj = HttpUtilities.getHttpPostResult(username, password, LOGIN_URL);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                result = obj.getString("status");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (result.equals("OK")) {
                                //保存已经登陆的用户
                                storeLoginUser(username);

                                text = getResources().getString(R.string.login_success);
                                Toast.makeText(LoginActivity.this, text + username, Toast.LENGTH_LONG).show();
                            } else if (result.equals("worng_input")) {
                                Toast.makeText(LoginActivity.this, R.string.wrong_username, Toast.LENGTH_LONG).show();
                            } else if (result.equals("locked")) {
                                text = getResources().getString(R.string.locked);
                                try {
                                    msg = obj.getString("wait_time");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(LoginActivity.this, text + "请等待" + msg + "小时！", Toast.LENGTH_LONG).show();
                            } else if (result.equals("wrong_password")) {
                                text = getResources().getString(R.string.wrong_password);
                                try {
                                    msg = obj.getString("rest_attempt");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(LoginActivity.this, text + "您还有" + msg + "次机会", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }

//    private boolean checkLoggedIn(String username) {
//        boolean flag = false;
//        JSONObject loggedInUser = null;
//        try {
//            loggedInUser = HttpUtilities.getHttpGetResult(LOGIN_URL);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        try {
//            if (loggedInUser.getString("status").equals("OK") && loggedInUser.getString("user_id").equals(username)) {
//                flag = true;
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        System.out.println("lgged in ? " + flag);
//        return flag;
//    }

    private void storeLoginUser(String username) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_id", username);
        editor.commit();
    }

    private String existlogin() {
        String user = null;
        user = sharedPreferences.getString("user_id", null);
        return user;
    }

    private boolean checkLoggedIn(String username) {
        String loginUser = null;
//        sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        loginUser = sharedPreferences.getString("user_id", null);
        if (loginUser != null && loginUser.equals(username)) {
            return true;
        }
        return false;
    }

    private void clearUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_id", null);
        editor.commit();
    }

//
//    private void updateUiWithUser(LoggedInUserView model) {
//        String welcome = getString(R.string.welcome) + model.getDisplayName();
//        // TODO : initiate successful logged in experience
//        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
//    }
//
//    private void showLoginFailed(@StringRes Integer errorString) {
//        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
//    }
}
