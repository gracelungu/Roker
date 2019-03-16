package com.bulksmskivubox.broker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class CodeVerify extends AppCompatActivity {

    private FirebaseAuth mAuth;

    String telephone, verificationId;
    Button resend, verify;
    ProgressBar spinner;
    EditText one, two, three, four, five, six;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_verify);

        mAuth = FirebaseAuth.getInstance();

        telephone = getIntent().getExtras().getString("telephone");

        //Widgets
        spinner = findViewById(R.id.spinner);
        spinner.setVisibility(View.VISIBLE);
        one = findViewById(R.id.one);
        two = findViewById(R.id.two);
        three = findViewById(R.id.three);
        four = findViewById(R.id.four);
        five = findViewById(R.id.five);
        six = findViewById(R.id.six);
        verify = findViewById(R.id.verify);
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(one.getText().toString().isEmpty()){
                    one.setError("Required");
                    return;
                }

                if(two.getText().toString().isEmpty()){
                    two.setError("Required");
                    return;
                }

                if(three.getText().toString().isEmpty()){
                    three.setError("Required");
                    return;
                }

                if(four.getText().toString().isEmpty()){
                    four.setError("Required");
                    return;
                }

                if(five.getText().toString().isEmpty()){
                    five.setError("Required");
                    return;
                }

                if(six.getText().toString().isEmpty()){
                    six.setError("Required");
                    return;
                }

                validateCode();

            }
        });


        //Verify the phone number
        phoneAuth(telephone);


        resend = findViewById(R.id.resend_code);
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CodeVerify.this, "Please wait..", Toast.LENGTH_SHORT).show();
                phoneAuth(telephone);
            }
        });


    }


    public void phoneAuth(String phoneNumber){

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                10,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                        signInWithPhoneAuthCredential(phoneAuthCredential);

                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {

                        spinner.setVisibility(View.INVISIBLE);

                        Toast.makeText(CodeVerify.this, "The verification has failed \n"+e.getMessage(), Toast.LENGTH_SHORT).show();

                        Log.i("Phone Verification",e.getMessage());

                    }

                    @Override
                    public void onCodeSent(String id, PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                        verificationId = id;

                        spinner.setVisibility(View.INVISIBLE);
                        Toast.makeText(CodeVerify.this, "The code was sent to your phone", Toast.LENGTH_SHORT).show();
                        super.onCodeSent(id, forceResendingToken);

                    }
                });

    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        spinner.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Code detected", Toast.LENGTH_SHORT).show();

        Toast.makeText(this, "Signing in please wait..", Toast.LENGTH_SHORT).show();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            spinner.setVisibility(View.INVISIBLE);

                            Toast.makeText(CodeVerify.this, "Verification success", Toast.LENGTH_SHORT).show();

                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SIGNIN", "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();

                            //Save the data in DB
                            saveUser(user);

                        } else {

                            spinner.setVisibility(View.INVISIBLE);

                            // Sign in failed, display a message and update the UI
                            Log.w("SIGNIN", "signInWithCredential:failure", task.getException());

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(CodeVerify.this, "The verification code is invalid", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
    }


    public void validateCode(){

        String code = one.getText().toString()+two.getText()+three.getText()+four.getText()+five.getText()+six.getText();

        PhoneAuthCredential credential;

        try{

            credential = PhoneAuthProvider.getCredential(verificationId, code);

        }catch(Exception e){
            Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = task.getResult().getUser();

                            saveUser(user);

                            Toast.makeText(CodeVerify.this, "Verification Success", Toast.LENGTH_SHORT).show();

                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(CodeVerify.this, "Verification Failed, Invalid credentials", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

    }

    public void saveUser(FirebaseUser user){

        spinner.setVisibility(View.VISIBLE);

        User newUser = new User(telephone);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");
        ref.child(user.getUid()).setValue(newUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        spinner.setVisibility(View.GONE);
                        startActivity(new Intent(CodeVerify.this,MapsActivity.class));
                        finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        spinner.setVisibility(View.GONE);
                        Toast.makeText(CodeVerify.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }
}
