package com.example.snapdiaryv3;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserProfileFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseUser user;
    TextView emailText;
    Button logoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        emailText = view.findViewById(R.id.emailText);
        logoutButton = view.findViewById(R.id.logoutButton);

        if (user != null) {
            emailText.setText(user.getEmail());
        }

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            getActivity().finish();
            startActivity(new Intent(getActivity(), MainActivity.class));
        });

        return view;
    }
}
