/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import ts.mqttsn.MqttSnClient;
import ts.mqttsn.XBeeAtClient;
/**
 *
 * @author loki.chuang
 */

public class SerialTool
{
    private static final Logger LOG = LoggerFactory.getLogger(MqttSnClient.class);
    
    public static byte[] readSerial(InputStream in ,long timeOut)
    {
        List<Byte> recvArray = new ArrayList<>(1024);
        long startTime = System.currentTimeMillis();
        int temp;
        while (System.currentTimeMillis() - startTime < timeOut)
        {
            try
            {
                temp = in.read();
                if (temp > -1)
                {
                    recvArray.add((byte)temp);
                }
            } catch (IOException ex)
            {
                LOG.error(ex.toString());
                //Logger.getLogger(XBeeAtClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        byte[] array = new byte[recvArray.size()];
        for (int i = 0; i < recvArray.size(); i++)
        {
            array[i] = recvArray.get(i);
        }

        return array;
    }
}
