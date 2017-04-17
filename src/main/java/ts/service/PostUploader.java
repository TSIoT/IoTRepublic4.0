/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.LoggerFactory;
import ts.mqttsn.MqttSnClient;

import ts.utility.ConfigurationParser;

/**
 *
 * @author loki.chuang
 */
public class PostUploader implements MqttCallback
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PostUploader.class);

    public String pageUrl = "";

    private Properties m_properties = null;
    private MqttClient mqttClient;

    private String filePath = "/config/UploadDataBase.config";

    public PostUploader()
    {
        this.readConfigFile();
    }

    public void Start()
    {
        this.connectToBroker();
        this.initSubscribe();
    }

    public void Stop()
    {
        this.disconnectFromBroker();
    }

    @Override
    public void connectionLost(Throwable thrwbl)
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        LOG.error("Connection lost from broker");
    }

    @Override
    public void messageArrived(String topicId, MqttMessage mm) throws Exception
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        LOG.info("Data arrived");
        this.postData(topicId, new String(mm.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt)
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }

    private void readConfigFile()
    {
        ConfigurationParser parser = new ConfigurationParser();
        try
        {
            String currentPath = System.getProperty("user.dir");
            File configFile = new File(currentPath + filePath);
            parser.parse(configFile);
            this.m_properties = parser.getProperties();
        } catch (ParseException ex)
        {
            LOG.error(ex.toString());
            //java.util.logging.Logger.getLogger(MqttSnClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void connectToBroker()
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
        } else
        {
            isAnonymous = true;
        }

        try
        {
            //this.mqttClient=new MqttClient("tcp://"+brokerIp+":"brokerPort, this.mqttSnName);
            this.mqttClient = new MqttClient("tcp://" + brokerIp + ":" + brokerPort, "PostUploader");
            this.mqttClient.setCallback(this);

            this.mqttClient.connect(mqttOption);

        } catch (MqttException ex)
        {
            LOG.error(ex.toString());
        }
    }

    private void disconnectFromBroker()
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

    private void initSubscribe()
    {
        String topics = this.m_properties.getProperty("subscribeTopic");
        if(topics.equals("ALL"))
        {
            try
            {
                this.mqttClient.subscribe("#", 0);
            } catch (MqttException ex)
            {
                LOG.error(ex.toString());
                //Logger.getLogger(PostUploader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            LOG.error("subscribe specific topic not implement yet");
        }
        
    }

    private void postData(String id,String value)
    {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(this.m_properties.getProperty("url"));
        try
        {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("deviceId", this.topicIdMapping(id)));
            nameValuePairs.add(new BasicNameValuePair("value", value));
            nameValuePairs.add(new BasicNameValuePair("loginId", this.m_properties.getProperty("userUploadId")));
            nameValuePairs.add(new BasicNameValuePair("password", this.m_properties.getProperty("userUploadPw")));

            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null)
            {
                System.out.println(line);
            }

        } catch (IOException e)
        {
            LOG.error(e.toString());
            //e.printStackTrace();
        }
    }
    
    private String topicIdMapping(String topicId)
    {
        String deviceId="arduino.grove.dl01.lux";
        
        return deviceId;
    }

}
