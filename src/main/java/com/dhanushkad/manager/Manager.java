package com.dhanushkad.manager;

import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Manager {

    private Snmp snmp;
    private ManagerConfig managerConfig;

    public Manager(ManagerConfig managerConfig) {
        this.managerConfig = managerConfig;
    }

    private OctetString getEngineId() {
        OctetString engineId;
        if (managerConfig.getLocalEngineID() == null) {
            engineId = new OctetString(MPv3.createLocalEngineID()).substring(0, 9);
        } else {
            engineId = managerConfig.getLocalEngineID();
        }
        return engineId;
    }

    // granting permission from os
    public void listen() throws IOException {
        TransportMapping transportMapping;
        if (managerConfig.isTCP()) {
            transportMapping = new DefaultTcpTransportMapping();
        } else {
            transportMapping = new DefaultUdpTransportMapping();
        }
        snmp = new Snmp(transportMapping);

        if (managerConfig.getVersion() == Constants.V3) {
            USM usm = new USM(SecurityProtocols.getInstance().addDefaultProtocols(),
                    getEngineId(),
                    managerConfig.getEngineBoot());
            SecurityModels.getInstance().addSecurityModel(usm);
            snmp.getUSM().addUser(managerConfig.getUserName(), managerConfig.getUser());
        }
        snmp.listen();
    }

    // make get request, validate and return map object
    public Map<String, String> getRequestValidateAndReturn() throws Exception {
        // make request
        ResponseEvent event;
        if (managerConfig.getVersion() == SnmpConstants.version3) {
            event = snmp.get(managerConfig.getPdu(), managerConfig.getUserTarget());
        } else {
            event = snmp.get(managerConfig.getPdu(), managerConfig.getCommunityTarget());
        }
        // validation
        if (event != null && event.getResponse() != null) {
            Map<String, String> map = new HashMap<>();
            for (VariableBinding vb : event.getResponse().getVariableBindings()) {
                map.put(vb.getOid().toString(), vb.getVariable().toString());
            }
            return map;
        }
        throw new Exception("response event is null");
    }

    // get map object, make set request and validate
    public void setRequestAndValidate(Map<String, String> map) throws Exception {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            this.managerConfig.getPdu().add(new VariableBinding(new OID(entry.getKey()),
                    new OctetString(entry.getValue())));
        }
        // make get request
        ResponseEvent event;
        if (managerConfig.getVersion() == SnmpConstants.version3) {
            event = snmp.set(managerConfig.getPdu(), managerConfig.getUserTarget());
        } else {
            event = snmp.set(managerConfig.getPdu(), managerConfig.getCommunityTarget());
        }
        // validation
        if (event == null) {
            throw new RuntimeException(" No such target / Invalid authentication / Timeout error ");
        }
        if (event.getResponse() == null) {
            throw new RuntimeException(" No such target / Invalid authentication / Timeout error ",
                    new Throwable("event response is null"));
        }
        if (event.getResponse().getErrorIndex() != Constants.NO_ERROR_INDEX) {
            throw new RuntimeException(" Error " + event.getResponse().getErrorStatusText());
        }
    }

    // clearing variable bindings and close snmp
    public void close() {
        try {
            if (snmp != null) {
                snmp.close();
            }
            if (managerConfig != null) {
                managerConfig.clear();
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception in closing SNMP listening ", e);
        }
    }
}