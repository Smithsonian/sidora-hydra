/**
 * Copyright 2017 Smithsonian Institution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.You may obtain a copy of
 * the License at: http://www.apache.org/licenses/
 *
 * This software and accompanying documentation is supplied without
 * warranty of any kind. The copyright holder and the Smithsonian Institution:
 * (1) expressly disclaim any warranties, express or implied, including but not
 * limited to any implied warranties of merchantability, fitness for a
 * particular purpose, title or non-infringement; (2) do not assume any legal
 * liability or responsibility for the accuracy, completeness, or usefulness of
 * the software; (3) do not represent that use of the software would not
 * infringe privately owned rights; (4) do not warrant that the software
 * is error-free or will be maintained, supported, updated or enhanced;
 * (5) will not be liable for any indirect, incidental, consequential special
 * or punitive damages of any kind or nature, including but not limited to lost
 * profits or loss of data, on any basis arising from contract, tort or
 * otherwise, even if any of the parties has been warned of the possibility of
 * such loss or damage.
 *
 * This distribution includes several third-party libraries, each with their own
 * license terms. For a complete copy of all copyright and license terms, including
 * those of third-party libraries, please see the product release notes.
 */
package edu.si.sidora.hydra;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.mina.core.RuntimeIoException;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpGear extends ExternalResource {

    private static final Logger logger = LoggerFactory.getLogger("Test FTP Gear");

    private final int port;
    private final File userPropertiesFile;
    FtpServer server;
    FTPClient client;

    FtpGear(File userPropertiesFile, int port) {
        this.userPropertiesFile = userPropertiesFile;
        this.port = port;
    }

    @Override
    protected void before() throws Throwable {
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(port);
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(userPropertiesFile);
        userManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());
        serverFactory.setUserManager(userManagerFactory.createUserManager());
        serverFactory.setListeners(new HashMap<String, Listener>() {{ 
            put("default", listenerFactory.createListener());
        }});
        server = serverFactory.createServer();
        logger.info("Starting FTP server on port: {}", port);
        server.start();

        client = new FTPClient();
        client.configure(new FTPClientConfig());
        client.connect("localhost", port);
        assertTrue("Failed to connect to FTP server!", FTPReply.isPositiveCompletion(client.getReplyCode()));
        client.login("testUser", "testPassword");
    }

    @Override
    protected void after() {
        try {
            client.quit();
            client.disconnect();
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
        server.stop();
    }
}
