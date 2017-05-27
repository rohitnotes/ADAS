package com.example.mego.adas.auth;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mego.adas.MainActivity;
import com.example.mego.adas.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Activity used for signing in
 */
public class SignUpActivity extends AppCompatActivity {

    /**
     * Tag for the logs
     */
    private static final String LOG_TAG = SignUpActivity.class.getSimpleName();

    /**
     * Constants for the saving the values in saved instance
     */
    public static final String USERS = "users";
    public static final String FIREBASE_PROPERTY_TIMESTAMP = "timestamp";


    /**
     * User information
     */
    private String fullName, email, password, phoneNumber, location;


    /**
     * UI Element
     */
    private TextView termsAndConditionTextView;
    private Button signUpButton;
    private EditText fullNameEditText, emailEditText, passwordEditText, phoneNumberEditText, locationEditText;
    private TextInputLayout fullNameWrapper, emailWrapper, passwordWrapper, phoneNumberWrapper, locationWrapper;
    private ProgressDialog mProgressDialog;

    /**
     * Firebase Authentication
     */
    private FirebaseAuth mFirebaseAuth;

    /**
     * Firebase objects
     * to specific part of the database
     */
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_sign_up);

        initializeScreen();

        //initialize the Firebase auth object
        mFirebaseAuth = FirebaseAuth.getInstance();

        //set up the firebase
        mFirebaseDatabase = FirebaseDatabase.getInstance();


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                AuthenticationUtilities.hideKeyboard(SignUpActivity.this);
                if (AuthenticationUtilities.isAvailableInternetConnection(getApplicationContext())) {
                    createAccount(email, password);
                } else {
                    Toast.makeText(SignUpActivity.this, R.string.error_message_failed_sign_in_no_network,
                            Toast.LENGTH_SHORT).show();
                }
            }


        });

        termsAndConditionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent termsAndConditionsIntent = new Intent(SignUpActivity.this, TermsAndConditionsActivity.class);
                startActivity(termsAndConditionsIntent);
            }
        });
    }


    /**
     * Helper method to make the sign up process
     *
     * @param email
     * @param password
     */
    private void createAccount(final String email, final String password) {
        if (!validateForm()) {
            return;
        }
        showProgressDialog();

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showErrorDialog(e.getLocalizedMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                FirebaseUser user = authResult.getUser();
                String uid = user.getUid();
                getUserInfo();
                createUserInFirebaseHelper(uid);
                hideProgressDialog();
                signIn(email, password);
            }
        });
    }

    /**
     * Helper method to create a user in firebase database
     *
     * @param uid the user unique id
     */
    private void createUserInFirebaseHelper(String uid) {
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child(USERS).child(uid);
        mUsersDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /* If there is no user, make one */
                if (dataSnapshot.getValue() == null) {
                    /* Set raw version of date to the ServerValue.TIMESTAMP value and save into dateCreatedMap */
                    HashMap<String, Object> timestampJoined = new HashMap<String, Object>();
                    timestampJoined.put(FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

                    User newUser = new User(email, phoneNumber, location, fullName, timestampJoined);
                    mUsersDatabaseReference.setValue(newUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    /**
     * Helper Method to sign the user up after creating an account
     *
     * @param email
     * @param password
     */
    private void signIn(String email, String password) {
        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent mainIntent = new Intent(SignUpActivity.this, MainActivity.class);
                            //clear the application stack (clear all  former the activities)
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showErrorDialog(e.getLocalizedMessage());
            }
        });
    }


    /**
     * Helper method to validate the data from the edit text
     *
     * @return
     */
    private boolean validateForm() {
        boolean valid = true;

        String email = emailEditText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailWrapper.setError(getString(R.string.error_message_required));
            valid = false;
        } else if (!AuthenticationUtilities.isEmailValid(email)) {
            emailWrapper.setError(getString(R.string.error_message_valid_email));
            valid = false;
        } else {
            emailWrapper.setError(null);
        }

        String password = passwordEditText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordWrapper.setError(getString(R.string.error_message_required));
            valid = false;
        } else if (!AuthenticationUtilities.isPasswordValid(password)) {
            passwordWrapper.setError(getString(R.string.password_not_strong));
            valid = false;
        } else {
            passwordWrapper.setError(null);
        }

        String fullName = fullNameEditText.getText().toString();
        if (TextUtils.isEmpty(fullName) || !AuthenticationUtilities.isUserNameValid(fullName)) {
            fullNameWrapper.setError(getString(R.string.error_message_required));
            valid = false;
        } else {
            fullNameWrapper.setError(null);
        }

        String location = locationEditText.getText().toString();
        if (TextUtils.isEmpty(location) || !AuthenticationUtilities.isUserNameValid(location)) {
            locationWrapper.setError(getString(R.string.error_message_required));
            valid = false;
        } else {
            locationWrapper.setError(null);
        }

        String phoneNumber = phoneNumberEditText.getText().toString();
        if (TextUtils.isEmpty(phoneNumber) || !AuthenticationUtilities.isUserNameValid(phoneNumber)) {
            phoneNumberWrapper.setError(getString(R.string.error_message_required));
            valid = false;
        } else if (!AuthenticationUtilities.isPhoneNumberValid(phoneNumber)) {
            phoneNumberWrapper.setError(getString(R.string.error_message_valid_number));
            valid = false;
        } else {
            phoneNumberWrapper.setError(null);
        }

        return valid;
    }

    /**
     * Helper method to show progress dialog
     */
    public void showProgressDialog() {

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.sign_up_loading));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    /**
     * Helper method to get the user information form the edit text
     */
    private void getUserInfo() {
        fullName = fullNameEditText.getText().toString();
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();
        phoneNumber = phoneNumberEditText.getText().toString();
        location = locationEditText.getText().toString();
    }

    /**
     * Helper method to hide progress dialog
     */
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideProgressDialog();
    }


    /**
     * show a dialog that till that the reset process is done
     */
    private void showErrorDialog(String error) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
        builder.setMessage(error);
        builder.setTitle(R.string.error);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        //create and show the alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Link the layout element from XML to Java
     */
    private void initializeScreen() {
        termsAndConditionTextView = (TextView) findViewById(R.id.terms_conditions_textView_sign_up_activity);

        signUpButton = (Button) findViewById(R.id.sign_up_Button_sign_up_activity);

        fullNameEditText = (EditText) findViewById(R.id.full_name_editText_sign_up_activity);
        emailEditText = (EditText) findViewById(R.id.email_editText_sign_up_activity);
        passwordEditText = (EditText) findViewById(R.id.password_editText_sign_up_activity);
        phoneNumberEditText = (EditText) findViewById(R.id.phone_number_editText_sign_up_activity);
        locationEditText = (EditText) findViewById(R.id.location_editText_sign_up_activity);

        fullNameWrapper = (TextInputLayout) findViewById(R.id.full_name_wrapper_sign_up_activity);
        emailWrapper = (TextInputLayout) findViewById(R.id.email_wrapper_sign_up_activity);
        passwordWrapper = (TextInputLayout) findViewById(R.id.password_wrapper_sign_up_activity);
        phoneNumberWrapper = (TextInputLayout) findViewById(R.id.phone_number_wrapper_sign_up_activity);
        locationWrapper = (TextInputLayout) findViewById(R.id.location_wrapper_sign_up_activity);

    }
}
