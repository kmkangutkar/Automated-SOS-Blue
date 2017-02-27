package com.example.komal.blue;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by komal on 16/1/17.
 */

public class FindLocation implements LocationListener{

    private final Context locationContext;
    private boolean gpsEnabled, networkEnabled, canGetLocation;
    protected LocationManager locationManager;
    private Location location = null;
    private static final int UPDATE_INTERVAL = 5000; //5 seconds
    private static final int UPDATE_DISTANCE = 10; //10 meters

    //constructor
    public FindLocation(Context myContext){
        this.locationContext = myContext;
    }

    public Location getLocation() {
        locationManager = (LocationManager)locationContext.getSystemService(Context.LOCATION_SERVICE);
        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGpsEnabled() && !isNetworkEnabled()){
            //cannot get location
            canGetLocation = false;
        }else {
            canGetLocation = true;
            if (isNetworkEnabled()){
                providerManager(LocationManager.NETWORK_PROVIDER);
                System.out.println("Using Network");
                Log.d("Network", "Network1");
            }
            if (isGpsEnabled() && location == null){
                //if network doesn't work use gps
                providerManager(LocationManager.GPS_PROVIDER);
                System.out.println("Using gps");
                Log.d("gps", "gps1");
            }
        }
        return location;
    }

    private void providerManager(String provider){
        //shows error because it thinks that permission check has not been done
        //permission check is done separately
        locationManager.requestLocationUpdates(provider, UPDATE_INTERVAL, UPDATE_DISTANCE, this);
        if(locationManager != null){
            location = locationManager.getLastKnownLocation(provider);
        }
    }

    public boolean isGpsEnabled() {
        return gpsEnabled;
    }


    public boolean isNetworkEnabled() {
        return networkEnabled;
    }

    public boolean isCanGetLocation() {
        return canGetLocation;
    }

    public double getLatitude(){
        if(location != null){
            return location.getLatitude();
        }
        //location = null
        return -0.1;
    }

    public double getLongitude(){
        if(location != null){
            return location.getLongitude();
        }
        //location = null
        return -0.1;
    }

    @Override
    public void onLocationChanged(Location location) {
        //update location
        this.location = location;
        Toast.makeText(locationContext.getApplicationContext(), " Location changed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        //enable provider if disabled
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        locationContext.startActivity(settingsIntent);
    }
}
