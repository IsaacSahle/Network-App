package com.example.android.networkmonitor;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

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
            NetworkScan myScan = new NetworkScan();
            myScan.mContext = this;
            myScan.execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected class NetworkScan extends AsyncTask<String,Void,ArrayList<Integer>>{

        private int networkBits;
        private int hostBits;
        private Context mContext;
        TextView info;
        DhcpInfo d;
        WifiManager wifii;

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
        protected ArrayList<Integer> doInBackground(String... params) {
            //Inefficient: MultiThread
            //check if device is connected to wifi..
            //determine total range of IP addresses to scan
            wifii = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            //boolean check = wifii.setWifiEnabled(true);
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

            String connections = "";
            InetAddress host, networkAddress;
            try
            {
                host = InetAddress.getByName(intToIp(d.ipAddress));
                //perform logical and with subnet mask to determine first subnet address
                int startingAddr = d.ipAddress & d.netmask;
                int numBytesReversed = Integer.reverseBytes(startingAddr);
                String ipc = intToIp(startingAddr);
                String ipx = intToIp(numBytesReversed);
                networkAddress = InetAddress.getByName(intToIp(startingAddr));
                byte ip [] = host.getAddress();
                byte firstSubnetAddress [] = networkAddress.getAddress();

                //number of hosts minus two reserved IP
                int hosts = ((int) Math.pow(2,hostBits)) - 2;
                int reversed;
                for (i = 1; i <= hosts;i++){
                    int addr = numBytesReversed ^ i;
                    //String temp = intToIp(addr);
                    InetAddress address = InetAddress.getByName(intToIp(Integer.reverseBytes(addr)));

                    if(address.isReachable(500))
                    {
                        System.out.println(address + " machine is turned on and can be pinged");
                        connections+= address+"\n";
                    }
                    else if(!address.getHostAddress().equals(address.getHostName()))
                    {
                        System.out.println(address + " machine is known in a DNS lookup");
                    }

                }

            }
            catch(UnknownHostException e1)
            {
                e1.printStackTrace();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            System.out.println(connections);
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... nothing) {
         //will not be used, here for learning purposes
        }

        @Override
        protected void onPostExecute(ArrayList<Integer> results) {
            //mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            //new Ping().execute();
        }

    }



    protected class Ping extends AsyncTask<Void,Void,Void>{


        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params){
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... params) {
            //will not be used, here for learning purposes
        }

        @Override
        protected void onPostExecute(Void param) {
            //mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }

    }


}
