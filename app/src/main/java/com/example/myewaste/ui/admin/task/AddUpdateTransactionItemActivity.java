package com.example.myewaste.ui.admin.task;

import static com.example.myewaste.utils.Constant.ACCEPTED;
import static com.example.myewaste.utils.Constant.DEFAULT_NO_TRANSACTION_ITEM;
import static com.example.myewaste.utils.Constant.DEFAULT_NO_TRANSACTION_SALOD;
import static com.example.myewaste.utils.Constant.DEPOSIT;
import static com.example.myewaste.utils.Constant.EXTRAS_ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Constant.ITEM_MASTER;
import static com.example.myewaste.utils.Constant.ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.ITEM_TYPE;
import static com.example.myewaste.utils.Constant.NO_ITEM_MASTER;
import static com.example.myewaste.utils.Constant.NO_REGIS;
import static com.example.myewaste.utils.Constant.NO_UNI_ITEM;
import static com.example.myewaste.utils.Constant.SALDO_NASABAH;
import static com.example.myewaste.utils.Constant.SALDO_TRANSACTION;
import static com.example.myewaste.utils.Constant.UNIT_ITEM;
import static com.example.myewaste.utils.Constant.USER_DATA;
import static com.example.myewaste.utils.Util.convertToRupiah;
import static com.example.myewaste.utils.Util.increseNumber;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.myewaste.R;
import com.example.myewaste.SessionManagement;
import com.example.myewaste.adapter.ListItemAdapter;
import com.example.myewaste.databinding.ActivityAddInputTransactionScalesBinding;
import com.example.myewaste.databinding.BottomSheetInputItemScaleBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.ListItem;
import com.example.myewaste.model.item.Item;
import com.example.myewaste.model.item.ItemMaster;
import com.example.myewaste.model.item.ItemTransaction;
import com.example.myewaste.model.item.ItemType;
import com.example.myewaste.model.item.UnitItem;
import com.example.myewaste.model.saldo.Saldo;
import com.example.myewaste.model.saldo.SaldoTransaction;
import com.example.myewaste.model.user.User;
import com.example.myewaste.model.user.UserData;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class AddUpdateTransactionItemActivity extends AppCompatActivity {
//    private Spinner spinnerNasabah;
//    private Spinner spinnerJenisBarang;
//    private TextView namaBarang;
//    private TextView namaSatuan;
//    private EditText harga;
//    private EditText jumlahBarang;
//    private EditText totalHarga;
//    private EditText keterangan;
//    private Button button;
//
//    private ItemTransaction masterTransaksi;
//    private SaldoTransaction saldoTransaction;
//    private static final String TAG = "MasterTransaksi";
//    private int mode = 0; //0 for tambah, 1 for edit
//    private SessionManagement sessionManagement;
//    public static final String DEFAULT_KODE_TRANSAKSI_BARANG = "TRB-0001";
//    public static final String DEFAULT_KODE_TRANSAKSI_SALDO = "TRS-0001";
//
//    ArrayList<UserData> dataNasabah = new ArrayList<>();
//    ArrayList<ItemType> dataJenisBarang = new ArrayList<>();
//    onFetchDataListener listener;
//    UnitItem unitItem;
//    private ItemMaster itemMaster;
//    int lastTotalHarga = 0;

    private ItemTransaction itemTransaction = new ItemTransaction();
    private UserData userData = new UserData();
    private ItemType itemType;
    private ArrayList<ItemType> listItemType = new ArrayList<>();
    private ArrayList<Item> listItem = new ArrayList<>();
    private ArrayList<ListItem> listItemsForAdapter = new ArrayList<>();
    private Saldo saldo = new Saldo();

    private SessionManagement sessionManagement;
    private DatabaseReference databaseReference;
    private BottomSheetInputItemScaleBinding bindingBottomSheet;
    private BottomSheetDialog bottomSheetDialog;

    private String newNoTransactionItem = "";
    private String newNoTransactionSaldo = "";
    private String nameUnitItem = "";
    private int priceTotal = 0;
    private int price = 0;
    private long date = 0L;

    private ListItemAdapter adapter;
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

        if (getIntent().getParcelableExtra(EXTRAS_USER_DATA) != null) {
            userData = getIntent().getParcelableExtra(EXTRAS_USER_DATA);
            loadDataUser(userData);
        }

        if (getIntent().getParcelableExtra(EXTRAS_ITEM_TRANSACTION)!= null){
            itemTransaction = getIntent().getParcelableExtra(EXTRAS_ITEM_TRANSACTION);
            fetchDataUserData(itemTransaction.getNo_nasabah());

            priceTotal = (int) itemTransaction.getTotal_price();
            binding.tvIncome.setText(convertToRupiah(priceTotal));
            binding.tvTotal.setText(String.valueOf(itemTransaction.getItem_list().size()));
        }

        generateNoTransactionItemAndNoTransactionSaldo();

        fecthDataSaldoNasabah(userData.getNo_regis());

        adapter = new ListItemAdapter();

        binding.fbAddTypeItem.setOnClickListener(v -> {
            fetchDataItemTypeForSpinner();
            showAddTransactionItemBottomSheet();
        });

        setRecyclerView();

        binding.btnSave.setOnClickListener(v -> {
            saldo.setNo_regis(userData.getNo_regis());
            saldo.setSaldo(priceTotal);
//            onSubmitTransactionItemAndSaldo(
//                    new ItemTransaction(
//                            newNoTransactionItem,
//                            userData.getNo_regis(),
//                            sessionManagement.getUserSession(),
//                            newNoTransactionSaldo,
//                            listItem,
//                            date,
//                            priceTotal
//                    ),
//                    new SaldoTransaction(
//                            newNoTransactionSaldo,
//                            userData.getNo_regis(),
//                            DEPOSIT,
//                            sessionManagement.getUserSession(),
//                            ACCEPTED,
//                            priceTotal,
//                            0,
//                            date
//                    ),
//                    saldo
//            );

        });

    }

    private void setBottomSheet() {
        bottomSheetDialog = new BottomSheetDialog(this);
        bindingBottomSheet = BottomSheetInputItemScaleBinding.inflate(getLayoutInflater());
        View viewBottomSheet = bindingBottomSheet.getRoot();
        bottomSheetDialog.setContentView(viewBottomSheet);
    }

    public void showAddTransactionItemBottomSheet() {
        date = System.currentTimeMillis();
        setBottomSheet();
        bottomSheetDialog.show();

        bindingBottomSheet.tvDdTypeItem.setOnItemClickListener((adapterView, view, i, l) -> {
            fetchDataItemMasterByNoItem(listItemType.get(i).getNo_item_master());
            fetchDataUnitItemByNoUnitItem(listItemType.get(i).getNo_unit_item(), i);
            itemType = listItemType.get(i);
            bindingBottomSheet.edtTotal.setEnabled(true);
        });

        bindingBottomSheet.edtTotal.setEnabled(false);
        bindingBottomSheet.edtTotal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (itemType != null && charSequence.length() > 0) {
                    price = (int) (itemType.getPrice() * Double.parseDouble(charSequence.toString()));
                    bindingBottomSheet.tvTotalIncome.setText(convertToRupiah(price));
                }
                if (charSequence.length() == 0) {
                    price = 0;
                    bindingBottomSheet.tvTotalIncome.setText(convertToRupiah(0));
                }
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
                double totals = Double.parseDouble(total);
                binding.btnSave.setVisibility(View.VISIBLE);
                listItem.add(new Item(noItemType, totals));
                priceTotal += price;
                listItemsForAdapter.add(new ListItem(bindingBottomSheet.tvItemMaster.getText().toString(), bindingBottomSheet.tvDdTypeItem.getText().toString(), nameUnitItem, price, totals));
                binding.tvIncome.setText(convertToRupiah(priceTotal));
                binding.tvTotal.setText(String.valueOf(listItemsForAdapter.size()));
                adapter.setAdapter(listItemsForAdapter);
                showPlaceholderOrRecyclerView(listItemsForAdapter.size() > 0);
                bottomSheetDialog.cancel();
            }
        });
    }


    private void setRecyclerView() {
        binding.rvItem.setLayoutManager(new LinearLayoutManager(this));
        binding.rvItem.setAdapter(adapter);

        adapter.setOnItemClickCallback(position -> {
            if (listItemsForAdapter.size() > 1) {
                priceTotal -= listItemsForAdapter.get(position).getPrice();
                listItem.remove(position);
                listItemsForAdapter.remove(position);
            } else {
                priceTotal = 0;
                listItem = new ArrayList<>();
                listItemsForAdapter = new ArrayList<>();
                binding.btnSave.setVisibility(View.GONE);
            }
            binding.tvTotal.setText(String.valueOf(listItem.size()));
            binding.tvIncome.setText(convertToRupiah(priceTotal));
            adapter.setAdapter(listItemsForAdapter);
        });

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

    private void fetchDataItemTypeForSpinner() {
        databaseReference.child(ITEM_TYPE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
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
                        ArrayAdapter<String> arrayAdapterTypeItem = new ArrayAdapter<>(AddUpdateTransactionItemActivity.this, R.layout.dropdown_item, listNameItemType);
                        bindingBottomSheet.tvDdTypeItem.setAdapter(arrayAdapterTypeItem);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void fetchDataItemMasterByNoItem(String noItem) {
        if (noItem != null) {
            Query queryItemMasterByNoItem = databaseReference.child(ITEM_MASTER).orderByChild(NO_ITEM_MASTER).equalTo(noItem);
            queryItemMasterByNoItem.get().addOnCompleteListener(task -> {
                DataSnapshot result = task.getResult();
                if (result != null) {
                    for (DataSnapshot dataSnapshot : result.getChildren()) {
                        ItemMaster itemMasterResult = dataSnapshot.getValue(ItemMaster.class);
                        if (itemMasterResult != null) {
                            bindingBottomSheet.tvItemMaster.setText(itemMasterResult.getName());
                        }
                    }
                }
            });
        }
    }

    private void fetchDataUnitItemByNoUnitItem(String noUnitItem, int i) {
        if (noUnitItem != null) {
            Query queryUnitItemByNoUnitItem = databaseReference.child(UNIT_ITEM).orderByChild(NO_UNI_ITEM).equalTo(noUnitItem);
            queryUnitItemByNoUnitItem.get().addOnCompleteListener(task -> {
                DataSnapshot result = task.getResult();
                if (result != null) {
                    for (DataSnapshot dataSnapshot : result.getChildren()) {
                        UnitItem unitItemResult = dataSnapshot.getValue(UnitItem.class);
                        if (unitItemResult != null) {
                            String price = convertToRupiah((int) listItemType.get(i).getPrice()) + "/" + unitItemResult.getName();
                            nameUnitItem = unitItemResult.getName();
                            bindingBottomSheet.tvPrice.setText(price);
                        }
                    }
                }
            });
        }
    }

    private void loadDataUser(UserData userData) {
        Glide.with(this)
                .load(userData.getAvatar())
                .placeholder(R.drawable.ic_placeholder)
                .into(binding.ivAvatar);

        binding.tvName.setText(userData.getName());
        binding.tvNoRegis.setText(userData.getNo_regis());
    }

    private void generateNoTransactionItemAndNoTransactionSaldo() {
        databaseReference.child(ITEM_TRANSACTION).get().addOnCompleteListener(task -> {
            newNoTransactionItem = DEFAULT_NO_TRANSACTION_ITEM;
            if (task.isSuccessful()) {
                DataSnapshot result = task.getResult();
                if (result != null) {
                    for (DataSnapshot dataSnapshot : result.getChildren()) {
                        ItemTransaction itemTransactionResult = dataSnapshot.getValue(ItemTransaction.class);
                        if (itemTransactionResult != null) {
                            newNoTransactionItem = increseNumber(itemTransactionResult.getNo_item_transaction());
                        }
                    }
                }
            }
        });
        databaseReference.child(SALDO_TRANSACTION).get().addOnCompleteListener(task -> {
            newNoTransactionSaldo = DEFAULT_NO_TRANSACTION_SALOD;
            if (task.isSuccessful()) {
                DataSnapshot result = task.getResult();
                if (result != null) {
                    for (DataSnapshot dataSnapshot : result.getChildren()) {
                        SaldoTransaction saldoTransactionResult = dataSnapshot.getValue(SaldoTransaction.class);
                        if (saldoTransactionResult != null) {
                            newNoTransactionSaldo = increseNumber(saldoTransactionResult.getNo_saldo_transaction());
                        }
                    }
                }
            }
        });
    }

    private void onSubmitTransactionItemAndSaldo(ItemTransaction itemTransaction, SaldoTransaction saldoTransaction, Saldo saldo) {
        if (itemTransaction != null && saldoTransaction != null) {
            DatabaseReference databaseReferenceItemTransaction = databaseReference.child(ITEM_TRANSACTION).child(itemTransaction.getNo_item_transaction());
            DatabaseReference databaseReferenceSaldoTransaction = databaseReference.child(SALDO_TRANSACTION).child(saldoTransaction.getNo_saldo_transaction());
            DatabaseReference databaseReferenceSaldo = databaseReference.child(SALDO_NASABAH).child(saldo.getNo_regis());

            databaseReferenceItemTransaction.setValue(itemTransaction)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "success", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "failure", Toast.LENGTH_SHORT).show());

            databaseReferenceSaldoTransaction.setValue(saldoTransaction)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "success", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "failure", Toast.LENGTH_SHORT).show());

            databaseReferenceSaldo.setValue(saldo)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "success", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "failure", Toast.LENGTH_SHORT).show());
        }
    }

    private void fecthDataSaldoNasabah(String noRegis){
        databaseReference.child(SALDO_NASABAH).orderByChild(NO_REGIS).equalTo(noRegis).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Saldo saldoResult = dataSnapshot.getValue(Saldo.class);
                    if (saldoResult != null){
                        saldo = saldoResult;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchDataUserData(String noRegis){
        if (noRegis != null){
            databaseReference.child(USER_DATA).orderByChild(NO_REGIS).equalTo(noRegis).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        UserData userDataResult = dataSnapshot.getValue(UserData.class);
                        if (userDataResult != null){
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



//
//    private void loadSatuanById(String idSatuan){
//        Query satuanQuery = databaseReference.child("satuan_barang").orderByChild("noSatuan").equalTo(idSatuan);
//        satuanQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot data : snapshot.getChildren()){
//                    unitItem = data.getValue(UnitItem.class);
//                }
//                namaSatuan.setText(unitItem.getNamaSatuan());
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
//                namaBarang.setText(itemMaster.getNama_master_barang());
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    private void onSubmit(ItemTransaction masterTransaksi)
//    {
//        DatabaseReference saldoReference = databaseReference.child("saldonasabah").child(masterTransaksi.getNo_nasabah());
//        DatabaseReference dbReferencesTeller = databaseReference.child("transaksi_barang").child(masterTransaksi.getNo_transaksi_barang());
//        dbReferencesTeller.setValue(masterTransaksi).addOnSuccessListener(aVoid->
//        {
//            saldoReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    Saldo saldoNasabah = snapshot.getValue(Saldo.class);
//                    int currentBalance = saldoNasabah.getSaldo();
//                    int newBalance;
//                    if(mode == 0){
//                        newBalance = currentBalance + masterTransaksi.getTotal_harga();
//                        saldoNasabah.setSaldo(newBalance);
//                        saldoReference.setValue(saldoNasabah);
//                    }else{
//                        int removeLastBalance = currentBalance - lastTotalHarga;
//                        newBalance = removeLastBalance + masterTransaksi.getTotal_harga();
//                        saldoNasabah.setSaldo(newBalance);
//                        saldoReference.setValue(saldoNasabah);
//                    }
//
//                    createTransaksiSaldo();
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//        });
//    }
//
//    private void createTransaksiSaldo(){
//        saldoTransaction.setId_nasabah(masterTransaksi.getNo_nasabah());
//        saldoTransaction.setId_penerima(masterTransaksi.getNo_teller());
//        saldoTransaction.setJenis_transaksi("SETOR");
//        saldoTransaction.setJumlah_transaksi(masterTransaksi.getTotal_harga());
//        saldoTransaction.setPotongan(0);
//        saldoTransaction.setStatus("ACCEPTED");
//        saldoTransaction.setTanggal_transaksi(masterTransaksi.getTanggal_transaksi());
//        DatabaseReference transaksiSaldoReference = databaseReference.child("transaksi_saldo").child(masterTransaksi.getNo_transaksi_saldo());
//        transaksiSaldoReference.setValue(saldoTransaction).addOnSuccessListener(aVoid->
//        {
//            Toast.makeText(AddUpdateTransactionItemActivity.this, "Berhasil Menambahkan Jenis Barang", Toast.LENGTH_SHORT).show();
//            finish();
//        });
//    }
//
//    //ID Otomatis
//    private void fetchDataMasterTeller()
//    {
//        databaseReference.child("transaksi_barang").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DataSnapshot> task) {
//                String kodeTransaksi = DEFAULT_KODE_TRANSAKSI_BARANG;
//                if(task.isSuccessful()) {
//                    DataSnapshot result = task.getResult();
//                    if(result.getValue() != null) {
//                        for (DataSnapshot dataSnapshot : result.getChildren()) {
//                            if(mode == 0)
//                            {
//                                String dataKodeTransaksi = dataSnapshot.getKey();
//                                if (dataKodeTransaksi != null) {
//                                    kodeTransaksi = increseNumber(dataKodeTransaksi);
//                                }
//                            }else{
//                                kodeTransaksi = masterTransaksi.getNo_transaksi_barang();
//                            }
//                        }
//                    }
//                }
//                listener.onSuccessGenerateKodeTrBarang(kodeTransaksi);
//            }
//        });
//    }
//
//    //ID Otomatis
//    private void fetchDataTransaksiSaldo()
//    {
//        databaseReference.child("transaksi_saldo").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DataSnapshot> task) {
//                String kodeTransaksiSaldo = DEFAULT_KODE_TRANSAKSI_SALDO;
//                if(task.isSuccessful()) {
//                    DataSnapshot result = task.getResult();
//                    if(result.getValue() != null) {
//                        for (DataSnapshot dataSnapshot : result.getChildren()) {
//                            if(mode == 0)
//                            {
//                                String dataKodeTransaksi = dataSnapshot.getKey();
//                                if (dataKodeTransaksi != null) {
//                                    kodeTransaksiSaldo = increseNumber(dataKodeTransaksi);
//                                }
//                            }else{
//                                kodeTransaksiSaldo = masterTransaksi.getNo_transaksi_saldo();
//                            }
//                        }
//                    }
//                }
//                listener.onSuccessGenerateKodeTrSaldo(kodeTransaksiSaldo);
//            }
//        });
//    }
//
//    //View Spinner
//    private void showDataSpinnerNasabah()
//    {
//
//        UserData dumy = new UserData();
//        dumy.setName("Pilih Nasabah");
//        dataNasabah.add(0, dumy);
//        DatabaseReference refrenceNasabah = databaseReference.child("userdata");
//        refrenceNasabah.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                int index = 0;
//                for(DataSnapshot item: snapshot.getChildren())
//                {
//                    UserData userData = item.getValue(UserData.class);
//                    if(getRegisterCode(userData.getNo_regis()).toLowerCase().equals("n"))
//                    {
//                        dataNasabah.add(userData);
//                        if(mode == 1 && userData.getNo_regis().toLowerCase().equals(masterTransaksi.getNo_nasabah().toLowerCase())){
//                            index = dataNasabah.size() -1;
//                        }
//                    }
//
//                }
//                ArrayAdapter<UserData> arrayAdapter = new ArrayAdapter<>(AddUpdateTransactionItemActivity.this, R.layout.style_spinner, dataNasabah);
//                spinnerNasabah.setAdapter(arrayAdapter);
//                spinnerNasabah.setSelection(index);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    private void showDataSpinnerJenisBarang()
//    {
//        ItemType dumy = new ItemType();
//        dumy.setNama_master_jenis_barang("Pilih Jenis Barang");
//        dataJenisBarang.add(0, dumy);
//        DatabaseReference referenceJenisBarang = databaseReference.child("jenis_barang");
//        referenceJenisBarang.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                int index = 0;
//                for(DataSnapshot item: snapshot.getChildren())
//                {
//                    ItemType pilihBarang = item.getValue(ItemType.class);
//                    dataJenisBarang.add(pilihBarang);
//                    if(mode == 1 && pilihBarang.getNo_master_jenis_barang().toLowerCase().equals(masterTransaksi.getNomor_jenis_barang().toLowerCase())){
//                        index = dataJenisBarang.size() -1;
//                    }
//                }
//                ArrayAdapter<ItemType> arrayAdapter = new ArrayAdapter<>(AddUpdateTransactionItemActivity.this, R.layout.style_spinner, dataJenisBarang);
//                spinnerJenisBarang.setAdapter(arrayAdapter);
//                spinnerJenisBarang.setSelection(index);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

//
//    public void setTotalHarga() {
//        Integer hargaToInt = Integer.parseInt(harga.getText().toString()); //kegunaannya untuk merubah harga yang tadinya berbentuk string menjadi integer
//        Integer jumlahToInt = Integer.parseInt(jumlahBarang.getText().toString()); //kegunaannya untuk merubah jumlah yang tadinya berbentuk string menjadi integer
//        Integer hasilTotal = hargaToInt * jumlahToInt; // totalkan value
//        totalHarga.setText(String.valueOf(hasilTotal));
//    }
//
//    public interface onFetchDataListener {
//        void onSuccessGenerateKodeTrBarang(String noTransaksi);
//
//        void onSuccessGenerateKodeTrSaldo(String noTransaksi);
//    }
}
