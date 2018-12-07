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

package org.eclipse.hono.adapter.mqtt;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.hono.auth.Device;
import org.eclipse.hono.util.MapBasedExecutionContext;
import org.eclipse.hono.util.ResourceIdentifier;

import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.messages.MqttPublishMessage;

/**
 * A dictionary of relevant information required during the
 * processing of an MQTT message published by a device.
 *
 */
public final class MqttContext extends MapBasedExecutionContext {

    private final MqttPublishMessage message;
    private final MqttEndpoint deviceEndpoint;
    private final Device authenticatedDevice;
    private final ResourceIdentifier topic;

    private String contentType;

    /**
     * Creates a new context for a message and an endpoint.
     * 
     * @param publishedMessage The MQTT message to process.
     * @param deviceEndpoint The endpoint representing the device
     *                       that has published the message.
     * @throws NullPointerException if message or endpoint are {@code null}.
     */
    public MqttContext(
            final MqttPublishMessage publishedMessage,
            final MqttEndpoint deviceEndpoint) {

        this(publishedMessage, deviceEndpoint, null);
    }

    /**
     * Creates a new context for a message and an endpoint.
     * 
     * @param publishedMessage The published MQTT message.
     * @param deviceEndpoint The endpoint representing the device
     *                       that has published the message.
     * @param authenticatedDevice The authenticated device identity.
     * @throws NullPointerException if message or endpoint are {@code null}.
     */
    public MqttContext(
            final MqttPublishMessage publishedMessage,
            final MqttEndpoint deviceEndpoint,
            final Device authenticatedDevice) {

        this.message = Objects.requireNonNull(publishedMessage);
        this.deviceEndpoint = Objects.requireNonNull(deviceEndpoint);
        this.authenticatedDevice = authenticatedDevice;
        ResourceIdentifier t = null;
        try {
            t = ResourceIdentifier.fromString(publishedMessage.topicName());
        } catch (final Throwable e) {
        }
        this.topic = t;
    }

    /**
     * Gets the MQTT message to process.
     * 
     * @return The message.
     */
    public MqttPublishMessage message() {
        return message;
    }

    /**
     * Gets the MQTT endpoint over which the message has been
     * received.
     * 
     * @return The endpoint.
     */
    public MqttEndpoint deviceEndpoint() {
        return deviceEndpoint;
    }

    /**
     * Gets the identity of the authenticated device
     * that has published the message.
     * 
     * @return The identity or {@code null} if the device has not
     *         been authenticated.
     */
    public Device authenticatedDevice() {
        return authenticatedDevice;
    }

    /**
     * Gets the content type of the message payload.
     * 
     * @return The type or {@code null} if the content type is unknown.
     */
    public String contentType() {
        return contentType;
    }

    /**
     * Sets the content type of the message payload.
     * 
     * @param contentType The type or {@code null} if the content type is unknown.
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the topic that the message has been published to.
     * 
     * @return The topic or {@code null} if the topic could not be
     * parsed into a resource identifier.
     */
    public ResourceIdentifier topic() {
        return topic;
    }

    /**
     * Gets the tenant that the device belongs to that published
     * the message.
     * 
     * @return The tenant identifier or {@code null} if the tenant cannot
     *         be determined from the message's topic.
     */
    public String tenant() {
        return Optional.ofNullable(topic).map(t -> t.getTenantId()).orElse(null);
    }

    /**
     * Gets the name of the endpoint that the message has been published to.
     * 
     * @return The name or {@code null} if the endpoint cannot
     *         be determined from the message's topic.
     */
    public String endpoint() {
        return Optional.ofNullable(topic).map(t -> t.getEndpoint()).orElse(null);
    }
}
