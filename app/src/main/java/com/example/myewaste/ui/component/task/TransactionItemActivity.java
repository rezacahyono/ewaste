package com.example.myewaste.ui.component.task;

import static com.example.myewaste.utils.Constant.DATE;
import static com.example.myewaste.utils.Constant.EXTRAS_ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Constant.FORMATE_EXCEL;
import static com.example.myewaste.utils.Constant.ITEM_MASTER;
import static com.example.myewaste.utils.Constant.ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.ITEM_TYPE;
import static com.example.myewaste.utils.Constant.NASABAH;
import static com.example.myewaste.utils.Constant.NONE;
import static com.example.myewaste.utils.Constant.NO_ITEM_MASTER;
import static com.example.myewaste.utils.Constant.NO_ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.NO_ITEM_TYPE;
import static com.example.myewaste.utils.Constant.NO_REGIS;
import static com.example.myewaste.utils.Constant.NO_UNI_ITEM;
import static com.example.myewaste.utils.Constant.REQUEST_CODE;
import static com.example.myewaste.utils.Constant.UNIT_ITEM;
import static com.example.myewaste.utils.Constant.USER_DATA;
import static com.example.myewaste.utils.Utils.convertDateAndTime;
import static com.example.myewaste.utils.Utils.convertToRupiah;
import static com.example.myewaste.utils.Utils.getRegisterCode;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.SECOND;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myewaste.R;
import com.example.myewaste.adapter.ListItemTransactionAdapter;
import com.example.myewaste.adapter.ListSaldoTransactionAdapter;
import com.example.myewaste.databinding.ActivityTransactionBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.item.Item;
import com.example.myewaste.model.item.ItemMaster;
import com.example.myewaste.model.item.ItemTransaction;
import com.example.myewaste.model.item.ItemType;
import com.example.myewaste.model.item.UnitItem;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.model.utils.ListItem;
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

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class TransactionItemActivity extends AppCompatActivity {

    private ProgressDialog loading;

    private ArrayList<ItemTransaction> listItemTransaction;
    private ArrayList<ArrayList<ListItem>> listItemss;
    private ArrayList<UserData> listUserData;

    private DatabaseReference databaseReference;
    private ListItemTransactionAdapter adapter;

    private MaterialDatePicker.Builder<Long> dateBuilder;
    private MaterialDatePicker<Long> datePicker;
    private SessionManagement sessionManagement;
    private NotificationUtils notificationUtils;

    private String user = "";
    private long startDate = 0L;
    private long endDate = 0L;

    private boolean flShow = true;
    private MainToolbarBinding bindingToolbar;
    private ActivityTransactionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();
        bindingToolbar = binding.mainToolbar;
        bindingToolbar.tvTitleBar.setText(R.string.report_transaction_item);
        bindingToolbar.btnBack.setOnClickListener(view -> onBackPressed());

        databaseReference = FirebaseDatabase.getInstance().getReference();
        sessionManagement = new SessionManagement(this);

        notificationUtils = new NotificationUtils(this);

        adapter = new ListItemTransactionAdapter();
        binding.rvTransaction.setHasFixedSize(true);

        if (getIntent().getStringExtra(EXTRAS_USER_DATA) != null) {
            user = getIntent().getStringExtra(EXTRAS_USER_DATA);
        }


        if (!getRegisterCode(user).equalsIgnoreCase(NASABAH)) {
            bindingToolbar.btnTrash.setVisibility(View.VISIBLE);
            bindingToolbar.btnTrash.setImageResource(R.drawable.ic_download);
        } else {
            bindingToolbar.btnTrash.setVisibility(View.GONE);
        }

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
                fetchDataItemTransactionByDate(startDate, endDate);
                flShow = true;
            } else {
                Toast.makeText(this, getResources().getString(R.string.input_can_not_be_empty, "Tanggal"), Toast.LENGTH_SHORT).show();
            }
        });
        binding.ibCancel.setOnClickListener(v -> {
            fetchDataItemTransactionById("");
            binding.flFilter.setVisibility(View.GONE);
            flShow = true;
        });


        bindingToolbar.btnTrash.setOnClickListener(v -> openDocumentTree());

        fetchDataUserData();
        actionSearchView();
        fetchDataItemTransactionById("");
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

        adapter.setOnItemAddUser(new ListSaldoTransactionAdapter.OnItemAddUser() {
            @Override
            public void onAddDataNasabah(String noNasabah, TextView tvNoNasabah, TextView tvNameNasabah) {
                databaseReference.child(USER_DATA).orderByChild(NO_REGIS).equalTo(noNasabah).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            UserData userDataResult = dataSnapshot.getValue(UserData.class);
                            if (userDataResult != null) {
                                tvNoNasabah.setText(userDataResult.getNo_regis());
                                tvNameNasabah.setText(getResources().getString(R.string.field_name_nasabah, userDataResult.getName()));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onAddDataTeller(String noTeller, TextView tvNameTeller) {
                if (!noTeller.equals("-")) {
                    databaseReference.child(USER_DATA).orderByChild(NO_REGIS).equalTo(noTeller).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                UserData userDataResult = dataSnapshot.getValue(UserData.class);
                                if (userDataResult != null) {
                                    tvNameTeller.setText(getResources().getString(R.string.field_name_teller, userDataResult.getName()));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    tvNameTeller.setText(getResources().getString(R.string.field_name_teller, "-"));
                }
            }
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
                    fetchDataItemTransactionById("");
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

    private void fetchDataItemTransactionByDate(long start, long end) {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        databaseReference.child(ITEM_TRANSACTION).orderByChild(DATE).startAt(start).endAt(end).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listItemTransaction = new ArrayList<>();
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
                fetchListItem(listItemTransaction);
                showPlaceholderOrRecyclerView(listItemTransaction.size() > 0);
                loading.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loading.dismiss();
            }
        });
    }

    private void fetchDataItemTransactionById(String q) {
        databaseReference.child(ITEM_TRANSACTION).orderByChild(NO_ITEM_TRANSACTION).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listItemTransaction = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ItemTransaction itemTransactionResult = dataSnapshot.getValue(ItemTransaction.class);
                    if (itemTransactionResult != null) {
                        if (q.length() > 0) {
                            if (itemTransactionResult.getNo_item_transaction().toLowerCase().contains(q.toLowerCase())) {
                                if (!getRegisterCode(user).equalsIgnoreCase(NASABAH)) {
                                    listItemTransaction.add(itemTransactionResult);
                                } else {
                                    if (itemTransactionResult.getNo_nasabah().equalsIgnoreCase(user)) {
                                        listItemTransaction.add(itemTransactionResult);
                                    }
                                }
                            }
                        } else {
                            if (!getRegisterCode(user).equalsIgnoreCase(NASABAH)) {
                                listItemTransaction.add(itemTransactionResult);
                            } else {
                                if (itemTransactionResult.getNo_nasabah().equalsIgnoreCase(user)) {
                                    listItemTransaction.add(itemTransactionResult);
                                }
                            }
                        }
                    }
                }
                Collections.reverse(listItemTransaction);
                adapter.setAdapter(listItemTransaction);
                fetchListItem(listItemTransaction);
                showPlaceholderOrRecyclerView(listItemTransaction.size() > 0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void fetchListItem(@NonNull ArrayList<ItemTransaction> listItemTransaction) {
        if (listItemTransaction.size() > 0) {
            listItemss = new ArrayList<>();
            for (ItemTransaction itemTransaction : listItemTransaction) {
                ArrayList<ListItem> listItems = new ArrayList<>();
                for (Item item : itemTransaction.getItem_list()) {
                    databaseReference.child(ITEM_TYPE).orderByChild(NO_ITEM_TYPE).equalTo(item.getNo_type_item()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshotItemType : snapshot.getChildren()) {
                                ItemType itemTypeResult = dataSnapshotItemType.getValue(ItemType.class);
                                if (itemTypeResult != null) {
                                    databaseReference.child(ITEM_MASTER).orderByChild(NO_ITEM_MASTER).equalTo(itemTypeResult.getNo_item_master()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot dataSnapshotItemMaster : snapshot.getChildren()) {
                                                ItemMaster itemMasterResult = dataSnapshotItemMaster.getValue(ItemMaster.class);
                                                if (itemMasterResult != null) {
                                                    databaseReference.child(UNIT_ITEM).orderByChild(NO_UNI_ITEM).equalTo(itemTypeResult.getNo_unit_item()).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            ListItem listItem = new ListItem();
                                                            for (DataSnapshot dataSnapshotUnitItem : snapshot.getChildren()) {
                                                                UnitItem unitItemResult = dataSnapshotUnitItem.getValue(UnitItem.class);
                                                                if (unitItemResult != null) {
                                                                    listItem.setNameItem(itemMasterResult.getName());
                                                                    listItem.setNameItemType(itemTypeResult.getName());
                                                                    listItem.setPrice((int) (itemTypeResult.getPrice() * item.getTotal()));
                                                                    listItem.setTotal(item.getTotal());
                                                                    listItem.setNameUnit(unitItemResult.getName());
                                                                }
                                                            }
                                                            listItems.add(listItem);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                        }
                                                    });
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            loading.dismiss();
                        }
                    });
                }
                listItemss.add(listItems);
            }
        }
    }


    private void fetchDataUserData() {
        databaseReference.child(USER_DATA).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listUserData = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UserData userDataResult = dataSnapshot.getValue(UserData.class);
                    if (userDataResult != null) {
                        listUserData.add(userDataResult);
                    }
                }
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
            DocumentFile file = dir.createFile("application/vnd.ms-excel", getResources().getString(R.string.data_item_transaction) + convertDateAndTime(System.currentTimeMillis()) + FORMATE_EXCEL);
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
                HSSFSheet hssfSheet = hssfWorkbook.createSheet(getResources().getString(R.string.data_item_transaction));

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
                cellNameApp.setCellStyle(cellStyle);
                cellNameApp.setCellValue(getResources().getString(R.string.data_item_transaction) + " " + getResources().getString(R.string.ewaste_pch));
                hssfSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

                HSSFRow rowTitle = hssfSheet.createRow(1);

                HSSFCell cellTitleNoTransaction = rowTitle.createCell(0);
                cellTitleNoTransaction.setCellStyle(cellStyle);
                hssfSheet.setColumnWidth(0, 4000);
                cellTitleNoTransaction.setCellValue(getResources().getString(R.string.no_transaction));

                HSSFCell cellTitleDate = rowTitle.createCell(1);
                cellTitleDate.setCellStyle(cellStyle);
                hssfSheet.setColumnWidth(1, 8000);
                cellTitleDate.setCellValue(getResources().getString(R.string.date));

                HSSFCell cellTitleNameNasabah = rowTitle.createCell(2);
                cellTitleNameNasabah.setCellStyle(cellStyle);
                hssfSheet.setColumnWidth(2, 9000);
                cellTitleNameNasabah.setCellValue(getResources().getString(R.string.name_nasabah));

                HSSFCell cellTitleNameTeller = rowTitle.createCell(3);
                cellTitleNameTeller.setCellStyle(cellStyle);
                hssfSheet.setColumnWidth(3, 9000);
                cellTitleNameTeller.setCellValue(getResources().getString(R.string.name_teller));

                HSSFCell cellTitleNameItemMaster = rowTitle.createCell(4);
                cellTitleNameItemMaster.setCellStyle(cellStyle);
                hssfSheet.setColumnWidth(4, 7000);
                cellTitleNameItemMaster.setCellValue(getResources().getString(R.string.title_name_item_master));

                HSSFCell cellTitleNameItemType = rowTitle.createCell(5);
                cellTitleNameItemType.setCellStyle(cellStyle);
                hssfSheet.setColumnWidth(5, 7000);
                cellTitleNameItemType.setCellValue(getResources().getString(R.string.item_type));

                HSSFCell cellTitleNameUnitItem = rowTitle.createCell(6);
                cellTitleNameUnitItem.setCellStyle(cellStyle);
                hssfSheet.setColumnWidth(6, 4000);
                cellTitleNameUnitItem.setCellValue(getResources().getString(R.string.unit_item));

                HSSFCell cellTitleTotal = rowTitle.createCell(7);
                cellTitleTotal.setCellStyle(cellStyle);
                hssfSheet.setColumnWidth(7, 4000);
                cellTitleTotal.setCellValue(getResources().getString(R.string.total_scales));

                HSSFCell cellTitlePrice = rowTitle.createCell(8);
                cellTitlePrice.setCellStyle(cellStyle);
                hssfSheet.setColumnWidth(8, 7000);
                cellTitlePrice.setCellValue(getResources().getString(R.string.price));

                HSSFCell cellTitleTotalPrice = rowTitle.createCell(9);
                cellTitleTotalPrice.setCellStyle(cellStyle);
                hssfSheet.setColumnWidth(9, 7000);
                cellTitleTotalPrice.setCellValue(getResources().getString(R.string.total_price));

                int count = 0;
                for (int i = 0; i < listItemTransaction.size(); i++) {
                    if (listItemss.get(i).size() > 0) {
                        for (int j = 0; j < listItemss.get(i).size(); j++) {

                            HSSFRow rowData = hssfSheet.createRow(2 + count);

                            HSSFCell cellDataNoTransaction = rowData.createCell(0);
                            cellDataNoTransaction.setCellStyle(cellStyle);
                            cellDataNoTransaction.setCellValue(listItemTransaction.get(i).getNo_item_transaction());

                            HSSFCell cellDataDate = rowData.createCell(1);
                            cellDataDate.setCellStyle(cellStyle);
                            cellDataDate.setCellValue(convertDateAndTime(listItemTransaction.get(i).getDate()));

                            HSSFCell celLDataNameNasabah = rowData.createCell(2);
                            celLDataNameNasabah.setCellStyle(cellStyle);
                            celLDataNameNasabah.setCellValue(getNameUserData(listItemTransaction.get(i).getNo_nasabah()));

                            HSSFCell cellDataNameTeller = rowData.createCell(3);
                            cellDataNameTeller.setCellStyle(cellStyle);
                            cellDataNameTeller.setCellValue(getNameUserData(listItemTransaction.get(i).getNo_teller()));

                            HSSFCell cellDataItemMaster = rowData.createCell(4);
                            cellDataItemMaster.setCellStyle(cellStyle);
                            cellDataItemMaster.setCellValue(listItemss.get(i).get(j).getNameItem());

                            HSSFCell cellDataItemType = rowData.createCell(5);
                            cellDataItemType.setCellStyle(cellStyle);
                            cellDataItemType.setCellValue(listItemss.get(i).get(j).getNameItemType());

                            HSSFCell cellDataUniItem = rowData.createCell(6);
                            cellDataUniItem.setCellStyle(cellStyle);
                            cellDataUniItem.setCellValue(listItemss.get(i).get(j).getNameUnit());

                            HSSFCell cellDataTotalItem = rowData.createCell(7);
                            cellDataTotalItem.setCellStyle(cellStyle);
                            cellDataTotalItem.setCellValue(listItemss.get(i).get(j).getTotal());

                            HSSFCell cellDataPrice = rowData.createCell(8);
                            cellDataPrice.setCellStyle(cellStyle);
                            int pricePerItem = (int) (listItemss.get(i).get(j).getPrice() / listItemss.get(i).get(j).getTotal());
                            cellDataPrice.setCellValue(convertToRupiah(pricePerItem));

                            HSSFCell cellDataTotalPrice = rowData.createCell(9);
                            cellDataTotalPrice.setCellStyle(cellStyle);
                            cellDataTotalPrice.setCellValue(convertToRupiah(listItemss.get(i).get(j).getPrice()));

                            count++;
                        }
                    }
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
                Toast.makeText(this, getResources().getString(R.string.failure) + " download", Toast.LENGTH_SHORT).show();
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

    private String getNameUserData(String noRegis) {
        String name = "";
        for (UserData userData : listUserData) {
            if (userData.getNo_regis().equalsIgnoreCase(noRegis)) {
                name = userData.getName();
                break;
            }
        }
        return name;
    }

}
