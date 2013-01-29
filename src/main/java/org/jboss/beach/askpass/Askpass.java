/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.beach.askpass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Permission;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class Askpass {
    public static final String ENV_JBOSS_ASKPASS = "JBOSS_ASKPASS";
    public static final String ENV_SSH_ASKPASS = "SSH_ASKPASS";
    public static final String PROP_JBOSS_ASKPASS = "jboss.askpass";

    private static final AskpassConfigurationPermission SET_COMMAND_PERMISSION = new AskpassConfigurationPermission("setCommand");

    private static interface ReturnValue {
        static int OK = 0;
        static int CANCELED = 1;
    }

    private String command;

    public final char[] askPassword(final String prompt) throws AskpassException {
        if (prompt == null)
            throw new IllegalArgumentException("prompt is null");
        final String command = computeAskpassCommand();
        final ProcessBuilder builder = new ProcessBuilder(command, prompt);
        // do we want to capture stderr, or just redirect it?
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        try {
            final Process process = builder.start();
            try {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                final String line = reader.readLine();
                if (line != null)
                    return line.toCharArray();
                // this might be tricky as we could wait indefinitely
                final int returnValue = process.waitFor();
                switch(returnValue) {
                    case ReturnValue.OK:
                        throw new RuntimeException("Did not receive a password from the askpass process");
                    case ReturnValue.CANCELED:
                        throw new AskpassCancelledException(prompt);
                    default:
                        throw new AskpassFailedException(prompt);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                process.destroy();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final void checkPermission(final Permission perm) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(perm);
    }

    protected final String computeAskpassCommand() {
        if (command != null)
            return command;
        {
            final String prop = System.getProperty(PROP_JBOSS_ASKPASS);
            if (prop != null)
                return prop;
        }
        {
            final String env = System.getenv(ENV_JBOSS_ASKPASS);
            if (env != null)
                return env;
        }
        {
            final String env = System.getenv(ENV_SSH_ASKPASS);
            if (env != null)
                return env;
        }
        throw new AskpassConfigurationError("No command, nor JBOSS_ASKPASS or SSH_ASKPASS has been set");
    }

    public void setCommand(final String command) {
        checkPermission(SET_COMMAND_PERMISSION);
        this.command = command;
    }
}
