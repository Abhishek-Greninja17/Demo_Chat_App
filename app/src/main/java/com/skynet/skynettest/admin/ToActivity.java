package com.skynet.skynettest.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skynet.skynettest.R;

import java.util.ArrayList;

public class ToActivity extends AppCompatActivity {

    private String senderId, senderName;
    private DatabaseReference db;
    private ListView listView;
    private ArrayList nameList = new ArrayList();
    private ArrayList idList = new ArrayList();
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to);

        senderId = getIntent().getStringExtra("senderId");
        senderName = getIntent().getStringExtra("senderName");
        TextView textView = findViewById(R.id.toTV);
        textView.setText("Chat of " + senderName + " with");

        db = FirebaseDatabase.getInstance().getReference();
        listView = findViewById(R.id.chatFromList);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nameList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((adapterView, view, position, l) -> {
            String receiverId = idList.get(position).toString();
            Intent intent = new Intent(this, ShowChatActivity.class);
            intent.putExtra("sId", senderId);
            intent.putExtra("rId", receiverId);
            intent.putExtra("sName", senderName);
            intent.putExtra("rName", nameList.get(position).toString());
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        retrieveUsers();
    }

    private void retrieveUsers() {
        nameList.clear();
        idList.clear();
        db.child("Admin Message").child(senderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot s : snapshot.getChildren()){
                    db.child("Users").child(s.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            idList.add(s.getKey());
                            nameList.add(userSnapshot.child("name").getValue());
                            adapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}