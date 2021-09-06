package com.skynet.skynettest.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skynet.skynettest.R;

import java.util.ArrayList;

public class FromActivity extends AppCompatActivity {

    private DatabaseReference db;
    private ListView listView;
    private ArrayList nameList = new ArrayList();
    private ArrayList idList = new ArrayList();
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_from);

        db = FirebaseDatabase.getInstance().getReference();
        listView = findViewById(R.id.chatOfList);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nameList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((adapterView, view, position, l) -> {
            String id = idList.get(position).toString();
            String name = nameList.get(position).toString();
            Intent intent = new Intent(this, ToActivity.class);
            intent.putExtra("senderId", id);
            intent.putExtra("senderName", name);
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
        db.child("Admin Message").addValueEventListener(new ValueEventListener() {
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
}