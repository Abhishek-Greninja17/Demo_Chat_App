package com.skynet.skynettest.superAdmin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skynet.skynettest.R;

import java.util.List;

public class AdminInfoAdapter extends RecyclerView.Adapter<AdminInfoAdapter.ViewHolder> {

    private List<AdminModel> userList;
    private Context context;

    public AdminInfoAdapter(Context context, List<AdminModel> userList) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(userList.get(position).getName() + " ( " + userList.get(position).getType() + " )");
        holder.company.setText(userList.get(position).getCompany());
        holder.email.setText(userList.get(position).getEmail());
        holder.password.setText("Password: " + userList.get(position).getPassword());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddUserActivity.class);
            intent.putExtra("uid", userList.get(position).getUid());
            intent.putExtra("name", userList.get(position).getName());
            intent.putExtra("company", userList.get(position).getCompany());
            intent.putExtra("email", userList.get(position).getEmail());
            intent.putExtra("password", userList.get(position).getPassword());
            intent.putExtra("type", userList.get(position).getType());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView name, company, email, password;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvname);
            company = itemView.findViewById(R.id.tvcompany);
            email = itemView.findViewById(R.id.tvemail);
            password = itemView.findViewById(R.id.tvpassword);
        }
    }
}
