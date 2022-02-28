package com.example.myewaste;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myewaste.model.item.ItemMaster;
import com.example.myewaste.ui.admin.task.AddUpdateItemMasterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class DetailMasterBarang extends AppCompatActivity {
    ImageView ivDetailFotoBarang;
    TextView tvDetailNamaBarang;
    LinearLayout btnDetailEditBarang;
    LinearLayout btnDetailHapusBarang;
    ItemMaster dataBarang;
    private static final String TAG = "DetailMasterBarang";
    private static final String PACKAGE_MESSAGE = "Data_Master_barang";
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_master_barang);

        Intent intent = getIntent();
        dataBarang = intent.getParcelableExtra("barang");
        ivDetailFotoBarang = (ImageView) findViewById(R.id.ivDetailFotoBarang);
        loadImage(dataBarang.getPhoto(), ivDetailFotoBarang);
        tvDetailNamaBarang = (TextView) findViewById(R.id.tvDetailNamaBarang);
        tvDetailNamaBarang.setText(dataBarang.getName());
        btnDetailEditBarang = (LinearLayout) findViewById(R.id.btnDetailEditBarang);
        btnDetailEditBarang.setOnClickListener(view -> doEditBarang());
        btnDetailHapusBarang = (LinearLayout) findViewById(R.id.btnDetailHapusBarang);


        getSupportActionBar().setTitle("Detail Mater Barang");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnDetailHapusBarang.setOnClickListener(view -> {
            //todo delete to risky to use it
//            showConfirmDeleteDialog();
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    void loadImage(String imageSource, ImageView bindOn){
        if(!imageSource.equals("none")){
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Drawable drawableBitmap = new BitmapDrawable(DetailMasterBarang.this.getResources(), bitmap);
                    bindOn.setBackground(drawableBitmap);
                    Log.d(TAG, "onBitmapLoaded: loaded");
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    bindOn.setBackgroundResource(R.drawable.ic_launcher_foreground);
                    Log.d(TAG, "onBitmapLoaded: failed");
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    bindOn.setBackgroundResource(R.drawable.progress);
                    Log.d(TAG, "onBitmapLoaded: prepare");
                }
            };

            Picasso.get().load(imageSource).into(target);
            bindOn.setTag(target);
            Picasso.get().setLoggingEnabled(true);
        }
    }

    void doEditBarang(){
        Intent intent = new Intent(DetailMasterBarang.this, AddUpdateItemMasterActivity.class);
        intent.putExtra(PACKAGE_MESSAGE, dataBarang);
        startActivity(intent);
        finish();
    }

    void showConfirmDeleteDialog(){
        databaseReference = FirebaseDatabase.getInstance().getReference("barang");

        AlertDialog confirmationDeleteDialog = new AlertDialog.Builder(this)
                .setTitle("Hapus Data Barang")
                .setMessage("Apakah anda yakin ingin menghapus " + dataBarang.getName() + " ?")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        databaseReference.child(dataBarang.getNo_item_master()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(DetailMasterBarang.this, "Berhasil Menghapus Data", Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }
                        });
                    }
                })
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}