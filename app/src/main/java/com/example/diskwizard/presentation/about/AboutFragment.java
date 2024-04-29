package com.example.diskwizard.presentation.about;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.diskwizard.R;


public class AboutFragment extends Fragment {

    private final Runnable onAppAboutClick;
    private final Runnable onDeveloperAboutClick;

    public AboutFragment(Runnable onAppAboutClick, Runnable onDeveloperAboutClick) {
        this.onDeveloperAboutClick = onDeveloperAboutClick;
        this.onAppAboutClick = onAppAboutClick;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        Button appAboutButton = view.findViewById(R.id.appAboutButton);
        Button developerAboutButton = view.findViewById(R.id.developerAboutButton);

        appAboutButton.setOnClickListener(v -> onAppAboutClick.run());
        developerAboutButton.setOnClickListener(v -> onDeveloperAboutClick.run());

        return view;
    }

}