package com.fanwe.gesture;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class TestViewDragActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_view_drag_helper);
    }

    public void onClickButton(View view)
    {
        Toast.makeText(this, "click", Toast.LENGTH_SHORT).show();
    }
}
