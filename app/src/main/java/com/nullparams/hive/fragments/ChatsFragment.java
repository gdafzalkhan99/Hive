package com.nullparams.hive.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nullparams.hive.R;
import com.nullparams.hive.adapters.UserAdapter;
import com.nullparams.hive.models.User;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ChatsFragment extends Fragment {

    private Context context;
    private RecyclerView recyclerView;
    private List<String> usersList = new ArrayList<>();
    private List<User> mUsers = new ArrayList<>();
    private FirebaseFirestore mFireBaseFireStore;
    private String mCurrentUserId;
    private ConstraintLayout layout;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        context = getActivity();

        AutoCompleteTextView searchField = getActivity().findViewById(R.id.searchField);
        searchField.setVisibility(View.VISIBLE);

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            mCurrentUserId = firebaseUser.getUid();
        }

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        layout = view.findViewById(R.id.container);

        sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", true);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }

        readChats();
        return view;
    }

    private void lightMode() {

        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
    }

    private void darkMode() {

        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
    }

    private void readChats() {

        CollectionReference chatPath = mFireBaseFireStore.collection("Chats").document(mCurrentUserId).collection("Single");
        chatPath.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                usersList.clear();

                for (QueryDocumentSnapshot doc : value) {
                    usersList.add(doc.getId());
                }
                readUsers();
            }
        });
    }

    private void readUsers() {

        CollectionReference usersPath = mFireBaseFireStore.collection("User");
        usersPath.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                mUsers.clear();

                for (QueryDocumentSnapshot doc : value) {

                    User user = doc.toObject(User.class);

                    for (String id : usersList) {

                        if (user.getId().equals(id)) {
                            mUsers.add(user);
                        }
                    }
                }

                UserAdapter userAdapter = new UserAdapter(context, mUsers, true, sharedPreferences, false, false, null, null);
                recyclerView.setAdapter(userAdapter);
            }
        });
    }
}
