package com.maps.alerts;


import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.widget.Toast;

import com.maps.alerts.R;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity {
    
    GoogleMap googleMap;
    LocationManager locationManager;
    PendingIntent pendingIntent;
    SharedPreferences sharedPreferences;
    int locationCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        // Showing status
        if(status!=ConnectionResult.SUCCESS){ // Google Play Services are not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        }else { // Google Play Services are available            

            // Getting reference to the SupportMapFragment of activity_main.xml
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            // Getting GoogleMap object from the fragment
            googleMap = fm.getMap();

            // Enabling MyLocation Layer of Google Map
            googleMap.setMyLocationEnabled(true);           
            
            
            // Getting LocationManager object from System Service LOCATION_SERVICE
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            
            
            // Opening the sharedPreferences object
            sharedPreferences = getSharedPreferences("location", 0);
            
            // Getting number of locations already stored
            locationCount = sharedPreferences.getInt("locationCount", 0);            
            
            // Getting stored zoom level if exists else return 0
            String zoom = sharedPreferences.getString("zoom", "0");            
           ///setting a camera for first tim
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(41.889329942,-87.634580193)));

            //set zoom for first time
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(Float.parseFloat("12.0")));
            if(locationCount!=0){

                    String lat = "";
                    String lng = "";

                    // Iterating through all the locations stored
                    for(int i=0;i<locationCount;i++){

                            // Getting the latitude of the i-th location
                            lat = sharedPreferences.getString("lat"+i,"0");

                            // Getting the longitude of the i-th location
                            lng = sharedPreferences.getString("lng"+i,"0");

                            // Drawing marker on the map
                            drawMarker(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
                            
                            // Drawing circle on the map
                            drawCircle(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
                    }

                    // Moving CameraPosition to last clicked position
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng))));

                    // Setting the zoom level in the map on last position  is clicked
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(Float.parseFloat(zoom)));                                                   
            }           
            
            ///McDonalds
            setLocations(41.89309070,-87.63179490,0,"McDonals");
            ///setLocations(41.8869261,-87.63409315);
            
            ///Office
            setLocations(41.89329942,-87.63480193,1, "HQ");
            ///Brown line station
            setLocations(41.8889524,-87.634217888,2, "Train Station");
           
            
            
            // If locations are already saved
           
           /// setLocations(41.0989899,-87.649000000, 3, "new location");
            
            googleMap.setOnMapClickListener(new OnMapClickListener() {
                
                @Override
                public void onMapClick(LatLng point) {                
                    
                    // Incrementing location count
                    locationCount++;
                                        
                    // Drawing marker on the map
                    drawMarker(point);
                    
                    // Drawing circle on the map
                    drawCircle(point);                    
                    
                    // This intent will call the activity ProximityActivity
                    Intent proximityIntent = new Intent("com.maps.alerts.activity.proximity");
                    
                    // Passing latitude to the PendingActivity
                    proximityIntent.putExtra("lat",point.latitude);
                    
                    
                    // Passing longitude to the PendingActivity
                    proximityIntent.putExtra("lng", point.longitude);
                    
                    // Creating a pending intent which will be invoked by LocationManager when the specified region is
                    // entered or exited
                    pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, proximityIntent,Intent.FLAG_ACTIVITY_NEW_TASK);
                    
                    
                    // Setting proximity alert 
                    // The pending intent will be invoked when the device enters or exits the region 20 meters
                    // away from the marked point
                    // The -1 indicates that, the monitor will not be expired
                    locationManager.addProximityAlert(point.latitude, point.longitude, 50, -1, pendingIntent);                
                    
                    /** Opening the editor object to write data to sharedPreferences */
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    // Storing the latitude for the i-th location
                    editor.putString("lat"+ Integer.toString((locationCount-1)), Double.toString(point.latitude));

                    // Storing the longitude for the i-th location
                    editor.putString("lng"+ Integer.toString((locationCount-1)), Double.toString(point.longitude));

                    // Storing the count of locations or marker count
                    editor.putInt("locationCount", locationCount);
                    
                    /** Storing the zoom level to the shared preferences */
                    editor.putString("zoom", Float.toString(googleMap.getCameraPosition().zoom));                

                    /** Saving the values stored in the shared preferences */
                    editor.commit();                
                    
                    Toast.makeText(getBaseContext(), "Proximity Alert is added", Toast.LENGTH_SHORT).show();                    
                    
                }
            });    
            
            googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {                
                @Override
                public void onMapLongClick(LatLng point) {
                    Intent proximityIntent = new Intent("com.maps.alerts.activity.proximity");
                    
                    pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, proximityIntent,Intent.FLAG_ACTIVITY_NEW_TASK);
                    
                    // Removing the proximity alert                    
                    locationManager.removeProximityAlert(pendingIntent);
                    
                    // Removing the marker and circle from the Google Map
                    googleMap.clear();
                    
                    // Opening the editor object to delete data from sharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    
                    // Clearing the editor
                    editor.clear();
                    
                    // Committing the changes
                    editor.commit();
                    
                    Toast.makeText(getBaseContext(), "Proximity Alert is removed", Toast.LENGTH_LONG).show();
                }
            }); 
            
            
            
        }        
    }
    
    
    private void drawCircle(LatLng point){
        
        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions();
     
        // Specifying the center of the circle
        circleOptions.center(point);
        
        // Radius of the circle
        circleOptions.radius(20);
        
        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK);
        
        // Fill color of the circle
        circleOptions.fillColor(0x30ff0000);
        
        // Border width of the circle
        circleOptions.strokeWidth(2);
        
        // Adding the circle to the GoogleMap
        googleMap.addCircle(circleOptions);
        
    }    
    
    private void drawLocationCircle(double lati,double longi)
    {
       CircleOptions circleOptions = new CircleOptions();
       LatLng point2 = new LatLng(lati, longi);
      
        
        // Specifying the center of the circle

        circleOptions.center(point2);
        
        // Radius of the circle
        circleOptions.radius(10);
        
        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK);
        
        // Fill color of the circle
        circleOptions.fillColor(0x30ff0000);
        
        // Border width of the circle
        circleOptions.strokeWidth(2);
        
        // Adding the circle to the GoogleMap
        googleMap.addCircle(circleOptions);
    }
    
    
    private void drawMarker(LatLng point){
        // Creating an instance of MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();                    
        
        // Setting latitude and longitude for the marker
        markerOptions.position(point);
        
        // Adding InfoWindow title
        markerOptions.title("Location Coordinates");        
        
        // Adding InfoWindow contents
        markerOptions.snippet(Double.toString(point.latitude) + "," + Double.toString(point.longitude));        
        
        // Adding marker on the Google Map
        googleMap.addMarker(markerOptions);
        
    }
    private void drawMarkerLocation(double lati,double longi){
        // Creating an instance of MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();                    
        LatLng point2 = new LatLng(lati, longi);
        // Setting latitude and longitude for the marker
        markerOptions.position(point2);
        
        // Adding InfoWindow title
        markerOptions.title("Location Coordinates");        
        
        // Adding InfoWindow contents
        markerOptions.snippet(Double.toString(point2.latitude) + "," + Double.toString(point2.longitude));        
        
        // Adding marker on the Google Map
        googleMap.addMarker(markerOptions);
        
    }
    
    public void setLocations(double latitude, double longitude, int place, String location){
    	drawMarkerLocation(latitude,longitude);
        drawLocationCircle(latitude,longitude); 
    	Intent proximityIntent2 = new Intent("com.maps.activity.proximity");
        proximityIntent2.putExtra("lat",latitude);
        proximityIntent2.putExtra("lng",longitude);
        proximityIntent2.putExtra("place",place);
        proximityIntent2.putExtra("location", location);
        IntentFilter filter = new IntentFilter("1000");
        registerReceiver(new ProximityReciever(), filter);
        pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, proximityIntent2,Intent.FLAG_ACTIVITY_NEW_TASK);
        locationManager.addProximityAlert(latitude,longitude, 30, -1, pendingIntent);                
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lat"+ Integer.toString((locationCount-1)), Double.toString(latitude));
        editor.putString("lng"+ Integer.toString((locationCount-1)), Double.toString(longitude));
        editor.putInt("locationCount", locationCount);
        editor.putString("zoom", Float.toString(googleMap.getCameraPosition().zoom));                
        editor.commit();
        
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}