package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class GroupChatActivity extends AppCompatActivity {

    private static final String TAG = "GroupChatActivity";

    private Toolbar toolbar;
    private ImageButton imageButton;
    private EditText UesrMessage;
    private ScrollView scrollView;
    private TextView displatTextMessages;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessgaeKetRef ;

    private String currentGroupName, currentUserId, currentUserName, cuurentDate, currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth =FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);






        initializeField();

        GetUserInfo();

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMassage();

                UesrMessage.setText("");

                scrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });

    }




    private void initializeField() {

        toolbar =  (Toolbar)findViewById(R.id.groupChatBarLayout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentGroupName);

        UesrMessage = (EditText)findViewById(R.id.inputGroupMessage);
        imageButton = (ImageButton)findViewById(R.id.sendMessageButtun);
        displatTextMessages = (TextView)findViewById(R.id.GroupChatText);
        scrollView = findViewById(R.id.MyScrool);



    }
    
    // user information 
    private void GetUserInfo() {
        UsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

              if (dataSnapshot.exists()){
                  currentUserName = dataSnapshot.child("name").getValue().toString();
                  Log.i(TAG, "onDataChange: get info");
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onStart() {

        super.onStart();
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }

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

    

    //save massage on firebase
    private void saveMassage() {

        String message = UesrMessage.getText().toString();
        String messageKey = GroupNameRef.push().getKey();

        if (TextUtils.isEmpty(message)){
            Toast.makeText(this, "please write message first...", Toast.LENGTH_SHORT).show();
        }
        else {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            cuurentDate = currentDateFormat.format(calForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm");
            currentTime = currentTimeFormat.format(callForTime.getTime());
            Log.i(TAG, "saveMassage: get time");

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessgaeKetRef = GroupNameRef.child(messageKey);

            HashMap<String, Object> messageinfoMap = new HashMap<>();
            messageinfoMap.put("name", currentUserName);
            messageinfoMap.put("message", message);
            messageinfoMap.put("date", cuurentDate);
            messageinfoMap.put("time", currentTime);
            GroupMessgaeKetRef.updateChildren(messageinfoMap);
            Log.i(TAG, "saveMassage: save massage");





        }
    }
    
    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()){
            String chatDate = (String)  ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String)  ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String)  ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String)  ((DataSnapshot)iterator.next()).getValue();

            displatTextMessages.append(chatName + ":\n" + chatMessage + "\n" + chatTime + "  " + chatDate +"\n\n\n" );

            scrollView.fullScroll(ScrollView.FOCUS_DOWN);


        }
        
    }
}
