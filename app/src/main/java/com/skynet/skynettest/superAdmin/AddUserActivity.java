package com.skynet.skynettest.superAdmin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skynet.skynettest.R;

import java.util.HashMap;

public class AddUserActivity extends AppCompatActivity {

    final int userLimit=20;

    private EditText name,company,email,password;
    private ProgressDialog loadingBar;
    private FirebaseAuth firebaseAuth;
    public DatabaseReference dbReferences;
    private String userType = "";
    private String uid;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        Toolbar toolbar = findViewById(R.id.addAdminToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Admin");

        radioGroup = findViewById(R.id.rg);
        name = findViewById(R.id.inputAdminName);
        company = findViewById(R.id.inputCompanyName);
        email = findViewById(R.id.inputAdminEmail);
        password = findViewById(R.id.inputAdminPassword);
        TextView addButton = findViewById(R.id.addAdminButton);
        addButton.setText("Add User");
        loadingBar = new ProgressDialog(AddUserActivity.this);
        firebaseAuth = FirebaseAuth.getInstance();
        dbReferences = FirebaseDatabase.getInstance().getReference();

        if (getIntent().hasExtra("uid")){
            name.setText(getIntent().getStringExtra("name"));
            company.setText(getIntent().getStringExtra("company"));
            email.setText(getIntent().getStringExtra("email"));
            password.setText(getIntent().getStringExtra("password"));
            uid = getIntent().getStringExtra("uid");
            userType = getIntent().getStringExtra("type");
            RadioButton adminButton = findViewById(R.id.adminRadioButton);
            RadioButton empButton = findViewById(R.id.EmployeeRadioButton);
            if (userType.equals("admin")) adminButton.setChecked(true);
            if (userType.equals("employee")) empButton.setChecked(true);
            addButton.setText("Update User");
            email.setClickable(false);
            password.setClickable(false);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            android.widget.RadioButton radioButton = findViewById(checkedId);
            switch (radioButton.getId()){
                case R.id.adminRadioButton:
                    userType = "admin";
                    break;
                case R.id.EmployeeRadioButton:
                    userType = "employee";
                    break;
            }
        });
        addButton.setOnClickListener(v -> {
            if (addButton.getText().toString().equals("Add User")) createNewAccount();
            else if(addButton.getText().toString().equals("Update User")) updateUser();
        });
    }

    private void updateUser() {
        loadingBar.setMessage("Updating user info");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        Log.i("userInfoup", userType);
        HashMap<String, Object> profileMap = new HashMap<>();
        profileMap.put("uid", uid);
        profileMap.put("name", name.getText().toString());
        profileMap.put("company", company.getText().toString());
        profileMap.put("email", email.getText().toString());
        profileMap.put("password", password.getText().toString());
        profileMap.put("type", userType);
        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).updateChildren(profileMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(AddUserActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else {
                        new AlertDialog.Builder(AddUserActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Failed to update settings")
                                .setMessage(task.getException().getMessage())
                                .setPositiveButton("Ok", null)
                                .show();
                    }
                    loadingBar.dismiss();
                });
    }

    public void createNewAccount(){
        dbReferences.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount()>=userLimit+1){
                    Toast.makeText(AddUserActivity.this, "Can not create more users than given Limit. Please contact Distributors :)", Toast.LENGTH_SHORT).show();
                    sendUserToSuperAdminMainActivity();
                }
                else {
                    String input_email = email.getText().toString();
                    String input_password = password.getText().toString();
                    String input_name = name.getText().toString();
                    String input_company = company.getText().toString();

                    if (input_name.equals(""))
                        Toast.makeText(AddUserActivity.this, "Please enter name...", Toast.LENGTH_SHORT).show();
                    else if (input_email.equals(""))
                        Toast.makeText(AddUserActivity.this, "Please enter email...", Toast.LENGTH_SHORT).show();
                    else if (input_password.equals(""))
                        Toast.makeText(AddUserActivity.this, "Please enter password...", Toast.LENGTH_SHORT).show();
                    else if(input_company.equals(""))
                        Toast.makeText(AddUserActivity.this, "Please enter company name...", Toast.LENGTH_SHORT).show();
                    else if(userType.equals(""))
                        Toast.makeText(AddUserActivity.this, "Please select user type", Toast.LENGTH_SHORT).show();
                    else {
                        //  add loading bar to indicate the user
                        loadingBar.setTitle("Creating new account");
                        loadingBar.setMessage("Please wait! while we are creating new account");
                        loadingBar.setCanceledOnTouchOutside(true);
                        loadingBar.show();
                        firebaseAuth.createUserWithEmailAndPassword(input_email, input_password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        String currentUserId = firebaseAuth.getCurrentUser().getUid();
                                        addUserToDataBase(currentUserId);
                                        Toast.makeText(AddUserActivity.this,
                                                "User created successfully...", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                        sendUserToSuperAdminMainActivity();
                                    }
                                    else {
                                        new AlertDialog.Builder(AddUserActivity.this)
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .setTitle("Failed to create user")
                                                .setMessage(task.getException().getMessage())
                                                .setPositiveButton("Ok", null)
                                                .show();
                                        loadingBar.dismiss();
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void addUserToDataBase(String currentUserId) {
        String companyName = company.getText().toString();
        HashMap<String, Object> profileMap = new HashMap<>();
        profileMap.put("uid", currentUserId);
        profileMap.put("name", name.getText().toString());
        profileMap.put("company", companyName);
        profileMap.put("email", email.getText().toString());
        profileMap.put("password", password.getText().toString());
        profileMap.put("type", userType);
        profileMap.put("status", "offline");
        profileMap.put("install", "no");

        dbReferences.child("Users").child(currentUserId).updateChildren(profileMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dbReferences.child("Users").child("3utTKdn1QEgyZTqJ0AVxyKkOds62").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                loginWithSuperAdmin(snapshot.child("email").getValue().toString(), snapshot.child("password").getValue().toString());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    else {
                        new AlertDialog.Builder(AddUserActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Failed to update settings")
                                .setMessage(task.getException().getMessage())
                                .setPositiveButton("Ok", null)
                                .show();
                    }
                });
    }

    void loginWithSuperAdmin(String email, String password) {
        firebaseAuth.signOut();
        firebaseAuth.signInWithEmailAndPassword(email, password);
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
            sendUserToSuperAdminMainActivity();
        return true;
    }

    void sendUserToSuperAdminMainActivity(){
        startActivity(new Intent(this, SuperAdminMainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserToSuperAdminMainActivity();
    }
}
