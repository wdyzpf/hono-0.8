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
package org.eclipse.hono.messaging;

import org.eclipse.hono.service.AbstractApplication;
import org.eclipse.hono.service.HealthCheckProvider;
import org.eclipse.hono.service.auth.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import io.vertx.core.Future;
import io.vertx.core.Verticle;

/**
 * The Hono Messaging service main application class.
 * <p>
 * This class configures and wires up the Messaging service's components (Verticles).
 */
@ComponentScan(basePackages = {
                        "org.eclipse.hono.messaging",
                        "org.eclipse.hono.telemetry",
                        "org.eclipse.hono.event",
                        "org.eclipse.hono.service.auth" })
@Configuration
@EnableAutoConfiguration
public class HonoMessagingApplication extends AbstractApplication {

    private static final Logger LOG = LoggerFactory.getLogger(HonoMessagingApplication.class);

    private AuthenticationService authenticationService;

    /**
     * @param authenticationService the authenticationService to set
     */
    @Autowired
    public final void setAuthenticationService(final AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Deploys the additional service implementations that are
     * required by the HonoServer.
     *
     */
    @Override
    protected Future<Void> deployRequiredVerticles(final int maxInstances) {

        final Future<Void> result = Future.future();

        final Future<String> authFuture = deployAuthenticationService(); // we only need 1 authentication service
        authFuture.setHandler(ar -> {
            if (ar.succeeded()) {
                result.complete();
            } else {
                result.fail(ar.cause());
            }
        });
        return result;
    }

    private Future<String> deployAuthenticationService() {
        final Future<String> result = Future.future();
        if (!Verticle.class.isInstance(authenticationService)) {
            result.fail("authentication service is not a verticle");
        } else {
            LOG.info("Starting authentication service {}", authenticationService);
            getVertx().deployVerticle((Verticle) authenticationService, result.completer());
        }
        return result;
    }

    /**
     * Registers any additional health checks that the service implementation components provide.
     * 
     * @return A succeeded future.
     */
    @Override
    protected Future<Void> postRegisterServiceVerticles() {
        if (HealthCheckProvider.class.isInstance(authenticationService)) {
            registerHealthchecks((HealthCheckProvider) authenticationService);
        }
        return Future.succeededFuture();
    }

    /**
     * Starts the service.
     * 
     * @param args command line arguments to pass to the service.
     */
    public static void main(final String[] args) {
        SpringApplication.run(HonoMessagingApplication.class, args);
    }
}
