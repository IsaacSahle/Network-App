package com.example.android.networkmonitor.utilities;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.net.InetAddress;

import static android.content.ContentValues.TAG;

/**
 * Created by Isaac on 2017-02-14.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class NetworkUtils {


       private NsdManager mNsdManager;
       private ResolveListener mResolveListener;
       private DiscoveryListener mDiscoveryListener;
       private String mServiceName = "";
       private NsdServiceInfo mService;
       private final String SERVICE_TYPE = "_ipp._tcp";


        private void initializeDiscoveryListener() {

            // Instantiate a new DiscoveryListener
            mDiscoveryListener = new DiscoveryListener() {

                //  Called as soon as service discovery begins.
                @Override
                public void onDiscoveryStarted(String regType) {
                    Log.d(TAG, "Service discovery started");
                }

                @Override
                public void onServiceFound(NsdServiceInfo service) {
                    // A service was found!  Do something with it.
                    Log.d(TAG, "Service discovery success" + service);
                }

                @Override
                public void onServiceLost(NsdServiceInfo service) {
                    // When the network service is no longer available.
                    // Internal bookkeeping code goes here.
                    Log.e(TAG, "service lost" + service);
                }

                @Override
                public void onDiscoveryStopped(String serviceType) {
                    Log.i(TAG, "Discovery stopped: " + serviceType);
                }

                @Override
                public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                    Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                    mNsdManager.stopServiceDiscovery(this);
                }

                @Override
                public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                    Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                    mNsdManager.stopServiceDiscovery(this);
                }
            };

        }

    private void initializeResolveListener() {
        mResolveListener = new ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                mService = serviceInfo;
                int port = mService.getPort();
                InetAddress host = mService.getHost();
            }
        };
    }

        public void discoverServices(){
            //stopDiscovery();
            initializeDiscoveryListener();
            initializeResolveListener();
            mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        }



}
