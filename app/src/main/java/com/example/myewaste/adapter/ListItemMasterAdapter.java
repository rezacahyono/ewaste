package com.example.myewaste.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myewaste.R;
import com.example.myewaste.databinding.ItemListItemMasterBinding;
import com.example.myewaste.model.item.ItemMaster;

import java.util.ArrayList;
import java.util.List;

public class ListItemMasterAdapter extends RecyclerView.Adapter<ListItemMasterAdapter.ListItemViewHolder> {

    private final ArrayList<ItemMaster> listItem = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter(List<ItemMaster> list) {
        if (list == null) return;
        listItem.clear();
        listItem.addAll(list);
        notifyDataSetChanged();
    }

    private OnItemClickCallbackItemMaster onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallbackItemMaster onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    @NonNull
    @Override
    public ListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListItemMasterBinding binding = ItemListItemMasterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ListItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemViewHolder holder, int position) {
        holder.bind(listItem.get(position));
        holder.itemView.setOnLongClickListener(v -> {
            onItemClickCallback.onClickedLong(listItem.get(holder.getAdapterPosition()));
            return false;
        });

        holder.itemView.setOnClickListener(v -> onItemClickCallback.onClickedShort(listItem.get(holder.getAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return listItem.size();
    }

    public static class ListItemViewHolder extends RecyclerView.ViewHolder {
        ItemListItemMasterBinding binding;

        public ListItemViewHolder(ItemListItemMasterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ItemMaster itemMaster) {
            Glide.with(itemView)
                    .load(itemMaster.getPhoto())
                    .error(R.drawable.ic_item_master)
                    .placeholder(R.color.green_20)
                    .into(binding.ivPhotoItem);

            binding.tvName.setText(itemMaster.getName());
        }
    }

    public interface OnItemClickCallbackItemMaster {
        void onClickedLong(ItemMaster itemMaster);

        void onClickedShort(ItemMaster itemMaster);
    }
}
