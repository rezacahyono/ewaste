package com.example.myewaste.ui.component.task;

import static com.example.myewaste.utils.Constant.DATE;
import static com.example.myewaste.utils.Constant.EWASTE;
import static com.example.myewaste.utils.Constant.EXTRAS_ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Constant.FORMATE_EXCEL;
import static com.example.myewaste.utils.Constant.ITEM_MASTER;
import static com.example.myewaste.utils.Constant.ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.ITEM_TYPE;
import static com.example.myewaste.utils.Constant.NASABAH;
import static com.example.myewaste.utils.Constant.NO_ITEM_MASTER;
import static com.example.myewaste.utils.Constant.NO_ITEM_TRANSACTION;
import static com.example.myewaste.utils.Constant.NO_ITEM_TYPE;
import static com.example.myewaste.utils.Constant.NO_UNI_ITEM;
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
import android.os.Bundle;
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
import com.example.myewaste.model.item.Item;
import com.example.myewaste.model.item.ItemMaster;
import com.example.myewaste.model.item.ItemTransaction;
import com.example.myewaste.model.item.ItemType;
import com.example.myewaste.model.item.UnitItem;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.model.utils.ListItem;
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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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


        bindingToolbar.btnTrash.setOnClickListener(v -> generateReportItemTransaction());

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


    private void generateReportItemTransaction() {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        if (listItemTransaction.size() > 0 && listItemss.size() > 0) {
            File filePath = new File(getExternalFilesDir(null) + File.separator + EWASTE);
            if (!filePath.exists()) {
                if (filePath.mkdir()) {
                    filePath = new File(filePath.getAbsolutePath() + File.separator + getResources().getString(R.string.data_item_transaction) + convertDateAndTime(System.currentTimeMillis()) + FORMATE_EXCEL);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.failure), Toast.LENGTH_SHORT).show();
                    loading.dismiss();
                }
            } else {
                filePath = new File(filePath.getAbsolutePath() + File.separator + getResources().getString(R.string.data_item_transaction) + convertDateAndTime(System.currentTimeMillis()) + FORMATE_EXCEL);
            }


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
                        cellDataTotalPrice.setCellValue(convertToRupiah((int) listItemss.get(i).get(j).getPrice()));

                        count++;
                    }

                }

            }

            try {
                if (!filePath.exists()) {
                    filePath.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                hssfWorkbook.write(fileOutputStream);

                fileOutputStream.flush();
                fileOutputStream.close();
                Toast.makeText(this, getResources().getString(R.string.success) + " download", Toast.LENGTH_SHORT).show();
                loading.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, getResources().getString(R.string.failure) + " download", Toast.LENGTH_SHORT).show();
                loading.dismiss();
            }

        } else {
            Toast.makeText(this, getResources().getString(R.string.data_not_found), Toast.LENGTH_SHORT).show();
        }
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
