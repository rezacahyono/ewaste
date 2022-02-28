package com.example.myewaste;

import static com.example.myewaste.utils.Constant.AKTIF;
import static com.example.myewaste.utils.Constant.DEACTIVE;
import static com.example.myewaste.utils.Constant.EXTRAS_ACTION_MODE;
import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
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
import static com.example.myewaste.utils.Util.getRegisterCode;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myewaste.adapter.ListUserAdapater;
import com.example.myewaste.databinding.ActivitySearchUserBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.user.User;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.ui.login.AddUserActivity;
import com.example.myewaste.utils.Mode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

public class DataUserActivity extends AppCompatActivity {
//    private DatabaseReference reference;
//    ArrayList<UserData> listUserData;
//    DataUserAdapter adapter;
//    private static final String TAG = "DataNasabahActivity";
//    private ImageView filter;
//    private static String[] PERMISSIONS_STORAGE = {
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//    };
//    private static final int STORAGE_PERMISSION_CODE = 1;
//
//    FloatingActionButton btn_addnasabah, btnDownload;
//
//    private RecyclerView mRecyler;
//    private LinearLayoutManager mManager;
//    /*private RecyclerView dataNasabahRecylcer;
//    private LinearLayoutManager dataNasabahManager;*/


    private Mode modeUser;
    private ArrayList<UserData> listUserData;
    private ArrayList<UserData> listUser;
    private ListUserAdapater adapter;
    private DatabaseReference databaseReference;
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

        requestPermissions();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        sessionManagement = new SessionManagement(this);

        listUserData = new ArrayList<>();
        listUser = new ArrayList<>();
        adapter = new ListUserAdapater();
        binding.rvUser.setHasFixedSize(true);

        actionSearchView();
        loadUserBySearch("");
        setRecylcerViewUser();

        bindingToolbar.btnTrash.setImageResource(R.drawable.ic_download);

        bindingToolbar.btnTrash.setOnClickListener(v -> {
            createExcelFileReportBarang();
        });

        binding.fbAddUser.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddUserActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(MODE, modeUser);
            intent.putExtra(EXTRAS_ACTION_MODE, MODE_ADD);
            startActivity(intent);
        });


//
//
//        //event klik untuk tambah mahasiswa
//        btn_addnasabah.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent addnasabah = new Intent(getApplicationContext(), AddUserActivity.class);
//                addnasabah.putExtra("modeUser", modeUser);
//                startActivity(addnasabah);
//            }
//        });
//
//        btnDownload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(ContextCompat.checkSelfPermission(getApplicationContext(),
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
//                    createExcelFileReportBarang();
//                }else{
//                    requestStoragePermission();
//                }
//            }
//        });

//        loadStatusUser();
    }

    private void setRecylcerViewUser() {
        binding.rvUser.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUser.setAdapter(adapter);

        adapter.setOnItemClickCallback(userData -> {
            Intent intent = new Intent(this, AddUserActivity.class);
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
                                    ibActive.setVisibility(View.GONE);
                                    ibDeactive.setVisibility(View.VISIBLE);
                                } else {
                                    ibDeactive.setVisibility(View.GONE);
                                    ibActive.setVisibility(View.VISIBLE);
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
                setDeactiveUser(userData.getNo_regis());
//                Toast.makeText(DataUserActivity.this, "Deactive", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void setActive(UserData userData) {
                setActiveUser(userData.getNo_regis());
//                Toast.makeText(DataUserActivity.this, "Active", Toast.LENGTH_SHORT).show();
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
                        loadUserBySearch(query);
                        binding.scUser.clearFocus();
                    }
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals("")) {
                    loadUserBySearch(newText);
                } else {
                    loadUserBySearch("");
                }
                return false;
            }
        });
    }

    public void loadUserBySearch(String q) {
        DatabaseReference refUserData = databaseReference.child(USER_DATA);
        Query queryUserData = refUserData.orderByChild(NAME);
        queryUserData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                listUserData.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setDeactiveUser(String noRegis) {
        if (!noRegis.equals(sessionManagement.getUserSession())){
            databaseReference.child(USER).orderByChild(NO_REGIS).equalTo(noRegis).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        dataSnapshot.getRef().child(STATUS).setValue(DEACTIVE);
                    }
                    Toast.makeText(DataUserActivity.this, getResources().getString(R.string.active_or_deactive_success, "active"), Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "onDataChange: berhasil");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DataUserActivity.this, getResources().getString(R.string.active_or_deactive_failure, "active"), Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "onDataChange: gagal");
                }
            });
        }else {
            Toast.makeText(this, getResources().getString(R.string.active_or_deactive_failure, "active"), Toast.LENGTH_SHORT).show();
            Log.d("TAG", "onDataChange: gagal");
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
                    Log.d("TAG", "onDataChange: berhasil");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DataUserActivity.this, getResources().getString(R.string.active_or_deactive_failure, "active"), Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "onDataChange: gagal");
                }
            });
        }else {
            Toast.makeText(this, getResources().getString(R.string.active_or_deactive_failure, "active"), Toast.LENGTH_SHORT).show();
            Log.d("TAG", "onDataChange: gagal");
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


    private void requestPermissions() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(getApplication(), perms)) {
            Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getResources().getString(R.string.message_permission),
                    101,
                    perms
            );
        }
    }

//
    private void createExcelFileReportBarang(){
        File filePath = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "MyEwaste");

        if(!filePath.exists()){
            if(filePath.mkdir()){
                filePath = new File(filePath.getAbsolutePath() + File.separator +"data_nasabah_"+System.currentTimeMillis()+".xls");
            }else{
                Toast.makeText(DataUserActivity.this, "Failed To make Directory",Toast.LENGTH_SHORT).show();
            }
        }else{
            filePath = new File(filePath.getAbsolutePath() + File.separator +"data_nasabah_"+System.currentTimeMillis()+".xls");
        }
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet("Data Nasabah");

        //cell tanggal
        HSSFRow rowTanggal =hssfSheet.createRow(0);
        HSSFCell cellTanggal = rowTanggal.createCell(0);
        cellTanggal.setCellValue("Data Nasabah My Ewaste");
        hssfSheet.addMergedRegion(new CellRangeAddress(0,0,0,5));

        //todo cell style
        CellStyle cellStyle = hssfWorkbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setWrapText(true);

        HSSFRow rowTitle = hssfSheet.createRow(1);
        HSSFCell cellTitleNoRegis = rowTitle.createCell(0);
        cellTitleNoRegis.setCellStyle(cellStyle);
        cellTitleNoRegis.setCellValue("No Regis");

        HSSFCell cellTitleNIK = rowTitle.createCell(1);
        cellTitleNIK.setCellStyle(cellStyle);
        cellTitleNIK.setCellValue("NIK");

        HSSFCell cellTitleNama = rowTitle.createCell(2);
        cellTitleNama.setCellStyle(cellStyle);
        cellTitleNama.setCellValue("Nama Nasabah");

        HSSFCell cellTitleJK = rowTitle.createCell(3);
        cellTitleJK.setCellStyle(cellStyle);
        cellTitleJK.setCellValue("Jenis Kelamin");

        HSSFCell cellTitleAlamat = rowTitle.createCell(4);
        cellTitleAlamat.setCellStyle(cellStyle);
        cellTitleAlamat.setCellValue("Alamat");
//
//        HSSFCell cellTitleRT = rowTitle.createCell(5);
//        cellTitleRT.setCellStyle(cellStyle);
//        cellTitleRT.setCellValue("RT");
//
//        HSSFCell cellTitleRW = rowTitle.createCell(6);
//        cellTitleRW.setCellStyle(cellStyle);
//        cellTitleRW.setCellValue("RW");

        HSSFCell cellTitleNoTelp = rowTitle.createCell(5);
        cellTitleNoTelp.setCellStyle(cellStyle);
        cellTitleNoTelp.setCellValue("No Telp");

        HSSFCell cellTitleProfile = rowTitle.createCell(6);
        cellTitleProfile.setCellStyle(cellStyle);
        cellTitleProfile.setCellValue("Link Foto Profile");

        for(int i = 2; i <= listUserData.size()+1; i++){

            HSSFRow rowData = hssfSheet.createRow(i);

            HSSFCell cellDataNoregis = rowData.createCell(0);
            cellDataNoregis.setCellStyle(cellStyle);
            cellDataNoregis.setCellValue(listUserData.get(i-2).getNo_regis());

            HSSFCell cellDataNIK = rowData.createCell(1);
            cellDataNIK.setCellStyle(cellStyle);
            cellDataNIK.setCellValue(listUserData.get(i-2).getNik());

            HSSFCell cellDataNama = rowData.createCell(2);
            cellDataNama.setCellStyle(cellStyle);
            cellDataNama.setCellValue(listUserData.get(i-2).getName());

            HSSFCell cellDataJenisKelamin = rowData.createCell(3);
            cellDataJenisKelamin.setCellStyle(cellStyle);
            cellDataJenisKelamin.setCellValue(listUserData.get(i-2).getGender());

            HSSFCell cellDataAlamat = rowData.createCell(4);
            cellDataAlamat.setCellStyle(cellStyle);
            cellDataAlamat.setCellValue(listUserData.get(i-2).getAddress());
//
//            HSSFCell cellDataRT = rowData.createCell(5);
//            cellDataRT.setCellStyle(cellStyle);
//            cellDataRT.setCellValue(listUserData.get(i-2).getRT());
//
//            HSSFCell cellDataRW = rowData.createCell(6);
//            cellDataRW.setCellStyle(cellStyle);
//            cellDataRW.setCellValue(listUserData.get(i-2).getRW());

            HSSFCell cellDataNoTelp = rowData.createCell(5);
            cellDataNoTelp.setCellStyle(cellStyle);
            cellDataNoTelp.setCellValue(listUserData.get(i-2).getPhone());

            HSSFCell cellDataFotoProfile = rowData.createCell(6);
            cellDataFotoProfile.setCellStyle(cellStyle);
            cellDataFotoProfile.setCellValue(listUserData.get(i-2).getAvatar());
        }


        hssfSheet.setColumnWidth(8, 12000);

        try{
            if(!filePath.exists()){
                filePath.createNewFile();
            }
            FileOutputStream fileOutputStream= new FileOutputStream(filePath);
            hssfWorkbook.write(fileOutputStream);

            if (fileOutputStream!=null){
                fileOutputStream.flush();
                fileOutputStream.close();
//                showMessage(DataUserActivity.this, "Location : " +filePath.getAbsolutePath());
                finish();
            }
        }catch (Exception e){
            Log.d("TAG", "exportToExcel: " + e.getMessage());
        }
    }
//
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createExcelFileReportBarang();
            } else {
                Toast.makeText(this, "Tidak Mendapatkan Hak Akses Storage",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("We Need Permission to Access Your Galery to upload your File into Our Database")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
//                            ActivityCompat.requestPermissions(DataUserActivity.this, PERMISSIONS_STORAGE,STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
        }else{
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }
}