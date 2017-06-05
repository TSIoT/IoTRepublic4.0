/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.iot;

import java.util.Properties;
import java.util.logging.Level;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ts.utility.SystemUtility;

/**
 *
 * @author loki.chuang
 */
public abstract class MqttNode implements IMqttNode, MqttCallback
{
    private String NodeName="Default NodeName";
    private static final Logger LOG = LoggerFactory.getLogger(MqttNode.class);
    private MqttClient mqttClient = null;
     private Properties m_properties = null;
    private final String filePath = "/config/IoT.config";

    
    public MqttNode()
    {
        this.m_properties = SystemUtility.readConfigFile(this.filePath);
    }
    
    @Override //IMqttNode
    public abstract void start();

    @Override //IMqttNode
    public abstract void stop();

    @Override //MqttCallback
    public void connectionLost(Throwable thrwbl)
    {
        //LOG.info("Lost connection from broker:"+this.m_properties.getProperty("brokerIp"));
        LOG.info("Lost connection from broker");
    }

    @Override //MqttCallback
    public void deliveryComplete(IMqttDeliveryToken imdt)
    {

    }

    @Override //MqttCallback
    public void messageArrived(String topic, MqttMessage mm) throws Exception
    {
        String msg = new String(mm.getPayload());
        LOG.info(msg);
        //LOG.debug(topic+":"+msg);
    }

    public void setNodeName(String name)
    {
        this.NodeName=name;
    }
    
    public boolean connectToBrokerUntilSuccess(String ip, String port, String id, String password, int maxTimes)
    {
        boolean succeed=false;
        int counter=0;
        while(!this.connectToBroker(ip, port, id, password) && counter<maxTimes)
        {
            
            counter++;
            LOG.error("Connect to broker failed, try "+String.valueOf(counter)+" times");
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException ex)
            {
                LOG.error(ex.toString());
            }
        }
        
        if(counter<=maxTimes)
            succeed=true;
        
        
        return succeed;
    }
    
    public boolean connectToBroker(String ip, String port, String id, String password)
    {
        boolean isAnonymous;
        MqttConnectOptions mqttOption = new MqttConnectOptions();
        mqttOption.setCleanSession(true);
        mqttOption.setAutomaticReconnect(true);
        
        
        if (id.length() > 0 && password.length() > 0)
        {
            isAnonymous = false;
            mqttOption.setUserName(id);
            mqttOption.setPassword(password.toCharArray());
        } else
        {
            isAnonymous = true;
        }

        try
        {
            //this.mqttClient=new MqttClient("tcp://"+brokerIp+":"brokerPort, this.mqttSnName);
            this.mqttClient = new MqttClient("tcp://" + ip + ":" + port, this.NodeName);
            this.mqttClient.setCallback(this);

            this.mqttClient.connect(mqttOption);

        } catch (MqttException ex)
        {
            LOG.error(ex.toString());       
            return false;
        }
        
        return true;
    }

    public void disconnectFromBroker()
    {
        if (this.mqttClient.isConnected())
        {
            try
            {
                this.mqttClient.disconnect();
            } catch (MqttException ex)
            {
                LOG.error(ex.toString());
                //java.util.logging.Logger.getLogger(MqttSnClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void publish(String topic, byte[] payload, int qos, boolean retained)
    {
        try
        {
            this.mqttClient.publish(topic, payload, qos, retained);
        } catch (MqttException ex)
        {
            LOG.error(ex.toString());
        }
    }

    public void subscribe(String topic, int qos)
    {
        try
        {
            this.mqttClient.subscribe(topic, qos);
        } catch (MqttException ex)
        {
            LOG.error(ex.toString());
            //java.util.logging.Logger.getLogger(MqttSnClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void connectToDefaultBroker()
    {                
        String brokerIp = this.m_properties.getProperty("brokerIp");
        String brokerPort = this.m_properties.getProperty("brokerPort");
        String userId = this.m_properties.getProperty("userId");
        String userPassword = this.m_properties.getProperty("userPassword");
        this.connectToBroker(userId, userId, userId, userPassword);        
    }

}
