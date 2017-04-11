package com.example.android.networkmonitor;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
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
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import static android.content.ContentValues.TAG;

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

    protected class NetworkScan extends AsyncTask<String,Void,String>{

        private String   s_dns1 ;
        private String   s_dns2;
        private String   s_gateway;
        private String   s_ipAddress;
        private String   s_leaseDuration;
        private String   s_netmask;
        private String   s_serverAddress;
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
            wifii = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            //boolean check = wifii.setWifiEnabled(true);
            d = wifii.getDhcpInfo();

            s_dns1 = "DNS 1: " + String.valueOf(d.dns1);
            s_dns2 = "DNS 2: " + String.valueOf(d.dns2);
            s_gateway = "Default Gateway: " + String.valueOf(d.gateway);
            s_ipAddress = "IP Address: " + String.valueOf(d.ipAddress);
            s_leaseDuration = "Lease Time: " + String.valueOf(d.leaseDuration);
            s_netmask = "Subnet Mask: " + String.valueOf(d.netmask);
            s_serverAddress = "Server IP: " + String.valueOf(d.serverAddress);
        }

        @Override
        protected String doInBackground(String... params) {
            //Inefficient: MultiThread
            String connections = "";
            InetAddress host;
            try
            {
                host = InetAddress.getByName(intToIp(d.ipAddress));
                byte[] ip = host.getAddress();

                for(int i = 1; i <= 254; i++)
                {
                    ip[3] = (byte) i;
                    InetAddress address = InetAddress.getByAddress(ip);
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
            return "";
        }

        @Override
        protected void onProgressUpdate(Void... nothing) {
         //will not be used, here for learning purposes
        }

        @Override
        protected void onPostExecute(String results) {
            //mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }

    }


}
