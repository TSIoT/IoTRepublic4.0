/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.iot;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author loki.chuang
 */
public class MqttService extends MqttNode
{
    private static final Logger LOG = LoggerFactory.getLogger(MqttService.class);
    
    @Override
    public void start()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stop()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override //MqttCallback
    public void messageArrived(String topic, MqttMessage mm) throws Exception
    {
        String msg = new String(mm.getPayload());
        LOG.info(msg);
        //LOG.debug(topic+":"+msg);
    }
    
    
    
    public void initSubsciebe (String[] topics)
    {        
        for (String topic : topics)
        {
            super.subscribe(topic, 1);
        }                
    }
    
    
    
}
