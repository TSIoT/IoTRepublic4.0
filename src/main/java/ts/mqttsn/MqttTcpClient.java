/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.mqttsn;

import ts.iot.MqttNode;


import java.util.Properties;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import ts.utility.SystemUtility;

/**
 *
 * @author loki.chuang
 */
//public class MqttTcpClient  implements MqttCallback,IMqttNode
public class MqttTcpClient extends MqttNode
{
    //protected String mqttSnName = "Default SN";

    private static final Logger LOG = LoggerFactory.getLogger(MqttTcpClient.class);

    //private final ConfigurationParser parser = new ConfigurationParser();
    private final String defaultConfigFilePath = "/config/IoT.config";
    private final String defaultTopicTablePath = "/config/TopicIdTable.config";

    //private MqttClient mqttClient;
    private Properties m_properties = null;
    private Properties topicProperties = null;

    public MqttTcpClient()
    {
        //this.m_properties = this.readConfigFile(this.defaultConfigFilePath);
        this.m_properties = SystemUtility.readConfigFile(this.defaultConfigFilePath);
        this.topicProperties = SystemUtility.readConfigFile(this.defaultTopicTablePath);
        //this.initGatewayFromConfigFile();
    }

    @Override //MqttNode
    public void Start()
    {
        String brokerIp = this.m_properties.getProperty("brokerIp");
        String brokerPort = this.m_properties.getProperty("brokerPort");
        String userId = this.m_properties.getProperty("userId");
        String userPassword = this.m_properties.getProperty("userPassword");
        
        while(!super.connectToBroker(brokerIp, brokerPort, userId, userPassword))
        {
            try
            {
                LOG.info("Connect failed, reconnet now...");
                Thread.sleep(1000);
            } catch (InterruptedException ex)
            {
                java.util.logging.Logger.getLogger(MqttTcpClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }     
    }

    @Override //MqttNode
    public void Stop()
    {
        super.disconnectFromBroker();        
    }

    protected final String findTopicById(int id) throws NullPointerException
    {
        String topic;
        topic = this.topicProperties.getProperty(String.valueOf(id));
        if (topic == null)
        {
            throw new NullPointerException("Cannot found TopicID:" + id);           
        }
        return topic;
    }
/*
    protected final Properties readConfigFile(String filePath)
    {
        ConfigurationParser parser = new ConfigurationParser();
        Properties properties = null;
        try
        {
            String currentPath = System.getProperty("user.dir");
            File configFile = new File(currentPath + filePath);
            parser.parse(configFile);
            properties = parser.getProperties();
        } catch (ParseException ex)
        {
            LOG.error(ex.toString());
            //java.util.logging.Logger.getLogger(MqttTcpClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return properties;
    }
    */
}
