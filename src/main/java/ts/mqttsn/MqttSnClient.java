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
public class MqttSnClient implements MqttCallback,IMqttSnClient
{
    protected String mqttSnName = "Default SN";

    private static final Logger LOG = LoggerFactory.getLogger(MqttSnClient.class);

    //private final ConfigurationParser parser = new ConfigurationParser();
    private final String defaultConfigFilePath = "/config/MqttsnGateway.config";
    private final String defaultTopicTablePath = "/config/TopicIdTable.config";

    private MqttClient mqttClient;

    private Properties m_properties = null;
    private Properties topicProperties = null;

    public MqttSnClient()
    {        
        this.m_properties=this.readConfigFile(this.defaultConfigFilePath);
        this.topicProperties=this.readConfigFile(this.defaultTopicTablePath);
        //this.initGatewayFromConfigFile();
    }

    @Override //MqttCallback
    public void connectionLost(Throwable thrwbl)
    {
        LOG.info("Lost connection from broker:"+this.m_properties.getProperty("brokerIp"));
    }
    
    @Override //MqttCallback
    public void deliveryComplete(IMqttDeliveryToken imdt)
    {
        
    }
    
    @Override //MqttCallback
    public void messageArrived(String topic, MqttMessage mm) throws Exception
    {
        String msg=new String(mm.getPayload());
        LOG.info(msg);
        //LOG.debug(topic+":"+msg);
    }
    
    @Override //IMqttSnClient
    public void ClientStart()
    {
        this.ConnectToBroker();
    }
        
    @Override //IMqttSnClient
    public void ClientStop()
    {
        this.DisconnectFromBroker();
    }
    
    @Override //IMqttSnClient
    public void Publish(String topic, byte[] payload,int qos, boolean retained)
    {
        try
        {
            this.mqttClient.publish(topic, payload,qos, retained);
        } catch (MqttException ex)
        {
            LOG.error(ex.toString());
        }                
    }
    
    @Override //IMqttSnClient
    public void Subscribe(String topic, int qos)
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
                LOG.error(ex.toString());
                //java.util.logging.Logger.getLogger(MqttSnClient.class.getName()).log(Level.SEVERE, null, ex);
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
    
    protected final String findTopicById(int id) throws NullPointerException
    {
        String topic;
        topic=this.topicProperties.getProperty(String.valueOf(id));
        if(topic==null)
        {
            throw new NullPointerException("Cannot found TopicID:"+id);
            //LOG.error("Cannot found TopicID:"+id);
            //throw new NullPointerException();
        }
        return topic;
    }
    
    protected final Properties readConfigFile(String filePath)
    {
        ConfigurationParser parser = new ConfigurationParser();
        Properties properties=null;
        try
        {
            String currentPath = System.getProperty("user.dir");
            File configFile = new File(currentPath +filePath);
            parser.parse(configFile);
            properties = parser.getProperties();
        } catch (ParseException ex)
        {
            LOG.error(ex.toString());
            //java.util.logging.Logger.getLogger(MqttSnClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return properties;
    }

    

}
