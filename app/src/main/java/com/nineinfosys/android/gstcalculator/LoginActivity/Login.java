package com.nineinfosys.android.gstcalculator.LoginActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nineinfosys.android.gstcalculator.MainActivity;
import com.nineinfosys.android.gstcalculator.R;

import org.json.JSONObject;

public class Login extends AppCompatActivity {

    private static final String TAG = "AndroidBash";
    public User user;
    private EditText email;
    private EditText password;
    private TextView resetPassword;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mDataBase;
    private DatabaseReference mDataBaseGoogle;
    //FaceBook callbackManager
    private CallbackManager callbackManager;
    private SignInButton mGoogleBtn;
    private  static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

       /* Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);*/

        mAuth = FirebaseAuth.getInstance();
        mDataBase = FirebaseDatabase.getInstance().getReference().child("Users");
        mDataBaseGoogle = FirebaseDatabase.getInstance().getReference().child("Users");

        email = (EditText) findViewById(R.id.edit_text_email_id);
        password = (EditText) findViewById(R.id.edit_text_password);


        //Resetting Password of Registered Email ID
        resetPassword = (TextView)findViewById(R.id.textViewForgetPass);
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResetPassword();
            }
        });

        //FaceBook Login
        startAuthentication();

        //Google Sign In
        mGoogleBtn = (SignInButton) findViewById(R.id.signin);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Toast.makeText(Login.this, "You got an error", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                signIn();

            }
        });
    }


    //Facebook Login Authentication
    private void startAuthentication() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
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

                                    user = new User();

                                    user.setEmail(object.getString("email"));
                                    user.setId(object.getString("id").toString());
                                    user.setName(object.getString("name").toString());
                                   // user.setGender(object.getString("gender").toString());

                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(Login.this, "Welcome " + user.getName(), Toast.LENGTH_LONG).show();


                            }

                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();
                signInWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });

    }

    //FaceBook and Google OnActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInResult Signinresult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                GoogleSignInAccount account = Signinresult.getSignInAccount();
                String personName = account.getDisplayName();
                String personEmail = account.getEmail();
                String personId = account.getId();
                Log.e("personName",personName);
                Log.e("personEmail",personEmail);


                Log.e("personId",personId);
                user = new User();
                user.setName(personName);
                user.setEmail(personEmail);
                user.setId(personId);
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                Toast.makeText(this, "Google Sign in Failed", Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }

        }
        if(mGoogleApiClient!=null && mGoogleApiClient.isConnected()){
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        }


    }

    //User Set For Login With Email And Password
  /*  protected void setUpUser() {
        user = new User();
        user.setEmail(email.getText().toString());
        user.setPassword(password.getText().toString());
    }*/

    //Goes to SignUp Activity for registering User
    public void onSignUpClicked(View view) {
        Intent intent = new Intent(this, SignUp.class);

        startActivity(intent);
        finish();
    }

    //Login User
    public void onLoginClicked(View view) {
       // setUpUser();
        signIn(email.getText().toString(), password.getText().toString());

    }

    //Validation And Email Verification Of Email Password Login
    private void signIn(final String email, final String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail", task.getException());

                            user = new User();
                            if (password != user.getPassword() || email != user.getEmail()) {
                                Toast.makeText(Login.this, "Incorrect Email or Password? If Not Registered? Register First!", Toast.LENGTH_SHORT).show();
                            }



                        } else {
                            FirebaseUser mUser = mAuth.getCurrentUser();
                            if (mUser.isEmailVerified()) {
                                Toast.makeText(Login.this,"You are in =)",Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                finish();
                                startActivity(intent);

                            }

                            else {

                                //---- HERE YOU SEND THE EMAIL
                              //  mUser.sendEmailVerification();
                                Toast.makeText(Login.this,"Verify your email first...",Toast.LENGTH_LONG).show();
                                FirebaseAuth.getInstance().signOut();
                            }



                        }

                        hideProgressDialog();
                    }
                });



    }



    //Validation checked
    private boolean validateForm() {
        boolean valid = true;

        String userEmail = email.getText().toString();
        if (TextUtils.isEmpty(userEmail)) {
            email.setError("Required.");
            valid = false;
        } else {
            email.setError(null);
        }

        String userPassword = password.getText().toString();
        if (TextUtils.isEmpty(userPassword)) {
            password.setError("Required.");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }



    //Facebook Sign In
    private void signInWithFacebook(AccessToken token) {
        Log.d(TAG, "signInWithFacebook:" + token);

               showProgressDialog();


        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(Login.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                            signOut();
                        }else{
                            CreateNewUserInDatabase();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        hideProgressDialog();
                    }
                });
    }

    // [END auth_with_facebook]
    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        //mAuth.signOut();
    }
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


    private void signIn() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        showProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);


        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        else if(user!=null){
                            CreateGoogleUserInDataBase();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        // ...
                        hideProgressDialog();
                    }
                });
    }

    //Password Reseting Code
    private void ResetPassword() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_reset_password, null);
        dialogBuilder.setView(dialogView);
        user = new User();
        final EditText editEmail = (EditText) dialogView.findViewById(R.id.email);
        final Button btnReset = (Button) dialogView.findViewById(R.id.btn_reset_password);
        final ProgressBar progressBar1 = (ProgressBar) dialogView.findViewById(R.id.progressBar);
        final AlertDialog dialog = dialogBuilder.create();

        btnReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String emailid = editEmail.getText().toString().trim();

                if (TextUtils.isEmpty(emailid)) {
                    editEmail.setError("Required");
                    Toast.makeText(getApplication(), "Enter Registered Email Id", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar1.setVisibility(View.VISIBLE);
                mAuth.sendPasswordResetEmail(emailid)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    Toast.makeText(Login.this, "Password Resetting Link Sent To Email Id ", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e(TAG, "sendEmailVerification", task.getException());
                                    Toast.makeText(Login.this, "Enter Registered Email Id.", Toast.LENGTH_SHORT).show();
                                }

                                progressBar1.setVisibility(View.GONE);
                               // dialog.dismiss();
                            }
                        });

            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


    private void CreateNewUserInDatabase(){

        String user_id = mAuth.getCurrentUser().getUid();
        DatabaseReference current_user_db = mDataBase.child(user_id);
        current_user_db.child("name").setValue(user.getName());
        current_user_db.child("FacebookId").setValue(user.getId());
        current_user_db.child("Email").setValue(user.getEmail());
       // current_user_db.child("Gender").setValue(user.getGender());
    }

    private void CreateGoogleUserInDataBase(){
        String user_id = mAuth.getCurrentUser().getUid();
        DatabaseReference current_user_db = mDataBaseGoogle.child(user_id);
        current_user_db.child("name").setValue(user.getName());
        current_user_db.child("GoogleId").setValue(user.getId());
        current_user_db.child("Email").setValue(user.getEmail());

    }
}