package com.example.myewaste;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myewaste.model.item.ItemType;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DetailActivityJenisBarang extends AppCompatActivity {
    private ImageView fotoBarang;
    private TextView tvNamaBarang,tvNamaJenisBarang, tvSatuan, tvHarga;
    private Button btnEdit, btnHapus;
    private ItemType itemType;
    private DatabaseReference referenceBarang;
    private DatabaseReference referenceSatuan;
    private DatabaseReference jenisBarangReference;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_jenis_barang);

        itemType = getIntent().getParcelableExtra("EXTRA_JENIS_BARANG");
        fotoBarang = findViewById(R.id.iv_foto_barang_djb);
        tvNamaBarang = findViewById(R.id.tv_nama_barang_djb);
        tvNamaJenisBarang = findViewById(R.id.tv_nama_jenis_barang_djb);
        tvSatuan = findViewById(R.id.tv_nama_satuan_djb);
        tvHarga = findViewById(R.id.tv_harga_djb);
        btnEdit = findViewById(R.id.btn_edit_djb);
        btnHapus = findViewById(R.id.btn_hapus_djb);

        getSupportActionBar().setTitle("Detail Jenis Barang");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        referenceBarang = FirebaseDatabase.getInstance().getReference("barang");
        referenceSatuan = FirebaseDatabase.getInstance().getReference("satuan_barang");
        jenisBarangReference  = FirebaseDatabase.getInstance().getReference("jenis_barang");
//        initializeData();
//
//        btnEdit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                editData(itemType);
//            }
//        });

        btnHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //todo delete to risky to use it
//                hapusData(itemType);
            }
        });

    }

//    private void initializeData(){
//        tvNamaJenisBarang.setText(itemType.getNama_master_jenis_barang());
//        tvHarga.setText(convertToRupiah(itemType.getHarga()));
//        loadDataBarang();
//        loadDataSatuan();
//    }
//
//    private void loadDataBarang(){
//        referenceBarang.child(itemType.getNo_master_barang()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ItemMaster itemMaster = snapshot.getValue(ItemMaster.class);
//                tvNamaBarang.setText(itemMaster.getNama_master_barang());
//                loadImage(itemMaster.getFoto_master_barang(), fotoBarang, DetailActivityJenisBarang.this);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    private void loadDataSatuan(){
//        referenceSatuan.child(itemType.getNo_satuan_barang()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                UnitItem unitItem = snapshot.getValue(UnitItem.class);
//                tvSatuan.setText(unitItem.getNamaSatuan());
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    private void hapusData(ItemType jenisBarang)
//    {
//        jenisBarangReference.child(jenisBarang.getNo_master_jenis_barang()).removeValue(new DatabaseReference.CompletionListener() {
//            @Override
//            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
//                Toast.makeText(getApplicationContext(), "Berhasil Dihapus", Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        });
//    }
//
//    private void editData(ItemType jenisBarang)
//    {
//        Intent intent = new Intent(DetailActivityJenisBarang.this, AddUpdateItemTypeActivity.class);
//        intent.putExtra("EXTRAS_JENIS_BARANG", jenisBarang);
//        startActivity(intent);
//        finish();
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed();
//        return true;
//    }
}
