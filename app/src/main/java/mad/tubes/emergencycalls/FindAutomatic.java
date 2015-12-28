package mad.tubes.emergencycalls;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
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
            locationText.setText("Anda berada di...\nlat: " + lat + "\nlong: " + lng);

            PlacesFetcher place = new PlacesFetcher();
            Toast.makeText(getApplicationContext(), "Mencari pertolongan terdekat...", Toast.LENGTH_SHORT).show();

            //String type = "police";
            place.execute(location);

//            policeButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    String type = "police";
//                    new PlacesFetcher().execute(location, type);
//                }
//            });
//
//            hospitalButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    String type = "hospital";
//                    new PlacesFetcher().execute(location, type);
//                }
//            });
//
//            fireStationButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    String type = "fire_station";
//                    new PlacesFetcher().execute(location, type);
//                }
//            });

        }else{
            gps.showSettingsAlert();
        }
    }

    private class PlacesFetcher extends AsyncTask<Location, Integer, String[]> {
        @Override
        protected String[] doInBackground(Location... params) {
            StringBuffer bufferPlaces = null;
            Location location = (Location) params[0];
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            String apiKey = "AIzaSyDhoQvCmrgH-tkN0LNbiQZqPs-zoZzz1hM";

            int radius = 1000;

            String [] types = {"police", "hospital", "fire_station"};
            //LOCATION ON GOOGLE WEB SERVICE IS LAT/LONG, PLEASE NOTICE
            //DO NOT REPEAT THE SAME MISTAKE

            String[] allResult=new String[types.length];


            for(int a=0; a<types.length; a++){

            String type = types[a];

            String urlSearchPlaces ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "location="+lat+","+lng+"&radius="+radius+"&types="+type+"&key="+apiKey;

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
                    String placeDetails = getPlaceDetails(placeId);

                    newBuffer.append((i+1)+". "+placeName+placeDetails+"\n");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            resultPlaces = newBuffer.toString();
            allResult[a] = resultPlaces;
            }

            return allResult;
        }

        @Override
        protected void onPostExecute(String []sResult) {
            super.onPostExecute(sResult);

            TextView resultText = (TextView) findViewById(R.id.resultText);
            for(int b=0; b<sResult.length; b++){
                resultText.append("Daftar lokasi terdekat:\n"+sResult[b]);
            }
            resultText.append("\nDONE!");
        }

        private String getPlaceDetails(String placeId){
            StringBuffer bufferDetails = null;
            String id = placeId;
            String apiKey = "AIzaSyDhoQvCmrgH-tkN0LNbiQZqPs-zoZzz1hM";
            String urlSearchDetails="https://maps.googleapis.com/maps/api/place/details/json?placeid="+id+"&key="+apiKey;;


            try {
                URL urlDetails = new URL(urlSearchDetails);
                HttpURLConnection connection = (HttpURLConnection) urlDetails.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader readerDetails = new BufferedReader(new InputStreamReader(inputStream));
                bufferDetails = new StringBuffer();
                String lineDetails="";

                while((lineDetails=readerDetails.readLine())!=null){
                    bufferDetails.append(lineDetails);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String details = bufferDetails.toString();

            StringBuffer finalResult = new StringBuffer();

            try {
                JSONObject objectDetails = new JSONObject(details);
                JSONObject objectResult = (JSONObject) objectDetails.get("result");

                String address = (String) objectResult.get("formatted_address");
                String phone = (String) objectResult.get("international_phone_number");


                finalResult.append(": "+address+" "+phone);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return finalResult.toString();
        }
    }


    }
