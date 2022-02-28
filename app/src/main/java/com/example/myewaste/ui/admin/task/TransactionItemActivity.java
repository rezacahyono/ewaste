package com.example.myewaste.ui.admin.task;

import static com.example.myewaste.utils.Constant.DATE;
import static com.example.myewaste.utils.Constant.EXTRAS_ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Constant.ITEM_MASTER;
import static com.example.myewaste.utils.Constant.ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.NASABAH;
import static com.example.myewaste.utils.Constant.NO_ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.NO_NASABAH;
import static com.example.myewaste.utils.Util.getRegisterCode;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myewaste.DetailTransactionItemActivity;
import com.example.myewaste.R;
import com.example.myewaste.adapter.ListItemTransactionAdapter;
import com.example.myewaste.databinding.ActivityTransactionBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.item.ItemTransaction;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class TransactionItemActivity extends AppCompatActivity {
//    DatabaseReference databaseReference  = FirebaseDatabase.getInstance().getReference("transaksi_barang");
//    RecyclerView recyclerView;
//    TransaksiBarangAdapter transaksiBarangAdapter;
//    SessionManagement sessionManagement;
//    MasterTransaksiListener listener;
//    FloatingActionButton btnDownloadLaporanBarang;
//
//    private RelativeLayout layoutFilterTanggal;
//    private LinearLayout layoutTanggalMulai, layoutTanggalAkhir;
//    private ImageView ivFilter;
//    private EditText etSearch, etTanggalMulai, etTanggalAkhir;
//    private ImageButton btnSearchFilter, closeFilter;


    private ProgressDialog loading;
    private ArrayList<ItemTransaction> listItemTransaction = new ArrayList<>();
    private DatabaseReference databaseReference;
    private ListItemTransactionAdapter adapter;
    private MaterialDatePicker.Builder<Long> dateBuilder;
    private MaterialDatePicker<Long> datePicker;
    private String user = "";
    private long startDate = 0L;
    private long endDate = 0L;

    private boolean flShow = true;
    private ActivityTransactionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();
        MainToolbarBinding bindingToolbar = binding.mainToolbar;
        bindingToolbar.tvTitleBar.setText(R.string.report_transaction_item);
        bindingToolbar.btnBack.setOnClickListener(view -> onBackPressed());

        databaseReference = FirebaseDatabase.getInstance().getReference();

        adapter = new ListItemTransactionAdapter();
        binding.rvTransaction.setHasFixedSize(true);

        if (getIntent().getStringExtra(EXTRAS_USER_DATA) != null) {
            user = getIntent().getStringExtra(EXTRAS_USER_DATA);
        }

        binding.ibFilterChoose.setOnClickListener(v -> {
            if (flShow) {
                binding.pickStartDate.setText(R.string.start_date);
                binding.pickEndDate.setText(R.string.end_date);
                binding.flFilter.setVisibility(View.VISIBLE);
                flShow = false;
            } else {
                Log.d("TAG", "onCreate: not");
                binding.flFilter.setVisibility(View.GONE);
                flShow = true;
            }
        });

        binding.pickStartDate.setOnClickListener(v -> {
            setDatePickerStart();
        });

        binding.pickEndDate.setOnClickListener(v -> {
            setDatePickerEnd();
        });

        binding.ibFilter.setOnClickListener(v -> {
            if (startDate != 0L && endDate != 0L) {
                fetchDataItemTransactionByDate(startDate, endDate);
                flShow = true;
            } else {
                Toast.makeText(this, "Tanggal tidak boleh kosong", Toast.LENGTH_SHORT).show();
            }
        });
        binding.ibCancel.setOnClickListener(v -> {
            if (getRegisterCode(user).equalsIgnoreCase(NASABAH)) {
                fetchDataItemTransactionByNasabah();
            } else {
                fetchDataItemTransaction();
            }
            binding.flFilter.setVisibility(View.GONE);
            flShow = true;
        });

        actionSearchView();
        if (getRegisterCode(user).equalsIgnoreCase(NASABAH)) {
            fetchDataItemTransactionByNasabah();
            Log.d("TAG", "masabah: "+user);
        } else {
            fetchDataItemTransaction();
            Log.d("TAG", "tidak: "+user);
        }
        setRecyclerViewListItemTransaction();


//        layoutFilterTanggal = findViewById(R.id.filterTanggal);
//        layoutTanggalMulai = findViewById(R.id.layout_tanggal_start);
//        layoutTanggalAkhir = findViewById(R.id.layout_tanggal_end);
//        ivFilter = findViewById(R.id.ivFilter);
//        etSearch = findViewById(R.id.etSearch);
//        etTanggalMulai = findViewById(R.id.et_tanggalStart);
//        etTanggalAkhir = findViewById(R.id.et_tanggalEnd);
//        btnSearchFilter = findViewById(R.id.searchFilter);
//        closeFilter = findViewById(R.id.cancelFilter);
//
//        recyclerView = findViewById(R.id.recycler_view_transaksi);
//        btnDownloadLaporanBarang = findViewById(R.id.btnDownloadExclLaporanBarang);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        sessionManagement = new SessionManagement(getApplicationContext());
//        listener = new MasterTransaksiListener() {
//            @Override
//            public void onClickListTransaksi(ItemTransaction itemTransaction) {
//                Intent intent = new Intent(TransactionItemActivity.this, DetailTransactionItemActivity.class);
//                intent.putExtra("EXTRA_TRANSAKSI_BARANG", itemTransaction);
//                startActivity(intent);
//            }
//        };
//
//        etTanggalMulai.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showDialogCalendar(etTanggalMulai, "Tanggal Mulai");
//            }
//        });
//
//        layoutTanggalMulai.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showDialogCalendar(etTanggalMulai, "Tanggal Mulai");
//            }
//        });
//
//        etTanggalAkhir.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showDialogCalendar(etTanggalAkhir, "Tanggal Akhir");
//            }
//        });
//
//        layoutTanggalAkhir.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showDialogCalendar(etTanggalAkhir, "Tanggal Akhir");
//            }
//        });
//
//        btnSearchFilter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!etTanggalMulai.getText().toString().isEmpty() && !etTanggalAkhir.getText().toString().isEmpty()) {
//                    loadDataFilterByDate(changeFormat(etTanggalMulai.getText().toString()), changeFormat(etTanggalAkhir.getText().toString()));
//                    closeFilter.setVisibility(View.VISIBLE);
//                    btnSearchFilter.setVisibility(View.GONE);
//                }else{
//                    showMessage(TransactionItemActivity.this, "Harap Lengkapi Memilih Tanggal");
//                }
//            }
//        });
//
//        closeFilter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                etTanggalMulai.setText("");
//                etTanggalAkhir.setText("");
//                closeFilter.setVisibility(View.GONE);
//                btnSearchFilter.setVisibility(View.VISIBLE);
//                loadAllData();
//            }
//        });
//
//        etSearch.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if(!charSequence.equals("")){
//                    searchData(String.valueOf(charSequence));
//                }else{
//                    if (!etTanggalMulai.getText().toString().isEmpty() && !etTanggalAkhir.getText().toString().isEmpty()) {
//                        Log.d("TAG", "onStart: work");
//                        loadDataFilterByDate(changeFormat(etTanggalMulai.getText().toString()), changeFormat(etTanggalAkhir.getText().toString()));
//                    } else{
//                        loadAllData();
//                    }
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
//
//        ivFilter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (layoutFilterTanggal.getVisibility() == View.GONE) {
//                    layoutFilterTanggal.setVisibility(View.VISIBLE);
//                } else {
//                    layoutFilterTanggal.setVisibility(View.GONE);
//                }
//            }
//        });
//
//        btnDownloadLaporanBarang.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(TransactionItemActivity.this, SettingDownloadExcel.class);
//                intent.putExtra("mode", 0);
//                startActivity(intent);
//            }
//        });
    }

    public void onClickListTransaksi(ItemTransaction itemTransaction) {
    }


    private void setRecyclerViewListItemTransaction() {
        binding.rvTransaction.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTransaction.setAdapter(adapter);

        adapter.setOnItemClickCallback(itemTransaction -> {
            Intent intent = new Intent(this, DetailTransactionItemActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(EXTRAS_ITEM_TRANSACTION, itemTransaction);
            startActivity(intent);
        });
    }

    private void showPlaceholderOrRecyclerView(boolean isShow) {
        if (isShow) {
            binding.rvTransaction.setVisibility(View.VISIBLE);
            binding.ivPlaceholderEmpty.setVisibility(View.GONE);
            binding.tvTitle.setVisibility(View.GONE);
        } else {
            binding.rvTransaction.setVisibility(View.INVISIBLE);
            binding.ivPlaceholderEmpty.setVisibility(View.VISIBLE);
            binding.tvTitle.setVisibility(View.VISIBLE);
            binding.tvTitle.setText(getResources().getString(R.string.title_data_transaction_empty, ITEM_MASTER));
        }
    }

    public void actionSearchView() {
        binding.scId.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null) {
                    if (!query.isEmpty()) {
                        fetchDataItemTransactionById(query);
                        binding.scId.clearFocus();
                    }
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals("")) {
                    fetchDataItemTransactionById(newText);
                }
                if (newText.length() == 0) {
                    if (getRegisterCode(user).equalsIgnoreCase(NASABAH)) {
                        fetchDataItemTransactionByNasabah();
                    } else {
                        fetchDataItemTransaction();
                    }
                }
                return false;
            }
        });
    }


    private void setDatePickerStart() {
        dateBuilder = MaterialDatePicker.Builder.datePicker();
        datePicker = dateBuilder.build();
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        datePicker.addOnPositiveButtonClickListener(selection -> {
            binding.pickStartDate.setText(datePicker.getHeaderText());
            startDate = selection;
        });
    }

    private void setDatePickerEnd() {
        dateBuilder = MaterialDatePicker.Builder.datePicker();
        datePicker = dateBuilder.build();
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        datePicker.addOnPositiveButtonClickListener(selection -> {
            binding.pickEndDate.setText(datePicker.getHeaderText());
            endDate = selection;
        });
    }


    private void fetchDataItemTransaction() {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        databaseReference.child(ITEM_TRANSACTION).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ItemTransaction itemTransactionResult = dataSnapshot.getValue(ItemTransaction.class);
                    if (itemTransactionResult != null) {
                        listItemTransaction.add(itemTransactionResult);
                    }
                }
                adapter.setAdapter(listItemTransaction);
                showPlaceholderOrRecyclerView(listItemTransaction.size() > 0);
                listItemTransaction.clear();
                loading.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loading.dismiss();
            }
        });
    }

    private void fetchDataItemTransactionByDate(long start, long end) {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        databaseReference.child(ITEM_TRANSACTION).orderByChild(DATE).startAt(start).endAt(end).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ItemTransaction itemTransactionResult = dataSnapshot.getValue(ItemTransaction.class);
                    if (itemTransactionResult != null) {
                        if (getRegisterCode(user).equalsIgnoreCase(NASABAH)) {
                            if (itemTransactionResult.getNo_nasabah().equalsIgnoreCase(user)) {
                                listItemTransaction.add(itemTransactionResult);
                            }
                        } else {
                            listItemTransaction.add(itemTransactionResult);
                        }
                    }
                }
                adapter.setAdapter(listItemTransaction);
                showPlaceholderOrRecyclerView(listItemTransaction.size() > 0);
                listItemTransaction.clear();
                loading.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loading.dismiss();
            }
        });
    }

    private void fetchDataItemTransactionByNasabah() {
        if (!user.equals("")) {
            loading = ProgressDialog.show(this,
                    null,
                    getResources().getString(R.string.loading_message),
                    true,
                    false);
            databaseReference.child(ITEM_TRANSACTION).orderByChild(NO_NASABAH).equalTo(user).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ItemTransaction itemTransactionResult = dataSnapshot.getValue(ItemTransaction.class);
                        if (itemTransactionResult != null) {
                            listItemTransaction.add(itemTransactionResult);
                        }
                    }
                    showPlaceholderOrRecyclerView(listItemTransaction.size() > 0);
                    adapter.setAdapter(listItemTransaction);
                    listItemTransaction.clear();
                    loading.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    loading.dismiss();
                }
            });
        }
    }

    private void fetchDataItemTransactionById(String q) {
        databaseReference.child(ITEM_TRANSACTION).orderByChild(NO_ITEM_TRANSACTION).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ItemTransaction itemTransactionResult = dataSnapshot.getValue(ItemTransaction.class);
                    if (itemTransactionResult != null) {
                        if (itemTransactionResult.getNo_item_transaction().toLowerCase().contains(q.toLowerCase()) && !getRegisterCode(user).equalsIgnoreCase(NASABAH)) {
                            listItemTransaction.add(itemTransactionResult);
                        }
                        if (itemTransactionResult.getNo_item_transaction().toLowerCase().contains(q.toLowerCase()) && getRegisterCode(user).equalsIgnoreCase(NASABAH) && itemTransactionResult.getNo_nasabah().equalsIgnoreCase(user)) {
                            listItemTransaction.add(itemTransactionResult);
                        }
                    }
                }
                showPlaceholderOrRecyclerView(listItemTransaction.size() > 0);
                adapter.setAdapter(listItemTransaction);
                listItemTransaction.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //    @Override
//    protected void onStart() {
//        super.onStart();
//        if (!etSearch.getText().toString().isEmpty()) {
//            searchData(etSearch.getText().toString().toLowerCase());
//        } else if (!etTanggalMulai.getText().toString().isEmpty() && !etTanggalAkhir.getText().toString().isEmpty()) {
//            Log.d("TAG", "onStart: work");
//            loadDataFilterByDate(changeFormat(etTanggalMulai.getText().toString()), changeFormat(etTanggalAkhir.getText().toString()));
//        } else{
//            loadAllData();
//        }
//    }
//
//    private void loadAllData()
//    {
//        Query transaksiQuery = databaseReference.orderByChild("tanggal_transaksi");
//        // Read from the database
//        if(getRegisterCode(sessionManagement.getUserSession()).toLowerCase().equals("n")){
//            btnDownloadLaporanBarang.setVisibility(View.GONE);
//            transaksiQuery = databaseReference.orderByChild("no_nasabah").equalTo(sessionManagement.getUserSession());
//        }else if(getRegisterCode(sessionManagement.getUserSession()).toLowerCase().equals("t")){
//            btnDownloadLaporanBarang.setVisibility(View.GONE);
//            transaksiQuery = databaseReference.orderByChild("no_teller").equalTo(sessionManagement.getUserSession());
//        }
//        transaksiQuery.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ArrayList<ItemTransaction> list = new ArrayList<>();
//                for(DataSnapshot data : snapshot.getChildren()){
//                    ItemTransaction value = data.getValue(ItemTransaction.class);
//                    list.add(value);
//                }
//                transaksiBarangAdapter = new TransaksiBarangAdapter(TransactionItemActivity.this,list, listener);
//                recyclerView.setAdapter(transaksiBarangAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.w("Tag", "Failed to read value.", error.toException());
//            }
//        });
//    }
//
//    private void loadDataFilterByDate(long start, long end){
//        Query transaksiQuery = databaseReference.orderByChild("tanggal_transaksi").startAt(start).endAt(end);
//        // Read from the database
//        if(getRegisterCode(sessionManagement.getUserSession()).toLowerCase().equals("n")){
//            btnDownloadLaporanBarang.setVisibility(View.GONE);
//            transaksiQuery = databaseReference.orderByChild("no_nasabah").equalTo(sessionManagement.getUserSession());
//        }else if(getRegisterCode(sessionManagement.getUserSession()).toLowerCase().equals("t")){
//            btnDownloadLaporanBarang.setVisibility(View.GONE);
//            transaksiQuery = databaseReference.orderByChild("no_teller").equalTo(sessionManagement.getUserSession());
//        }
//        transaksiQuery.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ArrayList<ItemTransaction> list = new ArrayList<>();
//                for(DataSnapshot data : snapshot.getChildren()){
//                    ItemTransaction value = data.getValue(ItemTransaction.class);
//                    list.add(value);
//                }
//                transaksiBarangAdapter = new TransaksiBarangAdapter(TransactionItemActivity.this,list, listener);
//                recyclerView.setAdapter(transaksiBarangAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.w("Tag", "Failed to read value.", error.toException());
//            }
//        });
//    }
//
//    private void searchData(String str){
//        Query transaksiQuery = databaseReference.orderByChild("tanggal_transaksi");
//        // Read from the database
//        if(getRegisterCode(sessionManagement.getUserSession()).toLowerCase().equals("n")){
//            btnDownloadLaporanBarang.setVisibility(View.GONE);
//            transaksiQuery = databaseReference.orderByChild("no_nasabah").equalTo(sessionManagement.getUserSession());
//        }else if(getRegisterCode(sessionManagement.getUserSession()).toLowerCase().equals("t")){
//            btnDownloadLaporanBarang.setVisibility(View.GONE);
//            transaksiQuery = databaseReference.orderByChild("no_teller").equalTo(sessionManagement.getUserSession());
//        }
//        transaksiQuery.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ArrayList<ItemTransaction> list = new ArrayList<>();
//                for(DataSnapshot data : snapshot.getChildren()){
//                    ItemTransaction value = data.getValue(ItemTransaction.class);
////                    if(value.getNo_transaksi_barang().toLowerCase().contains(str)){
////                        list.add(value);
////                    }
//                }
//                transaksiBarangAdapter = new TransaksiBarangAdapter(TransactionItemActivity.this,list, listener);
//                recyclerView.setAdapter(transaksiBarangAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.w("Tag", "Failed to read value.", error.toException());
//            }
//        });
//    }
//
//    private void showDialogCalendar(View AttachTo, String title) {
//        final BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setCancelable(true);
//        dialog.setContentView(R.layout.custom_dialog_edit_user);
//
//        TextView titleDialog = dialog.findViewById(R.id.tvTitleDialog);
//        Button btnBatal = dialog.findViewById(R.id.btnDialogBatal);
//        Button btnSimpan = dialog.findViewById(R.id.btnDialogSimpan);
//        btnSimpan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
//        btnBatal.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
//        LinearLayout target = dialog.findViewById(R.id.frameEditData);
//        titleDialog.setText(title);
//        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View child = inflater.inflate(R.layout.frame_calendar, null);
//        CalendarView calendar = child.findViewById(R.id.calendarView);
//        target.addView(child);
//        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
//            @Override
//            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
//                ((TextView) AttachTo).setText(day + "-" + (month + 1) + "-" + year);
//            }
//        });
//
//        dialog.show();
//    }
//
//    private long changeFormat(String oldDateString){
//        final String OLD_FORMAT = "dd-MM-yyyy HH:mm:ss";
//        long millisecond = 0;
//
//        SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
//        Date d = null;
//        try {
//            d = sdf.parse(oldDateString+ " 12:0:0");
//            millisecond = d.getTime();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//
//        return millisecond;
//    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        finish();
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed();
//        return true;
//    }
//
    public interface MasterTransaksiListener {
        void onClickListTransaksi(ItemTransaction itemTransaction);
    }

}
