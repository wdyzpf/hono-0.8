/*******************************************************************************
 * Copyright (c) 2016, 2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.hono.auth;

/**
 * Defines permissions that are required to access Hono service resources.
 */
public enum Activity {
    /**
     * Permission required for receiving message from a node.
     */
    READ,
    /**
     * Permission required for sending messages to a node.
     */
    WRITE,
    /**
     * Permission required for executing an operation on a node.
     */
    EXECUTE;

    /**
     * Gets the single character representation for this activity.
     * 
     * @return The first character of this activity's name.
     */
    public char getCode() {
        return name().charAt(0);
    }
}