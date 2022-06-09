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
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ServerTest
{
    private static final long KB = 1024;
    private static final long MB = 1024 * KB;
    private static final long GB = 1024 * MB;

    private static Main main;
    private static URI serverBaseUri;

    @BeforeAll
    public static void initServer() throws Exception
    {
        main = new Main();
        main.createServer(0);
        main.getServer().start();
        serverBaseUri = main.getServer().getURI().resolve("/");

        // now let's create some content
        Path baseDir = main.getBaseResourceDir();
        makeStaticFile(baseDir.resolve("example.png"), 2 * MB);
        makeStaticFile(baseDir.resolve("large.mkv"), 2 * GB);
    }

    private static void makeStaticFile(Path staticFile, long size) throws IOException
    {
        byte[] buf = new byte[(int)MB];
        Arrays.fill(buf, (byte)'x');
        ByteBuffer src = ByteBuffer.wrap(buf);

        if (Files.exists(staticFile) && Files.size(staticFile) == size)
        {
            // all done, nothing left to do.
            System.err.printf("File Exists Already: %s (%,d bytes)%n", staticFile, Files.size(staticFile));
            return;
        }

        System.err.printf("Creating %,d byte file: %s ...%n", size, staticFile);
        try (SeekableByteChannel channel = Files.newByteChannel(staticFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))
        {
            long remaining = size;
            while (remaining > 0)
            {
                ByteBuffer slice = src.slice();
                int len = buf.length;
                if (remaining < Integer.MAX_VALUE)
                {
                    len = Math.min(buf.length, (int)remaining);
                    slice.limit(len);
                }

                channel.write(slice);
                remaining -= len;
            }
        }
        System.err.println(" Done");
    }

    @AfterAll
    public static void stopServer()
    {
        LifeCycle.stop(main.getServer());
    }

    /**
     * Get small file using DefaultServlet
     */
    @Test
    public void testGetSmallDefault() throws Exception
    {
        HttpURLConnection http = (HttpURLConnection)serverBaseUri.resolve("/example.png").toURL().openConnection();
        http.connect();
        dumpRequestResponse(http);
        assertEquals(HttpURLConnection.HTTP_OK, http.getResponseCode());
        String contentLengthResponse = http.getHeaderField("Content-Length");
        assertNotNull(contentLengthResponse);
        long contentLengthLong = Long.parseLong(contentLengthResponse);
        assertEquals(2 * MB, contentLengthLong);
        assertEquals("image/png", http.getHeaderField("Content-Type"));
    }

    /**
     * Get small file using MyFileServlet
     */
    @Test
    public void testGetSmallMyFileServlet() throws Exception
    {
        HttpURLConnection http = (HttpURLConnection)serverBaseUri.resolve("/files/example.png").toURL().openConnection();
        http.connect();
        dumpRequestResponse(http);
        assertEquals(HttpURLConnection.HTTP_OK, http.getResponseCode());
        String contentLengthResponse = http.getHeaderField("Content-Length");
        assertNotNull(contentLengthResponse);
        long contentLengthLong = Long.parseLong(contentLengthResponse);
        assertEquals(2 * MB, contentLengthLong);
        assertEquals("image/png", http.getHeaderField("Content-Type"));
        assertEquals("attachment; filename=\"example.png\"", http.getHeaderField("Content-Disposition"));
    }

    /**
     * Get large file using DefaultServlet
     */
    @Test
    public void testGetLargeDefault() throws Exception
    {
        HttpURLConnection http = (HttpURLConnection)serverBaseUri.resolve("/large.mkv").toURL().openConnection();
        http.connect();
        dumpRequestResponse(http);
        assertEquals(HttpURLConnection.HTTP_OK, http.getResponseCode());
        String contentLengthResponse = http.getHeaderField("Content-Length");
        assertNotNull(contentLengthResponse);
        long contentLengthLong = Long.parseLong(contentLengthResponse);
        assertEquals(2 * GB, contentLengthLong);
        assertNull(http.getHeaderField("Content-Type"), "Not a recognized mime-type by Jetty");
    }

    /**
     * Get large file using MyFileServlet
     */
    @Test
    public void testGetLargeMyFileServlet() throws Exception
    {
        HttpURLConnection http = (HttpURLConnection)serverBaseUri.resolve("/files/large.mkv").toURL().openConnection();
        http.connect();
        dumpRequestResponse(http);
        assertEquals(HttpURLConnection.HTTP_OK, http.getResponseCode());
        String contentLengthResponse = http.getHeaderField("Content-Length");
        assertNotNull(contentLengthResponse);
        long contentLengthLong = Long.parseLong(contentLengthResponse);
        assertEquals(2 * GB, contentLengthLong);
        assertEquals("video/x-matroska", http.getHeaderField("Content-Type"));
        assertEquals("attachment; filename=\"large.mkv\"", http.getHeaderField("Content-Disposition"));
    }

    private static void dumpRequestResponse(HttpURLConnection http)
    {
        System.out.println();
        System.out.println("----");
        System.out.printf("%s %s HTTP/1.1%n", http.getRequestMethod(), http.getURL());
        System.out.println("----");
        System.out.printf("%s%n", http.getHeaderField(null));
        http.getHeaderFields().entrySet().stream()
            .filter(entry -> entry.getKey() != null)
            .forEach((entry) -> System.out.printf("%s: %s%n", entry.getKey(), http.getHeaderField(entry.getKey())));
    }
}
