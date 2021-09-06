package com.skynet.skynettest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.skynet.skynettest.admin.AdminChatActivity;
import com.skynet.skynettest.admin.AdminMessageAdapter;

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

public class GroupChatActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference dbRef;
    private String senderId, senderName, groupName;
    private EditText inputMsg;
    private final ArrayList<GroupChatModel> messagesList = new ArrayList<>();
    private GroupChatAdapter groupMessageAdapter;
    private RecyclerView groupMessageRecycleList;
    private ProgressDialog loadingBar;
    private boolean fin = true;

    private final byte[] encryptionKey = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    private Cipher cipher;
    private SecretKeySpec secretKeySpec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        secretKeySpec = new SecretKeySpec(encryptionKey, "AES");

        groupName = getIntent().getStringExtra("groupName");
        String getGroupImage = getIntent().getStringExtra("groupImage");

        firebaseAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        senderId = firebaseAuth.getCurrentUser().getUid();
        loadingBar = new ProgressDialog(this);

        groupMessageRecycleList = findViewById(R.id.group_chat_recycler_view);
        groupMessageAdapter = new GroupChatAdapter(messagesList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        groupMessageRecycleList.setLayoutManager(layoutManager);
        groupMessageRecycleList.setAdapter(groupMessageAdapter);

        dbRef.child("Users").child(senderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                senderName = snapshot.child("name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ImageView back = findViewById(R.id.close_group_chat);
        ImageView deleteAll = findViewById(R.id.delete_all_group_chat);
        CircleImageView groupLogo = findViewById(R.id.group_chat_logo);
        TextView groupNameTextView = findViewById(R.id.group_chat_name);
        inputMsg = findViewById(R.id.input_chat_message);
        ImageView sendButton = findViewById(R.id.send_message_button);
        ImageView selectAttachment = findViewById(R.id.select_chat_attachment);

        groupNameTextView.setText(groupName);
        Glide.with(this).load(getGroupImage).placeholder(R.drawable.user_profile_icon).into(groupLogo);

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
        back.setOnClickListener(v -> finish());
        sendButton.setOnClickListener(v -> addMessageToDatabase());
        deleteAll.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete all chats")
                .setMessage("Do you want to delete all chats")
                .setPositiveButton("Yes", (dialogInterface, i) -> deleteAllChat())
                .setNegativeButton("No", null)
                .show());
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
        if (resultCode == RESULT_OK){

            loadingBar.setMessage("Please wait...");
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

            DatabaseReference messageKeyRef = dbRef.child(groupName).push();
            String messagePushId = messageKeyRef.getKey();
            StorageReference filePath = storageReference.child(messagePushId + type);
            String finalType = type;
            filePath.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                    String url = uri.toString();

                    Calendar calForTime = Calendar.getInstance();
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
                    String currentTime = currentTimeFormat.format(calForTime.getTime());

                    Log.i("imgurl", url + " " + imageUri.getLastPathSegment());
                    Map<String, Object> messageTextBody = new HashMap();
                    messageTextBody.put("message", url);
                    messageTextBody.put("fileName", imageUri.getLastPathSegment());
                    messageTextBody.put("type", finalType.substring(1,4));
                    messageTextBody.put("from", senderId);
                    messageTextBody.put("time", currentTime);
                    messageTextBody.put("msgkey", messagePushId);
                    messageTextBody.put("fromName", senderName);

                    dbRef.child(groupName).child(messagePushId).updateChildren(messageTextBody).addOnCompleteListener(task -> {
                        if (task.isSuccessful());
                        else Log.i("grupImgEr", task.getException().getMessage());
                        loadingBar.dismiss();
                    });
                });
            });
        }
    }

    public void retrieveChat() {
        messagesList.clear();
        dbRef.child(groupName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                GroupChatModel chatModel = snapshot.getValue(GroupChatModel.class);
                messagesList.add(chatModel);
                groupMessageAdapter.notifyDataSetChanged();
                groupMessageRecycleList.smoothScrollToPosition(groupMessageRecycleList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                GroupChatModel chatModel = snapshot.getValue(GroupChatModel.class);
                messagesList.remove(chatModel);
                groupMessageAdapter.notifyDataSetChanged();
                groupMessageRecycleList.smoothScrollToPosition(groupMessageRecycleList.getAdapter().getItemCount());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addMessageToDatabase() {
        if (inputMsg.getText().toString().equals("")) {
            Toast.makeText(this, "Please enter the message..", Toast.LENGTH_SHORT).show();
        } else {
            DatabaseReference messageKeyRef = dbRef.child(groupName).push();
            String messagePushId = messageKeyRef.getKey();

            Calendar calForTime = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            String currentTime = currentTimeFormat.format(calForTime.getTime());

            Map<String, Object> messageTextBody = new HashMap();
            messageTextBody.put("message", AESEncryption(inputMsg.getText().toString().trim()));
            messageTextBody.put("type", "text");
            messageTextBody.put("from", senderId);
            messageTextBody.put("time", currentTime);
            messageTextBody.put("msgkey", messagePushId);
            messageTextBody.put("fromName", senderName);

            dbRef.child(groupName).child(messagePushId).updateChildren(messageTextBody).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    inputMsg.setText("");
                }
            });
        }
    }

    private void deleteAllChat() {
        dbRef.child(groupName).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                finish();
                Toast.makeText(GroupChatActivity.this, "All chat deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(GroupChatActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Failed to delete chats")
                        .setMessage(task.getException().getMessage())
                        .setPositiveButton("Ok", null)
                        .show();
            }
        });
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
    protected void onStart() {
        super.onStart();
        retrieveChat();
        Log.i("grupC", "start");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fin) finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fin) finish();
    }

}