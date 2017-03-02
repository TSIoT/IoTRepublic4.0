/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.utility;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.logging.Level;

import ts.mqttsn.MqttSnClient;
import ts.mqttsn.XBeeAtClient;

/**
 *
 * @author loki.chuang
 */
public class SerialTool
{

    private static final Logger LOG = LoggerFactory.getLogger(SerialTool.class);

    private CommPort commPort;
    private InputStream in;
    private OutputStream out;
    private boolean isOpened = false;

    public void openComport(String portName, String baudRate)
    {
        try
        {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

            //open comport
            if (portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                this.commPort = portIdentifier.open(portName, 2000); //2000 is timeout                
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(Integer.parseInt(baudRate), SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                this.in = serialPort.getInputStream();
                this.out = serialPort.getOutputStream();
                LOG.debug(portName + " is opened with baudrate:" + baudRate);
            }
            this.isOpened = true;

        } catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | IOException ex)
        {
            LOG.error(ex.toString());
        }
    }

    public void closeComport()
    {
        try
        {
            this.in.close();
            this.out.close();
            this.commPort.close();
            this.isOpened = false;
        } catch (IOException ex)
        {
            LOG.error(ex.toString());
            //Logger.getLogger(XBeeAtClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public byte[] readSerial(long timeOut)
    {
        List<Byte> recvArray = new ArrayList<>(1024);
        long startTime = System.currentTimeMillis();
        int temp;
        while (System.currentTimeMillis() - startTime < timeOut)
        {
            try
            {
                temp = this.in.read();
                if (temp > -1)
                {
                    recvArray.add((byte) temp);
                }
            } catch (IOException ex)
            {
                LOG.error(ex.toString());
                //Logger.getLogger(XBeeAtClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        byte[] array = new byte[recvArray.size()];
        for (int i = 0; i < recvArray.size(); i++)
        {
            array[i] = recvArray.get(i);
        }

        return array;
    }

    public void startListerSerial(ISerialListener listener)
    {
        try
        {
            SerialPort serialPort = (SerialPort) this.commPort;
            serialPort.addEventListener(new SerialListener(in,listener));
            serialPort.notifyOnDataAvailable(true);
        } catch (TooManyListenersException ex)
        {
            LOG.error(ex.toString());
            //java.util.logging.Logger.getLogger(SerialTool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void stopListerSerial()
    {
        SerialPort serialPort = (SerialPort) this.commPort;
        serialPort.notifyOnDataAvailable(false);
        serialPort.removeEventListener();                
    }

    private class SerialListener implements SerialPortEventListener
    {
        private ISerialListener listener;
        private InputStream in;

        private volatile boolean running = true;

        public SerialListener(InputStream in, ISerialListener listener)
        {
            this.in = in;
            this.listener = listener;
        }

        public void StopListener()
        {

        }

        @Override
        public void serialEvent(SerialPortEvent spe)
        {
            List<Byte> recvArray = new ArrayList<>(1024);
            long startTime = System.currentTimeMillis();
            int data;
            try
            {
                int len = 0;
                while ((data = in.read()) > -1)
                {
                    recvArray.add((byte) data);
                }
                //System.out.print(new String(buffer, 0, len));
            } catch (IOException ex)
            {
                LOG.error(ex.toString());
            }

            byte[] array = new byte[recvArray.size()];
            for (int i = 0; i < recvArray.size(); i++)
            {
                array[i] = recvArray.get(i);
            }
            this.listener.dataReceived(array);

        }
    }

}
