package com.dhanushkad.manager;

import org.snmp4j.mp.SnmpConstants;

public class Constants {
    private Constants() {
    }

    public static final String DEFAULT_AGENT_PORT = "161";
    public static final String DEFAULT_RETRIES = "5";
    public static final String DEFAULT_TIMEOUT = "1500";
    public static final String DEFAULT_TRANSPORT_PROTOCOL = "udp";
    public static final int V1  = SnmpConstants.version1;
    public static final int V2C = SnmpConstants.version2c;
    public static final int V3  = SnmpConstants.version3;
    public static final int NO_ERROR_INDEX = 0;

    public static final String HOST = "host";
    public static final String VERSION = "version";
    public static final String REQUEST_INTERVAL = "request.interval";
    public static final String COMMUNITY = "community";
    public static final String OIDS = "oids";

    public static final String ifInOctects = "1.3.6.1.2.1.2.2.1.10";
    public static final String ifOutOctects = "1.3.6.1.2.1.2.2.1.16";
    public static final String ifOutDiscards = "1.3.6.1.2.1.2.2.1.19";
    public static final String ifInUcast = "1.3.6.1.2.1.2.2.1.11";
    public static final String ifInNUcastPkts = "1.3.6.1.2.1.2.2.1.12";
}
