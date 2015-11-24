package andreas.gps;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by Andreas on 20/11/2015.
 */
public class Shop extends AppCompatActivity {

    private String PointInformation = "Remaining Points :  ";
    static final String STATE_SCORE = "playerScore";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_shop);

        //login
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_low_in_rank);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.i("point", "before succes");
        Log.i("point", "succes1");
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        int storedPreference = preferences.getInt(STATE_SCORE, 10);
        Log.i("point", "succes2");
        TextView points_score = (TextView) findViewById(R.id.powerUpPoints);
        String points_str = Integer.toString(storedPreference);
        points_score.setText(PointInformation + points_str);
    // set remaining points



    }

}
