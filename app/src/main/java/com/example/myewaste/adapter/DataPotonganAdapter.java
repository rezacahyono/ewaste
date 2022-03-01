package com.example.myewaste.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myewaste.R;
import com.example.myewaste.model.saldo.SaldoTransaction;

import java.text.SimpleDateFormat;
import java.util.List;

public class DataPotonganAdapter extends RecyclerView.Adapter<DataPotonganAdapter.ViewHolder> {

    Context context;
    List<SaldoTransaction> list;

    public DataPotonganAdapter(Context context, List<SaldoTransaction> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public DataPotonganAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_pendapatan_kas,parent,false);
        return new DataPotonganAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataPotonganAdapter.ViewHolder holder, int position) {
//        holder.idTransaksi.setText("ID Transaksi       : "+ list.get(position).getId_transaksi_saldo());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//        Date date = new Date(list.get(position).getTanggal_transaksi());
//        String tanggalTransaksi = sdf.format(date);
//        holder.tglTransaksi.setText("Tanggal Transaksi : "+ tanggalTransaksi);
//        holder.jmlPotongan.setText("Jumlah Potongan    : "+ Utils.convertToRupiah(list.get(position).getPotongan()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        RelativeLayout layout;
        TextView idTransaksi, tglTransaksi, jmlPotongan;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            idTransaksi = itemView.findViewById(R.id.tv_transaksi_saldo);
            layout = itemView.findViewById(R.id.layout_list_potongan);
            tglTransaksi = itemView.findViewById(R.id.tv_tanggal_transaksi);
            jmlPotongan = itemView.findViewById(R.id.tv_jumlah_potongan);
        }
    }
}
