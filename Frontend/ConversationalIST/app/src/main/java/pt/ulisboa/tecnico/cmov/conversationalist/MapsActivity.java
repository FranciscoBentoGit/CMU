package pt.ulisboa.tecnico.cmov.conversationalist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    // New variables for Current Place Picker
    private static final String TAG = "MapsActivity";
    ListView lstPlaces;
    private PlacesClient mPlacesClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;


    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private LatLng lastMarker = mDefaultLocation;
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;
    private String username;
    private String chatName;
    private String isPublicToggleButtonState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set up the views
        lstPlaces = (ListView) findViewById(R.id.listPlaces);

        // Initialize the Places client
        String apiKey = getString(R.string.google_maps_key);
        Places.initialize(getApplicationContext(), apiKey);
        mPlacesClient = Places.createClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        chatName = intent.getStringExtra("chatName");
        isPublicToggleButtonState = intent.getStringExtra("isPublicToggleButtonState");

    }

    /**
     * Populates the app bar with the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handles user clicks on menu items.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_geolocate:
                // Present the current place picker
                pickCurrentPlace();
                return true;

            case R.id.clear_markers:
                mMap.clear();
                return true;

            case R.id.finish:
                Intent intent = new Intent();
                intent.putExtra(Intent.EXTRA_TEXT, "" + lastMarker.latitude + "," + lastMarker.longitude);
                intent.putExtra("username", username);
                intent.putExtra("chatName", chatName);
                intent.putExtra("isPublicToggleButtonState", isPublicToggleButtonState);
                setResult(RESULT_OK, intent);
                finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        mLocationPermissionGranted = false;
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
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

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation));
        //
        // PASTE THE LINES BELOW THIS COMMENT
        //

        // Enable the zoom controls for the map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                LatLng target = mMap.getCameraPosition().target;
                Log.d(getClass().getSimpleName(), String.format("camera at %f:%f", target.latitude, target.longitude));
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng arg0)
            {
                mMap.addMarker(new MarkerOptions().title("clicked location").position(arg0).snippet(getString(R.string.default_info_snippet)).draggable(true));
                lastMarker = arg0;
            }
        });
        // Prompt the user for permission.
        getLocationPermission();
        pickCurrentPlace();

    }

    /**
     * Calls the findCurrentPlace method in Google Maps Platform Places API.
     * Response contains a list of placeLikelihood objects.
     * Takes the most likely places and extracts the place details for access in other methods.
     */
    private void getCurrentPlaceLikelihoods() {
        // Use fields to define the data types to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.LAT_LNG);

        // Get the likely places - that is, the businesses and other points of interest that
        // are the best match for the device's current location.
        @SuppressWarnings("MissingPermission") final FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.builder(placeFields).build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<FindCurrentPlaceResponse> placeResponse = mPlacesClient.findCurrentPlace(request);
        placeResponse.addOnCompleteListener(this,
                new OnCompleteListener<FindCurrentPlaceResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                        if (task.isSuccessful()) {
                            FindCurrentPlaceResponse response = task.getResult();
                            // Set the count, handling cases where less than 5 entries are returned.
                            int count;
                            if (response.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
                                count = response.getPlaceLikelihoods().size();
                            } else {
                                count = M_MAX_ENTRIES;
                            }

                            int i = 0;
                            mLikelyPlaceNames = new String[count];
                            mLikelyPlaceAddresses = new String[count];
                            mLikelyPlaceAttributions = new String[count];
                            mLikelyPlaceLatLngs = new LatLng[count];

                            for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                                Place currPlace = placeLikelihood.getPlace();
                                mLikelyPlaceNames[i] = currPlace.getName();
                                mLikelyPlaceAddresses[i] = currPlace.getAddress();
                                mLikelyPlaceAttributions[i] = (currPlace.getAttributions() == null) ?
                                        null : TextUtils.join(" ", currPlace.getAttributions());
                                mLikelyPlaceLatLngs[i] = currPlace.getLatLng();

                                String currLatLng = (mLikelyPlaceLatLngs[i] == null) ?
                                        "" : mLikelyPlaceLatLngs[i].toString();

                                Log.i(TAG, String.format("Place " + currPlace.getName() + " has likelihood: " + placeLikelihood.getLikelihood() + " at " + currLatLng));

                                i++;
                                if (i > (count - 1)) {
                                    break;
                                }
                            }


                            // COMMENTED OUT UNTIL WE DEFINE THE METHOD
                            // Populate the ListView
                            fillPlacesList();
                        } else {
                            Exception exception = task.getException();
                            if (exception instanceof ApiException) {
                                ApiException apiException = (ApiException) exception;
                                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                            }
                        }
                    }
                });
    }

    /**
     * Get the current location of the device, and position the map's camera
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = location;
                            Log.d(TAG, "Latitude: " + mLastKnownLocation.getLatitude());
                            Log.d(TAG, "Longitude: " + mLastKnownLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        }

                        getCurrentPlaceLikelihoods();
                    }
                });
            }
        } catch (Exception e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Fetch a list of likely places, and show the current place on the map - provided the user
     * has granted location permission.
     */
    private void pickCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            getDeviceLocation();
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(new MarkerOptions().title(getString(R.string.default_info_title)).position(mDefaultLocation).snippet(getString(R.string.default_info_snippet)).draggable(true));
            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    /**
     * When user taps an item in the Places list, add a marker to the map with the place details
     */
    private AdapterView.OnItemClickListener listClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            // position will give us the index of which place was selected in the array
            LatLng markerLatLng = mLikelyPlaceLatLngs[position];
            String markerSnippet = mLikelyPlaceAddresses[position];
            if (mLikelyPlaceAttributions[position] != null) {
                markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[position];
            }

            // Add a marker for the selected place, with an info window
            // showing information about that place.
            mMap.addMarker(new MarkerOptions().title(mLikelyPlaceNames[position]).position(markerLatLng).snippet(markerSnippet).draggable(true));
            lastMarker = markerLatLng;

            // Position the map's camera at the location of the marker.
            mMap.moveCamera(CameraUpdateFactory.newLatLng(markerLatLng));
        }
    };

    /**
     * Display a list allowing the user to select a place from a list of likely places.
     */
    private void fillPlacesList() {
        // Set up an ArrayAdapter to convert likely places into TextViews to populate the ListView
        ArrayAdapter<String> placesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mLikelyPlaceNames);
        lstPlaces.setAdapter(placesAdapter);
        lstPlaces.setOnItemClickListener(listClickedHandler);
    }

    public void searchplace(View view) {

        EditText editText = (EditText) this.findViewById(R.id.search_bar);
        if (editText == null){
            Toast.makeText(MapsActivity.this, "no edit text found", Toast.LENGTH_SHORT).show();
            return;
        }
        String location = editText.getText().toString();
        if (location == null){
            Toast.makeText(MapsActivity.this, "no location typed", Toast.LENGTH_SHORT).show();
        }else{
            Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
            try{
                List<Address> listAddress = geocoder.getFromLocationName(location, 1);
                if(listAddress.size()>0){
                    LatLng latLng = new LatLng(listAddress.get(0).getLatitude(),listAddress.get(0).getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.title(listAddress.get(0).getFeatureName());
                    markerOptions.position(latLng);
                    markerOptions.draggable(true);
                    mMap.addMarker(markerOptions);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 5);
                    mMap.animateCamera(cameraUpdate);
                    lastMarker = latLng;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_SHORT).show();
    }

    public void onMarkerDragStart(Marker marker) {
        LatLng position=marker.getPosition();

        Log.d(getClass().getSimpleName(), String.format("Drag from %f:%f", position.latitude, position.longitude));
    }

    public void onMarkerDrag(Marker marker) {
        LatLng position=marker.getPosition();

        Log.d(getClass().getSimpleName(), String.format("Dragging to %f:%f", position.latitude, position.longitude));
    }

    public void onMarkerDragEnd(Marker marker) {
        LatLng position=marker.getPosition();

        Log.d(getClass().getSimpleName(), String.format("Dragged to %f:%f", position.latitude, position.longitude));
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}