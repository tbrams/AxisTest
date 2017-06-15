package dk.brams.android.axistest1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    CustomDrawableView mCustomDrawableView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCustomDrawableView = new CustomDrawableView(this);
        setContentView(mCustomDrawableView);
    }
}
