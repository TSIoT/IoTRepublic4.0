/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ts.iot.node.LoRaNodeTcpClient;
import ts.iot.node.S76sTcpClient;
import ts.iot.service.MongoDBUploader;
import ts.iot.service.ThingsBoardProxy;



import ts.utility.SystemUtility;
import ts.iot.IMqttNode;

public class MainClass
{

    private final String defaultConfigFilePath = "/config/IoT.config";

    public static void main(String args[])
    {
        MainClass mainClass = new MainClass();
        mainClass.MainProcess();
    }

    public void MainProcess()
    {
        List<IMqttNode> nodes = new ArrayList<IMqttNode>();
        Terminate terminteThread = null;
        Properties m_properties = null;
          
        m_properties = SystemUtility.readConfigFile(this.defaultConfigFilePath);

        if (m_properties.getProperty("LoRaNodeClientEnable").equals("YES"))
        {            
            nodes.add(new LoRaNodeTcpClient());         
        }

        if (m_properties.getProperty("MongoDBUploaderEnable").equals("YES"))
        {            
            nodes.add(new MongoDBUploader());         
        }
        
        if (m_properties.getProperty("ThingsBoardEnable").equals("YES"))
        {            
            nodes.add(new ThingsBoardProxy());         
        }
        
        if (m_properties.getProperty("S76sTcpClientEnable").equals("YES"))
        {            
            nodes.add(new S76sTcpClient());         
        }
                
        terminteThread = new Terminate(nodes);

        Runtime.getRuntime().addShutdownHook(terminteThread);
    }
    
    //class Terminate implements Runnable 
    class Terminate extends Thread
    {
        List<IMqttNode> mqttNodes;

        public Terminate(List<IMqttNode> nodes)
        {
            this.mqttNodes = nodes;
            this.mqttNodes.forEach((node) ->
            {
                node.start();                
            });
        }


        @Override
        public void run()
        {
            this.mqttNodes.forEach((node) ->
            {
                node.stop();
            });
        }

    }
}
