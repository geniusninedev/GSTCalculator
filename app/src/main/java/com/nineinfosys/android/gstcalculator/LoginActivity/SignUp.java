package com.nineinfosys.android.gstcalculator.LoginActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nineinfosys.android.gstcalculator.R;

public class SignUp extends AppCompatActivity {

    private static final String TAG = "Login";
    //Add YOUR Firebase Reference URL instead of the following URL
    private DatabaseReference mRef;
    private User user;
    private EditText name;
    private EditText phoneNumber;
    private EditText email;
    private EditText password;
    private FirebaseAuth mAuth;
    private Button SignUp_User;
    private TextView UserLogin;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mProgressDialog;
    final String emailpattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

     /*   Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sign Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);*/

        name = (EditText) findViewById(R.id.edit_text_username);
        phoneNumber = (EditText) findViewById(R.id.edit_text_phone_number);
        email = (EditText) findViewById(R.id.edit_text_new_email);
        password = (EditText) findViewById(R.id.edit_text_new_password);

        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");


        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }

            }
        };

        SignUp_User = (Button)findViewById(R.id.btn_user_sign_up);
        SignUp_User.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validateForm()){
                    return;
                    //validateForm();
                }

                if(password.length()<=5){
                    Toast.makeText(SignUp.this, "Password Should Contain Minimum 6 Characters", Toast.LENGTH_SHORT).show();
                    password.setText("");
                }

                if(!email.getText().toString().trim().matches(emailpattern)){
                    Toast.makeText(SignUp.this, "Enter Valid Email id?", Toast.LENGTH_SHORT).show();
                    email.setText("");
                }

                onSignUpClicked();
            }
        });


        UserLogin = (TextView)findViewById(R.id.textViewRegistered);
        UserLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(SignUp.this,Login.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);


    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    //This method sets up a new User by fetching the user entered details.
    protected void setUpUser() {
        user = new User();
        user.setName(name.getText().toString());
        user.setPhoneNumber(phoneNumber.getText().toString());
        user.setEmail(email.getText().toString());
        user.setPassword(password.getText().toString());
    }

    public void onSignUpClicked() {

        if(!validateForm()){
            return;
            //validateForm();
        }
        else {

            createNewAccount(email.getText().toString(), password.getText().toString());
            showProgressDialog();

           //AlertDialogBox();
        }

    }


    private void createNewAccount(final String email, final String password) {
        Log.d(TAG, "createNewAccount:" + email);
        if (!validateForm()) {
            return;
        }
        //This method sets up a new User by fetching the user entered details.
        setUpUser();
        //This method  method  takes in an email address and password, validates them and then creates a new user
        // with the createUserWithEmailAndPassword method.
        // If the new account was created, the user is also signed in, and the AuthStateListener runs the onAuthStateChanged callback.
        // In the callback, you can use the getCurrentUser method to get the user's account data.
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        hideProgressDialog();
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignUp.this, "Already Registered!", Toast.LENGTH_SHORT).show();
                        } else {
                            onAuthenticationSucess();
                        }

                    }
                });

    }

    private void onAuthenticationSucess() {
        // Write new user
        saveNewUser();
        sendEmailVerification();
        signOut();
        finish();
        startActivity(new Intent(SignUp.this, Login.class));


    }

  private void saveNewUser() {

        String user_id = mAuth.getCurrentUser().getUid();
        DatabaseReference current_user_db = mRef.child(user_id);
        current_user_db.child("name").setValue(user.getName());
        current_user_db.child("id").setValue(user.getId());
        current_user_db.child("Email").setValue(user.getEmail());
        current_user_db.child("Password").setValue(user.getPassword());
        current_user_db.child("Phone Number").setValue(user.getPhoneNumber());

    }


    private void signOut() {
        mAuth.signOut();
    }



    private void sendEmailVerification() {
        // Disable button
        findViewById(R.id.btn_user_sign_up).setEnabled(false);
       // Toast.makeText(SignUp.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        Log.e(TAG, "sendEmailVerification");
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                findViewById(R.id.btn_user_sign_up).setEnabled(true);
                Log.e(TAG, "sendEmailVerification", task.getException());

                if (task.isSuccessful()) {
                    Toast.makeText(SignUp.this,
                            "Verification email sent to " + user.getEmail(),
                            Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "sendEmailVerification", task.getException());
                    Toast.makeText(SignUp.this,
                            "Failed to send verification email.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        // [END send_email_verification]
    }

    //This method, validates email address and password
    private boolean validateForm() {
        boolean valid = true;

        if(name.getText().toString().equals("")){
            name.setError("Required");
        }

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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {

            finish();
            startActivity(new Intent(SignUp.this, Login.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(SignUp.this, Login.class));
    }
}