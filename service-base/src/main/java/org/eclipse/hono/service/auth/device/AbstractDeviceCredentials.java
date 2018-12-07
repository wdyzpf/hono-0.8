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

package org.eclipse.hono.service.auth.device;

import java.util.Objects;

import org.eclipse.hono.util.CredentialsObject;

import io.vertx.core.json.JsonObject;

/**
 * A base class providing utility methods for verifying credentials.
 *
 */
public abstract class AbstractDeviceCredentials implements DeviceCredentials {

    private final String tenantId;
    private final String authId;

    /**
     * Creates credentials for a tenant and authentication identifier.
     * 
     * @param tenantId The tenant that the device belongs to.
     * @param authId The identifier that the device uses for authentication.
     */
    protected AbstractDeviceCredentials(final String tenantId, final String authId) {
        this.tenantId = tenantId;
        this.authId = authId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getAuthId() {
        return authId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getTenantId() {
        return tenantId;
    }

    @Override
    public final boolean validate(final CredentialsObject credentialsOnRecord) {

        Objects.requireNonNull(credentialsOnRecord);
        if (!getAuthId().equals(credentialsOnRecord.getAuthId())) {
            return false;
        } else if (!getType().equals(credentialsOnRecord.getType())) {
            return false;
        } else if (!credentialsOnRecord.isEnabled()) {
            return false;
        } else {
            return credentialsOnRecord.getCandidateSecrets().stream().anyMatch(candidateSecret -> matchesCredentials(candidateSecret));
        }
    }

    /**
     * Checks if the credentials provided by the device match a secret on record for the device.
     * 
     * @param candidateSecret The secret to match against.
     * @return {@code true} if the credentials match.
     */
    public abstract boolean matchesCredentials(JsonObject candidateSecret);
}
