/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.mqttsn;

import java.util.Properties;
import org.slf4j.LoggerFactory;
import ts.utility.SerialTool;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author loki.chuang
 */
public class LoRaNodeClient extends MqttSnClient
{

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LoRaNodeClient.class);

    private Properties m_properties = null;
    private final SerialTool serialTool = new SerialTool();

    private HashMap<String, Boolean> slaveAddressMap;
    private final String defaultConfigFilePath = "/config/LoRaNodeClient.config";
    private final int responseOkTimeout = 100;
    private NodePolling nodePolling;

    public LoRaNodeClient()
    {
        this.m_properties = super.readConfigFile(this.defaultConfigFilePath);
        this.slaveAddressMap = new HashMap<>(20);
        //get all slave address in to this.slaveAddressMap
        String slaveNodes = this.m_properties.getProperty("slaveNodes");
        String[] slaveAddresses = slaveNodes.split(",");
        for (String address : slaveAddresses)
        {
            this.slaveAddressMap.put(address, Boolean.FALSE);
        }
    }

    @Override //MqttSnClient
    public void ClientStart()
    {
        super.ClientStart();

        LOG.info("LoRa-Node Client start");

        String portName = this.m_properties.getProperty("portName");
        String baudRate = this.m_properties.getProperty("baudRate");

        this.serialTool.openComport(portName, baudRate);
        this.initLoRaNode();
        this.nodePolling = new NodePolling(this);
        new Thread(this.nodePolling).start();
    }

    @Override //MqttSnClient
    public void ClientStop()
    {
        LOG.info("LoRa-Node Client stop");
        this.nodePolling.StopRunning();
        this.serialTool.closeComport();
        super.ClientStop();
    }

    private void initLoRaNode()
    {
        String cmd;
        cmd = "LoraSystemMode inNormal\r";
        this.sendAtCommand(cmd);
        cmd = "LoraMode MASTER\r";
        this.sendAtCommand(cmd);

        if (this.m_properties.getProperty("needReJoin").equals("YES"))
        {
            this.slaveAddressMap.entrySet().forEach((entry) ->
            {
                boolean joinSuccess = this.joinNode(entry.getKey());
                entry.setValue(joinSuccess);
            });
        }
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

    private boolean joinNode(String nodeAddress)
    {
        String cmd = "LoraJoinNode";
        byte[] result;
        String result_str;
        boolean isSuccess = false;

        cmd = cmd + " " + nodeAddress + "\r";
        this.serialTool.writeSerial(cmd.getBytes(), cmd.length());
        result = this.serialTool.readSerial(3000);
        result_str = new String(result);
        if (result_str.equals("OKJoinOK"))
        {
            LOG.info("Join node:" + nodeAddress + " success");
            isSuccess = true;
        } else
        {
            LOG.info("Join node:" + nodeAddress + " failed");
            isSuccess = false;
        }

        return isSuccess;
    }

    private void sendData(String address, String data)
    {
        String cmd = "LoraNodeData";
        cmd = cmd + " " + address + " " + data + "\r";
        this.serialTool.writeSerial(cmd.getBytes(), cmd.length());
    }

    private class NodePolling implements Runnable
    {

        private LoRaNodeClient loRaNodeClient;
        private int responseTimeout = 0;
        private volatile boolean runnning = true;
        private boolean needReJoin;

        public NodePolling(LoRaNodeClient loRaNodeClient)
        {
            this.loRaNodeClient = loRaNodeClient;
            String responseTimeout_str = this.loRaNodeClient.m_properties.getProperty("responseTimeout");
            this.responseTimeout = Integer.valueOf(responseTimeout_str);
            String needReJoin = this.loRaNodeClient.m_properties.getProperty("needReJoin");
            if (needReJoin.equals("YES"))
            {
                this.needReJoin = true;
            } else if (needReJoin.equals("NO"))
            {
                this.needReJoin = false;
            } else
            {
                LOG.error("propertie: NeedReJoin setting error, set to default NO");
                this.needReJoin = false;
            }
        }

        @Override
        public void run()
        {
            while (this.runnning)
            {
                this.pollingAllNodes();
            }
            LOG.info("Polling node thread safely stoped");
        }

        public void StopRunning()
        {
            this.runnning = false;
        }

        private void pollingAllNodes()
        {
            String pollingWord = "REQ";

            this.loRaNodeClient.slaveAddressMap.entrySet().forEach((entry) ->
            {
                //check if the node is already join success
                if (Objects.equals(entry.getValue(), Boolean.FALSE)
                        && this.needReJoin == true)
                {
                    boolean isSuccess = this.loRaNodeClient.joinNode(entry.getKey());
                    entry.setValue(isSuccess);
                }

                //if node already join success or dont care just polling
                if (Objects.equals(entry.getValue(), Boolean.TRUE) || this.needReJoin == false)
                {
                    this.loRaNodeClient.sendData(entry.getKey(), pollingWord);
                    byte[] result = this.loRaNodeClient.serialTool.readSerial(this.loRaNodeClient.responseOkTimeout);
                    String result_str = new String(result);
                    if (result_str.equals("OK"))
                    {
                        result = this.loRaNodeClient.serialTool.readSerial(this.responseTimeout);
                        LOG.info(entry.getKey() + "OK:" + new String(result));
                        this.mqttPackageTransfer(result);
                    } else
                    {
                        LOG.error("Polling failed: cannot got OK for the sending command:[" + result_str + "]");
                    }
                }
            });
        }

        private void mqttPackageTransfer(byte[] recvBuffer)
        {
            if (MqttSnPackage.isValidPackage(recvBuffer))
            {
                try
                {
                    MqttSnPackage pack = new MqttSnPackage();

                    pack.parseRecvData(recvBuffer);
                    String topic = this.loRaNodeClient.findTopicById(pack.topicId);
                    this.loRaNodeClient.Publish(topic, pack.payload, 1, true);
                    LOG.info("Publish topic:" + topic + ",payload:" + new String(pack.payload));
                } catch (MqttSnPackage.ParseException ex)
                {
                    Logger.getLogger(LoRaNodeClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        //private
    }

}
