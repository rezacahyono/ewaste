package com.example.myewaste.adapter;

import static com.example.myewaste.utils.Constant.PENDING;
import static com.example.myewaste.utils.Constant.REJECTED;
import static com.example.myewaste.utils.Utils.convertDate;
import static com.example.myewaste.utils.Utils.convertToRupiah;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myewaste.R;
import com.example.myewaste.databinding.ItemListSaldoTransactionBinding;
import com.example.myewaste.model.saldo.SaldoTransaction;

import java.util.ArrayList;
import java.util.List;

public class ListSaldoTransactionAdapter extends RecyclerView.Adapter<ListSaldoTransactionAdapter.ListSaldoTransactionViewHolder> {
    private ArrayList<SaldoTransaction> listSaldoTransaction = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter(List<SaldoTransaction> list) {
        if (list == null) return;
        listSaldoTransaction.clear();
        listSaldoTransaction.addAll(list);
        notifyDataSetChanged();
    }

    private OnItemClickCallbackSaldoTransaction onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallbackSaldoTransaction onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    @NonNull
    @Override
    public ListSaldoTransactionAdapter.ListSaldoTransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListSaldoTransactionBinding binding = ItemListSaldoTransactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ListSaldoTransactionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListSaldoTransactionAdapter.ListSaldoTransactionViewHolder holder, int position) {
        holder.bind(listSaldoTransaction.get(position));
        holder.itemView.setOnClickListener(v -> onItemClickCallback.onClicked(listSaldoTransaction.get(holder.getAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return listSaldoTransaction.size();
    }

    public static class ListSaldoTransactionViewHolder extends RecyclerView.ViewHolder {
        ItemListSaldoTransactionBinding binding;

        public ListSaldoTransactionViewHolder(ItemListSaldoTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SaldoTransaction saldoTransaction) {
            binding.tvNoTransaction.setText(saldoTransaction.getNo_saldo_transaction());
            binding.tvDate.setText(convertDate(saldoTransaction.getDate()));
            binding.tvWithdraw.setText(convertToRupiah((int) saldoTransaction.getTotal_income()));
            binding.tvCutsTransaction.setText(convertToRupiah((int) saldoTransaction.getCuts_transaction()));
            int income = (int) (saldoTransaction.getTotal_income() - saldoTransaction.getCuts_transaction());
            binding.tvTotalIncome.setText(convertToRupiah(income));

            if (saldoTransaction.getStatus().equalsIgnoreCase(PENDING)){
                binding.flNoTransaction.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_pending));
                binding.tvNoTransaction.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.yellow));
            }else if (saldoTransaction.getStatus().equalsIgnoreCase(REJECTED)){
                binding.flNoTransaction.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_rejected));
                binding.tvNoTransaction.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.red));
            }else {
                binding.flNoTransaction.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_accepted));
                binding.tvNoTransaction.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.green));
            }
        }
    }

    public interface OnItemClickCallbackSaldoTransaction {
        void onClicked(SaldoTransaction saldoTransaction);
    }
}
