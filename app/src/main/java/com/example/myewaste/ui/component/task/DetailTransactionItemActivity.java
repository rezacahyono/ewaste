package com.example.myewaste.ui.component.task;

import static com.example.myewaste.utils.Constant.EXTRAS_FROM;
import static com.example.myewaste.utils.Constant.EXTRAS_ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.FROM_DETAIL;
import static com.example.myewaste.utils.Constant.ITEM_MASTER;
import static com.example.myewaste.utils.Constant.ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.ITEM_TYPE;
import static com.example.myewaste.utils.Constant.NASABAH;
import static com.example.myewaste.utils.Constant.NO_ITEM_MASTER;
import static com.example.myewaste.utils.Constant.NO_ITEM_TYPE;
import static com.example.myewaste.utils.Constant.NO_REGIS;
import static com.example.myewaste.utils.Constant.NO_SALDO_TRANSACTION;
import static com.example.myewaste.utils.Constant.NO_UNI_ITEM;
import static com.example.myewaste.utils.Constant.SALDO_TRANSACTION;
import static com.example.myewaste.utils.Constant.UNIT_ITEM;
import static com.example.myewaste.utils.Constant.USER_DATA;
import static com.example.myewaste.utils.Utils.convertDate;
import static com.example.myewaste.utils.Utils.convertToRupiah;
import static com.example.myewaste.utils.Utils.getRegisterCode;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myewaste.R;
import com.example.myewaste.adapter.ListItemDetailTransactionAdapter;
import com.example.myewaste.databinding.ActivityDetailTransactionItemBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.item.Item;
import com.example.myewaste.model.item.ItemMaster;
import com.example.myewaste.model.item.ItemTransaction;
import com.example.myewaste.model.item.ItemType;
import com.example.myewaste.model.item.UnitItem;
import com.example.myewaste.model.saldo.SaldoTransaction;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.model.utils.ListItem;
import com.example.myewaste.pref.SessionManagement;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class DetailTransactionItemActivity extends AppCompatActivity {

    private ProgressDialog loading;
    private ItemTransaction itemTransaction = new ItemTransaction();
    private final ArrayList<ListItem> listItemsForAdapter = new ArrayList<>();
    private ListItem listItems = new ListItem();
    private DatabaseReference databaseReference;

    private String nameNasabah = "";
    private String nameTeller = "";

    private ListItemDetailTransactionAdapter adapter;
    private ActivityDetailTransactionItemBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailTransactionItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();
        MainToolbarBinding bindingToolbar = binding.mainToolbar;
        bindingToolbar.tvTitleBar.setText(R.string.details_transaction);
        bindingToolbar.btnBack.setOnClickListener(view -> onBackPressed());

        databaseReference = FirebaseDatabase.getInstance().getReference();
        SessionManagement sessionManagement = new SessionManagement(this);

        adapter = new ListItemDetailTransactionAdapter();
        binding.rvItem.setHasFixedSize(true);

        itemTransaction = getIntent().getParcelableExtra(EXTRAS_ITEM_TRANSACTION);
        if (itemTransaction != null) {
            fetchUserData(itemTransaction.getNo_nasabah(), itemTransaction.getNo_teller());
            fetchDataSaldoTransaction(itemTransaction.getNo_saldo_transaction());
            fetchDataTypeItem(itemTransaction.getItem_list());
        }

        if (!getRegisterCode(sessionManagement.getUserSession()).equalsIgnoreCase(NASABAH)) {
            bindingToolbar.btnTrash.setVisibility(View.VISIBLE);
            binding.btnUpdateTransaction.setVisibility(View.VISIBLE);
        } else {
            bindingToolbar.btnTrash.setVisibility(View.GONE);
            binding.btnUpdateTransaction.setVisibility(View.GONE);
        }

        bindingToolbar.btnTrash.setOnClickListener(v -> alertDelete(itemTransaction));

        binding.btnUpdateTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddUpdateTransactionItemActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(EXTRAS_FROM, FROM_DETAIL);
            intent.putExtra(EXTRAS_ITEM_TRANSACTION, itemTransaction);
            startActivity(intent);
        });

        fetchDataItemTransaction(itemTransaction.getNo_item_transaction());

        setDataItemTransaction(itemTransaction);
        setRecyclerViewItemListDetail();

    }

    private void setRecyclerViewItemListDetail() {
        binding.rvItem.setLayoutManager(new LinearLayoutManager(this));
        binding.rvItem.setAdapter(adapter);
    }

    private void setDataItemTransaction(ItemTransaction itemTransaction) {
        binding.tvDate.setText(convertDate(itemTransaction.getDate()));
        binding.tvNoTransaction.setText(itemTransaction.getNo_item_transaction());
    }

    private void alertDelete(ItemTransaction itemTransaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailTransactionItemActivity.this);
        builder.setTitle(getResources().getString(R.string.delete));
        builder.setMessage(getResources().getString(R.string.message_delete_transaction));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.yes), (dialogInterface, i) -> {
//            actionDelete(itemMaster);
//            Log.d("TAG", "alertDelete: " + itemTransaction.getNo_item_transaction());
        });
        builder.setNegativeButton(getResources().getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void actionDeleteTransaction(ItemTransaction itemTransaction) {
        if (itemTransaction != null) {
            databaseReference.child(ITEM_TRANSACTION).child(itemTransaction.getNo_item_transaction()).removeValue()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, getResources().getString(R.string.message_success_delete_transaction), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, getResources().getString(R.string.message_failure_delete_transaction), Toast.LENGTH_SHORT).show());
        }
    }


    private void fetchDataSaldoTransaction(String noSaldoTransaction) {
        databaseReference.child(SALDO_TRANSACTION).orderByChild(NO_SALDO_TRANSACTION).equalTo(noSaldoTransaction).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SaldoTransaction saldoTransactionResult = dataSnapshot.getValue(SaldoTransaction.class);
                    if (saldoTransactionResult != null) {
                        binding.tvTotalCutsTransaction.setText(convertToRupiah((int) saldoTransactionResult.getCuts_transaction()));
                        binding.tvTotalIncome.setText(convertToRupiah((int) saldoTransactionResult.getTotal_income()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchDataItemTransaction(String noItemTransaction) {
        databaseReference.child(ITEM_TRANSACTION).child(noItemTransaction).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ItemTransaction itemTransactionResult = snapshot.getValue(ItemTransaction.class);
                if (itemTransactionResult != null) {
                    itemTransaction = itemTransactionResult;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void fetchUserData(String noNasabah, String noTeller) {
        databaseReference.child(USER_DATA).orderByChild(NO_REGIS).equalTo(noNasabah).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    nameNasabah = Objects.requireNonNull(dataSnapshot.getValue(UserData.class)).getName();
                }
                binding.tvNameNasabah.setText(nameNasabah);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        databaseReference.child(USER_DATA).orderByChild(NO_REGIS).equalTo(noTeller).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    nameTeller = Objects.requireNonNull(dataSnapshot.getValue(UserData.class)).getName();
                }
                binding.tvNameTeller.setText(nameTeller);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchDataTypeItem(ArrayList<Item> items) {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        for (Item item : items) {
            databaseReference.child(ITEM_TYPE).orderByChild(NO_ITEM_TYPE).equalTo(item.getNo_type_item()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshotItemType : snapshot.getChildren()) {
                        ItemType itemTypeResult = dataSnapshotItemType.getValue(ItemType.class);
                        if (itemTypeResult != null) {
                            databaseReference.child(ITEM_MASTER).orderByChild(NO_ITEM_MASTER).equalTo(itemTypeResult.getNo_item_master()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshotItemMaster : snapshot.getChildren()) {
                                        ItemMaster itemMasterResult = dataSnapshotItemMaster.getValue(ItemMaster.class);
                                        if (itemMasterResult != null) {
                                            databaseReference.child(UNIT_ITEM).orderByChild(NO_UNI_ITEM).equalTo(itemTypeResult.getNo_unit_item()).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    listItems = new ListItem();
                                                    for (DataSnapshot dataSnapshotUnitItem : snapshot.getChildren()) {
                                                        UnitItem unitItemResult = dataSnapshotUnitItem.getValue(UnitItem.class);
                                                        if (unitItemResult != null) {
                                                            listItems.setNameItem(itemMasterResult.getName());
                                                            listItems.setNameItemType(itemTypeResult.getName());
                                                            listItems.setPrice((int) (itemTypeResult.getPrice() * item.getTotal()));
                                                            listItems.setTotal(item.getTotal());
                                                            listItems.setNameUnit(unitItemResult.getName());
                                                        }
                                                    }
                                                    listItemsForAdapter.add(listItems);
                                                    adapter.setAdapter(listItemsForAdapter);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                    loading.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    loading.dismiss();
                }
            });
        }
    }
}
