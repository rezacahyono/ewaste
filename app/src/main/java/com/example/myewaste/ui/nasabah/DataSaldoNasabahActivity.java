package com.example.myewaste.ui.nasabah;

import static com.example.myewaste.utils.Constant.NAME;
import static com.example.myewaste.utils.Constant.NASABAH;
import static com.example.myewaste.utils.Constant.NO_REGIS;
import static com.example.myewaste.utils.Constant.SALDO_NASABAH;
import static com.example.myewaste.utils.Constant.USER_DATA;
import static com.example.myewaste.utils.Utils.convertToRupiah;
import static com.example.myewaste.utils.Utils.getRegisterCode;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myewaste.R;
import com.example.myewaste.adapter.ListSaldoNasabahAdapter;
import com.example.myewaste.databinding.ActivityDataUserBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.saldo.Saldo;
import com.example.myewaste.model.user.UserData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.protobuf.Value;

import java.util.ArrayList;
import java.util.Objects;

public class DataSaldoNasabahActivity extends AppCompatActivity {
    private ArrayList<UserData> listUserData;
    private Saldo saldo;
    private DatabaseReference databaseReference;

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

        saldo = new Saldo();
        listUserData = new ArrayList<>();
        adapter = new ListSaldoNasabahAdapter();
        binding.rvUser.setHasFixedSize(true);

        fetchDataUserBySearch("");

        setRecylcerView();
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


    private void setRecylcerView(){
        binding.rvUser.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUser.setAdapter(adapter);


        adapter.setOnItemAction((userData, tvSaldo) -> {
            databaseReference.child(SALDO_NASABAH).orderByChild(NO_REGIS).equalTo(userData.getNo_regis()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        Saldo saldoResult = dataSnapshot.getValue(Saldo.class);
                        if (saldoResult != null){
                            saldo = saldoResult;
                        }
                    }
                    tvSaldo.setText(convertToRupiah((int) saldo.getSaldo()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
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
                listUserData.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


//    private void loadDataNasabah(){
//        userDataArrayList = new ArrayList<>();
//        Query nasabahQuery = databaseReference.child("userdata").orderByChild("noregis");
//        nasabahQuery.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists()){
//                    for(DataSnapshot data :snapshot.getChildren()){
//                        UserData userData = data.getValue(UserData.class);
//                        if(Utils.getRegisterCode(userData.getNo_regis()).toLowerCase().equals("n")){
//                            userDataArrayList.add(userData);
//                        }
//                    }
//                    prepareRecycleView(userDataArrayList);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    private void prepareRecycleView(ArrayList<UserData> userDataArrayList){
//        ListSaldoNasabahAdapter adapter = new ListSaldoNasabahAdapter(this, userDataArrayList, listener);
//        LinearLayoutManager mManager = new LinearLayoutManager(this);
//        rvDataSaldoNasabah.setLayoutManager(mManager);
//        rvDataSaldoNasabah.setAdapter(adapter);
//
//    }
//
//    private void loadSaldoNasabah(String idNasabah, TextView attachTo){
//        Query saldoNasabahQuery = databaseReference.child("saldonasabah").child(idNasabah).child("saldo");
//        saldoNasabahQuery.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                int saldoNasabah = snapshot.getValue(int.class);
//                attachTo.setText(Utils.convertToRupiah(saldoNasabah));
//                listener.setTotalSaldoNasabah(saldoNasabah);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    private void loadAllSaldoNasabah(){
//        ArrayList<Saldo> saldoArrayList = new ArrayList<>();
//        Query saldoNasabahQuery = databaseReference.child("saldonasabah").orderByChild("noregis");
//        saldoNasabahQuery.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot data : snapshot.getChildren()){
//                    Saldo saldo = data.getValue(Saldo.class);
//                    saldoArrayList.add(saldo);
//                }
//                createExcel(saldoArrayList);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    private void createExcel(ArrayList<Saldo> saldoArrayList){
//        File filePath = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "MyEwaste");
//
//        if(!filePath.exists()){
//            if(filePath.mkdir()){
//                filePath = new File(filePath.getAbsolutePath() + File.separator +"data_saldo_nasabah_"+System.currentTimeMillis()+".xls");
//            }else{
//                showMessage(DataSaldoNasabahActivity.this, "Failed To make Directory");
//            }
//        }else{
//            filePath = new File(filePath.getAbsolutePath() + File.separator +"data_saldo_nasabah_"+System.currentTimeMillis()+".xls");
//        }
//        //todo create new workbook
//        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
//        //todo create new worksheet
//        HSSFSheet hssfSheet = hssfWorkbook.createSheet("Data Saldo Nasabah");
//
//        //cell tanggal
//        HSSFRow rowTanggal =hssfSheet.createRow(0);
//        HSSFCell cellTanggal = rowTanggal.createCell(0);
//        cellTanggal.setCellValue("Data Saldo Nasabah");
//        hssfSheet.addMergedRegion(new CellRangeAddress(0,0,0,5));
//
//        //todo cell style
//        CellStyle cellStyle = hssfWorkbook.createCellStyle();
////        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
////        cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
////        cellStyle.setBorderTop(CellStyle.BORDER_THIN);
////        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
////        cellStyle.setBorderRight(CellStyle.BORDER_THIN);
//        cellStyle.setWrapText(true);
//
//        HSSFRow rowTitle = hssfSheet.createRow(1);
//        HSSFCell cellTitleNoRegis = rowTitle.createCell(0);
//        cellTitleNoRegis.setCellStyle(cellStyle);
//        cellTitleNoRegis.setCellValue("ID Nasabah");
//
//        HSSFCell cellTitleNama = rowTitle.createCell(1);
//        cellTitleNama.setCellStyle(cellStyle);
//        cellTitleNama.setCellValue("Nama Nasabah");
//
//        HSSFCell cellTitleSaldo = rowTitle.createCell(2);
//        cellTitleSaldo.setCellStyle(cellStyle);
//        cellTitleSaldo.setCellValue("Saldo Nasabah");
//
//        int total = 0;
//
//        for(int i = 2; i <= userDataArrayList.size()+1; i++){
//
//            HSSFRow rowData = hssfSheet.createRow(i);
//
//            HSSFCell cellDataNoregis = rowData.createCell(0);
//            cellDataNoregis.setCellStyle(cellStyle);
//            cellDataNoregis.setCellValue(userDataArrayList.get(i-2).getNo_regis());
//
//            HSSFCell cellDataNama = rowData.createCell(1);
//            cellDataNama.setCellStyle(cellStyle);
//            cellDataNama.setCellValue(userDataArrayList.get(i-2).getName());
//
//            HSSFCell cellDataSaldo = rowData.createCell(2);
//            cellDataSaldo.setCellStyle(cellStyle);
//            cellDataSaldo.setCellValue(convertToRupiah(saldoArrayList.get(i-2).getSaldo()));
//            total+= saldoArrayList.get(i-2).getSaldo();
//        }
//
//        HSSFRow rowTotal = hssfSheet.createRow(userDataArrayList.size() + 2);
//        HSSFCell cellTitleTotal = rowTotal.createCell(1);
//        cellTitleTotal.setCellStyle(cellStyle);
//        cellTitleTotal.setCellValue("TOTAL");
//
//        HSSFCell cellValueTotal = rowTotal.createCell(2);
//        cellValueTotal.setCellStyle(cellStyle);
//        cellValueTotal.setCellValue(convertToRupiah(total));
//
//        try{
//            if(!filePath.exists()){
//                filePath.createNewFile();
//            }
//
//            FileOutputStream fileOutputStream= new FileOutputStream(filePath);
//            hssfWorkbook.write(fileOutputStream);
//
//            if (fileOutputStream!=null){
//                fileOutputStream.flush();
//                fileOutputStream.close();
//                showMessage(DataSaldoNasabahActivity.this, "Location : " +filePath.getAbsolutePath());
//                finish();
//            }
//        }catch (Exception e){
//            Log.d("TAG", "exportToExcel: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == STORAGE_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                loadAllSaldoNasabah();
//            } else {
//                Toast.makeText(this, "Tidak Mendapatkan Hak Akses Storage",Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void requestStoragePermission() {
//        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
//            new AlertDialog.Builder(this)
//                    .setTitle("Permission Needed")
//                    .setMessage("We Need Permission to Access Your Galery to upload your File into Our Database")
//                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            ActivityCompat.requestPermissions(DataSaldoNasabahActivity.this, PERMISSIONS_STORAGE,STORAGE_PERMISSION_CODE);
//                        }
//                    })
//                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            dialogInterface.dismiss();
//                        }
//                    })
//                    .create()
//                    .show();
//        }else{
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
//        }
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed();
//        return true;
//    }
//
//    public interface DataSaldoNasabahListener{
//        void setTotalSaldoNasabah(int addSaldo);
//        void onLoadSaldoNasabah(String idNasabah, TextView attachTo);
//    }
//

}
