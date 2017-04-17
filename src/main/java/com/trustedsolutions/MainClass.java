/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import ts.mqttsn.MqttSnClient;
import ts.mqttsn.XBeeAtClient;
import ts.mqttsn.LoRaNodeClient;
import ts.service.MongoDBUploader;
import ts.mqttsn.MqttSnClient;

import ts.utility.SystemUtility;


/**
 *
 * @author loki.chuang
 */
import ts.iot.IMqttNode;/**
 *
 * @author loki.chuang
 */
public class MainClass
{
    private final String defaultConfigFilePath = "/config/IoT.config";        
    
    public static void main(String args[])
    {
        MainClass mainClass=new MainClass();
        mainClass.MainProcess();             
    }
    
    public void MainProcess()
    {
        Terminate terminteThread=null;
        Properties m_properties = null;
        IMqttNode loraNodeClient = null;
        MongoDBUploader mongoUploader = null;                

        m_properties = SystemUtility.readConfigFile(this.defaultConfigFilePath);
        if (m_properties.getProperty("LoRaNodeClientEnable").equals("YES"))
        {
            loraNodeClient = new LoRaNodeClient();
            loraNodeClient.Start();
        }

        if (m_properties.getProperty("MongoDBUploaderEnable").equals("YES"))
        {
            mongoUploader = new MongoDBUploader();
            mongoUploader.Start();
        }
        
        terminteThread=new Terminate(loraNodeClient,mongoUploader);
        
        Runtime.getRuntime().addShutdownHook(terminteThread);
    }
    
    
     //class Terminate implements Runnable 
    class Terminate extends Thread
     {
        IMqttNode loraNodeClient = null;
        MongoDBUploader mongoUploader = null;
        
        public Terminate(IMqttNode loraNodeClient,MongoDBUploader mongoUploader)
        {
            this.loraNodeClient=loraNodeClient;
            this.mongoUploader=mongoUploader;
        }
        
        @Override
        public void run()
        {
            if(this.mongoUploader!=null)
                this.mongoUploader.Stop();
            
            if(this.loraNodeClient!=null)
                this.loraNodeClient.Stop();
        }
         
     }
}
