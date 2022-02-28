package com.example.myewaste.ui.component;

import static com.example.myewaste.utils.Constant.DATE;
import static com.example.myewaste.utils.Constant.EXTRAS_SALDO_TRANSACTION;
import static com.example.myewaste.utils.Constant.ITEM_MASTER;
import static com.example.myewaste.utils.Constant.ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.SALDO_TRANSACTION;
import static com.example.myewaste.utils.Constant.TYPE_TRANSACTION;
import static com.example.myewaste.utils.Constant.WITHDRAW;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myewaste.DetailTransactionSaldoActivity;
import com.example.myewaste.R;
import com.example.myewaste.adapter.ListSaldoTransactionAdapter;
import com.example.myewaste.databinding.ActivityTransactionBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.saldo.SaldoTransaction;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class TransactionSaldoActivity extends AppCompatActivity {
//
//    private RelativeLayout layoutFilterTanggal;
//    private LinearLayout layoutTanggalMulai, layoutTanggalAkhir;
//    private ImageView ivFilter;
//    private EditText etSearch, etTanggalMulai, etTanggalAkhir;
//    private ImageButton btnSearchFilter, closeFilter;
//
//    DatabaseReference databaseReference  = FirebaseDatabase.getInstance().getReference("transaksi_saldo");
//    RecyclerView recyclerView;
//    TransaksiSaldoAdapter transaksiSaldoAdapter;
//    SessionManagement sessionManagement;
//    TransactionSaldoActivity.MasterSaldoListener listener;
//    FloatingActionButton btnDownloadExcel;

    private ListSaldoTransactionAdapter adapter;
    private ProgressDialog loading;
    private ArrayList<SaldoTransaction> listSaldoTransaction = new ArrayList<>();
    private DatabaseReference databaseReference;
    private MaterialDatePicker.Builder<Long> dateBuilder;
    private MaterialDatePicker<Long> datePicker;
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
        bindingToolbar.tvTitleBar.setText(R.string.report_transaction_saldo);
        bindingToolbar.btnBack.setOnClickListener(view -> onBackPressed());

        databaseReference = FirebaseDatabase.getInstance().getReference();


        adapter = new ListSaldoTransactionAdapter();
        binding.rvTransaction.setHasFixedSize(true);

        binding.ibFilterChoose.setOnClickListener(v -> {
            if (flShow) {
                binding.pickStartDate.setText(R.string.start_date);
                binding.pickEndDate.setText(R.string.end_date);
                binding.flFilter.setVisibility(View.VISIBLE);
                flShow = false;
            } else {
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
                fetchDataSaldoTransactionByDate(startDate, endDate);
                flShow = true;
            } else {
                Toast.makeText(this, "Tanggal tidak boleh kosong", Toast.LENGTH_SHORT).show();
            }
        });

        binding.ibCancel.setOnClickListener(v -> {
            fetchDataSaldoTransaction();
            binding.flFilter.setVisibility(View.GONE);
            flShow = true;
        });

//        getSupportActionBar().setTitle("Data Transaksi Penarikan");
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
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
//        recyclerView = findViewById(R.id.recycler_view_transaksi_saldo);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        btnDownloadExcel = findViewById(R.id.btnDownloadExclLaporanSaldo);
//        sessionManagement = new SessionManagement(getApplicationContext());
//
//        if(!getRegisterCode(sessionManagement.getUserSession()).toLowerCase().equals("sa")){
//            btnDownloadExcel.setVisibility(View.GONE);
//        }
//
//        listener = new TransactionSaldoActivity.MasterSaldoListener() {
//            @Override
//            public void onClickListTransaksiSaldo(SaldoTransaction saldoTransaction) {
//                Intent intent = new Intent(TransactionSaldoActivity.this, DetailTransactionSaldoActivity.class);
//                intent.putExtra("EXTRA_TRANSAKSI_SALDO", saldoTransaction);
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
//                    showMessage(TransactionSaldoActivity.this, "Harap Lengkapi Memilih Tanggal");
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
//        btnDownloadExcel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(TransactionSaldoActivity.this, SettingDownloadExcel.class);
//                intent.putExtra("mode", 1);
//                startActivity(intent);
//            }
//        });

        actionSearchView();
        fetchDataSaldoTransaction();
        setRecyclerViewListSaldoTransaction();
    }


    private void setRecyclerViewListSaldoTransaction() {
        binding.rvTransaction.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTransaction.setAdapter(adapter);

        adapter.setOnItemClickCallback(saldoTransaction -> {
            Intent intent = new Intent(this, DetailTransactionSaldoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(EXTRAS_SALDO_TRANSACTION, saldoTransaction);
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
                        fetchDataSaldoTransactionById(query);
                        binding.scId.clearFocus();
                    }
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals("")) {
                    fetchDataSaldoTransactionById(newText);
                }
                if (newText.length() == 0) {
                    fetchDataSaldoTransaction();
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


    public void fetchDataSaldoTransaction() {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        databaseReference.child(SALDO_TRANSACTION).orderByChild(TYPE_TRANSACTION).equalTo(WITHDRAW).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SaldoTransaction saldoTransactionResult = dataSnapshot.getValue(SaldoTransaction.class);
                    if (saldoTransactionResult != null) {
                        listSaldoTransaction.add(saldoTransactionResult);
                    }
                }
                showPlaceholderOrRecyclerView(listSaldoTransaction.size() > 0);
                adapter.setAdapter(listSaldoTransaction);
                listSaldoTransaction.clear();
                loading.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loading.dismiss();
            }
        });
    }

    private void fetchDataSaldoTransactionByDate(long start, long end) {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        databaseReference.child(ITEM_TRANSACTION).orderByChild(DATE).startAt(start).endAt(end).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SaldoTransaction saldoTransactionResult = dataSnapshot.getValue(SaldoTransaction.class);
                    if (saldoTransactionResult != null) {
                        listSaldoTransaction.add(saldoTransactionResult);
                    }
                }
                adapter.setAdapter(listSaldoTransaction);
                showPlaceholderOrRecyclerView(listSaldoTransaction.size() > 0);
                listSaldoTransaction.clear();
                loading.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loading.dismiss();
            }
        });
    }


    private void fetchDataSaldoTransactionById(String q) {
        databaseReference.child(SALDO_TRANSACTION).orderByChild(TYPE_TRANSACTION).equalTo(WITHDRAW).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SaldoTransaction saldoTransactionResult = dataSnapshot.getValue(SaldoTransaction.class);
                    if (saldoTransactionResult != null) {
                        if (saldoTransactionResult.getNo_saldo_transaction().toLowerCase().contains(q.toLowerCase())) {
                            listSaldoTransaction.add(saldoTransactionResult);

                        }
                    }
                }
                showPlaceholderOrRecyclerView(listSaldoTransaction.size() > 0);
                adapter.setAdapter(listSaldoTransaction);
                listSaldoTransaction.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        if(!etSearch.getText().toString().isEmpty()){
//            searchData(etSearch.getText().toString().toLowerCase());
//        }else if(!etTanggalMulai.getText().toString().isEmpty() && !etTanggalAkhir.getText().toString().isEmpty()){
//            loadDataFilterByDate(changeFormat(etTanggalMulai.getText().toString()), changeFormat(etTanggalAkhir.getText().toString()));
//        }else{
//            loadAllData();
//        }
//    }
//
//    private void loadAllData() {
//        Query transaksiQuery = databaseReference.orderByChild("tanggal_transaksi");
//        // Read from the database
//        if(getRegisterCode(sessionManagement.getUserSession()).toLowerCase().equals("n")){
//            transaksiQuery = databaseReference.orderByChild("id_nasabah").equalTo(sessionManagement.getUserSession());
//        }
//        transaksiQuery.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ArrayList<SaldoTransaction> list = new ArrayList<>();
//                for(DataSnapshot data : snapshot.getChildren()){
//                    SaldoTransaction value = data.getValue(SaldoTransaction.class);
////                    if(value.getJenis_transaksi().equals("TARIK")){
////                        list.add(value);
////                        if(getRegisterCode(sessionManagement.getUserSession()).toLowerCase().equals("t")){
//////                            if((!value.getStatus().equals("PENDING") && !value.getId_penerima().toLowerCase().equals(sessionManagement.getUserSession()))){
//////                                int lastIndex = list.size()-1;
//////                                list.remove(lastIndex);
//////                            }
////                        }
////                    }
//                }
//                transaksiSaldoAdapter = new TransaksiSaldoAdapter(TransactionSaldoActivity.this,list, listener);
//                recyclerView.setAdapter(transaksiSaldoAdapter);
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
//            transaksiQuery = databaseReference.orderByChild("id_nasabah").equalTo(sessionManagement.getUserSession());
//        }
//        transaksiQuery.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ArrayList<SaldoTransaction> list = new ArrayList<>();
//                for(DataSnapshot data : snapshot.getChildren()){
//                    SaldoTransaction value = data.getValue(SaldoTransaction.class);
////                    if(value.getJenis_transaksi().equals("TARIK")){
////                        list.add(value);
////                        if(getRegisterCode(sessionManagement.getUserSession()).toLowerCase().equals("t")){
//////                            if((!value.getStatus().equals("PENDING") && !value.getId_penerima().toLowerCase().equals(sessionManagement.getUserSession()))){
//////                                int lastIndex = list.size()-1;
//////                                list.remove(lastIndex);
//////                            }
////                        }
////                    }
//                }
//                transaksiSaldoAdapter = new TransaksiSaldoAdapter(TransactionSaldoActivity.this,list, listener);
//                recyclerView.setAdapter(transaksiSaldoAdapter);
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
//            transaksiQuery = databaseReference.orderByChild("id_nasabah").equalTo(sessionManagement.getUserSession());
//        }
//        transaksiQuery.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ArrayList<SaldoTransaction> list = new ArrayList<>();
//                for(DataSnapshot data : snapshot.getChildren()){
//                    SaldoTransaction value = data.getValue(SaldoTransaction.class);
////                    if(value.getJenis_transaksi().equals("TARIK") && value.getId_transaksi_saldo().toLowerCase().contains(str)){
////                        list.add(value);
////                        if(getRegisterCode(sessionManagement.getUserSession()).toLowerCase().equals("t")){
////                            if((!value.getStatus().equals("PENDING") && !value.getId_penerima().toLowerCase().equals(sessionManagement.getUserSession()))){
////                                int lastIndex = list.size()-1;
////                                list.remove(lastIndex);
////                            }
////                        }
////                    }
//                }
//                transaksiSaldoAdapter = new TransaksiSaldoAdapter(TransactionSaldoActivity.this,list, listener);
//                recyclerView.setAdapter(transaksiSaldoAdapter);
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
//    public interface MasterSaldoListener{
//        void onClickListTransaksiSaldo(SaldoTransaction saldoTransaction);
//    }
}
