package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Button CreateAccoBtn;
    private EditText UserEmail, UserPass;
    private TextView AlreadyHaveAccount;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ProgressDialog loadingBar;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        initializeFields();

        AlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sentUserToLogin();
            }
        });

        CreateAccoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount();
            }
        });

    }


    //create account with firebase
    private void createNewAccount() {

        String email = UserEmail.getText().toString();
        String password = UserPass.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "please enter email...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "please enter password...", Toast.LENGTH_SHORT).show();
        }
        else {

            loadingBar.setTitle("Creating new account");
            loadingBar.setMessage("Please wait");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                RootRef.child("Users").child(currentUserID).setValue("");

                               RootRef.child("Users").child(currentUserID).child("device_Token")
                                       .setValue(deviceToken);

                                sendUserToMain();
                                Toast.makeText(RegisterActivity.this, "Account create successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else {

                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error :" + message , Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            }
                        }
                    });

        }
    }

    private void initializeFields() {


        CreateAccoBtn = findViewById(R.id.RestegirBtn);
        UserEmail = findViewById(R.id.RestegirEml);
        UserPass = findViewById(R.id.RestegirPassword);
        AlreadyHaveAccount = findViewById(R.id.alreatyHaveAcount);

        loadingBar = new ProgressDialog(this);
    }

    private void sentUserToLogin() {
        Intent LgnIntet = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(LgnIntet);
    }

    private void sendUserToMain() {
        Intent MainIntet = new Intent(RegisterActivity.this,MainActivity.class);
        MainIntet.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntet);
        finish();
    }

}
