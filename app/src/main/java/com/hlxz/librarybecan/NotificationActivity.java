package com.hlxz.librarybecan;
import com.navigine.naviginesdk.*;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.text.method.ScrollingMovementMethod;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.util.*;
import java.io.*;
import java.lang.*;
import java.util.*;

public class NotificationActivity extends Activity
{
  // GUI parameters
  private TextView  mTitleLabel   = null;
  private TextView  mTextLabel    = null;
  
  private int       mId           = 0;
  private String    mName         = "";
  private String    mUuid         = "";
  
  /** Called when the activity is hlxz created */
  @Override public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_notification);
    
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                         WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    
    mId   = getIntent().getIntExtra("zone_id", 0);
    mUuid = getIntent().getStringExtra("zone_uuid");
    mName = getIntent().getStringExtra("zone_name");
    
    mTitleLabel = (TextView)findViewById(R.id.notification__title_label);
    mTextLabel  = (TextView)findViewById(R.id.notification__text_label);

    mTitleLabel.setText(mName);
    mTextLabel.setText("已經進入區域「" + mName + "」"+mId+"OOOOO"+mUuid);
  }
  
  public void onClose(View v)
  {
    finish();
  }
  
}
