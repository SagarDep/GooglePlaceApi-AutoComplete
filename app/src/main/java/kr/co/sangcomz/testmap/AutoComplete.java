package kr.co.sangcomz.testmap;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kr.co.sangcomz.testmap.bean.AutoCompleteBean;


public class AutoComplete extends ActionBarActivity implements LocationListener {

    private ArrayList<AutoCompleteBean> resultList;
    private ArrayList<Double> locationResult;
    GoogleMap googleMap;
    double latitude = 0;
    double longitude = 0;

    private static final String LOG_TAG = "ExampleApp";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_SEARCH = "/search";
    private static final String TYPE_DETAILS = "/details";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyA65-eqSvIefv4lY3vARmN4fwVc1d4lPaE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_complete);

        AutoCompleteTextView autoCompView = (AutoCompleteTextView)findViewById(R.id.autocomplete);
        autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));

        autoCompView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                System.out.println("click");
                System.out.println(resultList.get(position).getDescription());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        locationResult = Details(resultList.get(position).getDescription(), resultList.get(position).getReference());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setLocation(locationResult.get(0), locationResult.get(1));
                            }
                        });
                    }
                }).start();
            }
        });


        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        googleMap = fragment.getMap();
        googleMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);

    }

    public void setLocation(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
        LatLng latLng = new LatLng(latitude, longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(17));

        MarkerOptions marker = new MarkerOptions().position(latLng).snippet("클릭하시면 위치가 선택됩니다.");

        marker.title(getAddress(latLng.latitude, latLng.longitude));
//        marker.position(latLng);

        googleMap.clear();
        googleMap.addMarker(marker);
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        System.out.println("latitude :::: " + latitude + " longitude :::: " + longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
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

    private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
//        private ArrayList<AutoCompleteBean> resultList;
        private ArrayList<String> result;

        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index).getDescription();
        }


        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());
                        result = new ArrayList<String>(resultList.size());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }};


            return filter;
        }
    }
    private ArrayList<AutoCompleteBean> autocomplete(String input) {

        ArrayList<AutoCompleteBean> resultList = null;
        AutoCompleteBean autoCompleteBean;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?input=" + URLEncoder.encode(input, "utf8"));
            sb.append("&sensor=true&key=" + API_KEY);
//            sb.append("&components=country:kr");

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());

            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
//            System.out.println(predsJsonArray.length());
//            System.out.println("jsonObj.toString() :::: " + jsonObj.toString());
            // Extract the Place descriptions from the results
            resultList = new ArrayList<AutoCompleteBean>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
//                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
                resultList.add(new AutoCompleteBean(predsJsonArray.getJSONObject(i).getString("description"), predsJsonArray.getJSONObject(i).getString("reference")));
//                Details(predsJsonArray.getJSONObject(i).getString("description"), predsJsonArray.getJSONObject(i).getString("reference"));
//                resultList.add(predsJsonArray.getJSONObject(i).getString("reference"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    private ArrayList<Double> Details(String description, String reference ) {

        ArrayList<Double> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        //    https://maps.googleapis.com/maps/api/place/details/json
        // ?reference=CmRYAAAAciqGsTRX1mXRvuXSH2ErwW-jCINE1aLiwP64MCWDN5vkXvXoQGPKldMfmdGyqWSpm7BEYCgDm-iv7Kc2PF7QA7brMAwBbAcqMr5i1f4PwTpaovIZjysCEZTry8Ez30wpEhCNCXpynextCld2EBsDkRKsGhSLayuRyFsex6JA6NPh9dyupoTH3g
        // &key=AddYourOwnKeyHere

        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_DETAILS + OUT_JSON);
            sb.append("?reference=" + URLEncoder.encode(reference, "utf8"));
            sb.append("&key=" + API_KEY);
//            sb.append("&components=country:kr");

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONObject jsonObjResult = jsonObj.getJSONObject("result");
            JSONObject jsonObjGemmetry = jsonObjResult.getJSONObject("geometry");
            JSONObject jsonObjLocation = jsonObjGemmetry.getJSONObject("location");

            System.out.println("jsonObj.toString() :::: " + jsonObj.toString());
            System.out.println("jsonObjLocation.toString() :::: " + jsonObjLocation.toString());

            resultList = new ArrayList<Double>(2);
            resultList.add(jsonObjLocation.getDouble("lat"));
            resultList.add(jsonObjLocation.getDouble("lng"));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    public String getAddress(double lat, double lng) {
        String address = "";

        Geocoder geocoder = new Geocoder(this, Locale.KOREA);

        List<Address> list = null;

        try {
            list = geocoder.getFromLocation(lat, lng, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (list == null) {
            Log.d("실패", "실패");
            return null;
        }

        if (list.size() > 0) {
            Address addr = list.get(0);
            Log.d(String.valueOf(addr.getMaxAddressLineIndex()), addr.toString());
            for (int j = 0; j <= addr.getMaxAddressLineIndex(); j++) {
                address = address + addr.getAddressLine(j);
            }

        }

        return address;
    }
}
