package com.example.myewaste.adapter;

import static com.example.myewaste.utils.Constant.EXTRAS_ACTION_MODE;
import static com.example.myewaste.utils.Constant.EXTRAS_ITEM_TYPE;
import static com.example.myewaste.utils.Constant.MODE_UPDATE;
import static com.example.myewaste.utils.Utils.convertToRupiah;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myewaste.ui.admin.task.AddUpdateItemTypeActivity;
import com.example.myewaste.databinding.ItemListItemTypeChildBinding;
import com.example.myewaste.model.item.ItemType;

import java.util.ArrayList;
import java.util.List;

public class ListItemTypeChildAdapter extends RecyclerView.Adapter<ListItemTypeChildAdapter.ListItemTypeChildViewHolder> {

    private final ArrayList<ItemType> listItemType = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter(List<ItemType> list) {
        if (list == null) return;
        listItemType.clear();
        listItemType.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ListItemTypeChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListItemTypeChildBinding binding = ItemListItemTypeChildBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ListItemTypeChildViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemTypeChildViewHolder holder, int position) {
        holder.bind(listItemType.get(position));
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), AddUpdateItemTypeActivity.class);
            intent.putExtra(EXTRAS_ACTION_MODE, MODE_UPDATE);
            intent.putExtra(EXTRAS_ITEM_TYPE, listItemType.get(position));
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listItemType.size();
    }

    public static class ListItemTypeChildViewHolder extends RecyclerView.ViewHolder {
        ItemListItemTypeChildBinding binding;

        public ListItemTypeChildViewHolder(ItemListItemTypeChildBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ItemType itemType) {
            binding.tvNameItemType.setText(itemType.getName());
            binding.tvPriceAndUnitItemType.setText(convertToRupiah((int) itemType.getPrice()));
        }
    }
}
