package com.skynet.skynettest.superAdmin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skynet.skynettest.LoginActivity;
import com.skynet.skynettest.R;
import com.skynet.skynettest.SettingActivity;
import com.skynet.skynettest.admin.FromActivity;

import java.util.ArrayList;

public class SuperAdminMainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private ArrayList<AdminModel> customer = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_admin_activity_main);

        recyclerView = findViewById(R.id.rcv);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        firebaseAuth = FirebaseAuth.getInstance();
        Toolbar toolbar = findViewById(R.id.sAdminToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Super Admin");
        //loginWithSuperAdmin();

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(view -> {
            startActivity(new Intent(SuperAdminMainActivity.this, AddUserActivity.class));
            finish();
        });

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = db.getReference("/Users"); // Admin->Users
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                customer.clear();
                for (DataSnapshot user : snapshot.getChildren()) {
                    if (!user.child("uid").getValue().equals(firebaseAuth.getCurrentUser().getUid())) {
                        Log.i("check", user.getKey() + "\t" + firebaseAuth.getCurrentUser().getUid());
                        String name = user.child("name").getValue().toString();
                        String company = user.child("company").getValue().toString();
                        String email = user.child("email").getValue().toString();
                        String password = user.child("password").getValue().toString();
                        String type = user.child("type").getValue().toString();
                        String uid = user.child("uid").getValue().toString();
                        AdminModel admin = new AdminModel(name, company, email, password, type, uid);
                        customer.add(admin);
                        adapter = new AdminInfoAdapter(SuperAdminMainActivity.this, customer);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(layoutManager);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.logoutOption) {
            logout();
        }
        if (item.getItemId() == R.id.profileOption) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.showChatOption){
            startActivity(new Intent(this, FromActivity.class));
        }
        return true;
    }

    private void logout() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Log Out");
        dialog.setMessage("Do you want to Log out?");
        dialog.setPositiveButton("Yes", (dialogInterface, d) -> {
            firebaseAuth.signOut();
            Context context = getApplicationContext();
            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
            finish();
        })
                .setNegativeButton("No", (dialogInterface, d) -> {

                });
        dialog.create();
        dialog.show();
    }

//    private void loginWithSuperAdmin() {
//        final String[] mail = new String[1];
//        final String[] pass = new String[1];
//        FirebaseDatabase.getInstance().getReference().child("Users").child("AfSChGihtMccw7vEkY8q5TwAm1X2").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                mail[0] = snapshot.child("email").getValue().toString();
//                pass[0] = snapshot.child("password").getValue().toString();
//                firebaseAuth.signOut();
//                firebaseAuth.signInWithEmailAndPassword(mail[0], pass[0]);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//            }
//        });
//    }
}