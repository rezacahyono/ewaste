package com.example.myewaste.ui.teller;

import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Constant.NO_REGIS;
import static com.example.myewaste.utils.Constant.USER_DATA;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.myewaste.R;
import com.example.myewaste.ui.component.task.TransactionSaldoActivity;
import com.example.myewaste.adapter.ListTaskAdapter;
import com.example.myewaste.databinding.ActivityTellerBinding;
import com.example.myewaste.model.utils.Task;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.ui.profile.ProfileUserActivity;
import com.example.myewaste.ui.component.task.TransactionItemActivity;
import com.example.myewaste.utils.TaskData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Objects;

public class TellerActivity extends AppCompatActivity {

    private UserData userData;

    private ArrayList<Task> taskTeller;
    private ListTaskAdapter adapter;
    private DatabaseReference databaseReference;

    private ActivityTellerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTellerBinding.inflate(getLayoutInflater());
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
        taskTeller.addAll(TaskData.getTaskTeller());

        setupRecyclerTask();

        fetchDataUserData();

        binding.ibWithdraw.setOnClickListener(view -> {
            Intent intent = new Intent(this, SearchUserActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

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

        adapter.setOnItemClickCallback(this::navigate);
    }

    private void loadDataUser(UserData userData) {
        Glide.with(this)
                .load(userData.getAvatar())
                .placeholder(R.drawable.ic_placeholder)
                .into(binding.ivAvatar);
        binding.tvNoRegis.setText(userData.getNo_regis());
        binding.tvName.setText(userData.getName());
    }


    private void navigate(int to){
        Intent intent;
        switch (to){
            case 0:
                intent = new Intent(this, TransactionSaldoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case 1:
                intent = new Intent(this, TransactionItemActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            default:
                Toast.makeText(this, "false", Toast.LENGTH_SHORT).show();
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