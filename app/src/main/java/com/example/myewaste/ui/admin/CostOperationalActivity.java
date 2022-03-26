package com.example.myewaste.ui.admin;

import static com.example.myewaste.utils.Constant.ACCEPTED;
import static com.example.myewaste.utils.Constant.DATE;
import static com.example.myewaste.utils.Constant.FORMATE_EXCEL;
import static com.example.myewaste.utils.Constant.NONE;
import static com.example.myewaste.utils.Constant.REQUEST_CODE;
import static com.example.myewaste.utils.Constant.SALDO_TRANSACTION;
import static com.example.myewaste.utils.Constant.TYPE_TRANSACTION;
import static com.example.myewaste.utils.Constant.WITHDRAW;
import static com.example.myewaste.utils.Utils.convertDate;
import static com.example.myewaste.utils.Utils.convertDateAndTime;
import static com.example.myewaste.utils.Utils.convertToRupiah;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.SECOND;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myewaste.R;
import com.example.myewaste.adapter.ListCostOperationalAdapter;
import com.example.myewaste.databinding.ActivityTransactionBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.saldo.SaldoTransaction;
import com.example.myewaste.pref.SessionManagement;
import com.example.myewaste.utils.NotificationUtils;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CostOperationalActivity extends AppCompatActivity {

    private ProgressDialog loading;
    private DatabaseReference databaseReference;
    private MaterialDatePicker.Builder<Long> dateBuilder;
    private MaterialDatePicker<Long> datePicker;

    private ArrayList<SaldoTransaction> listSaldoTransaction;

    private double totalIncome;
    private long startDate = 0L;
    private long endDate = 0L;
    private boolean flShow = true;

    private SessionManagement sessionManagement;
    private NotificationUtils notificationUtils;

    private ListCostOperationalAdapter adapter;
    private MainToolbarBinding bindingToolbar;
    private ActivityTransactionBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();
        bindingToolbar = binding.mainToolbar;
        bindingToolbar.tvTitleBar.setText(R.string.operational_cost);

        bindingToolbar.btnBack.setOnClickListener(view -> onBackPressed());
        bindingToolbar.btnTrash.setVisibility(View.VISIBLE);
        bindingToolbar.btnTrash.setImageResource(R.drawable.ic_download);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        sessionManagement = new SessionManagement(this);
        notificationUtils = new NotificationUtils(this);

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

        binding.pickStartDate.setOnClickListener(v -> setDatePickerStart());

        binding.pickEndDate.setOnClickListener(v -> setDatePickerEnd());

        binding.ibFilter.setOnClickListener(v -> {
            if (startDate != 0L && endDate != 0L) {
                fetchDataSaldoTransactionByDate(startDate, endDate);
                flShow = true;
            } else {
                Toast.makeText(this, getResources().getString(R.string.input_can_not_be_empty, "Tanggal"), Toast.LENGTH_SHORT).show();
            }
        });

        binding.ibCancel.setOnClickListener(v -> {
            fetchDataSaldoTransactionBySearch("");
            binding.flFilter.setVisibility(View.GONE);
            flShow = true;
        });


        binding.viewTotalIncome.setVisibility(View.VISIBLE);
        binding.tvTitleTotalIncome.setVisibility(View.VISIBLE);
        binding.tvTotalIncome.setVisibility(View.VISIBLE);


        adapter = new ListCostOperationalAdapter();
        binding.rvTransaction.setHasFixedSize(true);

        bindingToolbar.btnTrash.setOnClickListener(v -> openDocumentTree());

        fetchDataCostOperation();
        fetchDataSaldoTransactionBySearch("");
        actionSearchView();
        setRecyclerView();
    }


    private void setRecyclerView() {
        binding.rvTransaction.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTransaction.setAdapter(adapter);
    }

    private void setDatePickerStart() {
        dateBuilder = MaterialDatePicker.Builder.datePicker();
        datePicker = dateBuilder.build();
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Date date = new Date();
            date.setTime(selection);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(SECOND, 0);
            calendar.set(MINUTE, 0);
            calendar.set(HOUR_OF_DAY, 0);
            date = calendar.getTime();
            binding.pickStartDate.setText(datePicker.getHeaderText());
            startDate = date.getTime();
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
            calendar.add(Calendar.DATE, 1);
            date = calendar.getTime();
            binding.pickEndDate.setText(datePicker.getHeaderText());
            endDate = date.getTime();
        });
    }

    private void showPlaceholderOrRecyclerView(boolean isShow) {
        if (isShow) {
            binding.rvTransaction.setVisibility(View.VISIBLE);
            bindingToolbar.btnTrash.setVisibility(View.VISIBLE);
            binding.ivPlaceholderEmpty.setVisibility(View.GONE);
            binding.tvTitle.setVisibility(View.GONE);
        } else {
            binding.rvTransaction.setVisibility(View.INVISIBLE);
            bindingToolbar.btnTrash.setVisibility(View.GONE);
            binding.ivPlaceholderEmpty.setVisibility(View.VISIBLE);
            binding.tvTitle.setVisibility(View.VISIBLE);
            binding.tvTitle.setText(getResources().getString(R.string.title_data_transaction_empty, "saldo"));
        }
    }

    public void actionSearchView() {
        binding.scId.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null) {
                    if (!query.isEmpty()) {
                        fetchDataSaldoTransactionBySearch(query);
                        binding.scId.clearFocus();
                    }
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals("")) {
                    fetchDataSaldoTransactionBySearch(newText);
                } else {
                    fetchDataSaldoTransactionBySearch("");
                }
                return false;
            }
        });
    }

    private void fetchDataSaldoTransactionBySearch(String q) {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        databaseReference.child(SALDO_TRANSACTION).orderByChild(TYPE_TRANSACTION).equalTo(WITHDRAW).limitToLast(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listSaldoTransaction = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SaldoTransaction saldoTransactionResult = dataSnapshot.getValue(SaldoTransaction.class);
                    if (saldoTransactionResult != null && saldoTransactionResult.getStatus().equalsIgnoreCase(ACCEPTED)) {
                        if (q.length() > 0) {
                            if (saldoTransactionResult.getNo_saldo_transaction().toLowerCase().contains(q.toLowerCase())) {
                                listSaldoTransaction.add(saldoTransactionResult);
                            }
                        } else {
                            listSaldoTransaction.add(saldoTransactionResult);
                        }
                    }
                }
                loading.dismiss();
                Collections.reverse(listSaldoTransaction);
                adapter.setAdapter(listSaldoTransaction);
                showPlaceholderOrRecyclerView(listSaldoTransaction.size() > 0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchDataSaldoTransactionByDate(long startDate, long endDate) {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        databaseReference.child(SALDO_TRANSACTION).orderByChild(DATE).startAt(startDate).endAt(endDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listSaldoTransaction = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SaldoTransaction saldoTransactionResult = dataSnapshot.getValue(SaldoTransaction.class);
                    if (saldoTransactionResult != null) {
                        if (saldoTransactionResult.getType_transaction().equalsIgnoreCase(WITHDRAW)) {
                            listSaldoTransaction.add(saldoTransactionResult);

                        }
                    }
                }
                loading.dismiss();
                Collections.reverse(listSaldoTransaction);
                adapter.setAdapter(listSaldoTransaction);
                showPlaceholderOrRecyclerView(listSaldoTransaction.size() > 0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loading.dismiss();
            }
        });
    }


    private void fetchDataCostOperation() {
        totalIncome = 0.0;
        binding.tvTotalIncome.setText(convertToRupiah((int) totalIncome));
        databaseReference.child(SALDO_TRANSACTION).orderByChild(TYPE_TRANSACTION).equalTo(WITHDRAW).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SaldoTransaction saldoTransactionResult = dataSnapshot.getValue(SaldoTransaction.class);
                    if (saldoTransactionResult != null) {
                        if (saldoTransactionResult.getStatus().equalsIgnoreCase(ACCEPTED)) {
                            totalIncome += saldoTransactionResult.getCuts_transaction();
                        }
                    }
                }
                binding.tvTotalIncome.setText(convertToRupiah((int) totalIncome));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void openDocumentTree() {
        String uriString = sessionManagement.getUriString();
        if (uriString.equalsIgnoreCase(NONE)) {
            askPermission();
        } else if (arePermissionGranted(uriString)) {
            makeDoc(Uri.parse(uriString));
        } else {
            askPermission();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            if (data != null) {
                Uri uriTree = data.getData();
                if (uriTree != null) {
                    if (Uri.decode(uriTree.toString()).endsWith(":")) {
                        Toast.makeText(this, "Cannot use root folder", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION ^ Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    getContentResolver().takePersistableUriPermission(uriTree, takeFlags);
                    sessionManagement.setUriString(uriTree.toString());

                    makeDoc(uriTree);
                }
            }
        }
    }

    private void makeDoc(Uri uriTree) {
        DocumentFile dir = DocumentFile.fromTreeUri(this, uriTree);
        if (dir == null || !dir.exists()) {
            releasePermissions(uriTree);
            Toast.makeText(this, "Folder deleted, please choose another!", Toast.LENGTH_SHORT).show();
            openDocumentTree();
        } else {
            DocumentFile file = dir.createFile("application/vnd.ms-excel", getResources().getString(R.string.operational_cost) + convertDateAndTime(System.currentTimeMillis()) + FORMATE_EXCEL);
            if (file != null && file.canWrite()) {
                writeReport(file.getUri());
            } else {
                Toast.makeText(this, "error write", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void writeReport(Uri uri) {
        if (uri != null) {
            try {
                HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
                HSSFSheet hssfSheet = hssfWorkbook.createSheet(getResources().getString(R.string.operational_cost));

                HSSFCellStyle cellStyle = hssfWorkbook.createCellStyle();
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                cellStyle.setBorderBottom(BorderStyle.THIN);
                cellStyle.setBorderTop(BorderStyle.THIN);
                cellStyle.setBorderLeft(BorderStyle.THIN);
                cellStyle.setBorderRight(BorderStyle.THIN);
                cellStyle.setWrapText(true);

                HSSFRow rowNameApp = hssfSheet.createRow(0);
                HSSFCell cellNameApp = rowNameApp.createCell(0);
                cellNameApp.setCellValue(getResources().getString(R.string.operational_cost) + " " + getResources().getString(R.string.ewaste_pch));
                cellNameApp.setCellStyle(cellStyle);
                hssfSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

                HSSFRow rowTitle = hssfSheet.createRow(1);

                HSSFCell cellTitleNoTransaction = rowTitle.createCell(0);
                cellTitleNoTransaction.setCellStyle(cellStyle);
                hssfSheet.setColumnWidth(0, 5000);
                cellTitleNoTransaction.setCellValue(getResources().getString(R.string.no_transaction));

                HSSFCell cellTitleDateTransaction = rowTitle.createCell(1);
                cellTitleDateTransaction.setCellStyle(cellStyle);
                hssfSheet.setColumnWidth(1, 6000);
                cellTitleDateTransaction.setCellValue(getResources().getString(R.string.date));

                HSSFCell cellTitleIncome = rowTitle.createCell(2);
                cellTitleIncome.setCellStyle(cellStyle);
                hssfSheet.setColumnWidth(2, 6000);
                cellTitleIncome.setCellValue(getResources().getString(R.string.income));

                for (int i = 0; i < listSaldoTransaction.size(); i++) {

                    HSSFRow rowData = hssfSheet.createRow(i + 2);

                    HSSFCell cellDataNoTransaction = rowData.createCell(0);
                    cellDataNoTransaction.setCellStyle(cellStyle);
                    cellDataNoTransaction.setCellValue(listSaldoTransaction.get(i).getNo_saldo_transaction());

                    HSSFCell cellDataDateTranscation = rowData.createCell(1);
                    cellDataDateTranscation.setCellStyle(cellStyle);
                    cellDataDateTranscation.setCellValue(convertDate(listSaldoTransaction.get(i).getDate()));

                    HSSFCell cellDataIncomeTransaction = rowData.createCell(2);
                    cellDataIncomeTransaction.setCellStyle(cellStyle);
                    cellDataIncomeTransaction.setCellValue(convertToRupiah((int) listSaldoTransaction.get(i).getCuts_transaction()));

                }
                FileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(uri, "w").getFileDescriptor();
                FileOutputStream fos = new FileOutputStream(fileDescriptor);
                hssfWorkbook.write(fos);

                fos.flush();
                fos.close();
                Toast.makeText(this, getResources().getString(R.string.success) + " download", Toast.LENGTH_SHORT).show();

                notificationUtils.sendNotificationBuilder(sessionManagement.getUriString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void releasePermissions(Uri uriTree) {
        int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION ^ Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        getContentResolver().releasePersistableUriPermission(uriTree, flags);
        sessionManagement.setUriString(NONE);
    }

    private void askPermission() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE);
    }

    private boolean arePermissionGranted(String uriString) {
        List<UriPermission> list = getContentResolver().getPersistedUriPermissions();
        for (int i = 0; i < list.size(); i++) {
            String persistedUriString = list.get(i).getUri().toString();
            if (persistedUriString.equalsIgnoreCase(uriString) && list.get(i).isWritePermission() && list.get(i).isReadPermission()) {
                return true;
            }
        }
        return false;
    }

}
