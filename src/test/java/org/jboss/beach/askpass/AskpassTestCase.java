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

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class AskpassTestCase {
    private static final Askpass ASKPASS = new Askpass();

    static String absolutePathToResource(String resource) {
        // something that works on my machine :-)
        return AskpassTestCase.class.getResource(resource).getFile();
    }

    @BeforeClass
    public static void beforeClass() {
        final String command = absolutePathToResource("/askpass.sh");
        ASKPASS.setCommand(command);
    }

    @Test
    public void test1() throws AskpassException {
        // 'Enter passphrase for *' is hard-coded into kwalletaskpass
        final char[] pwd = ASKPASS.askPassword("Enter passphrase for JBoss Askpass test");
        assertArrayEquals(new String("a86271ed0f30b924c3eab37a3a750274  -").toCharArray(), pwd);
    }

    @Test
    public void testNoPrompt() throws AskpassException {
        try {
            ASKPASS.askPassword(null);
            fail("Should have throws IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // good
        }
    }
}
