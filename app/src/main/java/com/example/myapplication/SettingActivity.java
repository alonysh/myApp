package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private static final String TAG = "SettingActivity";


    private Button UpdtBtn;
    private EditText Uname, Ustatus;
    private CircleImageView UserImage;
    private String UserID;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private static final int GALLERTPICK = 1;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;


    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();
        UserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        initializeFields();

        Uname.setVisibility(View.INVISIBLE);


        UpdtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSetting();
            }


        });

        RetrieveUserInfo();

        UserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERTPICK );
            }
        });
    }



    private void initializeFields() {
        UpdtBtn = findViewById(R.id.UpdateBtn);
        Uname = findViewById(R.id.setUesrName);
        Ustatus = findViewById(R.id.setProfileStatus);
        UserImage = findViewById(R.id.SetProfileImage);
        loadingBar = new ProgressDialog(this);

        toolbar = findViewById(R.id.settingBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("setting");
    }


    @Override
    //crop image
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
        {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERTPICK &&resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){

                loadingBar.setTitle("set Profile inage");
                loadingBar.setMessage("please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();


            // update image to firebase
                final Uri resultUri = result.getUri();
                final StorageReference filePath = UserProfileImageRef.child(UserID + ".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                final String downloadUrl = uri.toString();
                                RootRef.child("Users").child(UserID).child("image").setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SettingActivity.this, "Profile image stored to firebase database successfully.", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                    Log.i(TAG, "onComplete: Profile image stored to firebase database successfully");
                                                } else {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SettingActivity.this, "Error Occurred..." + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                            }
                        });
                    }




              /* Uri resultUri = result.getUri();

                StorageReference FilePath = UserProfileImageRef.child(UserID + ".jpg");
                FilePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()){
                            Toast.makeText(SettingActivity.this, "Profile image uploaded succssfully", Toast.LENGTH_SHORT).show();

                            final  String downloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();

                            RootRef.child("Users").child(UserID).child("image")
                                    .setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                       if (task.isSuccessful()){
                                           Toast.makeText(SettingActivity.this, "image save in datebase...", Toast.LENGTH_SHORT).show();
                                           loadingBar.dismiss();
                                       }
                                       else {
                                           String message = task.getException().toString();
                                           Toast.makeText(SettingActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                           loadingBar.dismiss();


                                       }
                                        }
                                    });
                        }
                        else {
                            String message= task.getException().toString();
                            Toast.makeText(SettingActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "onComplete: Error" + message);
                            loadingBar.dismiss();

                        }
                    }*/
                });
            }
        }

    }

    private void UpdateSetting() {

        String SetUesrName = Uname.getText().toString();
        String SetUesrStauts = Ustatus.getText().toString();

        if (TextUtils.isEmpty(SetUesrName)){
            Toast.makeText(this, "please write your username...", Toast.LENGTH_LONG).show();
        }

        if (TextUtils.isEmpty(SetUesrStauts)){
            Toast.makeText(this, "please write your status...", Toast.LENGTH_LONG).show();
        }

        else {
            // update user profile
            HashMap<String ,Object> profileMap = new HashMap<>();
                profileMap.put("uid", UserID);
                profileMap.put("name", SetUesrName);
                profileMap.put("status", SetUesrStauts);
                RootRef.child("Users").child(UserID).updateChildren(profileMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(SettingActivity.this, "Profile update successfully...", Toast.LENGTH_SHORT).show();
                                    sendUserToMain();
                                    Log.i(TAG, "onComplete: upate succssful");
                                    
                                }
                                else {
                                    String messega = task.getException().toString();
                                    Toast.makeText(SettingActivity.this, "Error:" + messega, Toast.LENGTH_SHORT).show();
                                    Log.i(TAG, "onComplete: error update");
                                }
                            }
                        });


        }
    }

    //show info
    private void RetrieveUserInfo() {
        RootRef.child("Users").child(UserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))){

                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStasus = dataSnapshot.child("status").getValue().toString();
                    String retrieveProfileImg = dataSnapshot.child("image").getValue().toString();
                    Log.i(TAG, "onDataChange: image update");


                    Uname.setText(retrieveUserName);
                    Ustatus.setText(retrieveStasus);
                    Picasso.get().load(retrieveProfileImg).into(UserImage);


                    Log.i(TAG, "onDataChange: name, status update");
                }
                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){

                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStasus = dataSnapshot.child("status").getValue().toString();


                    Uname.setText(retrieveUserName);
                    Ustatus.setText(retrieveStasus);

                    Log.e(TAG, "onDataChange:name, status update ");
                }
                else {
                    Uname.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingActivity.this, "Please set your profile information", Toast.LENGTH_SHORT).show();
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void sendUserToMain() {
        Intent MainIntet = new Intent(SettingActivity.this,MainActivity.class);
        MainIntet.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntet);
        finish();
    }

}

