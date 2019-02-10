package com.example.week5daily4homeassignment;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        PermissionsManager.IPermissionManager, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ResultCallback<Status> {

    private static long UPDATE_INTERVAL = 1000;
    private static long FASTEST_INTERVAL = 900;
    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 200.0f;
    LocationRequest locationRequest;

    private GoogleMap mMap;
    PermissionsManager permissionsManager;

    GoogleApiClient googleApiClient;
    Marker geoFenceMarker;
    PendingIntent geofencePendingIntent;
    final int GEOFENCE_REQ_CODE = 0;

    double move = 0.0002;
    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";

    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent( context, MapsActivity.class );
        intent.putExtra( NOTIFICATION_MSG, msg );
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        permissionsManager = new PermissionsManager(this);
        permissionsManager.checkPermission();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        createGoogleApi();


    }

    private void createGoogleApi() {

        if(googleApiClient == null) {

            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            googleApiClient.connect();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onStop() {
        super.onStop();

        googleApiClient.disconnect();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng chick_Fil_A = new LatLng(33.901627, -84.478073);
        LatLng popeyes = new LatLng(33.901491, -84.476599);
        LatLng wing_City = new LatLng(33.903659, -84.481215);
        LatLng lahore_Grill = new LatLng(33.907598, -84.491181);
        LatLng dunkin_Donuts = new LatLng(33.903172, -84.484179);

        mMap.addMarker(new MarkerOptions().position(chick_Fil_A).title("Chick-fil-A").icon(BitmapDescriptorFactory.fromResource(R.drawable.chickfila_logo)).snippet(getLocationAddress(chick_Fil_A)));
        mMap.addMarker(new MarkerOptions().position(popeyes).title("Popeyes").icon(BitmapDescriptorFactory.fromResource(R.drawable.popeyes_logo)).snippet(getLocationAddress(popeyes)));
        mMap.addMarker(new MarkerOptions().position(wing_City).title("Wing City").icon(BitmapDescriptorFactory.fromResource(R.drawable.wingcity_logo)).snippet(getLocationAddress(wing_City)));
        mMap.addMarker(new MarkerOptions().position(lahore_Grill).title("Lahore Grill").icon(BitmapDescriptorFactory.fromResource(R.drawable.lahoregrill_logo)).snippet(getLocationAddress(lahore_Grill)));
        mMap.addMarker(new MarkerOptions().position(dunkin_Donuts).title("Dunkin Donuts").icon(BitmapDescriptorFactory.fromResource(R.drawable.dunkindonut_logo)).snippet(getLocationAddress(dunkin_Donuts)));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(wing_City));
        mMap.setMinZoomPreference(14);


    }

    @Override
    public void onLocationChanged(Location location) {

        markerLocation(new LatLng(location.getLatitude() + move, location.getLongitude()));

        move = move + 0.0002;
//        moveToNewLocation(location, "You are here.");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        permissionsManager.checkResult(requestCode, permissions, grantResults);

    }

    @Override
    public void onPermissionResult(boolean isGranted) {

        Log.d("TAG", "onPermissionResult: LOCATION PERMISSION GRANTED " + isGranted);
        if(isGranted) {
            getCurrentLocation();
        }

    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setInterval(UPDATE_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        LocationServices.getSettingsClient(this);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        },Looper.myLooper());

        markerForGeofence(new LatLng(33.927685, -84.058904));

        startGeofence();

    }

    private void moveToNewLocation(Location location, String locationName) {

        LatLng latLng = new LatLng(33.909301, -84.478915);
        if(location != null) {
            latLng = new LatLng(location.getLatitude() + move, location.getLongitude());

            mMap.addMarker(new MarkerOptions().position(latLng).title(locationName));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.setMinZoomPreference(10);
        }
        move = move + 0.0002;

//        Intent intent = new Intent(this, GeofenceTransitionService.class);
//        startService(intent);
    }

    private Marker locationMarker;
    private void markerLocation(LatLng latLng) {
        Log.i("TAG", "markerLocation("+latLng+")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if ( mMap!=null ) {
            if ( locationMarker != null )
                locationMarker.remove();
            locationMarker = mMap.addMarker(markerOptions);
            float zoom = 10f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            mMap.animateCamera(cameraUpdate);
        }
    }


    // Start Geofence creation process
    private void startGeofence() {
        Log.i("TAG", "startGeofence()");
        if( geoFenceMarker != null ) {
            Geofence geofence = createGeofence( geoFenceMarker.getPosition(), GEOFENCE_RADIUS );
            GeofencingRequest geofenceRequest = createGeofenceRequest( geofence );
            addGeofence( geofenceRequest );
        } else {
            Log.e("TAG", "Geofence marker is null");
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public String getLocationAddress(LatLng latLng) {
        String locationAddress = "";
        Geocoder geocoder = new Geocoder(this);

        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            for (Address address: addresses) {
                Log.d("TAG", "getCurrentLocationAddress: " + address.getAddressLine(0));
                locationAddress = address.getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locationAddress;
    }


    private void markerForGeofence(LatLng latLng) {

        String title = latLng.latitude + ", " + latLng.longitude;

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);

        if(mMap != null) {

            if(geoFenceMarker != null) {
                geoFenceMarker.remove();
            }

            geoFenceMarker = mMap.addMarker(markerOptions);

        }

    }

    private Geofence createGeofence(LatLng latLng, float radius) {

        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    private PendingIntent createGeofencePendingIntent() {

        if(geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent = new Intent(this, GeofenceTransitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    @SuppressLint("MissingPermission")
    private void addGeofence(GeofencingRequest request) {

        LocationServices.GeofencingApi.addGeofences(
                googleApiClient,
                request,
                createGeofencePendingIntent()
        ).setResultCallback(this);

    }

    @Override
    public void onResult(@NonNull Status status) {
        if(status.isSuccess()) {
            drawGeofence();
        } else {

        }
    }

    private Circle geoFenceLimits;
    private void drawGeofence() {
        if(geoFenceLimits != null) {
            geoFenceLimits.remove();
        }

        CircleOptions circleOptions = new CircleOptions()
                .center(geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor( Color.argb(100, 150,150,150) )
                .radius( GEOFENCE_RADIUS );
        geoFenceLimits = mMap.addCircle( circleOptions );

    }
}
