/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions;
import gnu.io.CommPortIdentifier;
import java.util.Enumeration;
/**
 *
 * @author loki.chuang
 */
public class TestRxTx
{
    public static void main(String args[])
    {
        System.out.println("RXTX TEST!");
        
        Enumeration ports=CommPortIdentifier.getPortIdentifiers();
        while(ports.hasMoreElements())
        {
            CommPortIdentifier cpIdentifier = (CommPortIdentifier)ports.nextElement();
            System.out.println(cpIdentifier.getName());
        }
        
    }
}
