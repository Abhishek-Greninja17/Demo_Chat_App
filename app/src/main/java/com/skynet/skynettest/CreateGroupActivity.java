package com.skynet.skynettest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skynet.skynettest.admin.AdminMainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CreateGroupActivity extends AppCompatActivity implements CreateGroupAdapter.onItemCheckListener {

    private TextInputEditText group_name;
    private FirebaseAuth firebaseAuth;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private ArrayList<ShowUserModel> users = new ArrayList<>();
    private ArrayList<String> checkList = new ArrayList<>();
    private String userId;
    private DatabaseReference db;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
        userId = firebaseAuth.getCurrentUser().getUid();

        loadingBar = new ProgressDialog(this);
        group_name = findViewById(R.id.inputGroupName);
        ImageView close = findViewById(R.id.closeCreateGroup);
        TextView create_button = findViewById(R.id.createGroupButton);
        recyclerView = findViewById(R.id.createGroupRecycleList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        close.setOnClickListener(v -> finish());
        create_button.setOnClickListener(v -> createGroup());


//        to get the group name of user using uid
//        db.child("Group").child(userId).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot data: snapshot.getChildren()) Log.i("grupName", data.getKey());
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//        to get the user of current group
//        db.child("Group").child("ABC g1").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot data: snapshot.getChildren()) Log.i("grupName", data.getKey());
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

    }

    private void createGroup() {
        if (group_name.getText().toString().equals("")) {
            group_name.setError("Required");
            return;
        }
        if (checkList.size() == 0) {
            Toast.makeText(this, "Please select at least one member", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingBar.setMessage("Please wait, group is creating...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        checkList.add(userId);
        for (int i = 0; i < checkList.size(); i++) {
            String id = checkList.get(i);
//            db.child("Group").child(id).addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (snapshot.exists()) {
//                        Log.i("grupAdd", String.valueOf(snapshot.getChildrenCount()));
//                        db.child("Group").child(id).child(String.valueOf(snapshot.getChildrenCount())).setValue(group_name.getText().toString());
//                    } else
//                        db.child("Group").child(id).child("0").setValue(group_name.getText().toString());
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
            if (id.equals(userId)) {
                db.child("Group").child(group_name.getText().toString()).child(id).child("type").setValue("admin");
                db.child("Group").child(id).child(group_name.getText().toString()).child("type").setValue("admin");
            }
            else {
                db.child("Group").child(group_name.getText().toString()).child(id).child("type").setValue("active");
                db.child("Group").child(id).child(group_name.getText().toString()).child("type").setValue("active");
            }
        }

        loadingBar.dismiss();
        finish();
        Toast.makeText(this, "Group create successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        db.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot user : snapshot.getChildren()) {
                    if (!user.child("uid").getValue().equals(userId)) {
                        if (!user.child("type").getValue().equals("Super Admin")) {
                            String name = user.child("name").getValue().toString();
                            String email = user.child("email").getValue().toString();
                            String company = user.child("company").getValue().toString();
                            String type = user.child("type").getValue().toString();
                            String uid = user.child("uid").getValue().toString();
                            ShowUserModel chatModel;
                            if (user.child("image").exists()) {
                                String image = user.child("image").getValue().toString();
                                chatModel = new ShowUserModel(name, company, type, uid, email, image);
                            } else chatModel = new ShowUserModel(name, company, type, uid, email);
                            users.add(chatModel);
                            adapter = new CreateGroupAdapter(users, CreateGroupActivity.this, CreateGroupActivity.this);
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(layoutManager);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onItemCheck(String list) {
        checkList.add(list);
    }

    @Override
    public void onItemUncheck(String list) {
        checkList.remove(list);
    }
}

// DatabaseError: Data to write exceeds the maximum size that can be modified with a single request.