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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.jboss.beach.askpass.Askpass.ENV_JBOSS_ASKPASS;
import static org.jboss.beach.askpass.Askpass.ENV_SSH_ASKPASS;
import static org.jboss.beach.askpass.Askpass.PROP_JBOSS_ASKPASS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class AskpassConfigurationTestCase {
    @Before
    public void before() {
        assertNull(System.getenv(ENV_JBOSS_ASKPASS));
        // not sure if this will fly
        assertNull(System.getenv(ENV_SSH_ASKPASS));
        assertNull(System.getProperty(PROP_JBOSS_ASKPASS));
    }

    @Ignore("requires a restart of the current process with env set or wicked maven surefire config, too lazy to do that now")
    @Test
    public void testJBossAskpass() {
        System.getenv().put(ENV_JBOSS_ASKPASS, "testJBossAskpass");
        try {
            final Askpass askpass = new Askpass();
            final String command = askpass.computeAskpassCommand();
            assertEquals("testJBossAskpass", command);
        } finally {
            System.getenv().remove(ENV_JBOSS_ASKPASS);
        }
    }

    @Test
    public void testNoConfiguration() {
        final Askpass askpass = new Askpass();
        try {
            askpass.computeAskpassCommand();
            fail("Expected AskpassConfigurationError");
        } catch (AskpassConfigurationError e) {
            // good
        }
    }

    @Test
    public void testPropJBossAskpass() {
        System.setProperty(PROP_JBOSS_ASKPASS, "testPropJBossAskpass");
        try {
            final Askpass askpass = new Askpass();
            final String command = askpass.computeAskpassCommand();
            assertEquals("testPropJBossAskpass", command);
        } finally {
            System.clearProperty(PROP_JBOSS_ASKPASS);
        }
    }

    @Ignore("requires a restart of the current process with env set or wicked maven surefire config, too lazy to do that now")
    @Test
    public void testSshAskpass() {
        System.getenv().put(ENV_SSH_ASKPASS, "testSshAskpass");
        try {
            final Askpass askpass = new Askpass();
            final String command = askpass.computeAskpassCommand();
            assertEquals("testSshAskpass", command);
        } finally {
            System.getenv().remove(ENV_SSH_ASKPASS);
        }
    }
}
