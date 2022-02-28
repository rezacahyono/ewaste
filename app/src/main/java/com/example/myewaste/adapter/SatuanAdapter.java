package com.example.myewaste.adapter;



import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myewaste.R;
import com.example.myewaste.model.item.UnitItem;

import java.util.List;

public class SatuanAdapter extends RecyclerView.Adapter<SatuanAdapter.ViewHolder> {
    Context context;
    List<UnitItem> list;

    OnCallBack onCallBack;

    public void setOnCallBack(OnCallBack onCallBack) {
        this.onCallBack = onCallBack;
    }

    public SatuanAdapter(Context context, List<UnitItem> list) {
        this.context = context;
        this.list = list;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_satuan,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.textSatuan.setText(list.get(position).getName());

        holder.tblHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCallBack.onTblHapus(list.get(position));
            }
        });

        holder.tblEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCallBack.onTblEdit(list.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textSatuan;
        ImageButton tblEdit,tblHapus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textSatuan = itemView.findViewById(R.id.text_satuan);
            tblEdit = itemView.findViewById(R.id.tbl_edit);
            tblHapus = itemView.findViewById(R.id.tbl_hapus);
        }
    }

    public interface OnCallBack{
        void onTblHapus(UnitItem unitItem);
        void onTblEdit(UnitItem unitItem);
    }
}

