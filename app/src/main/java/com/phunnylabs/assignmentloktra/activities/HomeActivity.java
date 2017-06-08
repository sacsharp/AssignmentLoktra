package com.phunnylabs.assignmentloktra.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.phunnylabs.assignmentloktra.R;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button buttonGithub = (Button) findViewById(R.id.buttonGithub);
        Button buttonLocation = (Button) findViewById(R.id.buttonLocation);

        buttonGithub.setOnClickListener(this);
        buttonLocation.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonGithub:
                Intent intentGithubTask = new Intent(this, GithubActivity.class);
                startActivity(intentGithubTask);
                break;

            case R.id.buttonLocation:
                Intent intentLocationTask = new Intent(this, MapsActivity.class);
                startActivity(intentLocationTask);
                break;
        }
    }
}
