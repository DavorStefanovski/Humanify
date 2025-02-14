package com.Davor.humanify;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.Davor.humanify.Adapters.EventDiscussionsAdapter;
import com.Davor.humanify.Adapters.MyEventsAdapter;
import com.Davor.humanify.DTO.UserWithEventsResponse;
import com.Davor.humanify.Service.UserService;

public class EventsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_events, container, false);
        UserService.init(getContext());
        UserService.getCurrentUserWithEvents(new UserService.UserWithEventsCallback() {
            @Override
            public void onSuccess(UserWithEventsResponse userWithEventsResponse) {
                requireActivity().runOnUiThread(() -> {
                    RecyclerView recyclerView = v.findViewById(R.id.myEventsRecyclerView);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    MyEventsAdapter myEventsAdapter = new MyEventsAdapter(getContext(),userWithEventsResponse.getEventResponseList());
                    recyclerView.setAdapter(myEventsAdapter);
                });
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });
        return v;
    }
}