package com.example.myewaste.ui.admin.task;


import static com.example.myewaste.utils.Constant.EXTRAS_ACTION_MODE;
import static com.example.myewaste.utils.Constant.EXTRAS_ITEM_MASTER;
import static com.example.myewaste.utils.Constant.IMAGE_FOLDER;
import static com.example.myewaste.utils.Constant.ITEM_FOLDER;
import static com.example.myewaste.utils.Constant.ITEM_MASTER;
import static com.example.myewaste.utils.Constant.MODE_ADD;
import static com.example.myewaste.utils.Constant.MODE_UPDATE;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.myewaste.R;
import com.example.myewaste.adapter.ListItemMasterAdapter;
import com.example.myewaste.databinding.ActivityItemMasterBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.item.ItemMaster;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

public class ItemMasterActivity extends AppCompatActivity {

    private ProgressDialog loading;
    private ArrayList<ItemMaster> listItemMaster;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private ListItemMasterAdapter adapter;

    private ActivityItemMasterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemMasterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();
        MainToolbarBinding bindingToolbar = binding.mainToolbar;
        bindingToolbar.tvTitleBar.setText(R.string.item_master);

        bindingToolbar.btnBack.setOnClickListener(v -> onBackPressed());

        adapter = new ListItemMasterAdapter();
        binding.rvItemMaster.setHasFixedSize(true);
        listItemMaster = new ArrayList<>();

        binding.fbAddTypeItem.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddUpdateItemMasterActivity.class);
            intent.putExtra(EXTRAS_ACTION_MODE, MODE_ADD);
            startActivity(intent);
        });

        databaseReference = FirebaseDatabase.getInstance().getReference(ITEM_MASTER);
        storageReference = FirebaseStorage.getInstance().getReference(IMAGE_FOLDER + ITEM_FOLDER);

        fetchDataItemMaster();
        setRecylerViewItemMaster();
    }

    private void setRecylerViewItemMaster() {
        binding.rvItemMaster.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvItemMaster.setAdapter(adapter);

        adapter.setOnItemClickCallback(new ListItemMasterAdapter.OnItemClickCallbackItemMaster() {
            @Override
            public void onClickedLong(ItemMaster itemMaster) {
                alertDelete(itemMaster);
            }

            @Override
            public void onClickedShort(ItemMaster itemMaster) {
                Intent intent = new Intent(ItemMasterActivity.this, AddUpdateItemMasterActivity.class);
                intent.putExtra(EXTRAS_ACTION_MODE, MODE_UPDATE);
                intent.putExtra(EXTRAS_ITEM_MASTER, itemMaster);
                startActivity(intent);
            }
        });
    }


    private void alertDelete(ItemMaster itemMaster) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ItemMasterActivity.this);
        builder.setTitle(getResources().getString(R.string.delete));
        builder.setMessage(getResources().getString(R.string.message_delete_item_master, itemMaster.getName()));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.yes), (dialogInterface, i) -> {
            actionDelete(itemMaster);
        });
        builder.setNegativeButton(getResources().getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void actionDelete(ItemMaster itemMaster) {
        databaseReference.child(itemMaster.getNo_item_master()).removeValue()
                .addOnSuccessListener(unused -> Toast.makeText(this, getResources().getString(R.string.action_delete_item_message, itemMaster.getName(), "berhasil"), Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, getResources().getString(R.string.action_delete_item_message, itemMaster.getName(), "gagal"), Toast.LENGTH_SHORT).show());
        storageReference.child(itemMaster.getNo_item_master()).delete()
                .addOnSuccessListener(unused -> Toast.makeText(this, getResources().getString(R.string.action_delete_item_message, itemMaster.getName(), "berhasil"), Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, getResources().getString(R.string.action_delete_item_message, itemMaster.getName(), "gagal"), Toast.LENGTH_SHORT).show());
    }

    private void showPlaceholderOrRecyclerView(boolean isShow) {
        if (isShow) {
            binding.rvItemMaster.setVisibility(View.VISIBLE);
            binding.ivPlaceholderEmpty.setVisibility(View.GONE);
            binding.tvTitle.setVisibility(View.GONE);
        } else {
            binding.rvItemMaster.setVisibility(View.GONE);
            binding.ivPlaceholderEmpty.setVisibility(View.VISIBLE);
            binding.tvTitle.setVisibility(View.VISIBLE);
        }
    }

    private void fetchDataItemMaster() {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ItemMaster itemMaster = dataSnapshot.getValue(ItemMaster.class);
                    assert itemMaster != null;
                    listItemMaster.add(itemMaster);
                }

                adapter.setAdapter(listItemMaster);
                showPlaceholderOrRecyclerView(listItemMaster.size() > 0);
                listItemMaster.clear();
                loading.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loading.dismiss();
            }
        });

    }

}