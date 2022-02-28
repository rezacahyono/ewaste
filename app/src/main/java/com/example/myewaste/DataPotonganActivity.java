package com.example.myewaste;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myewaste.adapter.DataPotonganAdapter;
import com.example.myewaste.model.saldo.SaldoTransaction;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.myewaste.utils.Util.convertToRupiah;
import static com.example.myewaste.utils.Util.showMessage;

public class DataPotonganActivity extends AppCompatActivity {

    private RelativeLayout layoutFilterTanggal;
    private LinearLayout layoutTanggalMulai, layoutTanggalAkhir;
    private ImageView ivFilter;
    private EditText etSearch, etTanggalMulai, etTanggalAkhir;
    private ImageButton btnSearchFilter, closeFilter;

    private RecyclerView rvDataPotongan;
    private TextView tvTotalPotongan;
    private FloatingActionButton btnDownload;
    private DatabaseReference reference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_biaya_operasional);

        layoutFilterTanggal = findViewById(R.id.filterTanggal);
        layoutTanggalMulai = findViewById(R.id.layout_tanggal_start);
        layoutTanggalAkhir = findViewById(R.id.layout_tanggal_end);
        ivFilter = findViewById(R.id.ivFilter);
        etSearch = findViewById(R.id.etSearch);
        etTanggalMulai = findViewById(R.id.et_tanggalStart);
        etTanggalAkhir = findViewById(R.id.et_tanggalEnd);
        btnSearchFilter = findViewById(R.id.searchFilter);
        closeFilter = findViewById(R.id.cancelFilter);

        rvDataPotongan = findViewById(R.id.rv_list_potongan);
        tvTotalPotongan = findViewById(R.id.total_potongan_transaksi);
        btnDownload = findViewById(R.id.btnDownloadExclTotalPotongan);
        reference = FirebaseDatabase.getInstance().getReference();

        etTanggalMulai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogCalendar(etTanggalMulai, "Tanggal Mulai");
            }
        });

        layoutTanggalMulai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogCalendar(etTanggalMulai, "Tanggal Mulai");
            }
        });

        etTanggalAkhir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogCalendar(etTanggalAkhir, "Tanggal Akhir");
            }
        });

        layoutTanggalAkhir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogCalendar(etTanggalAkhir, "Tanggal Akhir");
            }
        });

        btnSearchFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!etTanggalMulai.getText().toString().isEmpty() && !etTanggalAkhir.getText().toString().isEmpty()) {
                    loadDataPotonganFilterByDate(changeFormat(etTanggalMulai.getText().toString()), changeFormat(etTanggalAkhir.getText().toString()));
                    closeFilter.setVisibility(View.VISIBLE);
                    btnSearchFilter.setVisibility(View.GONE);
                }else{
                    showMessage(DataPotonganActivity.this, "Harap Lengkapi Memilih Tanggal");
                }
            }
        });

        closeFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etTanggalMulai.setText("");
                etTanggalAkhir.setText("");
                closeFilter.setVisibility(View.GONE);
                btnSearchFilter.setVisibility(View.VISIBLE);
                loadAllDataPotongan();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.equals("")){
                    searchDataPotongan(String.valueOf(charSequence));
                }else{
                    if (!etTanggalMulai.getText().toString().isEmpty() && !etTanggalAkhir.getText().toString().isEmpty()) {
                        Log.d("TAG", "onStart: work");
                        loadDataPotonganFilterByDate(changeFormat(etTanggalMulai.getText().toString()), changeFormat(etTanggalAkhir.getText().toString()));
                    } else{
                        loadAllDataPotongan();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ivFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (layoutFilterTanggal.getVisibility() == View.GONE) {
                    layoutFilterTanggal.setVisibility(View.VISIBLE);
                } else {
                    layoutFilterTanggal.setVisibility(View.GONE);
                }
            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DataPotonganActivity.this, SettingDownloadExcel.class);
                intent.putExtra("mode", 2);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!etSearch.getText().toString().isEmpty()) {
            searchDataPotongan(etSearch.getText().toString().toLowerCase());
        } else if (!etTanggalMulai.getText().toString().isEmpty() && !etTanggalAkhir.getText().toString().isEmpty()) {
            Log.d("TAG", "onStart: work");
            loadDataPotonganFilterByDate(changeFormat(etTanggalMulai.getText().toString()), changeFormat(etTanggalAkhir.getText().toString()));
        } else if (reference != null) {
            loadAllDataPotongan();
        }
    }

    private void prepareRecycleView(ArrayList<SaldoTransaction> saldoTransactionArrayList) {
        rvDataPotongan.setLayoutManager(new LinearLayoutManager(this));
        DataPotonganAdapter dataPotonganAdapter = new DataPotonganAdapter(this, saldoTransactionArrayList);
        rvDataPotongan.setAdapter(dataPotonganAdapter);
    }

    private void loadAllDataPotongan() {
        ArrayList<SaldoTransaction> saldoTransactionArrayList = new ArrayList<>();
        Query transaksiSaldoQuery = reference.child("transaksi_saldo").orderByKey();
        transaksiSaldoQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int total = 0;
                    for (DataSnapshot data : snapshot.getChildren()) {
                        SaldoTransaction saldoTransaction = data.getValue(SaldoTransaction.class);
//                        if (saldoTransaction.getJenis_transaksi().equals("TARIK") && saldoTransaction.getStatus().equals("APPROVED")) {
//                            saldoTransactionArrayList.add(saldoTransaction);
////                            total += saldoTransaction.getPotongan();
//                        }
                    }
                    prepareRecycleView(saldoTransactionArrayList);
                    tvTotalPotongan.setText(convertToRupiah(total));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadDataPotonganFilterByDate(long start, long end) {
        ArrayList<SaldoTransaction> saldoTransactionArrayList = new ArrayList<>();
        Query transaksiSaldoQuery = reference.child("transaksi_saldo").orderByChild("tanggal_transaksi").startAt(start).endAt(end);
        transaksiSaldoQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int total = 0;
                    for (DataSnapshot data : snapshot.getChildren()) {
                        SaldoTransaction saldoTransaction = data.getValue(SaldoTransaction.class);
//                        if (saldoTransaction.getJenis_transaksi().equals("TARIK") && saldoTransaction.getStatus().equals("APPROVED")) {
                            saldoTransactionArrayList.add(saldoTransaction);
//                            total += saldoTransaction.getPotongan();
//                        }
                    }
                    prepareRecycleView(saldoTransactionArrayList);
//                    tvTotalPotongan.setText(convertToRupiah(total));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchDataPotongan(String str) {
        ArrayList<SaldoTransaction> saldoTransactionArrayList = new ArrayList<>();
        Query transaksiSaldoQuery = reference.child("transaksi_saldo");
        transaksiSaldoQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int total = 0;
                    for (DataSnapshot data : snapshot.getChildren()) {
                        SaldoTransaction saldoTransaction = data.getValue(SaldoTransaction.class);
//                        if (saldoTransaction.getJenis_transaksi().equals("TARIK") && saldoTransaction.getStatus().equals("APPROVED")) {
//                            if(saldoTransaction.getId_transaksi_saldo().toLowerCase().contains(str)){
//                                saldoTransactionArrayList.add(saldoTransaction);
//                                total += saldoTransaction.getPotongan();
//                            }
//                        }
                    }
                    prepareRecycleView(saldoTransactionArrayList);
                    tvTotalPotongan.setText(convertToRupiah(total));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showDialogCalendar(View AttachTo, String title) {
        final BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_dialog_edit_user);

        TextView titleDialog = dialog.findViewById(R.id.tvTitleDialog);
        Button btnBatal = dialog.findViewById(R.id.btnDialogBatal);
        Button btnSimpan = dialog.findViewById(R.id.btnDialogSimpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        LinearLayout target = dialog.findViewById(R.id.frameEditData);
        titleDialog.setText(title);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View child = inflater.inflate(R.layout.frame_calendar, null);
        CalendarView calendar = child.findViewById(R.id.calendarView);
        target.addView(child);
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                ((TextView) AttachTo).setText(day + "-" + (month + 1) + "-" + year);
            }
        });

        dialog.show();
    }

    private long changeFormat(String oldDateString){
        final String OLD_FORMAT = "dd-MM-yyyy HH:mm:ss";
        long millisecond = 0;

        SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
        Date d = null;
        try {
            d = sdf.parse(oldDateString+ " 12:0:0");
            millisecond = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return millisecond;
    }
}
