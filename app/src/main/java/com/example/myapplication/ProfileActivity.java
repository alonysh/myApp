package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";


        private String receiveUsId, senderUserId, CurrentStat;

        private CircleImageView UserImg;
        private TextView UserName, UserStatus;
        private Button sendMessge, declinebtn;

        private DatabaseReference UserRef, ChatReqRef, ContactsRef, NotificationRef;
        private FirebaseAuth mAuth;


// manage the friend list. add, remove
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatReqRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notification");



        receiveUsId = getIntent().getExtras().get("visit user id").toString();
        senderUserId = mAuth.getCurrentUser().getUid();


        UserImg = findViewById(R.id.visitProfImg);
        UserName = findViewById(R.id.visitUsName);
        UserStatus = findViewById(R.id.visitProfSatus);
        sendMessge = findViewById(R.id.sendMessageBtn);
        declinebtn = findViewById(R.id.declineMessageBtn);

        CurrentStat = "new";

        RetrieveUserInfo();
    }

        //Retrieve User Friend info to the user
    private void RetrieveUserInfo() {

    UserRef.child(receiveUsId).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

       if ((dataSnapshot.exists())  && (dataSnapshot.hasChild("image")) ){

           String userImg = dataSnapshot.child("image").getValue().toString();
           String userName = dataSnapshot.child("name").getValue().toString();
           String userSats = dataSnapshot.child("status").getValue().toString();

           Picasso.get().load(userImg).placeholder(R.drawable.profile_image).into(UserImg);
           UserName.setText(userName);
           UserStatus.setText(userSats);


            ManagChatReaueets();
           }
       else {
           String userName = dataSnapshot.child("name").getValue().toString();
           String userSats = dataSnapshot.child("status").getValue().toString();

           UserName.setText(userName);
           UserStatus.setText(userSats);

           ManagChatReaueets();

       }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });

    }

    private void ManagChatReaueets() {

        ChatReqRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(receiveUsId)){

                            String request_type = dataSnapshot.child(receiveUsId).child("request_type").getValue().toString();
                            if (request_type.equals("sent")){
                                CurrentStat  = "request_send";
                                sendMessge.setText("Cancel Chat Request");

                            }

                            else if (request_type.equals("received")){
                                CurrentStat = "request_received";
                                sendMessge.setText("Accept Chat Request");
                                declinebtn.setVisibility(View.VISIBLE);
                                declinebtn.setEnabled(true);

                                declinebtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CancelChatReq();
                                    }
                                });
                            }
                        }
                        else {
                            ContactsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiveUsId)){
                                                CurrentStat = "Friends";
                                                sendMessge.setText("remove this friend");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.i(TAG, "onCancelled: btn cancel");
                    }
                });

        if (!senderUserId.equals(receiveUsId))
        {
        sendMessge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessge.setEnabled(false);

                if (CurrentStat.equals("new")){
                    SendChatRequest();
                }

                if (CurrentStat.equals("request_send")){
                    CancelChatReq();
                }

                if (CurrentStat.equals("request_received")){
                    AcceptReq();
                }

                if (CurrentStat.equals("friends")){
                    RemoveContact();
                }

            }
        });
        }
        else {
           sendMessge.setVisibility(View.INVISIBLE);
        }
    }
    //remove friend
    private void RemoveContact() {

        ContactsRef.child(senderUserId).child(receiveUsId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                           ContactsRef.child(receiveUsId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                sendMessge.setEnabled(true);
                                                CurrentStat = "new";
                                                sendMessge.setText("Send Message");
                                                Log.i(TAG, "onComplete: request cancel");

                                                declinebtn.setVisibility(View.INVISIBLE);
                                                declinebtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }

                });

    }

    // add friend to the contact
    private void AcceptReq() {

            ContactsRef.child(senderUserId).child(receiveUsId)
                    .child("Contacts").setValue("Saved")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                ContactsRef.child(receiveUsId).child(senderUserId)
                                        .child("Contacts").setValue("Saved")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){

                                                        ChatReqRef.child(senderUserId).child(receiveUsId)
                                                                .removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if (task.isSuccessful()){
                                                                            ChatReqRef.child(receiveUsId).child(senderUserId)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            sendMessge.setEnabled(true);
                                                                                            CurrentStat = "friends";
                                                                                            sendMessge.setText("Remove this friend");

                                                                                            declinebtn.setVisibility(View.INVISIBLE);
                                                                                            declinebtn.setEnabled(false);
                                                                                            Log.i(TAG, "onComplete: friend add");

                                                                                        }
                                                                                    });
                                                                        }

                                                                    }
                                                                });
                                                }
                                            }
                                        });

                            }
                        }
                    });

    }

    //cancel  request
    private void CancelChatReq() {
        ChatReqRef.child(senderUserId).child(receiveUsId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                                ChatReqRef.child(receiveUsId).child(senderUserId)
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {
                                                    sendMessge.setEnabled(true);
                                                    CurrentStat = "new";
                                                    sendMessge.setText("Send Message");
                                                    Log.i(TAG, "onComplete: request cancel");

                                                    declinebtn.setVisibility(View.INVISIBLE);
                                                    declinebtn.setEnabled(false);
                                                }
                                            }
                                        });
                            }
                        }

                });
    }

    //send request to the friend
    private void SendChatRequest() {

        ChatReqRef.child(senderUserId).child(receiveUsId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            ChatReqRef.child(receiveUsId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                HashMap<String,String> ChatNotification = new HashMap<>();
                                                ChatNotification.put("from", senderUserId);
                                                ChatNotification.put("type", "request");

                                                NotificationRef.child(receiveUsId).push()
                                                        .setValue(ChatNotification)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){

                                                                    sendMessge.setEnabled(true);
                                                                    CurrentStat = "request_send";
                                                                    sendMessge.setText("Cancel Chat Request");
                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                    }
                });
    }


}
