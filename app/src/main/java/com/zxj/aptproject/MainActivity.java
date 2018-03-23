package com.zxj.aptproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zxj.apt_annotation.Route;

@Route(path = "aaaa")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
