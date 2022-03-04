package com.example.myewaste.ui.component.task;

import static com.example.myewaste.utils.Constant.AKTIF;
import static com.example.myewaste.utils.Constant.DEFAULT_NO_REGIST_NASABAH;
import static com.example.myewaste.utils.Constant.DEFAULT_NO_REGIST_SUPER_ADMIN;
import static com.example.myewaste.utils.Constant.DEFAULT_NO_REGIST_TELLER;
import static com.example.myewaste.utils.Constant.EXTRAS_ACTION_MODE;
import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Constant.FROM_LOGIN;
import static com.example.myewaste.utils.Constant.IMAGE_FOLDER;
import static com.example.myewaste.utils.Constant.KTP_FOLDER;
import static com.example.myewaste.utils.Constant.MODE;
import static com.example.myewaste.utils.Constant.MODE_ADD;
import static com.example.myewaste.utils.Constant.MODE_UPDATE;
import static com.example.myewaste.utils.Constant.NASABAH;
import static com.example.myewaste.utils.Constant.NONE;
import static com.example.myewaste.utils.Constant.NO_REGIS;
import static com.example.myewaste.utils.Constant.SALDO_NASABAH;
import static com.example.myewaste.utils.Constant.SUPER_ADMIN;
import static com.example.myewaste.utils.Constant.TELLER;
import static com.example.myewaste.utils.Constant.USER;
import static com.example.myewaste.utils.Constant.USER_DATA;
import static com.example.myewaste.utils.Mode.MODE_NASABAH;
import static com.example.myewaste.utils.Utils.convertMd5;
import static com.example.myewaste.utils.Utils.getRegisterCode;
import static com.example.myewaste.utils.Utils.increseNumber;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myewaste.R;
import com.example.myewaste.databinding.ActivityAddUserBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.saldo.Saldo;
import com.example.myewaste.model.user.User;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.utils.Mode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class AddUserActivity extends AppCompatActivity {

    private Mode modeUser;
    private String mode;
    private static final int PICK_IMAGE_REQUEST = 100;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private Uri filePath;
    private ProgressDialog loading;
    private ActivityAddUserBinding binding;
    private UserData userData;
    private User user;
    private boolean hasSimiliarUsernameAndNik = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();
        MainToolbarBinding bindingToolbar = binding.mainToolbar;

        userData = new UserData();
        user = new User();

        boolean fromLogin = getIntent().getBooleanExtra(FROM_LOGIN, false);
        if (fromLogin) {
            bindingToolbar.getRoot().setVisibility(View.GONE);
            binding.ivLogoRegister.setVisibility(View.VISIBLE);
        } else {
            bindingToolbar.getRoot().setVisibility(View.VISIBLE);
            binding.ivLogoRegister.setVisibility(View.GONE);
            binding.edtLayoutPassword.setVisibility(View.GONE);
            bindingToolbar.btnBack.setOnClickListener(view -> onBackPressed());
        }

        if (getIntent().getStringExtra(EXTRAS_ACTION_MODE) != null) {
            mode = getIntent().getStringExtra(EXTRAS_ACTION_MODE);
        }
        if (mode.equals(MODE_ADD)) {
            binding.edtLayoutPassword.setVisibility(View.VISIBLE);
        }else {
            binding.btnRegister.setText(R.string.update);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        if (getIntent().getParcelableExtra(EXTRAS_USER_DATA) != null) {
            userData = getIntent().getParcelableExtra(EXTRAS_USER_DATA);
            fetchUser(userData.getNo_regis());
        }

        if (getIntent().hasExtra(MODE)) {
            modeUser = (Mode) getIntent().getSerializableExtra(MODE);
            switch (modeUser) {
                case MODE_SUPER_ADMIN:
                    bindingToolbar.tvTitleBar.setText(R.string.data_admin);
                    binding.tvRegister.setText(getResources().getString(R.string.register_by, "Super Admin"));
                    break;
                case MODE_TELLER:
                    bindingToolbar.tvTitleBar.setText(R.string.data_teller);
                    binding.tvRegister.setText(getResources().getString(R.string.register_by, "Teller"));
                    break;
                case MODE_NASABAH:
                    bindingToolbar.tvTitleBar.setText(R.string.data_nasabah);
                    binding.tvRegister.setText(getResources().getString(R.string.register));
                    break;
                default:
                    break;
            }
        }

        binding.flUploadKtp.setOnClickListener(v -> selectImage());

        binding.btnRegister.setOnClickListener(v -> {
            int btnId = binding.rgGender.getCheckedRadioButtonId();
            RadioButton rbGender = findViewById(btnId);
            String nik = Objects.requireNonNull(binding.edtNik.getText()).toString().toLowerCase();
            String username = Objects.requireNonNull(binding.edtUsername.getText()).toString().toLowerCase();
            String name = Objects.requireNonNull(binding.edtName.getText()).toString().toLowerCase();
            String password = convertMd5(Objects.requireNonNull(binding.edtPassword.getText()).toString().toLowerCase());
            String gender = Objects.requireNonNull(rbGender.getText().toString()).toLowerCase();
            String phone = Objects.requireNonNull(binding.edtPhone.getText()).toString().toLowerCase();
            String address = Objects.requireNonNull(binding.edtAddress.getText()).toString().toLowerCase();

            if (nik.isEmpty()) {
                binding.edtNik.setError(getResources().getString(R.string.input_can_not_be_empty, getResources().getString(R.string.nik)));
            } else if (username.isEmpty()) {
                binding.edtUsername.setError(getResources().getString(R.string.input_can_not_be_empty, getResources().getString(R.string.username)));
            } else if (name.isEmpty()) {
                binding.edtName.setError(getResources().getString(R.string.input_can_not_be_empty, getResources().getString(R.string.name)));
            } else if (password.isEmpty()) {
                binding.edtPassword.setError(getResources().getString(R.string.input_can_not_be_empty, getResources().getString(R.string.password)));
            } else if (phone.isEmpty()) {
                binding.edtPhone.setError(getResources().getString(R.string.input_can_not_be_empty, getResources().getString(R.string.phone)));
            } else if (address.isEmpty()) {
                binding.edtAddress.setError(getResources().getString(R.string.input_can_not_be_empty, getResources().getString(R.string.address)));
            } else {
                fetchDataUserDataAndUser(new onFetchDataListener() {
                    @Override
                    public void onSuccessGenerateData(String noRegis, boolean hasSimiliar) {
                        userData.setNo_regis(noRegis);
                        userData.setAvatar("none");
                        userData.setNik(nik);
                        userData.setName(name);
                        userData.setGender(gender);
                        userData.setPhone(phone);
                        userData.setAddress(address);
                        user.setNo_regis(noRegis);
                        user.setUsername(username);
                        user.setPassword(password);
                        user.setStatus(AKTIF);
                        uploadImage((uploaded, uri) -> {
                            if (uploaded && !hasSimiliar) {
                                userData.setPhoto_nik(uri);
                                submitUserDataAndUser(userData, user);
                            } else {
                                if (!uploaded) {
                                    Toast.makeText(AddUserActivity.this, getResources().getString(R.string.submit_failure, "Foto"), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AddUserActivity.this, getResources().getString(R.string.nik_is_registerd), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onSuccessCheckSimiliar(boolean hasSimiliar) {
                        uploadImage((uploaded, uri) -> {
                            if (uploaded && !hasSimiliar) {
                                userData.setNik(nik);
                                userData.setName(name);
                                userData.setGender(gender);
                                userData.setPhone(phone);
                                userData.setAddress(address);
                                user.setUsername(username);
                                user.setPassword(password);
                                submitUserDataAndUser(userData, user);
                            } else {
                                if (!uploaded) {
                                    Toast.makeText(AddUserActivity.this, getResources().getString(R.string.submit_failure, "Foto"), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AddUserActivity.this, getResources().getString(R.string.nik_is_registerd), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                binding.ivKtp.setVisibility(View.GONE);
                binding.tvClickChooseImage.setVisibility(View.VISIBLE);
            }
        });

        binding.btnCancel.setOnClickListener(v -> onBackPressed());
    }

    private void setUserDataExtras(UserData userData, User user) {
        binding.edtNik.setText(userData.getNik());
        binding.edtName.setText(userData.getName());
        binding.edtUsername.setText(user.getUsername());
        if (userData.getGender().equalsIgnoreCase(getString(R.string.male))) {
            binding.rbMale.setChecked(true);
        } else {
            binding.rbFemale.setChecked(true);
        }
        binding.edtPhone.setText(userData.getPhone());
        binding.edtAddress.setText(userData.getAddress());
        loadImage(userData.getPhoto_nik());
    }

    private void loadImage(String image) {
        if (!image.equals(NONE)) {
            Glide.with(this)
                    .load(image)
                    .into(binding.ivKtp);
        }
    }

    private void fetchUser(String noRegis) {
        databaseReference.child(USER).orderByChild(NO_REGIS).equalTo(noRegis).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User userResult = dataSnapshot.getValue(User.class);
                    if (userResult != null) {
                        user = userResult;
                    }
                }
                setUserDataExtras(userData, user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            filePath = data.getData();
            binding.ivKtp.setVisibility(View.VISIBLE);
            binding.ivKtp.setImageURI(data.getData());
            binding.ivKtp.setScaleType(ImageView.ScaleType.CENTER_CROP);
            binding.tvClickChooseImage.setVisibility(View.GONE);
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

    private void submitUserDataAndUser(UserData userData, User user) {
        if (userData != null && user != null) {
            loading = ProgressDialog.show(this,
                    null,
                    getResources().getString(R.string.loading_message),
                    true,
                    false);
            databaseReference.child(USER_DATA).child(userData.getNo_regis()).setValue(userData)
                    .addOnSuccessListener(unused -> {
                        if (mode.equals(MODE_ADD)) {
                            Toast.makeText(this, getResources().getString(R.string.submit_success, userData.getName()), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.update_success, userData.getName()), Toast.LENGTH_SHORT).show();
                        }
                        loading.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, getResources().getString(R.string.failure), Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    });
            databaseReference.child(USER).child(user.getUsername()).setValue(user)
                    .addOnSuccessListener(unused -> {
                        if (mode.equals(MODE_ADD)) {
                            Toast.makeText(this, getResources().getString(R.string.submit_success, userData.getName()), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.update_success, userData.getName()), Toast.LENGTH_SHORT).show();
                        }
                        loading.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, getResources().getString(R.string.failure), Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    });
            if (mode.equals(MODE_ADD) && getRegisterCode(userData.getNo_regis()).equalsIgnoreCase(NASABAH)) {
                Saldo saldo = new Saldo(userData.getNo_regis(), 0);
                databaseReference.child(SALDO_NASABAH).child(userData.getNo_regis()).setValue(saldo)
                        .addOnSuccessListener(unused -> {
                            if (mode.equals(MODE_ADD)) {
                                Toast.makeText(this, getResources().getString(R.string.submit_success, userData.getName()), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, getResources().getString(R.string.update_success, userData.getName()), Toast.LENGTH_SHORT).show();
                            }
                            loading.dismiss();
                        }).addOnFailureListener(e -> {
                    Toast.makeText(this, getResources().getString(R.string.failure), Toast.LENGTH_SHORT).show();
                    loading.dismiss();
                });
            }
            navigateToBack();

        }

    }

    private void fetchDataUserDataAndUser(final onFetchDataListener listener) {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        DatabaseReference databaseReferenceUserData = databaseReference.child(USER_DATA);
        DatabaseReference databaseReferenceUser = databaseReference.child(USER);
        databaseReferenceUserData.get().addOnCompleteListener(task -> {
            String noRegis;
            if (modeUser.equals(Mode.MODE_SUPER_ADMIN)) {
                noRegis = DEFAULT_NO_REGIST_SUPER_ADMIN;
            } else if (modeUser.equals(Mode.MODE_TELLER)) {
                noRegis = DEFAULT_NO_REGIST_TELLER;
            } else {
                noRegis = DEFAULT_NO_REGIST_NASABAH;
            }

            if (task.isSuccessful()) {
                DataSnapshot result = task.getResult();
                if (result != null) {
                    for (DataSnapshot dataSnapshot : result.getChildren()) {
                        boolean isValid = false;
                        UserData userDataResult = dataSnapshot.getValue(UserData.class);
                        String dataRegis = dataSnapshot.getKey();
                        if (userDataResult != null && dataRegis != null) {
                            if (mode.equals(MODE_ADD)) {
                                String extractRegisterCode = getRegisterCode(dataRegis.toLowerCase());
                                if ((extractRegisterCode.equals(NASABAH) && (modeUser.equals(MODE_NASABAH)))
                                        || (extractRegisterCode.equals(TELLER) && modeUser.equals(Mode.MODE_TELLER))
                                        || (extractRegisterCode.equals(SUPER_ADMIN) && modeUser.equals(Mode.MODE_SUPER_ADMIN))) {
                                    isValid = true;
                                }
                                if (isValid) {
                                    noRegis = increseNumber(dataRegis);
                                }
                                if (userDataResult.getNik().equals(Objects.requireNonNull(binding.edtNik.getText()).toString())) {
                                    hasSimiliarUsernameAndNik = true;
                                }
                            } else {
                                if (userDataResult.getNik().equals(Objects.requireNonNull(binding.edtNik.getText()).toString()) && !Objects.equals(dataSnapshot.getKey(), userData.getNo_regis())) {
                                    hasSimiliarUsernameAndNik = true;
                                }
                            }
                        }
                    }
                }
            }
            databaseReferenceUser.get().addOnCompleteListener(taskUser -> {
                if (taskUser.isSuccessful()) {
                    DataSnapshot resultUser = taskUser.getResult();
                    if (resultUser != null) {
                        for (DataSnapshot dataSnapshot : resultUser.getChildren()) {
                            User userResult = dataSnapshot.getValue(User.class);
                            if (userResult != null) {
                                if (userResult.getUsername().equalsIgnoreCase(binding.edtUsername.toString())) {
                                    hasSimiliarUsernameAndNik = true;
                                }
                            }
                        }

                    }
                }
            });
            if (mode.equals(MODE_ADD)) {
                listener.onSuccessGenerateData(noRegis, hasSimiliarUsernameAndNik);
            } else {
                listener.onSuccessCheckSimiliar(hasSimiliarUsernameAndNik);
            }
            loading.dismiss();
        });
    }


    private void uploadImage(final onUploadImageListener listenerUpload) {
        loading = ProgressDialog.show(this,
                null,
                getResources().getString(R.string.loading_message),
                true,
                false);
        if (filePath != null) {
            StorageReference refStorageImageKtp = storageReference.child(IMAGE_FOLDER + KTP_FOLDER + userData.getNo_regis());
            refStorageImageKtp.putFile(filePath)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(this, getResources().getString(R.string.submit_failure, "Foto"), Toast.LENGTH_SHORT).show();
                            loading.dismiss();
                        }
                        return refStorageImageKtp.getDownloadUrl();
                    })
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            assert downloadUri != null;
                            listenerUpload.onSuccess(true, downloadUri.toString());
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.submit_failure, "Foto"), Toast.LENGTH_SHORT).show();
                        }
                        loading.dismiss();
                    });
        } else {
            if (mode.equals(MODE_UPDATE)) {
                listenerUpload.onSuccess(true, userData.getPhoto_nik());
            } else {
                listenerUpload.onSuccess(true, "none");
            }
        }
        loading.dismiss();
    }

    private void navigateToBack() {
        onBackPressed();
    }

    public interface onFetchDataListener {
        void onSuccessGenerateData(String noRegis, boolean hasSimiliar);

        void onSuccessCheckSimiliar(boolean hasSimiliar);
    }

    public interface onUploadImageListener {
        void onSuccess(boolean uploaded, String uri);
    }

}