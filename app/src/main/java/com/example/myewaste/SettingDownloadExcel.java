package com.example.myewaste;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myewaste.model.item.ItemMaster;
import com.example.myewaste.model.item.ItemTransaction;
import com.example.myewaste.model.item.ItemType;
import com.example.myewaste.model.saldo.SaldoTransaction;
import com.example.myewaste.model.item.UnitItem;
import com.example.myewaste.model.user.UserData;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.example.myewaste.utils.Util.convertToRupiah;
import static com.example.myewaste.utils.Util.getRegisterCode;
import static com.example.myewaste.utils.Util.showMessage;

public class SettingDownloadExcel extends AppCompatActivity {
    private LinearLayout layoutStart, layoutEnd;
    private static final int STORAGE_PERMISSION_CODE = 1;
    private EditText etStart, etEnd;
    private Button download;
    private ArrayList<ItemMaster> itemMasterArrayList;
    private ArrayList<UnitItem> unitItemArrayList;
    private ArrayList<ItemType> itemTypeArrayList;
    private ArrayList<UserData> userDataArrayList;
    private ArrayList<ItemTransaction> itemTransactionArrayList;
    private ArrayList<SaldoTransaction> saldoTransactionArrayList;
    private int mode = 0; //0 export barang, 1 export saldo, 2 export potongan
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private LinearLayout layoutStartDate, layoutEndDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_download_excel);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        layoutStart = findViewById(R.id.layout_tanggal_start);
        layoutEnd = findViewById(R.id.layout_tanggal_end);
        etStart = findViewById(R.id.et_tanggalStart);
        etEnd = findViewById(R.id.et_tanggalEnd);
        download = findViewById(R.id.downloadNow);
        mode = getIntent().getIntExtra("mode",0);
        if(mode == 0){
            getSupportActionBar().setTitle("Export Laporan Penimbangan Barang");
        }else if(mode == 1){
            getSupportActionBar().setTitle("Export Laporan Transaksi Nasabah");
        }else{
            getSupportActionBar().setTitle("Export Laporan Biaya Operasional");
        }

        layoutStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //todo show dialog calendar
                showDialogCalendar(etStart, "Pilih Tanggal Awal");
            }
        });

        layoutEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //todo show dialog calendar
                showDialogCalendar(etEnd, "Pilih Tanggal Akhir");
            }
        });

        etStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogCalendar(etStart, "Pilih Tanggal Awal");
            }
        });

        etEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogCalendar(etEnd, "Pilih Tanggal Akhir");
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etStart.getText().toString().isEmpty()){
                    etStart.setError("Tidak Boleh Kosong");
                }else if(etEnd.getText().toString().isEmpty()){
                    etEnd.setError("Tidak Boleh Kosong");
                }else{
                    if(ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        long start = changeFormat(etStart.getText().toString());
                        long end = changeFormat(etEnd.getText().toString());
                        if(mode == 0){
                            loadDataTransaksiBarang(start, end);
                        }else if(mode == 1){
                            loadDataTransaksiSaldo(start, end);
                        }else{
                            loadDataPotongan(start, end);
                        }
                    }else{
                        requestStoragePermission();
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                long start = changeFormat(etStart.getText().toString());
                long end = changeFormat(etEnd.getText().toString());
                if(mode == 0){
                    loadDataTransaksiBarang(start, end);
                }else if(mode == 1){
                    loadDataTransaksiSaldo(start, end);
                }else{
                    loadDataPotongan(start, end);
                }
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
                            ActivityCompat.requestPermissions(SettingDownloadExcel.this, PERMISSIONS_STORAGE,STORAGE_PERMISSION_CODE);
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

    private void loadDataMasterBarang(){
        itemMasterArrayList = new ArrayList<>();
        Query masterBarangReference = reference.child("barang").orderByChild("no_master_barang");
        masterBarangReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data : snapshot.getChildren()){
                    ItemMaster itemMaster = data.getValue(ItemMaster.class);
                    itemMasterArrayList.add(itemMaster);
                }
                //todo create Excel file
                if(mode == 0){
//                    createExcelFileReportBarang();
                }else{
//                    createExcelFileReportSaldo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadDataMasterSatuan(){
        unitItemArrayList = new ArrayList<>();
        Query satuanModelReference = reference.child("satuan_barang").orderByChild("noSatuan");
        satuanModelReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data : snapshot.getChildren()){
                    UnitItem unitItem = data.getValue(UnitItem.class);
                    unitItemArrayList.add(unitItem);
                }
                loadDataMasterBarang();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadDataMasterJenisBarang(){
        itemTypeArrayList = new ArrayList<>();
        Query jenisBarangReference = reference.child("jenis_barang").orderByChild("no_master_barang");
        jenisBarangReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data :snapshot.getChildren()){
                    ItemType itemType = data.getValue(ItemType.class);
                    itemTypeArrayList.add(itemType);
                }
                loadDataMasterSatuan();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadDataNasabah(){
        userDataArrayList = new ArrayList<>();
        Query nasabahReference = reference.child("userdata").orderByChild("noregis");
        nasabahReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data : snapshot.getChildren()){
                    UserData nasabah = data.getValue(UserData.class);
                    if(getRegisterCode(nasabah.getNo_regis()).toLowerCase().equals("n") || getRegisterCode(nasabah.getNo_regis()).toLowerCase().equals("t")){
                        userDataArrayList.add(nasabah);
                    }
                }
                loadDataMasterJenisBarang();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void loadDataTransaksiBarang(long startDate, long endDate){
        itemTransactionArrayList = new ArrayList<>();
        Query transaksiBarangReference = reference.child("transaksi_barang").orderByChild("tanggal_transaksi").startAt(startDate).endAt(endDate);
        transaksiBarangReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data :snapshot.getChildren()){
                    ItemTransaction itemTransaction = data.getValue(ItemTransaction.class);
                    itemTransactionArrayList.add(itemTransaction);
//                    Log.d("TAG", "onDataChange: "+ itemTransaction.getTanggal_transaksi());
                }
                loadDataNasabah();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadDataTransaksiSaldo(long startDate, long endDate){
        saldoTransactionArrayList = new ArrayList<>();
        Query transakdiSaldoReference = reference.child("transaksi_saldo").orderByChild("tanggal_transaksi").startAt(startDate).endAt(endDate);
        transakdiSaldoReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data :snapshot.getChildren()){
                    SaldoTransaction saldoTransaction = data.getValue(SaldoTransaction.class);
                    if(!saldoTransaction.getStatus().equals("PENDING") && !saldoTransaction.getStatus().equals("REJECTED")){
                        saldoTransactionArrayList.add(saldoTransaction);
                    }
                }
                loadDataNasabah();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadDataPotongan(long start, long end){
        saldoTransactionArrayList = new ArrayList<>();
        Query transaksiSaldoQuery = reference.child("transaksi_saldo").orderByChild("tanggal_transaksi").startAt(start).endAt(end);
        transaksiSaldoQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        SaldoTransaction saldoTransaction = data.getValue(SaldoTransaction.class);
//                        if (saldoTransaction.getJenis_transaksi().equals("TARIK") && saldoTransaction.getStatus().equals("APPROVED")) {
                            saldoTransactionArrayList.add(saldoTransaction);
//                        }
                    }
                    //todo create Excel
//                    createExcelFileReportPotongan();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private long changeFormat(String oldDateString){
        final String OLD_FORMAT = "dd-MM-yyyy";
        long millisecond = 0;
        SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT, Locale.ENGLISH);
        Date d = null;
        try {
            d = sdf.parse(oldDateString);
            millisecond = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return millisecond;
    }

    private void showDialogCalendar(View AttachTo, String title){
        final BottomSheetDialog dialog = new BottomSheetDialog(this,R.style.BottomSheetDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_dialog_edit_user);

        TextView titleDialog = dialog.findViewById(R.id.tvTitleDialog);
        Button btnBatal = dialog.findViewById(R.id.btnDialogBatal);
        Button btnSimpan = dialog.findViewById(R.id.btnDialogSimpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        LinearLayout target = dialog.findViewById(R.id.frameEditData);
        titleDialog.setText(title);
        LayoutInflater inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View child = inflater.inflate(R.layout.frame_calendar, null);
        CalendarView calendar = child.findViewById(R.id.calendarView);
        target.addView(child);
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                ((TextView) AttachTo).setText(day + "-" + (month+1) + "-" + year);
            }
        });

        dialog.show();
    }

//    private void createExcelFileReportPotongan(){
//        File filePath = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "MyEwaste");
//
//        if(!filePath.exists()){
//            if(filePath.mkdir()){
//                filePath = new File(filePath.getAbsolutePath() + File.separator +"laporan_biaya_operasional_"+System.currentTimeMillis()+".xls");
//            }else{
//                showMessage(SettingDownloadExcel.this, "Failed To make Directory");
//            }
//        }else{
//            filePath = new File(filePath.getAbsolutePath() + File.separator +"laporan_biaya_operasional_"+System.currentTimeMillis()+".xls");
//        }
//        //todo create new workbook
//        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
//        //todo create new worksheet
//        HSSFSheet hssfSheet = hssfWorkbook.createSheet("Laporan Biaya Operasional");
//
//        //cell tanggal
//        HSSFRow rowTanggal = hssfSheet.createRow(0);
//        HSSFCell cellTanggal = rowTanggal.createCell(0);
//        cellTanggal.setCellValue("Tanggal :" + etStart.getText().toString() + " - " +etEnd.getText().toString());
//        hssfSheet.addMergedRegion(new CellRangeAddress(0,0,0,5));
//
//        //todo cell style
//        CellStyle cellStyle = hssfWorkbook.createCellStyle();
//        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
//        cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
//        cellStyle.setBorderTop(CellStyle.BORDER_THIN);
//        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
//        cellStyle.setBorderRight(CellStyle.BORDER_THIN);
//        cellStyle.setWrapText(true);
//
//        HSSFRow rowTitle = hssfSheet.createRow(1);
//        HSSFCell cellTitleNo = rowTitle.createCell(0);
//        cellTitleNo.setCellStyle(cellStyle);
//        cellTitleNo.setCellValue("NO");
//
//        HSSFCell cellTitleIdTransaksi = rowTitle.createCell(1);
//        cellTitleIdTransaksi.setCellStyle(cellStyle);
//        cellTitleIdTransaksi.setCellValue("Id Transaksi");
//
//        HSSFCell cellTanggalTransaksi = rowTitle.createCell(2);
//        cellTanggalTransaksi.setCellStyle(cellStyle);
//        cellTanggalTransaksi.setCellValue("Tanggal Transaksi");
//
//        HSSFCell cellPotongan = rowTitle.createCell(3);
//        cellPotongan.setCellStyle(cellStyle);
//        cellPotongan.setCellValue("Potongan");
//
//        int totalPotongan = 0;
//
//
//        for(int i = 2; i <= saldoTransactionArrayList.size()+1; i++){
//
//            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//            Date date = new Date(saldoTransactionArrayList.get(i-2).getTanggal_transaksi());
//            String tanggalTransaksi = sdf.format(date);
//
//            HSSFRow rowData = hssfSheet.createRow(i);
//            HSSFCell cellDataNo = rowData.createCell(0);
//            cellDataNo.setCellStyle(cellStyle);
//            cellDataNo.setCellValue(i-1);
//
//            HSSFCell cellDataIdTransaksi = rowData.createCell(1);
//            cellDataIdTransaksi.setCellStyle(cellStyle);
//            cellDataIdTransaksi.setCellValue(saldoTransactionArrayList.get(i-2).getId_transaksi_saldo());
//
//            HSSFCell cellDataTanggalTransaksi = rowData.createCell(2);
//            cellDataTanggalTransaksi.setCellStyle(cellStyle);
//            cellDataTanggalTransaksi.setCellValue(tanggalTransaksi);
//
//            HSSFCell cellDataPotongan = rowData.createCell(3);
//            cellDataPotongan.setCellStyle(cellStyle);
//            cellDataPotongan.setCellValue(convertToRupiah(saldoTransactionArrayList.get(i-2).getPotongan()));
//            totalPotongan+= saldoTransactionArrayList.get(i-2).getPotongan();
//
//        }
//
//        HSSFRow hssfRowTotal = hssfSheet.createRow(saldoTransactionArrayList.size() + 2);
//        HSSFCell titleTotal = hssfRowTotal.createCell(0);
//        titleTotal.setCellStyle(cellStyle);
//        titleTotal.setCellValue("Total");
//
//
//        HSSFCell cellTotalPotongan = hssfRowTotal.createCell(3);
//        cellTotalPotongan.setCellStyle(cellStyle);
//        cellTotalPotongan.setCellValue(convertToRupiah(totalPotongan));
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
//                showMessage(SettingDownloadExcel.this, "Location : " +filePath.getAbsolutePath());
//                finish();
//            }
//        }catch (Exception e){
//            Log.d("TAG", "exportToExcel: " + e.getMessage());
//        }
//    }
//
//    private void createExcelFileReportSaldo(){
//        File filePath = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "MyEwaste");
//
//        if(!filePath.exists()){
//            if(filePath.mkdir()){
//                filePath = new File(filePath.getAbsolutePath() + File.separator +"laporan_transaksi_nasabah_"+System.currentTimeMillis()+".xls");
//            }else{
//                showMessage(SettingDownloadExcel.this, "Failed To make Directory");
//            }
//        }else{
//            filePath = new File(filePath.getAbsolutePath() + File.separator +"laporan_transaksi_nasabah_"+System.currentTimeMillis()+".xls");
//        }
//        //todo create new workbook
//        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
//        //todo create new worksheet
//        HSSFSheet hssfSheet = hssfWorkbook.createSheet("Laporan Transaksi Nasabah");
//
//        //cell tanggal
//        HSSFRow rowTanggal = hssfSheet.createRow(0);
//        HSSFCell cellTanggal = rowTanggal.createCell(0);
//        cellTanggal.setCellValue("Tanggal :" + etStart.getText().toString() + " - " +etEnd.getText().toString());
//        hssfSheet.addMergedRegion(new CellRangeAddress(0,0,0,5));
//
//        //todo cell style
//        CellStyle cellStyle = hssfWorkbook.createCellStyle();
//        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
//        cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
//        cellStyle.setBorderTop(CellStyle.BORDER_THIN);
//        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
//        cellStyle.setBorderRight(CellStyle.BORDER_THIN);
//        cellStyle.setWrapText(true);
//
//        HSSFRow rowTitle = hssfSheet.createRow(1);
//        HSSFCell cellTitleNo = rowTitle.createCell(0);
//        cellTitleNo.setCellStyle(cellStyle);
//        cellTitleNo.setCellValue("NO");
//
//        HSSFCell cellTitleIdTransaksi = rowTitle.createCell(1);
//        cellTitleIdTransaksi.setCellStyle(cellStyle);
//        cellTitleIdTransaksi.setCellValue("Id Transaksi");
//
//        HSSFCell cellTanggalTransaksi = rowTitle.createCell(2);
//        cellTanggalTransaksi.setCellStyle(cellStyle);
//        cellTanggalTransaksi.setCellValue("Tanggal Transaksi");
//
//        HSSFCell cellNamaTeller = rowTitle.createCell(3);
//        cellNamaTeller.setCellStyle(cellStyle);
//        cellNamaTeller.setCellValue("Nama Teller");
//
//        HSSFCell cellNamaNasabah = rowTitle.createCell(4);
//        cellNamaNasabah.setCellStyle(cellStyle);
//        cellNamaNasabah.setCellValue("Nama Nasabah");
//
//        HSSFCell cellJumlahPenimbangan = rowTitle.createCell(5);
//        cellJumlahPenimbangan.setCellStyle(cellStyle);
//        cellJumlahPenimbangan.setCellValue("Penimbangan");
//
//        HSSFCell cellJumlahPenarikan = rowTitle.createCell(6);
//        cellJumlahPenarikan.setCellStyle(cellStyle);
//        cellJumlahPenarikan.setCellValue("Penarikan");
//
//        HSSFCell cellPotongan = rowTitle.createCell(7);
//        cellPotongan.setCellStyle(cellStyle);
//        cellPotongan.setCellValue("Potongan");
//
//        HSSFCell cellTotalDapat = rowTitle.createCell(8);
//        cellTotalDapat.setCellStyle(cellStyle);
//        cellTotalDapat.setCellValue("Total Didapatkan");
//
//        int totalPotongan = 0;
//        int totalPenerimaanBarang = 0;
//        int totalTarikSaldo = 0;
//
//
//        for(int i = 2; i <= saldoTransactionArrayList.size()+1; i++){
//
//            UserData dataTeller = lookForNamaUser(saldoTransactionArrayList.get(i-2).getId_penerima());
//            UserData dataNasabah = lookForNamaUser(saldoTransactionArrayList.get(i-2).getId_nasabah());
//            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//            Date date = new Date(saldoTransactionArrayList.get(i-2).getTanggal_transaksi());
//            String tanggalTransaksi = sdf.format(date);
//
//            HSSFRow rowData = hssfSheet.createRow(i);
//            HSSFCell cellDataNo = rowData.createCell(0);
//            cellDataNo.setCellStyle(cellStyle);
//            cellDataNo.setCellValue(i-1);
//
//            HSSFCell cellDataIdTransaksi = rowData.createCell(1);
//            cellDataIdTransaksi.setCellStyle(cellStyle);
//            cellDataIdTransaksi.setCellValue(saldoTransactionArrayList.get(i-2).getId_transaksi_saldo());
//
//            HSSFCell cellDataTanggalTransaksi = rowData.createCell(2);
//            cellDataTanggalTransaksi.setCellStyle(cellStyle);
//            cellDataTanggalTransaksi.setCellValue(tanggalTransaksi);
//
//            HSSFCell cellDataNamaTeller = rowData.createCell(3);
//            cellDataNamaTeller.setCellStyle(cellStyle);
//            cellDataNamaTeller.setCellValue(dataTeller.getName());
//
//            HSSFCell cellDataNamaNasabah = rowData.createCell(4);
//            cellDataNamaNasabah.setCellStyle(cellStyle);
//            cellDataNamaNasabah.setCellValue(dataNasabah.getName());
//
//            HSSFCell cellDataJumlahPenimbangan = rowData.createCell(5);
//            cellDataJumlahPenimbangan.setCellStyle(cellStyle);
//
//            HSSFCell cellDataJumlahPenarikan = rowData.createCell(6);
//            cellDataJumlahPenarikan.setCellStyle(cellStyle);
//
//            if(saldoTransactionArrayList.get(i-2).getJenis_transaksi().equals("SETOR")){
//                cellDataJumlahPenimbangan.setCellValue(convertToRupiah(saldoTransactionArrayList.get(i-2).getJumlah_transaksi()));
//                cellDataJumlahPenarikan.setCellValue(convertToRupiah(0));
//                totalPenerimaanBarang+= saldoTransactionArrayList.get(i-2).getJumlah_transaksi();
//            }else{
//                cellDataJumlahPenimbangan.setCellValue(convertToRupiah(0));
//                cellDataJumlahPenarikan.setCellValue(convertToRupiah(saldoTransactionArrayList.get(i-2).getJumlah_transaksi()));
//                totalTarikSaldo+= saldoTransactionArrayList.get(i-2).getJumlah_transaksi();
//            }
//
//            HSSFCell cellDataPotongan = rowData.createCell(7);
//            cellDataPotongan.setCellStyle(cellStyle);
//            cellDataPotongan.setCellValue(convertToRupiah(saldoTransactionArrayList.get(i-2).getPotongan()));
//            totalPotongan+= saldoTransactionArrayList.get(i-2).getPotongan();
//
//            HSSFCell cellDataTotalDapat = rowData.createCell(8);
//            cellDataTotalDapat.setCellStyle(cellStyle);
//            cellDataTotalDapat.setCellValue(convertToRupiah(saldoTransactionArrayList.get(i-2).getJumlah_transaksi() - saldoTransactionArrayList.get(i-2).getPotongan()));
//
//        }
//
//        HSSFRow hssfRowTotal = hssfSheet.createRow(saldoTransactionArrayList.size() + 2);
//        HSSFCell titleTotal = hssfRowTotal.createCell(0);
//        titleTotal.setCellStyle(cellStyle);
//        titleTotal.setCellValue("Total");
//
//        HSSFCell cellTotalPenimbangan = hssfRowTotal.createCell(5);
//        cellTotalPenimbangan.setCellStyle(cellStyle);
//        cellTotalPenimbangan.setCellValue(convertToRupiah(totalPenerimaanBarang));
//
//        HSSFCell cellTotalPenarikan = hssfRowTotal.createCell(6);
//        cellTotalPenarikan.setCellStyle(cellStyle);
//        cellTotalPenarikan.setCellValue(convertToRupiah(totalTarikSaldo));
//
//        HSSFCell cellTotalPotongan = hssfRowTotal.createCell(7);
//        cellTotalPotongan.setCellStyle(cellStyle);
//        cellTotalPotongan.setCellValue(convertToRupiah(totalPotongan));
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
//                showMessage(SettingDownloadExcel.this, "Location : " +filePath.getAbsolutePath());
//                finish();
//            }
//        }catch (Exception e){
//            Log.d("TAG", "exportToExcel: " + e.getMessage());
//        }
//    }
//
//    private void createExcelFileReportBarang(){
//        File filePath = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "MyEwaste");
//
//        if(!filePath.exists()){
//            if(filePath.mkdir()){
//                filePath = new File(filePath.getAbsolutePath() + File.separator +"laporan_penimbangan_nasabah_"+System.currentTimeMillis()+".xls");
//            }else{
//                showMessage(SettingDownloadExcel.this, "Failed To make Directory");
//            }
//        }else{
//            filePath = new File(filePath.getAbsolutePath() + File.separator +"laporan_penimbangan_nasabah_"+System.currentTimeMillis()+".xls");
//        }
//        //todo create new workbook
//        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
//        //todo create new worksheet
//        HSSFSheet hssfSheet = hssfWorkbook.createSheet("Laporan Penimbangan Barang");
//
//        //cell tanggal
//        HSSFRow rowTanggal =hssfSheet.createRow(0);
//        HSSFCell cellTanggal = rowTanggal.createCell(0);
//        cellTanggal.setCellValue("Tanggal :" + etStart.getText().toString() + " - " +etEnd.getText().toString());
//        hssfSheet.addMergedRegion(new CellRangeAddress(0,0,0,5));
//
//        //todo cell style
//        CellStyle cellStyle = hssfWorkbook.createCellStyle();
//        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
//        cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
//        cellStyle.setBorderTop(CellStyle.BORDER_THIN);
//        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
//        cellStyle.setBorderRight(CellStyle.BORDER_THIN);
//        cellStyle.setWrapText(true);
//
//        HSSFRow rowTitle = hssfSheet.createRow(1);
//        HSSFCell cellTitleNo = rowTitle.createCell(0);
//        cellTitleNo.setCellStyle(cellStyle);
//        cellTitleNo.setCellValue("NO");
//
//        HSSFCell cellTitleIdTransaksi = rowTitle.createCell(1);
//        cellTitleIdTransaksi.setCellStyle(cellStyle);
//        cellTitleIdTransaksi.setCellValue("Id Transaksi");
//
//        HSSFCell cellTanggalTransaksi = rowTitle.createCell(2);
//        cellTanggalTransaksi.setCellStyle(cellStyle);
//        cellTanggalTransaksi.setCellValue("Tanggal Transaksi");
//
//        HSSFCell cellTitleBarang = rowTitle.createCell(3);
//        cellTitleBarang.setCellStyle(cellStyle);
//        cellTitleBarang.setCellValue("Barang");
//
//        HSSFCell cellTitleJenisBarang = rowTitle.createCell(4);
//        cellTitleJenisBarang.setCellStyle(cellStyle);
//        cellTitleJenisBarang.setCellValue("Jenis Barang");
//
//        HSSFCell cellNamaTeller = rowTitle.createCell(5);
//        cellNamaTeller.setCellStyle(cellStyle);
//        cellNamaTeller.setCellValue("Nama Teller");
//
//        HSSFCell cellNamaNasabah = rowTitle.createCell(6);
//        cellNamaNasabah.setCellStyle(cellStyle);
//        cellNamaNasabah.setCellValue("Nama Nasabah");
//
//        HSSFCell cellJumlah = rowTitle.createCell(7);
//        cellJumlah.setCellStyle(cellStyle);
//        cellJumlah.setCellValue("Jumlah Barang");
//
//        HSSFCell cellSatuan = rowTitle.createCell(8);
//        cellSatuan.setCellStyle(cellStyle);
//        cellSatuan.setCellValue("Satuan Barang");
//
//        HSSFCell HargaBarang = rowTitle.createCell(9);
//        HargaBarang.setCellStyle(cellStyle);
//        HargaBarang.setCellValue("Harga Barang");
//
//        HSSFCell cellTotal = rowTitle.createCell(10);
//        cellTotal.setCellStyle(cellStyle);
//        cellTotal.setCellValue("Total");


//        for(int i = 2; i <= itemTransactionArrayList.size(); i++){
//
//            UserData dataTeller = lookForNamaUser(itemTransactionArrayList.get(i-2).getNo_teller());
//            UserData dataNasabah = lookForNamaUser(itemTransactionArrayList.get(i-2).getNo_nasabah());
//            ItemType dataJenisBarang = lookForNamaJenisBarang(itemTransactionArrayList.get(i-2).getNomor_jenis_barang());
//            UnitItem unitItem = lookForSatuan(dataJenisBarang.getNo_satuan_barang());
//            ItemMaster itemMaster = lookForBarang(dataJenisBarang.getNo_master_barang());
//            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//            Date date = new Date(itemTransactionArrayList.get(i-2).getTanggal_transaksi());
//            String tanggalTransaksi = sdf.format(date);
//
//            HSSFRow rowData = hssfSheet.createRow(i);
//            HSSFCell cellDataNo = rowData.createCell(0);
//            cellDataNo.setCellStyle(cellStyle);
//            cellDataNo.setCellValue(i-1);
//
//            HSSFCell cellDataIdTransaksi = rowData.createCell(1);
//            cellDataIdTransaksi.setCellStyle(cellStyle);
//            cellDataIdTransaksi.setCellValue(itemTransactionArrayList.get(i-2).getNo_transaksi_barang());
//
//            HSSFCell cellDataTanggalTransaksi = rowData.createCell(2);
//            cellDataTanggalTransaksi.setCellStyle(cellStyle);
//            cellDataTanggalTransaksi.setCellValue(tanggalTransaksi);
//
//            HSSFCell cellDataBarang = rowData.createCell(3);
//            cellDataBarang.setCellStyle(cellStyle);
//            cellDataBarang.setCellValue(itemMaster.getNama_master_barang());
//
//            HSSFCell cellDataJenisBarang = rowData.createCell(4);
//            cellDataJenisBarang.setCellStyle(cellStyle);
//            cellDataJenisBarang.setCellValue(dataJenisBarang.getNama_master_jenis_barang());
//
//            HSSFCell cellDataNamaTeller = rowData.createCell(5);
//            cellDataNamaTeller.setCellStyle(cellStyle);
//            cellDataNamaTeller.setCellValue(dataTeller.getName());
//
//            HSSFCell cellDataNamaNasabah = rowData.createCell(6);
//            cellDataNamaNasabah.setCellStyle(cellStyle);
//            cellDataNamaNasabah.setCellValue(dataNasabah.getName());
//
//
//
//            HSSFCell cellDataJumlah = rowData.createCell(7);
//            cellDataJumlah.setCellStyle(cellStyle);
//            cellDataJumlah.setCellValue(String.valueOf(itemTransactionArrayList.get(i-2).getJumlah()));
//
//            HSSFCell cellDataSatuan = rowData.createCell(8);
//            cellDataSatuan.setCellStyle(cellStyle);
//            cellDataSatuan.setCellValue(unitItem.getNamaSatuan());
//
//            HSSFCell cellDataHargaBarang = rowData.createCell(9);
//            cellDataHargaBarang.setCellStyle(cellStyle);
//            cellDataHargaBarang.setCellValue(convertToRupiah(dataJenisBarang.getHarga()));
//
//            HSSFCell cellDataTotal = rowData.createCell(10);
//            cellDataTotal.setCellStyle(cellStyle);
//            cellDataTotal.setCellValue(convertToRupiah(itemTransactionArrayList.get(i-2).getTotal_harga()));
//
//        }
//        Log.d("TAG", "createExcelFileReportBarang: " + filePath.getAbsolutePath());
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
//                showMessage(SettingDownloadExcel.this, "Location : " +filePath.getAbsolutePath());
//                finish();
//            }
//        }catch (Exception e){
//            Log.d("TAG", "exportToExcel: " + e.getMessage());
//        }
//    }
//
//    private UserData lookForNamaUser(String idUser){
//        for(UserData user : userDataArrayList){
//            if(user.getNo_regis().equals(idUser)){
//                return user;
//            }
//        }
//
//        return null;
//    }
//
//    private UnitItem lookForSatuan(String idSatuan){
//        for(UnitItem unitItem : unitItemArrayList){
//            if(unitItem.getNo_unit_item().equals(idSatuan)){
//                return unitItem;
//            }
//        }
//        return null;
//    }
//
//    private ItemMaster lookForBarang(String idBarang){
//        for(ItemMaster itemMaster : itemMasterArrayList){
//            if(itemMaster.getNo_item_master().equals(idBarang)){
//                return itemMaster;
//            }
//        }
//        return null;
//    }
//
//    private ItemType lookForNamaJenisBarang(String idJenisBarang){
//        for(ItemType itemType : itemTypeArrayList){
//            if(itemType.getNo_item_type().equals(idJenisBarang)){
//                return itemType;
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed();
//        return true;
//    }

}
