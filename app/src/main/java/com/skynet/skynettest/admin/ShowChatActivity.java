package com.skynet.skynettest.admin;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skynet.skynettest.MessageModel;
import com.skynet.skynettest.R;

import java.util.ArrayList;
import java.util.List;

public class ShowChatActivity extends AppCompatActivity {

    private TextView toolBar;
    private String sId, sName, rId, rName;
    private DatabaseReference db;
    private ImageView delete;
    private final List<MessageModel> messagesList = new ArrayList<>();
    private ShowChatMessageAdapter showChatMessageAdapter;
    private RecyclerView userMessageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_chat);

        sId = getIntent().getStringExtra("sId");
        sName = getIntent().getStringExtra("sName");
        rId = getIntent().getStringExtra("rId");
        rName = getIntent().getStringExtra("rName");

        delete = findViewById(R.id.delete_chat);
        toolBar = findViewById(R.id.sctv);
        toolBar.setText(sName + " - " + rName);

        userMessageList = findViewById(R.id.showChatRv);
        showChatMessageAdapter = new ShowChatMessageAdapter(messagesList, sId, rId, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        userMessageList.setLayoutManager(layoutManager);
        userMessageList.setAdapter(showChatMessageAdapter);

        db = FirebaseDatabase.getInstance().getReference();

        delete.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete all chats")
                .setMessage("Do you want to delete all chats")
                .setPositiveButton("Yes", (dialogInterface, i) -> deleteAllChats())
                .setNegativeButton("No",null)
                .show());
    }

    @Override
    protected void onStart() {
        super.onStart();
        retrieveChat();
    }

    private void retrieveChat() {
        db.child("Admin Message").child(sId).child(rId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        MessageModel messageModel = snapshot.getValue(MessageModel.class);
                        messagesList.add(messageModel);
                        showChatMessageAdapter.notifyDataSetChanged();
                        userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void deleteAllChats() {
        db.child("Admin Message").child(sId).child(rId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                db.child("Admin Message").child(rId).child(sId).removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()){
                        db.child("Employee Message").child(sId).child(rId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    db.child("Employee Message").child(sId).child(rId).removeValue().addOnCompleteListener(task3 -> {
                                        if (task3.isSuccessful()){
                                            db.child("Employee Message").child(rId).child(sId).removeValue().addOnCompleteListener(task4 -> {
                                                if (task4.isSuccessful()){
                                                    Toast.makeText(ShowChatActivity.this, "Chat delete successfully..", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    Toast.makeText(ShowChatActivity.this, "Chat delete successfully..", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}