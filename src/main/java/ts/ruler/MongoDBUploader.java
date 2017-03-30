/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.ruler;


import java.util.Properties;
import org.slf4j.LoggerFactory;
import ts.utility.SystemUtility;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import java.util.Arrays;
import java.util.Map;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

import org.bson.Document;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


/**
 *
 * @author loki.chuang
 */
public class MongoDBUploader implements MqttCallback
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MongoDBUploader.class);

    private Properties m_properties = null;
    private final String filePath = "/config/MongoDB.config";
    
    private final String dbName="IoTRepublic";
    private final String colName="dailydata";
    String userUploadId;
    
    private MqttClient mqttClient;
    private MongoCollection<Document> collection;    

    public MongoDBUploader()
    {
        this.m_properties = SystemUtility.readConfigFile(this.filePath);
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
        LOG.info("Data arrived");   
        
        TimeZone tz = TimeZone.getDefault();
        Date gmtTime = new Date( new Date().getTime() + tz.getRawOffset() );
        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                
        Document doc = new Document().append("deviceId", topicId).
                append("userId", this.userUploadId).
                append("value", new String(mm.getPayload())).
                append("date", gmtTime);
        this.collection.insertOne(doc);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt)
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }

    public void Start()
    {        
        this.initMongoDB();                        
        this.connectToBroker();             
        this.initSubscribe();
    }
    
    private void initMongoDB()
    {
        String hostIp = this.m_properties.getProperty("dbAddress");
        String port = this.m_properties.getProperty("dbPort");

        this.userUploadId = this.m_properties.getProperty("userUploadId");
        String userUploadPw = this.m_properties.getProperty("userUploadPw");
            

        MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(this.userUploadId, "admin",userUploadPw.toCharArray());               
        //String mechanism= mongoCredential.getMechanism();
        MongoClient mongoClient = new MongoClient(new ServerAddress(hostIp, Integer.parseInt(port)),Arrays.asList(mongoCredential));     
        MongoDatabase mongoDatabase = mongoClient.getDatabase(this.dbName);
        this.collection = mongoDatabase.getCollection(this.colName);       
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

}
