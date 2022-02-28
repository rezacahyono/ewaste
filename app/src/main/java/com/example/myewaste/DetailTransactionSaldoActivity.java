package com.example.myewaste;

import static com.example.myewaste.utils.Constant.ACCEPTED;
import static com.example.myewaste.utils.Constant.EXTRAS_SALDO_TRANSACTION;
import static com.example.myewaste.utils.Constant.NASABAH;
import static com.example.myewaste.utils.Constant.NO_REGIS;
import static com.example.myewaste.utils.Constant.PENDING;
import static com.example.myewaste.utils.Constant.USER_DATA;
import static com.example.myewaste.utils.Util.convertDate;
import static com.example.myewaste.utils.Util.convertToRupiah;
import static com.example.myewaste.utils.Util.getRegisterCode;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myewaste.databinding.ActivityDetailTransactionSaldoBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.saldo.SaldoTransaction;
import com.example.myewaste.model.user.UserData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class DetailTransactionSaldoActivity extends AppCompatActivity {
//    private LinearLayout layout;
//    private TextView tvIdTransaksi, tvNamaNasabah, tvNamaTeller, tvJumlahPenarikan, tvPotongan,tvTotal, tvStatus, tvTanggal;
//    private Button editTransaksi;
//    private Button cancelTransaksi;
//    private Button approveTransaksi;
//    private Button rejectTransaksi;
//    private SessionManagement sessionManagement;
//
//
//    private SaldoTransaction saldoTransaction;
//    private UserData dataTeller;
//    private UserData dataUser;
//    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();


    private DatabaseReference databaseReference;
    private SaldoTransaction saldoTransaction;
    private String nameNasabah = "";
    private String nameTeller = "";

    private String user = "";
    private boolean modePending = false;
    private SessionManagement sessionManagement;
    private ActivityDetailTransactionSaldoBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailTransactionSaldoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();
        MainToolbarBinding bindingToolbar = binding.mainToolbar;
        bindingToolbar.tvTitleBar.setText(R.string.details_transaction);
        bindingToolbar.btnBack.setOnClickListener(view -> onBackPressed());

        saldoTransaction = getIntent().getParcelableExtra(EXTRAS_SALDO_TRANSACTION);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        sessionManagement = new SessionManagement(this);

        if (saldoTransaction != null) {
            fetchUserData(saldoTransaction.getNo_nasabah(), saldoTransaction.getNo_teller());
            setStatus(saldoTransaction);
            setDataSaldoTransaction(saldoTransaction);
            modePending = saldoTransaction.getStatus().equalsIgnoreCase(PENDING);
        } else {
            saldoTransaction = new SaldoTransaction();
        }
        user = getRegisterCode(sessionManagement.getUserSession());


        if (!modePending) {
            binding.btnAccepted.setVisibility(View.GONE);
            binding.btnRejected.setVisibility(View.GONE);
        } else {
            binding.btnRejected.setVisibility(View.VISIBLE);
            binding.btnAccepted.setVisibility(View.VISIBLE);
            if (user.equalsIgnoreCase(NASABAH)) {
                binding.btnAccepted.setText(R.string.update_transaction);
                binding.btnRejected.setText(R.string.cancel_transaction);
            }
        }

//        getSupportActionBar().setTitle("Detail Transaksi Penarikan");
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        layout = findViewById(R.id.layout_detail_transaksi_saldo);
//        tvIdTransaksi = findViewById(R.id.tv_id_transaksi_dts);
//        tvNamaNasabah = findViewById(R.id.tv_nama_nasabah_dts);
//        tvNamaTeller = findViewById(R.id.tv_nama_teller_dts);
//        tvJumlahPenarikan = findViewById(R.id.tv_jumlah_penarikan_dts);
//        tvPotongan = findViewById(R.id.tv_jumlah_potongan_dts);
//        tvTotal = findViewById(R.id.tv_total_dts);
//        tvStatus = findViewById(R.id.tv_status_dts);
//        tvTanggal = findViewById(R.id.tv_tanggal_transaksi_dts);
//        editTransaksi = findViewById(R.id.btn_edit_dts);
//        cancelTransaksi = findViewById(R.id.btn_cancel_dts);
//        approveTransaksi = findViewById(R.id.btn_approve_dts);
//        rejectTransaksi = findViewById(R.id.btn_reject_dts);
//
//        saldoTransaction = getIntent().getParcelableExtra("EXTRA_TRANSAKSI_SALDO");
//        sessionManagement = new SessionManagement(getApplicationContext());
//
//        if(!saldoTransaction.getStatus().equals("PENDING") || getRegisterCode(sessionManagement.getUserSession()).toLowerCase().equals("sa")){
//            approveTransaksi.setVisibility(View.GONE);
//            rejectTransaksi.setVisibility(View.GONE);
//            editTransaksi.setVisibility(View.GONE);
//            cancelTransaksi.setVisibility(View.GONE);
//        }else{
//            if(getRegisterCode(sessionManagement.getUserSession()).toLowerCase().equals("n")){
//                approveTransaksi.setVisibility(View.GONE);
//                rejectTransaksi.setVisibility(View.GONE);
//            }else if(getRegisterCode(sessionManagement.getUserSession()).toLowerCase().equals("t")){
//                editTransaksi.setVisibility(View.GONE);
//                cancelTransaksi.setVisibility(View.GONE);
//            }
//        }
//
//        editTransaksi.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent  = new Intent(DetailTransactionSaldoActivity.this, AddUpdateTransactionSaldoActivity.class);
//                intent.putExtra("EXTRA_TRANSAKSI_SALDO", saldoTransaction);
//                startActivity(intent);
//            }
//        });
//
//        cancelTransaksi.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                cancelTransaction();
//            }
//        });
//
//        approveTransaksi.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                approveTransaction();
//            }
//        });
//
//        rejectTransaksi.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                rejectTransaction();
//            }
//        });
//
//        prepareLayout();
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


    private void setStatus(SaldoTransaction saldoTransaction) {
        if (saldoTransaction.getStatus().equalsIgnoreCase(ACCEPTED)) {
            binding.ivStatus.setImageResource(R.drawable.ic_accepted);
            binding.ivStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_accepted));
        } else if (saldoTransaction.getStatus().equalsIgnoreCase(PENDING)) {
            binding.ivStatus.setImageResource(R.drawable.ic_pending);
            binding.ivStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_pending));
        } else {
            binding.ivStatus.setImageResource(R.drawable.ic_rejected);
            binding.ivStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rejected));
        }
    }


    private void setDataSaldoTransaction(SaldoTransaction saldoTransaction) {
        binding.tvDate.setText(convertDate(saldoTransaction.getDate()));
        binding.tvNoTransaction.setText(saldoTransaction.getNo_saldo_transaction());
        binding.tvTotalWithdraw.setText(convertToRupiah((int) saldoTransaction.getTotal_income()));
        binding.tvTotalCutsTransaction.setText(convertToRupiah((int) saldoTransaction.getCuts_transaction()));
        int incomeClean = (int) (saldoTransaction.getTotal_income() - saldoTransaction.getCuts_transaction());
        binding.tvTotalIncome.setText(convertToRupiah(incomeClean));
    }



//    private void prepareLayout(){
//        tvIdTransaksi.setText(saldoTransaction.getId_transaksi_saldo());
//        tvJumlahPenarikan.setText(convertToRupiah(saldoTransaction.getJumlah_transaksi()));
//        tvPotongan.setText(convertToRupiah(saldoTransaction.getPotongan()));
//        tvTotal.setText(convertToRupiah(saldoTransaction.getJumlah_transaksi()- saldoTransaction.getPotongan()));
//        tvStatus.setText(saldoTransaction.getStatus());
//        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
//        Date date = new Date(saldoTransaction.getTanggal_transaksi());
//        String tanggalTransaksi = sdf.format(date);
//        tvTanggal.setText(tanggalTransaksi);
//        loadUserData(0, saldoTransaction.getId_nasabah());
//        if(saldoTransaction.getId_penerima().equals("none")){
//            tvNamaTeller.setText(saldoTransaction.getId_penerima());
//        }else{
//            loadUserData(1, saldoTransaction.getId_penerima());
//        }
//    }
//
//    private void loadUserData(int mode, String idUser){
//        //todo mode 0 for user , 1 for teller
//        Query userQuery = databaseReference.child("userdata").orderByChild("noregis").equalTo(idUser);
//        userQuery.addValueEventListener(new ValueEventListener() {
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
//                    tvNamaNasabah.setText(dataUser.getName());
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
//    private void cancelTransaction(){
//        DatabaseReference saldoReference = databaseReference.child("saldonasabah").child(saldoTransaction.getId_nasabah());
//        DatabaseReference transaksiSaldoReference = databaseReference.child("transaksi_saldo").child(saldoTransaction.getId_transaksi_saldo());
//        saldoReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Saldo saldoNasabah = snapshot.getValue(Saldo.class);
//                int currentBalance = saldoNasabah.getSaldo();
//                int retriveCanceledBalance = currentBalance + saldoTransaction.getJumlah_transaksi();
//                saldoNasabah.setSaldo(retriveCanceledBalance);
//                saldoReference.setValue(saldoNasabah);
//                transaksiSaldoReference.getRef().removeValue();
//                showMessage(DetailTransactionSaldoActivity.this, "Berhasil Membatalkan Penarikan Saldo");
//                finish();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    private void approveTransaction(){
//        DatabaseReference transaksiSaldoReference = databaseReference.child("transaksi_saldo").child(saldoTransaction.getId_transaksi_saldo());
//        saldoTransaction.setStatus("APPROVED");
//        saldoTransaction.setId_penerima(sessionManagement.getUserSession());
//        transaksiSaldoReference.setValue(saldoTransaction);
//        showMessage(DetailTransactionSaldoActivity.this, "Berhasil Menyetujui Penarikan");
//        finish();
//    }
//
//    private void rejectTransaction(){
//        DatabaseReference transaksiSaldoReference = databaseReference.child("transaksi_saldo").child(saldoTransaction.getId_transaksi_saldo());
//        DatabaseReference saldoReference = databaseReference.child("saldonasabah").child(saldoTransaction.getId_nasabah());
//        saldoTransaction.setStatus("REJECTED");
//        saldoTransaction.setId_penerima(sessionManagement.getUserSession());
//        transaksiSaldoReference.setValue(saldoTransaction).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                saldoReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        Saldo saldoNasabah = snapshot.getValue(Saldo.class);
//                        int currentBalance = saldoNasabah.getSaldo();
//                        saldoNasabah.setSaldo(currentBalance + saldoTransaction.getJumlah_transaksi());
//                        saldoReference.setValue(saldoNasabah);
//                        showMessage(DetailTransactionSaldoActivity.this, "Berhasil Menolak Penarikan");
//                        finish();
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
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
