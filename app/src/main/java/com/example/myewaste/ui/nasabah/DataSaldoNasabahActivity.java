package com.example.myewaste.ui.nasabah;

import static com.example.myewaste.utils.Constant.FORMATE_EXCEL;
import static com.example.myewaste.utils.Constant.NAME;
import static com.example.myewaste.utils.Constant.NASABAH;
import static com.example.myewaste.utils.Constant.NONE;
import static com.example.myewaste.utils.Constant.NO_REGIS;
import static com.example.myewaste.utils.Constant.REQUEST_CODE;
import static com.example.myewaste.utils.Constant.SALDO_NASABAH;
import static com.example.myewaste.utils.Constant.USER_DATA;
import static com.example.myewaste.utils.Utils.convertDateAndTime;
import static com.example.myewaste.utils.Utils.convertToRupiah;
import static com.example.myewaste.utils.Utils.getRegisterCode;

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
import com.example.myewaste.adapter.ListSaldoNasabahAdapter;
import com.example.myewaste.databinding.ActivityDataUserBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.saldo.Saldo;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.pref.SessionManagement;
import com.example.myewaste.utils.NotificationUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DataSaldoNasabahActivity extends AppCompatActivity {

    private ArrayList<UserData> listUserData;
    private ArrayList<Saldo> listSaldo;
    private Saldo saldo;
    private DatabaseReference databaseReference;

    private NotificationUtils notificationUtils;
    private SessionManagement sessionManagement;

    private ListSaldoNasabahAdapter adapter;
    private MainToolbarBinding bindingToolbar;
    private ActivityDataUserBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDataUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();

        bindingToolbar = binding.mainToolbar;
        bindingToolbar.tvTitleBar.setText(R.string.data_saldo_nasabah);
        bindingToolbar.btnBack.setOnClickListener(view -> onBackPressed());
        bindingToolbar.btnTrash.setImageResource(R.drawable.ic_download);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        sessionManagement = new SessionManagement(this);
        notificationUtils = new NotificationUtils(this);

        saldo = new Saldo();
        adapter = new ListSaldoNasabahAdapter();
        binding.rvUser.setHasFixedSize(true);

        fetchDataUserBySearch("");
        actionSearchView();
        setRecylcerView();


        bindingToolbar.btnTrash.setOnClickListener(v -> openDocumentTree());
    }

    private void showPlaceholderOrRecyclerView(boolean isShow) {
        if (isShow) {
            binding.rvUser.setVisibility(View.VISIBLE);
            bindingToolbar.btnTrash.setVisibility(View.VISIBLE);
            binding.ivPlaceholderEmpty.setVisibility(View.GONE);
            binding.tvTitle.setVisibility(View.GONE);
        } else {
            binding.rvUser.setVisibility(View.GONE);
            bindingToolbar.btnTrash.setVisibility(View.GONE);
            binding.ivPlaceholderEmpty.setVisibility(View.VISIBLE);
            binding.tvTitle.setVisibility(View.VISIBLE);
        }
    }


    private void setRecylcerView() {
        binding.rvUser.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUser.setAdapter(adapter);


        adapter.setOnItemAction((userData, tvSaldo) -> databaseReference.child(SALDO_NASABAH).orderByChild(NO_REGIS).equalTo(userData.getNo_regis()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Saldo saldoResult = dataSnapshot.getValue(Saldo.class);
                    if (saldoResult != null) {
                        saldo = saldoResult;
                        listSaldo.add(saldo);
                    }
                }
                tvSaldo.setText(convertToRupiah(saldo.getSaldo()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));
    }

    public void actionSearchView() {
        binding.scUser.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null) {
                    if (!query.isEmpty()) {
                        fetchDataUserBySearch(query);
                        binding.scUser.clearFocus();
                    }
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals("")) {
                    fetchDataUserBySearch(newText);
                } else {
                    fetchDataUserBySearch("");
                }
                return false;
            }
        });
    }

    public void fetchDataUserBySearch(String q) {
        DatabaseReference refUserData = databaseReference.child(USER_DATA);
        Query queryUserData = refUserData.orderByChild(NAME);
        queryUserData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listUserData = new ArrayList<>();
                listSaldo = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    UserData userData = data.getValue(UserData.class);
                    if (userData != null) {
                        if (getRegisterCode(userData.getNo_regis()).equalsIgnoreCase(NASABAH)) {
                            if (q.length() > 0) {
                                if (userData.getName().toLowerCase().contains(q.toLowerCase()) || userData.getNo_regis().toLowerCase().contains(q.toLowerCase())) {
                                    listUserData.add(userData);
                                }
                            } else {
                                listUserData.add(userData);
                            }
                        }
                    }
                }
                adapter.setAdapter(listUserData);
                showPlaceholderOrRecyclerView(listUserData.size() > 0);
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
            DocumentFile file = dir.createFile("application/vnd.ms-excel", getResources().getString(R.string.data_saldo_nasabah) + convertDateAndTime(System.currentTimeMillis()) + FORMATE_EXCEL);
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
                HSSFSheet hssfSheet = hssfWorkbook.createSheet(SALDO_NASABAH);

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
                cellNameApp.setCellValue(getResources().getString(R.string.data_saldo_nasabah) + " " + getResources().getString(R.string.ewaste_pch));
                cellNameApp.setCellStyle(cellStyle);
                hssfSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

                HSSFRow rowTitle = hssfSheet.createRow(1);

                HSSFCell cellTitleNoRegis = rowTitle.createCell(0);
                cellTitleNoRegis.setCellStyle(cellStyle);
                hssfSheet.setColumnWidth(0, 4000);
                cellTitleNoRegis.setCellValue(getResources().getString(R.string.no_register));

                HSSFCell cellTitleName = rowTitle.createCell(1);
                cellTitleName.setCellStyle(cellStyle);
                hssfSheet.setColumnWidth(1, 9000);
                cellTitleName.setCellValue(getResources().getString(R.string.name));

                HSSFCell cellTitleSaldo = rowTitle.createCell(2);
                cellTitleSaldo.setCellStyle(cellStyle);
                hssfSheet.setColumnWidth(2, 6000);
                cellTitleSaldo.setCellValue(getResources().getString(R.string.ballance));

                for (int i = 0; i < listUserData.size(); i++) {
                    HSSFRow rowData = hssfSheet.createRow(i + 2);

                    HSSFCell cellDataNoRegis = rowData.createCell(0);
                    cellDataNoRegis.setCellStyle(cellStyle);
                    cellDataNoRegis.setCellValue(listSaldo.get(i).getNo_regis());

                    HSSFCell cellDataName = rowData.createCell(1);
                    cellDataName.setCellStyle(cellStyle);
                    cellDataName.setCellValue(listUserData.get(i).getName());

                    HSSFCell cellDataSaldo = rowData.createCell(2);
                    cellDataSaldo.setCellStyle(cellStyle);
                    cellDataSaldo.setCellValue(convertToRupiah(listSaldo.get(i).getSaldo()));
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


}
