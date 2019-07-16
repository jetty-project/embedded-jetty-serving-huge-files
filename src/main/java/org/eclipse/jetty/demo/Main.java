//
//  ========================================================================
//  Copyright (c) 1995-2019 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Main main = new Main();
        Server server = main.createServer();
        server.start();
        server.join();
    }

    public Server server;
    public Path baseResourcePath;

    public Server createServer() throws IOException
    {
        server = new Server(8888);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.setBaseResource(getBaseResource());

        ServletHolder defHolder = new ServletHolder("default", DefaultServlet.class);
        context.addServlet(defHolder, "/");

        HandlerList handlers = new HandlerList();
        handlers.addHandler(context);
        handlers.addHandler(new DefaultHandler()); // to report (in html) any issues serving request in prior contexts

        server.setHandler(handlers);
        return server;
    }

    /**
     * Get the Resource representing the Base Resource for the Root Context
     */
    private Resource getBaseResource() throws IOException
    {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path workDir = tempDir.resolve("jetty-huge-work");

        if (!Files.exists(workDir))
        {
            Files.createDirectories(workDir);
        }

        baseResourcePath = workDir;
        System.err.println("Using base resource of : " + workDir);

        return new PathResource(workDir);
    }
}
