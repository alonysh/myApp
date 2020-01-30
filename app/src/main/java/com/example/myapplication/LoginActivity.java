package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {


     private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference URef;

    private Button LgnBtn, phonLgnBtn;
    private EditText UserEmail, UserPass;
    private TextView needNewAccount, ForgetPass;
    private ProgressDialog loadingBar;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        URef = FirebaseDatabase.getInstance().getReference().child("Users");

        initializeFields();

        needNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            sendUserToRegister();
            }
        });


        LgnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AllowLgn();
            }
        });

        phonLgnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent PhoneLgnIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(PhoneLgnIntent);
            }
        });
    }


    //login with firebase
    private void AllowLgn() {

        String email = UserEmail.getText().toString();
        String password = UserPass.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "please enter email...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "please enter password...", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Sign in");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                String currentUId = mAuth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                URef.child(currentUId).child("device_Token")
                                        .setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){

                                                    sendUserToMain();
                                                    Toast.makeText(LoginActivity.this, "Looged in sucssefully", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                    Log.i(TAG, "onComplete: Looged suvssfully");
                                                }
                                            }
                                        });
                            }
                            else {

                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error :" + message , Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Log.i(TAG, "onComplete:looged Error ");
                            }
                        }
                    });
        }
    }

    private void initializeFields() {

        LgnBtn = findViewById(R.id.LgnBtn);
        phonLgnBtn = findViewById(R.id.ponLgnBtn);
        UserEmail = findViewById(R.id.LgnEml);
        UserPass = findViewById(R.id.LgnPassword);
        needNewAccount = findViewById(R.id.HaveAcount);
        ForgetPass = findViewById(R.id.FrgtPassword);
        loadingBar = new ProgressDialog(this);





    }



    private void sendUserToMain() {
        Intent MainIntet = new Intent(LoginActivity.this,MainActivity.class);
        MainIntet.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntet);
        finish();
    }
    private void sendUserToRegister() {
        Intent RegIntet = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(RegIntet);
    }

}


