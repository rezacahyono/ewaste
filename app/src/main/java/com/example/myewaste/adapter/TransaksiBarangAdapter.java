package com.example.myewaste.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myewaste.R;
import com.example.myewaste.model.item.ItemTransaction;
import com.example.myewaste.ui.admin.task.TransactionItemActivity;

import java.util.List;


public class TransaksiBarangAdapter extends RecyclerView.Adapter<TransaksiBarangAdapter.ViewHolder>
{
    Context context;
    List<ItemTransaction> list;
    TransactionItemActivity listener;

    public TransaksiBarangAdapter(Context context, List<ItemTransaction> list, TransactionItemActivity listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_transaksi_barang,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.transaksi.setText(list.get(position).getNo_item_transaction());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickListTransaksi(list.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        LinearLayout layout;
        TextView transaksi;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transaksi = itemView.findViewById(R.id.txt_transaksi);
            layout = itemView.findViewById(R.id.layout_list_transaksi);
        }
    }
}
