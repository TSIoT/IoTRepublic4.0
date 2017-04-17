/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.iot;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author loki.chuang
 */
public abstract class MqttNode implements IMqttNode, MqttCallback
{
    private String NodeName="Default NodeName";
    private static final Logger LOG = LoggerFactory.getLogger(MqttNode.class);
    private MqttClient mqttClient = null;

    @Override //IMqttNode
    public abstract void Start();

    @Override //IMqttNode
    public abstract void Stop();

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
    
    public void connectToBroker(String ip, String port, String id, String password)
    {
        boolean isAnonymous;
        MqttConnectOptions mqttOption = new MqttConnectOptions();

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
        }
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

}
