package org.futto.app.ui.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;

import org.futto.app.R;
import org.futto.app.ui.adapters.TransitAdapter;
import org.futto.app.ui.handlers.Routes;
import org.futto.app.ui.handlers.Step;
import org.futto.app.ui.handlers.Stop;
import org.futto.app.ui.handlers.TransitManager;
import org.futto.app.ui.handlers.UberManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.google.android.gms.maps.model.JointType.ROUND;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TransitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HybridFragment extends Fragment implements OnMapReadyCallback {

    private List<Routes> routes;
    private TransitAdapter adapter;
    public static LatLng source;
    public static LatLng destination;
    GoogleMap mMap;
    private RelativeLayout mapHolder;
    private Button backToResults;
    private ListView lvTransits;

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

        lvTransits = (ListView) v.findViewById(R.id.lvRoutes);
        TextView tvNone = (TextView) v.findViewById(R.id.tvNone);
        adapter = new TransitAdapter(getActivity(), routes);
        lvTransits.setEmptyView(tvNone);
        lvTransits.setAdapter(adapter);

        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentByTag("mapFragment");
        mapHolder = (RelativeLayout) v.findViewById(R.id.mapHolder1);
        mapHolder.setVisibility(View.GONE);
        if (mapFragment == null) {
            mapFragment = new SupportMapFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.mapFragmentContainer1, mapFragment, "mapFragment");
            ft.commit();
            fm.executePendingTransactions();
        }
        mapFragment.getMapAsync(this);

        if (routes.isEmpty()) {
            TransitManager.getInstance(this).getTransitData(source, destination, new TransitManager.TransitManagerListener() {
                @Override
                public void onResponseRoutes(List<Routes> r) {
                    //routes.addAll(r);
                    //adapter.notifyDataSetChanged();
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

        lvTransits.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mapHolder.setVisibility(View.VISIBLE);
                lvTransits.setVisibility(View.GONE);
                Routes route = routes.get(position);
                List<Step> steps = route.getSteps();

                PolylineOptions lineOptions = new PolylineOptions();
                lineOptions.width(10);
                lineOptions.color(Color.BLACK);
                lineOptions.startCap(new SquareCap());
                lineOptions.endCap(new SquareCap());
                lineOptions.jointType(ROUND);

                onMapReady(mMap);

                for (Step s : steps) {
                    String polyline = s.getPolyline();
                    if (polyline == null) {
                        continue;
                    }
                    List<LatLng> latLngs = PolyUtil.decode(polyline);
                    lineOptions.addAll(latLngs);

                    IconGenerator icg = new IconGenerator(getActivity());
                    Bitmap bm = icg.makeIcon(s.getName());

                    MarkerOptions options = new MarkerOptions();
                    options.position(latLngs.get(0));
                    options.icon(BitmapDescriptorFactory.fromBitmap(bm));
                    mMap.addMarker(options);
                }
                mMap.addPolyline(lineOptions);
            }
        });

        backToResults = (Button) v.findViewById(R.id.bBackToResults1);
        backToResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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
        if (mapHolder.getVisibility() != View.VISIBLE) {
            adapter.notifyDataSetChanged();
        }
        Log.d("Route", source + " " + destination);
        //Log.d("Route", "Main : " + main);
        Log.d("Route", "Uber : " + r);
        parseStops(stops);
    }

    public void onBackPressed() {
        if (mapHolder.getVisibility() == View.VISIBLE) {
            lvTransits.setVisibility(View.VISIBLE);
            mapHolder.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            mMap.clear();
            return;
        }
        getActivity().finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(20);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(source.latitude, source.longitude))
                .zoom(17)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
