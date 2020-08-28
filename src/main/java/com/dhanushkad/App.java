package com.dhanushkad;

import com.dhanushkad.manager.Constants;
import com.dhanushkad.manager.Manager;
import com.dhanushkad.manager.ManagerConfig;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Hello world!
 *
 */
public class App {

    private static String  ipAddress  = "127.0.0.1";//"192.168.43.174";
    private static String  port    = "161";
    private static String  runTime  = ".1.3.6.1.2.1.1.3.0";
    private static String  ifInOctect  = ".1.3.6.1.2.1.2.2.1.10";
    private static int    snmpVersion  = SnmpConstants.version2c;
    private static String  community  = "public";

    public static void main( String[] args ) throws IOException {
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("public"));
            target.setAddress(GenericAddress.parse("udp:127.0.0.1/161")); // supply your own IP and port
            target.setRetries(2);
            target.setTimeout(1500);
            target.setVersion(SnmpConstants.version2c);

            Map<String, String> result = new HashMap<>();
            try {
                //result = doWalk(Constants.ifOutOctects, target );
                result.putAll(RequestGetResponseAndWriteOnTheText(Constants.ifInNUcastPkts, result));
                result.putAll(doWalk(Constants.ifInOctects, target, result));
            } catch (Exception e) {
                e.printStackTrace();
            }

            FileWriter writer = new FileWriter("MyFile.txt", true);
            for(Map.Entry<String, String> entry : result.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                writer.write(key+" "+value+"\r\n");
            }

            writer.write("\r\n");
            writer.close();
    }

    public static Map<String, String> doWalk(String tableOid, Target target,Map<String, String> result) throws IOException {
        TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
        List<TreeEvent> events = treeUtils.getSubtree(target, new OID(tableOid));
        if (events == null || events.size() == 0) {
            System.out.println("Error: Unable to read table...");
            return result;
        }

        for (TreeEvent event : events) {
            if (event == null) {
                continue;
            }
            if (event.isError()) {
                System.out.println("Error: table OID [" + tableOid + "] " + event.getErrorMessage());
                continue;
            }

            VariableBinding[] varBindings = event.getVariableBindings();
            if (varBindings == null || varBindings.length == 0) {
                continue;
            }
            for (VariableBinding varBinding : varBindings) {
                if (varBinding == null) {
                    continue;
                }

                result.put("." + varBinding.getOid().toString(), varBinding.getVariable().toString());
            }

        }
        snmp.close();

        return result;
    }

    public static Map<String, String> RequestGetResponseAndWriteOnTheText(String oid, Map<String, String> result) {
        TransportMapping transport = null;
        try {
            transport = new DefaultUdpTransportMapping();
            transport.listen();
            CommunityTarget comtarget = new CommunityTarget();
            comtarget.setCommunity(new OctetString(community));
            comtarget.setVersion(snmpVersion);
            comtarget.setAddress(new UdpAddress(ipAddress + "/" + port));
            comtarget.setRetries(2);
            comtarget.setTimeout(1000);

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));
            pdu.setType(PDU.GET);
            pdu.setRequestID(new Integer32(1));
            Snmp snmp = new Snmp(transport);

            System.out.println("Sending Request to Agent...");
            ResponseEvent response = snmp.get(pdu, comtarget);

            // Process Agent Response
            if (response != null) {
                System.out.println("Got Response from Agent");
                PDU responsePDU = response.getResponse();
                if (responsePDU != null) {
                    int errorStatus = responsePDU.getErrorStatus();
                    int errorIndex = responsePDU.getErrorIndex();
                    String errorStatusText = responsePDU.getErrorStatusText();

                    if (errorStatus == PDU.noError) {
                        System.out.println("Snmp Get Response = " + responsePDU.getVariableBindings());
                        result.put(oid, responsePDU.getVariableBindings().get(0).getVariable().toString());
                    }
                    else {
                        System.out.println("Error: Request Failed");
                        System.out.println("Error Status = " + errorStatus);
                        System.out.println("Error Index = " + errorIndex);
                        System.out.println("Error Status Text = " + errorStatusText);
                    }
                }
                else {
                    System.out.println("Error: Response PDU is null");
                }
            }
            else {
                System.out.println("Error: Agent Timeout... ");
            }
            snmp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}

class MySecondThread extends Thread {

    private static String  ipAddress  = "127.0.0.1";//"192.168.43.174";
    private static String  port    = "161";
    private static String  runTime  = ".1.3.6.1.2.1.1.3.0";
    private static String  ifInOctect  = "1.3.6.1.2.1.2.2.1.10";
    private static int    snmpVersion  = SnmpConstants.version2c;
    private static String  community  = "public";
    private static String ramStatus = ".1.3.6.1.4.1.2021.4";

   //1.3.6.1.2.1.2.2.1.10 (ifInOctets) - SNMP OID

    public void run() {
        TransportMapping transport = null;
        try {
            transport = new DefaultUdpTransportMapping();
            transport.listen();
            CommunityTarget comtarget = new CommunityTarget();
            comtarget.setCommunity(new OctetString(community));
            comtarget.setVersion(snmpVersion);
            comtarget.setAddress(new UdpAddress(ipAddress + "/" + port));
            comtarget.setRetries(2);
            comtarget.setTimeout(1000);

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.25.2.3.1.4")));
            pdu.setType(PDU.GET);
            pdu.setRequestID(new Integer32(1));
            Snmp snmp = new Snmp(transport);

            System.out.println("Sending Request to Agent...");
            ResponseEvent response = snmp.get(pdu, comtarget);

            // Process Agent Response
            if (response != null) {
                System.out.println("Got Response from Agent");
                PDU responsePDU = response.getResponse();
                if (responsePDU != null) {
                    int errorStatus = responsePDU.getErrorStatus();
                    int errorIndex = responsePDU.getErrorIndex();
                    String errorStatusText = responsePDU.getErrorStatusText();

                    if (errorStatus == PDU.noError) {
                        System.out.println("Snmp Get Response = " + responsePDU.getVariableBindings());
                    }
                    else {
                        System.out.println("Error: Request Failed");
                        System.out.println("Error Status = " + errorStatus);
                        System.out.println("Error Index = " + errorIndex);
                        System.out.println("Error Status Text = " + errorStatusText);
                    }
                }
                else {
                    System.out.println("Error: Response PDU is null");
                }
            }
            else {
                System.out.println("Error: Agent Timeout... ");
            }
            snmp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class MyThread extends Thread {
    public void run() {
        ManagerConfig managerConfig = new ManagerConfig();
        managerConfig.setCommunityTarget("192.168.1.101", "161", "public", 1, 1500);
        List VBList = new ArrayList();
        VBList.add(new VariableBinding(new OID("1.3.6.1.2.1.1.1.0")));
        managerConfig.setVariableBindings(VBList);
        managerConfig.isTcp(false);
        managerConfig.setVersion(Constants.V2C);
        Manager manager = new Manager(managerConfig);
        try {
            Map map = manager.getRequestValidateAndReturn();
            System.out.println(map.get("1.3.6.1.2.1.1.1.0"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}