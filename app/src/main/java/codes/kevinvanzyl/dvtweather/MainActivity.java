package codes.kevinvanzyl.dvtweather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String THEME = "THEME";
    private static final String PREFERENCE_CURRENT_WEATHER = "CURRENT_WEATHER";

    private static final int THEME_FOREST = 0;
    private static final int THEME_SEA = 1;

    private static final int SKY_CLOUDY = 0;
    private static final int SKY_SUNNY = 1;
    private static final int SKY_RAINY = 2;

    private static final int MY_PERMISSIONS_REQUEST_ACESS_COARSE_LOCATION = 252;

    private static final String API_URL_SCHEME = "https";
    private static final String API_URL_AUTHORITY = "api.openweathermap.org";
    private static final String[] API_URL_PATH = {"data", "2.5", "weather"};
    private static final String API_URL_KEY_LAT = "lat";
    private static final String API_URL_KEY_LON = "lon";
    private static final String API_APPID = "d58f73abc992aa3eec2f39ebda4a61e2";
    private static final String API_URL_KEY_APPID = "APPID";

    private static final int WEATHER_COND_CLEAR_SKY = 800;

    private static final int WEATHER_COND_FEW_CLOUDS = 801;
    private static final int WEATHER_COND_SCATTERED_CLOUDS = 802;
    private static final int WEATHER_COND_BROKEN_CLOUDS = 803;
    private static final int WEATHER_COND_OVERCAST_CLOUDS = 804;

    private static final int WEATHER_COND_LIGHT_RAIN = 500;
    private static final int WEATHER_COND_MODERATE_RAIN = 501;
    private static final int WEATHER_COND_HEAVY_INTENSITY_RAIN = 502;
    private static final int WEATHER_COND_VERY_HEAVY_RAIN = 503;
    private static final int WEATHER_COND_EXTREME_RAIN = 504;
    private static final int WEATHER_COND_FREEZING_RAIN = 511;
    private static final int WEATHER_COND_LIGHT_INTENSITY_SHOWER_RAIN = 520;
    private static final int WEATHER_COND_SHOWER_RAIN = 521;
    private static final int WEATHER_COND_HEAVY_INTENSITY_SHOWER_RAIN = 522;
    private static final int WEATHER_COND_RAGGED_SHOWER_RAIN = 531;

    private static final int[] WEATHER_COND_RAINY = {
            WEATHER_COND_LIGHT_RAIN, WEATHER_COND_MODERATE_RAIN, WEATHER_COND_HEAVY_INTENSITY_RAIN, WEATHER_COND_VERY_HEAVY_RAIN,
            WEATHER_COND_EXTREME_RAIN, WEATHER_COND_FREEZING_RAIN, WEATHER_COND_LIGHT_INTENSITY_SHOWER_RAIN, WEATHER_COND_SHOWER_RAIN,
            WEATHER_COND_HEAVY_INTENSITY_SHOWER_RAIN, WEATHER_COND_RAGGED_SHOWER_RAIN
    };
    private static final int[] WEATHER_COND_CLOUDY = {
            WEATHER_COND_FEW_CLOUDS, WEATHER_COND_SCATTERED_CLOUDS, WEATHER_COND_BROKEN_CLOUDS, WEATHER_COND_OVERCAST_CLOUDS
    };

    SharedPreferences preferences;

    int currentTheme;
    int currentSky;

    private ImageView imgToday;
    private LinearLayout panelForecast;
    private TextView txtCurrentTemp;
    private TextView txtCurrentSky;

    JSONObject currentWeather;
    LocationManager locationManager;
    String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentTheme = preferences.getInt(THEME, THEME_FOREST);

        setContentView(R.layout.activity_main);

        imgToday = (ImageView) findViewById(R.id.image_today);
        panelForecast = (LinearLayout) findViewById(R.id.panel_forecast);
        txtCurrentTemp = (TextView) findViewById(R.id.text_current_temp);
        txtCurrentSky = (TextView) findViewById(R.id.text_current_sky);

        try {
            currentWeather = new JSONObject(preferences.getString(PREFERENCE_CURRENT_WEATHER, ""));
            updateCurrentWeather();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACESS_COARSE_LOCATION);
        }
        else {

            getCurrentWeather();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @SuppressLint("MissingPermission")
    private void getCurrentWeather() {

        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        locationManager.requestLocationUpdates(provider, 400, 1, this);

        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {

            onLocationChanged(location);
        }
        else {
            Toast.makeText(this, "Location is unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private class CurrentWeatherTask extends AsyncTask<Double, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Double... doubles) {

            try {
                //https://api.openweathermap.org/data/2.5/weather
                Uri.Builder builder = new Uri.Builder();
                builder.scheme(API_URL_SCHEME).authority(API_URL_AUTHORITY);
                for (String path: API_URL_PATH) {
                    builder.appendPath(path);
                }
                builder.appendQueryParameter(API_URL_KEY_APPID, API_APPID)
                        .appendQueryParameter(API_URL_KEY_LAT, doubles[0]+"")
                        .appendQueryParameter(API_URL_KEY_LON, doubles[1]+"");

                String apiUrl = builder.build().toString();
                URL url = new URL(apiUrl);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStream stream = new BufferedInputStream(httpURLConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder stringBuilder = new StringBuilder();

                String inputString;
                while ((inputString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(inputString);
                }

                JSONObject topLevel = new JSONObject(stringBuilder.toString());
                Log.d(TAG, "Weather api returned: "+topLevel.toString());
                return topLevel;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            if (jsonObject != null) {
                currentWeather = jsonObject;
                preferences.edit().putString(PREFERENCE_CURRENT_WEATHER, jsonObject.toString()).apply();
                updateCurrentWeather();
            }
        }
    }

    private void updateCurrentWeather() {

        try {
            JSONObject main = currentWeather.getJSONObject("main");
            double temp = kelvinToCelcius(main.getDouble("temp"));
            int intTemp = (int) Math.round(temp);
            txtCurrentTemp.setText(intTemp+"º");

            JSONArray weather = currentWeather.getJSONArray("weather");
            JSONObject objWeather = weather.getJSONObject(0);
            int weatherCode = objWeather.getInt("id");
            int sky = getSky(weatherCode);
            txtCurrentSky.setText(getSkyName(sky));

            currentSky = sky;
            updateBackground();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String getSkyName(int sky) {

        if (sky == SKY_CLOUDY) {
            return "CLOUDY";
        }
        else if (sky == SKY_RAINY) {
            return "RAINY";
        }
        else {
            return "SUNNY";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACESS_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    getCurrentWeather();
                } else {

                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setMessage("The app needs to access your coarse location to check which city you are in.\n\nPlease allow access now to use the app.");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {

                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_ACESS_COARSE_LOCATION);
                        }
                    });
                    alertBuilder.show();
                }
                return;
            }
        }
    }

    private void updateBackground() {

        int statusColor;
        int backgroundDrawable;
        int forecastBackgroundColor;

        switch (currentTheme) {
            case THEME_FOREST:

                switch (currentSky) {
                    case SKY_CLOUDY:
                        statusColor = R.color.forest_cloudy_status_blue;
                        backgroundDrawable = R.drawable.forest_cloudy;
                        forecastBackgroundColor = R.color.forest_cloudy_blue;
                        break;
                    case SKY_RAINY:
                        statusColor = R.color.forest_rainy_status_grey;
                        backgroundDrawable = R.drawable.forest_rainy;
                        forecastBackgroundColor = R.color.forest_rainy_gray;
                        break;
                    default:
                        statusColor = R.color.forest_sunny_status_orange;
                        backgroundDrawable = R.drawable.forest_sunny;
                        forecastBackgroundColor = R.color.forest_sunny_green;
                }
                break;
            default:
                switch (currentSky) {
                    case SKY_CLOUDY:
                        statusColor = R.color.sea_cloudy_status_blue;
                        backgroundDrawable = R.drawable.sea_cloudy;
                        forecastBackgroundColor = R.color.sea_cloudy_blue;
                        break;
                    case SKY_RAINY:
                        statusColor = R.color.sea_rainy_status_grey;
                        backgroundDrawable = R.drawable.sea_rainy;
                        forecastBackgroundColor = R.color.sea_rainy_gray;
                        break;
                    default:
                        statusColor = R.color.sea_sunny_status_yellow;
                        backgroundDrawable = R.drawable.sea_sunny;
                        forecastBackgroundColor = R.color.sea_sunny_blue;
                }
        }

        imgToday.setImageResource(backgroundDrawable);
        panelForecast.setBackgroundColor(ContextCompat.getColor(this, forecastBackgroundColor));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusColor));
    }

    @Override
    public void onLocationChanged(Location location) {

        double lat = location.getLatitude();
        double lng = location.getLongitude();

        Double[] latlng = {lat, lng};

        CurrentWeatherTask currentWeatherTask = new CurrentWeatherTask();
        currentWeatherTask.execute(latlng);
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

    public double kelvinToCelcius(double kelvin) {
        return kelvin - 273.15;
    }

    public int getSky(int weatherCode) {

        for (int i: WEATHER_COND_RAINY) {
            if (i == weatherCode) {
                return SKY_RAINY;
            }
        }
        for (int i: WEATHER_COND_CLOUDY) {
            if (i == weatherCode) {
                return SKY_CLOUDY;
            }
        }
        return SKY_SUNNY;
    }
}
