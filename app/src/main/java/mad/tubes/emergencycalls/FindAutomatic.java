package mad.tubes.emergencycalls;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FindAutomatic extends Activity{
    TextView locationText;
    Button refreshButton;
    Button policeButton;
    Button fireStationButton;
    Button hospitalButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_automatic);
        setTitle("Mencari...");
        getLocation();

        policeButton = (Button) findViewById(R.id.policeButton);
        fireStationButton = (Button) findViewById(R.id.fireStationButton);
        hospitalButton = (Button) findViewById(R.id.hospitalButton);

        refreshButton = (Button) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });

    }

    public void getLocation(){
        GPSTracker gps = new GPSTracker(this);

        if(gps.canGetLocation()){
            final Location location = gps.getLocation();
            double lat = gps.getLatitude();
            double lng = gps.getLongitude();
            locationText = (TextView) findViewById(R.id.locationText);
            locationText.setText("Anda berada di...\nlat: "+lat+"\nlong: "+lng);

            PlacesFetcher place = new PlacesFetcher();
            place.execute(location);
            Toast.makeText(getApplicationContext(), "Mencari pertolongan terdekat...", Toast.LENGTH_SHORT).show();
        }else{
            gps.showSettingsAlert();
        }
    }

    private class PlacesFetcher extends AsyncTask<Location, Integer, String> {
        @Override
        protected String doInBackground(Location... params) {
            StringBuffer bufferPlaces = null;
            Location location = params[0];
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            String apiKey = "AIzaSyDhoQvCmrgH-tkN0LNbiQZqPs-zoZzz1hM";

            String types ="hospital";
            int radius = 1000;

            //LOCATION ON GOOGLE WEB SERVICE IS LAT/LONG, PLEASE NOTICE
            //DO NOT REPEAT THE SAME MISTAKE

            String urlSearchPlaces ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "location="+lat+","+lng+"&radius="+radius+"&types="+types+"&key="+apiKey;

            try {
                URL urlPlaces = new URL(urlSearchPlaces);
                HttpURLConnection connection = (HttpURLConnection) urlPlaces.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader readerPlaces = new BufferedReader(new InputStreamReader(inputStream));
                bufferPlaces = new StringBuffer();
                String linePlaces = "";

                while ((linePlaces = readerPlaces.readLine())!=null){
                    bufferPlaces.append(linePlaces);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String resultPlaces = bufferPlaces.toString();
            StringBuffer newBuffer = new StringBuffer();

            try {
                JSONObject objectPlaces = new JSONObject(resultPlaces);
                JSONArray arrayPlaces = objectPlaces.getJSONArray("results");

                for(int i=0; i<arrayPlaces.length(); i++){

                    JSONObject place = arrayPlaces.getJSONObject(i);
                    String placeName = place.getString("name");
                    String placeId = place.getString("place_id");

                    newBuffer.append((i+1)+". "+placeName+"\n");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            resultPlaces = newBuffer.toString();
            return resultPlaces;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


            TextView resultText = (TextView) findViewById(R.id.resultText);

            resultText.setText("Daftar lokasi terdekat:\n"+s);
        }
    }


    }
