package com.bignerdranch.android.registrar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.android.registrar.utilities.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import butterknife.ButterKnife;

public class UserActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://internship-api-v0.7.intcore.net/";

    private static final String TAG = "UserActivity";
    private static final String USER_DATA ="user-data";


    private DrawerLayout mDrawerLayout;

    private User mUser;
    private FragmentManager mFragmentManager;
    private SharedPreferences mSharedPref;

    public static Intent newIntent(Context ctx, String response){
        Intent i = new Intent(ctx, UserActivity.class);
        i.putExtra(USER_DATA, response);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);
        mSharedPref = this.getSharedPreferences(LoginActivity.API_TOKEN_SHARED_TAG,Context.MODE_PRIVATE);
        mFragmentManager = getSupportFragmentManager();
        mUser = parseUserData(getIntent().getStringExtra(USER_DATA));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_very_bright_red_24dp);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        launchFragment(menuItem);
                        return true;
                    }
                });
        View view = navigationView.getHeaderView(0);

        ImageView HeaderImageView = view.findViewById(R.id.photo_header);
        TextView userName = view.findViewById(R.id.user_name);
        TextView userEmail = view.findViewById(R.id.user_email);
        Picasso.get().load(BASE_URL+mUser.getImage()).into(HeaderImageView);
        userName.setText(mUser.getName());
        userEmail.setText(mUser.getEmail());

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private User parseUserData(String userData){
        try {
            HashMap<String, String> accountData= new HashMap<>();
            JSONObject data = new JSONObject(userData);
            data = data.getJSONObject("user");
            Gson gson = new GsonBuilder().create();
            User user = gson.fromJson(data.toString(), User.class);
            mSharedPref.edit()
                    .putString(LoginActivity.API_TOKEN, user.getApi_token())
                    .apply();
            return user;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void launchFragment(MenuItem item){
        switch (item.getItemId()){
            case R.id.profile:
                Fragment fragment = mFragmentManager.findFragmentById(R.id.fragment_container);
                if(fragment == null) {
                    fragment = ProfileFragment.newInstance(mUser);
                    mFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                }
                break;
            case R.id.logout:
                mSharedPref.edit()
                        .putString(LoginActivity.API_TOKEN,"")
                        .commit();
                Intent i = new Intent(UserActivity.this,LoginActivity.class);
                startActivity(i);
                finish();
        }
    }
}
