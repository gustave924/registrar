package com.bignerdranch.android.registrar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bignerdranch.android.registrar.utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignupActivity extends AppCompatActivity {

    private final static String TAG = "SignupActivity";
    private static final String BASE_URL =
            "https://internship-api-v0.7.intcore.net/api/v1/user/auth/signup";

    @BindView(R.id.name) EditText mNameEditText;
    @BindView(R.id.email) EditText mEmailEditText;
    @BindView(R.id.phone_number) EditText mPhoneNumberEditText;
    @BindView(R.id.password) EditText mPasswordEditText;
    @BindView(R.id.password_second) EditText mpPasswordReenteredEditText;
    @BindView(R.id.progress_bar) ProgressBar mBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ButterKnife.bind(this);

    }

    @OnClick (R.id.singup_button)
    public void signupUser(View view){
        if(validate()){
            showProgressBar();
            String name = mNameEditText.getText().toString();
            String email = mEmailEditText.getText().toString();
            String phoneNumber = mPhoneNumberEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();
            signUpLogic(name, phoneNumber, password, email);
        }
    }

    @OnClick (R.id.go_to_login)
    public void goToLogin(View view){
        Intent i = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(i);
    }





    private boolean validate() {
        boolean valid = true;

        String name = mNameEditText.getText().toString();
        String email = mEmailEditText.getText().toString();
        String mobile = mPhoneNumberEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        String reEnterPassword = mpPasswordReenteredEditText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            mNameEditText.setError("at least 3 characters");
            valid = false;
        } else {
            mNameEditText.setError(null);
        }

       if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailEditText.setError("enter a valid email address");
            valid = false;
        } else {
            mEmailEditText.setError(null);
        }

        if (mobile.isEmpty() || mobile.length()!=11) {
            mPhoneNumberEditText.setError("Enter Valid Mobile Number");
            valid = false;
        } else {
            mPhoneNumberEditText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPasswordEditText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            mPasswordEditText.setError(null);
        }

        if (reEnterPassword.isEmpty()
                || reEnterPassword.length() < 4 || reEnterPassword.length() > 10
                || !(reEnterPassword.equals(password))) {
            mpPasswordReenteredEditText.setError("Password Do not match");
            valid = false;
        } else {
            mpPasswordReenteredEditText.setError(null);
        }

        return valid;
    }



    private void showProgressBar(){
        mBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        mBar.setVisibility(View.INVISIBLE);
    }


    private void signUpLogic(final String name,
                             final String phone,
                             final String password,
                             final String email){

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest jsonObjectRequest = new StringRequest
                (Request.Method.POST, BASE_URL, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        hideProgressBar();
                        Intent i = UserActivity.newIntent(SignupActivity.this, response);
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
                            Utilities.showErrorDialog(SignupActivity.this,"Oops..",
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
                body.put("name", name);
                body.put("phone", phone);
                body.put("password", password);
                body.put("email", email);
                return body;
            }
        };
        queue.add(jsonObjectRequest);
    }

}
