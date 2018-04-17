package org.beiwe.app.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.beiwe.app.R;
import org.beiwe.app.ui.adapters.TransitAdapter;
import org.beiwe.app.ui.handlers.Routes;
import org.beiwe.app.ui.handlers.Step;
import org.beiwe.app.ui.handlers.Stop;
import org.beiwe.app.ui.handlers.TransitManager;
import org.beiwe.app.ui.handlers.UberManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TransitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HybridFragment extends Fragment {

    private List<Routes> routes;
    private TransitAdapter adapter;
    public static LatLng source;
    public static LatLng destination;

    public HybridFragment() {
        // Required empty public constructor
    }

    public static HybridFragment newInstance(LatLng from, LatLng to) {
        HybridFragment fragment = new HybridFragment();
        source = from;
        destination = to;
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
        View v = inflater.inflate(R.layout.fragment_hybrid, container, false);
        if (routes == null || routes.isEmpty()) {
            routes = new ArrayList<Routes>();
        }

        ListView lvTransits = (ListView) v.findViewById(R.id.lvRoutes);
        TextView tvNone = (TextView) v.findViewById(R.id.tvNone);
        adapter = new TransitAdapter(getActivity(), routes);
        lvTransits.setEmptyView(tvNone);
        lvTransits.setAdapter(adapter);

        if (routes.isEmpty()) {
            TransitManager.getInstance(this).getTransitData(source, destination, new TransitManager.TransitManagerListener() {
                @Override
                public void onResponseRoutes(List<Routes> r) {
                    //routes.addAll(r);
                    List<Step> transits = new ArrayList<Step>();

                    for (Step s : r.get(0).getSteps()) {
                        if (s.getType().contentEquals("TRANSIT")) {
                            transits.add(s);
                        }
                    }

                    if (!transits.isEmpty()) {
                        getStops(transits);

                    }
                }

                @Override
                public void onResponseStops(List<Stop> s) {

                }
            });
        }

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

    private void getStops(List<Step> transits) {
        TransitManager.getInstance(this).getTransitStops(transits, source, destination, new TransitManager.TransitManagerListener() {
            @Override
            public void onResponseRoutes(List<Routes> r) {

            }

            @Override
            public void onResponseStops(List<Stop> s) {
                parseStops(s);
            }
        });
    }

    private void parseStops(List<Stop> stops) {
        if (stops.isEmpty()) {
            return;
        }
        Stop stop = stops.get(stops.size() - 1);
        stops.remove(stop);
        UberManager.getInstance(this).getUberRoutes(source, stop.getStop(), new UberManager.UberManagerListener() {
            @Override
            public void onResponse(Step step) {
                List<Step> steps = new ArrayList<Step>();
                step.setArrivalStop(stop.getStopName());
                step.setStart(source);
                step.setEnd(stop.getStop());
                steps.add(step);
                createRemainingSteps(stops, stop, steps);
            }
        });
    }

    private void createRemainingSteps(List<Stop> stops, Stop start, List<Step> steps) {
        Date departureTime = new Date(steps.get(0).getArrivalTime().getTime() + 2 * 60000);
        TransitManager.getInstance(this).getTransitData(start.getStop(), destination, departureTime, new TransitManager.TransitManagerListener() {
            @Override
            public void onResponseRoutes(List<Routes> routes) {
                Routes r = routes.get(0);
                List<Step> s = r.getSteps();
                Step transit = s.stream()
                        .filter(t -> t.getType().contentEquals("TRANSIT"))
                        .findFirst()
                        .orElse(null);
                if (transit != null) {
                    steps.addAll(r.getSteps());
                    createRoute(stops, steps);
                }
                else {
                    parseStops(stops);
                }
            }

            @Override
            public void onResponseStops(List<Stop> stops) {

            }
        });
    }

    private void createRoute(List<Stop> stops, List<Step> steps) {
        //Routes main = routes.get(0);
        Routes r = new Routes(steps);
        routes.add(r);
        routes.sort(new Comparator<Routes>() {
            @Override
            public int compare(Routes o1, Routes o2) {
                return o1.compareTo(o2);
            }
        });
        adapter.notifyDataSetChanged();
        Log.d("Route", source + " " + destination);
        //Log.d("Route", "Main : " + main);
        Log.d("Route", "Uber : " + r);
        parseStops(stops);
    }
}
