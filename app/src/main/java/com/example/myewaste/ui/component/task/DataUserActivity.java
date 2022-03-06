package com.example.myewaste.ui.component.task;

import static com.example.myewaste.utils.Constant.AKTIF;
import static com.example.myewaste.utils.Constant.DEACTIVE;
import static com.example.myewaste.utils.Constant.EWASTE;
import static com.example.myewaste.utils.Constant.EXTRAS_ACTION_MODE;
import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Constant.FORMATE_EXCEL;
import static com.example.myewaste.utils.Constant.MODE;
import static com.example.myewaste.utils.Constant.MODE_ADD;
import static com.example.myewaste.utils.Constant.MODE_UPDATE;
import static com.example.myewaste.utils.Constant.NAME;
import static com.example.myewaste.utils.Constant.NASABAH;
import static com.example.myewaste.utils.Constant.NO_REGIS;
import static com.example.myewaste.utils.Constant.STATUS;
import static com.example.myewaste.utils.Constant.SUPER_ADMIN;
import static com.example.myewaste.utils.Constant.TELLER;
import static com.example.myewaste.utils.Constant.USER;
import static com.example.myewaste.utils.Constant.USER_DATA;
import static com.example.myewaste.utils.Utils.convertDateAndTime;
import static com.example.myewaste.utils.Utils.getRegisterCode;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myewaste.R;
import com.example.myewaste.adapter.ListUserAdapater;
import com.example.myewaste.databinding.ActivitySearchUserBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.user.User;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.pref.SessionManagement;
import com.example.myewaste.utils.Mode;
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
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class DataUserActivity extends AppCompatActivity {


    private Mode modeUser;
    private ArrayList<UserData> listUserData;
    private DatabaseReference databaseReference;

    private ListUserAdapater adapter;
    private ActivitySearchUserBinding binding;
    private MainToolbarBinding bindingToolbar;
    private SessionManagement sessionManagement;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();

        bindingToolbar = binding.mainToolbar;
        bindingToolbar.btnBack.setOnClickListener(view -> onBackPressed());

        binding.ivPlaceholderEmpty.setImageResource(R.drawable.ic_placeholder_empty_data_user);

        if (getIntent().hasExtra(MODE)) {
            modeUser = (Mode) getIntent().getSerializableExtra(MODE);
            switch (modeUser) {
                case MODE_SUPER_ADMIN:
                    bindingToolbar.tvTitleBar.setText(R.string.data_admin);
                    binding.tvTitle.setText(getResources().getString(R.string.title_empty_placeholder, "admin", "admin"));
                    break;
                case MODE_TELLER:
                    bindingToolbar.tvTitleBar.setText(R.string.data_teller);
                    binding.tvTitle.setText(getResources().getString(R.string.title_empty_placeholder, "teller", "teller"));
                    break;
                case MODE_NASABAH:
                    bindingToolbar.tvTitleBar.setText(R.string.data_nasabah);
                    binding.tvTitle.setText(getResources().getString(R.string.title_empty_placeholder, "nasabah", "nasabah"));
                    break;
                default:
                    break;
            }
        }
        databaseReference = FirebaseDatabase.getInstance().getReference();
        sessionManagement = new SessionManagement(this);

        adapter = new ListUserAdapater();
        binding.rvUser.setHasFixedSize(true);

        actionSearchView();
        fetchDataUserBySearch("");
        setRecylcerViewUser();

        bindingToolbar.btnTrash.setImageResource(R.drawable.ic_download);

        bindingToolbar.btnTrash.setOnClickListener(v -> generateReportDataUser());

        binding.fbAddUser.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddUserActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(MODE, modeUser);
            intent.putExtra(EXTRAS_ACTION_MODE, MODE_ADD);
            startActivity(intent);
        });
    }

    private void setRecylcerViewUser() {
        binding.rvUser.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUser.setAdapter(adapter);

        adapter.setOnItemClickCallback(userData -> {
            Intent intent = new Intent(DataUserActivity.this, AddUserActivity.class);
            intent.putExtra(EXTRAS_USER_DATA, userData);
            intent.putExtra(MODE, modeUser);
            intent.putExtra(EXTRAS_ACTION_MODE, MODE_UPDATE);
            startActivity(intent);
        });

        adapter.setOnItemAction(new ListUserAdapater.OnItemAction() {
            @Override
            public void setVisibilityUpdate(UserData userData, ImageButton ibActive, ImageButton ibDeactive) {
                databaseReference.child(USER).orderByChild(NO_REGIS).equalTo(userData.getNo_regis()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User userResult = dataSnapshot.getValue(User.class);
                            if (userResult != null) {
                                if (userResult.getStatus().equals(AKTIF)) {
                                    ibDeactive.setVisibility(View.GONE);
                                    ibActive.setVisibility(View.VISIBLE);
                                } else {
                                    ibActive.setVisibility(View.GONE);
                                    ibDeactive.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void setDeactive(UserData userData) {
                setActiveUser(userData.getNo_regis());
            }

            @Override
            public void setActive(UserData userData) {
                setDeactiveUser(userData.getNo_regis());
            }
        });
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
                for (DataSnapshot data : snapshot.getChildren()) {
                    UserData userData = data.getValue(UserData.class);
                    if (userData != null) {
                        if (getRegisterCode(userData.getNo_regis()).equalsIgnoreCase(getModeNoRegis(modeUser))) {
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

    private void setDeactiveUser(String noRegis) {
        if (!noRegis.equals(sessionManagement.getUserSession())) {
            databaseReference.child(USER).orderByChild(NO_REGIS).equalTo(noRegis).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        dataSnapshot.getRef().child(STATUS).setValue(DEACTIVE);
                    }
                    Toast.makeText(DataUserActivity.this, getResources().getString(R.string.active_or_deactive_success, "non-aktif"), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DataUserActivity.this, getResources().getString(R.string.active_or_deactive_failure, "non-aktif"), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, getResources().getString(R.string.active_or_deactive_failure, "non-aktif"), Toast.LENGTH_SHORT).show();
        }
    }

    private void setActiveUser(String noRegis) {
        if (!noRegis.equals(sessionManagement.getUserSession())) {
            databaseReference.child(USER).orderByChild(NO_REGIS).equalTo(noRegis).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        dataSnapshot.getRef().child(STATUS).setValue(AKTIF);
                    }
                    Toast.makeText(DataUserActivity.this, getResources().getString(R.string.active_or_deactive_success, "active"), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DataUserActivity.this, getResources().getString(R.string.active_or_deactive_failure, "active"), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, getResources().getString(R.string.active_or_deactive_failure, "active"), Toast.LENGTH_SHORT).show();
        }
    }

    private String getModeNoRegis(Mode modeUser) {
        String result = "";
        switch (modeUser) {
            case MODE_NASABAH: {
                result = NASABAH;
                break;
            }
            case MODE_TELLER: {
                result = TELLER;
                break;
            }
            case MODE_SUPER_ADMIN: {
                result = SUPER_ADMIN;
            }
        }
        return result;
    }

    private String dataModeUser(Mode modeUser) {
        String result = "";
        switch (modeUser) {
            case MODE_NASABAH: {
                result = getResources().getString(R.string.data_nasabah);
                break;
            }
            case MODE_TELLER: {
                result = getResources().getString(R.string.data_teller);
                break;
            }
            case MODE_SUPER_ADMIN: {
                result = getResources().getString(R.string.data_admin);
            }
        }
        return result;
    }

    private void generateReportDataUser() {
        if (listUserData.size() > 0){
            File filePath = new File(getExternalFilesDir(null) + File.separator + EWASTE);
            if (!filePath.exists()) {
                if (filePath.mkdir()) {
                    filePath = new File(filePath.getAbsolutePath() + File.separator + dataModeUser(modeUser) + convertDateAndTime(System.currentTimeMillis()) + FORMATE_EXCEL);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.failure), Toast.LENGTH_SHORT).show();
                }
            } else {
                filePath = new File(filePath.getAbsolutePath() + File.separator + dataModeUser(modeUser) + convertDateAndTime(System.currentTimeMillis()) + FORMATE_EXCEL);
            }

            HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
            HSSFSheet hssfSheet = hssfWorkbook.createSheet(dataModeUser(modeUser));

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
            cellNameApp.setCellValue(dataModeUser(modeUser) + " " + getResources().getString(R.string.ewaste_pch));
            cellNameApp.setCellStyle(cellStyle);
            hssfSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            HSSFRow rowTitle = hssfSheet.createRow(1);

            HSSFCell cellTitleNoRegis = rowTitle.createCell(0);
            cellTitleNoRegis.setCellStyle(cellStyle);
            hssfSheet.setColumnWidth(0, 5000);
            cellTitleNoRegis.setCellValue(getResources().getString(R.string.no_register));

            HSSFCell cellTitleNik = rowTitle.createCell(1);
            cellTitleNik.setCellStyle(cellStyle);
            hssfSheet.setColumnWidth(1, 8000);
            cellTitleNik.setCellValue(getResources().getString(R.string.nik));

            HSSFCell cellTitleName = rowTitle.createCell(2);
            cellTitleName.setCellStyle(cellStyle);
            hssfSheet.setColumnWidth(2, 9000);
            cellTitleName.setCellValue(getResources().getString(R.string.name));

            HSSFCell cellTitleGender = rowTitle.createCell(3);
            cellTitleGender.setCellStyle(cellStyle);
            hssfSheet.setColumnWidth(3, 8000);
            cellTitleGender.setCellValue(getResources().getString(R.string.gender));

            HSSFCell cellTitleNoPhone = rowTitle.createCell(4);
            cellTitleNoPhone.setCellStyle(cellStyle);
            hssfSheet.setColumnWidth(4, 7000);
            cellTitleNoPhone.setCellValue(getResources().getString(R.string.phone));

            HSSFCell cellTitleAddress = rowTitle.createCell(5);
            cellTitleAddress.setCellStyle(cellStyle);
            hssfSheet.setColumnWidth(5, 10000);
            cellTitleAddress.setCellValue(getResources().getString(R.string.address));

            HSSFCell cellTitleAvatar = rowTitle.createCell(6);
            cellTitleAvatar.setCellStyle(cellStyle);
            hssfSheet.setColumnWidth(6, 12000);
            cellTitleAvatar.setCellValue(getResources().getString(R.string.photo_profil));

            HSSFCell cellTitlePhotoNik = rowTitle.createCell(7);
            cellTitlePhotoNik.setCellStyle(cellStyle);
            hssfSheet.setColumnWidth(7, 12000);
            cellTitlePhotoNik.setCellValue(getResources().getString(R.string.photo_profil));

            for (int i = 0; i < listUserData.size(); i++) {
                HSSFRow rowData = hssfSheet.createRow(i + 2);

                HSSFCell cellDataNoRegis = rowData.createCell(0);
                cellDataNoRegis.setCellStyle(cellStyle);
                cellDataNoRegis.setCellValue(listUserData.get(i).getNo_regis());

                HSSFCell cellDataNik = rowData.createCell(1);
                cellDataNik.setCellStyle(cellStyle);
                cellDataNik.setCellValue(listUserData.get(i).getNik());

                HSSFCell cellDataName = rowData.createCell(2);
                cellDataName.setCellStyle(cellStyle);
                cellDataName.setCellValue(listUserData.get(i).getName());

                HSSFCell cellDataGender = rowData.createCell(3);
                cellDataGender.setCellStyle(cellStyle);
                cellDataGender.setCellValue(listUserData.get(i).getGender());

                HSSFCell cellDataNoPhone = rowData.createCell(4);
                cellDataNoPhone.setCellStyle(cellStyle);
                cellDataNoPhone.setCellValue(listUserData.get(i).getPhone());

                HSSFCell cellDataAddress = rowData.createCell(5);
                cellDataAddress.setCellStyle(cellStyle);
                cellDataAddress.setCellValue(listUserData.get(i).getAddress());

                HSSFCell cellDataAvatar = rowData.createCell(6);
                cellDataAvatar.setCellStyle(cellStyle);
                cellDataAvatar.setCellValue(listUserData.get(i).getAvatar());

                HSSFCell cellDataPhotoNik = rowData.createCell(7);
                cellDataPhotoNik.setCellStyle(cellStyle);
                cellDataPhotoNik.setCellValue(listUserData.get(i).getPhoto_nik());
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
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, getResources().getString(R.string.failure) + " download", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, getResources().getString(R.string.data_not_found), Toast.LENGTH_SHORT).show();
        }
    }
}