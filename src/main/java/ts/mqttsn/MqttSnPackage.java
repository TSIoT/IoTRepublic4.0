/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.mqttsn;

/**
 *
 * @author loki.chuang
 */
public class MqttSnPackage
{
    public enum PackageTyep
    {   
        NOTIMPLIMENT,
        PUBLISH,
        SUBSCRIBE
    }
    
    public class ParseFailedException extends Exception
    {
        public ParseFailedException(String message)
        {
            super(message);
        }
    }
    
    public int length=0;
    public PackageTyep packageType=PackageTyep.NOTIMPLIMENT;
    public int topicId;
    public byte[] payload=new byte[0];
    
    public void parsePackage(byte[] dataArray) throws ParseFailedException
    {
        int len=dataArray[0];        
        if(dataArray.length!=len)
        {            
            throw new ParseFailedException("Data length field not equal to received data length");
        }
        
        
        PackageTyep type=PackageTyep.NOTIMPLIMENT;
        switch (dataArray[1])
        {
            case 0x10:
                type=PackageTyep.PUBLISH;
                break;
            case 0x12:
                type=PackageTyep.SUBSCRIBE;
                break;
            default:
                throw new ParseFailedException("The mqtt message type not impliment yet");                
        }
        
        this.length=len;
        this.packageType=type;
        this.topicId=dataArray[2];
        this.payload=new byte[len-3];
        
        for(int i=0;i<len-3;i++)
        {
            this.payload[i]=dataArray[i+3];
        }        
        
    }
    
    
    
}
