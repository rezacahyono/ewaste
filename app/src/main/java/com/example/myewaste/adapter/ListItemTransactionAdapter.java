package com.example.myewaste.adapter;

import static com.example.myewaste.utils.Util.convertDate;
import static com.example.myewaste.utils.Util.convertToRupiah;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myewaste.databinding.ItemListItemTransactionBinding;
import com.example.myewaste.model.item.ItemTransaction;

import java.util.ArrayList;
import java.util.List;

public class ListItemTransactionAdapter extends RecyclerView.Adapter<ListItemTransactionAdapter.ListItemTransactionViewHolder> {

    private final ArrayList<ItemTransaction> listItemTransaction = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter(List<ItemTransaction> list) {
        if (list == null) return;
        listItemTransaction.clear();
        listItemTransaction.addAll(list);
        notifyDataSetChanged();
    }

    private OnItemClickCallbackItemTransaction onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallbackItemTransaction onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    @NonNull
    @Override
    public ListItemTransactionAdapter.ListItemTransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListItemTransactionBinding binding = ItemListItemTransactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ListItemTransactionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemTransactionAdapter.ListItemTransactionViewHolder holder, int position) {
        holder.bind(listItemTransaction.get(position));
        holder.itemView.setOnClickListener(v -> onItemClickCallback.onClicked(listItemTransaction.get(holder.getAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return listItemTransaction.size();
    }

    public static class ListItemTransactionViewHolder extends RecyclerView.ViewHolder {
        ItemListItemTransactionBinding binding;

        public ListItemTransactionViewHolder(ItemListItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ItemTransaction itemTransaction) {
            binding.tvNoTransaction.setText(itemTransaction.getNo_item_transaction());
            binding.tvTotalIncome.setText(convertToRupiah((int) itemTransaction.getTotal_price()));
            binding.tvDate.setText(convertDate(itemTransaction.getDate()));
        }
    }

    public interface OnItemClickCallbackItemTransaction {
        void onClicked(ItemTransaction itemTransaction);
    }
}