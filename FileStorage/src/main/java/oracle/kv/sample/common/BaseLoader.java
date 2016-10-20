/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2016 Oracle and/or its affiliates.  All rights reserved.
 *
 *  Oracle NoSQL Database is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation, version 3.
 *
 *  Oracle NoSQL Database is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License in the LICENSE file along with Oracle NoSQL Database.  If not,
 *  see <http://www.gnu.org/licenses/>.
 *
 *  An active Oracle commercial licensing agreement for this product
 *  supercedes this license.
 *
 *  For more information please contact:
 *
 *  Vice President Legal, Development
 *  Oracle America, Inc.
 *  5OP-10
 *  500 Oracle Parkway
 *  Redwood Shores, CA 94065
 *
 *  or
 *
 *  berkeleydb-info_us@oracle.com
 *
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  EOF
 *
 */
package oracle.kv.sample.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import oracle.kv.FaultException;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.table.Table;
import oracle.kv.table.TableAPI;

/**
 * Base Class which does all the initialization activities
 * 
 * @author vsettipa
 * 
 */
public abstract class BaseLoader {
    
    protected String inputPathStr;
    protected String fileId;
    protected boolean load;
    
    protected static String storeName = "istore";
    protected static String hostName = "slcao401.us.oracle.com:15000,slcao399.us.oracle.com:15000,slcao398.us.oracle.com:15000";
    protected static String hostPort = "";
    protected static String tableName = "fileinfo";
    
    protected static KVStore kvStore;
    protected static TableAPI tableh;
    protected static Table table;
    protected static String delimiter = ",";
    
    protected static Properties prop = new Properties();
    
    /**
     * Parses command line args and opens the KVStore.
     */
    protected BaseLoader(String[] args) {
	
	final int nArgs = args.length;
	int argc = 0;
	
	if (nArgs < 1) {
	    usage("");
	}
	
	try {
	    ClassLoader classLoader = getClass().getClassLoader();
	    File file = new File(
		    classLoader.getResource("config.properties").getFile());
	    FileInputStream in = new FileInputStream(file);
	    prop.load(in);
	    System.out.println("properties :" + prop.toString());
	} catch (IOException e) {
	    e.printStackTrace();
	}
	// load all properties
	storeName = prop.getProperty("storeName");
	hostName = prop.getProperty("storeconfig");
	// hostPort = prop.getProperty("hostPort");
	tableName = prop.getProperty("tableName");
	
	while (argc < nArgs) {
	    final String thisArg = args[argc++];
	    if (thisArg.equals("-i")) {
		if (argc < nArgs) {
		    inputPathStr = args[argc++];
		} else {
		    usage("-i requires an argument.");
		}
	    } else if (thisArg.equals("-g")) {
		if (argc < nArgs) {
		    fileId = args[argc++];
		} else {
		    usage("-g requires an argument");
		}
	    } else if (thisArg.equals("-u")) {
		usage("");
	    } else {
		usage("'" + thisArg + "' is not an expected argument.");
	    }
	}
	
	// initialize objects
	init();
    }
    
    /**
     * Initialize KVStore and get Table Handle
     */
    private void init() {
	
	kvStore = getKVConnection();
	if (!tableName.equals("")) {
	    getTableHandle();
	}
	if (validate()) {
	    System.out.println("Validation was successful.");
	} else {
	    System.out.println("Validation failed.");
	    System.exit(1);
	}
    }
    
    /**
     * Present the usage on valid arguments
     * 
     * @param message
     */
    protected void usage(String message) {
	System.out.println("\n" + message + "\n");
	System.out.println("Usage: FileLoader");
	System.out
		.println("\t-i The Path to the location where PDF files reside"
			+ "\n\t-g <row id> of the PDF file to be fetched"
			+ "\n\t-u (Shows the Usage)");
	System.exit(0);
    }
    
    /**
     * utulity method to return host name and port number
     * 
     * @return
     */
    public static String[] getHostPort() {
	String[] hosts = hostName.split(",");
	return hosts;
    }
    
    /**
     * utility method to return the table handle
     */
    public void getTableHandle() {
	if (tableName == null || tableName.equals("")) {
	    System.exit(0);
	    throw new RuntimeException("Table Name cannot be empty");
	} else {
	    try {
		table = tableh.getTable(tableName);
		if (table == null) {
		    throw new RuntimeException("Table has not been created");
		}
	    } catch (FaultException e) {
		e.printStackTrace();
		throw new RuntimeException("Table has not been created");
	    }
	}
    }
    
    /**
     * utility method to get a connection to a KVStore
     * 
     * @return
     */
    private static KVStore getKVConnection() {
	if (kvStore == null) {
	    kvStore = KVStoreFactory
		    .getStore(new KVStoreConfig(storeName, getHostPort()));
	}
	if (tableh == null) {
	    tableh = kvStore.getTableAPI();
	}
	
	return kvStore;
    }
    
    public void logError(String errMsg) {
	System.out.println("ERROR: " + errMsg);
    }
    
    public abstract boolean validate();
}
