/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions;

import java.util.logging.Level;
import java.util.logging.Logger;
import ts.mqttsn.IMqttSnClient;
import ts.mqttsn.MqttSnClient;
import ts.mqttsn.XBeeClient;

/**
 *
 * @author loki.chuang
 */
public class MainClass
{

    public static void main(String args[])
    {
        
        try
        {                         
            /*
            MqttSnClient gateway = new MqttSnClient();
            gateway.ClientStart();            
            */
            /*
            for (int i = 0; i < 10; i++)
            {
                gateway.SimplePublish("/hello/world", "This is from java");
                Thread.sleep(1000);
            }
            */    
            /*
            gateway.SimpleSubscribe("/hello/world");
            Thread.sleep(10000);
            gateway.ClientStop();            
            */
            
       
            IMqttSnClient xbeeClient=new XBeeClient();
            xbeeClient.ClientStart();
            //xbeeClient.SimpleSubscribe("/hello/world");
            Thread.sleep(5000);
            xbeeClient.ClientStop();


        } catch (InterruptedException ex)
        {
            Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
