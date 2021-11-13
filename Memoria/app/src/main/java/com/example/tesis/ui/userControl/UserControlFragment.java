package com.example.tesis.ui.userControl;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.tesis.Model.User;
import com.example.tesis.R;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class UserControlFragment extends Fragment implements AdapterUser.ItemClickListener {
    private FirebaseFirestore db;
    private ArrayList<User> users;
    private RecyclerView recyclerView;
    private AdapterUser adapterUser;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_user_control, container, false);

        db = FirebaseFirestore.getInstance();
        users = new ArrayList<>();
        getUsers();

        recyclerView = root.findViewById(R.id.list_users);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        adapterUser = new AdapterUser(users, this);
        recyclerView.setAdapter(adapterUser);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_event, menu);

        MenuItem menuItem = menu.findItem(R.id.bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Buscar por rut (ej: 13991860-6)");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapterUser.getFilter().filter(newText);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void getUsers(){
        db.collection("Usuarios")
                .whereNotEqualTo("rol", "administrador")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@com.google.firebase.database.annotations.Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        users.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            User user = new User(doc.getId(), doc.getString("nombre"), doc.getString("apellido"),
                                    doc.getString("correo"), doc.getString("rol"), doc.getString("rut"), doc.getBoolean("habilitado"));
                           users.add(user);
                        }
                        adapterUser.notifyDataSetChanged();
                        adapterUser.setAdapterUser(users);
                    }
                });
    }

    @SuppressLint("ResourceType")
    @Override
    public void onItemClick(User user) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);
        Navigation.findNavController(root).navigate(R.id.userInfoFragment, bundle);
    }
}