package com.example.myapplication.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Models.Messages;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final String TAG = "MessageAdapter";

    private List<Messages> userMEssagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference URef;

    public MessageAdapter (List<Messages> userMEssagesList){
        this.userMEssagesList = userMEssagesList;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderText, reciverText;
        public CircleImageView recivImg;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderText = itemView.findViewById(R.id.senderMessageText);
            reciverText = itemView.findViewById(R.id.reciverMessageText);
            recivImg = itemView.findViewById(R.id.messageProfImage);

        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();


        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {

        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMEssagesList.get(position);

        String fromUID = messages.getFrom();
        String fromType = messages.getType();

        URef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUID);

        URef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("image")){

                    String receiverProfImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverProfImage).placeholder(R.drawable.profile_image).into(holder.recivImg);

                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (fromType.equals("text")){

            holder.reciverText.setVisibility(View.INVISIBLE);
            holder.recivImg.setVisibility(View.INVISIBLE);
            holder.senderText.setVisibility(View.INVISIBLE);

            if (fromUID.equals(messageSenderID))
            {
                holder.senderText.setVisibility(View.VISIBLE);

                holder.senderText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderText.setTextColor(Color.BLACK);
                holder.senderText.setText(messages.getMessage());
            }
            else {


                holder.recivImg.setVisibility(View.VISIBLE);
                holder.reciverText.setVisibility(View.VISIBLE);

                holder.reciverText.setBackgroundResource(R.drawable.revicer_messages_layout);
                holder.reciverText.setTextColor(Color.BLACK);
                holder.reciverText.setText(messages.getMessage());
            }


        }
    }

    @Override
    public int getItemCount() {
        return userMEssagesList.size();
    }
}
