package pl.jakub.lineup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import neko.App;

import pl.jakub.lineup.R;

public class SplashActivity extends Activity {

    private static boolean firstLaunch = true;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (firstLaunch) {
            firstLaunch = false;
            setupSplash();
            App.loadAsynchronously("pl.jakub.lineup.MyActivity",
                                   new Runnable() {
                                       @Override
                                       public void run() {
                                           proceed();
                                       }});
        } else {
            proceed();
        }
    }

    public void setupSplash() {
        setContentView(R.layout.gamescreen);

        Button col1 = (Button)findViewById(R.id.col1);

        Button col2 = (Button)findViewById(R.id.col2);
        Button col3 = (Button)findViewById(R.id.col3);
        Button col4 = (Button)findViewById(R.id.col4);
        Button col5 = (Button)findViewById(R.id.col5);
        Button col6 = (Button)findViewById(R.id.col6);
        Button col7 = (Button)findViewById(R.id.col7);
        ImageView col0row0 = (ImageView)findViewById(R.id.col0row0);
        ImageView col0row1 = (ImageView)findViewById(R.id.col0row1);
        ImageView col0row2 = (ImageView)findViewById(R.id.col0row2);
        ImageView col0row3 = (ImageView)findViewById(R.id.col0row3);
        ImageView col0row4 = (ImageView)findViewById(R.id.col0row4);
        ImageView col0row5 = (ImageView)findViewById(R.id.col0row5);
        ImageView col1row0 = (ImageView)findViewById(R.id.col1row0);
        ImageView col1row1 = (ImageView)findViewById(R.id.col1row1);
        ImageView col1row2 = (ImageView)findViewById(R.id.col1row2);
        ImageView col1row3 = (ImageView)findViewById(R.id.col1row3);
        ImageView col1row4 = (ImageView)findViewById(R.id.col1row4);
        ImageView col1row5 = (ImageView)findViewById(R.id.col1row5);
        ImageView col2row0 = (ImageView)findViewById(R.id.col2row0);
        ImageView col2row1 = (ImageView)findViewById(R.id.col2row1);
        ImageView col2row2 = (ImageView)findViewById(R.id.col2row2);
        ImageView col2row3 = (ImageView)findViewById(R.id.col2row3);
        ImageView col2row4 = (ImageView)findViewById(R.id.col2row4);
        ImageView col2row5 = (ImageView)findViewById(R.id.col2row5);
        ImageView col3row0 = (ImageView)findViewById(R.id.col3row0);
        ImageView col3row1 = (ImageView)findViewById(R.id.col3row1);
        ImageView col3row2 = (ImageView)findViewById(R.id.col3row2);
        ImageView col3row3 = (ImageView)findViewById(R.id.col3row3);
        ImageView col3row4 = (ImageView)findViewById(R.id.col3row4);
        ImageView col3row5 = (ImageView)findViewById(R.id.col3row5);
        ImageView col4row0 = (ImageView)findViewById(R.id.col4row0);
        ImageView col4row1 = (ImageView)findViewById(R.id.col4row1);
        ImageView col4row2 = (ImageView)findViewById(R.id.col4row2);
        ImageView col4row3 = (ImageView)findViewById(R.id.col4row3);
        ImageView col4row4 = (ImageView)findViewById(R.id.col4row4);
        ImageView col4row5 = (ImageView)findViewById(R.id.col4row5);
        ImageView col5row0 = (ImageView)findViewById(R.id.col5row0);
        ImageView col5row1 = (ImageView)findViewById(R.id.col5row1);
        ImageView col5row2 = (ImageView)findViewById(R.id.col5row2);
        ImageView col5row3 = (ImageView)findViewById(R.id.col5row3);
        ImageView col5row4 = (ImageView)findViewById(R.id.col5row4);
        ImageView col5row5 = (ImageView)findViewById(R.id.col5row5);
        ImageView col6row0 = (ImageView)findViewById(R.id.col6row0);
        ImageView col6row1 = (ImageView)findViewById(R.id.col6row1);
        ImageView col6row2 = (ImageView)findViewById(R.id.col6row2);
        ImageView col6row3 = (ImageView)findViewById(R.id.col6row3);
        ImageView col6row4 = (ImageView)findViewById(R.id.col6row4);
        ImageView col6row5 = (ImageView)findViewById(R.id.col6row5);

        setContentView(R.layout.splashscreen);

        TextView appNameView = (TextView)findViewById(R.id.splash_app_name);
        appNameView.setText(R.string.app_name);

        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.splash_rotation);
        ImageView circleView = (ImageView)findViewById(R.id.splash_circles);
        circleView.startAnimation(rotation);
    }

    public void proceed() {
        startActivity(new Intent("pl.jakub.lineup.MAIN"));
        finish();
    }

}
