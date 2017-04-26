/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions;

import gnu.io.CommPortIdentifier;
import java.util.Enumeration;
import java.util.List;
/**
 *
 * @author loki.chuang
 */

import ts.utility.SerialTool;

public class TestRxTx
{

    public static void main(String args[])
    {
        SerialTool uart = new SerialTool();
        System.out.println("RXTX TEST!");

        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        while (ports.hasMoreElements())
        {
            CommPortIdentifier cpIdentifier = (CommPortIdentifier) ports.nextElement();
            System.out.println(cpIdentifier.getName());
        }

        //uart.openComport("/dev/ttyS1", "9600");
        //uart.openComport("COM15", "9600");
        uart.openComport("/dev/ttyUSB0", "9600");
        

        while (true)
        {
            List<Byte> readBuffer = uart.readSerial_List(100);
            if (readBuffer.size() > 0)
            {
                readBuffer.forEach((item) ->
                {
                    System.out.println(item);
                });
            }

        }

    }
}
