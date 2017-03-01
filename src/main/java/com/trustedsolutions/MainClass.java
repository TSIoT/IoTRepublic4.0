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
import ts.mqttsn.XBeeAtClient;


/**
 *
 * @author loki.chuang
 */
public class MainClass
{

    public static void main(String args[])
    {        
        IMqttSnClient xbeeClient=new XBeeAtClient();
        xbeeClient.ClientStart();
        Runtime.getRuntime().addShutdownHook(new Thread() 
        {
            @Override
            public void run() 
            {
                xbeeClient.ClientStop();
            }
        });
        
    }
}
