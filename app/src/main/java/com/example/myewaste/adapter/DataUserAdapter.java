package com.example.myewaste.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myewaste.R;
import com.example.myewaste.model.user.User;
import com.example.myewaste.model.user.UserData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.example.myewaste.utils.Util.loadImage;

public class DataUserAdapter extends RecyclerView.Adapter<DataUserAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<com.example.myewaste.model.user.UserData> UserData;

    public DataUserAdapter(Context cont, ArrayList<UserData> data){
        context= cont;
        UserData= data;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user ,parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.vnoregis.setText(UserData.get(position).getNo_regis());
        holder.vnama.setText(UserData.get(position).getName());
        loadImage(UserData.get(position).getNik(), holder.vdetailnasabah, context);
        isActiveAccount(UserData.get(position), holder.restoreUser, holder.deleteUser);
        holder.deleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("Hapus User")
                        .setMessage("Apakah Anda Yakin Ingin Menghapus User Ini ?")
                        .setPositiveButton("Ya", (dialogInterface, i) -> {
                            holder.deleteUser.setVisibility(View.GONE);
                            holder.restoreUser.setVisibility(View.VISIBLE);
                            onDelete(UserData.get(position));
                            Toast.makeText(context, "Berhasil Menghapus Data", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Tidak", (dialogInterface, i) -> dialogInterface.dismiss()).show();
            }
        });

        holder.restoreUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("Restore User")
                        .setMessage("Apakah Anda Merestore User Ini ?")
                        .setPositiveButton("Ya", (dialogInterface, i) -> {
                            holder.deleteUser.setVisibility(View.VISIBLE);
                            holder.restoreUser.setVisibility(View.GONE);
                            onRestore(UserData.get(position));
                            Toast.makeText(context, "Berhasil Merestore Data", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Tidak", (dialogInterface, i) -> dialogInterface.dismiss()).show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return UserData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView vnoregis, vnama;
        ImageView vdetailnasabah;
        ImageView deleteUser;
        ImageView restoreUser;

        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            vnoregis = itemView.findViewById(R.id.tv_noregis);
            vnama = itemView.findViewById(R.id.tv_nama);
            vdetailnasabah = itemView.findViewById(R.id.imageViewFoto);
            deleteUser = itemView.findViewById(R.id.deleteUser);
            restoreUser = itemView.findViewById(R.id.restoreUser);
        }
    }

    private void isActiveAccount(UserData userData, ImageView update, ImageView delete){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user");
        Query userQuery = databaseReference.orderByChild("noregis").equalTo(userData.getNo_regis());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data : snapshot.getChildren()){
                    String status = data.getValue(User.class).getStatus();
                    if(status.equals("aktif")){
                        delete.setVisibility(View.VISIBLE);
                        update.setVisibility(View.GONE);
                    }else{
                        delete.setVisibility(View.GONE);
                        update.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void onRestore(com.example.myewaste.model.user.UserData userData) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user");
        Query user = databaseReference.orderByChild("noregis").equalTo(userData.getNo_regis());
        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data : snapshot.getChildren()){
                    data.getRef().child("status").setValue("aktif");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("error", "onCancelled: delete user");
            }
        });
    }

    private void onDelete(UserData userData){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user");
        Query user = databaseReference.orderByChild("noregis").equalTo(userData.getNo_regis());
        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data : snapshot.getChildren()){
                    data.getRef().child("status").setValue("non-aktif");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("error", "onCancelled: delete user");
            }
        });

    }
}

