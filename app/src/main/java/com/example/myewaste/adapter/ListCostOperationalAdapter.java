package com.example.myewaste.adapter;

import static com.example.myewaste.utils.Utils.convertDate;
import static com.example.myewaste.utils.Utils.convertToRupiah;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myewaste.databinding.ItemListCostOperationalBinding;
import com.example.myewaste.model.saldo.SaldoTransaction;

import java.util.ArrayList;
import java.util.List;

public class ListCostOperationalAdapter extends RecyclerView.Adapter<ListCostOperationalAdapter.ListCostOperationalViewHolder> {
    private final ArrayList<SaldoTransaction> listSaldoTranasction = new ArrayList<>();


    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter(List<SaldoTransaction> list) {
        if (list == null) return;
        listSaldoTranasction.clear();
        listSaldoTranasction.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ListCostOperationalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListCostOperationalBinding binding = ItemListCostOperationalBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ListCostOperationalViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListCostOperationalViewHolder holder, int position) {
        holder.bind(listSaldoTranasction.get(position));
    }

    @Override
    public int getItemCount() {
        return listSaldoTranasction.size();
    }

    public static class ListCostOperationalViewHolder extends RecyclerView.ViewHolder {
        ItemListCostOperationalBinding binding;

        public ListCostOperationalViewHolder(ItemListCostOperationalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SaldoTransaction saldoTransaction) {
            binding.tvNoTransaction.setText(saldoTransaction.getNo_saldo_transaction());
            binding.tvDate.setText(convertDate(saldoTransaction.getDate()));
            binding.tvTotalIncome.setText(convertToRupiah((int) saldoTransaction.getCuts_transaction()));
        }
    }
}
