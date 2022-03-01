package com.example.myewaste.ui.admin;

import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Constant.MODE;
import static com.example.myewaste.utils.Constant.NO_REGIS;
import static com.example.myewaste.utils.Constant.USER_DATA;
import static com.example.myewaste.utils.Mode.MODE_NASABAH;
import static com.example.myewaste.utils.Mode.MODE_SUPER_ADMIN;
import static com.example.myewaste.utils.Mode.MODE_TELLER;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.myewaste.ui.nasabah.DataSaldoNasabahActivity;
import com.example.myewaste.ui.component.task.DataUserActivity;
import com.example.myewaste.R;
import com.example.myewaste.adapter.ListTaskAdapter;
import com.example.myewaste.databinding.ActivityAdminBinding;
import com.example.myewaste.model.utils.Task;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.ui.component.task.TransactionSaldoActivity;
import com.example.myewaste.ui.profile.ProfileUserActivity;
import com.example.myewaste.ui.admin.task.ItemMasterActivity;
import com.example.myewaste.ui.admin.task.ItemMasterTypeActivity;
import com.example.myewaste.ui.admin.task.ItemUnitActivity;
import com.example.myewaste.ui.component.task.TransactionItemActivity;
import com.example.myewaste.utils.TaskData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Objects;

public class AdminActivity extends AppCompatActivity {

    private UserData userData;
    private DatabaseReference databaseReference;

    private ArrayList<Task> taskTeller;
    private ListTaskAdapter adapter;

    private ActivityAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();

        if (getIntent().getParcelableExtra(EXTRAS_USER_DATA) != null) {
            userData = getIntent().getParcelableExtra(EXTRAS_USER_DATA);
            loadDataUser(userData);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();

        adapter = new ListTaskAdapter();
        binding.rvTask.setHasFixedSize(true);

        taskTeller = new ArrayList<>();
        taskTeller.addAll(TaskData.getTaskAdmin());

        setupRecyclerTask();

        fetchDataUserData();

        binding.ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileUserActivity.class);
            if (userData != null) {
                intent.putExtra(EXTRAS_USER_DATA, userData);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDataUserData();
    }

    private void setupRecyclerTask() {
        binding.rvTask.setLayoutManager(new GridLayoutManager(this, 2));
        adapter.setAdapter(taskTeller);
        binding.rvTask.setAdapter(adapter);
        adapter.setOnItemClickCallback(this::navigateTask);
    }

    private void loadDataUser(UserData userData) {
        Glide.with(this)
                .load(userData.getAvatar())
                .placeholder(R.drawable.ic_placeholder)
                .into(binding.ivAvatar);
        binding.tvName.setText(userData.getName());
        binding.tvNoRegis.setText(userData.getNo_regis());
    }

    private void navigateTask(int to) {
        Intent intent;
        switch (to) {
            case 0:
                intent = new Intent(this, ItemMasterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case 1:
                intent = new Intent(this, ItemMasterTypeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case 2:
                intent = new Intent(this, ItemUnitActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case 3:
                intent = new Intent(this, TransactionItemActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case 4:
                intent = new Intent(this, TransactionSaldoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case 5:
                intent = new Intent(this, DataUserActivity.class);
                intent.putExtra(MODE, MODE_NASABAH);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case 6:
                intent = new Intent(this, DataUserActivity.class);
                intent.putExtra(MODE, MODE_TELLER);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case 7:
                intent = new Intent(this, DataUserActivity.class);
                intent.putExtra(MODE, MODE_SUPER_ADMIN);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case 8:
                intent = new Intent(this, DataSaldoNasabahActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case 9:
                Toast.makeText(this, "data biaya operational", Toast.LENGTH_SHORT).show();
                break;

        }
    }


    private void fetchDataUserData() {
        if (userData.getNo_regis() != null) {
            Query queryUserData = databaseReference.child(USER_DATA).orderByChild(NO_REGIS).equalTo(userData.getNo_regis());
            queryUserData.get()
                    .addOnCompleteListener(task -> {
                        DataSnapshot result = task.getResult();
                        if (result != null) {
                            for (DataSnapshot dataSnapshot : result.getChildren()) {
                                UserData userDataResult = dataSnapshot.getValue(UserData.class);
                                if (userDataResult != null) {
                                    userData = userDataResult;
                                    loadDataUser(userData);
                                }
                            }
                        }
                    });
        }
    }

}