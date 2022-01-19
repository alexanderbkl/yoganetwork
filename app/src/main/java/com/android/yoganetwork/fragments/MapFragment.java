package com.android.yoganetwork.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.android.yoganetwork.ChatActivity;
import com.android.yoganetwork.R;
import com.android.yoganetwork.ThereProfileActivity;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.android.yoganetwork.constants.Constant;
import com.android.yoganetwork.models.Conversation;
import com.android.yoganetwork.models.UserProfile;
import com.android.yoganetwork.utils.DatabaseUtils;
import static com.android.yoganetwork.constants.Constant.LOCATION_SERVICES;
import static java.lang.Float.parseFloat;


public class MapFragment extends Fragment {
    public static final double RADIUS = 1500000;
    private final int imageSize = 120;

    private Map<String, Marker> stringMarkerMap;
    private MapView mMapView;
    private GoogleMap googleMap;
    private GeoFire geoFire;
    private String userId;
    private Circle circle;
    private OnFragmentInteractionListener activity;
    private GeoQuery geoQuery;
    private Marker marker;
    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            for (Location location : locationResult.getLocations()) {
                Log.d(LOCATION_SERVICES, "onLocationResult() called with: locationResult = [" + locationResult + "]" + location.getProvider() + " " + location.getAccuracy());

                GeoLocation myLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
                geoFire.setLocation(userId, myLocation);

                updateQuery(myLocation);

                drawCenteredCircle(new LatLng(location.getLatitude(), location.getLongitude()), userId);

            }
        }
    };
    private int totalUser;
    private final GeoQueryEventListener geoQueryEventListener = new GeoQueryEventListener() {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            Log.d(Constant.NEARBY_CHAT, String.format("Key %s entered the search area at [%f,nicetry]", key, location.latitude, location.longitude));
            if (key != userId) {
                Log.d(Constant.NEARBY_CHAT, String.format("Key %s entered the search area at [%f,nicetry]", key, location.latitude, location.longitude));
                double tempran = ThreadLocalRandom.current().nextDouble(-1, 1);
                double rand = tempran * 35000;
                int random = Double.valueOf(rand).intValue();
                double templat = location.latitude * 100;
                double templong = location.longitude * 100;

                double lat = Math.round(templat * 100000d) / 100000d;
                double lon = Math.round(templong * 100000d) / 100000d;

                String stringlat = String.valueOf(lat);
                stringlat = stringlat.substring(stringlat.indexOf(".") + 1);
                int numberlat = Integer.parseInt(stringlat) + random;
                String stringlong = String.valueOf((int) lon);
                stringlong = stringlong.substring(stringlong.indexOf(".") + 1);
                int numberlong = Integer.parseInt(stringlong) + random;
                double finallat = (Math.floor(lat) + numberlat * 0.00001) / 100;
                double finallong = (Math.floor(lon) + numberlong * 0.00001) / 100;
                LatLng latLng = new LatLng(finallat, finallong);
                UserProfile tempUserProfile = new UserProfile();
                tempUserProfile.setId(key);
                marker = addMarker(latLng, tempUserProfile);
            } else {
                LatLng latLng = new LatLng(location.latitude, location.longitude);
                UserProfile tempUserProfile = new UserProfile();
                tempUserProfile.setId(key);
                marker = addMarker(latLng, tempUserProfile);
            }

            //retrieve the user from the database with an async task

            DatabaseUtils.getUserProfileReferenceById(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserProfile profile = dataSnapshot.getValue(UserProfile.class);
                    if (profile != null) {
                        marker.setTitle(profile.getPseudonym());
                        marker.setSnippet(profile.getType());
                        marker.showInfoWindow();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(Constant.NEARBY_CHAT, "onCancelled() called with: databaseError = [" + databaseError + "]");
                    Log.w(Constant.NEARBY_CHAT, "onCancelled: ", databaseError.toException());
                }
            });

            DatabaseUtils.loadProfileImage(key, bitmap -> {
                //resize here
                if (bitmap != null) {
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false);
                    if (resizedBitmap != null) {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
                    }
                }
            }, null);


            //update number of people connected
            incTotalUser();
        }


        @Override
        public void onKeyExited(String key) {
            Log.d(Constant.NEARBY_CHAT, String.format("Key %s is no longer in the search area", key));
            Marker marker = stringMarkerMap.remove(key);
            marker.remove();
            //update number of people connected
            decTotalUser();
        }


        @Override
        public void onKeyMoved(String key, GeoLocation location) {
            if (key == userId) {
            Log.d(Constant.NEARBY_CHAT, String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            Marker marker = stringMarkerMap.get(key);
            LatLng position = new LatLng(location.latitude, location.longitude);
            updateMarkerPosition(marker, position);
            drawCenteredCircle(position, key);

            }
        }
  
        @Override
        public void onGeoQueryReady() {
            Log.d(Constant.NEARBY_CHAT, "onGeoQueryReady: All initial data has been loaded and events have been fired!");
        }

        @Override
        public void onGeoQueryError(DatabaseError error) {
            Log.w(Constant.NEARBY_CHAT, "onGeoQueryError: There was an error with this query: ", error.toException());
        }
    };
    private final GoogleMap.OnMarkerClickListener markerClickListener = marker -> {

        String partnerId = (String) marker.getTag();
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());



        alertBuilder.setTitle("Perfil");


        alertBuilder.setMessage("¿Ir al perfil del usuario?")
                .setPositiveButton("Sí", (dialogInterface, i) -> {

                    //start chat activity with that user
                    Intent intent = new Intent(getContext(), ThereProfileActivity.class);
                    intent.putExtra("uid", partnerId);
                    intent.putExtra("myUid", userId);
                    getContext().startActivity(intent);



                })

                .setNegativeButton("No", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                });

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();


        return false;
    };




    private void updateQuery(GeoLocation myLocation) {
        if (geoQuery == null) {
            geoQuery = geoFire.queryAtLocation(myLocation, RADIUS);
            geoQuery.addGeoQueryEventListener(geoQueryEventListener);
        } else {
            geoQuery.setLocation(myLocation, RADIUS);
        }
    }




    private void updateCameraPosition(LatLng position) {
        // For zooming automatically to the location of the marker
        Log.d(Constant.NEARBY_CHAT, "updateCameraPosition() called with: position = [" + position + "]");
        CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(googleMap.getMaxZoomLevel() - 6).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);

    }

    private void updateMarkerPosition(Marker marker, LatLng position) {
        marker.setPosition(position);
        marker.showInfoWindow();
    }

    private void updateCircle(LatLng center, String key) {
        if (userId.equals(key)) {
            circle.setCenter(center);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stringMarkerMap = Collections.synchronizedMap(new HashMap<>());

        geoFire = DatabaseUtils.getNewLocationDatabase();
        userId = DatabaseUtils.getCurrentUUID();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_map_view, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(mMap -> {
            googleMap = mMap;
            googleMap.setOnMarkerClickListener(markerClickListener);
            activity.addLocationCallback(locationCallback);

        });


        return rootView;
    }

    @NonNull
    private Marker addMarker(LatLng latLng, UserProfile userProfile) {
        //Create maker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(userProfile.getPseudonym())
                .snippet(userProfile.getType());
        Marker marker = googleMap.addMarker(markerOptions);

        // save the user id for future use
        marker.setTag(userProfile.getId());

        marker.showInfoWindow();//show the windows


        drawCenteredCircle(latLng, userProfile.getId());
        //Save the reference in the map
        stringMarkerMap.put(userProfile.getId(), marker);


        return marker;
    }

    private void drawCenteredCircle(LatLng latLng, String key) {
        if (userId.equals(key)) {
            if (circle != null) {
                updateCircle(latLng, key);
            } else {
                circle = googleMap.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(150)
                        .strokeColor(Color.CYAN)
                        .fillColor(0x220000FF)
                        .strokeWidth(5));
                updateCameraPosition(latLng);
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (geoFire != null) geoFire.removeLocation(userId);
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MapFragment.OnFragmentInteractionListener) {
            activity = (MapFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        clearGeoQuery();
        activity.removeLocationCallback(locationCallback);
        activity = null;
    }

    private void clearGeoQuery() {

        if (geoQuery != null) {
            geoQuery.removeAllListeners();
            geoQuery = null;
        }
    }

    private void decTotalUser() {
        if (totalUser > 0) totalUser--;
        updateSubtitle();
    }

    private void updateSubtitle() {
        Log.d(Constant.NEARBY_CHAT, "updateSubtitle: ");

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_main);
        toolbar.setSubtitle(totalUser + " online user(s)");
    }

    private void incTotalUser() {
        totalUser++;
        updateSubtitle();
    }

    public interface OnFragmentInteractionListener {
        void addLocationCallback(LocationCallback locationCallback);

        void removeLocationCallback(LocationCallback locationCallback);
    }
}