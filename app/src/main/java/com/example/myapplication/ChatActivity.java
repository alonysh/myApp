package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Models.Messages;
import com.example.myapplication.adapter.MessageAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String RecivID, RecivName, recivImage, messageSendId;

    private TextView Uname, ULastSeen;
    private CircleImageView UImage;

    private ImageButton sendBtn;
    private EditText InputText;

    private Toolbar ChatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSendId= mAuth.getCurrentUser().getUid();

        RecivID= getIntent().getExtras().get("UserId").toString();
        RecivName= getIntent().getExtras().get("UserName").toString();
        recivImage= getIntent().getExtras().get("UserImage").toString();
        RootRef = FirebaseDatabase.getInstance().getReference();


        InitializeControllers();

        Uname.setText(RecivName);
        Picasso.get().load(recivImage).placeholder(R.drawable.profile_image).into(UImage);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SendMessage();
            }
        });

    }

    private void InitializeControllers() {



        ChatToolBar = findViewById(R.id.ChatTookBar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView =  layoutInflater.inflate(R.layout.custam_chat_ar, null);
        actionBar.setCustomView(actionBarView);


        UImage = findViewById(R.id.customProfImage);
        Uname = findViewById(R.id.customProfName);
        ULastSeen = findViewById(R.id.customUserLast);
        sendBtn = findViewById(R.id.sendMtn);
        InputText = findViewById(R.id.inputMessage);

        messageAdapter = new MessageAdapter(messagesList);
        recyclerView = findViewById(R.id.messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Messages").child(messageSendId).child(RecivID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void  SendMessage(){
        String messageText = InputText.getText().toString();

        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
        }
        else {
            String messageSendRef = "Messages/" + messageSendId + "/" + RecivID;
            String messageRecievRef = "Messages/" + RecivID + "/" + messageSendId;

            DatabaseReference databaseReference = RootRef.child("Messages")
                    .child(messageSendId).child(RecivID).push();

            String messagePushId = databaseReference.getKey();

            Map messageBody = new HashMap();
            messageBody.put("message", messageText);
            messageBody.put("type", "text");
            messageBody.put("from", messageSendId);

            Map messageDetails = new HashMap();
            messageDetails.put(messageSendRef + "/" + messagePushId, messageBody);
            messageDetails.put(messageRecievRef + "/" + messagePushId, messageBody);

            RootRef.updateChildren(messageDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful()){

                        Toast.makeText(ChatActivity.this, "message sent...", Toast.LENGTH_SHORT).show();
                    }
                    
                    else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }

                    InputText.setText("");
                }
                
            });

        }
    }
}
