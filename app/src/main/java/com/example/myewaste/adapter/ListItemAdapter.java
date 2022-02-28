package com.example.myewaste.adapter;

import static com.example.myewaste.utils.Util.convertToRupiah;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myewaste.R;
import com.example.myewaste.databinding.ItemListItemBinding;
import com.example.myewaste.model.ListItem;

import java.util.ArrayList;
import java.util.List;

public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ListItemViewHolder> {

    private final ArrayList<ListItem> listItems = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter(List<ListItem> list) {
        if (list == null) return;
        listItems.clear();
        listItems.addAll(list);
        notifyDataSetChanged();
    }

    private OnItemClickCallbackListItem onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallbackListItem onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    @NonNull
    @Override
    public ListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListItemBinding binding = ItemListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ListItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemViewHolder holder, int position) {
        holder.bind(listItems.get(position));
        holder.itemView.setOnLongClickListener(v -> {
            onItemClickCallback.onClickedRemove(holder.getAdapterPosition());
            return false;
        });
        holder.itemView.setOnClickListener(v -> onItemClickCallback.onClickedListItem(listItems.get(holder.getAdapterPosition()),holder.getAdapterPosition()));

    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public static class ListItemViewHolder extends RecyclerView.ViewHolder {
        ItemListItemBinding binding;

        public ListItemViewHolder(ItemListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ListItem listItem) {
            binding.tvItemMaster.setText(listItem.getNameItem());
            binding.tvItemType.setText(listItem.getNameItemType());
            binding.tvItemTotal.setText(itemView.getResources().getString(R.string.placeholder_unit, String.valueOf(listItem.getTotal()), listItem.getNameUnit()));
            binding.tvItemTotalIncome.setText(convertToRupiah(listItem.getPrice()));
        }
    }


    public interface OnItemClickCallbackListItem {
        void onClickedRemove(int position);

        void onClickedListItem(ListItem listItem, int postion);
    }
}
