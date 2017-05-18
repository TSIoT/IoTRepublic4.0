/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.mqttsn;

import java.nio.charset.StandardCharsets;
import java.util.*;
import org.slf4j.LoggerFactory;

/**
 *
 * @author loki.chuang
 */
public class MqttSnPackage
{      
    public enum PackageType
    {
        NOT_IMPLEMENT,
        PUBLISH,
        SUBSCRIBE
    }

    public class ParseException extends Exception
    {
        public ParseException(String message)
        {
            super(message);
        }
    }
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MqttSnPackage.class);
    public static final int headerLength = 3;
    
    
    public int packageLength;
    public PackageType type = PackageType.NOT_IMPLEMENT;
    public int topicId;
    public byte[] payload;
        
    public void parseRecvData(byte[] recvData) throws ParseException
    {                
        int len = recvData[0];

        if (len != recvData.length)
        {
            throw new ParseException("The length field not equal to true length");
        }

        switch (recvData[1])
        {
            case 0x10:
                this.type = PackageType.PUBLISH;
                break;
            case 0x12:
                this.type = PackageType.SUBSCRIBE;
                break;
            default:
                throw new ParseException("The package type" + recvData[1] + " not implement yet");
        }

        this.packageLength = len;
        this.topicId = recvData[2];

        this.payload = new byte[this.packageLength - MqttSnPackage.headerLength];

        for (int i = 0; i < payload.length; i++)
        {
            this.payload[i] = recvData[i + MqttSnPackage.headerLength];
        }
        //String msg=new String(this.payload);                       
    }

    public static boolean isValidPackage(List<Byte> recvData)
    {
        boolean isValid = true;
        int len;
        byte type;

        //check the received buffer size is large than header size
        if (recvData.size() > MqttSnPackage.headerLength)
        {
            len = recvData.get(0);
            type = recvData.get(1);

            if (recvData.size()<len)
            {
                isValid = false;
            }             
        }
        else
        {
            isValid=false;
        }

        return isValid;
    }
    
    public static boolean isValidPackage(byte[] recvData)
    {
        boolean isValid = true;
        int len;
        byte type;

        //check the received buffer size is large than header size
        if (recvData.length> MqttSnPackage.headerLength)
        {
            len = recvData[0];
            type = recvData[1];

            if (recvData.length< len)
            {
                isValid = false;
            }             
        }
        else
        {
            isValid=false;
        }

        return isValid;
    }

}
