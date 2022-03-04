package com.example.myewaste.ui.component.task;

import static com.example.myewaste.utils.Constant.DEFAULT_NO_TRANSACTION_SALDO;
import static com.example.myewaste.utils.Constant.EXTRAS_SALDO;
import static com.example.myewaste.utils.Constant.EXTRAS_SALDO_TRANSACTION;
import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Constant.MODE;
import static com.example.myewaste.utils.Constant.MODE_UPDATE;
import static com.example.myewaste.utils.Constant.NO_REGIS;
import static com.example.myewaste.utils.Constant.PENDING;
import static com.example.myewaste.utils.Constant.SALDO_NASABAH;
import static com.example.myewaste.utils.Constant.SALDO_TRANSACTION;
import static com.example.myewaste.utils.Constant.WITHDRAW;
import static com.example.myewaste.utils.Utils.convertToRupiah;
import static com.example.myewaste.utils.Utils.increseNumber;
import static com.example.myewaste.utils.Utils.parseCurrencyValue;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myewaste.R;
import com.example.myewaste.databinding.ActivityAddUpdateTransactionSaldoBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.saldo.Saldo;
import com.example.myewaste.model.saldo.SaldoTransaction;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.pref.SessionManagement;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AddUpdateTransactionSaldoActivity extends AppCompatActivity {


    private DatabaseReference databaseReference;
    private String mode;
    private Saldo saldo;
    private UserData userData;
    private SaldoTransaction saldoTransaction;
    private int lastTransaction = 0;
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

        databaseReference = FirebaseDatabase.getInstance().getReference();

        saldo = new Saldo();
        userData = new UserData();
        saldoTransaction = new SaldoTransaction();

        mode = getIntent().getStringExtra(MODE);

        if (getIntent().getParcelableExtra(EXTRAS_SALDO) != null) {
            saldo = getIntent().getParcelableExtra(EXTRAS_SALDO);
        }

        if (getIntent().getParcelableExtra(EXTRAS_USER_DATA) != null) {
            userData = getIntent().getParcelableExtra(EXTRAS_USER_DATA);
        }
        if (saldo.getNo_regis() != null && userData.getNo_regis() != null) {
            setDataSaldo(saldo, userData);
        }

        if (getIntent().getParcelableExtra(EXTRAS_SALDO_TRANSACTION) != null) {
            saldoTransaction = getIntent().getParcelableExtra(EXTRAS_SALDO_TRANSACTION);
            lastTransaction = (int) saldoTransaction.getTotal_income();
            binding.tvCutsTransaction.setText(convertToRupiah((int) saldoTransaction.getCuts_transaction()));
            int income = (int) (saldoTransaction.getTotal_income() - saldoTransaction.getCuts_transaction());
            binding.tvTotalIncome.setText(convertToRupiah(income));
            binding.edtWithdraw.setText(convertToRupiah((int) saldoTransaction.getTotal_income()));
        } else {
            fetchDataTransactionSaldo();
        }

        fetchDataSaldo(userData.getNo_regis());

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
                binding.tvTotalIncome.setText(convertToRupiah(income));
                binding.tvCutsTransaction.setText(convertToRupiah(cuts));

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
            if (withdraw > (saldo.getSaldo()+lastTransaction)) {
                Toast.makeText(this, getResources().getString(R.string.not_enough_balance), Toast.LENGTH_SHORT).show();
            } else if (withdraw <= 0) {
                Toast.makeText(this, getResources().getString(R.string.input_can_not_be_empty, "input"), Toast.LENGTH_SHORT).show();
            } else {
                if (!mode.equals(MODE_UPDATE)) {
                    Date date = new Date();
                    saldoTransaction.setType_transaction(WITHDRAW);
                    saldoTransaction.setStatus(PENDING);
                    saldoTransaction.setNo_nasabah(userData.getNo_regis());
                    saldoTransaction.setDate(date.getTime());
                }

                saldoTransaction.setTotal_income(withdraw);
                saldoTransaction.setCuts_transaction(cuts);
                saldoTransaction.setNo_teller("-");
                submitSaldoTransaction(saldoTransaction);
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
    private void fetchDataTransactionSaldo() {
        databaseReference.child(SALDO_TRANSACTION).orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            String noTransactionSaldo = DEFAULT_NO_TRANSACTION_SALDO;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.getKey() != null) {
                        noTransactionSaldo = increseNumber(dataSnapshot.getKey());
                    }
                }
                saldoTransaction.setNo_saldo_transaction(noTransactionSaldo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void submitSaldoTransaction(SaldoTransaction saldoTransaction) {
        DatabaseReference databaseReferenceSaldoNasabah = databaseReference.child(SALDO_NASABAH).child(userData.getNo_regis());
        DatabaseReference databaseReferenceTransactionSaldo = databaseReference.child(SALDO_TRANSACTION).child(saldoTransaction.getNo_saldo_transaction());
        databaseReferenceTransactionSaldo.setValue(saldoTransaction).addOnSuccessListener(unused -> databaseReferenceSaldoNasabah.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Saldo saldoResult = snapshot.getValue(Saldo.class);
                if (saldoResult != null) {
                    saldoResult.setSaldo(saldoResult.getSaldo() + lastTransaction);
                    saldoResult.setSaldo((int) (saldoResult.getSaldo() - saldoTransaction.getTotal_income()));
                    databaseReferenceSaldoNasabah.setValue(saldoResult);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));
    }

//    private void createTransaksiSaldo(int withdraw, int cuts, String noTransactionSaldo, int lastTransaction) {
//        saldoTransaction.setStatus(PENDING);
//        saldoTransaction.setNo_saldo_transaction(noTransactionSaldo);
//        saldoTransaction.setDate(System.currentTimeMillis());
//        saldoTransaction.setTotal_income(withdraw);
//        saldoTransaction.setType_transaction(WITHDRAW);
//        saldoTransaction.setCuts_transaction(cuts);
//        saldoTransaction.setNo_nasabah(userData.getNo_regis());
//        saldoTransaction.setNo_teller("none");
//
//        DatabaseReference databaseReferenceSaldoNasabah = databaseReference.child(SALDO_NASABAH).child(userData.getNo_regis());
//        DatabaseReference databaseReferenceTransactionSaldo = databaseReference.child(SALDO_TRANSACTION).child(noTransactionSaldo);
//        databaseReferenceTransactionSaldo.setValue(saldoTransaction).addOnSuccessListener(aVoid -> databaseReferenceSaldoNasabah.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Saldo saldoNasabahResult = snapshot.getValue(Saldo.class);
//                if (saldoNasabahResult != null) {
//                    int currentBalance = saldoNasabahResult.getSaldo();
//                    int newBalance = currentBalance - withdraw;
//                    saldoNasabahResult.setSaldo(newBalance);
//                    databaseReferenceSaldoNasabah.setValue(saldoNasabahResult);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        }));
//    }
}
