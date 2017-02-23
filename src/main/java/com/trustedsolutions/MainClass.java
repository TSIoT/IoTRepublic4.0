/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions;

import java.util.logging.Level;
import java.util.logging.Logger;
import ts.mqttsn.MqttSnGateway;

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
            MqttSnGateway gateway = new MqttSnGateway();
            gateway.ConnectToBroker();
            /*
            for (int i = 0; i < 10; i++)
            {
                gateway.SimplePublish("/hello/world", "This is from java");
                Thread.sleep(1000);
            }
            */
            gateway.SimpleSubscribe("/hello/world");
            Thread.sleep(10000);
            gateway.DisconnectFromBroker();

        } catch (InterruptedException ex)
        {
            Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
