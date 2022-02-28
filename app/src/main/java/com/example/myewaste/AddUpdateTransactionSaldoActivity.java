package com.example.myewaste;

import static com.example.myewaste.utils.Constant.DEFAULT_NO_TRANSACTION_SALOD;
import static com.example.myewaste.utils.Constant.EXTRAS_SALDO;
import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Constant.NO_REGIS;
import static com.example.myewaste.utils.Constant.PENDING;
import static com.example.myewaste.utils.Constant.SALDO_NASABAH;
import static com.example.myewaste.utils.Constant.SALDO_TRANSACTION;
import static com.example.myewaste.utils.Constant.WITHDRAW;
import static com.example.myewaste.utils.Util.convertToRupiah;
import static com.example.myewaste.utils.Util.increseNumber;
import static com.example.myewaste.utils.Util.parseCurrencyValue;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myewaste.databinding.ActivityAddUpdateTransactionSaldoBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.saldo.Saldo;
import com.example.myewaste.model.saldo.SaldoTransaction;
import com.example.myewaste.model.user.UserData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class AddUpdateTransactionSaldoActivity extends AppCompatActivity {

//    private TextView tvSaldo, tvPotongan, tvtotal;
//    private EditText jumlahPenarikan;
//    private Button btnDoPenarikan;
//    private SaldoTransaction saldoTransaction;
//    public static final String DEFAULT_KODE_TRANSAKSI_SALDO = "TRS-0001";
//    private DatabaseReference databaseReference;
//    private SessionManagement sessionManagement;
//    private int saldoNasabah = 0;
//    private int mode = 0; //mode 0 = tambah, 1 = edit;

    private DatabaseReference databaseReference;
    private Saldo saldo;
    private UserData userData;
    private SaldoTransaction saldoTransaction = new SaldoTransaction();
    private int income = 0;
    private int cuts = 0;

    private ActivityAddUpdateTransactionSaldoBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddUpdateTransactionSaldoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();
        MainToolbarBinding bindingToolbar = binding.mainToolbar;
        bindingToolbar.tvTitleBar.setText(R.string.withdraw);

        bindingToolbar.btnBack.setOnClickListener(v -> onBackPressed());

        saldo = getIntent().getParcelableExtra(EXTRAS_SALDO);
        userData = getIntent().getParcelableExtra(EXTRAS_USER_DATA);
        if (saldo != null && userData != null) {
            setDataSaldo(saldo, userData);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();

        fetchDataSaldo(userData.getNo_regis());


//        tvSaldo = findViewById(R.id.tv_saldo);
//        tvPotongan = findViewById(R.id.tv_potongan);
//        tvtotal = findViewById(R.id.tv_total_diterima);
//        jumlahPenarikan = findViewById(R.id.txt_jumlah_penarikan);
//        btnDoPenarikan = findViewById(R.id.btnLakukanPenarikan);
//        databaseReference = FirebaseDatabase.getInstance().getReference();
//        sessionManagement = new SessionManagement(getApplicationContext());
//
//        getSupportActionBar().setTitle("Tarik Saldo Saya");
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        if(getIntent().hasExtra("EXTRA_TRANSAKSI_SALDO")){
//            getSupportActionBar().setTitle("Edit Penarikan Nasabah");
//            mode = 1;
//            saldoTransaction = getIntent().getParcelableExtra("EXTRA_TRANSAKSI_SALDO");
////            jumlahPenarikan.setText(String.valueOf(saldoTransaction.getJumlah_transaksi()));
////            setPotonganAndTotal(saldoTransaction.getJumlah_transaksi());
//        }else{
////            saldoTransaction = new SaldoTransaction(no_saldo_transaction, no_nasabah, type_transaction, no_teller, status, total_income, cuts_transaction, date);
//        }
//        getSaldoNasabah();
//
//        btnDoPenarikan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(jumlahPenarikan.getText().toString().isEmpty()){
//                    showMessage(AddUpdateTransactionSaldoActivity.this, "Jumlah Tidak boleh Kosong");
//                }else if(Integer.valueOf(jumlahPenarikan.getText().toString()) > saldoNasabah || Integer.valueOf(jumlahPenarikan.getText().toString()) <= 0){
//                    showMessage(AddUpdateTransactionSaldoActivity.this, "Maaf Saldo Anda Tidak Cukup untuk melakukan penarikan tersebut");
//                }else{
//                    if(mode == 0){
//                        fetchDataTransaksiSaldo(Integer.valueOf(jumlahPenarikan.getText().toString()));
//                    }else{
////                        createTransaksiSaldo(Integer.valueOf(jumlahPenarikan.getText().toString()), saldoTransaction.getId_transaksi_saldo(), saldoTransaction.getJumlah_transaksi());
//                    }
//                    finish();
//                }
//            }
//        });
//
//        jumlahPenarikan.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if(!charSequence.equals("")){
//                    if(!jumlahPenarikan.getText().toString().isEmpty()){
//                        setPotonganAndTotal(Integer.valueOf(jumlahPenarikan.getText().toString()));
//                    }else{
//                        tvPotongan.setText(convertToRupiah(0));
//                        tvtotal.setText(convertToRupiah(0));
//                    }
//                }else{
//                    tvPotongan.setText(convertToRupiah(0));
//                    tvtotal.setText(convertToRupiah(0));
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });

        binding.edtWithdraw.addTextChangedListener(new TextWatcher() {
            public final NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            private final WeakReference<EditText> editTextWeakReference = new WeakReference<>(binding.edtWithdraw);

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                numberFormat.setMaximumFractionDigits(0);
                numberFormat.setRoundingMode(RoundingMode.FLOOR);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence == null || charSequence.length() < 0) {
                    return;
                }
                String c = String.valueOf(parseCurrencyValue(charSequence.toString()));
                cuts = (int) (Double.parseDouble(c) * 10) / 100;
                income = (int) Double.parseDouble(c) - cuts;
                binding.tvTotalIncome.setText(convertToRupiah((int) income));
                binding.tvCutsTransaction.setText(convertToRupiah((int) cuts));

            }


            @Override
            public void afterTextChanged(Editable editable) {
                EditText editText = editTextWeakReference.get();
                if (editText == null || editText.getText().toString().equals("")) {
                    return;
                }
                editText.removeTextChangedListener(this);

                BigDecimal parsed = parseCurrencyValue(editText.getText().toString());
                String formatted = numberFormat.format(parsed);

                editText.setText(formatted);
                editText.setSelection(formatted.length());
                editText.addTextChangedListener(this);
            }
        });

        binding.btnSlideWithdraw.setOnSlideCompleteListener(slideToActView -> {
            int withdraw = parseCurrencyValue(Objects.requireNonNull(binding.edtWithdraw.getText()).toString()).intValue();
            if (withdraw > saldo.getSaldo()) {
                Toast.makeText(this, getResources().getString(R.string.not_enough_balance), Toast.LENGTH_SHORT).show();
            } else if (withdraw <= 0) {
                Toast.makeText(this, getResources().getString(R.string.input_can_not_be_empty, "input"), Toast.LENGTH_SHORT).show();
            } else {
                fetchDataTransactionSaldo(withdraw, cuts);
            }
            binding.btnSlideWithdraw.resetSlider();
            binding.edtWithdraw.setText("");
        });
    }


    private void setDataSaldo(Saldo saldo, UserData userData) {
        binding.tvSaldo.setText(convertToRupiah(saldo.getSaldo()));
        binding.tvNameUser.setText(userData.getName());
    }


    private void fetchDataSaldo(String noRegis) {
        databaseReference.child(SALDO_NASABAH).orderByChild(NO_REGIS).equalTo(noRegis).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Saldo saldoResult = dataSnapshot.getValue(Saldo.class);
                    if (saldoResult != null) {
                        saldo = saldoResult;
                    }
                }
                setDataSaldo(saldo, userData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //    private void setPotonganAndTotal(int jumlahPenarikan){
//        int potongan = (jumlahPenarikan*10)/100;
//        int total = jumlahPenarikan - potongan;
//
//        tvPotongan.setText(convertToRupiah(potongan));
//        tvtotal.setText(convertToRupiah(total));
//    }
//
//    private void getSaldoNasabah(){
//        DatabaseReference saldoReference = databaseReference.child("saldonasabah").child(sessionManagement.getUserSession()).child("saldo");
//        saldoReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                saldoNasabah = snapshot.getValue(int.class);
//                if(mode == 0){
//                    tvSaldo.setText(convertToRupiah(snapshot.getValue(int.class)));
//                }else{
////                    tvSaldo.setText(convertToRupiah(snapshot.getValue(int.class)) + " + "+ convertToRupiah(saldoTransaction.getJumlah_transaksi()));
////                    saldoNasabah = saldoNasabah + saldoTransaction.getJumlah_transaksi();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.d(error.getMessage(), "onCancelled: ");
//            }
//        });
//    }
//
    private void fetchDataTransactionSaldo(int withdraw, int cuts) {
        databaseReference.child(SALDO_TRANSACTION).get().addOnCompleteListener(task -> {
            String noTransactionSaldo = DEFAULT_NO_TRANSACTION_SALOD;
            if (task.isSuccessful()) {
                DataSnapshot result = task.getResult();
                if (result != null) {
                    for (DataSnapshot dataSnapshot : result.getChildren()) {
                        String noGetKey = dataSnapshot.getKey();
                        if (noGetKey != null) {
                            noTransactionSaldo = increseNumber(noGetKey);
                        }
                    }
                }
                createTransaksiSaldo(withdraw, cuts, noTransactionSaldo, 0);
//                Log.d("TAG", "fetchDataTransactionSaldo: " + kodeTransaksiSaldo);
            }
        });
    }

    private void createTransaksiSaldo(int withdraw, int cuts, String noTransactionSaldo, int lastTransaction) {
        saldoTransaction.setStatus(PENDING);
        saldoTransaction.setNo_saldo_transaction(noTransactionSaldo);
        saldoTransaction.setDate(System.currentTimeMillis());
        saldoTransaction.setTotal_income(withdraw);
        saldoTransaction.setType_transaction(WITHDRAW);
        saldoTransaction.setCuts_transaction(cuts);
        saldoTransaction.setNo_nasabah(userData.getNo_regis());
        saldoTransaction.setNo_teller("none");

        DatabaseReference databaseReferenceSaldoNasabah = databaseReference.child(SALDO_NASABAH).child(userData.getNo_regis());
        DatabaseReference databaseReferenceTransactionSaldo = databaseReference.child(SALDO_TRANSACTION).child(noTransactionSaldo);
        databaseReferenceTransactionSaldo.setValue(saldoTransaction).addOnSuccessListener(aVoid -> databaseReferenceSaldoNasabah.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Saldo saldoNasabahResult = snapshot.getValue(Saldo.class);
                if (saldoNasabahResult != null) {
                    int currentBalance = saldoNasabahResult.getSaldo();
                    int newBalance = currentBalance - withdraw;
                    saldoNasabahResult.setSaldo(newBalance);
                    databaseReferenceSaldoNasabah.setValue(saldoNasabahResult);
                }

//                if(mode == 0){
//                    int newBalance = currentBalance - jumlahPenarikan;
//                    saldoNasabah.setSaldo(newBalance);
//                    saldoReference.setValue(saldoNasabah);
//                    showMessage(AddUpdateTransactionSaldoActivity.this, "Berhasil Meminta Penarikan Harap Tunggu Di Approve oleh admin");
//                }else{
//                    currentBalance = currentBalance + lastTransaction;
//                    int newBalance = currentBalance - jumlahPenarikan;
//                    saldoNasabah.setSaldo(newBalance);
//                    saldoReference.setValue(saldoNasabah);
//                    showMessage(AddUpdateTransactionSaldoActivity.this, "Berhasil Meminta Penarikan Harap Tunggu Di Approve oleh admin");
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));
    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed();
//        return true;
//    }
}
