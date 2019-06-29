package com.acafela.harmony.util;

import android.content.Context;

import com.acafela.harmony.Config;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class ConfigSetup {
    private static ConfigSetup INSTANCE;
    final static String filename = "configFile";
    static String fileServer;

    public synchronized static ConfigSetup getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ConfigSetup();
        }
        return INSTANCE;
    }

    public String getServerIP(Context context)
    {
        if(fileServer !=null)
            return fileServer;

        try
        {
            InputStream instream = context.openFileInput(filename);
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                fileServer = buffreader.readLine();
                return fileServer;
            }
        }
        catch (Exception e)
        {
           e.printStackTrace();
        }

        return Config.SERVER_IP;
    }

    public void saveServerIP(Context context, String serverip)
    {
        FileOutputStream outputStream;
        //File file = new File(context.getFilesDir(), filename);
        try {
            try {
                InetAddress.getByName(serverip);
                outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(serverip.getBytes());
                //System.out.println("save serverip : " + serverip);
                outputStream.close();
            } catch (Exception i) {
                i.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
