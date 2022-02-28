package com.example.myewaste.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myewaste.databinding.ItemListUnitBinding;
import com.example.myewaste.model.item.UnitItem;

import java.util.ArrayList;
import java.util.List;

public class ListUnitItemAdapter extends RecyclerView.Adapter<ListUnitItemAdapter.ListUnitItemViewHolder> {

    private final ArrayList<UnitItem> listUnit = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter(List<UnitItem> list) {
        if (list == null) return;
        listUnit.clear();
        listUnit.addAll(list);
        notifyDataSetChanged();
    }

    private OnItemClickCallbackUnitItem onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallbackUnitItem onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    @NonNull
    @Override
    public ListUnitItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListUnitBinding binding = ItemListUnitBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ListUnitItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListUnitItemViewHolder holder, int position) {
        holder.bind(listUnit.get(position));
        holder.binding.ibUpdateUnitItem.setOnClickListener(v -> onItemClickCallback.onClickedUpdate(listUnit.get(holder.getAdapterPosition())));
        holder.binding.ibTrashUnitItem.setOnClickListener(v -> onItemClickCallback.onClickedRemove(listUnit.get(holder.getAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return listUnit.size();
    }


    public static class ListUnitItemViewHolder extends RecyclerView.ViewHolder {

        ItemListUnitBinding binding;

        public ListUnitItemViewHolder(ItemListUnitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(UnitItem unitItem) {
            binding.tvUnitItemName.setText(unitItem.getName());
        }
    }

    public interface OnItemClickCallbackUnitItem {
        void onClickedRemove(UnitItem unitItem);

        void onClickedUpdate(UnitItem unitItem);
    }
}
