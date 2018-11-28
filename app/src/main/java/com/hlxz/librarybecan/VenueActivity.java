package com.hlxz.librarybecan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.navigine.naviginesdk.Location;
import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;
import com.navigine.naviginesdk.Venue;
import java.util.Timer;
import java.util.TimerTask;

public class VenueActivity extends Activity {

    private TextView mDescriptionLabel = null;
    private ImageView mImageView = null;
    private TextView mTitleLabel = null;
    private Venue mVenue = null;

    private int Pid[] ;
    private float Pxy[];
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        setContentView(R.layout.venue);
        getWindow().setFlags(67108864, 67108864);
        Pid = getIntent().getExtras().getIntArray("Pid");
        Pxy = getIntent().getExtras().getFloatArray("Pxy");
        int id = getIntent().getExtras().getInt("venue_id", 0);
        NavigationThread mNavigation = NavigineSDK.getNavigation();


        if (mNavigation != null) {
            Location loc = mNavigation.getLocation();
            if (loc != null) {
                this.mVenue = loc.getVenue(id);
            }
        }

        this.mTitleLabel = (TextView) findViewById(R.id.venue__title_text_view);
        this.mDescriptionLabel = (TextView) findViewById(R.id.venue__description_label);
        this.mImageView = (ImageView) findViewById(R.id.venue__image);
        this.mImageView.setLayerType(1, null);
        this.mImageView.setBackgroundColor(Color.argb(255, 230, 230, 230));

        if (this.mVenue != null) {
            String titleText = this.mVenue.name;
            if (titleText.length() > 25) {
                titleText = titleText.substring(0, 24) + "…";
            }
            this.mTitleLabel.setText(titleText);
            this.mDescriptionLabel.setText(this.mVenue.description);
            if(this.mDescriptionLabel.getText().equals("")) this.mDescriptionLabel.setText("無相關資料。");
            Bitmap bm = this.mVenue.getBitmap();
            if (bm != null) {
                this.mImageView.setImageBitmap(bm);
            }
        }

    }

    public void onBackPressed() {
        sendCancelRoute();
        finish();
    }

    public void onBackButtonClicked(View v) {
        sendCancelRoute();
        finish();
    }

    public void onRouteToPlace(View v) {
        if (this.mVenue != null) {
            Intent extra = new Intent();
            extra.putExtra("venue_id",this.mVenue.id);
            extra.putExtra("Pid",Pid);
            extra.putExtra("Pxy",Pxy);
            setResult(-1, extra);
        }
        finish();
    }
    public void sendCancelRoute(){
        Intent intent = new Intent();
        intent.putExtra("bool",true);
        setResult(-2,intent);

    }
}