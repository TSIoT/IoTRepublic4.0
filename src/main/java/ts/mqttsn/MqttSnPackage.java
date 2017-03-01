/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.mqttsn;

import java.nio.charset.StandardCharsets;

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
    
    public final int headerLength=3;
    
    public int packageLength;
    public PackageType type=PackageType.NOT_IMPLEMENT;
    public int topicId;
    public byte[] payload;
    
    
    public void parseRecvData(byte[] recvData) throws ParseException
    {
        int len=recvData[0];
        
        if(len!=recvData.length)
        {
            throw new ParseException("The length field not equal to true length");
        }
        
        switch(recvData[1])
        {
            case 0x10:
                this.type=PackageType.PUBLISH;
                break;
            case 0x12:
                this.type=PackageType.SUBSCRIBE;
                break;
            default:
                throw new ParseException("The package type"+ recvData[1]+" not implement yet");
        }
        
        this.packageLength=len;
        this.topicId=recvData[2];
        
        this.payload=new byte[this.packageLength-this.headerLength];
        
        for(int i=0;i<payload.length;i++)
        {
            this.payload[i]=recvData[i+this.headerLength];
        }        
        //String msg=new String(this.payload);                       
    }
    
}
