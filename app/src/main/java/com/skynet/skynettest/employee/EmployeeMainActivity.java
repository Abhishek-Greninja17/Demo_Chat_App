package com.skynet.skynettest.employee;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skynet.skynettest.LoginActivity;
import com.skynet.skynettest.MessageModel;
import com.skynet.skynettest.R;
import com.skynet.skynettest.SettingActivity;
import com.skynet.skynettest.ShowUserAdapter;
import com.skynet.skynettest.ShowUserModel;
import com.skynet.skynettest.admin.AdminChatActivity;
import com.skynet.skynettest.admin.AdminMainActivity;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class EmployeeMainActivity extends AppCompatActivity {

    final String CHANNEL_ID = "new_message";
    private DatabaseReference db;
    private FirebaseAuth firebaseAuth;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    ArrayList<ShowUserModel> Employee = new ArrayList<ShowUserModel>();
    private String uid;
    MessageModel newModel,oldModel;

    private final byte[] encryptionKey = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    private Cipher deCipher;
    private SecretKeySpec secretKeySpec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employee_activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
        uid = firebaseAuth.getCurrentUser().getUid();
        setToolBar();

        recyclerView = findViewById(R.id.chatEmployeeRcv);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        newMessage();

    }

    public void newMessage() {


        for (ShowUserModel model : Employee) {
            db.child("Employee Message").child(uid).child(model.getUid()).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    newModel=snapshot.getValue(MessageModel.class);
                    if (newModel!=oldModel){
                        oldModel=newModel;
                        String type=model.getType();
                        String company=model.getCompany();
                        String image=model.getImage();
                        String email=model.getEmail();
                        String fromName = snapshot.child("fromName").getValue().toString();
                        String from = snapshot.child("from").getValue().toString();
                        try {
                            deCipher = Cipher.getInstance("AES");
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (NoSuchPaddingException e) {
                            e.printStackTrace();
                        }
                        secretKeySpec = new SecretKeySpec(encryptionKey, "AES");
                        String msgbody=snapshot.child("message").getValue().toString();
                        if (!from.equals(uid)) {
                            try {
                                addNotification(fromName,AESDecryption(msgbody),from,email,type,company,image);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }


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

    }

    @Override
    protected void onStart() {
        super.onStart();
        db.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Employee.clear();
                for (DataSnapshot user : snapshot.getChildren()) {
                    if (!user.getKey().equals(uid)) {
                        if (!user.child("type").getValue().equals("Super Admin")) {
                            if (user.child("install").getValue().equals("yes")) {
                                String name = user.child("name").getValue().toString();
                                String email = user.child("email").getValue().toString();
                                String company = user.child("company").getValue().toString();
                                String type = user.child("type").getValue().toString();
                                String uid = user.child("uid").getValue().toString();
                                ShowUserModel chatModel;
                                if (user.child("image").exists()) {
                                    String image = user.child("image").getValue().toString();
                                    chatModel = new ShowUserModel(name, company, type, uid, email, image);
                                } else
                                    chatModel = new ShowUserModel(name, company, type, uid, email);
                                Employee.add(chatModel);
                                adapter = new ShowUserAdapter(Employee, EmployeeMainActivity.this);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(layoutManager);
                            }
                        }
                    }
                }
                db.child("Group").child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot data : snapshot.getChildren()) {
                                String name = data.getKey();
                                ShowUserModel groupModel = new ShowUserModel(name, "group", null);
                                Employee.add(groupModel);
                                adapter = new ShowUserAdapter(Employee, EmployeeMainActivity.this);
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
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //newMessage();
    }

    private void setToolBar() {
        db.child("Users").child(firebaseAuth.getCurrentUser().getUid())     //  currentUserId in Users node from database
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Toolbar toolbar = findViewById(R.id.employeeToolBar);
                        setSupportActionBar(toolbar);
                        getSupportActionBar().setTitle(snapshot.child("name").getValue().toString());
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
        menu.findItem(R.id.showChatOption).setVisible(false);
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
        return true;
    }

    private void logout() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Log Out");
        dialog.setMessage("Do you want to Log out?");
        dialog.setPositiveButton("Yes", (dialogInterface, d) -> {
            //FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("status").setValue("offline");
            firebaseAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        })
                .setNegativeButton("No", (dialogInterface, d) -> {

                });
        dialog.create();
        dialog.show();

    }



    private void addNotification(String name,String body,String id,String email,String type,String company,String image) {
        createNotificationChannel();
        Intent intent;

        if (type.equals("employee")){
            intent = new Intent(this, EmployeeChatActivity.class);
            if (image!=null) {
                intent.putExtra("id", id);
                intent.putExtra("company", company);
                intent.putExtra("email", email);
                intent.putExtra("name", name);
                intent.putExtra("image", image);
            } else {
                intent.putExtra("id", id);
                intent.putExtra("company", company);
                intent.putExtra("email", email);
                intent.putExtra("name", name);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_speech_bubble)
                    .setContentTitle("New message from " + name)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
            notificationManager.notify(0, builder.build());
        }
        else {
            intent = new Intent(this, AdminChatActivity.class);
            if (image!=null) {
                intent.putExtra("id", id);
                intent.putExtra("company", company);
                intent.putExtra("email", email);
                intent.putExtra("name", name);
                intent.putExtra("image", image);
            } else {
                intent.putExtra("id", id);
                intent.putExtra("company", company);
                intent.putExtra("email", email);
                intent.putExtra("name", name);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_speech_bubble)
                    .setContentTitle("New message from " + name)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
            notificationManager.notify(0, builder.build());
        }



    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "notification";
            String description = "New message";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private String AESDecryption(String string) throws UnsupportedEncodingException {
        byte[] encryptedByte = string.getBytes("ISO-8859-1");
        String decryptedString = string;
        byte[] decryption;
        try {
            deCipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            decryption = deCipher.doFinal(encryptedByte);
            decryptedString = new String(decryption);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decryptedString;
    }
}