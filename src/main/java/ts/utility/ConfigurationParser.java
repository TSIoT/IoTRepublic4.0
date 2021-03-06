/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.utility;

/**
 *
 * @author loki.chuang
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.Properties;

/**
 * Mosquitto configuration parser.
 *
 * A line that at the very first has # is a comment Each line has key value
 * format, where the separator used it the space.
 *
 * @author andrea
 */
public class ConfigurationParser
{

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationParser.class);

    private Properties m_properties = new Properties();

    /**
     * Parse the configuration from file.
     */
    public void parse(File file) throws ParseException
    {
        if (file == null)
        {
            LOG.warn("parsing NULL file, so fallback on default configuration!");
            return;
        }
        if (!file.exists())
        {
            LOG.warn(
                    String.format(
                            "parsing not existing file %s, so fallback on default configuration!",
                            file.getAbsolutePath()));
            return;
        }
        try
        {
            FileReader reader = new FileReader(file);
            parse(reader);
        } catch (FileNotFoundException fex)
        {
            LOG.warn(
                    String.format(
                            "parsing not existing file %s, so fallback on default configuration!",
                            file.getAbsolutePath()),
                    fex);
            return;
        }
    }

    public Properties getProperties()
    {
        return m_properties;
    }

    /**
     * Parse the configuration
     *
     * @throws ParseException if the format is not compliant.
     */
    void parse(Reader reader) throws ParseException
    {
        if (reader == null)
        {
            // just log and return default properties
            LOG.warn("parsing NULL reader, so fallback on default configuration!");
            return;
        }

        BufferedReader br = new BufferedReader(reader);
        String line;
        try
        {
            while ((line = br.readLine()) != null)
            {                
                if(line.isEmpty())
                    continue;
                                
                if (line.charAt(0)!='#')                    
                {                    
                    if (line.matches("^\\s*$"))
                    {
                        // skip it's a black line
                        continue;
                    }

                    // split till the first space
                    int delimiterIdx = line.indexOf('=');
                    String key = line.substring(0, delimiterIdx).trim();
                    String value = line.substring(delimiterIdx + 1).trim();

                    m_properties.put(key, value);
                } 

            }
        } catch (IOException ex)
        {
            throw new ParseException("Failed to read", 1);
        } finally
        {
            try
            {
                reader.close();
            } catch (IOException e)
            {
                // ignore
            }
        }
    }

}
