package com.example.myewaste;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myewaste.adapter.DataSaldoNasabahAdapter;
import com.example.myewaste.model.saldo.Saldo;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.utils.Util;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import static com.example.myewaste.utils.Util.convertToRupiah;
import static com.example.myewaste.utils.Util.showMessage;

public class DataSaldoNasabahActivity extends AppCompatActivity {
    private RecyclerView rvDataSaldoNasabah;
    private TextView totalSaldo;
    private FloatingActionButton btnDownload;
    private DatabaseReference databaseReference;
    private DataSaldoNasabahListener listener;
    private int total = 0;
    private ArrayList<UserData> userDataArrayList;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int STORAGE_PERMISSION_CODE = 1;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_total_saldo);

        getSupportActionBar().setTitle("Data Saldo Nasabah");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rvDataSaldoNasabah = findViewById(R.id.list_data_saldo_nasabah);
        totalSaldo = findViewById(R.id.total_saldo_nasabah);
        btnDownload = findViewById(R.id. btnDownloadExclTotalSaldoNasabah);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        loadDataNasabah();

        listener = new DataSaldoNasabahListener() {
            @Override
            public void setTotalSaldoNasabah(int addSaldo) {
                total+=addSaldo;
                totalSaldo.setText(Util.convertToRupiah(total));
            }

            @Override
            public void onLoadSaldoNasabah(String idNasabah, TextView attachTo) {
                loadSaldoNasabah(idNasabah, attachTo);
            }
        };

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    loadAllSaldoNasabah();
                }else{
                    requestStoragePermission();
                }
            }
        });


    }

    private void loadDataNasabah(){
        userDataArrayList = new ArrayList<>();
        Query nasabahQuery = databaseReference.child("userdata").orderByChild("noregis");
        nasabahQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot data :snapshot.getChildren()){
                        UserData userData = data.getValue(UserData.class);
                        if(Util.getRegisterCode(userData.getNo_regis()).toLowerCase().equals("n")){
                            userDataArrayList.add(userData);
                        }
                    }
                    prepareRecycleView(userDataArrayList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void prepareRecycleView(ArrayList<UserData> userDataArrayList){
        DataSaldoNasabahAdapter adapter = new DataSaldoNasabahAdapter(this, userDataArrayList, listener);
        LinearLayoutManager mManager = new LinearLayoutManager(this);
        rvDataSaldoNasabah.setLayoutManager(mManager);
        rvDataSaldoNasabah.setAdapter(adapter);

    }

    private void loadSaldoNasabah(String idNasabah, TextView attachTo){
        Query saldoNasabahQuery = databaseReference.child("saldonasabah").child(idNasabah).child("saldo");
        saldoNasabahQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int saldoNasabah = snapshot.getValue(int.class);
                attachTo.setText(Util.convertToRupiah(saldoNasabah));
                listener.setTotalSaldoNasabah(saldoNasabah);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadAllSaldoNasabah(){
        ArrayList<Saldo> saldoArrayList = new ArrayList<>();
        Query saldoNasabahQuery = databaseReference.child("saldonasabah").orderByChild("noregis");
        saldoNasabahQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data : snapshot.getChildren()){
                    Saldo saldo = data.getValue(Saldo.class);
                    saldoArrayList.add(saldo);
                }
                createExcel(saldoArrayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createExcel(ArrayList<Saldo> saldoArrayList){
        File filePath = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "MyEwaste");

        if(!filePath.exists()){
            if(filePath.mkdir()){
                filePath = new File(filePath.getAbsolutePath() + File.separator +"data_saldo_nasabah_"+System.currentTimeMillis()+".xls");
            }else{
                showMessage(DataSaldoNasabahActivity.this, "Failed To make Directory");
            }
        }else{
            filePath = new File(filePath.getAbsolutePath() + File.separator +"data_saldo_nasabah_"+System.currentTimeMillis()+".xls");
        }
        //todo create new workbook
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        //todo create new worksheet
        HSSFSheet hssfSheet = hssfWorkbook.createSheet("Data Saldo Nasabah");

        //cell tanggal
        HSSFRow rowTanggal =hssfSheet.createRow(0);
        HSSFCell cellTanggal = rowTanggal.createCell(0);
        cellTanggal.setCellValue("Data Saldo Nasabah");
        hssfSheet.addMergedRegion(new CellRangeAddress(0,0,0,5));

        //todo cell style
        CellStyle cellStyle = hssfWorkbook.createCellStyle();
//        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
//        cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
//        cellStyle.setBorderTop(CellStyle.BORDER_THIN);
//        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
//        cellStyle.setBorderRight(CellStyle.BORDER_THIN);
        cellStyle.setWrapText(true);

        HSSFRow rowTitle = hssfSheet.createRow(1);
        HSSFCell cellTitleNoRegis = rowTitle.createCell(0);
        cellTitleNoRegis.setCellStyle(cellStyle);
        cellTitleNoRegis.setCellValue("ID Nasabah");

        HSSFCell cellTitleNama = rowTitle.createCell(1);
        cellTitleNama.setCellStyle(cellStyle);
        cellTitleNama.setCellValue("Nama Nasabah");

        HSSFCell cellTitleSaldo = rowTitle.createCell(2);
        cellTitleSaldo.setCellStyle(cellStyle);
        cellTitleSaldo.setCellValue("Saldo Nasabah");

        int total = 0;

        for(int i = 2; i <= userDataArrayList.size()+1; i++){

            HSSFRow rowData = hssfSheet.createRow(i);

            HSSFCell cellDataNoregis = rowData.createCell(0);
            cellDataNoregis.setCellStyle(cellStyle);
            cellDataNoregis.setCellValue(userDataArrayList.get(i-2).getNo_regis());

            HSSFCell cellDataNama = rowData.createCell(1);
            cellDataNama.setCellStyle(cellStyle);
            cellDataNama.setCellValue(userDataArrayList.get(i-2).getName());

            HSSFCell cellDataSaldo = rowData.createCell(2);
            cellDataSaldo.setCellStyle(cellStyle);
            cellDataSaldo.setCellValue(convertToRupiah(saldoArrayList.get(i-2).getSaldo()));
            total+= saldoArrayList.get(i-2).getSaldo();
        }

        HSSFRow rowTotal = hssfSheet.createRow(userDataArrayList.size() + 2);
        HSSFCell cellTitleTotal = rowTotal.createCell(1);
        cellTitleTotal.setCellStyle(cellStyle);
        cellTitleTotal.setCellValue("TOTAL");

        HSSFCell cellValueTotal = rowTotal.createCell(2);
        cellValueTotal.setCellStyle(cellStyle);
        cellValueTotal.setCellValue(convertToRupiah(total));

        try{
            if(!filePath.exists()){
                filePath.createNewFile();
            }

            FileOutputStream fileOutputStream= new FileOutputStream(filePath);
            hssfWorkbook.write(fileOutputStream);

            if (fileOutputStream!=null){
                fileOutputStream.flush();
                fileOutputStream.close();
                showMessage(DataSaldoNasabahActivity.this, "Location : " +filePath.getAbsolutePath());
                finish();
            }
        }catch (Exception e){
            Log.d("TAG", "exportToExcel: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadAllSaldoNasabah();
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
                            ActivityCompat.requestPermissions(DataSaldoNasabahActivity.this, PERMISSIONS_STORAGE,STORAGE_PERMISSION_CODE);
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public interface DataSaldoNasabahListener{
        void setTotalSaldoNasabah(int addSaldo);
        void onLoadSaldoNasabah(String idNasabah, TextView attachTo);
    }


}
