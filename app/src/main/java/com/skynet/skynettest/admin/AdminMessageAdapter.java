package com.skynet.skynettest.admin;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.skynet.skynettest.MessageModel;
import com.skynet.skynettest.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AdminMessageAdapter extends RecyclerView.Adapter<AdminMessageAdapter.MessageViewHolder> {

    private List<MessageModel> msgList;
    private FirebaseAuth mAuth;
    private Context context;
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();

    private final byte[] encryptionKey = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    private Cipher deCipher;
    private SecretKeySpec secretKeySpec;

    public AdminMessageAdapter(List<MessageModel> msgList, Context context) {
        this.context = context;
        this.msgList = msgList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.custom_message_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();

        try {
            deCipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        secretKeySpec = new SecretKeySpec(encryptionKey, "AES");

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String uid = mAuth.getCurrentUser().getUid();
        MessageModel messages = msgList.get(position);

        if (msgList.get(position).getType().equals("text")){    // text
            String stringMsg = null;
            try {
                stringMsg = AESDecryption(msgList.get(position).getMessage());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (msgList.get(position).getFrom().equals(uid)) {
                holder.sender_text_layout.setVisibility(View.VISIBLE);
                holder.sender_text_time.setText(msgList.get(position).getTime());
                holder.sender_msg.setText(stringMsg);
            }
            else {
                holder.receiver_text_layout.setVisibility(View.VISIBLE);
                holder.receiver_msg.setText(stringMsg);
                holder.receiver_text_time.setText(msgList.get(position).getTime());
            }
        }
        else if (msgList.get(position).getType().equals("jpg")){    // image
            if (msgList.get(position).getFrom().equals(uid)) {
                holder.sender_image_layout.setVisibility(View.VISIBLE);
                holder.sender_image_time.setText(msgList.get(position).getTime());
                Glide.with(context).load(msgList.get(position).getMessage()).into(holder.sender_image);
            }
            else {
                holder.receiver_image_layout.setVisibility(View.VISIBLE);
                holder.receiver_image_time.setText(msgList.get(position).getTime());
                Glide.with(context).load(msgList.get(position).getMessage()).into(holder.receiver_image);

            }
        }
        else if (msgList.get(position).getType().equals("mp4")){    // video
            if (msgList.get(position).getFrom().equals(uid)) {
                holder.sender_video_layout.setVisibility(View.VISIBLE);
                holder.sender_video_time.setText(msgList.get(position).getTime());
                holder.sender_video.setVideoPath(msgList.get(position).getMessage());
            }
            else {
                holder.receiver_video_layout.setVisibility(View.VISIBLE);
                holder.receiver_video_time.setText(msgList.get(position).getTime());
                holder.receiver_video.setVideoPath(msgList.get(position).getMessage());
            }
        }
        else {    // audio or pdf
            if (msgList.get(position).getFrom().equals(uid)) {
                holder.sender_audio_layout.setVisibility(View.VISIBLE);
                holder.sender_audio.setText(msgList.get(position).getName());
                holder.sender_audio_time.setText(msgList.get(position).getTime());
            }
            else {
                holder.receiver_audio_layout.setVisibility(View.VISIBLE);
                holder.receiver_audio.setText(msgList.get(position).getName());
                holder.receiver_audio_time.setText(msgList.get(position).getTime());

            }
        }

        holder.sender_video.setOnClickListener(v -> {holder.sender_video.start();});
        holder.receiver_video.setOnClickListener(v -> {holder.receiver_video.start();});
        holder.sender_audio_layout.setOnClickListener(v -> downloadMyData(msgList.get(position).getMessage(), msgList.get(position).getName()));
        holder.receiver_audio_layout.setOnClickListener(v -> downloadMyData(msgList.get(position).getMessage(), msgList.get(position).getName()));
        holder.sender_image_layout.setOnClickListener(v -> {
            Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(msgList.get(position).getMessage()));
            context.startActivity(browserIntent);
        });
        holder.receiver_image_layout.setOnClickListener(v -> {
            Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(msgList.get(position).getMessage()));
            context.startActivity(browserIntent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Delete this message").setMessage("Do you want to delete this message")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        db.child("Employee Message").child(messages.getFrom()).child(messages.getTo()).child(messages.getMsgkey()).removeValue();
                        db.child("Employee Message").child(messages.getTo()).child(messages.getFrom()).child(messages.getMsgkey()).removeValue();
                        db.child("Admin Message").child(messages.getFrom()).child(messages.getTo()).child(messages.getMsgkey()).removeValue();
                        db.child("Admin Message").child(messages.getTo()).child(messages.getFrom()).child(messages.getMsgkey()).removeValue();
                    })
                    .setNegativeButton("No",null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        LinearLayout sender_text_layout, receiver_text_layout, sender_image_layout, receiver_image_layout;
        LinearLayout receiver_audio_layout, sender_audio_layout, receiver_video_layout, sender_video_layout;
        TextView receiver_audio, sender_audio, receiver_audio_time, sender_audio_time;
        TextView sender_msg, sender_text_time, receiver_text_time, receiver_msg;
        TextView receiver_image_time, sender_image_time;
        TextView receiver_video_time, sender_video_time;
        ImageView  receiver_image, sender_image;
        VideoView receiver_video, sender_video;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sender_text_layout = itemView.findViewById(R.id.sender_layout);
            receiver_text_layout = itemView.findViewById(R.id.receiver_layout);
            sender_msg = itemView.findViewById(R.id.sender_message);
            receiver_msg = itemView.findViewById(R.id.receiver_msg);
            sender_text_time = itemView.findViewById(R.id.sender_time);
            receiver_text_time = itemView.findViewById(R.id.receiver_time);

            sender_image_layout = itemView.findViewById(R.id.sender_image_layout);
            receiver_image_layout = itemView.findViewById(R.id.receiver_image_layout);
            receiver_image = itemView.findViewById(R.id.receiver_image);
            sender_image = itemView.findViewById(R.id.sender_image);
            receiver_image_time = itemView.findViewById(R.id.receiver_image_time);
            sender_image_time = itemView.findViewById(R.id.sender_image_time);

            receiver_audio_layout = itemView.findViewById(R.id.receiver_audio_layout);
            sender_audio_layout = itemView.findViewById(R.id.audio_sender_layout);
            receiver_audio = itemView.findViewById(R.id.audio_receiver);
            sender_audio = itemView.findViewById(R.id.audio_sender);
            receiver_audio_time = itemView.findViewById(R.id.audio_receiver_time);
            sender_audio_time = itemView.findViewById(R.id.audio_sender_time);

            receiver_video_layout = itemView.findViewById(R.id.receiver_video_layout);
            sender_video_layout = itemView.findViewById(R.id.video_sender_layout);
            receiver_video = itemView.findViewById(R.id.video_receiver);
            sender_video = itemView.findViewById(R.id.video_sender);
            receiver_video_time = itemView.findViewById(R.id.video_receiver_time);
            sender_video_time = itemView.findViewById(R.id.video_sender_time);
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

    public void downloadMyData(final String url, String name) {
        String downloadUrlOfImage = url;
        File direct = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getAbsolutePath() + "/" + "chatApplication" + "/");

        if (!direct.exists()) {
            direct.mkdir();
            Log.i("", "dir created for first time");
        }

        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri downloadUri = Uri.parse(downloadUrlOfImage);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(name)
//                .setMimeType("image/jpeg")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, File.separator + "chatApplication" + File.separator + name);

        dm.enqueue(request);
        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        context.startActivity(browserIntent);
    }
}
