package com.example.myewaste.ui.admin.task;

import static com.example.myewaste.utils.Constant.DEFAULT_NO_ITEM_MASTER;
import static com.example.myewaste.utils.Constant.EXTRAS_ACTION_MODE;
import static com.example.myewaste.utils.Constant.EXTRAS_ITEM_MASTER;
import static com.example.myewaste.utils.Constant.IMAGE_FOLDER;
import static com.example.myewaste.utils.Constant.ITEM_FOLDER;
import static com.example.myewaste.utils.Constant.ITEM_MASTER;
import static com.example.myewaste.utils.Constant.MODE_ADD;
import static com.example.myewaste.utils.Constant.MODE_UPDATE;
import static com.example.myewaste.utils.Util.increseNumber;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myewaste.R;
import com.example.myewaste.databinding.ActivityAddUpdateItemMasterBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.item.ItemMaster;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.Objects;

public class AddUpdateItemMasterActivity extends AppCompatActivity {
    private Uri filePath;
    private ItemMaster itemMaster;
    private final int PICK_IMAGE_REQUEST = 100;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private ProgressDialog loading;
    private String mode;

    private ActivityAddUpdateItemMasterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddUpdateItemMasterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();
        MainToolbarBinding bindingToolbar = binding.mainToolbar;
        bindingToolbar.tvTitleBar.setText(R.string.add_item);

        bindingToolbar.btnBack.setOnClickListener(v -> onBackPressed());

        itemMaster = new ItemMaster();

        if (getIntent().getStringExtra(EXTRAS_ACTION_MODE) != null) {
            mode = getIntent().getStringExtra(EXTRAS_ACTION_MODE);
        }

        if (getIntent().getParcelableExtra(EXTRAS_ITEM_MASTER) != null) {
            itemMaster = getIntent().getParcelableExtra(EXTRAS_ITEM_MASTER);
            bindingToolbar.tvTitleBar.setText(getResources().getString(R.string.update_item));
            binding.btnSave.setText(getResources().getString(R.string.update));
            Glide.with(this)
                    .load(itemMaster.getPhoto())
                    .error(R.drawable.ic_item_master)
                    .placeholder(R.color.green_20)
                    .into(binding.ivPhotoItem);
            binding.edtNameItemMaster.setText(itemMaster.getName());
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        binding.flUploadKtp.setOnClickListener(v -> selectImage());

        binding.btnCancel.setOnClickListener(v -> onBackPressed());

        binding.btnSave.setOnClickListener(v -> fetchDataItemMaster(new onFetchDataListener() {

            @Override
            public void onSuccessGenerateData(String noItemMaster, boolean hasSimiliarName) {
                itemMaster.setNo_item_master(noItemMaster);
                itemMaster.setName(Objects.requireNonNull(binding.edtNameItemMaster.getText()).toString());
                uploadImage((uploaded, uri) -> {
                    if (uploaded && !hasSimiliarName) {
                        itemMaster.setPhoto(uri);
                        submitItemMaster(itemMaster);
                    } else {
                        if (!uploaded) {
                            Toast.makeText(AddUpdateItemMasterActivity.this, getResources().getString(R.string.submit_failure, "Foto"), Toast.LENGTH_SHORT).show();
                        }
                        if (hasSimiliarName) {
                            Toast.makeText(AddUpdateItemMasterActivity.this, getResources().getString(R.string.hasSimiliar, Objects.requireNonNull(binding.edtNameItemMaster.getText()).toString()), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onSuccessCheckSimiliarName(boolean hasSimiliarName) {
                uploadImage((uploaded, uri) -> {
                    if (uploaded && !hasSimiliarName) {
                        itemMaster.setName(Objects.requireNonNull(binding.edtNameItemMaster.getText()).toString());
                        itemMaster.setPhoto(uri);
                        submitItemMaster(itemMaster);
                    } else {
                        if (!uploaded) {
                            Toast.makeText(AddUpdateItemMasterActivity.this, getResources().getString(R.string.submit_failure, "Foto"), Toast.LENGTH_SHORT).show();
                        }
                        if (hasSimiliarName) {
                            Toast.makeText(AddUpdateItemMasterActivity.this, getResources().getString(R.string.hasSimiliar, Objects.requireNonNull(binding.edtNameItemMaster.getText()).toString()), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            filePath = data.getData();
            binding.ivPhotoItem.setVisibility(View.VISIBLE);
            binding.ivPhotoItem.setImageURI(data.getData());
            binding.ivPhotoItem.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }


    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }

    private void submitItemMaster(ItemMaster itemMaster) {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        DatabaseReference referenceItemMaster = databaseReference.child(ITEM_MASTER);
        referenceItemMaster.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference referenceItemMaster = databaseReference.child(ITEM_MASTER).child(itemMaster.getNo_item_master());
                referenceItemMaster.setValue(itemMaster).addOnSuccessListener(aVoid -> {
                    if (mode.equals(MODE_ADD)) {
                        Toast.makeText(AddUpdateItemMasterActivity.this, getResources().getString(R.string.submit_success, getResources().getString(R.string.item_master)), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AddUpdateItemMasterActivity.this, getResources().getString(R.string.update_success, getResources().getString(R.string.item_master)), Toast.LENGTH_SHORT).show();
                    }
                    loading.dismiss();
                    navigateToItemMaster();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loading.dismiss();
                Toast.makeText(AddUpdateItemMasterActivity.this, getResources().getString(R.string.submit_failure, getResources().getString(R.string.item_master)), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void fetchDataItemMaster(final onFetchDataListener listener) {
        databaseReference.child(ITEM_MASTER).get()
                .addOnCompleteListener(task -> {
                    String noItemMaster = DEFAULT_NO_ITEM_MASTER;
                    boolean hasSimiliarName = false;
                    if (task.isSuccessful()) {
                        DataSnapshot result = task.getResult();
                        if (result != null) {
                            for (DataSnapshot dataSnapshot : result.getChildren()) {
                                String name = Objects.requireNonNull(dataSnapshot.getValue(ItemMaster.class)).getName();
                                if (mode.equals(MODE_ADD)) {
                                    String noItem = dataSnapshot.getKey();
                                    if (noItem != null) {
                                        noItemMaster = increseNumber(noItem);
                                    }
                                    if (name.equals(Objects.requireNonNull(binding.edtNameItemMaster.getText()).toString())) {
                                        hasSimiliarName = true;
                                    }
                                } else {
                                    if (name.equals(Objects.requireNonNull(binding.edtNameItemMaster.getText()).toString()) && !Objects.equals(dataSnapshot.getKey(), itemMaster.getNo_item_master())) {
                                        hasSimiliarName = true;
                                    }
                                }
                            }
                        }
                    }
                    if (mode.equals(MODE_ADD)) {
                        listener.onSuccessGenerateData(noItemMaster, hasSimiliarName);
                    } else {
                        listener.onSuccessCheckSimiliarName(hasSimiliarName);
                    }
                });
    }

    private void uploadImage(final onUploadFileListener listener) {
        if (filePath != null) {
            StorageReference refImageItem = storageReference.child(IMAGE_FOLDER + ITEM_FOLDER + itemMaster.getNo_item_master());
            refImageItem.putFile(filePath)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(this, getResources().getString(R.string.submit_failure, "Foto"), Toast.LENGTH_SHORT).show();
                        }
                        return refImageItem.getDownloadUrl();
                    })
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            assert downloadUri != null;
                            listener.onSucces(true, downloadUri.toString());
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.submit_failure, "Foto"), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            if (mode.equals(MODE_UPDATE)) {
                listener.onSucces(true, itemMaster.getPhoto());
            } else {
                listener.onSucces(true, "none");
            }
        }
    }

    private void navigateToItemMaster() {
        Intent intent = new Intent(this, ItemMasterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public interface onFetchDataListener {
        void onSuccessGenerateData(String noItemMaster, boolean hasSimiliarName);

        void onSuccessCheckSimiliarName(boolean hasSimiliarName);
    }

    public interface onUploadFileListener {
        void onSucces(boolean uploaded, String uri);
    }
}