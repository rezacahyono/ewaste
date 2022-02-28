package com.example.myewaste.ui;

import static com.example.myewaste.utils.Constant.AVATAR_FOLDER;
import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Constant.IMAGE_FOLDER;
import static com.example.myewaste.utils.Constant.MODE_UPDATE;
import static com.example.myewaste.utils.Constant.NIK;
import static com.example.myewaste.utils.Constant.NONE;
import static com.example.myewaste.utils.Constant.USER_DATA;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myewaste.R;
import com.example.myewaste.SessionManagement;
import com.example.myewaste.databinding.ActivityProfileUserBinding;
import com.example.myewaste.databinding.DialogUploadAvatarBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.ui.login.LoginActivitiy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class ProfileUserActivity extends AppCompatActivity {

    private final int PICK_IMAGE_REQUEST = 100;

    private ProgressDialog loading;
    private String mode = "";
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private SessionManagement sessionManagement;
    private UserData userData = new UserData();
    private ActivityProfileUserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();
        MainToolbarBinding bindingToolbar = binding.mainToolbar;
        bindingToolbar.tvTitleBar.setText(R.string.profile);

        bindingToolbar.btnBack.setOnClickListener(v -> onBackPressed());

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();


        userData = getIntent().getParcelableExtra(EXTRAS_USER_DATA);
        if (getIntent().getParcelableExtra(EXTRAS_USER_DATA) != null) {
            setEditText(userData);
            loadImage(userData.getAvatar());
        }

        sessionManagement = new SessionManagement(this);

        binding.edtNik.setOnFocusChangeListener((view, b) -> {
            binding.btnCancel.setVisibility(View.VISIBLE);
            binding.btnSave.setText(R.string.update);
            mode = MODE_UPDATE;
        });
        binding.edtName.setOnFocusChangeListener((view, b) -> {
            binding.btnCancel.setVisibility(View.VISIBLE);
            binding.btnSave.setText(R.string.update);
            mode = MODE_UPDATE;
        });
        binding.tvDdGender.setOnFocusChangeListener((view, b) -> {
            binding.btnCancel.setVisibility(View.VISIBLE);
            binding.btnSave.setText(R.string.update);
            mode = MODE_UPDATE;
        });
        binding.edtPhone.setOnFocusChangeListener((view, b) -> {
            binding.btnCancel.setVisibility(View.VISIBLE);
            binding.btnSave.setText(R.string.update);
            mode = MODE_UPDATE;
        });
        binding.edtAddress.setOnFocusChangeListener((view, b) -> {
            binding.btnSave.setText(R.string.update);
            mode = MODE_UPDATE;
        });

        binding.btnCancel.setOnClickListener(v -> {
            clearFocusableEditText();
            setEditText(userData);
        });

        binding.btnSave.setOnClickListener(v -> {
            if (mode.equals(MODE_UPDATE)) {
                UserData userDataUpdate = new UserData();
                String name = Objects.requireNonNull(binding.edtName.getText()).toString();
                String nik = Objects.requireNonNull(binding.edtNik.getText()).toString();
                String gender = binding.tvDdGender.getText().toString();
                String phone = Objects.requireNonNull(binding.edtPhone.getText()).toString();
                String address = Objects.requireNonNull(binding.edtAddress.getText()).toString();
                if (nik.isEmpty()) {
                    binding.edtNik.setText(getResources().getString(R.string.input_can_not_be_empty, getResources().getString(R.string.nik)));
                } else if (name.isEmpty()) {
                    binding.edtName.setText(getResources().getString(R.string.input_can_not_be_empty, getResources().getString(R.string.name)));
                } else if (gender.isEmpty()) {
                    binding.tvDdGender.setText(getResources().getString(R.string.input_can_not_be_empty, getResources().getString(R.string.gender)));
                } else if (phone.isEmpty()) {
                    binding.edtPhone.setText(getResources().getString(R.string.input_can_not_be_empty, getResources().getString(R.string.phone)));
                } else if (address.isEmpty()) {
                    binding.edtAddress.setText(getResources().getString(R.string.input_can_not_be_empty, getResources().getString(R.string.address)));
                } else {
                    userDataUpdate.setNo_regis(userData.getNo_regis());
                    userDataUpdate.setAvatar(userData.getAvatar());
                    userDataUpdate.setPhoto_nik(userData.getPhoto_nik());
                    userDataUpdate.setNik(nik);
                    userDataUpdate.setName(name);
                    userDataUpdate.setGender(gender);
                    userDataUpdate.setPhone(phone);
                    userDataUpdate.setAddress(address);
                    checkHasSimiliarNik(userDataUpdate);
                    clearFocusableEditText();
                }
            } else {
                sessionManagement.removeUserSession();
                Intent intent = new Intent(this, LoginActivitiy.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        });

        binding.ibPickAvatar.setOnClickListener(v -> alertChooseActionAvatar());

        binding.ivAvatar.setOnClickListener(v -> alertChooseActionAvatar());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setAdapterSpinner();
    }

    private void setAdapterSpinner() {
        String[] listGender = getResources().getStringArray(R.array.list_gender);
        ArrayAdapter<String> arrayAdapterGender = new ArrayAdapter<>(this, R.layout.dropdown_item, listGender);
        binding.tvDdGender.setAdapter(arrayAdapterGender);
    }

    private void setEditText(UserData userData) {
        binding.edtNik.setText(userData.getNik());
        binding.edtName.setText(userData.getName());
        binding.edtPhone.setText(userData.getPhone());
        binding.edtAddress.setText(userData.getAddress());
        binding.tvDdGender.setText(userData.getGender());
        setAdapterSpinner();
    }

    private void clearFocusableEditText() {
        binding.edtNik.clearFocus();
        binding.edtName.clearFocus();
        binding.edtPhone.clearFocus();
        binding.edtAddress.clearFocus();
        binding.tvDdGender.clearFocus();
        binding.btnSave.setText(R.string.logout);
        binding.btnCancel.setVisibility(View.GONE);
        mode = "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            Uri filePath = data.getData();
            alertUpload(filePath);

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

    private void alertChooseActionAvatar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileUserActivity.this);
        DialogUploadAvatarBinding bindingDialog = DialogUploadAvatarBinding.inflate(getLayoutInflater());
        View viewDialog = bindingDialog.getRoot();

        builder.setView(viewDialog);
        AlertDialog dialog = builder.create();
        dialog.show();

        bindingDialog.ibCancel.setOnClickListener(v -> dialog.dismiss());
        bindingDialog.ibSubmit.setOnClickListener(v -> {
            selectImage();
            dialog.dismiss();
        });
        bindingDialog.ibDeleteAvatar.setOnClickListener(v -> {
            userData.setAvatar(NONE);
            alertDelete(userData);
            dialog.dismiss();
        });

    }

    private void alertUpload(Uri uri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileUserActivity.this);
        builder.setTitle(getResources().getString(R.string.upload));
        builder.setMessage(getResources().getString(R.string.message_avatar, "upload"));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.yes), (dialogInterface, i) -> {
            uploadImageToStorage(uri);
            dialogInterface.dismiss();
        });
        builder.setNegativeButton(getResources().getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void alertDelete(UserData userData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileUserActivity.this);
        builder.setTitle(getResources().getString(R.string.delete));
        builder.setMessage(getResources().getString(R.string.message_avatar, "delete"));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.yes), (dialogInterface, i) -> {
            Log.d("TAG", "alertDelete: avatara" + userData.getAvatar());
            actionDeleteAvatar(userData);
            dialogInterface.dismiss();
        });
        builder.setNegativeButton(getResources().getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void uploadImageToStorage(Uri uri) {
        if (uri != null) {
            loading = ProgressDialog.show(this,
                    null,
                    getResources().getString(R.string.loading_message),
                    true,
                    false);
            StorageReference refImageItem = storageReference.child(IMAGE_FOLDER + AVATAR_FOLDER + userData.getNo_regis());
            refImageItem.putFile(uri)
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
                            userData.setAvatar(downloadUri.toString());
                            submitUserData(userData);
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.submit_failure, "Foto"), Toast.LENGTH_SHORT).show();
                        }
                        loading.dismiss();
                    });
        }
    }

    private void checkHasSimiliarNik(UserData userData) {
        databaseReference.child(USER_DATA).orderByChild(NIK).equalTo(userData.getNik()).get().addOnCompleteListener(task -> {
            DataSnapshot result = task.getResult();
            if (result != null) {
                if (result.exists()) {
                    for (DataSnapshot dataSnapshot : result.getChildren()) {
                        UserData userDataResult = dataSnapshot.getValue(UserData.class);
                        if (userDataResult != null) {
                            if (userDataResult.getNo_regis().equals(userData.getNo_regis())) {
                                submitUserData(userData);
                            } else {
                                Toast.makeText(ProfileUserActivity.this, getResources().getString(R.string.nik_is_registerd), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    submitUserData(userData);

                }
            }
        });
    }

    private void submitUserData(UserData userData) {
        if (userData != null) {
            databaseReference
                    .child(USER_DATA)
                    .child(userData.getNo_regis())
                    .setValue(userData)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "berhasil", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "gagal", Toast.LENGTH_SHORT).show();
                    });
            loadImage(userData.getAvatar());
        }
    }

    private void actionDeleteAvatar(UserData userData) {
        if (userData != null) {
            loading = ProgressDialog.show(this,
                    null,
                    getResources().getString(R.string.loading_message),
                    true,
                    false);
            storageReference.child(IMAGE_FOLDER).child(AVATAR_FOLDER).child(userData.getNo_regis()).delete()
                    .addOnSuccessListener(unused -> Toast.makeText(ProfileUserActivity.this, getResources().getString(R.string.action_delete_avatar_message, "erhasil"), Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(ProfileUserActivity.this, getResources().getString(R.string.action_delete_avatar_message, "gagal"), Toast.LENGTH_SHORT).show());
            submitUserData(userData);
            loading.dismiss();
        }
    }

    private void loadImage(String image) {
        if (!image.equals(NONE)) {
            Glide.with(this)
                    .load(image)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(binding.ivAvatar);
        } else {
            binding.ivAvatar.setImageResource(R.drawable.ic_placeholder);
        }
    }
}
