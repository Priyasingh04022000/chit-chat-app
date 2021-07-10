package com.example.chitchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chitchatapp.Adapters.ChatAdapter;
import com.example.chitchatapp.Model.MessageModel;
import com.example.chitchatapp.databinding.ActivityChatDetailBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatDetailActivity extends AppCompatActivity {
    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        database=FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

       final String senderId = auth.getUid();
        String recieveId = getIntent().getStringExtra("userId");
        Toast.makeText(ChatDetailActivity.this , "Recieve Id : " + recieveId , Toast.LENGTH_SHORT).show();
        String userName = getIntent().getStringExtra("userName");
        Toast.makeText(ChatDetailActivity.this , "UserName : " + userName , Toast.LENGTH_SHORT).show();
        String profilePic = getIntent().getStringExtra("profilePic");
        binding.userName.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.ic_user__1_).into(binding.profileImage);
        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatDetailActivity.this,MainActivity.class);
                startActivity(intent);

            }
        });
        final ArrayList<MessageModel> messageModels = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(messageModels,this);
        binding.chatRecyclerView.setAdapter(chatAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);
        final String senderRoom = senderId + recieveId;
        final String receiverRoom = recieveId+senderId;
        database.getReference().child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for (DataSnapshot snapshot1 :snapshot.getChildren())
                        {
                            MessageModel model = snapshot1.getValue(MessageModel.class);
                            messageModels.add(model);
                            Toast.makeText(ChatDetailActivity.this , "Sender Id :" + senderId , Toast.LENGTH_SHORT).show();
                            Toast.makeText(ChatDetailActivity.this , "Receiver Id :" + recieveId , Toast.LENGTH_SHORT).show();
                            Toast.makeText(ChatDetailActivity.this , "Retrieving data :" + senderRoom , Toast.LENGTH_SHORT).show();

                        }
                        chatAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             String message =   binding.etMessage.getText().toString();
             final MessageModel model = new MessageModel(senderId,message);
             model.setTimestamp(new Date().getTime());
             binding.etMessage.setText("");

             database.getReference().child("chats")
                     .child(senderRoom)
                     .push()
                     .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                 @Override
                 public void onSuccess(Void aVoid) {
                     Toast.makeText(ChatDetailActivity.this , "data push to sender" + senderRoom , Toast.LENGTH_SHORT).show();

                     database.getReference().child("chats")
                             .child(receiverRoom)
                             .push()
                             .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                         @Override
                         public void onSuccess(Void aVoid) {
                             Toast.makeText(ChatDetailActivity.this , "data push to receiver" + receiverRoom , Toast.LENGTH_SHORT).show();


                         }
                     });


                 }
             });

            }
        });

    }
}