package com.example.myewaste.ui.admin.task;

import static com.example.myewaste.utils.Constant.DEFAULT_NO_ITEM_TYPE;
import static com.example.myewaste.utils.Constant.EXTRAS_ACTION_MODE;
import static com.example.myewaste.utils.Constant.EXTRAS_ITEM_TYPE;
import static com.example.myewaste.utils.Constant.ITEM_MASTER;
import static com.example.myewaste.utils.Constant.ITEM_TYPE;
import static com.example.myewaste.utils.Constant.MODE_ADD;
import static com.example.myewaste.utils.Constant.MODE_UPDATE;
import static com.example.myewaste.utils.Constant.NAME;
import static com.example.myewaste.utils.Constant.NO_ITEM_MASTER;
import static com.example.myewaste.utils.Constant.NO_UNI_ITEM;
import static com.example.myewaste.utils.Constant.UNIT_ITEM;
import static com.example.myewaste.utils.Utils.increseNumber;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myewaste.R;
import com.example.myewaste.databinding.ActivityAddUpdateItemTypeBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.item.ItemMaster;
import com.example.myewaste.model.item.ItemType;
import com.example.myewaste.model.item.UnitItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class AddUpdateItemTypeActivity extends AppCompatActivity {

    private String mode = "";
    private ProgressDialog loading;
    private final ArrayList<String> listNameItem = new ArrayList<>();
    private final ArrayList<String> listNameUnit = new ArrayList<>();

    private UnitItem unitItem = new UnitItem();
    private ItemMaster itemMaster = new ItemMaster();
    private final ItemType itemType = new ItemType();

    private ArrayAdapter<String> adapterDropdownItemMaster;
    private ArrayAdapter<String> adapterDropdownUniItem;

    private DatabaseReference databaseReference;

    private ActivityAddUpdateItemTypeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddUpdateItemTypeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();
        MainToolbarBinding bindingToolbar = binding.mainToolbar;
        bindingToolbar.tvTitleBar.setText(R.string.add_item_type);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        fetchDataItemMasterAndUnitItemForSpinner();

        bindingToolbar.btnBack.setOnClickListener(v -> onBackPressed());

        mode = getIntent().getStringExtra(EXTRAS_ACTION_MODE);
        if (mode.equals(MODE_UPDATE)) {
            bindingToolbar.tvTitleBar.setText(R.string.update_item_type);
            binding.btnSave.setText(R.string.update);
            bindingToolbar.btnTrash.setVisibility(View.VISIBLE);
        }

        if (getIntent().getParcelableExtra(EXTRAS_ITEM_TYPE) != null) {
            ItemType extrasItemType = getIntent().getParcelableExtra(EXTRAS_ITEM_TYPE);

            fetchDataItemMasterByNoItemMaster(extrasItemType.getNo_item_master());
            fetchDataUnitItemByNoUnit(extrasItemType.getNo_unit_item());

            binding.edtPriceItemType.setText(String.valueOf(extrasItemType.getPrice()));
            binding.edtNameItemType.setText(extrasItemType.getName());

            itemType.setName(extrasItemType.getName());
            itemType.setNo_item_type(extrasItemType.getNo_item_type());
        }

        binding.tvDdItemMaster.setOnItemClickListener((adapterView, view, i, l) -> {
            String nameItem = adapterView.getItemAtPosition(i).toString();
            fetchDataItemMasterByNameItem(nameItem);
        });

        binding.tvDdUnitItem.setOnItemClickListener((adapterView, view, i, l) -> {
            String nameUnit = adapterView.getItemAtPosition(i).toString();
            fetchDataUniItem(nameUnit);
        });


        binding.btnCancel.setOnClickListener(v -> onBackPressed());

        binding.btnSave.setOnClickListener(v -> {
            String nameType = Objects.requireNonNull(binding.edtNameItemType.getText()).toString();
            String price = Objects.requireNonNull(binding.edtPriceItemType.getText()).toString();
            boolean valid = true;
            if (nameType.isEmpty()) {
                binding.edtNameItemType.setError(getResources().getString(R.string.input_can_not_be_empty, "nama jenis barnag"));
                valid = false;
            }

            if (price.isEmpty()) {
                binding.edtPriceItemType.setError(getResources().getString(R.string.input_can_not_be_empty, "Harga"));
                valid = false;
            } else if (itemMaster.getNo_item_master() == null) {
                Toast.makeText(this, getResources().getString(R.string.input_can_not_be_empty, "Nama barang"), Toast.LENGTH_SHORT).show();
                valid = false;
            } else if (unitItem.getNo_unit_item() == null) {
                Toast.makeText(this, getResources().getString(R.string.input_can_not_be_empty, "Satuan barang"), Toast.LENGTH_SHORT).show();
                valid = false;
            }

            if (valid) {
                double prices = Double.parseDouble(price);
                itemType.setNo_item_master(itemMaster.getNo_item_master());
                itemType.setNo_unit_item(unitItem.getNo_unit_item());
                itemType.setName(nameType);
                itemType.setPrice(prices);
                checkHasSimiliar(itemType);
            }
        });

        bindingToolbar.btnTrash.setOnClickListener(v -> alertDelete(itemType));

    }

    private void alertDelete(ItemType itemType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddUpdateItemTypeActivity.this);
        builder.setTitle(getResources().getString(R.string.delete));
        builder.setMessage(getResources().getString(R.string.message_delete_item_master, itemType.getName()));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.yes), (dialogInterface, i) -> actionDelete(itemType));
        builder.setNegativeButton(getResources().getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void actionDelete(ItemType itemType) {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        databaseReference.child(ITEM_TYPE).child(itemType.getNo_item_type()).removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, getResources().getString(R.string.action_delete_item_message, itemType.getName(), "berhasil"), Toast.LENGTH_SHORT).show();
                    navigateToItemType();
                })
                .addOnFailureListener(e -> Toast.makeText(this, getResources().getString(R.string.action_delete_item_message, itemType.getName(), "gagal"), Toast.LENGTH_SHORT).show());
        loading.dismiss();
    }


    private void setImageLoad(String image) {
        Glide.with(this)
                .load(image)
                .placeholder(R.color.green_20)
                .error(R.color.green_20)
                .into(binding.ivPhotoItem);
    }

    private void fetchDataItemMasterByNoItemMaster(String noItemMaster) {
        if (noItemMaster != null) {
            Query queuryItemMasterItemByNoItem = databaseReference.child(ITEM_MASTER).orderByChild(NO_ITEM_MASTER).equalTo(noItemMaster);
            queuryItemMasterItemByNoItem.get().addOnCompleteListener(task -> {
                DataSnapshot result = task.getResult();
                if (result != null) {
                    for (DataSnapshot dataSnapshot : result.getChildren()) {
                        ItemMaster itemMasterResult = dataSnapshot.getValue(ItemMaster.class);
                        if (itemMasterResult != null) {
                            itemMaster = itemMasterResult;
                            binding.tvDdItemMaster.setText(itemMasterResult.getName());
                            setImageLoad(itemMasterResult.getPhoto());
                        }
                    }
                }
            });
        }
    }

    private void fetchDataItemMasterByNameItem(String nameItem) {
        if (nameItem != null) {
            Query queryItemMasterByNameItem = databaseReference.child(ITEM_MASTER).orderByChild(NAME).equalTo(nameItem);
            queryItemMasterByNameItem.get().addOnCompleteListener(task -> {
                DataSnapshot result = task.getResult();
                if (result != null) {
                    for (DataSnapshot dataSnapshot : result.getChildren()) {
                        ItemMaster itemMasterResult = dataSnapshot.getValue(ItemMaster.class);
                        if (itemMasterResult != null) {
                            itemMaster = itemMasterResult;
                            setImageLoad(itemMasterResult.getPhoto());
                        }
                    }
                }
            });
        }

    }

    private void fetchDataUnitItemByNoUnit(String noUnit) {
        if (noUnit != null) {
            Query queryUnitItemByNoUnit = databaseReference.child(UNIT_ITEM).orderByChild(NO_UNI_ITEM).equalTo(noUnit);
            queryUnitItemByNoUnit.get().addOnCompleteListener(task -> {
                DataSnapshot result = task.getResult();
                if (result != null) {
                    for (DataSnapshot dataSnapshot : result.getChildren()) {
                        UnitItem unitItemResult = dataSnapshot.getValue(UnitItem.class);
                        if (unitItemResult != null) {
                            binding.tvDdUnitItem.setText(unitItemResult.getName());
                            unitItem = unitItemResult;
                        }
                    }
                }
            });
        }
    }

    private void fetchDataUniItem(String nameUnit) {
        if (nameUnit != null) {
            Query queryUnitItemByNameUnit = databaseReference.child(UNIT_ITEM).orderByChild(NAME).equalTo(nameUnit);
            queryUnitItemByNameUnit.get().addOnCompleteListener(task -> {
                DataSnapshot result = task.getResult();
                if (result != null) {
                    for (DataSnapshot dataSnapshot : result.getChildren()) {
                        UnitItem unitItemResult = dataSnapshot.getValue(UnitItem.class);
                        if (unitItemResult != null) {
                            unitItem = unitItemResult;
                        }
                    }
                }
            });
        }
    }


    private void fetchDataItemMasterAndUnitItemForSpinner() {
        DatabaseReference childItemMaster = databaseReference.child(ITEM_MASTER);
        DatabaseReference childUnitItem = databaseReference.child(UNIT_ITEM);
        childItemMaster.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ItemMaster itemMaster = dataSnapshot.getValue(ItemMaster.class);
                    if (itemMaster != null) {
                        listNameItem.add(itemMaster.getName());
                    }
                }
                adapterDropdownItemMaster = new ArrayAdapter<>(AddUpdateItemTypeActivity.this, R.layout.dropdown_item, listNameItem);
                binding.tvDdItemMaster.setAdapter(adapterDropdownItemMaster);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        childUnitItem.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UnitItem unitItem = dataSnapshot.getValue(UnitItem.class);
                    if (unitItem != null) {
                        listNameUnit.add(unitItem.getName());
                    }
                }
                adapterDropdownUniItem = new ArrayAdapter<>(AddUpdateItemTypeActivity.this, R.layout.dropdown_item, listNameUnit);
                binding.tvDdUnitItem.setAdapter(adapterDropdownUniItem);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void checkHasSimiliar(ItemType itemType) {
        databaseReference.child(ITEM_TYPE).get().addOnCompleteListener(task -> {
            String noItemType = DEFAULT_NO_ITEM_TYPE;
            boolean hasSimiliar = false;
            if (task.isSuccessful()) {
                DataSnapshot result = task.getResult();
                assert result != null;
                for (DataSnapshot dataSnapshot : result.getChildren()) {
                    ItemType itemTypeResult = dataSnapshot.getValue(ItemType.class);
                    if (itemTypeResult != null) {
                        noItemType = increseNumber(itemTypeResult.getNo_item_type());
                        if (itemTypeResult.getName().equalsIgnoreCase(itemType.getName())
                                && !itemTypeResult.getNo_item_type().equalsIgnoreCase(itemType.getNo_item_type())) {
                            hasSimiliar = true;
                        }
                    }
                }
                if (mode.equals(MODE_ADD)) {
                    itemType.setNo_item_type(noItemType);
                }
                if (!hasSimiliar) {
                    sumbitItemType(itemType);
                } else {
                    Toast.makeText(AddUpdateItemTypeActivity.this, getResources().getString(R.string.hasSimiliar, itemType.getName()), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void sumbitItemType(ItemType itemType) {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        if (itemType != null) {
            databaseReference
                    .child(ITEM_TYPE)
                    .child(itemType.getNo_item_type())
                    .setValue(itemType)
                    .addOnSuccessListener(unused -> {
                        if (mode.equals(MODE_ADD)) {
                            Toast.makeText(AddUpdateItemTypeActivity.this, getResources().getString(R.string.submit_success, getResources().getString(R.string.item_type)), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AddUpdateItemTypeActivity.this, getResources().getString(R.string.update_success, getResources().getString(R.string.item_type)), Toast.LENGTH_SHORT).show();
                        }
                        loading.dismiss();
                        navigateToItemType();
                    })
                    .addOnFailureListener(e -> {
                        if (mode.equals(MODE_ADD)) {
                            Toast.makeText(AddUpdateItemTypeActivity.this, getResources().getString(R.string.submit_failure, getResources().getString(R.string.item_type)), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AddUpdateItemTypeActivity.this, getResources().getString(R.string.update_failure, getResources().getString(R.string.item_type)), Toast.LENGTH_SHORT).show();
                        }
                        loading.dismiss();
                    });
        }

    }

    private void navigateToItemType() {
        Intent intent = new Intent(this, ItemMasterTypeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
