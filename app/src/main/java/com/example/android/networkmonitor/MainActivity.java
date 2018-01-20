package com.example.android.networkmonitor;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import java.math.BigDecimal;
import java.math.BigInteger;


public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getName();
    private static boolean DISABLE = false;
    private static boolean ENABLE = true;
    private TextView mScanView;
    private DhcpInfo dhcp;
    private WifiManager wifi;
    private boolean isWifiEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScanView = (TextView) findViewById(R.id.scan_results);
        retrieveWifiManger();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            // Check wifi status
            retrieveWifiManger();

            if(isWifiEnabled){
                item.setEnabled(DISABLE);
                mScanView.setText("Scanning ...");
                localNetworkScan();
                // TODO: Refactor into a callback Ping is done
                item.setEnabled(ENABLE);
            }else{
                Toast.makeText(this,R.string.wifi_disabled,Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void retrieveWifiManger(){
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        isWifiEnabled = (wifi != null && wifi.isWifiEnabled());
    }

    private void localNetworkScan(){
        dhcp = wifi.getDhcpInfo();
        if(dhcp == null){
            Log.e(TAG,"Can't retrieve dhcp info");
            Toast.makeText(this,R.string.error_message,Toast.LENGTH_SHORT).show();
            mScanView.setText(R.string.begin_scanning);
            return;
        }

        Integer hostBits = 32 - numberOfSetBits(dhcp.netmask);
        BigInteger startIp = BigInteger.valueOf(dhcp.ipAddress & dhcp.netmask);
        BigInteger numHosts = (new BigDecimal((Math.pow(2,hostBits)) - 2)).toBigInteger();
        BigInteger groupSize = numHosts.divide(BigInteger.valueOf(Ping.NUMBER_OF_CORES));
        for (int i = 0; i < Ping.NUMBER_OF_CORES;i++) {
            startIp = startIp.xor(BigInteger.valueOf(i).multiply(groupSize));
            Ping.startLocalPing(startIp,groupSize);
        }

    }

    protected int numberOfSetBits(int i) {
        i = i - ((i >>> 1) & 0x55555555);
        i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);
        return (((i + (i >>> 4)) & 0x0F0F0F0F) * 0x01010101) >>> 24;
    }
}