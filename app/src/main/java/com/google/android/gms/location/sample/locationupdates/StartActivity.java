package com.google.android.gms.location.sample.locationupdates;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class StartActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void enterMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        EditText heightText = (EditText) findViewById(R.id.heightText);
        EditText weightText = (EditText) findViewById(R.id.weightText);
        EditText inviteText = (EditText) findViewById(R.id.inviteText);
        EditText timeText = (EditText) findViewById(R.id.timeText);
        EditText distanceText = (EditText) findViewById(R.id.distanceText);
        String height = heightText.getText().toString();
        String weight = weightText.getText().toString();
        String invite = inviteText.getText().toString();
        String time = timeText.getText().toString();
        String distance = distanceText.getText().toString();
        Bundle extras = new Bundle();
        extras.putString("EXTRA_HEIGHT", height);
        extras.putString("EXTRA_WEIGHT", weight);
        extras.putString("EXTRA_INVITE", invite);
        extras.putString("EXTRA_TIME", time);
        extras.putString("EXTRA_DISTANCE", distance);
        intent.putExtras(extras);
        startActivity(intent);
    }
}
