package org.beiwe.app.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import org.beiwe.app.R;
import org.beiwe.app.ui.adapters.TransitAdapter;
import org.beiwe.app.ui.handlers.Routes;
import org.beiwe.app.ui.handlers.Stop;
import org.beiwe.app.ui.handlers.TransitManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TransitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransitFragment extends Fragment  {

    private static String KEY = "AIzaSyDRJSnmycmwl9emHWdzBErbzUdT7rRGJa0";
    private static String URL = "https://maps.googleapis.com/maps/api/directions/json?";
    private List<Routes> routes;
    private TransitAdapter adapter;
    public LatLng source;
    public LatLng destination;

    public TransitFragment() {
        // Required empty public constructor
    }

    public static TransitFragment newInstance(LatLng from, LatLng to) {
        TransitFragment fragment = new TransitFragment();
        fragment.source = from;
        fragment.destination = to;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_transit, container, false);
        routes = new ArrayList<Routes>();
        ListView lvTransits = (ListView) v.findViewById(R.id.lvTransits);
        adapter = new TransitAdapter(getActivity(), routes);
        lvTransits.setAdapter(adapter);
        TransitManager.getInstance(this).getTransitData(source, destination, new TransitManager.TransitManagerListener() {
            @Override
            public void onResponseRoutes(List<Routes> r) {
                routes.clear();
                routes.addAll(r);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onResponseStops(List<Stop> stops) {

            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void parseResponse(JSONObject response) throws JSONException {
        JSONArray routesList = response.getJSONArray("routes");
        for (int i = 0; i < routesList.length(); i++) {
           routes.add(new Routes(routesList.getJSONObject(i)));
        }
        adapter.notifyDataSetChanged();
    }
}
