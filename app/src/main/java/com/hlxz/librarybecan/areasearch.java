package com.hlxz.librarybecan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

public class areasearch extends AppCompatActivity {

    ListView lv;
    SearchView sv;
    String[] area_name={"1F / 流通櫃台","1F / 文思診療室","1F / 資訊檢索區","2F / 學習促進區","2F / 資訊檢索區","2F / 音樂欣賞室","4F / 學習促進區","4F / 個人座位區","5F / 學習促進區","5F / 個人座位區","6F / 學習促進區","6F / 個人座位區","7F / 學習促進區","8F / 討論室"};
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_areasearch);


        sv = (SearchView) findViewById(R.id.searchView);
        lv = (ListView) findViewById(R.id.listView);


        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, area_name);
        lv.setAdapter(adapter);

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String text) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {

                adapter.getFilter().filter(text);
                return false;
            }
        });



        Button buttonbook = (Button) findViewById(R.id.btasearbook);

        //實做OnClickListener界面
        buttonbook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //初始化Intent物件
                Intent intent = new Intent();
                //從MainActivity 到Main2Activity
                intent.setClass(areasearch.this , booksearch.class);
                //開啟Activity
                startActivity(intent);
            }
        });
    }
}
