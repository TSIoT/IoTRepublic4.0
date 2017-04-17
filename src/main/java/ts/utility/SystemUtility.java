/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.utility;

import java.io.File;
import java.text.ParseException;
import java.util.Properties;
import org.slf4j.LoggerFactory;
import ts.service.MongoDBUploader;

/**
 *
 * @author loki.chuang
 */
public class SystemUtility
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SystemUtility.class);
    
    public static Properties readConfigFile(String filePath)
    {
        Properties m_properties=null;
        ConfigurationParser parser = new ConfigurationParser();
        try
        {
            String currentPath = System.getProperty("user.dir");
            File configFile = new File(currentPath + filePath);
            parser.parse(configFile);
            m_properties = parser.getProperties();
        } catch (ParseException ex)
        {
            LOG.error(ex.toString());            
        }
        
        return m_properties;
    }
}
