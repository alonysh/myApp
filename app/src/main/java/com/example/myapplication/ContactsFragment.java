package com.example.myapplication;


import android.os.Bundle;
import android.util.Log;
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
public class ContactsFragment extends Fragment {
    private static final String TAG = "ContactsFragment";

    private View ContactsView;
    private RecyclerView myCoontacts;

    private DatabaseReference ContactsRef, UserRef;
    private FirebaseAuth mAuth;
    private String currentUserId;


    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView =  inflater.inflate(R.layout.fragment_contacts, container, false);
        myCoontacts =  ContactsView.findViewById(R.id.contactList);
        myCoontacts.setLayoutManager(new LinearLayoutManager(getContext()));


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);



        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContactsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {

                String userIds = getRef(position).getKey();

                UserRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("image")){
                            String UserImage = dataSnapshot.child("image").getValue().toString();
                            String profName = dataSnapshot.child("name").getValue().toString();
                            String profStatus = dataSnapshot.child("status").getValue().toString();

                            holder.Uname.setText(profName);
                            holder.Ustatus.setText(profStatus);
                            Picasso.get().load(UserImage).placeholder(R.drawable.profile_image).into(holder.profImg);
                            Log.i(TAG, "onDataChange: secssful");

                        }
                        else {
                            String profName = dataSnapshot.child("name").getValue().toString();
                            String profStatus = dataSnapshot.child("status").getValue().toString();

                            holder.Uname.setText(profName);
                            holder.Ustatus.setText(profStatus);
                            Log.i(TAG, "onDataChange: secssful");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
              View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.usersdiplaylayout, parent,false);
              ContactsViewHolder viewHolder = new ContactsViewHolder(view);
              return viewHolder;
            }
        };

        myCoontacts.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        TextView Uname, Ustatus;
        CircleImageView profImg;

        public ContactsViewHolder(@NonNull View itemView) {

            super(itemView);

            Uname = itemView.findViewById(R.id.UserProfName);
            Ustatus = itemView.findViewById(R.id.UserSatus);
            profImg = itemView.findViewById(R.id.UsrsrProfImage);
        }
    }
}
