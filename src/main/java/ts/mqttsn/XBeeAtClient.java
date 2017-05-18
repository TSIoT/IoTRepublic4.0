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
import ts.utility.SystemUtility;

/**
 *
 * @author loki.chuang
 */
public class XBeeAtClient extends MqttTcpClient
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(XBeeAtClient.class);

    private Properties m_properties = null;
    private final SerialTool serialTool = new SerialTool();
    private SerialReader serialReader;

    private final String defaultConfigFilePath = "/config/XBeeAtClient.config";

    //private CommPort commPort;
    public XBeeAtClient()
    {
        this.m_properties = SystemUtility.readConfigFile(this.defaultConfigFilePath);
    }

    @Override //MqttSnClient
    public void Start()
    {
        LOG.info("XBeeAT Client start");

        String portName = this.m_properties.getProperty("portName");
        String baudRate = this.m_properties.getProperty("baudRate");

        super.Start();
        this.serialTool.openComport(portName, baudRate);
        //this.initXBee();
        this.serialTool.startListerSerial(new SerialReader(this));

    }

    @Override //MqttSnClient
    public void Stop()
    {
        LOG.info("XBeeAT Client stop");
        this.serialTool.stopListerSerial();
        this.serialTool.closeComport();
        super.Stop();
    }

    private boolean initXBee()
    {
        boolean initFinish = false;
        byte[] readBuffer;
        int len = 0;
        try
        {
            Thread.sleep(1500);
            LOG.info("Try to entering XBee command mode");
            this.serialTool.writeSerial("+++".getBytes(), 3);
            //this.out.write("+++".getBytes());
            readBuffer = this.serialTool.readSerial(1500);
            String response = new String(readBuffer).trim();

            if (response.equals("OK"))
            {
                LOG.info("Entered XBee command mode:" + response);
                initFinish = true;
            } else
            {
                LOG.info("Entered XBee command mode failed");
            }

            this.serialTool.writeSerial("ATCN\r".getBytes(), 5);
            //this.out.write("+++".getBytes());
            readBuffer = this.serialTool.readSerial(200);
            response = new String(readBuffer).trim();

            if (response.equals("OK"))
            {
                LOG.info("Exit XBee command mode:" + response);
                initFinish = true;
            } else
            {
                LOG.info("Entered XBee command mode failed");
            }

        } catch (InterruptedException ex)
        {
            LOG.error(ex.toString());
        }

        return initFinish;
    }

    private class SerialReader implements ISerialListener
    {

        List<Byte> recvBuffer = new ArrayList<>();
        private XBeeAtClient xbeeAtClient;
        private int errorCount = 0;
        private final int maxErrorCount = 60; //always equal to the max payload of RF module

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
                for (int i = 0; i < recvData.length; i++)
                {
                    this.recvBuffer.add(recvData[i]);
                }

                if (MqttSnPackage.isValidPackage(recvBuffer))
                {
                    this.errorCount = 0;
                    do
                    {
                        MqttSnPackage pack = new MqttSnPackage();
                        int packLen = (int) this.recvBuffer.get(0);
                        byte[] recvPack = new byte[packLen];
                        for (int i = 0; i < packLen; i++)
                        {
                            recvPack[i] = this.recvBuffer.get(0);
                            this.recvBuffer.remove(0);
                        }

                        pack.parseRecvData(recvPack);
                        String topic = this.xbeeAtClient.findTopicById(pack.topicId);
                        this.xbeeAtClient.publish(topic, pack.payload, 1, true);
                        LOG.info("Publish topic:" + topic + ",payload:" + new String(pack.payload));
                    } while (MqttSnPackage.isValidPackage(recvBuffer));
                } else
                {
                    this.errorCount++;
                }

                if (this.errorCount > this.maxErrorCount)
                {
                    LOG.error("package parsing error more than " + this.maxErrorCount + " times");
                    this.recvBuffer.clear();
                }

                //LOG.debug(new String(pack.payload));
                //LOG.info("Recv data"+new String(recvData));
            } catch (MqttSnPackage.ParseException | NullPointerException ex)
            {
                LOG.error(ex.toString());
            } catch (Exception ex)
            {

                //Logger.getLogger(XBeeAtClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
