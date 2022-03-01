package com.example.myewaste.ui.component.task;

import static com.example.myewaste.utils.Constant.DATE;
import static com.example.myewaste.utils.Constant.EXTRAS_ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Constant.ITEM_MASTER;
import static com.example.myewaste.utils.Constant.ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.NASABAH;
import static com.example.myewaste.utils.Constant.NO_ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.NO_NASABAH;
import static com.example.myewaste.utils.Utils.getRegisterCode;

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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

public class TransactionItemActivity extends AppCompatActivity {
    private ProgressDialog loading;
    private ArrayList<ItemTransaction> listItemTransaction;
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

        listItemTransaction = new ArrayList<>();
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

        binding.pickStartDate.setOnClickListener(v -> setDatePickerStart());

        binding.pickEndDate.setOnClickListener(v -> setDatePickerEnd());

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
        } else {
            fetchDataItemTransaction();
        }
        setRecyclerViewListItemTransaction();

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
            Date date = new Date();
            date.setTime(selection);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE,1);
            date = calendar.getTime();
            binding.pickEndDate.setText(datePicker.getHeaderText());
            endDate = date.getTime();
        });
    }


    private void fetchDataItemTransaction() {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        databaseReference.child(ITEM_TRANSACTION).limitToLast(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ItemTransaction itemTransactionResult = dataSnapshot.getValue(ItemTransaction.class);
                    if (itemTransactionResult != null) {
                        listItemTransaction.add(itemTransactionResult);
                    }
                }
                Collections.reverse(listItemTransaction);
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
                Collections.reverse(listItemTransaction);
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
            databaseReference.child(ITEM_TRANSACTION).orderByChild(NO_NASABAH).equalTo(user).limitToLast(10).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ItemTransaction itemTransactionResult = dataSnapshot.getValue(ItemTransaction.class);
                        if (itemTransactionResult != null) {
                            listItemTransaction.add(itemTransactionResult);
                        }
                    }
                    Collections.reverse(listItemTransaction);
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
                Collections.reverse(listItemTransaction);
                adapter.setAdapter(listItemTransaction);
                showPlaceholderOrRecyclerView(listItemTransaction.size() > 0);
                listItemTransaction.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
