package com.example.tesis.ui.event;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tesis.Model.Event;
import com.example.tesis.R;
import com.squareup.picasso.Picasso;

import java.text.ParseException;

public class InfoEventFragment extends Fragment {
    private TextView title, description, zoneName, start, end, loading;
    private String auxTitle, auxDescription, auxZoneName, auxStart, auxEnd, auxLink;
    private ImageView event_image;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle eventObject = getArguments();
        if(eventObject != null){
            Event event = (Event) eventObject.getSerializable("event");
            auxTitle =event.getTitle();
            auxDescription = event.getDescription();
            auxZoneName = event.getZone();
            auxLink = event.getUrl();
            try {
                auxStart = event.converStartDateAndHour();
                auxEnd = event.converFinishDateAndHour();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_info_event, container, false);

        this.title = root.findViewById(R.id.title_info_event);
        this.description = root.findViewById(R.id.description_info_event);
        this.zoneName = root.findViewById(R.id.zone_info_event);
        this.start = root.findViewById(R.id.start_info_event);
        this.end = root.findViewById(R.id.end_info_event);
        this.event_image = root.findViewById(R.id.image_info_event);

        title.setText(auxTitle);
        description.setText(auxDescription);
        zoneName.setText(auxZoneName);
        if(!auxLink.equals("sin imagen")) {
            Picasso.get().load(auxLink).into(event_image);
        }
        start.setText(auxStart);
        end.setText(auxEnd);
        return root;
    }
}