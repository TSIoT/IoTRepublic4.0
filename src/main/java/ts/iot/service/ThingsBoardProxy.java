/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.iot.service;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;
import org.slf4j.LoggerFactory;
import ts.iot.MqttService;
import ts.iot.node.LoRaNodeTcpClient;
import ts.utility.SystemUtility;
import ts.iot.MqttNode;

/**
 *
 * @author loki.chuang
 */
public class ThingsBoardProxy extends MqttService
{

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ThingsBoardProxy.class);
    private Properties m_properties = null;
    private final String defaultConfigFilePath = "/config/IoT.config";
    private MqttClient mqttClient = null;
    
    private String telemetryTopic = "v1/devices/me/telemetry";

    public ThingsBoardProxy()
    {
        super.setNodeName("ThingsBoard Proxy");
        this.m_properties = SystemUtility.readConfigFile(this.defaultConfigFilePath);
    }

    @Override //MqttService
    public void start()
    {
        super.connectToDefaultBroker();
        this.initThingsBoardProxy();
        //this.initThingsBoardProxy();
/*
        super.connectToBroker(thingsBoardBrokerIp, thingsBoardBrokerPort, token, "");
        String[] topics = thingsBoardSubscribeTopic.split(",");
        super.initSubsciebe(topics);
         */
    }

    @Override //MqttService
    public void stop()
    {
        super.disconnectFromBroker();
    }

    @Override //MqttService (MqttCallback)
    public void messageArrived(String topic, MqttMessage mm) throws Exception
    {
        String msg = new String(mm.getPayload());
        
        JSONObject obj = new JSONObject();
        obj.put(topic, Double.valueOf(msg));
                
        this.mqttClient.publish(telemetryTopic, obj.toJSONString().getBytes(),1,false);
        LOG.info(obj.toJSONString());
        //LOG.debug(topic+":"+msg);
    }

    private void initThingsBoardProxy()
    {
        try
        {
            MqttConnectOptions mqttOption = new MqttConnectOptions();            
            
            String thingsBoardBrokerIp = this.m_properties.getProperty("ThingsBoardBrokerIp");
            String thingsBoardBrokerPort = this.m_properties.getProperty("ThingsBoardBrokerPort");
            String token = this.m_properties.getProperty("token");
            String thingsBoardSubscribeTopic = this.m_properties.getProperty("ThingsBoardSubscribeTopic");
            
            mqttOption.setCleanSession(true);
            mqttOption.setAutomaticReconnect(true);
            mqttOption.setUserName(token);            
            
            String[] subscribeTopics = thingsBoardSubscribeTopic.split(",");
            this.mqttClient = new MqttClient("tcp://" + thingsBoardBrokerIp + ":" + thingsBoardBrokerPort, "thingsBoardProxy");
            this.mqttClient.connect(mqttOption);
            
            super.initSubsciebe(subscribeTopics);
            
            //super.connectToBroker(thingsBoardBrokerIp, thingsBoardBrokerPort, token, "");
        } catch (MqttException ex)
        {
            LOG.error(ex.toString());
        }
    }

}
