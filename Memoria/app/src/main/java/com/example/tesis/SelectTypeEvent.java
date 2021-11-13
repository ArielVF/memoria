package com.example.tesis;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SelectTypeEvent extends Fragment {
    private Button event_zone_p, event_zone_temporal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_select_type_event, container, false);
        event_zone_p = root.findViewById(R.id.event_p_button);
        event_zone_temporal = root.findViewById(R.id.event_t_button);

        event_zone_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(root).navigate(R.id.createEventeFragment);
            }
        });

        event_zone_temporal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(root).navigate(R.id.createEventTemporalZone);
            }
        });

        return root;
    }
}