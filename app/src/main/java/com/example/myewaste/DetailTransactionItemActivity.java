package com.example.myewaste;

import static com.example.myewaste.utils.Constant.EXTRAS_ITEM_TRANSACTION;
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
import static com.example.myewaste.utils.Util.convertDate;
import static com.example.myewaste.utils.Util.convertToRupiah;
import static com.example.myewaste.utils.Util.getRegisterCode;

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

import com.example.myewaste.adapter.ListItemDetailTransactionAdapter;
import com.example.myewaste.databinding.ActivityDetailTransactionItemBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.ListItem;
import com.example.myewaste.model.item.Item;
import com.example.myewaste.model.item.ItemMaster;
import com.example.myewaste.model.item.ItemTransaction;
import com.example.myewaste.model.item.ItemType;
import com.example.myewaste.model.item.UnitItem;
import com.example.myewaste.model.saldo.SaldoTransaction;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.ui.admin.task.AddUpdateTransactionItemActivity;
import com.example.myewaste.ui.admin.task.TransactionItemActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class DetailTransactionItemActivity extends AppCompatActivity {

    //    private LinearLayout layout;
//    private TextView tvIdTransaksi, tvNamaBarang, tvNamaJenisBarang, tvNamaUser, tvNamaTeller, tvHargaBarang, tvJumlah, tvSatuan, tvTotalHarga, tvTanggalTransaksi;
//    private Button btnEditTransaksi;
//
//    private ItemTransaction itemTransaction;
//    private ItemMaster itemMaster;
//    private UnitItem unitItem;
//    private ItemType jenisBarang;
//    private UserData dataTeller;
//    private UserData dataUser;
//    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private SessionManagement sessionManagement;
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
        sessionManagement = new SessionManagement(this);

        adapter = new ListItemDetailTransactionAdapter();
        binding.rvItem.setHasFixedSize(true);

        itemTransaction = getIntent().getParcelableExtra(EXTRAS_ITEM_TRANSACTION);
        if (itemTransaction != null) {
            fetchUserData(itemTransaction.getNo_nasabah(), itemTransaction.getNo_teller());
            setDataItemTransaction(itemTransaction);
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

        bindingToolbar.btnTrash.setOnClickListener(v -> {
            alertDelete(itemTransaction);
        });

        binding.btnUpdateTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddUpdateTransactionItemActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(EXTRAS_ITEM_TRANSACTION, itemTransaction);
            startActivity(intent);
        });

        setRecyclerViewItemListDetail();

//        layout = findViewById(R.id.layout_detail_transaksi_barang);
//        tvIdTransaksi = findViewById(R.id.tv_id_transaksi_dtb);
//        tvNamaBarang = findViewById(R.id.tv_nama_barang_dtb);
//        tvNamaJenisBarang = findViewById(R.id.tv_nama_jenis_barang_dtb);
//        tvNamaUser = findViewById(R.id.tv_nama_user_dtb);
//        tvNamaTeller = findViewById(R.id.tv_nama_teller_dtb);
//        tvHargaBarang = findViewById(R.id.tv_harga_dtb);
//        tvJumlah = findViewById(R.id.tv_jumlah_dtb);
//        tvSatuan = findViewById(R.id.tv_satuan_dtb);
//        tvTotalHarga = findViewById(R.id.tv_total_harga_dtb);
//        tvTanggalTransaksi = findViewById(R.id.tv_tanggal_transaksi_dtb);
//        btnEditTransaksi = findViewById(R.id.btn_edit_dtb);
//        itemTransaction = getIntent().getParcelableExtra("EXTRA_TRANSAKSI_BARANG");
//        sessionManagement = new SessionManagement(getApplicationContext());
////        prepareLayout();
//
//        if(getRegisterCode(sessionManagement.getUserSession()).toLowerCase().equals("n")){
//            btnEditTransaksi.setVisibility(View.GONE);
//        }
//
//        btnEditTransaksi.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(DetailTransactionItemActivity.this, AddUpdateTransactionItemActivity.class);
//                intent.putExtra("EXTRA_TRANSAKSI_BARANG", itemTransaction);
//                startActivity(intent);
//                finish();
//            }
//        });
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
            Log.d("TAG", "alertDelete: " + itemTransaction.getNo_item_transaction());
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
                        navigateToTransactionItem();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, getResources().getString(R.string.message_failure_delete_transaction), Toast.LENGTH_SHORT).show());
        }
    }


    private void fetchDataSaldoTransaction(String noSaldoTransaction) {
        databaseReference.child(SALDO_TRANSACTION).orderByChild(NO_SALDO_TRANSACTION).equalTo(noSaldoTransaction).addListenerForSingleValueEvent(new ValueEventListener() {
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


    private void fetchUserData(String noNasabah, String noTeller) {
        databaseReference.child(USER_DATA).orderByChild(NO_REGIS).equalTo(noNasabah).addListenerForSingleValueEvent(new ValueEventListener() {
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
        databaseReference.child(USER_DATA).orderByChild(NO_REGIS).equalTo(noTeller).addListenerForSingleValueEvent(new ValueEventListener() {
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


    private void navigateToTransactionItem() {
        Intent intent = new Intent(this, TransactionItemActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


//    private void prepareLayout(){
//        tvIdTransaksi.setText(itemTransaction.getNo_transaksi_barang());
//        tvJumlah.setText(String.valueOf(itemTransaction.getJumlah()));
//        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
//        Date date = new Date(itemTransaction.getTanggal_transaksi());
//        String tanggalTransaksi = sdf.format(date);
//        tvTanggalTransaksi.setText(tanggalTransaksi);
//        tvTotalHarga.setText(convertToRupiah(itemTransaction.getTotal_harga()));
//        loadUserData(0, itemTransaction.getNo_nasabah());//mode 0 for user
//        loadUserData(1, itemTransaction.getNo_teller());//mode 1 for teller
//        loadJenisBarangById(itemTransaction.getNomor_jenis_barang());
//    }
//
//    private void loadUserData(int mode, String idUser){
//        //todo mode 0 for user , 1 for teller
//        Query userQuery = databaseReference.child("userdata").orderByChild("noregis").equalTo(idUser);
//        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot data : snapshot.getChildren()){
//                    if(mode == 0){
//                        dataUser = data.getValue(UserData.class);
//                    }else{
//                        dataTeller = data.getValue(UserData.class);
//                    }
//                }
//                if(mode == 0){
//                    tvNamaUser.setText(dataUser.getName());
//                }else{
//                    tvNamaTeller.setText(dataTeller.getName());
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    private void loadJenisBarangById(String idJenisBarang){
//        Query jenisBarangQuery = databaseReference.child("jenis_barang").orderByChild("no_master_jenis_barang").equalTo(idJenisBarang);
//        jenisBarangQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot data : snapshot.getChildren()){
//                    jenisBarang = data.getValue(ItemType.class);
//                }
//                tvNamaJenisBarang.setText(jenisBarang.getNama_master_jenis_barang());
//                tvHargaBarang.setText(convertToRupiah(jenisBarang.getHarga()));
//                loadSatuanById(jenisBarang.getNo_satuan_barang());
//                loadBarangById(jenisBarang.getNo_master_barang());
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    private void loadSatuanById(String idSatuan){
//        Query satuanQuery = databaseReference.child("satuan_barang").orderByChild("noSatuan").equalTo(idSatuan);
//        satuanQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot data : snapshot.getChildren()){
//                    unitItem = data.getValue(UnitItem.class);
//                }
//                tvSatuan.setText(unitItem.getNamaSatuan());
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    private void loadBarangById(String idBarang){
//        Query barangQuery = databaseReference.child("barang").orderByChild("no_master_barang").equalTo(idBarang);
//        barangQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot data : snapshot.getChildren()){
//                    itemMaster = data.getValue(ItemMaster.class);
//                }
//                tvNamaBarang.setText(itemMaster.getNama_master_barang());
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed();
//        return true;
//    }
}
