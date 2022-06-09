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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyFileServlet extends HttpServlet
{
    private Path baseDir;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        String baseDirStr = config.getInitParameter("baseDir");
        if (baseDirStr == null)
            throw new ServletException("'baseDir' init-param not specified");

        baseDir = Paths.get(baseDirStr);
        if (!Files.exists(baseDir))
            throw new ServletException("'baseDir' does not exist: " + baseDir);

        if (!Files.isDirectory(baseDir))
            throw new ServletException("'baseDir' is not a directory: " + baseDir);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String filePath = request.getPathInfo();

        // strip leading slash (if present)
        while (filePath.startsWith("/"))
        {
            filePath = filePath.substring(1);
        }

        Path file = baseDir.resolve(filePath);
        response.setContentLengthLong(Files.size(file));
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getFileName().toString() + "\"");
        response.setContentType(Files.probeContentType(file));
        response.flushBuffer();

        final int bufferSize = response.getBufferSize();
        try (InputStream in = Files.newInputStream(file))
        {
            ServletOutputStream out = response.getOutputStream();

            byte[] bytes = new byte[bufferSize];
            int bytesRead;

            while ((bytesRead = in.read(bytes)) != -1)
            {
                out.write(bytes, 0, bytesRead);
            }
        }
    }
}
