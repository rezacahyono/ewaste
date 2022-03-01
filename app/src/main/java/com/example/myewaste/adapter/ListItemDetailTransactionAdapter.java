package com.example.myewaste.adapter;

import static com.example.myewaste.utils.Utils.convertToRupiah;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myewaste.databinding.ItemListDetailItemTransactionBinding;
import com.example.myewaste.model.utils.ListItem;

import java.util.ArrayList;
import java.util.List;

public class ListItemDetailTransactionAdapter extends RecyclerView.Adapter<ListItemDetailTransactionAdapter.ListItemDetailTransactionViewHolder> {

    private final ArrayList<ListItem> listItems = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter(List<ListItem> list) {
        if (list == null) return;
        listItems.clear();
        listItems.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ListItemDetailTransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListDetailItemTransactionBinding binding = ItemListDetailItemTransactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ListItemDetailTransactionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemDetailTransactionViewHolder holder, int position) {
        holder.bind(listItems.get(position));
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public static class ListItemDetailTransactionViewHolder extends RecyclerView.ViewHolder {
        ItemListDetailItemTransactionBinding binding;

        public ListItemDetailTransactionViewHolder(ItemListDetailItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ListItem listItem) {
            binding.tvNameItemMaster.setText(listItem.getNameItem());
            binding.tvNameItemType.setText(listItem.getNameItemType());
            binding.tvTypeTotal.setText(listItem.getTotal() + " " + listItem.getNameUnit());
            binding.tvPrice.setText(convertToRupiah(listItem.getPrice()));
        }
    }
}
