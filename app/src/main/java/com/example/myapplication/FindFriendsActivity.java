package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Models.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private RecyclerView FindFrendView;
    private DatabaseReference URef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        URef = FirebaseDatabase.getInstance().getReference().child("Users");

        FindFrendView = findViewById(R.id.findFriendsRecycle);
        FindFrendView.setLayoutManager(new LinearLayoutManager(this));

        mToolBar = (Toolbar)findViewById(R.id.findFriends);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        }


    @Override
    protected void onStart() {

        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(URef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,FindFreindHolder > adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFreindHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFreindHolder holder, final int position, @NonNull Contacts model) {

                        holder.Uname.setText(model.getName());
                        holder.Ustatus.setText(model.getStatus());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.UProfImg);


                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String visitUserId  = getRef(position).getKey();

                                Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("visit user id", visitUserId);
                                startActivity(profileIntent);

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFreindHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.usersdiplaylayout, parent ,false);
                        FindFreindHolder viewHolder = new FindFreindHolder(view);
                        return viewHolder;
                    }
                };

        FindFrendView.setAdapter(adapter);

        adapter.startListening();
    }

    public static class FindFreindHolder extends RecyclerView.ViewHolder{

        TextView Uname, Ustatus;
        CircleImageView UProfImg;

        public FindFreindHolder(@NonNull View itemView) {
            super(itemView);

            Uname = itemView.findViewById(R.id.UserProfName);
            Ustatus = itemView.findViewById(R.id.UserSatus);
            UProfImg = itemView.findViewById(R.id.UsrsrProfImage);


        }
    }





    public void onBackPressed() {

        finish();
        Intent intent = new Intent(FindFriendsActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }





}
