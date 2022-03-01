package com.example.myewaste.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myewaste.R;
import com.example.myewaste.databinding.ItemListUserBinding;
import com.example.myewaste.model.user.UserData;

import java.util.ArrayList;
import java.util.List;

public class ListUserAdapater extends RecyclerView.Adapter<ListUserAdapater.ListUserViewHolder> {
    private final ArrayList<UserData> listUser = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter(List<UserData> list) {
        if (list == null) return;
        listUser.clear();
        listUser.addAll(list);
        notifyDataSetChanged();
    }

    private OnItemClickCallbackUser onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallbackUser onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    private OnItemAction onItemAction;

    public void setOnItemAction(OnItemAction onItemAction) {
        this.onItemAction = onItemAction;
    }

    @NonNull
    @Override
    public ListUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListUserBinding binding = ItemListUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ListUserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListUserViewHolder holder, int position) {
        holder.bind(listUser.get(position));
        holder.itemView.setOnClickListener(view -> onItemClickCallback.onClicked(listUser.get(holder.getAdapterPosition())));
        if (onItemAction != null) {
            onItemAction.setVisibilityUpdate(listUser.get(position), holder.binding.ibActive, holder.binding.ibDeactive);
            holder.binding.ibDeactive.setOnClickListener(view -> onItemAction.setDeactive(listUser.get(holder.getAdapterPosition())));
            holder.binding.ibActive.setOnClickListener(view -> onItemAction.setActive(listUser.get(holder.getAdapterPosition())));
        }
    }

    @Override
    public int getItemCount() {
        return listUser.size();
    }

    public static class ListUserViewHolder extends RecyclerView.ViewHolder {
        ItemListUserBinding binding;

        public ListUserViewHolder(ItemListUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(UserData user) {
            Glide.with(itemView.getContext())
                    .load(user.getAvatar())
                    .placeholder(R.drawable.ic_placeholder)
                    .into(binding.ivAvatar);

            binding.tvName.setText(user.getName());
            binding.tvNoRegis.setText(user.getNo_regis());
        }
    }

    public interface OnItemClickCallbackUser {
        void onClicked(UserData userData);
    }

    public interface OnItemAction {
        void setVisibilityUpdate(UserData userData, ImageButton ibActive, ImageButton ibDeactive);

        void setDeactive(UserData userData);

        void setActive(UserData userData);
    }
}
