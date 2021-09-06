package com.skynet.skynettest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skynet.skynettest.admin.AdminMainActivity;
import com.skynet.skynettest.employee.EmployeeMainActivity;
import com.skynet.skynettest.superAdmin.SuperAdminMainActivity;

public class SplashActivity extends AppCompatActivity {

    Handler handler;
    Animation topAnim;
    ImageView img;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        img = findViewById(R.id.logo_splash);
        img.setAnimation(topAnim);

        handler = new Handler();
        handler.postDelayed(() -> {
            if (mAuth.getCurrentUser() == null) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            } else {
                String uid = mAuth.getCurrentUser().getUid();
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String type = snapshot.child("type").getValue().toString();
                        switch (type) {
                            case "Super Admin":
                                startActivity(new Intent(SplashActivity.this, SuperAdminMainActivity.class));
                                finish();
                                break;
                            case "employee":
                                if (snapshot.child("install").getValue().toString().equals("no")) dbRef.child("install").setValue("yes");
                                Intent intent = new Intent(SplashActivity.this, EmployeeMainActivity.class);
                                startActivity(intent);
                                finish();
                                break;
                            case "admin":
                                if (snapshot.child("install").getValue().toString().equals("no")) dbRef.child("install").setValue("yes");
                                Intent intent1 = new Intent(SplashActivity.this, AdminMainActivity.class);
                                intent1.putExtra("name", snapshot.child("name").getValue().toString());
                                startActivity(intent1);
                                finish();
                                break;
                            default:
                                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                finish();
                                break;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        }, 2000);
    }

}
