/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.mqttsn;

import java.io.File;
import java.text.ParseException;
import java.util.Properties;
import java.util.logging.Level;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import ts.utility.ConfigurationParser;

/**
 *
 * @author loki.chuang
 */
public class MqttSnGateway implements MqttCallback
{
    protected String mqttSnName = "Default SN";

    private static final Logger LOG = LoggerFactory.getLogger(MqttSnGateway.class);

    private final ConfigurationParser parser = new ConfigurationParser();
    private final String defaultConfigFilePath = "/config/MqttsnGateway.config";

    private MqttClient mqttClient;

    private Properties m_properties = null;

    public MqttSnGateway()
    {
        boolean isDebugEnable=LOG.isDebugEnabled();
        this.initGatewayFromConfigFile();
    }

    @Override
    public void connectionLost(Throwable thrwbl)
    {
        LOG.info("Lost connection from broker:"+this.m_properties.getProperty("brokerIp"));
    }
    
    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt)
    {
        
    }
    
    @Override
    public void messageArrived(String topic, MqttMessage mm) throws Exception
    {
        String msg=new String(mm.getPayload());
        LOG.debug(topic+":"+msg);
    }

    public void ConnectToBroker()
    {
        boolean isAnonymous;
        MqttConnectOptions mqttOption = new MqttConnectOptions();
        

        String brokerIp = this.m_properties.getProperty("brokerIp");
        String brokerPort = this.m_properties.getProperty("brokerPort");
        String userId = this.m_properties.getProperty("userId");
        String userPassword = this.m_properties.getProperty("userPassword");

        if (userId.length() > 0 && userPassword.length() > 0)
        {
            isAnonymous = false;
            mqttOption.setUserName(userId);
            mqttOption.setPassword(userPassword.toCharArray());
        }
        else
        {
            isAnonymous = true;
        }

        try
        {
            //this.mqttClient=new MqttClient("tcp://"+brokerIp+":"brokerPort, this.mqttSnName);
            this.mqttClient = new MqttClient("tcp://" + brokerIp + ":" + brokerPort, "Test");           
            this.mqttClient.setCallback(this);
            
            
            
            this.mqttClient.connect(mqttOption);
            
        } catch (MqttException ex)
        {
            LOG.error(ex.toString());
        }

    }
    
    public void DisconnectFromBroker()
    {
        if(this.mqttClient.isConnected())
        {
            try
            {
                this.mqttClient.disconnect();
            } catch (MqttException ex)
            {
                java.util.logging.Logger.getLogger(MqttSnGateway.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //with QOS=2, retained=false
    public void SimplePublish(String topic, String message) 
    {        
        try
        {                        
            this.mqttClient.publish(topic,message.getBytes(),2,false );
        } catch (MqttException ex)
        {
            LOG.error(ex.toString());            
        }
    }
    
    public void SimpleSubscribe(String topic)
    {
        try
        {
            this.mqttClient.subscribe(topic);
        } catch (MqttException ex)
        {
            LOG.error(ex.toString());
        }
    }

    private void initGatewayFromConfigFile()
    {
        try
        {
            String currentPath = System.getProperty("user.dir");
            File configFile = new File(currentPath + defaultConfigFilePath);
            this.parser.parse(configFile);
            this.m_properties = this.parser.getProperties();
        } catch (ParseException ex)
        {
            java.util.logging.Logger.getLogger(MqttSnGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
