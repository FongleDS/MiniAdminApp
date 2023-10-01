package com.example.miniadminapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class LoginActivity extends AppCompatActivity {
    TextView sign;
    Button loginbtn;
    EditText ID;
    EditText PW;

    String inputPW;
    String inputID;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //회원가입 버튼
        sign = findViewById(R.id.signin);
        loginbtn = findViewById(R.id.loginbutton);
        ID = findViewById(R.id.editID);
        PW = findViewById(R.id.editPassword);
/*
        //회원가입 버튼 클릭시, 회원가입 페이지로 이동
        sign.setOnClickListener(v -> {
            Intent intent = new Intent(this, Signup.class);
            startActivity(intent);
        });
*/
        loginbtn.setOnClickListener(v -> {
            inputID = String.valueOf(ID.getText());
            inputPW = String.valueOf(PW.getText());
            fetchPassword(inputID);
        });
    }


    // 서버 연결 입니다!
    OkHttpClient client = new OkHttpClient();
    public void fetchPassword(String RestID) {
        RequestBody formBody = new FormBody.Builder()
                .add("RestID", RestID)
                .build();
        Request request = new Request.Builder()
                // .url("http://192.168.0.142:5000/get_password")
                .url("http://10.0.2.2:5000/getRestPassword")
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        if (jsonObject.has("password")) {
                            String password = jsonObject.getString("password");
                            runOnUiThread(() -> {
                                System.out.println(password);
                                String RealPW = password;
                                System.out.println(RealPW);
                                System.out.println(inputPW);
                                // 입력한 비밀번호가 학번의 비밀 번호와 같을 떄
                                if (RealPW.equals(inputPW)) {
                                    Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
                                    intent.putExtra("RestID", RestID);
                                    startActivity(intent);
                                }
                                // 입력한 비밀번호가 학번의 비밀 번호와 다를 때
                                else {
                                    PW.setText(null);
                                }
                            });
                        } else if (jsonObject.has("error")) {
                            String error = jsonObject.getString("error");
                            runOnUiThread(() -> {
                                System.out.println("error");
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}