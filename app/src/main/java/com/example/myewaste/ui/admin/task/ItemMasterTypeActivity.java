package com.example.myewaste.ui.admin.task;

import static com.example.myewaste.utils.Constant.EXTRAS_ACTION_MODE;
import static com.example.myewaste.utils.Constant.ITEM_MASTER;
import static com.example.myewaste.utils.Constant.ITEM_TYPE;
import static com.example.myewaste.utils.Constant.MODE_ADD;
import static com.example.myewaste.utils.Constant.NO_ITEM_MASTER;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myewaste.R;
import com.example.myewaste.adapter.ListItemTypeParentAdapter;
import com.example.myewaste.databinding.ActivityItemMasterBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.ListItemType;
import com.example.myewaste.model.item.ItemMaster;
import com.example.myewaste.model.item.ItemType;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class ItemMasterTypeActivity extends AppCompatActivity {

    private ProgressDialog loading;
    private DatabaseReference databaseReference;

    private ArrayList<ListItemType> listItemTypesAll;
    private ArrayList<ItemType> listItemType;
    private ListItemTypeParentAdapter adapter;

    private ActivityItemMasterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemMasterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();
        MainToolbarBinding bindingToolbar = binding.mainToolbar;
        bindingToolbar.tvTitleBar.setText(R.string.item_type);

        bindingToolbar.btnBack.setOnClickListener(v -> onBackPressed());
        binding.ivPlaceholderEmpty.setImageResource(R.drawable.ic_placeholder_empty_type);
        binding.tvTitle.setText(R.string.title_type_item_empty);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        fetchDataItemMaster();

        listItemTypesAll = new ArrayList<>();
        listItemType = new ArrayList<>();
        binding.rvItemMaster.setHasFixedSize(true);
        adapter = new ListItemTypeParentAdapter();

        setRecyclerViewItemMaster();

        binding.fbAddTypeItem.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddUpdateItemTypeActivity.class);
            intent.putExtra(EXTRAS_ACTION_MODE, MODE_ADD);
            startActivity(intent);
        });
    }

    private void setRecyclerViewItemMaster() {
        binding.rvItemMaster.setLayoutManager(new LinearLayoutManager(this));
        binding.rvItemMaster.setAdapter(adapter);
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
        DatabaseReference childItemMaster = databaseReference.child(ITEM_MASTER);
        childItemMaster.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ItemMaster itemMaster = dataSnapshot.getValue(ItemMaster.class);
                    if (itemMaster != null) {
                        fetchDataItemType(itemMaster.getNo_item_master(), itemMaster.getName());
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loading.dismiss();
            }
        });
        loading.dismiss();
    }

    private void fetchDataItemType(String noItemMaster, String nameItemMaster) {
        if (noItemMaster != null) {
            Query queryItemType = databaseReference.child(ITEM_TYPE).orderByChild(NO_ITEM_MASTER).equalTo(noItemMaster);
            queryItemType.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    listItemType = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ItemType itemTypeResult = dataSnapshot.getValue(ItemType.class);
                        if (itemTypeResult != null) {
                            listItemType.add(itemTypeResult);
                        }
                    }
                    listItemTypesAll.add(new ListItemType(nameItemMaster, listItemType));
                    adapter.setAdapter(listItemTypesAll);
                    showPlaceholderOrRecyclerView(listItemTypesAll.size() > 0);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}