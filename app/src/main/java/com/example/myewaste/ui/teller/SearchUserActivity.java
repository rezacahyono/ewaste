package com.example.myewaste.ui.teller;

import static com.example.myewaste.utils.Constant.AKTIF;
import static com.example.myewaste.utils.Constant.EXTRAS_ACTION_MODE;
import static com.example.myewaste.utils.Constant.EXTRAS_FROM;
import static com.example.myewaste.utils.Constant.EXTRAS_USER_DATA;
import static com.example.myewaste.utils.Constant.FROM_INPUT;
import static com.example.myewaste.utils.Constant.MODE_ADD;
import static com.example.myewaste.utils.Constant.NAME;
import static com.example.myewaste.utils.Constant.NASABAH;
import static com.example.myewaste.utils.Constant.STATUS;
import static com.example.myewaste.utils.Constant.USER;
import static com.example.myewaste.utils.Constant.USER_DATA;
import static com.example.myewaste.utils.Util.getRegisterCode;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myewaste.R;
import com.example.myewaste.adapter.ListUserAdapater;
import com.example.myewaste.databinding.ActivitySearchUserBinding;
import com.example.myewaste.databinding.MainToolbarBinding;
import com.example.myewaste.model.user.User;
import com.example.myewaste.model.user.UserData;
import com.example.myewaste.ui.admin.task.AddUpdateTransactionItemActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class SearchUserActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    private ArrayList<UserData> listUser;
    private ListUserAdapater adapter;
    private ActivitySearchUserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();
        MainToolbarBinding bindingToolbar = binding.mainToolbar;
        bindingToolbar.btnBack.setVisibility(View.VISIBLE);
        bindingToolbar.tvTitleBar.setText(R.string.input_ballance);

        bindingToolbar.btnBack.setOnClickListener(view -> onBackPressed());
        binding.fbAddUser.setVisibility(View.GONE);

        listUser = new ArrayList<>();
        adapter = new ListUserAdapater();
        binding.rvUser.setHasFixedSize(true);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        actionSearchView();
        setRecylcerViewUser();

    }

    private void setRecylcerViewUser() {
        binding.rvUser.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUser.setAdapter(adapter);

        adapter.setOnItemClickCallback(userData -> {
            Intent intent = new Intent(this, AddUpdateTransactionItemActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(EXTRAS_FROM, FROM_INPUT);
            intent.putExtra(EXTRAS_USER_DATA, userData);
            startActivity(intent);
        });
    }

    private void showPlaceholderOrRecyclerView(boolean isShow) {
        if (isShow) {
            binding.rvUser.setVisibility(View.VISIBLE);
            binding.ivPlaceholderEmpty.setVisibility(View.GONE);
            binding.tvTitle.setVisibility(View.GONE);
        } else {
            binding.rvUser.setVisibility(View.GONE);
            binding.ivPlaceholderEmpty.setVisibility(View.VISIBLE);
            binding.tvTitle.setVisibility(View.VISIBLE);
        }
    }

    public void actionSearchView() {
        binding.scUser.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null) {
                    if (!query.isEmpty()) {
                        loadUserBySearch(query);
                        binding.scUser.clearFocus();
                    }
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals("")) {
                    loadUserBySearch(newText);
                }
                if (newText.length() == 0) {
                    adapter.setAdapter(new ArrayList<>());
                    showPlaceholderOrRecyclerView(listUser.size() > 0);
                }
                return false;
            }
        });
    }

    public void loadUserBySearch(String q) {
        DatabaseReference refUserData = databaseReference.child(USER_DATA);
        Query queryUserData = refUserData.orderByChild(NAME);
        queryUserData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    UserData userData = data.getValue(UserData.class);
                    if (userData != null) {
                        if (userData.getName().toLowerCase().contains(q.toLowerCase()) || userData.getNo_regis().toLowerCase().contains(q.toLowerCase())) {
                            if (getRegisterCode(userData.getNo_regis()).equalsIgnoreCase(NASABAH)) {
                                listUser.add(userData);
                            }
                        }
                    }
                }
                adapter.setAdapter(listUser);
                showPlaceholderOrRecyclerView(listUser.size() > 0);
                listUser.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}