package com.example.myapplication;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Models.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
public class RequestsFragment extends Fragment {
    private static final String TAG = "RequestsFragment";

    private View requestsFragment;
    private RecyclerView recyclerView;


    private DatabaseReference databaseReference, UserRef, conractRef;
    private FirebaseAuth mAuth;
    private String curentUserId;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestsFragment = inflater.inflate(R.layout.fragment_requsets, container, false);

        mAuth = FirebaseAuth.getInstance();
        curentUserId = mAuth.getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        conractRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        recyclerView= requestsFragment.findViewById(R.id.chatReqList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        return requestsFragment;
    }


    @Override
    public void onStart() {


        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(databaseReference.child(curentUserId), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ReuestsViserHolder > adapter =
                new FirebaseRecyclerAdapter<Contacts, ReuestsViserHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ReuestsViserHolder holder, int position, @NonNull Contacts model) {

                        holder.itemView.findViewById(R.id.requestAccptBtn).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.requestCancelBtn).setVisibility(View.VISIBLE);

                        final String listUserId = getRef(position).getKey();

                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){
                                    String type = dataSnapshot.getValue().toString();

                                    if (type.equals("received")){

                                        UserRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                if (dataSnapshot.hasChild("image")){


                                                    final String requsetUSImage = dataSnapshot.child("image").getValue().toString();



                                                    Picasso.get().load(requsetUSImage).into(holder.ProfInamg);
                                                }

                                                final String requsetUName = dataSnapshot.child("name").getValue().toString();
                                                final String requsetUStatus = dataSnapshot.child("status").getValue().toString();

                                                holder.Uname.setText(requsetUName);
                                                holder.Ustatus.setText("want to connect with you");


                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {

                                                        CharSequence options[] = new CharSequence[]{
                                                                "Accept",
                                                                "Cancel"
                                                        };
                                                        AlertDialog.Builder  builder= new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requsetUName + "  Chat Request");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, final int i) {

                                                                //  save Contact to the list
                                                                if (i == 0){
                                                                    conractRef.child(curentUserId).child(listUserId).child("Contacts")
                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if (task.isSuccessful()){
                                                                                conractRef.child(listUserId).child(curentUserId).child("Contacts")
                                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                        if (task.isSuccessful()){
                                                                                            databaseReference.child(curentUserId).child(listUserId)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                                            if (task.isSuccessful()){
                                                                                                                databaseReference.child(listUserId).child(curentUserId)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                                                if (task.isSuccessful()){

                                                                                                                                    Toast.makeText(getContext(), " Contact save", Toast.LENGTH_SHORT).show();
                                                                                                                                    Log.i(TAG, "onComplete: contact save");

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
                                                                    });
                                                                }

                                                                //  Delete Contact from list
                                                                if (i == 1){

                                                                    databaseReference.child(curentUserId).child(listUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if (task.isSuccessful()){
                                                                                        databaseReference.child(listUserId).child(curentUserId)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                                        if (task.isSuccessful()){

                                                                                                            Toast.makeText(getContext(), " Contact Deleted", Toast.LENGTH_SHORT).show();
                                                                                                            Log.i(TAG, "onComplete: Contact Deleted");

                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }

                                                            }
                                                        });



                                                        builder.show();

                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ReuestsViserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.usersdiplaylayout, parent, false);
                        ReuestsViserHolder holder = new ReuestsViserHolder(view);
                        return holder;
                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ReuestsViserHolder extends RecyclerView.ViewHolder{

        TextView Uname, Ustatus;
        CircleImageView ProfInamg;
        Button AccpetBtn, CancelBtn;

        public ReuestsViserHolder(@NonNull View itemView) {
            super(itemView);

            Uname = itemView.findViewById(R.id.UserProfName);
            Ustatus = itemView.findViewById(R.id.UserSatus);
            ProfInamg = itemView.findViewById(R.id.UsrsrProfImage);
            AccpetBtn = itemView.findViewById(R.id.requestAccptBtn);
            CancelBtn = itemView.findViewById(R.id.requestCancelBtn);



        }
    }
}
