package com.dhanushkad;

import com.dhanushkad.manager.Constants;
import com.dhanushkad.manager.Manager;
import com.dhanushkad.manager.ManagerConfig;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.*;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {

        while(true) {
            MySecondThread myThread = new MySecondThread();
            myThread.start();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }
}

class MySecondThread extends Thread {

    private static String  ipAddress  = "192.168.43.174";
    private static String  port    = "161";
    private static String  oidValue  = ".1.3.6.1.2.1.1.3.0";
    private static int    snmpVersion  = SnmpConstants.version2c;
    private static String  community  = "public";


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
            pdu.add(new VariableBinding(new OID(oidValue)));
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