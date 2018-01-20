package com.example.android.networkmonitor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by isaacsahle on 2018-01-18.
 */

public class Ping {
    private static String TAG = Ping.class.getName();
    private static Integer PING_TIME_OUT = 50; //Milliseconds
    private static long KEEP_ALIVE = 500;
    // Available cores is not always the same as the maximum number of cores
    private ThreadPoolExecutor threadExec = new ThreadPoolExecutor(NUMBER_OF_CORES,NUMBER_OF_CORES,KEEP_ALIVE, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());
    static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    static Ping sInstance = new Ping();
    static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            // Do nothing for now
        }

    };

    // Singleton
    private Ping() {}

    public static void startLocalPing(BigInteger ip, BigInteger numHosts){
        sInstance.threadExec.execute(() -> {
            Log.i(TAG,"****Starting thread");
            BigInteger addr;
            for(int i = 0; i < numHosts.intValue(); i++){
                addr = ip.xor(BigInteger.valueOf(i));
                try {
                    InetAddress address = InetAddress.getByName(intToIP(Integer.reverse(addr.intValue())));
                    if(address.isReachable(PING_TIME_OUT)){
                        Log.i(TAG,"Reached: " +  address.getHostName() + ":" + address.getHostAddress());
                    }

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG,"****Done thread");
            }
        );
    }

    public static String intToIP(int ip){
        return String.format(Locale.US,
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));
    }

}
