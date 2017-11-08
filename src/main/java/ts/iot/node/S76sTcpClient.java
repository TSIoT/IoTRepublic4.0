/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.iot.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import org.slf4j.LoggerFactory;
import ts.utility.SerialTool;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import ts.iot.MqttSnPackage;
import ts.iot.MqttTcpClient;
import ts.utility.SystemUtility;

/**
 *
 * @author loki.chuang
 */
public class S76sTcpClient extends MqttTcpClient
{

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(S76sTcpClient.class);

    private Properties m_properties = null;
    private final SerialTool serialTool = new SerialTool();

    private HashMap<String, Boolean> slaveAddressMap;
    //private final String defaultConfigFilePath = "/config/LoRaNodeClient.config";
    private final String defaultConfigFilePath = "/config/IoT.config";
    private final int responseOkTimeout = 100;
    private NodePolling nodePolling;

    public S76sTcpClient()
    {
        super.setNodeName("S76sTcpClient");
        this.m_properties = SystemUtility.readConfigFile(this.defaultConfigFilePath);
        this.slaveAddressMap = new HashMap<>(20);
        //get all slave address in to this.slaveAddressMap
        String slaveNodes = this.m_properties.getProperty("S76sClientSlaveNodes");
        String[] slaveAddresses = slaveNodes.split(",");
        for (String address : slaveAddresses)
        {
            this.slaveAddressMap.put(address, Boolean.FALSE);
        }
    }

    @Override //MqttSnClient
    public void start()
    {
        super.start();

        LOG.info("LoRa-Node Client start");

        String portName = this.m_properties.getProperty("S76sTcpClientPortName");
        String baudRate = this.m_properties.getProperty("S76sTcpClientBaudRate");

        this.serialTool.openComport(portName, baudRate);
        this.initLoRaNode();
        this.nodePolling = new NodePolling(this);
        new Thread(this.nodePolling).start();
    }

    @Override //MqttSnClient
    public void stop()
    {
        LOG.info("LoRa-Node Client stop");
        this.nodePolling.stopRunning();
        this.serialTool.closeComport();
        super.stop();
    }

    private void initLoRaNode()
    {
        String masterAddress=this.m_properties.getProperty("S76sMasterAddress");
        List<String> cmds = Arrays.asList("LoraStartWork DISABLE", "LoraAutoBoot 0", "SetSystemMode inNormal", "LoraMode MASTER", "LoraSetSF 9" ,"LoraSetMyAddr "+masterAddress);
        cmds.forEach((cmd) ->
        {
            this.sendAtCommand(cmd + "\r");
        });

        if (this.m_properties.getProperty("S76sNeedReJoin").equals("YES"))
        {
            int index = 1;
            for (String address : this.slaveAddressMap.keySet())
            {
                this.joinNode(address, index);
                index++;

            }
        }
        this.sendAtCommand("LoraStartWork ENABLE" + "\r");
    }

    private boolean sendAtCommand(String cmd)
    {
        boolean isSucceed = false;
        byte[] recvBuf;
        this.serialTool.writeSerial(cmd.getBytes(), cmd.length());
        recvBuf = this.serialTool.readSerial(this.responseOkTimeout);
        cmd = cmd.replace("\r", "");
        LOG.info(cmd + ":" + new String(recvBuf));

        return isSucceed;
    }

    private void joinNode(String nodeAddress, int index)
    {
        String addNodeCmd = "LoraAddSlaveNode";
        String setIndexCmd = "LoraSaveSlaveNode";
        String removeCmd = "LoraRemoveSlaveNode";
        String cmd;
        byte[] result;
        String result_str;
        boolean isSuccess = false;

        //remove node first
        cmd = removeCmd + " " + String.valueOf(index) + "\r";
        this.serialTool.writeSerial(cmd.getBytes(), cmd.length());
        result = this.serialTool.readSerial(400);
        result_str = new String(result);
        if (result_str.equals("OK"))
        {
            LOG.info("Remove node:" + String.valueOf(index) + " success");
            isSuccess = true;
        } else
        {
            LOG.info("Remove node:" + String.valueOf(index) + " failed");
            isSuccess = false;
        }

        //add node
        cmd = addNodeCmd + " " + nodeAddress + "\r";
        this.serialTool.writeSerial(cmd.getBytes(), cmd.length());
        result = this.serialTool.readSerial(400);
        result_str = new String(result);
        if (result_str.equals("OK"))
        {
            LOG.info("Add node:" + nodeAddress + " success");
            isSuccess = true;
        } else
        {
            LOG.info("Add node:" + nodeAddress + " failed");
            isSuccess = false;
        }

        //set node index
        cmd = setIndexCmd + " " + String.valueOf(index) + "\r";
        this.serialTool.writeSerial(cmd.getBytes(), cmd.length());
        result = this.serialTool.readSerial(400);
        result_str = new String(result);
        if (result_str.equals("OK"))
        {
            LOG.info("Set node index:" + String.valueOf(index) + " success");
            isSuccess = true;
        } else
        {
            LOG.info("Join node:" + String.valueOf(index) + " failed");
            isSuccess = false;
        }

    }
  

    private class NodePolling implements Runnable
    {
        private S76sTcpClient loRaNodeClient;
        private volatile boolean runnning = true;

        public NodePolling(S76sTcpClient loRaNodeClient)
        {
            this.loRaNodeClient = loRaNodeClient;
        }

        @Override
        public void run()
        {
            List<Byte> responseArray = new ArrayList<Byte>();
            while (this.runnning)
            {
                List<Byte> response = this.loRaNodeClient.serialTool.readSerial_List(1000);
                responseArray.addAll(response);
                if (responseArray.size() > 0)
                {
                    LOG.info(responseArray.toString());
                    this.mqttPackageTransfer(responseArray);
                    
                }

            }
            LOG.info("Polling node thread safely stoped");
        }

        public void stopRunning()
        {
            this.runnning = false;
        }
     

        private void mqttPackageTransfer(List<Byte> recvBuffer)
        {
            if (MqttSnPackage.isValidPackage(recvBuffer))
            {
                try
                {
                    do
                    {
                        MqttSnPackage pack = new MqttSnPackage();
                        int packLen = (int) recvBuffer.get(0);
                        byte[] recvPack = new byte[packLen];
                        for (int i = 0; i < packLen; i++)
                        {
                            recvPack[i] = recvBuffer.get(0);
                            recvBuffer.remove(0);
                        }
                        pack.parseRecvData(recvPack);
                        String topic = this.loRaNodeClient.findTopicById(pack.topicId);
                        LOG.info("Publish topic:" + topic + ",payload:" + new String(pack.payload));
                        this.loRaNodeClient.publish(topic, pack.payload, 1, false);

                    } while (MqttSnPackage.isValidPackage(recvBuffer));

                } catch (MqttSnPackage.ParseException ex)
                {
                    recvBuffer.clear();
                    Logger.getLogger(LoRaNodeTcpClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

}
