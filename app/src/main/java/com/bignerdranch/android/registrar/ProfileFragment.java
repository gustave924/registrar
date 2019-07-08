package com.bignerdranch.android.registrar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bignerdranch.android.registrar.utilities.RealPathUtil;
import com.bignerdranch.android.registrar.utilities.User;
import com.bignerdranch.android.registrar.utilities.Utilities;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;


public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final String USER_DATA = "user-data";
    private static final String BASE_URL = "https://internship-api-v0.7.intcore.net/";
    private static final String CHANGE_PASSWORD_DIALOUG = "change-password-dialoug";
    private static final String UPDATE_PHONE_NUMBER_DIALOUG = "update-phone-number-dialoug";

    private static final int REQUEST_PHOTO = 1;
    private static final int READ_EXTERNAL_DATA_STORAGE = 2;
    private static final int CHANGE_PASSWORD_DIALOUG_RESULT = 3;
    private static final int UPDATE_PHONE_NUMBER_DIALOUG_RESULT = 4;


    private User mUser;


    private Uri mImageUri =  null;
    private File mImageFile = null;

    @BindView(R.id.user_photo_image_view) ImageView mUserImageView;
    @BindView(R.id.user_name_edit_text) EditText mUserNameEditText;
    @BindView(R.id.user_email_edit_text) EditText mUserEmailEditText;
    @BindView(R.id.request_mobile_number_change) AppCompatButton mUserChangePhoneNumberButton;
    @BindView(R.id.update_password_button) AppCompatButton mUpdatePasswordButton;
    @BindView(R.id.update_profile_button) AppCompatButton mSaveChangesButton;

    public static ProfileFragment newInstance(User userData) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable(USER_DATA, userData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args.getParcelable(USER_DATA) != null){
            mUser = (User) args.getParcelable(USER_DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, v);

        Picasso.get().load(BASE_URL+mUser.getImage()).into(mUserImageView);

        mUserNameEditText.setText(mUser.getName());

        mUserEmailEditText.setText(mUser.getEmail());


        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @OnClick(R.id.user_photo_image_view)
    public void chooseImage(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_PHOTO);
    }

    @OnClick(R.id.update_password_button)
    public void updatePassword(View view){
        DialogFragment dialogFragment =
                ChangePasswordFragment.newInstance(getActivity(), mUser.getApi_token());
        dialogFragment.setTargetFragment(ProfileFragment.this, CHANGE_PASSWORD_DIALOUG_RESULT);
        dialogFragment.show(getFragmentManager(), CHANGE_PASSWORD_DIALOUG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_PHOTO:
                try {
                    mImageUri = data.getData();
                    if (resultCode == Activity.RESULT_OK) {
                        if (ContextCompat.checkSelfPermission
                                (getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                    Manifest.permission.READ_CONTACTS)) {

                                } else {

                                    ActivityCompat.requestPermissions(getActivity(),
                                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                            READ_EXTERNAL_DATA_STORAGE);
                                }
                        } else {
                            getFileAndUri();
                        }
                        }
                    } catch (Exception e) {
                        Log.e("FileSelectorActivity", "File select error", e);
                    }

                    Picasso.get().load(mImageUri).into(mUserImageView);

                break;
            case CHANGE_PASSWORD_DIALOUG_RESULT:
                String message = data.getStringExtra(ChangePasswordFragment.RESULT_MESSAGE);
                if (resultCode == Activity.RESULT_OK){
                    Utilities.showSuccessDialog(getActivity(),
                            "Success!",
                            message);
                }else if(resultCode == Activity.RESULT_CANCELED){
                    Utilities.showErrorDialog(getActivity(),
                            "Oops...",
                            message);
                }
                break;

            case UPDATE_PHONE_NUMBER_DIALOUG_RESULT:
                String phoneUpdateMessage = data.getStringExtra(UpdatePhoneFragment.UPDATE_PHONE_RESULT);
                if (resultCode == Activity.RESULT_OK){
                    Utilities.showSuccessDialog(getActivity(),
                            "Success!",
                            phoneUpdateMessage);
                }else if(resultCode == Activity.RESULT_CANCELED){
                    Utilities.showErrorDialog(getActivity(),
                            "Oops...",
                            phoneUpdateMessage);
                }
                break;
        }
    }

    public String getPathFromURI(Uri contentUri) {
        String realPath;
        // SDK < API11
        if (Build.VERSION.SDK_INT < 11) {
            realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(getActivity(), contentUri);
        }

        // SDK >= 11 && SDK < 19
        else if (Build.VERSION.SDK_INT < 19) {
            realPath = RealPathUtil.getRealPathFromURI_API11to18(getActivity(), contentUri);
        }

        // SDK > 19 (Android 4.4)
        else {
            realPath = RealPathUtil.getRealPathFromURI_API19(getActivity(), contentUri);
        }
        return realPath;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case READ_EXTERNAL_DATA_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getFileAndUri();
                } else {

                }
                return;
        }
    }

    private void getFileAndUri() {
        final String path = getPathFromURI(mImageUri);
        if (path != null) {
            mImageFile = new File(path);
            mImageUri = Uri.fromFile(mImageFile);
        }
    }

    @OnClick(R.id.update_profile_button)
    public void updateProfileData(View view){
        //uploadPhoto();

        String name = mUserNameEditText.getText().toString();
        String email = mUserEmailEditText.getText().toString();

        String url = createUrlUpdateProfile(mUser.getApi_token(), name, email, mUser.getImage());

        final RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest jsonObjectRequest = new StringRequest
                (Request.Method.PATCH, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Utilities.showSuccessDialog(getActivity(), "Success","Profile updated successfuly");
                        queue.stop();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {

                            String responseBody =
                                    new String(error.networkResponse.data, "utf-8");
                            JSONObject data = new JSONObject(responseBody);
                            JSONArray errors = data.getJSONArray("errors");
                            String message = errors.getJSONObject(0).getString("message");
                            queue.stop();
                            Utilities.showErrorDialog(getActivity(),"Oops..", message);
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

    public String uploadPhoto(){
        if(mImageFile == null){
            return null;
        }
        String url = "https://internship-api-v0.7.intcore.net/api/v1/user/auth/file/upload";
        final RequestQueue queue = Volley.newRequestQueue(getActivity());
        Bitmap bitmap = BitmapFactory.decodeFile(mImageFile.getPath());
        final String imageString = Utilities.get64BaseImage(bitmap);
        StringRequest jsonObjectRequest = new StringRequest
                (Request.Method.POST, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Utilities.showSuccessDialog(getActivity(), "Success",response);
                        queue.stop();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            String responseBody =
                                    new String(error.networkResponse.data, "utf-8");
                            JSONObject data = new JSONObject(responseBody);
                            JSONArray errors = data.getJSONArray("errors");
                            String message = errors.getJSONObject(0).getString("message");
                            Utilities.showErrorDialog(getActivity(),"Oops..",
                                    message);
                            queue.stop();
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
                body.put("file", imageString);
                return body;
            }
        };
        /*SimpleMultiPartRequest smr = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Utilities.showSuccessDialog(getActivity(), "Success",response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    String responseBody =
                            new String(error.networkResponse.data, "utf-8");
                    JSONObject data = new JSONObject(responseBody);
                    JSONArray errors = data.getJSONArray("errors");
                    String message = errors.getJSONObject(0).getString("message");
                    Utilities.showErrorDialog(getActivity(),"Oops..",
                            message);
                } catch (JSONException e) {
                } catch (UnsupportedEncodingException errorr) {
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        smr.addStringParam("param string", " data text");
        smr.addFile("param file", mImageFile.getPath());*/

        queue.add(jsonObjectRequest);
        return null;
    }

    @OnClick(R.id.request_mobile_number_change)
    public void updatePhoneNumber(View view){
        String url = "https://internship-api-v0.7.intcore.net/api/v1/user/auth/request-update-phone";
        final RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest jsonObjectRequest = new StringRequest
                (Request.Method.POST, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: "+response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject.getString("message").equals("sent successfully")){
                                queue.stop();
                                DialogFragment fragment =
                                        UpdatePhoneFragment.newInstance(getActivity(),mUser.getApi_token());
                                fragment.setTargetFragment(ProfileFragment.this,UPDATE_PHONE_NUMBER_DIALOUG_RESULT);
                                fragment.show(getFragmentManager(), UPDATE_PHONE_NUMBER_DIALOUG);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            String responseBody =
                                    new String(error.networkResponse.data, "utf-8");
                            JSONObject data = new JSONObject(responseBody);
                            JSONArray errors = data.getJSONArray("errors");
                            String message = errors.getJSONObject(0).getString("message");
                            queue.stop();
                            Utilities.showErrorDialog(getActivity(),"Oops..",
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
                body.put("api_token", mUser.getApi_token());
                body.put("phone", mUser.getPhone());
                return body;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private String createUrlUpdateProfile(String apiToken, String name, String email, String image) {
        Uri.Builder builder =new Uri.Builder()
                .scheme("https")
                .authority("internship-api-v0.7.intcore.net")
                .appendPath("api")
                .appendPath("v1")
                .appendPath("user")
                .appendPath("auth")
                .appendPath("update-profile")
                .appendQueryParameter("api_token", apiToken)
                .appendQueryParameter("name", name)
                .appendQueryParameter("email", email)
                .appendQueryParameter("image", image);

        try {
            URL url = new URL(builder.build().toString());
            return url.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
