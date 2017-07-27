package com.example.mualla.mapskonum;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener {

    private static GoogleMap mMap;
    EditText langi;
    EditText longi;
    static Double latit;
    static Double longit;
    Button btn;
    EditText adres;

    protected static final String TAG = "main-activity";
    protected static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    protected static final String LOCATION_ADDRESS_KEY = "location-address";
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected boolean mAddressRequested;
    protected String mAddressOutput;
    private AddressResultReceiver mResultReceiver;
    protected EditText mLocationAddressTextView;
    ProgressBar mProgressBar;
    Button mFetchAddressButton;
    public Float[] distances = new Float[4];
    public LatLng [] latlng = new LatLng[5];
    public MarkerOptions [] marker = new MarkerOptions[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        btn = (Button) findViewById( R.id.button );
        langi = (EditText) findViewById( R.id.mapInput );
        longi = (EditText) findViewById( R.id.mapInput2 );
        adres = (EditText) findViewById( R.id.location_address_view );

        btn.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {

                Double latitude2 = Double.parseDouble( langi.getText().toString() );
                Double longitude2 = Double.parseDouble( longi.getText().toString() );

                MarkerOptions marker2 = new MarkerOptions().position( new LatLng( latitude2, longitude2 ) ).title( "İstenilen Konum : " + latitude2 + " - " + longitude2 );
                mMap.addMarker( marker2 );
                marker2.icon( BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_MAGENTA ) );


                setLocation( latitude2, longitude2 );
                AddPolyline( latitude2, longitude2 );


                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder( MainActivity.this, Locale.getDefault() );

                try {
                    addresses = geocoder.getFromLocation( latitude2, longitude2, 1 ); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    String address = addresses.get( 0 ).getAddressLine( 0 ); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get( 0 ).getLocality();
                    String state = addresses.get( 0 ).getAdminArea();
                    String country = addresses.get( 0 ).getCountryName();
                    String postalCode = addresses.get( 0 ).getPostalCode();
                    String knownName = addresses.get( 0 ).getFeatureName();
                    Toast.makeText( MainActivity.this, "Adress :" + address + " City: " + city + " State: " + state + " Country: " + country + " Postal Code: " + postalCode + " KnownName: " + knownName, Toast.LENGTH_SHORT ).show();
                    adres.setText( "Adress :" + address + " City: " + city + " State: " + state + " Country: " + country + " Postal Code: " + postalCode + " KnownName: " + knownName );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } );


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById( R.id.map );
        mapFragment.getMapAsync( this );

        mResultReceiver = new AddressResultReceiver( new Handler() );
        mLocationAddressTextView = (EditText) findViewById( R.id.location_address_view );
        mProgressBar = (ProgressBar) findViewById( R.id.progress_bar );
        mFetchAddressButton = (Button) findViewById( R.id.fetch_address_button );
        mAddressRequested = false;
        mAddressOutput = "";
        updateValuesFromBundle( savedInstanceState );
        updateUIWidgets();
        buildGoogleApiClient();


    }

    static double latitude = 0;
    static double longitude = 0;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled( true );
        googleMap.setOnMyLocationChangeListener( new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                latlng[0] = new LatLng( latitude, longitude );
                mMap.animateCamera( CameraUpdateFactory.newLatLngZoom( latlng[0], 17 ) );
                mMap.setOnMyLocationChangeListener( null );

                Location loc0 = new Location ("Konumum");
                loc0.setLatitude( latitude );
                loc0.setLongitude( longitude );
                marker[0] = new MarkerOptions().position( latlng[0] ).title( "Konumum 0 " + latitude + " , " + longitude );
                mMap.addMarker( marker[0] );
                marker[0].icon( BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_CYAN ) );

                Location loc1 = new Location ("Konum 2");
                loc1.setLatitude( 30.000 );
                loc1.setLongitude( 30.000 );
                latlng[1] = new LatLng( loc1.getLatitude(), loc1.getLongitude() );
                marker[1] = new MarkerOptions().position( latlng[1] ).title( "Konum 1" );
                mMap.addMarker( marker[1] );
                marker[1].icon( BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_CYAN ) );

                Location loc2 = new Location ("Konum 3");
                loc2.setLatitude( 40.000 );
                loc2.setLongitude( 35.000 );
                latlng[2] = new LatLng( loc2.getLatitude(), loc2.getLongitude() );
                marker[2] = new MarkerOptions().position( latlng[2] ).title( "Konum 2" );
                mMap.addMarker( marker[2] );
                marker[2].icon( BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_CYAN ) );

                Location loc3 = new Location ("Konum 4");
                loc3.setLatitude( 45.000 );
                loc3.setLongitude( 25.000 );
                latlng[3] = new LatLng( loc3.getLatitude(), loc3.getLongitude() );
                marker[3] = new MarkerOptions().position( latlng[3] ).title( "Konum 3" );
                mMap.addMarker( marker[3] );
                marker[3].icon( BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_CYAN ) );

                Location loc4 = new Location ("Konum 5");
                loc4.setLatitude( 38.000 );
                loc4.setLongitude( 36.000 );
                latlng[4] = new LatLng( loc4.getLatitude(), loc4.getLongitude() );
                marker[4] = new MarkerOptions().position( latlng[4] ).title( "Konum 4" );
                mMap.addMarker( marker[4] );
                marker[4].icon( BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_CYAN ) );

                distances[0] = loc0.distanceTo( loc1 );
                distances[1] = loc0.distanceTo( loc2 );
                distances[2] = loc0.distanceTo( loc3 );
                distances[3] = loc0.distanceTo( loc4 );

                Float min =distances[0];
                for(int i=1;i<distances.length;i++){
                    if(distances[i]<min){
                        min=distances[i];
                    }
                }
                for(int j=0;j<distances.length;j++){
                    if(min==distances[j]){
                        marker[j+1]=new MarkerOptions().position( latlng[j+1] ).title( "EN YAKIN KONUM " );
                        mMap.addPolyline( new PolylineOptions().geodesic( true )
                                .add( new LatLng( latitude, longitude ) )
                                .add( latlng[j+1] )
                        );
                    }
                }
            }
        } );

    }

    public static void setLocation(Double latitude, Double longitude) {
        latit = latitude;
        longit = longitude;
    }

    public static void AddPolyline(Double lat, Double lon) {

        MarkerOptions marker3 = new MarkerOptions().position( new LatLng( latit, longit ) ).title( "Konumum " );
        mMap.addMarker( marker3 );
        marker3.icon( BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_CYAN ) );

        mMap.addPolyline( new PolylineOptions().geodesic( true )
                        .add( new LatLng( latitude, longitude ) )
                        .add( new LatLng( lat, lon ) )
        );
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains( ADDRESS_REQUESTED_KEY )) {
                mAddressRequested = savedInstanceState.getBoolean( ADDRESS_REQUESTED_KEY );
            }
            if (savedInstanceState.keySet().contains( LOCATION_ADDRESS_KEY )) {
                mAddressOutput = savedInstanceState.getString( LOCATION_ADDRESS_KEY );
                displayAddressOutput();
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .addApi( LocationServices.API )
                .build();
    }

    public void fetchAddressButtonHandler(View view) {
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startIntentService();
        }
        mAddressRequested = true;
        updateUIWidgets();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation( mGoogleApiClient );
        if (mLastLocation != null) {
            if (!Geocoder.isPresent()) {
                Toast.makeText( this, R.string.no_geocoder_available, Toast.LENGTH_LONG ).show();
                return;
            }
            if (mAddressRequested) {
                startIntentService();
            }
        }
    }

    protected void startIntentService() {
        Intent intent = new Intent( this, FetchAddressIntentService.class );
        intent.putExtra( Constants.RECEIVER, mResultReceiver );
        intent.putExtra( Constants.LOCATION_DATA_EXTRA, mLastLocation );
        startService( intent );
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i( TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode() );
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i( TAG, "Connection suspended" );
        mGoogleApiClient.connect();
    }

    protected void displayAddressOutput() {
        mLocationAddressTextView.setText( mAddressOutput );
    }

    private void updateUIWidgets() {
        if (mAddressRequested) {
            mProgressBar.setVisibility( ProgressBar.VISIBLE );
            mFetchAddressButton.setEnabled( false );
        } else {
            mProgressBar.setVisibility( ProgressBar.GONE );
            mFetchAddressButton.setEnabled( true );
        }
    }

    protected void showToast(String text) {
        Toast.makeText( this, text, Toast.LENGTH_SHORT ).show();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean( ADDRESS_REQUESTED_KEY, mAddressRequested );
        savedInstanceState.putString( LOCATION_ADDRESS_KEY, mAddressOutput );
        super.onSaveInstanceState( savedInstanceState );
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super( handler );
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mAddressOutput = resultData.getString( Constants.RESULT_DATA_KEY );
            displayAddressOutput();
            if (resultCode == Constants.SUCCESS_RESULT) {
                showToast( getString( R.string.address_found ) );
            }
            mAddressRequested = false;
            updateUIWidgets();
        }
    }
}