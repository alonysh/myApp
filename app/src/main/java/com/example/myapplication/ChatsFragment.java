package com.example.myapplication;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Models.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragment";

    private View PrivateChatView;
    private RecyclerView ChatList;

    private DatabaseReference ChatsRef, UserRef;
    private FirebaseAuth mAuth;
    private String currentUserId;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatView = inflater.inflate(R.layout.fragment_chats, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();


        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        ChatList = PrivateChatView.findViewById(R.id.ChatsList);
        ChatList.setLayoutManager(new LinearLayoutManager(getContext()));

        UserRef  = FirebaseDatabase.getInstance().getReference().child("Users");

        return PrivateChatView;


    }


    //move to chat contact
    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new  FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ChatsHolder > adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsHolder holder, int position, @NonNull Contacts model) {

                        final  String UserId = getRef(position).getKey();
                        final String[] retImage = {"default_image"};


                        UserRef.child(UserId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){
                                    if (dataSnapshot.hasChild("image")){

                                        retImage[0] = dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage[0]).into(holder.profImage);

                                    }

                                    final String retName = dataSnapshot.child("name").getValue().toString();
                                    final String retStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.UName.setText(retName);
                                    holder.UStatus.setText("Last Seen: " + "\n" + "Date " + "Time");

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            Intent ChatIntat = new Intent(getContext(),ChatActivity.class);
                                            ChatIntat.putExtra("UserId",UserId );
                                            ChatIntat.putExtra("UserName", retName);
                                            ChatIntat.putExtra("UserImage", retImage[0]);
                                            startActivity(ChatIntat);

                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.usersdiplaylayout, parent, false);
                        return new ChatsHolder(view);
                    }
                };

        ChatList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class  ChatsHolder extends RecyclerView.ViewHolder{

        CircleImageView profImage;
        TextView UStatus, UName;

        public ChatsHolder(@NonNull View itemView) {
            super(itemView);


            profImage = itemView.findViewById(R.id.UsrsrProfImage);
            UStatus = itemView.findViewById(R.id.UserSatus);
            UName = itemView.findViewById(R.id.UserProfName);

        }
    }
}
