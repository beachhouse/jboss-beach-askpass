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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.DomainCombiner;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

import static org.junit.Assert.fail;

/**
 * User code might be able to obtain a reference to a singleton Askpass instance. In which case we do not want the
 * user code to be able to configure the Askpass instance.
 *
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class AskpassSecurityTestCase {
    private SecurityManager previousSM;

    private static <T> T[] array(T... a) {
        return a;
    }

    @After
    public void after() {
        System.setSecurityManager(previousSM);
        previousSM = null;
    }

    @Before
    public void before() {
        this.previousSM = System.getSecurityManager();
        System.setSecurityManager(new SecurityManager());
    }

    @Test
    public void testNoConfigurationPermission() {
        try {
            final Askpass askpass = new Askpass();
            askpass.setCommand("poof");
            fail("Expected SecurityException");
        } catch (SecurityException e) {
            // good
        }
    }

    @Test
    public void testWithConfigurationPermission() {
        // the test-classes codebase (without principal) does not have permission, so we need to leave that one behind
        final AccessControlContext context = new AccessControlContext(array(Askpass.class.getProtectionDomain()));
        final AccessControlContext context1 = new AccessControlContext(context, new DomainCombiner() {
            @Override
            public ProtectionDomain[] combine(ProtectionDomain[] currentDomains, ProtectionDomain[] assignedDomains) {
                return assignedDomains;
            }
        });

        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                final Subject subject = new Subject();
                // any simple principal will do, just make sure java.policy is updated accordingly
                subject.getPrincipals().add(new JMXPrincipal("test"));
                subject.setReadOnly(); // superfluous

                Subject.doAs(subject, new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        // from this point on we have test-classes codebase with a principal protection domain
                        try {
                            final Askpass askpass = new Askpass();
                            askpass.setCommand("poof");
                        } catch (SecurityException e) {
                            fail("Caught " + e);
                        }
                        return null;
                    }
                });
                return null;
            }
        }, context1);
    }
}
