package com.example.myewaste.ui.component.task;

import static com.example.myewaste.utils.Constant.ACCEPTED;
import static com.example.myewaste.utils.Constant.DEFAULT_NO_TRANSACTION_ITEM;
import static com.example.myewaste.utils.Constant.DEFAULT_NO_TRANSACTION_SALDO;
import static com.example.myewaste.utils.Constant.DEPOSIT;
import static com.example.myewaste.utils.Constant.EXTRAS_FROM;
import static com.example.myewaste.utils.Constant.EXTRAS_ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Constant.FROM_DETAIL;
import static com.example.myewaste.utils.Constant.ITEM_MASTER;
import static com.example.myewaste.utils.Constant.ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.ITEM_TYPE;
import static com.example.myewaste.utils.Constant.MODE_ADD;
import static com.example.myewaste.utils.Constant.MODE_UPDATE;
import static com.example.myewaste.utils.Constant.NAME;
import static com.example.myewaste.utils.Constant.NONE;
import static com.example.myewaste.utils.Constant.NO_ITEM_MASTER;
import static com.example.myewaste.utils.Constant.NO_ITEM_TYPE;
import static com.example.myewaste.utils.Constant.NO_REGIS;
import static com.example.myewaste.utils.Constant.NO_SALDO_TRANSACTION;
import static com.example.myewaste.utils.Constant.NO_UNI_ITEM;
import static com.example.myewaste.utils.Constant.SALDO_NASABAH;
import static com.example.myewaste.utils.Constant.SALDO_TRANSACTION;
import static com.example.myewaste.utils.Constant.UNIT_ITEM;
import static com.example.myewaste.utils.Constant.USER_DATA;
import static com.example.myewaste.utils.Utils.convertToRupiah;
import static com.example.myewaste.utils.Utils.increseNumber;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.example.myewaste.R;
import com.example.myewaste.adapter.ListItemAdapter;
import com.example.myewaste.databinding.ActivityAddInputTransactionScalesBinding;
import com.example.myewaste.databinding.BottomSheetInputItemScaleBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.item.Item;
import com.example.myewaste.model.item.ItemMaster;
import com.example.myewaste.model.item.ItemTransaction;
import com.example.myewaste.model.item.ItemType;
import com.example.myewaste.model.item.UnitItem;
import com.example.myewaste.model.saldo.Saldo;
import com.example.myewaste.model.saldo.SaldoTransaction;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.model.utils.ListItem;
import com.example.myewaste.pref.SessionManagement;
import com.example.myewaste.ui.nasabah.NasabahActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class AddUpdateTransactionItemActivity extends AppCompatActivity {

    private ProgressDialog loading;
    private String mode = "";
    private String from = "";
    private UserData userData;
    private ItemTransaction itemTransaction;
    private SaldoTransaction saldoTransaction;
    private UnitItem unitItem;
    private ItemMaster itemMaster;
    private ItemType itemType;
    private Saldo saldo;
    private ListItem listItemSheet = new ListItem();
    private ArrayList<Item> listItem = new ArrayList<>();
    private ArrayList<ListItem> listItemsForAdapter = new ArrayList<>();
    private final ArrayList<ItemType> listItemType = new ArrayList<>();

    private int lastPriceTransactionItem = 0;
    private int pricePerItem;
    private int priceTotal = 0;
    private double totalItem;
    private int indexOfChangeList;

    private DatabaseReference databaseReference;
    private SessionManagement sessionManagement;

    private ListItemAdapter adapter;
    private BottomSheetDialog bottomSheetDialog;
    private BottomSheetInputItemScaleBinding bindingBottomSheet;
    private ActivityAddInputTransactionScalesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddInputTransactionScalesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();
        MainToolbarBinding bindingToolbar = binding.mainToolbar;
        bindingToolbar.tvTitleBar.setText(R.string.input_ballance);
        bindingToolbar.btnBack.setOnClickListener(view -> onBackPressed());

        databaseReference = FirebaseDatabase.getInstance().getReference();
        sessionManagement = new SessionManagement(this);

        adapter = new ListItemAdapter();
        binding.rvItem.setHasFixedSize(true);

        itemTransaction = new ItemTransaction();
        saldoTransaction = new SaldoTransaction();
        saldo = new Saldo();

        if (getIntent().getParcelableExtra(EXTRAS_ITEM_TRANSACTION) != null) {
            itemTransaction = getIntent().getParcelableExtra(EXTRAS_ITEM_TRANSACTION);
            listItem = itemTransaction.getItem_list();
            lastPriceTransactionItem = (int) itemTransaction.getTotal_price();
            priceTotal = (int) itemTransaction.getTotal_price();
            fetchDataUserData(itemTransaction.getNo_nasabah());
            fetchSaldoNasabah(itemTransaction.getNo_nasabah());
            fetchDataTransactionSaldoByNoTransactionSaldo(itemTransaction.getNo_saldo_transaction());
            loadDataItemTransaction(itemTransaction);
        } else {
            lastPriceTransactionItem = 0;
            generateNoTransactionItemAndNoTransactionSaldo();
        }

        if (getIntent().getParcelableExtra(EXTRAS_USER_DATA) != null) {
            userData = getIntent().getParcelableExtra(EXTRAS_USER_DATA);
            loadDataUser(userData);
            fetchSaldoNasabah(userData.getNo_regis());
        }

        if (getIntent().getStringExtra(EXTRAS_FROM) != null) {
            from = getIntent().getStringExtra(EXTRAS_FROM);
        }

        if (from.equals(FROM_DETAIL)) {
            binding.btnSave.setText(R.string.update);
        }

        binding.fbAddTypeItem.setOnClickListener(v -> {
            mode = MODE_ADD;
            showAddTransactionItemBottomSheet();
        });


        binding.btnSave.setOnClickListener(v -> {
            itemTransaction.setNo_nasabah(userData.getNo_regis());
            itemTransaction.setNo_teller(sessionManagement.getUserSession());
            itemTransaction.setNo_saldo_transaction(saldoTransaction.getNo_saldo_transaction());
            itemTransaction.setItem_list(listItem);
            itemTransaction.setTotal_price(priceTotal);
            saldoTransaction.setNo_nasabah(userData.getNo_regis());
            saldoTransaction.setType_transaction(DEPOSIT);
            saldoTransaction.setNo_teller(sessionManagement.getUserSession());
            saldoTransaction.setStatus(ACCEPTED);
            saldoTransaction.setTotal_income(priceTotal);
            saldoTransaction.setCuts_transaction(0.0);
            if (!from.equals(FROM_DETAIL)){
                Date date = new Date();
                itemTransaction.setDate(date.getTime());
                saldoTransaction.setDate(date.getTime());
            }
            saldo.setSaldo(saldo.getSaldo()-lastPriceTransactionItem);
            saldo.setSaldo(priceTotal+saldo.getSaldo());
            onSumbitTransactionItem(itemTransaction, saldoTransaction, saldo);
            lastPriceTransactionItem = (int) itemTransaction.getTotal_price();
            binding.btnSave.setVisibility(View.GONE);
        });

        setRecyclerView();

    }


    private void loadDataItemTransaction(ItemTransaction itemTransaction) {
        binding.tvIncome.setText(convertToRupiah((int) itemTransaction.getTotal_price()));
        binding.tvTotal.setText(String.valueOf(itemTransaction.getItem_list().size()));
        fetchDataItem(itemTransaction.getItem_list());
    }

    private void setRecyclerView() {
        binding.rvItem.setLayoutManager(new LinearLayoutManager(this));
        binding.rvItem.setAdapter(adapter);

        adapter.setOnItemClickCallback(new ListItemAdapter.OnItemClickCallbackListItem() {
            @Override
            public void onClickedRemove(int position) {
                alertDeleteItem(position);
            }

            @Override
            public void onClickedListItem(ListItem listItem, int position) {
                mode = MODE_UPDATE;
                listItemSheet = listItem;
                indexOfChangeList = position;
                showAddTransactionItemBottomSheet();
            }
        });
    }


    private void alertDeleteItem(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddUpdateTransactionItemActivity.this);
        builder.setTitle(getResources().getString(R.string.delete));
        builder.setMessage(getResources().getString(R.string.message_delete_item));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.yes), (dialogInterface, i) -> actionDelete(position));
        builder.setNegativeButton(getResources().getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void actionDelete(int position) {
        if (listItemsForAdapter.size() > 0) {
            priceTotal -= listItemsForAdapter.get(position).getPrice();
            listItem.remove(position);
            listItemsForAdapter.remove(position);
            binding.btnSave.setVisibility(View.VISIBLE);
        } else {
            priceTotal = 0;
            listItem = new ArrayList<>();
            listItemsForAdapter = new ArrayList<>();
        }
        binding.tvTotal.setText(String.valueOf(listItem.size()));
        binding.tvIncome.setText(convertToRupiah(priceTotal));
        adapter.setAdapter(listItemsForAdapter);
    }

    private void showPlaceholderOrRecyclerView(boolean isShow) {
        if (isShow) {
            binding.rvItem.setVisibility(View.VISIBLE);
            binding.ivPlaceholderEmpty.setVisibility(View.GONE);
            binding.tvTitle.setVisibility(View.GONE);
        } else {
            binding.rvItem.setVisibility(View.GONE);
            binding.ivPlaceholderEmpty.setVisibility(View.VISIBLE);
            binding.tvTitle.setVisibility(View.VISIBLE);
        }
    }

    private void fetchDataItem(ArrayList<Item> items) {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        if (items != null) {
            for (Item item : items) {
                databaseReference.child(ITEM_TYPE).orderByChild(NO_ITEM_TYPE).equalTo(item.getNo_type_item()).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                                        ListItem listItems = new ListItem();
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
                                                        showPlaceholderOrRecyclerView(listItemsForAdapter.size() > 0);
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


    public void setBottomSheetDialog() {
        bottomSheetDialog = new BottomSheetDialog(this);
        bindingBottomSheet = BottomSheetInputItemScaleBinding.inflate(getLayoutInflater());
        View view = bindingBottomSheet.getRoot();
        bottomSheetDialog.setContentView(view);
    }


    public void showAddTransactionItemBottomSheet() {
        setBottomSheetDialog();
        bottomSheetDialog.show();
        itemType = new ItemType();
        pricePerItem = 0;
        totalItem = 0.0;
        if (mode.equals(MODE_UPDATE)) {
            bindingBottomSheet.tvDdTypeItem.setText(listItemSheet.getNameItemType());
            bindingBottomSheet.tvItemMaster.setText(listItemSheet.getNameItem());
            bindingBottomSheet.tvTotalPrice.setText(convertToRupiah(listItemSheet.getPrice()));
            pricePerItem = (int) (listItemSheet.getPrice() / listItemSheet.getTotal());
            bindingBottomSheet.tvPrice.setText(getResources().getString(R.string.placeholder_price_per_unit, convertToRupiah(pricePerItem), listItemSheet.getNameUnit()));
            bindingBottomSheet.edtTotal.setText(String.valueOf(listItemSheet.getTotal()));

            fetchDataItemTypeByNameItemType(listItemSheet.getNameItemType());
            fetchDataItemMasterByNameItemMaster(listItemSheet.getNameItem());
            fetchDataUnitItemByNameUnitItem(listItemSheet.getNameUnit());

            totalItem = listItemSheet.getTotal();
            bindingBottomSheet.btnAdd.setText(R.string.update);
        }

        fetchDataItemTypeForSpinner();

        bindingBottomSheet.edtTotal.setEnabled(!bindingBottomSheet.tvDdTypeItem.getText().toString().equalsIgnoreCase(NONE));

        bindingBottomSheet.tvDdTypeItem.setOnItemClickListener(((adapterView, view, i, l) -> {
            fetchDataUnitItemByNoUnitItem(listItemType.get(i).getNo_unit_item(), i);
            fetchDataItemMasterByNoItemMaster(listItemType.get(i).getNo_item_master());
            itemType = listItemType.get(i);
            bindingBottomSheet.edtTotal.setEnabled(!bindingBottomSheet.tvDdTypeItem.getText().toString().equalsIgnoreCase(NONE));
            pricePerItem = (int) (itemType.getPrice() * totalItem);
            bindingBottomSheet.tvTotalPrice.setText(convertToRupiah(pricePerItem));
        }));

        bindingBottomSheet.edtTotal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    totalItem = Double.parseDouble(charSequence.toString());
                    pricePerItem = (int) (itemType.getPrice() * Double.parseDouble(charSequence.toString()));
                } else {
                    pricePerItem = 0;
                }
                bindingBottomSheet.tvTotalPrice.setText(convertToRupiah(pricePerItem));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        bindingBottomSheet.btnCancel.setOnClickListener(v -> bottomSheetDialog.cancel());

        bindingBottomSheet.btnAdd.setOnClickListener(v -> {
            String noItemType = itemType.getNo_item_type();
            String total = Objects.requireNonNull(bindingBottomSheet.edtTotal.getText()).toString();
            if (noItemType.isEmpty()) {
                Toast.makeText(this, getResources().getString(R.string.input_can_not_be_empty, "jenis barang"), Toast.LENGTH_SHORT).show();
            } else if (total.isEmpty() || total.equalsIgnoreCase("0") || total.equalsIgnoreCase("0.0")) {
                Toast.makeText(this, getResources().getString(R.string.input_can_not_be_empty, "jumlah"), Toast.LENGTH_SHORT).show();
            } else {
                totalItem = Double.parseDouble(total);
                binding.btnSave.setVisibility(View.VISIBLE);
                if (mode.equals(MODE_UPDATE)) {
                    if (pricePerItem != listItemSheet.getPrice()) {
                        priceTotal -= listItemSheet.getPrice();
                        priceTotal += pricePerItem;
                    }
                    listItem.remove(indexOfChangeList);
                    listItemsForAdapter.remove(indexOfChangeList);
                } else {
                    priceTotal += pricePerItem;
                }
                itemTransaction.setTotal_price(priceTotal);

                listItem.add(new Item(itemType.getNo_item_type(), totalItem));
                listItemsForAdapter.add(new ListItem(itemMaster.getName(), itemType.getName(), unitItem.getName(), pricePerItem, totalItem));

                binding.tvIncome.setText(convertToRupiah(priceTotal));
                binding.tvTotal.setText(String.valueOf(listItemsForAdapter.size()));

                adapter.setAdapter(listItemsForAdapter);
                showPlaceholderOrRecyclerView(listItemsForAdapter.size() > 0);

                bottomSheetDialog.cancel();
            }

        });

    }


    private void fetchDataItemTypeForSpinner() {
        databaseReference.child(ITEM_TYPE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> listNameItemType = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ItemType itemTypeResult = dataSnapshot.getValue(ItemType.class);
                            if (itemTypeResult != null) {
                                listItemType.add(itemTypeResult);
                                listNameItemType.add(itemTypeResult.getName());
                            }
                        }
                        ArrayAdapter<String> arrayAdapterItemType = new ArrayAdapter<>(AddUpdateTransactionItemActivity.this, R.layout.dropdown_item, listNameItemType);
                        bindingBottomSheet.tvDdTypeItem.setAdapter(arrayAdapterItemType);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void fetchDataItemTypeByNameItemType(String nameItemType) {
        databaseReference.child(ITEM_TYPE).orderByChild(NAME).equalTo(nameItemType).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ItemType itemTypeResult = dataSnapshot.getValue(ItemType.class);
                    if (itemTypeResult != null) {
                        itemType = itemTypeResult;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchDataUnitItemByNoUnitItem(String noUnitItem, int i) {
        databaseReference.child(UNIT_ITEM).orderByChild(NO_UNI_ITEM).equalTo(noUnitItem).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UnitItem unitItemResult = dataSnapshot.getValue(UnitItem.class);
                    if (unitItemResult != null) {
                        unitItem = unitItemResult;
                        String pricePerUnit = convertToRupiah((int) listItemType.get(i).getPrice()) + "/" + unitItem.getName();
                        bindingBottomSheet.tvPrice.setText(pricePerUnit);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchDataUnitItemByNameUnitItem(String nameUnitItem) {
        databaseReference.child(UNIT_ITEM).orderByChild(NAME).equalTo(nameUnitItem).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UnitItem unitItemResult = dataSnapshot.getValue(UnitItem.class);
                    if (unitItemResult != null) {
                        unitItem = unitItemResult;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void fetchDataItemMasterByNoItemMaster(String noItemMaster) {
        databaseReference.child(ITEM_MASTER).orderByChild(NO_ITEM_MASTER).equalTo(noItemMaster).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ItemMaster itemMasterResult = dataSnapshot.getValue(ItemMaster.class);
                    if (itemMasterResult != null) {
                        itemMaster = itemMasterResult;
                        bindingBottomSheet.tvItemMaster.setText(itemMaster.getName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchDataItemMasterByNameItemMaster(String nameItemMaster) {
        databaseReference.child(ITEM_MASTER).orderByChild(NAME).equalTo(nameItemMaster).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ItemMaster itemMasterResult = dataSnapshot.getValue(ItemMaster.class);
                    if (itemMasterResult != null) {
                        itemMaster = itemMasterResult;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void fetchDataUserData(String noRegis) {
        if (noRegis != null) {
            databaseReference.child(USER_DATA).orderByChild(NO_REGIS).equalTo(noRegis).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        UserData userDataResult = dataSnapshot.getValue(UserData.class);
                        if (userDataResult != null) {
                            userData = userDataResult;
                        }
                    }
                    loadDataUser(userData);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


    private void fetchDataTransactionSaldoByNoTransactionSaldo(String noTransactionSaldo) {
        databaseReference.child(SALDO_TRANSACTION).orderByChild(NO_SALDO_TRANSACTION).equalTo(noTransactionSaldo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SaldoTransaction saldoTransactionResult = dataSnapshot.getValue(SaldoTransaction.class);
                    if (saldoTransactionResult != null) {
                        saldoTransaction = saldoTransactionResult;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void generateNoTransactionItemAndNoTransactionSaldo() {
        databaseReference.child(ITEM_TRANSACTION).orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            String noTransactionItem = DEFAULT_NO_TRANSACTION_ITEM;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.getKey() != null) {
                        noTransactionItem = increseNumber(dataSnapshot.getKey());
                    }
                }
                itemTransaction.setNo_item_transaction(noTransactionItem);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReference.child(SALDO_TRANSACTION).orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            String noSaldoTransaction = DEFAULT_NO_TRANSACTION_SALDO;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.getKey() != null) {
                        noSaldoTransaction = increseNumber(dataSnapshot.getKey());
                    }
                }
                saldoTransaction.setNo_saldo_transaction(noSaldoTransaction);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchSaldoNasabah(String noRegis) {
        if (noRegis != null) {
            databaseReference.child(SALDO_NASABAH).orderByChild(NO_REGIS).equalTo(noRegis).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Saldo saldoResult = dataSnapshot.getValue(Saldo.class);
                        if (saldoResult != null) {
                            saldo = saldoResult;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void loadDataUser(UserData userData) {
        Glide.with(this)
                .load(userData.getAvatar())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(binding.ivAvatar);
        binding.tvName.setText(userData.getName());
        binding.tvNoRegis.setText(userData.getNo_regis());
    }

    private void onSumbitTransactionItem(ItemTransaction itemTransaction, SaldoTransaction saldoTrasaction, Saldo saldo) {
        DatabaseReference databaseReferenceTransactinItem = databaseReference.child(ITEM_TRANSACTION).child(itemTransaction.getNo_item_transaction());
        DatabaseReference databaseReferenceTransactionSaldo = databaseReference.child(SALDO_TRANSACTION).child(saldoTrasaction.getNo_saldo_transaction());
        DatabaseReference databaseReferenceSaldoNasabah = databaseReference.child(SALDO_NASABAH).child(saldo.getNo_regis());
        databaseReferenceTransactinItem.setValue(itemTransaction).addOnSuccessListener(unused -> {
            databaseReferenceTransactionSaldo.setValue(saldoTrasaction).addOnSuccessListener(unused1 -> {
                databaseReferenceSaldoNasabah.setValue(saldo).addOnSuccessListener(unused2 -> {
                    Toast.makeText(this, getResources().getString(R.string.success), Toast.LENGTH_SHORT).show();
                    navigateToDetail();
                }).addOnFailureListener(e -> Toast.makeText(this, getResources().getString(R.string.failure), Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(this, getResources().getString(R.string.failure), Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(this, getResources().getString(R.string.failure), Toast.LENGTH_SHORT).show());
    }

    private void navigateToDetail() {
        Intent intent = new Intent(this, DetailTransactionItemActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
