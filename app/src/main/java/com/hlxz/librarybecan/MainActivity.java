package com.hlxz.librarybecan;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.util.*;
import java.io.*;
import java.lang.*;
import java.lang.reflect.Field;
import java.util.*;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import com.navigine.naviginesdk.*;

public class MainActivity extends AppCompatActivity
{
  private static final String   TAG                     = "BeCanLibrary";
  private static final String   NOTIFICATION_CHANNEL    = "BEACANLIBRARY_NOTIFICATION_CHANNEL";
  private static final int      UPDATE_TIMEOUT          = 100;  // milliseconds
  private static final int      ADJUST_TIMEOUT          = 5000; // milliseconds
  private static final int      ERROR_MESSAGE_TIMEOUT   = 5000; // milliseconds
  private static final boolean  ORIENTATION_ENABLED     = true; // Show device orientation?
  private static final boolean  NOTIFICATIONS_ENABLED   = true; // Show zone notifications?\

  // NavigationThread instance
  private NavigationThread mNavigation            = null;

  // UI Parameters
  private LocationView  mLocationView             = null;
  private Button        mPrevFloorButton          = null;
  private Button        mNextFloorButton          = null;
  private View          mBackView                 = null;
  private View          mPrevFloorView            = null;
  private View          mNextFloorView            = null;
  private View          mZoomInView               = null;
  private View          mZoomOutView              = null;
  private View          mAdjustModeView           = null;
  private TextView      mCurrentFloorLabel        = null;
  private TextView      mErrorMessageLabel        = null;
  private Handler       mHandler                  = new Handler();
  private float         mDisplayDensity           = 0.0f;

  private boolean       mAdjustMode               = false;
  private long          mAdjustTime               = 0;

  private boolean       mMenuVisible              = false;
  private Venue         mPinVenue                 = null;
  private Context       mContext                  = this;
  private Venue         mVenue                    = null;

  // Location parameters
  private Location      mLocation                 = null;
  private int           mCurrentSubLocationIndex  = -1;

  private float parameter_RSSI = 1;

  // Device parameters
  private DeviceInfo    mDeviceInfo               = null; // Current device
  private LocationPoint mPinPoint                 = null; // Potential device target
  private LocationPoint mTargetPoint              = null; // Current device target
  private RectF         mPinPointRect             = null;
  
  private Bitmap  mVenueBitmap    = null;
  private Venue   mTargetVenue    = null;
  private Venue   mSelectedVenue  = null;
  private RectF   mSelectedVenueRect = null;
  private Zone    mSelectedZone   = null;

  private boolean route_or_not = true;

  private TextView mTextMessage;
  private TextView  mStatusLabel = null;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState)
  {
    Log.d(TAG, "MainActivity started");
    
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_main);
    //----------------------
    mTextMessage = (TextView) findViewById(R.id.message);

    //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TutorialFragment()).addToBackStack(null).commit();

    BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation_bar);
    BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
    Menu menu = bottomNavigationView.getMenu();
    MenuItem menuItem = menu.getItem(0);
    menuItem.setChecked(true);
    bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.navigation_navigation:
                    break;
                case R.id.navigation_tutorial:
                    Intent intent_tutorial = new Intent(MainActivity.this, TutorialActivity.class);
                    startActivity(intent_tutorial);
                    break;
                case R.id.navigation_function:
                    Intent intent_function = new Intent(MainActivity.this, FunctionActivity.class);
                    startActivity(intent_function);
                    break;
                case R.id.navigation_adjust:
                    Intent intent_adjust = new Intent(MainActivity.this, AdjustActivity.class);
                    startActivityForResult(intent_adjust,5);
                    break;
            }
            return false;
        }
    });

    // asking for user permissions
    ActivityCompat.requestPermissions(this, new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,}, 101);

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                         WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    //----------------------
    // Setting up GUI parameters
    mBackView = (View)findViewById(R.id.navigation__back_view);
    mPrevFloorButton = (Button)findViewById(R.id.navigation__prev_floor_button);
    mNextFloorButton = (Button)findViewById(R.id.navigation__next_floor_button);
    mPrevFloorView = (View)findViewById(R.id.navigation__prev_floor_view);
    mNextFloorView = (View)findViewById(R.id.navigation__next_floor_view);
    mCurrentFloorLabel = (TextView)findViewById(R.id.navigation__current_floor_label);
    mZoomInView  = (View)findViewById(R.id.navigation__zoom_in_view);
    mZoomOutView = (View)findViewById(R.id.navigation__zoom_out_view);
    mAdjustModeView = (View)findViewById(R.id.navigation__adjust_mode_view);
    mErrorMessageLabel = (TextView)findViewById(R.id.navigation__error_message_label);

    mBackView.setVisibility(View.INVISIBLE);
    mPrevFloorView.setVisibility(View.INVISIBLE);
    mNextFloorView.setVisibility(View.INVISIBLE);
    mCurrentFloorLabel.setVisibility(View.INVISIBLE);
    mZoomInView.setVisibility(View.INVISIBLE);
    mZoomOutView.setVisibility(View.INVISIBLE);
    mAdjustModeView.setVisibility(View.INVISIBLE);
    mErrorMessageLabel.setVisibility(View.GONE);
    
    mVenueBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.navigation70_65);
    
    // Initializing location view
    mLocationView = (LocationView)findViewById(R.id.navigation__location_view);
    mLocationView.setBackgroundColor(0xffebebeb);
    mLocationView.setListener
    (
      new LocationView.Listener()
      {
        @Override public void onClick     ( float x, float y ) { handleClick(x, y);     }
        @Override public void onLongClick ( float x, float y ) { handleLongClick(x, y); }
        @Override public void onScroll    ( float x, float y, boolean byTouchEvent ) { handleScroll ( x, y,  byTouchEvent ); }
        @Override public void onZoom      ( float ratio,      boolean byTouchEvent ) { handleZoom   ( ratio, byTouchEvent ); }
        
        @Override public void onDraw(Canvas canvas)
        {
          drawZones(canvas);
          drawPoints(canvas);
          drawVenues(canvas);
          drawDevice(canvas);
        }
      }
    );
    
    // Loading map only when location view size is known
    mLocationView.addOnLayoutChangeListener
    (
      new OnLayoutChangeListener()
      {
        @Override public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom)
        {
          int width  = right  - left;
          int height = bottom - top;
          if (width == 0 || height == 0)
            return;
          
          Log.d(TAG, "Layout chaged: " + width + "x" + height);
          
          int oldWidth  = oldRight  - oldLeft;
          int oldHeight = oldBottom - oldTop;
          if (oldWidth != width || oldHeight != height)
            loadMap();
        }
      }
    );
    
    mDisplayDensity = getResources().getDisplayMetrics().density;
    mNavigation     = NavigineSDK.getNavigation();
    
    // Setting up device listener
    if (mNavigation != null)
    {
      mNavigation.setDeviceListener
      (
        new DeviceInfo.Listener()
        {
          @Override public void onUpdate(DeviceInfo info) { handleDeviceUpdate(info); }
        }
      );
    }
    
    // Setting up zone listener
    if (mNavigation != null)
    {
      mNavigation.setZoneListener
      (
        new Zone.Listener()
        {
          @Override public void onEnterZone(Zone z) { handleEnterZone(z); }
          @Override public void onLeaveZone(Zone z) { handleLeaveZone(z); }
        }
      );
    }
    
    if (NOTIFICATIONS_ENABLED)
    {
      NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
      if (Build.VERSION.SDK_INT >= 26)
        notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL, "default",
                                                                              NotificationManager.IMPORTANCE_LOW));
    }

  }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == 2)
        {
            if(requestCode == 5)
            {
              Log.d("Yeeeeeeeeeeeee","Good");
                parameter_RSSI = data.getExtras().getInt("P");
            }
        }
        if(resultCode == -1)
        {
          if(requestCode == 3)
          {
            Log.d("Yeeeeeeeeeee?","1"+data.getExtras().getBoolean("venue_id"));
            int pid[] = data.getExtras().getIntArray("Pid");
            float pxy[] = data.getExtras().getFloatArray("Pxy");
            mNavigation.setTarget(new LocationPoint(pid[0],pid[1],pxy[0],pxy[1]));
           // mTargetVenue = mSelectedVenue;
            //mTargetPoint = null;
            //mNavigation.setTarget(new LocationPoint(mLocation.id, subLoc2.id, mTargetVenue.x, mTargetVenue.y));
            mBackView.setVisibility(View.VISIBLE);
          }
        }
    }
  
  @Override public void onDestroy()
  {
    if (mNavigation != null)
    {
      NavigineSDK.finish();
      mNavigation = null;
    }
    
    super.onDestroy();
  }

  @Override public void onBackPressed()
  {
    moveTaskToBack(true);
  }
  
  public void toggleAdjustMode(View v)
  {
    mAdjustMode = !mAdjustMode;
    mAdjustTime = 0;
    Button adjustModeButton = (Button)findViewById(R.id.navigation__adjust_mode_button);
    adjustModeButton.setBackgroundResource(mAdjustMode ?
                                           R.drawable.navigation_touch :
                                           R.drawable.navigation_action);
    mLocationView.redraw();
  }

  public void onNextFloor(View v)
  {
    if (loadNextSubLocation())
      mAdjustTime = System.currentTimeMillis() + ADJUST_TIMEOUT;
  }

  public void onPrevFloor(View v)
  {
    if (loadPrevSubLocation())
      mAdjustTime = System.currentTimeMillis() + ADJUST_TIMEOUT;
  }

  public void onZoomIn(View v)
  {
    mLocationView.zoomBy(1.25f);
  }

  public void onZoomOut(View v)
  {
    mLocationView.zoomBy(0.8f);
  }
  public void onShowVenue(View v) {
    if (this.mMenuVisible) {
      Log.d("MessageABC", "error");
      return;
    } else if (this.mPinVenue != null) {
      Bundle b = new Bundle();
      b.putInt("venue_id", this.mPinVenue.id);
      Intent intent = new Intent(this.mContext, VenueActivity.class);
      intent.putExtras(b);
      startActivityForResult(intent, 1);
      this.mPinVenue = null;
    }
  }

  public void onCancelRoute(View v)
  {
    if (mNavigation == null)
      return;

    mTargetPoint  = null;
    mTargetVenue  = null;
    mPinPoint     = null;
    mPinPointRect = null;

    mNavigation.cancelTargets();
    mBackView.setVisibility(View.GONE);
    mLocationView.redraw();
  }

  private void handleClick(float x, float y)
  {
    Log.d(TAG, String.format(Locale.ENGLISH, "Click at (%.2f, %.2f)", x, y));

    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return;

    SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
    if (subLoc == null)
      return;

    if (mPinPoint != null)
    {
      if (mPinPointRect != null && mPinPointRect.contains(x, y))
      {
        mTargetPoint  = mPinPoint;
        mTargetVenue  = null;
        mPinPoint     = null;
        mPinPointRect = null;
        mNavigation.setTarget(mTargetPoint);
        mBackView.setVisibility(View.VISIBLE);
        return;
      }
      cancelPin();
      return;
    }

    if (mSelectedVenue != null)
    {
      if (mSelectedVenueRect != null && mSelectedVenueRect.contains(x, y))
      {
        Log.d(TAG, "按第二次");
        //Bundle b = new Bundle();
        //b.putInt("venue_id", mPinVenue.id);
        int Pid[] = {mLocation.id, subLoc.id };
        float Pxy[] = {mSelectedVenue.x, mSelectedVenue.y};
        Intent intent = new Intent(mContext, VenueActivity.class);
        intent.putExtra("venue_id",mSelectedVenue.id);
        intent.putExtra("Pid",Pid);
        intent.putExtra("Pxy",Pxy);
        startActivityForResult(intent, 3);
        mPinVenue = null;
      }
      cancelVenue();
      return;
    }

    // Check if we touched venue
    mSelectedVenue = getVenueAt(x, y);
    mSelectedVenueRect = new RectF();

    // Check if we touched zone
    if (mSelectedVenue == null)
    {
      Zone Z = getZoneAt(x, y);
      if (Z != null)
        mSelectedZone = (mSelectedZone == Z) ? null : Z;
    }

    mLocationView.redraw();
  }
  
  private void handleLongClick(float x, float y)
  {
    Log.d(TAG, String.format(Locale.ENGLISH, "Long click at (%.2f, %.2f)", x, y));
    makePin(mLocationView.getAbsCoordinates(x, y));
    cancelVenue();
  }
  
  private void handleScroll(float x, float y, boolean byTouchEvent)
  {
    if (byTouchEvent)
      mAdjustTime = NavigineSDK.currentTimeMillis() + ADJUST_TIMEOUT;
  }
  
  private void handleZoom(float ratio, boolean byTouchEvent)
  {
    if (byTouchEvent)
      mAdjustTime = NavigineSDK.currentTimeMillis() + ADJUST_TIMEOUT;
  }

  private void handleEnterZone(Zone z)
  {
    Log.d(TAG, "Enter zone " + z.name);
    if (NOTIFICATIONS_ENABLED)
    {
      Intent notificationIntent = new Intent(this, NotificationActivity.class);
      notificationIntent.putExtra("zone_id",    z.id);
      notificationIntent.putExtra("zone_name",  z.name);
      notificationIntent.putExtra("zone_color", z.color);
      notificationIntent.putExtra("zone_alias", z.alias);

      // Setting up a notification
      Notification.Builder notificationBuilder = new Notification.Builder(this, NOTIFICATION_CHANNEL);
      notificationBuilder.setContentIntent(PendingIntent.getActivity(this, z.id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT));
      notificationBuilder.setContentTitle("新區域");
      notificationBuilder.setContentText("您已進入區域「" + z.name + "」");
      notificationBuilder.setSmallIcon(R.drawable.becan_logo);
      notificationBuilder.setAutoCancel(true);

      // Posting a notification
      NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.notify(z.id, notificationBuilder.build());
    }
  }
  
  private void handleLeaveZone(Zone z)
  {
    Log.d(TAG, "離開區域「" + z.name+"」");
    if (NOTIFICATIONS_ENABLED)
    {
      NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.cancel(z.id);
    }
  }
  
  private void handleDeviceUpdate(DeviceInfo deviceInfo)
  {
    mDeviceInfo = deviceInfo;
    if (mDeviceInfo == null)
      return;
    
    // Check if location is loaded
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return;
    
    if (mDeviceInfo.isValid())
    {
      cancelErrorMessage();
      mBackView.setVisibility(mTargetPoint != null || mTargetVenue != null ?
                              View.VISIBLE : View.GONE);
      if (mAdjustMode)
        adjustDevice();
    }
    else
    {
      mBackView.setVisibility(View.GONE);
      switch (mDeviceInfo.errorCode)
      {
        case 4:
          setErrorMessage("於導航範圍外！請確認藍牙功能是否開啟。");
          break;
        
        case 8:
        case 30:
          setErrorMessage("該區beacon數量不足！");
          break;
        
        default:
          setErrorMessage(String.format(Locale.ENGLISH,
                          "在該區域發生未知錯誤 '%s' (error code %d)! " +
                          "錯誤代碼XE10",
                          mLocation.name, mDeviceInfo.errorCode));
          break;
      }
    }
    
    // This causes map redrawing
    mLocationView.redraw();
  }
  
  private void setErrorMessage(String message)
  {
    mErrorMessageLabel.setText(message);
    mErrorMessageLabel.setVisibility(View.VISIBLE);
  }
  
  private void cancelErrorMessage()
  {
    mErrorMessageLabel.setVisibility(View.GONE);
  }
  
  private boolean loadMap()
  {
    if (mNavigation == null)
    {
      Log.e(TAG, "Can't load map! Navigine SDK is not available!");
      return false;
    }
    
    mLocation = mNavigation.getLocation();
    mCurrentSubLocationIndex = -1;
    
    if (mLocation == null)
    {
      Log.e(TAG, "Loading map failed: no location");
      return false;
    }
    
    if (mLocation.subLocations.size() == 0)
    {
      Log.e(TAG, "Loading map failed: no sublocations");
      mLocation = null;
      return false;
    }
    
    if (!loadSubLocation(0))
    {
      Log.e(TAG, "Loading map failed: unable to load default sublocation");
      mLocation = null;
      return false;
    }
    
    if (mLocation.subLocations.size() >= 2)
    {
      mPrevFloorView.setVisibility(View.VISIBLE);
      mNextFloorView.setVisibility(View.VISIBLE);
      mCurrentFloorLabel.setVisibility(View.VISIBLE);
    }
    mZoomInView.setVisibility(View.VISIBLE);
    mZoomOutView.setVisibility(View.VISIBLE);
    mAdjustModeView.setVisibility(View.VISIBLE);    
    
    mNavigation.setMode(NavigationThread.MODE_NORMAL);
    
    if (D.WRITE_LOGS)
    {
      mNavigation.setLogFile(getLogFile("log"));
      mNavigation.setTrackFile(getLogFile("trk"));
    }
    
    mLocationView.redraw();
    return true;
  }
  
  private boolean loadSubLocation(int index)
  {
    if (mNavigation == null)
      return false;
    
    if (mLocation == null || index < 0 || index >= mLocation.subLocations.size())
      return false;
    
    SubLocation subLoc = mLocation.subLocations.get(index);
    Log.d(TAG, String.format(Locale.ENGLISH, "Loading sublocation %s (%.2f x %.2f)", subLoc.name, subLoc.width, subLoc.height));
    
    if (subLoc.width < 1.0f || subLoc.height < 1.0f)
    {
      Log.e(TAG, String.format(Locale.ENGLISH, "Loading sublocation failed: invalid size: %.2f x %.2f", subLoc.width, subLoc.height));
      return false;
    }
    
    if (!mLocationView.loadSubLocation(subLoc))
    {
      Log.e(TAG, "Loading sublocation failed: invalid image");
      return false;
    }
    
    float viewWidth  = mLocationView.getWidth();
    float viewHeight = mLocationView.getHeight();
    float minZoomFactor = Math.min(viewWidth / subLoc.width, viewHeight / subLoc.height);
    float maxZoomFactor = LocationView.ZOOM_FACTOR_MAX;
    mLocationView.setZoomRange(minZoomFactor, maxZoomFactor);
    mLocationView.setZoomFactor(minZoomFactor);
    Log.d(TAG, String.format(Locale.ENGLISH, "View size: %.1f x %.1f", viewWidth, viewHeight));
    
    mAdjustTime = 0;
    mCurrentSubLocationIndex = index;
    mCurrentFloorLabel.setText(String.format(Locale.ENGLISH, "%d", mCurrentSubLocationIndex + 1));
    
    if (mCurrentSubLocationIndex > 0)
    {
      mPrevFloorButton.setEnabled(true);
      mPrevFloorView.setBackgroundColor(Color.parseColor("#90aaaaaa"));
    }
    else
    {
      mPrevFloorButton.setEnabled(false);
      mPrevFloorView.setBackgroundColor(Color.parseColor("#90dddddd"));
    }
    
    if (mCurrentSubLocationIndex + 1 < mLocation.subLocations.size())
    {
      mNextFloorButton.setEnabled(true);
      mNextFloorView.setBackgroundColor(Color.parseColor("#90aaaaaa"));
    }
    else
    {
      mNextFloorButton.setEnabled(false);
      mNextFloorView.setBackgroundColor(Color.parseColor("#90dddddd"));
    }
    
    cancelVenue();
    mLocationView.redraw();
    return true;
  }
  
  private boolean loadNextSubLocation()
  {
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return false;
    return loadSubLocation(mCurrentSubLocationIndex + 1);
  }
  
  private boolean loadPrevSubLocation()
  {
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return false;
    return loadSubLocation(mCurrentSubLocationIndex - 1);
  }
  
  private void makePin(PointF P)
  {
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return;

    SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
    if (subLoc == null)
      return;

    if (P.x < 0.0f || P.x > subLoc.width ||
        P.y < 0.0f || P.y > subLoc.height)
    {
      // Missing the map
      return;
    }

    if (mTargetPoint != null || mTargetVenue != null)
      return;
    
    if (mDeviceInfo == null || !mDeviceInfo.isValid())
      return;

    mPinPoint = new LocationPoint(mLocation.id, subLoc.id, P.x, P.y);
    mPinPointRect = new RectF();
    mLocationView.redraw();
  }

  private void cancelPin()
  {
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return;

    SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
    if (subLoc == null)
      return;

    if (mTargetPoint != null || mTargetVenue != null || mPinPoint == null)
      return;

    mPinPoint = null;
    mPinPointRect = null;
    mLocationView.redraw();
  }
  
  private void cancelVenue()
  {
    mSelectedVenue = null;
    mLocationView.redraw();
  }
  
  private Venue getVenueAt(float x, float y)
  {
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return null;

    SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
    if (subLoc == null)
      return null;

    Venue v0 = null;
    float d0 = 1000.0f;
        
    for(int i = 0; i < subLoc.venues.size(); ++i)
    {
      Venue v = subLoc.venues.get(i);
      PointF P = mLocationView.getScreenCoordinates(v.x, v.y);
      float d = Math.abs(x - P.x) + Math.abs(y - P.y);
      if (d < 30.0f * mDisplayDensity && d < d0)
      {
        v0 = new Venue(v);
        d0 = d;
      }
    }
    
    return v0;
  }
  
  private Zone getZoneAt(float x, float y)
  {
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return null;

    SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
    if (subLoc == null)
      return null;
    
    PointF P = mLocationView.getAbsCoordinates(x, y);
    LocationPoint LP = new LocationPoint(mLocation.id, subLoc.id, P.x, P.y);
    
    for(int i = 0; i < subLoc.zones.size(); ++i)
    {
      Zone Z = subLoc.zones.get(i);
      if (Z.contains(LP))
        return Z;
    }
    return null;
  }
  
  private void drawPoints(Canvas canvas)
  {
    // Check if location is loaded
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return;

    // Get current sublocation displayed
    SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);

    if (subLoc == null)
      return;

    final int solidColor  = Color.argb(255, 64, 163, 205);  // Light-blue color
    final int circleColor = Color.argb(127, 64, 163, 205);  // Semi-transparent light-blue color
    final int arrowColor  = Color.argb(255, 255, 255, 255); // White color
    final float dp        = mDisplayDensity;
    final float textSize  = 16 * dp;
    
    // Preparing paints
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    paint.setTextSize(textSize);
    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

    // Drawing pin point (if it exists and belongs to the current sublocation)
    if (mPinPoint != null && mPinPoint.subLocation == subLoc.id)
    {
      final PointF T = mLocationView.getScreenCoordinates(mPinPoint);
      final float tRadius = 10 * dp;

      paint.setARGB(255, 0, 0, 0);
      paint.setStrokeWidth(4 * dp);
      canvas.drawLine(T.x, T.y, T.x, T.y - 3 * tRadius, paint);

      paint.setColor(solidColor);
      paint.setStrokeWidth(0);
      canvas.drawCircle(T.x, T.y - 3 * tRadius, tRadius, paint);
      
      final String text = "導航到此處";
      final float textWidth = paint.measureText(text);
      final float h  = 50 * dp;
      final float w  = Math.max(120 * dp, textWidth + h/2);
      final float x0 = T.x;
      final float y0 = T.y - 75 * dp;
      
      mPinPointRect.set(x0 - w/2, y0 - h/2, x0 + w/2, y0 + h/2);
      
      paint.setColor(solidColor);
      canvas.drawRoundRect(mPinPointRect, h/2, h/2, paint);
      
      paint.setARGB(255, 255, 255, 255);
      canvas.drawText(text, x0 - textWidth/2, y0 + textSize/4, paint);
    }
    
    // Drawing target point (if it exists and belongs to the current sublocation)
    if (mTargetPoint != null && mTargetPoint.subLocation == subLoc.id)
    {
      final PointF T = mLocationView.getScreenCoordinates(mTargetPoint);
      final float tRadius = 10 * dp;

      paint.setARGB(255, 0, 0, 0);
      paint.setStrokeWidth(4 * dp);
      canvas.drawLine(T.x, T.y, T.x, T.y - 3 * tRadius, paint);

      paint.setColor(solidColor);
      canvas.drawCircle(T.x, T.y - 3 * tRadius, tRadius, paint);
    }
  }

  private void drawVenues(Canvas canvas)
  {
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return;
    
    SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
    
    final float dp = mDisplayDensity;
    final float textSize  = 16 * dp;
    final float venueSize = 30 * dp;
    final int   venueColor = Color.argb(200, 100, 100, 200); // Venue color
    
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    paint.setStrokeWidth(0);
    paint.setColor(venueColor);
    paint.setTextSize(textSize);
    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    
    for(int i = 0; i < subLoc.venues.size(); ++i)
    {
      Venue v = subLoc.venues.get(i);
      if (v.subLocation != subLoc.id)
        continue;
      
      final PointF P = mLocationView.getScreenCoordinates(v.x, v.y);
      final float x0 = P.x - venueSize/2;
      final float y0 = P.y - venueSize/2;
      final float x1 = P.x + venueSize/2;
      final float y1 = P.y + venueSize/2;
      canvas.drawBitmap(mVenueBitmap, null, new RectF(x0, y0, x1, y1), paint);
    }
    
    if (mSelectedVenue != null)
    {
      final PointF T = mLocationView.getScreenCoordinates(mSelectedVenue.x, mSelectedVenue.y);
      final float textWidth = paint.measureText(mSelectedVenue.name);
      
      final float h  = 50 * dp;
      final float w  = Math.max(120 * dp, textWidth + h/2);
      final float x0 = T.x;
      final float y0 = T.y - 50 * dp;
      mSelectedVenueRect.set(x0 - w/2, y0 - h/2, x0 + w/2, y0 + h/2);
      
      paint.setColor(venueColor);
      canvas.drawRoundRect(mSelectedVenueRect, h/2, h/2, paint);
      
      paint.setARGB(255, 255, 255, 255);
      canvas.drawText(mSelectedVenue.name, x0 - textWidth/2, y0 + textSize/4, paint);
    }
  }
  
  private void drawZones(Canvas canvas)
  {
    // Check if location is loaded
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return;
    
    // Get current sublocation displayed
    SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
    if (subLoc == null)
      return;
    
    // Preparing paints
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    
    for(int i = 0; i < subLoc.zones.size(); ++i)
    {
      Zone Z = subLoc.zones.get(i);
      if (Z.points.size() < 3)
        continue;
      
      boolean selected = (Z == mSelectedZone);
      
      Path path = new Path();
      final LocationPoint P0 = Z.points.get(0);
      final PointF        Q0 = mLocationView.getScreenCoordinates(P0);
      path.moveTo(Q0.x, Q0.y);
      
      for(int j = 0; j < Z.points.size(); ++j)
      {
        final LocationPoint P = Z.points.get((j + 1) % Z.points.size());
        final PointF        Q = mLocationView.getScreenCoordinates(P);
        path.lineTo(Q.x, Q.y);
      }
      
      int zoneColor = Color.parseColor(Z.color);
      int red       = (zoneColor >> 16) & 0xff;
      int green     = (zoneColor >> 8 ) & 0xff;
      int blue      = (zoneColor >> 0 ) & 0xff;
      paint.setColor(Color.argb(selected ? 200 : 100, red, green, blue));
      canvas.drawPath(path, paint);
    }
  }
  
  private void drawDevice(Canvas canvas)
  {
    // Check if location is loaded
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return;
    
    // Check if navigation is available
    if (mDeviceInfo == null || !mDeviceInfo.isValid())
      return;

    // Get current sublocation displayed
    SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);

    if (subLoc == null)
      return;

    final int solidColor  = Color.argb(255, 212,  112, 15); // Light-blue color
    final int circleColor = Color.argb(127, 235 ,  144, 59); // Semi-transparent light-blue color
    final int arrowColor  = Color.argb(255, 255, 255, 255); // White color
    final float dp = mDisplayDensity;
    
    // Preparing paints
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    paint.setStrokeCap(Paint.Cap.ROUND);

    /// Drawing device path (if it exists)
    if (mDeviceInfo.paths != null && mDeviceInfo.paths.size() > 0)
    {
      RoutePath path = mDeviceInfo.paths.get(0);
      if (path.points.size() >= 2)
      {
        paint.setColor(solidColor);

        for(int j = 1; j < path.points.size(); ++j)
        {
          LocationPoint P = path.points.get(j-1);
          LocationPoint Q = path.points.get(j);
          if (P.subLocation == subLoc.id && Q.subLocation == subLoc.id)
          {
            paint.setStrokeWidth(3 * dp);
            PointF P1 = mLocationView.getScreenCoordinates(P);
            PointF Q1 = mLocationView.getScreenCoordinates(Q);
            canvas.drawLine(P1.x, P1.y, Q1.x, Q1.y, paint);
          }
        }
      }
    }
    
    paint.setStrokeCap(Paint.Cap.BUTT);

    // Check if device belongs to the current sublocation
    if (mDeviceInfo.subLocation != subLoc.id)
      return;
//cause---------------------------------------------crash?
    final float x  = mDeviceInfo.x*parameter_RSSI;
    final float y  = mDeviceInfo.y*parameter_RSSI;
    final float r  = mDeviceInfo.r*parameter_RSSI;
    final float angle = mDeviceInfo.azimuth;
    final float sinA = (float)Math.sin(angle);
    final float cosA = (float)Math.cos(angle);
    final float radius  = mLocationView.getScreenLengthX(r);  // External radius: navigation-determined, transparent
    final float radius1 = 25 * dp;                            // Internal radius: fixed, solid

    PointF O = mLocationView.getScreenCoordinates(x, y);
    PointF P = new PointF(O.x - radius1 * sinA * 0.22f, O.y + radius1 * cosA * 0.22f);
    PointF Q = new PointF(O.x + radius1 * sinA * 0.55f, O.y - radius1 * cosA * 0.55f);
    PointF R = new PointF(O.x + radius1 * cosA * 0.44f - radius1 * sinA * 0.55f, O.y + radius1 * sinA * 0.44f + radius1 * cosA * 0.55f);
    PointF S = new PointF(O.x - radius1 * cosA * 0.44f - radius1 * sinA * 0.55f, O.y - radius1 * sinA * 0.44f + radius1 * cosA * 0.55f);

    // Drawing transparent circle
    paint.setStrokeWidth(0);
    paint.setColor(circleColor);
    canvas.drawCircle(O.x, O.y, radius, paint);

    // Drawing solid circle
    paint.setColor(solidColor);
    canvas.drawCircle(O.x, O.y, radius1, paint);

    if (ORIENTATION_ENABLED)
    {
      // Drawing arrow
      paint.setColor(arrowColor);
      Path path = new Path();
      path.moveTo(Q.x, Q.y);
      path.lineTo(R.x, R.y);
      path.lineTo(P.x, P.y);
      path.lineTo(S.x, S.y);
      path.lineTo(Q.x, Q.y);
      canvas.drawPath(path, paint);
    }
  }

  private void adjustDevice()
  {
    // Check if location is loaded
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return;

    // Check if navigation is available
    if (mDeviceInfo == null || !mDeviceInfo.isValid())
      return;

    long timeNow = System.currentTimeMillis();

    // Adjust map, if necessary
    if (timeNow >= mAdjustTime)
    {
      // Firstly, set the correct sublocation
      SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
      if (mDeviceInfo.subLocation != subLoc.id)
      {
        for(int i = 0; i < mLocation.subLocations.size(); ++i)
          if (mLocation.subLocations.get(i).id == mDeviceInfo.subLocation)
            loadSubLocation(i);
      }
      
      // Secondly, adjust device to the center of the screen
      PointF center = mLocationView.getScreenCoordinates(mDeviceInfo.x, mDeviceInfo.y);
      float deltaX  = mLocationView.getWidth()  / 2 - center.x;
      float deltaY  = mLocationView.getHeight() / 2 - center.y;
      mAdjustTime   = timeNow;
      mLocationView.scrollBy(deltaX, deltaY);
    }
  }
  
  private String getLogFile(String extension)
  {
    try
    {
      final String extDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/Navigine.Demo";
      (new File(extDir)).mkdirs();
      if (!(new File(extDir)).exists())
        return null;
      
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(System.currentTimeMillis());
      
      return String.format(Locale.ENGLISH, "%s/%04d%02d%02d_%02d%02d%02d.%s", extDir,
                           calendar.get(Calendar.YEAR),
                           calendar.get(Calendar.MONTH) + 1,
                           calendar.get(Calendar.DAY_OF_MONTH),
                           calendar.get(Calendar.HOUR_OF_DAY),
                           calendar.get(Calendar.MINUTE),
                           calendar.get(Calendar.SECOND),
                           extension);
    }
    catch (Throwable e)
    {
      return null;
    }
  }
}
