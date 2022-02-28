package com.example.myewaste.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myewaste.DataSaldoNasabahActivity;
import com.example.myewaste.R;
import com.example.myewaste.model.user.UserData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.example.myewaste.utils.Util.loadImage;

public class DataSaldoNasabahAdapter extends RecyclerView.Adapter<DataSaldoNasabahAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<com.example.myewaste.model.user.UserData> UserData;
    DataSaldoNasabahActivity.DataSaldoNasabahListener listener;

    public DataSaldoNasabahAdapter(Context cont, ArrayList<UserData> data, DataSaldoNasabahActivity.DataSaldoNasabahListener listener){
        context= cont;
        UserData= data;
        this.listener = listener;
    }

    @NotNull
    @Override
    public DataSaldoNasabahAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_saldo_nasabah ,parent, false);
        return new DataSaldoNasabahAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataSaldoNasabahAdapter.MyViewHolder holder, int position) {
        holder.vnoregis.setText(UserData.get(position).getNo_regis());
        holder.vnama.setText(UserData.get(position).getName());
        loadImage(UserData.get(position).getNik(), holder.vdetailnasabah, context);
        listener.onLoadSaldoNasabah(UserData.get(position).getNik(), holder.vSaldo);
    }

    @Override
    public int getItemCount() {
        return UserData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView vnoregis, vnama,vSaldo;
        ImageView vdetailnasabah;

        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            vnoregis = itemView.findViewById(R.id.tv_noregis);
            vnama = itemView.findViewById(R.id.tv_nama);
            vSaldo = itemView.findViewById(R.id.saldo_nasabah);
            vdetailnasabah = itemView.findViewById(R.id.imageViewFoto);
        }
    }

}