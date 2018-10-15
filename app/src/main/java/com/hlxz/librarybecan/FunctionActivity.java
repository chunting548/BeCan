package com.hlxz.librarybecan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class FunctionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_function);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation_bar);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_navigation:
                        Intent intent_navigation = new Intent(FunctionActivity.this, MainActivity.class);
                        startActivity(intent_navigation);
                        break;
                    case R.id.navigation_tutorial:
                        Intent intent_tutorial = new Intent(FunctionActivity.this, TutorialActivity.class);
                        startActivity(intent_tutorial);
                        break;
                    case R.id.navigation_function:
                        break;
                    case R.id.navigation_adjust:
                        Intent intent_adjust = new Intent(FunctionActivity.this, AdjustActivity.class);
                        startActivity(intent_adjust);
                        break;
                }
                return false;
            }
        });
    }
    public void library(View view){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://webpac.lib.pu.edu.tw/webpac/webpacIndex.jsp"));
        startActivity(browserIntent);
    }
    public void signup(View view){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://activity.pu.edu.tw/main.php"));
        startActivity(browserIntent);
    }
}


