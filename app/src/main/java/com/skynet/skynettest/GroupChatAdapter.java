package com.skynet.skynettest;

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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.ViewHolder>{

    private ArrayList<GroupChatModel> msgList;
    private Context context;
    private FirebaseAuth mAuth;
    private String uid;

    private final byte[] encryptionKey = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    private Cipher deCipher;
    private SecretKeySpec secretKeySpec;

    public GroupChatAdapter(ArrayList<GroupChatModel> msgList, Context context) {
        this.msgList = msgList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_message_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        try {
            deCipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        secretKeySpec = new SecretKeySpec(encryptionKey, "AES");

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

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
                holder.receiver_text_name.setText(msgList.get(position).getFromName());
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
                holder.receiver_image_name.setText(msgList.get(position).getFromName());
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
                holder.receiver_video_name.setText(msgList.get(position).getFromName());
                holder.receiver_video_time.setText(msgList.get(position).getTime());
                holder.receiver_video.setVideoPath(msgList.get(position).getMessage());
            }
        }
        else {    // audio or pdf
            if (msgList.get(position).getFrom().equals(uid)) {
                holder.sender_audio_layout.setVisibility(View.VISIBLE);
                holder.sender_audio.setText(msgList.get(position).getFileName());
                holder.sender_audio_time.setText(msgList.get(position).getTime());
            }
            else {
                holder.receiver_audio_layout.setVisibility(View.VISIBLE);
                holder.receiver_audio_name.setText(msgList.get(position).getFromName());
                holder.receiver_audio.setText(msgList.get(position).getFileName());
                holder.receiver_audio_time.setText(msgList.get(position).getTime());

            }
        }

        holder.sender_video.setOnClickListener(v -> {holder.sender_video.start();});
        holder.receiver_video.setOnClickListener(v -> {holder.receiver_video.start();});
        holder.sender_audio_layout.setOnClickListener(v -> downloadMyData(msgList.get(position).getMessage(), msgList.get(position).getFileName()));
        holder.receiver_audio_layout.setOnClickListener(v -> downloadMyData(msgList.get(position).getMessage(), msgList.get(position).getFileName()));
        holder.sender_image_layout.setOnClickListener(v -> {
            Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(msgList.get(position).getMessage()));
            context.startActivity(browserIntent);
        });
        holder.receiver_image_layout.setOnClickListener(v -> {
            Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(msgList.get(position).getMessage()));
            context.startActivity(browserIntent);
        });
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout sender_text_layout, receiver_text_layout, sender_image_layout, receiver_image_layout;
        LinearLayout receiver_audio_layout, sender_audio_layout, receiver_video_layout, sender_video_layout;
        TextView receiver_audio, sender_audio, receiver_audio_name, receiver_audio_time, sender_audio_time;
        TextView sender_msg, receiver_text_name, sender_text_time, receiver_text_time, receiver_msg;
        TextView receiver_image_name, receiver_image_time, sender_image_time;
        TextView receiver_video_name, receiver_video_time, sender_video_time;
        ImageView  receiver_image, sender_image;
        VideoView receiver_video, sender_video;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sender_text_layout = itemView.findViewById(R.id.group_sender_layout);
            receiver_text_layout = itemView.findViewById(R.id.group_receiver_layout);
            sender_msg = itemView.findViewById(R.id.group_sender_message);
            receiver_msg = itemView.findViewById(R.id.group_receiver_msg);
            receiver_text_name = itemView.findViewById(R.id.group_receiver_name);
            sender_text_time = itemView.findViewById(R.id.group_sender_time);
            receiver_text_time = itemView.findViewById(R.id.group_receiver_time);

            sender_image_layout = itemView.findViewById(R.id.group_sender_image_layout);
            receiver_image_layout = itemView.findViewById(R.id.group_receiver_image_layout);
            receiver_image_name = itemView.findViewById(R.id.group_receiver_image_name);
            receiver_image = itemView.findViewById(R.id.group_receiver_image);
            sender_image = itemView.findViewById(R.id.group_sender_image);
            receiver_image_time = itemView.findViewById(R.id.group_receiver_image_time);
            sender_image_time = itemView.findViewById(R.id.group_sender_image_time);

            receiver_audio_layout = itemView.findViewById(R.id.group_receiver_audio_layout);
            sender_audio_layout = itemView.findViewById(R.id.group_audio_sender_layout);
            receiver_audio_name = itemView.findViewById(R.id.group_audio_receiver_name);
            receiver_audio = itemView.findViewById(R.id.group_audio_receiver);
            sender_audio = itemView.findViewById(R.id.group_audio_sender);
            receiver_audio_time = itemView.findViewById(R.id.group_audio_receiver_time);
            sender_audio_time = itemView.findViewById(R.id.group_audio_sender_time);

            receiver_video_layout = itemView.findViewById(R.id.group_receiver_video_layout);
            sender_video_layout = itemView.findViewById(R.id.group_video_sender_layout);
            receiver_video = itemView.findViewById(R.id.group_video_receiver);
            sender_video = itemView.findViewById(R.id.group_video_sender);
            receiver_video_name = itemView.findViewById(R.id.group_video_receiver_name);
            receiver_video_time = itemView.findViewById(R.id.group_video_receiver_time);
            sender_video_time = itemView.findViewById(R.id.group_video_sender_time);
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
