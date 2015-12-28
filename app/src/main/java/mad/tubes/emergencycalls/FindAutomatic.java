package mad.tubes.emergencycalls;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
    TextView resultText;
    ListView lvHasil;

    int radius;

    ArrayAdapter<String> adapter;
    String [][] stringFromAsync;
    String [] types = {"police", "hospital", "fire_station"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_automatic);
        setTitle("Mencari...");
        getLocation();


        policeButton = (Button) findViewById(R.id.policeButton);
        fireStationButton = (Button) findViewById(R.id.fireStationButton);
        hospitalButton = (Button) findViewById(R.id.hospitalButton);


        lvHasil = (ListView) findViewById(R.id.listView);
        resultText = (TextView) findViewById(R.id.resultText);

        refreshButton = (Button) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });


        policeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(stringFromAsync!=null){
                    adapter = new ArrayAdapter<String>(FindAutomatic.this,android.R.layout.simple_list_item_1, stringFromAsync[0]);
                    lvHasil.setAdapter(adapter);
                    resultText.setText("Daftar Kantor Polisi Terdekat dalam radius "+radius+" m");
                }else{
                    Toast.makeText(getApplicationContext(), "Please wait...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        hospitalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(stringFromAsync!=null){
                    adapter = new ArrayAdapter<String>(FindAutomatic.this,android.R.layout.simple_list_item_1, stringFromAsync[1]);
                    lvHasil.setAdapter(adapter);
                    resultText.setText("Daftar Rumah Sakit Terdekat dalam radius "+radius+" m");
                }else{
                    Toast.makeText(getApplicationContext(), "Please wait...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fireStationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(stringFromAsync!=null){
                    adapter = new ArrayAdapter<String>(FindAutomatic.this,android.R.layout.simple_list_item_1, stringFromAsync[2]);
                    lvHasil.setAdapter(adapter);
                    resultText.setText("Daftar Kantor Pemadam Kebakaran Terdekat dalam radius "+radius+" m");
                }else{
                    Toast.makeText(getApplicationContext(), "Please wait...", Toast.LENGTH_SHORT).show();
                }
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

            place.execute(location);

        }else{
            gps.showSettingsAlert();
        }
    }

    private class PlacesFetcher extends AsyncTask<Location, Integer, String[][]> {
        @Override
        protected String[][] doInBackground(Location... params) {
            StringBuffer bufferPlaces = null;
            Location location = (Location) params[0];
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            String apiKey = "AIzaSyDhoQvCmrgH-tkN0LNbiQZqPs-zoZzz1hM";

            radius = 1500;

            //LOCATION ON GOOGLE WEB SERVICE IS LAT/LONG, PLEASE NOTICE
            //DO NOT REPEAT THE SAME MISTAKE

            String[][] allResult= new String[types.length][];
            String [] tempResult = null;

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

                try {
                    JSONObject objectPlaces = new JSONObject(resultPlaces);
                    JSONArray arrayPlaces = objectPlaces.getJSONArray("results");

                    tempResult = new String[arrayPlaces.length()];

                    for(int i=0; i<arrayPlaces.length(); i++){

                        JSONObject place = arrayPlaces.getJSONObject(i);
                        String placeName = place.getString("name");
                        String placeId = place.getString("place_id");
                        String placeDetails = getPlaceDetails(placeId);

                        tempResult[i] = (i+1)+". "+placeName+"\n"+placeDetails;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                allResult[a] =tempResult;
            }

            return allResult;
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

//                if(address==""){
//                    address ="Data alamat tidak tersedia.";
//                }
//                if(phone==""){
//                    phone ="Data nomor telepon tidak tersedia.";
//                }

                finalResult.append(address+" "+phone);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return finalResult.toString();
        }

        @Override
        protected void onPostExecute(String [][]sResult) {
            super.onPostExecute(sResult);
            resultText.setText("\nData sudah difetch. Silahkan pilih menu.");
            stringFromAsync = sResult;
        }
    }


}
