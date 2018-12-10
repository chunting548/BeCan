package com.hlxz.librarybecan;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_search);

        Button buttonbook = (Button) findViewById(R.id.btsearbook);


        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation_bar);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_navigation:
                        Intent intent_navigation = new Intent(SearchActivity.this, MainActivity.class);
                        startActivity(intent_navigation);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        break;
                    case R.id.navigation_search:
                        break;
                    case R.id.navigation_tutorial:
                        Intent intent_tutorial = new Intent(SearchActivity.this, TutorialActivity.class);
                        startActivity(intent_tutorial);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        break;
                    case R.id.navigation_function:
                        Intent intent_function = new Intent(SearchActivity.this, FunctionActivity.class);
                        startActivity(intent_function);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        break;
                    case R.id.navigation_adjust:
                        Intent intent_adjust = new Intent(SearchActivity.this, AdjustActivity.class);
                        startActivity(intent_adjust);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        break;
                }
                return false;
            }
        });
        //實做OnClickListener界面
        buttonbook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //初始化Intent物件
                Intent intent = new Intent();
                //從MainActivity 到Main2Activity
                intent.setClass(SearchActivity.this , booksearch.class);
                //開啟Activity
                startActivity(intent);
            }
        });



        Button buttonarea = (Button)findViewById(R.id.btseararea);

        //實做OnClickListener界面
        buttonarea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //初始化Intent物件
                Intent intent = new Intent();
                //從MainActivity 到Main2Activity
                intent.setClass(SearchActivity.this , areasearch.class);
                //開啟Activity
                startActivity(intent);
            }
        });
    }
}
