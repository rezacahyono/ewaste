package com.example.myewaste.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myewaste.databinding.ItemListItemTypeParentBinding;
import com.example.myewaste.model.utils.ListItemType;

import java.util.ArrayList;
import java.util.List;

public class ListItemTypeParentAdapter extends RecyclerView.Adapter<ListItemTypeParentAdapter.ListItemTypeParentViewHolder> {

    private final ArrayList<ListItemType> listItemTypes = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter(List<ListItemType> list) {
        if (list == null) return;
        listItemTypes.clear();
        listItemTypes.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ListItemTypeParentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListItemTypeParentBinding binding = ItemListItemTypeParentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ListItemTypeParentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemTypeParentViewHolder holder, int position) {
        holder.bind(listItemTypes.get(position));
    }

    @Override
    public int getItemCount() {
        return listItemTypes.size();
    }

    public static class ListItemTypeParentViewHolder extends RecyclerView.ViewHolder {
        private final ListItemTypeChildAdapter adapterChild = new ListItemTypeChildAdapter();
        ItemListItemTypeParentBinding binding;

        public ListItemTypeParentViewHolder(ItemListItemTypeParentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ListItemType listItemType) {
            binding.tvNameItemMaster.setText(listItemType.getNameItemMaster());
            adapterChild.setAdapter(listItemType.getItemTypes());
            binding.rvItemMaster.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            binding.rvItemMaster.setAdapter(adapterChild);
        }
    }

}
