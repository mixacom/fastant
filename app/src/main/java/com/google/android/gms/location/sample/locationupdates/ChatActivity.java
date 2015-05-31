package com.google.android.gms.location.sample.locationupdates;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;


public class ChatActivity extends ActionBarActivity {
    protected LineChartView mLineChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mLineChart = (LineChartView) findViewById(R.id.linechart);

        String[] mLabels = {"ANT", "GNU", "OWL", "APE", "COD","YAK", "RAM", "JAY"};
        LineSet data1 = new LineSet();
        LineSet data2 = new LineSet();
        float dataset1[] = {0.2f, 3.4f, 1.2f,4.3f,2.3f,4.3f,2.3f,3.1f };
        float dataset2[] = {0.4f, 1.4f, 2.2f,4.3f,3.3f,4.4f,1.3f,2.1f };
        data1.addPoints(mLabels, dataset1);
        data2.addPoints(mLabels, dataset2);
        //int[] colors1 = {Color.parseColor("#3388c6c3"), Color.TRANSPARENT};
        data1.setLineColor(Color.parseColor("#3388c6c3"));
        //int[] colors2 = {Color.parseColor("#1133c6c3"), Color.TRANSPARENT};
        data2.setLineColor(Color.parseColor("#663313c3"));
        mLineChart.addData(data1);
        mLineChart.addData(data2);
        mLineChart.show();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
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
}
