package com.bignerdranch.android.registrar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bignerdranch.android.registrar.utilities.Utilities;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private static final String BASE_URL
            = "https://internship-api-v0.7.intcore.net/api/v1/user/auth/signin";

    public static final String API_TOKEN = "api-token";

    public static final String API_TOKEN_SHARED_TAG = "tag";

    private Button mLoginButton;
    private TextView mSignUpTextView;

    @BindView(R.id.email) EditText mEmailEditText;
    @BindView(R.id.password) EditText mPasswordEditText;
    @BindView(R.id.progress_bar) ProgressBar mBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        SharedPreferences sharedPref = this.getSharedPreferences(API_TOKEN_SHARED_TAG,Context.MODE_PRIVATE);

        if(!sharedPref.getString(API_TOKEN, "").equals("")){
            showProgressBar();
            String url = createUrlGetProfile(sharedPref.getString(API_TOKEN, ""));
            getProfileLogic(url);
        }

    }

    @OnClick (R.id.create_account_text_view)
    public void goSignUp(View view){
        Intent i = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.login_button)
    public void login(){
        showProgressBar();
       String email = mEmailEditText.getText().toString();
       String password = mPasswordEditText.getText().toString();
       loginLogic(email, password);
    }

    private void loginLogic(final String email,final String password){
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest jsonObjectRequest = new StringRequest
                (Request.Method.POST, BASE_URL, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        hideProgressBar();
                        Intent i = UserActivity.newIntent(LoginActivity.this, response);
                        startActivity(i);
                        finish();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideProgressBar();
                        try {
                            String responseBody =
                                    new String(error.networkResponse.data, "utf-8");
                            JSONObject data = new JSONObject(responseBody);
                            JSONArray errors = data.getJSONArray("errors");
                            String message = errors.getJSONObject(0).getString("message");
                            Utilities.showErrorDialog(LoginActivity.this,"Oops..",
                                    message);
                        } catch (JSONException e) {
                        } catch (UnsupportedEncodingException errorr) {
                        }
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Accept", "application/json");
                        return headers;
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String, String> body = new HashMap<>();
                        body.put("name", email);
                        body.put("password", password);
                        return body;
                    }
                };
        queue.add(jsonObjectRequest);
    }

    private void getProfileLogic(String url){

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest jsonObjectRequest = new StringRequest
                (Request.Method.GET, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        hideProgressBar();
                        Intent i = UserActivity.newIntent(LoginActivity.this, response);
                        startActivity(i);
                        finish();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideProgressBar();
                        try {
                            String responseBody =
                                    new String(error.networkResponse.data, "utf-8");
                            JSONObject data = new JSONObject(responseBody);
                            JSONArray errors = data.getJSONArray("errors");
                            String message = errors.getJSONObject(0).getString("message");
                            Utilities.showErrorDialog(LoginActivity.this,"Oops..", message);
                        } catch (JSONException e) {
                        } catch (UnsupportedEncodingException errorr) {
                        }
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        queue.add(jsonObjectRequest);


    }


    private String createUrlGetProfile(String apiToken) {
        Uri.Builder builder =new Uri.Builder()
                .scheme("https")
                .authority("internship-api-v0.7.intcore.net")
                .appendPath("api")
                .appendPath("v1")
                .appendPath("user")
                .appendPath("auth")
                .appendPath("get-profile")
                .appendQueryParameter("api_token", apiToken);

        try {
            URL url = new URL(builder.build().toString());
            return url.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showProgressBar(){
        mBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        mBar.setVisibility(View.INVISIBLE);
    }
}
