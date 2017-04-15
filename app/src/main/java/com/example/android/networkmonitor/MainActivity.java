package com.example.android.networkmonitor;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;


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
            mScanView.setText("");
            new NetworkScan(this).execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected class NetworkScan extends AsyncTask<String,Void,ArrayList<Integer>>{
        private static final int NUM_THREADS = 120;
        private int networkBits;
        private int hostBits;
        private Context mContext;
        TextView info;
        DhcpInfo d;
        WifiManager wifii;

        public NetworkScan(Context c){
            mContext = c;
        }

        @Override
        protected void onPreExecute(){
           super.onPreExecute();
        }

        @Override
        protected ArrayList<Integer> doInBackground(String... params) {
            //determine total range of IP addresses to scan
            wifii = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            d = wifii.getDhcpInfo();
            networkBits = 0;
            hostBits = 0;
            String mask = Integer.toBinaryString((d.netmask & 0xFF)) + Integer.toBinaryString((d.netmask >> 8 ) & 0xFF) + Integer.toBinaryString((d.netmask >> 16) & 0xFF) + Integer.toBinaryString((d.netmask >> 24) & 0xFF);
            int length = mask.length();
            //pad zeros
            while(length < 32){
                mask += "0";
                length++;
            }

            int i = 0;
            while(i < length && mask.charAt(i) != '0'){
                networkBits++;
                i++;
            }

            hostBits = length - networkBits;

            Integer startingAddr = new Integer(d.ipAddress & d.netmask);
            Integer hosts = ((int) Math.pow(2,hostBits)) - 2;
            ArrayList<Integer> list = new ArrayList<Integer>();
            list.add(startingAddr);
            list.add(hosts);
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<Integer> results) {
                int numBytesReversed = Integer.reverseBytes(results.get(0));
                int numHosts = results.get(1);

                int itemsPerThread = (numHosts / NUM_THREADS);
                int remainingItems = (numHosts % NUM_THREADS);
                int start = 1;

                for (int i = 1; i <= NUM_THREADS; i++)
                {
                    int extra = (i <= remainingItems) ? 1:0;
                    int amount = (itemsPerThread + extra);

                    if(amount == 0)
                        break;

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                        new Ping(mContext).executeOnExecutor(THREAD_POOL_EXECUTOR,numBytesReversed,start,amount);
                    }else{
                        new Ping(mContext).execute(numBytesReversed,start,amount);
                    }
                    start += amount;
                }
        }

    }

    protected class Ping extends AsyncTask<Integer,String,Void>{

        private Context mContext;

        public Ping(Context c){
            mContext = c;
        }

        public String intToIp(int i) {
            return (i & 0xFF) + "." +
                    ((i >> 8 ) & 0xFF) + "." +
                    ((i >> 16) & 0xFF) + "." +
                    ((i >> 24) & 0xFF);
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... param){

            for (int i = param[1]; i <= (param[1] + param[2]); i++){
                try{
                    int addr = param[0] ^ i;
                    InetAddress address = InetAddress.getByName(intToIp(Integer.reverseBytes(addr)));
                        if(address.isReachable(200)){
                            publishProgress(address.getHostName() + ":" + address.getHostAddress());
                        }
                }catch (UnknownHostException e1){
                    e1.printStackTrace();
                }catch (IOException e2){
                    e2.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            mScanView.append(progress[0] + "\n\n\n");
        }

        @Override
        protected void onPostExecute(Void results) {}
    }
}