package com.example.android.networkmonitor;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.networkmonitor.utilities.NetworkUtils;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends AppCompatActivity {
    //Scan text view
    private TextView mScanView;
    private NsdManager mDiscoveryListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScanView = (TextView) findViewById(R.id.scan_results);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        if (itemThatWasClickedId == R.id.action_search) {
            //do something when clicked
            mScanView.setVisibility(View.INVISIBLE);
            NetworkUtils scan = new NetworkUtils();
            scan.discoverServices();
            //new NetworkScan().execute("");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected class NetworkScan extends AsyncTask<String,Void,String>{

        @Override
        protected void onPreExecute(){
           super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return "";
        }

        @Override
        protected void onPostExecute(String githubSearchResults) {

        }

    }


}
