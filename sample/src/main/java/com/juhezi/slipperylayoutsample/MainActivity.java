package com.juhezi.slipperylayoutsample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.juhezi.slipperylayout.SlipperyLayout;

public class MainActivity extends AppCompatActivity {

    private SlipperyLayout mSlTest;
    private Button mBtnOpen;
    private Button mBtnClose;
    private Button mBtnList;
    private RelativeLayout mRlLike;
    private RelativeLayout mRlUnlike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSlTest = (SlipperyLayout) findViewById(R.id.sl_test);
        mBtnOpen = (Button) findViewById(R.id.btn_open);
        mBtnClose = (Button) findViewById(R.id.btn_close);
        mBtnList = (Button) findViewById(R.id.btn_list);
        mBtnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlTest.openMenuView();
            }
        });
        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlTest.closeMenuView();
            }
        });
        mBtnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                startActivity(intent);
            }
        });
        mRlLike = (RelativeLayout) mSlTest.getMenuView().findViewById(R.id.rl_like);
        mRlUnlike = (RelativeLayout) mSlTest.getMenuView().findViewById(R.id.rl_unlike);
        mRlLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "❤", Toast.LENGTH_SHORT).show();
            }
        });
        mRlUnlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "💔", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
