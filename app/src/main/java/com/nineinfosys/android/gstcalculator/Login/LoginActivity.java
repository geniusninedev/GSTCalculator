package com.nineinfosys.android.gstcalculator.Login;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.nineinfosys.android.gstcalculator.MainActivity;
import com.nineinfosys.android.gstcalculator.R;


import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    //Firebase and facebook variables
    private LoginButton facebookLoginButton;
    private CallbackManager callbackManager;
    private FirebaseAuth firebaseAuth;


    //User for Facebook data
    private UserFacebookData userFacebookData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_login);
        startAuthentication();
    }

    private void startAuthentication(){
        callbackManager = CallbackManager.Factory.create();
        firebaseAuth = FirebaseAuth.getInstance();
        facebookLoginButton = (LoginButton) findViewById(R.id.login_button);
        facebookLoginButton.setReadPermissions(Arrays.asList("email", "public_profile", "user_friends", "user_birthday", "user_location"));
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {


                Log.e("LoginActivity: ", "call back success found");
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                Log.e("LoginActivity: ", "Graph method called inside register call back");
                                Log.e("response: ", response + "");
                                try {

                                    userFacebookData = new UserFacebookData();

                                    userFacebookData.setEmail(object.getString("email"));
                                    userFacebookData.setFacebookid(object.getString("id").toString());
                                    userFacebookData.setUsername(object.getString("name").toString());
                                    userFacebookData.setGender(object.getString("gender").toString());

                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(LoginActivity.this, "Welcome " + userFacebookData.getUsername(), Toast.LENGTH_LONG).show();


                            }

                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e("LoginActivity: ", "Serious Error token seems not valid " );
                Log.e("Important --------", error.toString());

            }

        });


    }
    private void handleFacebookAccessToken(AccessToken token){
        Log.d("FB:", "handleFacebookAccessToken:" + token);
        Log.e("LoginActivity:", "Handle token process statted");
        AuthCredential credential= FacebookAuthProvider.getCredential(token.getToken());

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {




                if (!task.isSuccessful()) {



                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }

                else {
                    //updateUserProfile();
                    //uploadAppUsersDataToAzure();
                    Log.e("LoginActivity:", "Logged in and directing to main activity");
                    Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    finish();



                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("LoginActivity:", "On activity result called");
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
        facebookLoginButton.setVisibility(View.GONE);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_BACK:
              finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    //used this when mobile orientaion is changed
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }
}