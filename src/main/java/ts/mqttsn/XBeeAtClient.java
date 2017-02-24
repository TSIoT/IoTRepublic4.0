/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.mqttsn;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.*;
import ts.utility.SerialTool;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

/**
 *
 * @author loki.chuang
 */
public class XBeeAtClient extends MqttSnClient
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(XBeeAtClient.class);
    
    private Properties m_properties = null;
    private CommPort commPort;
    private InputStream in;
    private OutputStream out;
    private SerialReader serialReader;

    private final String defaultConfigFilePath = "/config/XBeeAtClient.config";
    private boolean isComportOpened = false;

    //private CommPort commPort;
    public XBeeAtClient()
    {
        this.m_properties = super.readConfigFile(this.defaultConfigFilePath);
    }

    @Override //MqttSnClient
    public void ClientStart()
    {
        LOG.info("XBeeAT Client start");
        super.ClientStart();
        this.openComport();

        if (this.isComportOpened && this.initXBee())
        {
            this.serialReader = new SerialReader(this.in, this);
            new Thread(this.serialReader).start();
        }
    }

    @Override //MqttSnClient
    public void ClientStop()
    {
        LOG.info("XBeeAT Client stop");
        //this.comportReadThread.stop();
        if (this.serialReader != null)
        {
            this.serialReader.Close();
        }

        super.ClientStop();
        this.closeComport();
        
    }
    
    private void openComport()
    {
        try
        {
            //read config file for comport
            String portName = this.m_properties.getProperty("portName");
            String baudRate = this.m_properties.getProperty("baudRate");
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

            //open comport
            if (portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                this.commPort = portIdentifier.open(portName, 2000); //2000 is timeout                
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(Integer.parseInt(baudRate), SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                this.in = serialPort.getInputStream();
                this.out = serialPort.getOutputStream();
                LOG.debug(portName+" is opened with baudrate:"+baudRate);
            }
            this.isComportOpened = true;

        } catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | IOException ex)
        {
            LOG.error(ex.toString());
        }
    }

    private void closeComport()
    {
        try
        {
            
            this.in.close();
            this.out.close();
            this.commPort.close();
            this.isComportOpened=false;
        } catch (IOException ex)
        {
            LOG.error(ex.toString());
            //Logger.getLogger(XBeeAtClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private boolean initXBee()
    {
        boolean initFinish = false;
        byte[] readBuffer;
        int len = 0;

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

        return initFinish;
    }
    
    private class SerialReader implements Runnable
    {

        XBeeAtClient xbeeAtClient;
        InputStream in;

        private volatile boolean runnning = true;

        public SerialReader(InputStream in, XBeeAtClient xbeeAtClient)
        {
            this.in = in;
            this.xbeeAtClient = xbeeAtClient;
        }

        @Override
        public void run()
        {
            byte[] buffer = new byte[1024];
            int len = -1;
            try
            {
                LOG.debug("SerialReader started");
                while (this.runnning && (len = this.in.read(buffer)) > -1)
                {
                    //System.out.print(new String(buffer, 0, len));                    
                }
                LOG.debug("SerialReader safely terminated");
                //System.out.println("Loop end");
            } catch (IOException e)
            {
                LOG.error(e.toString());
                //e.printStackTrace();
            }
        }

        public void Close()
        {
            this.runnning = false;
        }
    }
    

}
