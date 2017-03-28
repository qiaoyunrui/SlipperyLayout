package com.juhezi.slipperylayoutsample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.juhezi.slipperylayout.SlipperyLayout;

public class MainActivity extends AppCompatActivity {

    private SlipperyLayout mSlTest;
    private Button mBtnOpen;
    private Button mBtnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSlTest = (SlipperyLayout) findViewById(R.id.sl_test);
        mBtnOpen = (Button) findViewById(R.id.btn_open);
        mBtnClose = (Button) findViewById(R.id.btn_close);
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
    }
}
