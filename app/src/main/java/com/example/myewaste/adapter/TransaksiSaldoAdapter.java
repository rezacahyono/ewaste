package com.example.myewaste.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myewaste.ui.component.TransactionSaldoActivity;
import com.example.myewaste.R;
import com.example.myewaste.model.saldo.SaldoTransaction;

import java.util.List;

public class TransaksiSaldoAdapter extends RecyclerView.Adapter<TransaksiSaldoAdapter.ViewHolder> {

    Context context;
    List<SaldoTransaction> list;
    TransactionSaldoActivity listener;

    public TransaksiSaldoAdapter(Context context, List<SaldoTransaction> list, TransactionSaldoActivity listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransaksiSaldoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_transaksi_saldo,parent,false);
        return new TransaksiSaldoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransaksiSaldoAdapter.ViewHolder holder, int position) {
//        holder.transaksi.setText(list.get(position).getId_transaksi_saldo());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                listener.onClickListTransaksiSaldo(list.get(position));
            }
        });

        if(list.get(position).getStatus().equals("APPROVED")){
            holder.ivApproved.setVisibility(View.VISIBLE);
        }else if(list.get(position).getStatus().equals("REJECTED")){
            holder.ivRejected.setVisibility(View.VISIBLE);
        }else{
            holder.ivPending.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        RelativeLayout layout;
        TextView transaksi;
        ImageView ivPending, ivApproved, ivRejected;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transaksi = itemView.findViewById(R.id.tv_transaksi_saldo);
            layout = itemView.findViewById(R.id.layout_list_transaksi_saldo);
            ivPending = itemView.findViewById(R.id.iv_pending);
            ivApproved = itemView.findViewById(R.id.iv_approved);
            ivRejected = itemView.findViewById(R.id.iv_rejected);
        }
    }
}
