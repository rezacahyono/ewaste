package com.example.myewaste.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myewaste.databinding.ItemListTaskBinding;
import com.example.myewaste.model.utils.Task;

import java.util.ArrayList;
import java.util.List;

public class ListTaskAdapter extends RecyclerView.Adapter<ListTaskAdapter.ListTaskViewHolder> {
    private final ArrayList<Task> taskList = new ArrayList<>();

    public void setAdapter(List<Task> task) {
        if (task == null) return;
        taskList.clear();
        taskList.addAll(task);
    }

    private OnItemClickCallbackTask onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallbackTask onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    @NonNull
    @Override
    public ListTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListTaskBinding binding = ItemListTaskBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ListTaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListTaskViewHolder holder, int position) {
        holder.bind(taskList.get(position));
        holder.itemView.setOnClickListener(view -> onItemClickCallback.onItemClicked(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class ListTaskViewHolder extends RecyclerView.ViewHolder {
        ItemListTaskBinding binding;

        ListTaskViewHolder(ItemListTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(Task task) {
            Glide.with(itemView.getContext())
                    .load(task.getIcon())
                    .into(binding.ivLogoTask);
            binding.tvTitleTask.setText(itemView.getResources().getString(task.getTitle()));
            binding.tvDescTask.setText(itemView.getResources().getString(task.getDesc()));
        }
    }

    public interface OnItemClickCallbackTask{
        void onItemClicked(int position);
    }

}
