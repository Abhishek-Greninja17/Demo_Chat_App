package com.skynet.skynettest;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skynet.skynettest.admin.AdminChatActivity;
import com.skynet.skynettest.employee.EmployeeChatActivity;

import java.util.ArrayList;

public class ShowUserAdapter extends RecyclerView.Adapter<ShowUserAdapter.ViewHolder> {

    private ArrayList<ShowUserModel> AdminList;
    private Context context;
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users");

    public ShowUserAdapter(ArrayList<ShowUserModel> employee, Context context) {
        AdminList = employee;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_display_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        viewHolder.itemView.setTag(AdminList.get(position));

        if (AdminList.get(position).getType().equals("group")){
            viewHolder.chat_employee_name.setText(AdminList.get(position).getName());
            viewHolder.company_name.setText("This is a group");
        } else {
            viewHolder.chat_employee_name.setText(AdminList.get(position).getName());
            viewHolder.company_name.setText(AdminList.get(position).getEmail() + " (" + AdminList.get(position).getType() + " )");
        }
        Glide.with(context).load(AdminList.get(position).getImage()).placeholder(R.drawable.user_profile_icon).into(viewHolder.chat_user_logo);

        viewHolder.itemView.setOnClickListener(v -> {
            if (AdminList.get(position).getType().equals("group")){
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupName", AdminList.get(position).getName());
                intent.putExtra("groupImage", AdminList.get(position).getImage());
                context.startActivity(intent);
            } else {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                dbRef.child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child("type").getValue().equals("admin")) {
                            Intent intent = new Intent(context, AdminChatActivity.class);
                            dbRef.child(AdminList.get(position).getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild("image")) {
                                        intent.putExtra("id", AdminList.get(position).getUid());
                                        intent.putExtra("company", AdminList.get(position).getCompany());
                                        intent.putExtra("email", AdminList.get(position).getEmail());
                                        intent.putExtra("name", AdminList.get(position).getName());
                                        intent.putExtra("image", snapshot.child("image").getValue().toString());
                                    } else {
                                        intent.putExtra("id", AdminList.get(position).getUid());
                                        intent.putExtra("company", AdminList.get(position).getCompany());
                                        intent.putExtra("email", AdminList.get(position).getEmail());
                                        intent.putExtra("name", AdminList.get(position).getName());
                                    }
                                    context.startActivity(intent);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else if (snapshot.child("type").getValue().equals("employee")) {
                            Intent intent = new Intent(context, EmployeeChatActivity.class);
                            dbRef.child(AdminList.get(position).getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild("image")) {
                                        intent.putExtra("id", AdminList.get(position).getUid());
                                        intent.putExtra("company", AdminList.get(position).getCompany());
                                        intent.putExtra("email", AdminList.get(position).getEmail());
                                        intent.putExtra("name", AdminList.get(position).getName());
                                        intent.putExtra("image", snapshot.child("image").getValue().toString());
                                    } else {
                                        intent.putExtra("id", AdminList.get(position).getUid());
                                        intent.putExtra("company", AdminList.get(position).getCompany());
                                        intent.putExtra("email", AdminList.get(position).getEmail());
                                        intent.putExtra("name", AdminList.get(position).getName());
                                    }
                                    context.startActivity(intent);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return AdminList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView chat_employee_name;
        TextView company_name;
        ImageView chat_user_logo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chat_employee_name = itemView.findViewById(R.id.chat_employee_name);
            company_name = itemView.findViewById(R.id.company_name);
            chat_user_logo = itemView.findViewById(R.id.chat_user_logo);
        }
    }
}
