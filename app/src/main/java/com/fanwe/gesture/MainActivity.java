package com.fanwe.gesture;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickTestEventActivity(View view)
    {
        startActivity(new Intent(this, TestEventActivity.class));
    }

    public void onClickTestViewDragHelperActivity(View view)
    {
        startActivity(new Intent(this, TestViewDragHelperActivity.class));
    }
}
