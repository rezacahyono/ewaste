package com.example.myewaste.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myewaste.R;
import com.example.myewaste.databinding.ItemListSaldoNasabahBinding;
import com.example.myewaste.model.user.UserData;

import java.util.ArrayList;
import java.util.List;

public class ListSaldoNasabahAdapter extends RecyclerView.Adapter<ListSaldoNasabahAdapter.ListSaldoNasabahViewHolder> {

    private final ArrayList<UserData> listUserData = new ArrayList<>();


    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter(List<UserData> list) {
        if (list == null) return;
        listUserData.clear();
        listUserData.addAll(list);
        notifyDataSetChanged();
    }

    private OnItemAction onItemAction;

    public void setOnItemAction(OnItemAction onItemAction) {
        this.onItemAction = onItemAction;
    }

    @NonNull
    @Override
    public ListSaldoNasabahViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListSaldoNasabahBinding binding = ItemListSaldoNasabahBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ListSaldoNasabahViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListSaldoNasabahViewHolder holder, int position) {
        holder.bind(listUserData.get(position));
        onItemAction.setSaldoUser(listUserData.get(position), holder.binding.tvTotalSaldo);
    }


    @Override
    public int getItemCount() {
        return listUserData.size();
    }

    public static class ListSaldoNasabahViewHolder extends RecyclerView.ViewHolder {
        ItemListSaldoNasabahBinding binding;

        public ListSaldoNasabahViewHolder(ItemListSaldoNasabahBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(UserData userData) {
            Glide.with(itemView.getContext())
                    .load(userData.getAvatar())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(binding.ivAvatar);
            binding.tvName.setText(userData.getName());
            binding.tvNoRegis.setText(userData.getNo_regis());
        }
    }

    public interface OnItemAction {
        void setSaldoUser(UserData userData, TextView tvSaldo);
    }
}