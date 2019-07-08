package com.bignerdranch.android.registrar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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

public class UpdatePhoneFragment extends DialogFragment {

    private static final String PASSED_TOKEN = "token";
    private static final String BASE_URL
            = "internship-api-v0.7.intcore.net";
    public static final String UPDATE_PHONE_RESULT = "update-phone-result";
    private static final String TAG = "UpdatePhoneFragment";


    @BindView(R.id.new_phone_number) EditText phoneNumberEditText;
    @BindView(R.id.code) EditText sentCodeEditText;

    public static DialogFragment newInstance(Context ctx, String apiToken){
        UpdatePhoneFragment fragment = new UpdatePhoneFragment();
        Bundle args = new Bundle();
        args.putString(PASSED_TOKEN, apiToken);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_update_phone,null);

        ButterKnife.bind(this,v);

        final String apiToken = getArguments().getString(PASSED_TOKEN, "");
        return new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.update_phone_number)
                .setView(v)
                .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String codeToValidate = sentCodeEditText.getText().toString();
                        String newPhoneNumber = phoneNumberEditText.getText().toString();
                        String url = createUrl(newPhoneNumber, codeToValidate, apiToken);
                        updatePhoneNubmer(dialogInterface, url);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
    }

    public void updatePhoneNubmer(final DialogInterface dialogInterface, String url){
        final RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest jsonObjectRequest = new StringRequest
                (Request.Method.PATCH, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        dialogInterface.dismiss();
                        Log.d(TAG, "onResponse: "+response);
                        queue.stop();
                        sendResult(Activity.RESULT_OK, "You changed the password successfuly ");
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialogInterface.dismiss();
                        try {

                            String responseBody =
                                    new String(error.networkResponse.data, "utf-8");
                            JSONObject data = new JSONObject(responseBody);
                            JSONArray errors = data.getJSONArray("errors");
                            String message = errors.getJSONObject(0).getString("message");
                            queue.stop();
                            sendResult(Activity.RESULT_CANCELED, message);
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

    private String createUrl(String phoneNumber, String code, String apiToken) {
        Uri.Builder builder =new Uri.Builder()
                .scheme("https")
                .authority(BASE_URL)
                .appendPath("api")
                .appendPath("v1")
                .appendPath("user")
                .appendPath("auth")
                .appendPath("update-phone")
                .appendQueryParameter("api_token", apiToken)
                .appendQueryParameter("phone", phoneNumber)
                .appendQueryParameter("temp_phone_code", code);


        try {
            URL url = new URL(builder.build().toString());
            return url.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendResult(int resultCode, String message) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(UPDATE_PHONE_RESULT, message);
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
