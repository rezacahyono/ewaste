package com.example.myewaste.ui.nasabah;

import static com.example.myewaste.utils.Constant.ACCEPTED;
import static com.example.myewaste.utils.Constant.EXTRAS_ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.EXTRAS_SALDO;
import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Constant.ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.NO_NASABAH;
import static com.example.myewaste.utils.Constant.NO_REGIS;
import static com.example.myewaste.utils.Constant.PENDING;
import static com.example.myewaste.utils.Constant.REJECTED;
import static com.example.myewaste.utils.Constant.SALDO_NASABAH;
import static com.example.myewaste.utils.Constant.SALDO_TRANSACTION;
import static com.example.myewaste.utils.Constant.USER_DATA;
import static com.example.myewaste.utils.Constant.WITHDRAW;
import static com.example.myewaste.utils.Util.convertToRupiah;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.myewaste.AddUpdateTransactionSaldoActivity;
import com.example.myewaste.DetailTransactionItemActivity;
import com.example.myewaste.R;
import com.example.myewaste.adapter.ListItemTransactionAdapter;
import com.example.myewaste.adapter.ListSaldoTransactionAdapter;
import com.example.myewaste.databinding.ActivityNasabahBinding;
import com.example.myewaste.model.item.ItemTransaction;
import com.example.myewaste.model.saldo.Saldo;
import com.example.myewaste.model.saldo.SaldoTransaction;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.ui.ProfileUserActivity;
import com.example.myewaste.ui.admin.task.TransactionItemActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class NasabahActivity extends AppCompatActivity {

    private UserData userData;
    private Saldo saldo;
    private ListItemTransactionAdapter adapterItem;
    private ListSaldoTransactionAdapter adapterSaldo;
    private DatabaseReference databaseReference;
    private ArrayList<ItemTransaction> listItemTransaction = new ArrayList<>();
    private ArrayList<SaldoTransaction> listSaldoTransaction = new ArrayList<>();

    private ActivityNasabahBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNasabahBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();

        databaseReference = FirebaseDatabase.getInstance().getReference();
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
            Toast.makeText(this, "withdraw", Toast.LENGTH_SHORT).show();
        });


//        Log.d("TAG", "onCreate: "+userData.getNo_regis());

//
//        intentLaunch = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    userData = result.getData().getParcelableExtra(ProfileUserActivity.DEFAULT_EXTRAS_NAME);
//                    loadData();
//                });
//
//        ivFotoProfil.setOnClickListener(view -> {
//            Intent intent = new Intent(NasabahActivity.this, ProfileUserActivity.class);
//            intent.putExtra(DEFAULT_EXTRAS_NAME, userData);
//            intentLaunch.launch(intent);
//        });
//
//        cv_history_transaksi.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(NasabahActivity.this, TransactionItemActivity.class));
//            }
//        });
//
//        cv_laporan_saldo_nasabah.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(NasabahActivity.this, TransactionSaldoActivity.class));
//            }
//        });
//
//        logout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                sessionManagement.removeUserSession();
//                Intent keluar = new Intent(NasabahActivity.this, LoginActivitiy.class);
//                keluar.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(keluar);
//                finish();
//            }
//        });
//
//        btnPenarikan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(NasabahActivity.this, AddUpdateTransactionSaldoActivity.class));
//            }
//        });

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


    private void fetchDataSaldoTransactionAndItemTransaction() {
        if (userData.getNo_regis() != null) {
            databaseReference.child(SALDO_TRANSACTION).orderByChild(NO_NASABAH).equalTo(userData.getNo_regis()).limitToLast(5).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        SaldoTransaction saldoTransactionResult = dataSnapshot.getValue(SaldoTransaction.class);
                        if (saldoTransactionResult != null && saldoTransactionResult.getType_transaction().equalsIgnoreCase(WITHDRAW)) {
                            listSaldoTransaction.add(saldoTransactionResult);
                        }
                    }
                    adapterSaldo.setAdapter(listSaldoTransaction);
                    showPlaceholderOrRecyclerViewSaldo(listSaldoTransaction.size() > 0);
                    listSaldoTransaction.clear();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            databaseReference.child(ITEM_TRANSACTION).orderByChild(NO_NASABAH).equalTo(userData.getNo_regis()).limitToLast(5).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ItemTransaction itemTransactionResult = dataSnapshot.getValue(ItemTransaction.class);
                        if (itemTransactionResult != null) {
                            listItemTransaction.add(itemTransactionResult);
                        }
                    }
                    adapterItem.setAdapter(listItemTransaction);
                    showPlaceholderOrRecyclerViewItem(listItemTransaction.size() > 0);
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
                            listAccepted.add(saldoTransactionResult);
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