package com.skynet.skynettest.employee;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.skynet.skynettest.MessageModel;
import com.skynet.skynettest.R;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class EmployeeChatActivity extends AppCompatActivity {

    final String CHANNEL_ID = "new_message";
    private String senderId, senderName, receiverId, receiverName, email, receiverImage, checker = "";
    private FirebaseAuth firebaseAuth;
    private DatabaseReference dbRef;
    private TextView online;
    private EditText inputMsg;
    private final List<MessageModel> messagesList = new ArrayList<>();
    private EmployeeMessageAdapter employeeMessageAdapter;
    private RecyclerView userMessageList;
    private ProgressDialog loadingBar;
    private boolean fin = true;

    private final byte[] encryptionKey = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    private Cipher cipher;
    private SecretKeySpec secretKeySpec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        secretKeySpec = new SecretKeySpec(encryptionKey, "AES");

        firebaseAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

            senderId = firebaseAuth.getCurrentUser().getUid();


        receiverId = getIntent().getStringExtra("id");
        email = getIntent().getStringExtra("email");
        receiverName = getIntent().getStringExtra("name");
        receiverImage = getIntent().getStringExtra("image");

        ImageView back = findViewById(R.id.icon_back_chat_employee);
        ImageView deleteAll = findViewById(R.id.delete_all);
        CircleImageView userLogo = findViewById(R.id.chat_user_logo);
        TextView name = findViewById(R.id.chat_user_name);
        online = findViewById(R.id.online);
        inputMsg = findViewById(R.id.input_chat_message);
        ImageView sendButton = findViewById(R.id.send_message_button);
        ImageView selectAttachment = findViewById(R.id.select_chat_attachment);

        name.setText(receiverName);
        Glide.with(EmployeeChatActivity.this).load(receiverImage).placeholder(R.drawable.user_profile_icon).into(userLogo);

        dbRef.child("Users").child(senderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                senderName = snapshot.child("name").getValue().toString();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        loadingBar = new ProgressDialog(this);
        userMessageList = findViewById(R.id.chatRecyclerView);
        employeeMessageAdapter = new EmployeeMessageAdapter(messagesList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        userMessageList.setLayoutManager(layoutManager);
        userMessageList.setAdapter(employeeMessageAdapter);

        selectAttachment.setOnClickListener(v -> {
            fin = false;
            CharSequence[] option = new CharSequence[]{"Images", "Pdf", "Audio", "Video"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select file type");
            builder.setItems(option, (dialog, items) -> {
                if (option[items]=="Images") galleryIntent();
                else if(option[items] == "Pdf") pdfIntent();
                else if (option[items] == "Audio") audioIntent();
                else if (option[items] == "Video") videoIntent();
            });
            builder.show();
        });

        sendButton.setOnClickListener(v -> {
            addMessageToDatabase();
        });
        back.setOnClickListener(v -> finish());
        deleteAll.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete all chats")
                .setMessage("Do you want to delete all chats")
                .setPositiveButton("Yes", (dialogInterface, i) -> deleteAllChat())
                .setNegativeButton("No", null)
                .show());
    }

    @Override
    protected void onStart() {
        super.onStart();
        retrieveChat();
    }

    private void retrieveChat() {
        messagesList.clear();
        dbRef.child("Employee Message").child(senderId).child(receiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                        addNotification();
                        MessageModel employeeMessageModel = snapshot.getValue(MessageModel.class);
                        messagesList.add(employeeMessageModel);
                        employeeMessageAdapter.notifyDataSetChanged();
                        userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());


                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        MessageModel employeeMessageModel = snapshot.getValue(MessageModel.class);
                        messagesList.remove(employeeMessageModel);
                        employeeMessageAdapter.notifyDataSetChanged();
                        userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
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

    private void deleteAllChat() {
        dbRef.child("Employee Message").child(senderId).child(receiverId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dbRef.child("Employee Message").child(receiverId).child(senderId).removeValue();
                        Toast.makeText(EmployeeChatActivity.this, "Chat delete successfully..", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void galleryIntent() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, 10);
    }

    public void pdfIntent() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), 11);
    }

    public void audioIntent() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Audio"), 12);
    }

    public void videoIntent() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), 13);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fin = true;
        if (resultCode == RESULT_OK) {

            loadingBar.setTitle("Sending image");
            loadingBar.setMessage("Please wait, image is sending...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            Uri imageUri = data.getData();
            String type = null;
            StorageReference storageReference = null;

            switch (requestCode){
                case 10:
                    storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                    type = ".jpg";
                    break;
                case 11:
                    storageReference = FirebaseStorage.getInstance().getReference().child("Pdf Files");
                    type = ".pdf";
                    break;
                case 12:
                    storageReference = FirebaseStorage.getInstance().getReference().child("Audio Files");
                    type = ".mp3";
                    break;
                case 13:
                    storageReference = FirebaseStorage.getInstance().getReference().child("Video Files");
                    type = ".mp4";
                    break;
            }

            String AmessageSenderRef = "/Admin Message/" + senderId + "/" + receiverId;
            String AmessageReceiverRef = "/Admin Message/" + receiverId + "/" + senderId;
            String EmessageSenderRef = "/Employee Message/" + senderId + "/" + receiverId;
            String EmessageReceiverRef = "/Employee Message/" + receiverId + "/" + senderId;

            DatabaseReference messageKeyRef = dbRef.child("Employee Messages").child(senderId).child(receiverId).push();
            String messagePushId = messageKeyRef.getKey();
            StorageReference filePath = storageReference.child(messagePushId + type);
            String finalType = type;
            filePath.putFile(imageUri).addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                String url = uri.toString();

                Calendar calForTime = Calendar.getInstance();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
                String currentTime = currentTimeFormat.format(calForTime.getTime());

                Map messageTextBody = new HashMap();
                messageTextBody.put("message", url);
                messageTextBody.put("name", imageUri.getLastPathSegment());
                messageTextBody.put("type", finalType.substring(1,4));
                messageTextBody.put("from", senderId);
                messageTextBody.put("to", receiverId);
                messageTextBody.put("time", currentTime);
                messageTextBody.put("msgkey", messagePushId);
                messageTextBody.put("toName", receiverName);
                messageTextBody.put("fromName", senderName);

                Map messageBodyDetails = new HashMap();
                messageBodyDetails.put(EmessageSenderRef + "/" + messagePushId, messageTextBody);
                messageBodyDetails.put(EmessageReceiverRef + "/" + messagePushId, messageTextBody);

                Map messageBodyDetails2 = new HashMap();
                messageBodyDetails.put(AmessageSenderRef + "/" + messagePushId, messageTextBody);
                messageBodyDetails.put(AmessageReceiverRef + "/" + messagePushId, messageTextBody);

                dbRef.updateChildren(messageBodyDetails).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dbRef.updateChildren(messageBodyDetails2).addOnCompleteListener(task1 -> {
                            inputMsg.setText("");
                            loadingBar.dismiss();
                        });
                    } else loadingBar.dismiss();
                });
            }));
        }
    }

    private void addMessageToDatabase() {
        if (inputMsg.getText().toString().equals("")) {
            Toast.makeText(this, "Please enter the message..", Toast.LENGTH_SHORT).show();
        } else {
            String AmessageSenderRef = "/Admin Message/" + senderId + "/" + receiverId;
            String AmessageReceiverRef = "/Admin Message/" + receiverId + "/" + senderId;
            String EmessageSenderRef = "/Employee Message/" + senderId + "/" + receiverId;
            String EmessageReceiverRef = "/Employee Message/" + receiverId + "/" + senderId;

            DatabaseReference messageKeyRef = dbRef.child("Employee Messages").child(senderId)
                    .child(receiverId).push();
            String messagePushId = messageKeyRef.getKey();


            Calendar calForTime = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            String currentTime = currentTimeFormat.format(calForTime.getTime());

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", AESEncryption(inputMsg.getText().toString().trim()));
            messageTextBody.put("type", "text");
            messageTextBody.put("from", senderId);
            messageTextBody.put("to", receiverId);
            messageTextBody.put("time", currentTime);
            messageTextBody.put("msgkey", messagePushId);
            messageTextBody.put("toName", receiverName);
            messageTextBody.put("fromName", senderName);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(EmessageSenderRef + "/" + messagePushId, messageTextBody);
            messageBodyDetails.put(EmessageReceiverRef + "/" + messagePushId, messageTextBody);

            Map messageBodyDetails2 = new HashMap();
            messageBodyDetails.put(AmessageSenderRef + "/" + messagePushId, messageTextBody);
            messageBodyDetails.put(AmessageReceiverRef + "/" + messagePushId, messageTextBody);

            dbRef.updateChildren(messageBodyDetails).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    dbRef.updateChildren(messageBodyDetails2).addOnCompleteListener(task1 -> inputMsg.setText(""));
                }
            });

        }
    }

    private String AESEncryption(String string) {
        byte[] stringByte = string.getBytes();
        byte[] encryptedByte = new byte[stringByte.length];
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            encryptedByte = cipher.doFinal(stringByte);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        String returnString = null;
        try {
            returnString = new String(encryptedByte, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return returnString;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,EmployeeMainActivity.class));
        super.onBackPressed();
    }
}