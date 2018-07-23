package codes.kevinvanzyl.dvtweather;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String APP = "APP";
    private static final String THEME = "THEME";
    private static final int THEME_FOREST = 0;
    private static final int THEME_SEA = 1;

    private static final int SKY_CLOUDY = 0;
    private static final int SKY_SUNNY = 1;
    private static final int SKY_RAINY = 2;

    int currentTheme;
    int currentSky;

    private ImageView imgToday;
    private LinearLayout panelForecast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences(APP, MODE_PRIVATE);
        currentTheme = preferences.getInt(THEME, THEME_FOREST);
        currentSky = SKY_SUNNY;

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_main);

        imgToday = (ImageView) findViewById(R.id.image_today);
        panelForecast = (LinearLayout) findViewById(R.id.panel_forecast);

        updateBackground();
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
}
