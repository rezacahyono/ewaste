package com.example.myewaste.ui.admin.task;

import static com.example.myewaste.utils.Constant.DEFAULT_NO_UNIT_ITEM;
import static com.example.myewaste.utils.Constant.MODE_ADD;
import static com.example.myewaste.utils.Constant.MODE_UPDATE;
import static com.example.myewaste.utils.Constant.UNIT_ITEM;
import static com.example.myewaste.utils.Utils.increseNumber;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myewaste.R;
import com.example.myewaste.adapter.ListUnitItemAdapter;
import com.example.myewaste.databinding.ActivityItemMasterBinding;
import com.example.myewaste.databinding.DialogAddUnitItemBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.item.UnitItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


public class ItemUnitActivity extends AppCompatActivity {

    private String mode;
    private DatabaseReference databaseReference;
    private ArrayList<UnitItem> listUnit;
    private ListUnitItemAdapter adapter;
    private ProgressDialog loading;

    private String newNoUnitItem = "";

    private UnitItem unitItems;
    private ActivityItemMasterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemMasterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();
        MainToolbarBinding bindingToolbar = binding.mainToolbar;
        bindingToolbar.tvTitleBar.setText(R.string.unit_item);

        bindingToolbar.btnBack.setOnClickListener(v -> onBackPressed());
        binding.ivPlaceholderEmpty.setImageResource(R.drawable.ic_placeholder_empty_unit);
        binding.tvTitle.setText(R.string.title_unit_type);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        fetchDataUnitItem();

        listUnit = new ArrayList<>();
        adapter = new ListUnitItemAdapter();
        binding.rvItemMaster.setHasFixedSize(true);
        unitItems = new UnitItem();

        binding.fbAddTypeItem.setOnClickListener(v -> {
            mode = MODE_ADD;
            alertAddUpdateUnitItem();
        });

        setRecyclerViewUnitItem();
    }

    private void setRecyclerViewUnitItem() {
        binding.rvItemMaster.setLayoutManager(new LinearLayoutManager(this));
        binding.rvItemMaster.setAdapter(adapter);
        adapter.setOnItemClickCallback(new ListUnitItemAdapter.OnItemClickCallbackUnitItem() {
            @Override
            public void onClickedRemove(UnitItem unitItem) {
                alertDialogDelete(unitItem);
            }

            @Override
            public void onClickedUpdate(UnitItem unitItem) {
                mode = MODE_UPDATE;
                unitItems = unitItem;
                alertAddUpdateUnitItem();
            }
        });
    }

    private void alertAddUpdateUnitItem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogAddUnitItemBinding bindingDialog = DialogAddUnitItemBinding.inflate(getLayoutInflater());
        View viewDialog = bindingDialog.getRoot();

        if (mode.equals(MODE_UPDATE)) {
            bindingDialog.tvTitle.setText(R.string.update_unit_item);
            bindingDialog.btnSave.setText(R.string.update);
            bindingDialog.edtInputUnitItem.setText(unitItems.getName());
        } else {
            bindingDialog.tvTitle.setText(R.string.add_unit_item);
            bindingDialog.btnSave.setText(R.string.save);
            bindingDialog.edtInputUnitItem.setText("");
        }

        builder.setView(viewDialog);
        AlertDialog dialog = builder.create();
        dialog.show();
        bindingDialog.btnCancel.setOnClickListener(v -> dialog.dismiss());
        bindingDialog.btnSave.setOnClickListener(v -> {
            String nameItem = Objects.requireNonNull(bindingDialog.edtInputUnitItem.getText()).toString().trim();
            if (!nameItem.isEmpty()) {
                if (!mode.equals(MODE_UPDATE)) {
                    unitItems.setNo_unit_item(newNoUnitItem);
                }
                unitItems.setName(nameItem);
                submitUnitItem(unitItems);
            }
            dialog.dismiss();
        });
    }

    private void actionDelete(UnitItem unitItem) {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        databaseReference.child(UNIT_ITEM).child(unitItem.getNo_unit_item()).removeValue()
                .addOnSuccessListener(unused -> Toast.makeText(ItemUnitActivity.this, getResources().getString(R.string.action_delete_unit_message, unitItem.getName(), "berhasil"), Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ItemUnitActivity.this, getResources().getString(R.string.action_delete_unit_message, unitItem.getName(), "gagal"), Toast.LENGTH_SHORT).show());
        loading.dismiss();
    }


    private void alertDialogDelete(UnitItem unitItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.delete));
        builder.setMessage(getResources().getString(R.string.message_delete_unit_item, unitItem.getName()));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.yes), (dialogInterface, i) -> {
            actionDelete(unitItem);
            fetchDataUnitItem();
        });
        builder.setNegativeButton(getResources().getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
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


    private void fetchDataUnitItem() {
        databaseReference.child(UNIT_ITEM).get().addOnCompleteListener(task -> {
            String noUnit = DEFAULT_NO_UNIT_ITEM;
            if (task.isSuccessful()) {
                DataSnapshot result = task.getResult();
                UnitItem unitItem;
                if (result != null) {
                    for (DataSnapshot dataSnapshot : result.getChildren()) {
                        noUnit = dataSnapshot.getKey();
                        unitItem = dataSnapshot.getValue(UnitItem.class);
                        assert unitItem != null;
                        if (noUnit != null) {
                            noUnit = increseNumber(noUnit);
                            listUnit.add(unitItem);
                        }
                    }
                }
            }
            newNoUnitItem = noUnit;
            adapter.setAdapter(listUnit);
            showPlaceholderOrRecyclerView(listUnit.size() > 0);
            listUnit.clear();
        });
    }

    private void submitUnitItem(UnitItem unitItem) {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        databaseReference.child(UNIT_ITEM).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference unitReference = databaseReference.child(UNIT_ITEM).child(unitItem.getNo_unit_item());
                unitReference.setValue(unitItem)
                        .addOnSuccessListener(unused -> Toast.makeText(ItemUnitActivity.this, getResources().getString(R.string.submit_success, getResources().getString(R.string.unit)), Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(ItemUnitActivity.this, getResources().getString(R.string.submit_failure, getResources().getString(R.string.unit)), Toast.LENGTH_SHORT).show());
                loading.dismiss();
                fetchDataUnitItem();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loading.dismiss();
            }
        });
    }
}
