/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.mqttsn;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.*;
import java.util.logging.Level;
import ts.utility.SerialTool;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;
import ts.utility.ISerialListener;


/**
 *
 * @author loki.chuang
 */
public class XBeeAtClient extends MqttSnClient
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(XBeeAtClient.class);
    
    private Properties m_properties = null;
    
    private final SerialTool serialTool=new SerialTool();
    private SerialReader serialReader;

    private final String defaultConfigFilePath = "/config/XBeeAtClient.config";
    

    //private CommPort commPort;
    public XBeeAtClient()
    {
        this.m_properties = super.readConfigFile(this.defaultConfigFilePath);
    }

    @Override //MqttSnClient
    public void ClientStart()
    {
        LOG.info("XBeeAT Client start");
        String portName=this.m_properties.getProperty("portName");
        String baudRate=this.m_properties.getProperty("baudRate");
        
        super.ClientStart();
        this.serialTool.openComport(portName, baudRate);
        this.serialTool.startListerSerial(new SerialReader(this));
        /*
        this.openComport();

        if (this.isComportOpened && this.initXBee())
        {
            this.serialReader = new SerialReader(this.in, this);
            new Thread(this.serialReader).start();
        }
        */
    }

    @Override //MqttSnClient
    public void ClientStop()
    {
        LOG.info("XBeeAT Client stop");


        super.ClientStop();               
    }
    
    


    private boolean initXBee()
    {
        
        boolean initFinish = false;
        byte[] readBuffer;
        int len = 0;
/*
        try
        {
            Thread.sleep(1000);
            LOG.debug("Try to entering XBee command mode");
            this.out.write("+++".getBytes());
            readBuffer= SerialTool.readSerial(this.in, 1000);
            String response = new String(readBuffer).trim();

            if (response.equals("OK"))
            {
                LOG.debug("Entered XBee command mode:" + response);
                initFinish = true;
            }
        } catch (InterruptedException | IOException ex)
        {
            LOG.error(ex.toString());
        }
        */
        return initFinish;
    }
    
    private class SerialReader implements ISerialListener
    {
        XBeeAtClient xbeeAtClient;        
       
        private volatile boolean runnning = true;

        public SerialReader(XBeeAtClient xbeeAtClient)
        {           
            this.xbeeAtClient = xbeeAtClient;
        }

        @Override
        public void dataReceived(byte[] recvData)
        {
            try
            {
                MqttSnPackage pack=new MqttSnPackage();
                pack.parseRecvData(recvData);
                LOG.debug(new String(pack.payload));
                //LOG.info("Recv data"+new String(recvData));
            } catch (MqttSnPackage.ParseException ex)
            {
                LOG.error(ex.toString());
            }
        }
    }
    

}
