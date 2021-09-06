package com.skynet.skynettest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CreateGroupAdapter extends RecyclerView.Adapter<CreateGroupAdapter.ViewHolder>{

    private ArrayList<ShowUserModel> memberList;
    private Context context;
    private onItemCheckListener onItemCheckListener;

    public CreateGroupAdapter(ArrayList<ShowUserModel> memberList, Context context, onItemCheckListener onItemCheckListener) {
        this.memberList = memberList;
        this.context = context;
        this.onItemCheckListener = onItemCheckListener;
    }

    interface onItemCheckListener{
        void onItemCheck(String member);
        void onItemUncheck(String member);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.create_group_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(memberList.get(position).getName());
        holder.email.setText(memberList.get(position).getEmail());
        Glide.with(context).load(memberList.get(position).getImage())
                .placeholder(R.drawable.user_profile_icon).into(holder.profile);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.checkBox.setChecked(!holder.checkBox.isChecked());
                if (holder.checkBox.isChecked()) onItemCheckListener.onItemCheck(memberList.get(position).getUid());
                else onItemCheckListener.onItemUncheck(memberList.get(position).getUid());
            }
        });
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView name, email;
        ImageView profile;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.gmName);
            email = itemView.findViewById(R.id.gmEmail);
            profile = itemView.findViewById(R.id.gmProfile);
            checkBox = itemView.findViewById(R.id.gmCheck);
            checkBox.setClickable(false);
        }
    }
}
