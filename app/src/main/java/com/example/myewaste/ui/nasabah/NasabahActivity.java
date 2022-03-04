package com.example.myewaste.ui.nasabah;

import static com.example.myewaste.utils.Constant.ACCEPTED;
import static com.example.myewaste.utils.Constant.DATE;
import static com.example.myewaste.utils.Constant.EXTRAS_ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.EXTRAS_SALDO;
import static com.example.myewaste.utils.Constant.EXTRAS_SALDO_TRANSACTION;
import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Constant.ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.MODE;
import static com.example.myewaste.utils.Constant.MODE_ADD;
import static com.example.myewaste.utils.Constant.NO_NASABAH;
import static com.example.myewaste.utils.Constant.NO_REGIS;
import static com.example.myewaste.utils.Constant.PENDING;
import static com.example.myewaste.utils.Constant.REJECTED;
import static com.example.myewaste.utils.Constant.SALDO_NASABAH;
import static com.example.myewaste.utils.Constant.SALDO_TRANSACTION;
import static com.example.myewaste.utils.Constant.USER_DATA;
import static com.example.myewaste.utils.Constant.WITHDRAW;
import static com.example.myewaste.utils.Utils.convertToRupiah;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.myewaste.R;
import com.example.myewaste.adapter.ListItemTransactionAdapter;
import com.example.myewaste.adapter.ListSaldoTransactionAdapter;
import com.example.myewaste.databinding.ActivityNasabahBinding;
import com.example.myewaste.model.item.ItemTransaction;
import com.example.myewaste.model.saldo.Saldo;
import com.example.myewaste.model.saldo.SaldoTransaction;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.ui.component.task.AddUpdateTransactionSaldoActivity;
import com.example.myewaste.ui.component.task.DetailTransactionItemActivity;
import com.example.myewaste.ui.component.task.DetailTransactionSaldoActivity;
import com.example.myewaste.ui.component.task.TransactionItemActivity;
import com.example.myewaste.ui.component.task.TransactionSaldoActivity;
import com.example.myewaste.ui.profile.ProfileUserActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class NasabahActivity extends AppCompatActivity {

    private UserData userData;
    private Saldo saldo;
    private ListItemTransactionAdapter adapterItem;
    private ListSaldoTransactionAdapter adapterSaldo;
    private DatabaseReference databaseReference;
    private ArrayList<ItemTransaction> listItemTransaction;
    private ArrayList<SaldoTransaction> listSaldoTransaction;

    private ActivityNasabahBinding binding;
    private long time = 0L;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNasabahBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        listItemTransaction = new ArrayList<>();
        listSaldoTransaction = new ArrayList<>();
        saldo = new Saldo();

        if (getIntent().getParcelableExtra(EXTRAS_USER_DATA) != null) {
            userData = getIntent().getParcelableExtra(EXTRAS_USER_DATA);
            loadDataUser(userData);
            fetchSaldoNasabah(userData.getNo_regis());
        }

        adapterItem = new ListItemTransactionAdapter();
        adapterSaldo = new ListSaldoTransactionAdapter();
        binding.rvTransactionItem.setHasFixedSize(true);
        binding.rvTransactionSaldo.setHasFixedSize(true);

        binding.ibWithdraw.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddUpdateTransactionSaldoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(MODE, MODE_ADD);;
            intent.putExtra(EXTRAS_USER_DATA, userData);
            intent.putExtra(EXTRAS_SALDO, saldo);
            startActivity(intent);
        });


        binding.ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileUserActivity.class);
            if (userData != null) {
                intent.putExtra(EXTRAS_USER_DATA, userData);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        binding.ibSeeAllItemTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionItemActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(EXTRAS_USER_DATA, userData.getNo_regis());
            startActivity(intent);
        });

        binding.ibSeeAllWithdraw.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionSaldoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(EXTRAS_USER_DATA, userData.getNo_regis());
            startActivity(intent);
        });

        fetchDataTotalItemTransaction();
        fetchDataTotalWitdhrawAccepted();
        fetchDataSaldoTransactionAndItemTransaction();
        setRecyclerView();


    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDataUserData();
    }


    private void setRecyclerView() {
        binding.rvTransactionSaldo.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTransactionItem.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvTransactionSaldo.setAdapter(adapterSaldo);
        binding.rvTransactionItem.setAdapter(adapterItem);

        adapterItem.setOnItemClickCallback(itemTransaction -> {
            Intent intent = new Intent(this, DetailTransactionItemActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(EXTRAS_ITEM_TRANSACTION, itemTransaction);
            startActivity(intent);
        });

        adapterSaldo.setOnItemClickCallback(saldoTransaction -> {
            Intent intent = new Intent(this, DetailTransactionSaldoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(EXTRAS_SALDO_TRANSACTION, saldoTransaction);
            startActivity(intent);
        });
    }

    private void showPlaceholderOrRecyclerViewSaldo(boolean isShowSaldo) {
        if (isShowSaldo) {
            binding.rvTransactionSaldo.setVisibility(View.VISIBLE);
            binding.ivPlaceholderEmptySaldo.setVisibility(View.INVISIBLE);
        } else {
            binding.rvTransactionSaldo.setVisibility(View.GONE);
            binding.ivPlaceholderEmptySaldo.setVisibility(View.VISIBLE);
        }
    }

    private void showPlaceholderOrRecyclerViewItem(boolean isShowItem) {
        if (isShowItem) {
            binding.rvTransactionItem.setVisibility(View.VISIBLE);
            binding.ivPlaceholderEmptyItem.setVisibility(View.INVISIBLE);
        } else {
            binding.rvTransactionItem.setVisibility(View.INVISIBLE);
            binding.ivPlaceholderEmptyItem.setVisibility(View.VISIBLE);
        }
    }

    private void loadDataUser(UserData userData) {
        binding.tvName.setText(getResources().getString(R.string.greet, userData.getName()));
        binding.tvNoRegis.setText(userData.getNo_regis());
        loadImage(userData.getAvatar());
    }

    private void loadImage(String image) {
        Glide.with(this)
                .load(image)
                .placeholder(R.drawable.ic_placeholder)
                .into(binding.ivAvatar);
    }

    private void fetchSaldoNasabah(String noRegis) {
        databaseReference.child(SALDO_NASABAH).orderByChild(NO_REGIS).equalTo(noRegis).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Saldo saldoResult = dataSnapshot.getValue(Saldo.class);
                    if (saldoResult != null) {
                        binding.tvSaldo.setText(convertToRupiah(saldoResult.getSaldo()));
                        saldo = saldoResult;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchDataUserData() {
        if (userData.getNo_regis() != null) {
            Query queryUserData = databaseReference.child(USER_DATA).orderByChild(NO_REGIS).equalTo(userData.getNo_regis());
            queryUserData.get()
                    .addOnCompleteListener(task -> {
                        DataSnapshot result = task.getResult();
                        if (result != null) {
                            for (DataSnapshot dataSnapshot : result.getChildren()) {
                                UserData userDataResult = dataSnapshot.getValue(UserData.class);
                                if (userDataResult != null) {
                                    userData = userDataResult;
                                    loadDataUser(userData);
                                }
                            }
                        }
                    });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void fetchDataSaldoTransactionAndItemTransaction() {
        if (userData.getNo_regis() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            time = calendar.getTime().getTime();

            databaseReference.child(SALDO_TRANSACTION).orderByChild(DATE).startAt(time).limitToLast(5).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    listSaldoTransaction = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        SaldoTransaction saldoTransactionResult = dataSnapshot.getValue(SaldoTransaction.class);
                        if (saldoTransactionResult != null) {
                            if (saldoTransactionResult.getNo_nasabah().equalsIgnoreCase(userData.getNo_regis()) && saldoTransactionResult.getType_transaction().equalsIgnoreCase(WITHDRAW)) {
                                listSaldoTransaction.add(saldoTransactionResult);
                            }
                        }
                    }
                    Collections.reverse(listSaldoTransaction);
                    adapterSaldo.setAdapter(listSaldoTransaction);
                    showPlaceholderOrRecyclerViewSaldo(listSaldoTransaction.size() > 0);
                    if (listSaldoTransaction.size() == 0) {
                        databaseReference.child(SALDO_TRANSACTION).orderByChild(NO_NASABAH).equalTo(userData.getNo_regis()).limitToLast(5).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    SaldoTransaction saldoTransactionResult = dataSnapshot.getValue(SaldoTransaction.class);
                                    if (saldoTransactionResult != null && saldoTransactionResult.getType_transaction().equalsIgnoreCase(WITHDRAW)) {
                                        listSaldoTransaction.add(saldoTransactionResult);
                                    }
                                }
                                Collections.reverse(listSaldoTransaction);
                                adapterSaldo.setAdapter(listSaldoTransaction);
                                showPlaceholderOrRecyclerViewSaldo(listSaldoTransaction.size() > 0);
                                listSaldoTransaction.clear();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    listSaldoTransaction.clear();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            databaseReference.child(ITEM_TRANSACTION).orderByChild(DATE).startAt(time).limitToLast(3).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ItemTransaction itemTransactionResult = dataSnapshot.getValue(ItemTransaction.class);
                        if (itemTransactionResult != null) {
                            if (itemTransactionResult.getNo_nasabah().equalsIgnoreCase(userData.getNo_regis())) {
                                listItemTransaction.add(itemTransactionResult);
                            }
                        }
                    }
                    Collections.reverse(listItemTransaction);
                    adapterItem.setAdapter(listItemTransaction);
                    showPlaceholderOrRecyclerViewItem(listItemTransaction.size() > 0);
                    if (listItemTransaction.size() == 0){
                        databaseReference.child(ITEM_TRANSACTION).orderByChild(NO_NASABAH).equalTo(userData.getNo_regis()).limitToLast(3).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    ItemTransaction itemTransactionResult = dataSnapshot.getValue(ItemTransaction.class);
                                    if (itemTransactionResult != null) {
                                        listItemTransaction.add(itemTransactionResult);
                                    }
                                }
                                Collections.reverse(listItemTransaction);
                                adapterItem.setAdapter(listItemTransaction);
                                showPlaceholderOrRecyclerViewItem(listItemTransaction.size() > 0);
                                listItemTransaction.clear();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    listItemTransaction.clear();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


    private void fetchDataTotalItemTransaction() {
        if (userData.getNo_regis() != null) {
            databaseReference.child(ITEM_TRANSACTION).orderByChild(NO_NASABAH).equalTo(userData.getNo_regis()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<ItemTransaction> list = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ItemTransaction itemTransactionResult = dataSnapshot.getValue(ItemTransaction.class);
                        if (itemTransactionResult != null) {
                            list.add(itemTransactionResult);
                        }
                    }
                    binding.tvTotalItemTransaction.setText(String.valueOf(list.size()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }


    private void fetchDataTotalWitdhrawAccepted() {
        if (userData.getNo_regis() != null) {
            databaseReference.child(SALDO_TRANSACTION).orderByChild(NO_NASABAH).equalTo(userData.getNo_regis()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<SaldoTransaction> listAll = new ArrayList<>();
                    ArrayList<SaldoTransaction> listAccepted = new ArrayList<>();
                    ArrayList<SaldoTransaction> listPending = new ArrayList<>();
                    ArrayList<SaldoTransaction> listRejected = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        SaldoTransaction saldoTransactionResult = dataSnapshot.getValue(SaldoTransaction.class);
                        if (saldoTransactionResult != null && saldoTransactionResult.getType_transaction().equalsIgnoreCase(WITHDRAW)) {
                            listAll.add(saldoTransactionResult);
                            if (saldoTransactionResult.getStatus().equalsIgnoreCase(ACCEPTED)) {
                                listAccepted.add(saldoTransactionResult);
                            }
                            if (saldoTransactionResult.getStatus().equalsIgnoreCase(PENDING)) {
                                listPending.add(saldoTransactionResult);
                            }
                            if (saldoTransactionResult.getStatus().equalsIgnoreCase(REJECTED)) {
                                listRejected.add(saldoTransactionResult);
                            }

                        }
                    }
                    binding.tvTotalWithdraw.setText(String.valueOf(listAll.size()));
                    binding.tvTotalAccepted.setText(String.valueOf(listAccepted.size()));
                    binding.tvTotalPending.setText(String.valueOf(listPending.size()));
                    binding.tvTotalRejected.setText(String.valueOf(listRejected.size()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}