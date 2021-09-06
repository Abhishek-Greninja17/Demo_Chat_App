package com.skynet.skynettest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {
    private EditText userNameEditText;
    private TextView mobileEditText;
    private CircleImageView userProfileImage;
    private String currentUserId;
    private DatabaseReference dbReference;
    private static final int GalleryPickRequest = 1;
    private StorageReference userProfileImageReference;
    private ProgressDialog loadingBar;
    private TextView updateSettingButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar settingToolbar = findViewById(R.id.settingToolBar);
        setSupportActionBar(settingToolbar);
        getSupportActionBar().setTitle("Settings");

        userNameEditText = findViewById(R.id.input_User_Name);
        mobileEditText = findViewById(R.id.input_mobile);
        updateSettingButton = findViewById(R.id.updateSettingButton);
        userProfileImage = findViewById(R.id.input_profile_Image);
        progressBar = findViewById(R.id.settingProgressBar);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        userProfileImageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");
        loadingBar = new ProgressDialog(this);

        updateSettingButton.setOnClickListener(view -> updateSettings());

        userProfileImage.setOnClickListener(view -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GalleryPickRequest);
        });

        retrieveUserInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GalleryPickRequest && resultCode == RESULT_OK) {
            loadingBar.setMessage("Please wait, profile image is uploading...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            Uri selectImageUri = data.getData();
            final StorageReference imageReference = userProfileImageReference.child(currentUserId + ".jpg");
            imageReference.putFile(selectImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String url = uri.toString();
                        dbReference.child("Users").child(currentUserId).child("image").setValue(url);
                        loadingBar.dismiss();
                        finish();
                        Toast.makeText(SettingActivity.this, "Profile picture updated.", Toast.LENGTH_SHORT).show();

                    }))
                    .addOnFailureListener(e -> {
                        loadingBar.dismiss();
                        new AlertDialog.Builder(SettingActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Failed to upload profile image")
                                .setMessage(e.getMessage())
                                .setPositiveButton("Ok", null)
                                .show();
                    });
        }
    }

    private void retrieveUserInfo() {
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        dbReference.child("Users").child(currentUserId)     //  currentUserId in Users node from database
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {  // snapshot means currentUserId in Users
                        if (snapshot.exists() && snapshot.hasChild("name") && snapshot.hasChild("image")) {
                            userNameEditText.setText(snapshot.child("name").getValue().toString());
                            mobileEditText.setText(snapshot.child("email").getValue().toString());
                            Glide.with(SettingActivity.this).load(snapshot.child("image").getValue().toString())
                                    .placeholder(R.drawable.user_profile_icon).into(userProfileImage);
                        } else if (snapshot.exists() && snapshot.hasChild("name")) {
                            String retrieveUserName = snapshot.child("name").getValue().toString();
                            String retrieveUserStatus = snapshot.child("email").getValue().toString();
                            userNameEditText.setText(retrieveUserName);
                            mobileEditText.setText(retrieveUserStatus);
                        } else {
                            updateSettingButton.setText("Add Profile");
                            updateSettings();
                            new AlertDialog.Builder(SettingActivity.this)
                                    .setTitle("Update profile")
                                    .setMessage("please update your profile. Profile image is optional but username and mobile number is compulsory")
                                    .setPositiveButton("Ok", null)
                                    .show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void updateSettings() {

        if (userNameEditText.getText().toString().equals(""))
            Toast.makeText(this, "Please write username!", Toast.LENGTH_SHORT).show();
        else {
            dbReference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    HashMap<String, Object> profileMap = new HashMap<>();
                    profileMap.put("uid", currentUserId);
                    profileMap.put("name", userNameEditText.getText().toString());
                    dbReference.child("Users").child(currentUserId).updateChildren(profileMap)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    finish();
                                    Toast.makeText(SettingActivity.this, "Profile updated.", Toast.LENGTH_SHORT).show();
                                } else {
                                    new AlertDialog.Builder(SettingActivity.this)
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setTitle("Failed to update settings")
                                            .setMessage(task.getException().getMessage())
                                            .setPositiveButton("Ok", null)
                                            .show();
                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.cancel_btn, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.cancelOption)
            finish();
        return true;
    }

}
