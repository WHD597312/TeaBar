package com.peihou.teabar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jaygoo.widget.RangeSeekBar;

public class TestActivity extends AppCompatActivity {

    RangeSeekBar seekbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
//        seekbar=findViewById(R.id.seekbar);
//        seekbar.setIndicatorTextDecimalFormat("0");
//        seekbar.setValue(20);
    }
}
