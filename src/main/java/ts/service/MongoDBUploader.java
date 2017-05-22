/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.service;

import ts.iot.MqttNode;
import java.util.Properties;
import org.slf4j.LoggerFactory;
import ts.utility.SystemUtility;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;

import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.bson.Document;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author loki.chuang
 */
public class MongoDBUploader extends MqttNode
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MongoDBUploader.class);

    private Properties m_properties = null;
    private final String filePath = "/config/IoT.config";

    private final String dbName = "IoTRepublic";
    private final String colName = "dailydata";
    String userUploadId;

    //private MqttClient mqttClient;
    private MongoCollection<Document> collection;

    public MongoDBUploader()
    {
        super.setNodeName("MongoDB Uploader");
        this.m_properties = SystemUtility.readConfigFile(this.filePath);
    }

    @Override //MqttNode
    public void messageArrived(String topicId, MqttMessage mm) throws Exception
    {
        LOG.info("Data arrived");

        TimeZone tz = TimeZone.getDefault();
        Date gmtTime = new Date(new Date().getTime() + tz.getRawOffset());
        //Date gmtTime = new Date(new Date().getTime());

        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        String payload=new String(mm.getPayload());
        Document doc = new Document().append("deviceId", topicId).
                append("userId", this.userUploadId).
                append("value", Double.valueOf(payload)).
                append("date", gmtTime);
        this.collection.insertOne(doc);
    }

    @Override //MqttNode
    public void Start()
    {
        String brokerIp = this.m_properties.getProperty("brokerIp");
        String brokerPort = this.m_properties.getProperty("brokerPort");
        String userId = this.m_properties.getProperty("userId");
        String userPassword = this.m_properties.getProperty("userPassword");

        this.initMongoDB();
        super.connectToBroker(brokerIp, brokerPort, userId, userPassword);
        this.initSubscribe();    
    }
    
    @Override //MqttNode
    public void Stop()
    {
        super.disconnectFromBroker();
        //this.disconnectFromBroker();
    }

    private void initMongoDB()
    {
        String hostIp = this.m_properties.getProperty("MongoDBAddress");
        String port = this.m_properties.getProperty("MongoDBPort");

        this.userUploadId = this.m_properties.getProperty("MongoDBuserId");
        String userUploadPw = this.m_properties.getProperty("MongoDBuserPw");

        MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(this.userUploadId, "admin", userUploadPw.toCharArray());
        //String mechanism= mongoCredential.getMechanism();
        MongoClient mongoClient = new MongoClient(new ServerAddress(hostIp, Integer.parseInt(port)), Arrays.asList(mongoCredential));
        MongoDatabase mongoDatabase = mongoClient.getDatabase(this.dbName);
        this.collection = mongoDatabase.getCollection(this.colName);
    }

    private void initSubscribe()
    {
        String topics = this.m_properties.getProperty("MongoDBsubscribeTopic");
        if (topics.equals("ALL"))
        {
            super.subscribe("#", 0);
        } else
        {
            LOG.error("subscribe specific topic not implement yet");
        }
    }
}
