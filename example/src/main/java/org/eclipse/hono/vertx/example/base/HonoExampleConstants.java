/*******************************************************************************
 * Copyright (c) 2016, 2018 Contributors to the Eclipse Foundation
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

package org.eclipse.hono.vertx.example.base;

/**
 * Class defines where to reach Hono's microservices that need to be accessed for sending and consuming data.
 * This is intentionally done as pure Java constants to provide an example with minimal dependencies (no Spring is
 * used e.g.).
 *
 * Please adopt the values to your needs - the defaults serve for a typical docker swarm setup.
 */
public class HonoExampleConstants {
    /**
     * The default host name to assume for interacting with Hono.
     */
    public static final String HONO_CONTAINER_HOST = "127.0.0.1";
    /**
     * The name or IP address of the host to connect to for consuming messages.
     */
    public static final String HONO_AMQP_CONSUMER_HOST = System.getProperty("consumer.host", HONO_CONTAINER_HOST);
    /**
     * Port of the AMQP network where consumers can receive data (in the standard setup this is the port of the qdrouter).
     */
    public static final int HONO_AMQP_CONSUMER_PORT = Integer.parseInt(System.getProperty("consumer.port", "15671"));

    public static final String HONO_REGISTRY_HOST = HONO_CONTAINER_HOST;
    /**
     * Port of Hono's device registry microservice (used to register and enable devices).
     */
    public static final int HONO_REGISTRY_PORT = 25671;

    public static final String HONO_MESSAGING_HOST = HONO_CONTAINER_HOST;
    /**
     * Port of Hono's messaging microservice (used for sending data).
     */
    public static final int HONO_MESSAGING_PORT = 5671;

    public static final String TENANT_ID = "DEFAULT_TENANT";

    /**
     * Id of the device that is used inside these examples.
     * NB: you need to register the device before data can be sent.
     * E.g. like
     *    {@code http POST http://192.168.99.100:28080/registration/DEFAULT_TENANT device-id=4711}.
     * Please refer to Hono's "Getting started" guide for details.
     */
    public static final String DEVICE_ID = "4711"; // needs to be registered first

    /**
     * For devices signalling that they remain connected for an indeterminate amount of time, a command is periodically sent to the device after the following number of seconds elapsed.
     */
    public static final int COMMAND_INTERVAL_FOR_DEVICES_CONNECTED_WITH_UNLIMITED_EXPIRY = Integer.parseInt(System.getProperty("command.repetition.interval", "5"));

    private HonoExampleConstants() {
        // prevent instantiation
    }
}
