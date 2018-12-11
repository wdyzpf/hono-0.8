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

package org.eclipse.hono.service.auth.impl;

import org.eclipse.hono.config.ApplicationConfigProperties;
import org.eclipse.hono.config.ServiceConfigProperties;
import org.eclipse.hono.service.auth.AuthTokenHelper;
import org.eclipse.hono.service.auth.AuthTokenHelperImpl;
import org.eclipse.hono.service.metric.MetricsTags;
import org.eclipse.hono.util.AuthenticationConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ObjectFactoryCreatingFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.spring.autoconfigure.MeterRegistryCustomizer;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import org.springframework.context.annotation.Scope;

/**
 * Spring Boot configuration for the simple authentication server application.
 *
 */
@Configuration
public class ApplicationConfig {

    private static final String BEAN_NAME_SIMPLE_AUTHENTICATION_SERVER = "simpleAuthenticationServer";

    /**
     * Gets the singleton Vert.x instance to be used by Hono.
     * 简单认证服务器应用程序的Spring Boot配置。
     * @return the instance.
     */
    @Bean
    public Vertx vertx() {
        final VertxOptions options = new VertxOptions()
                .setWarningExceptionTime(1500000000)
                .setAddressResolverOptions(new AddressResolverOptions()
                        .setCacheNegativeTimeToLive(0) // discard failed DNS lookup results immediately
                        .setCacheMaxTimeToLive(0) // support DNS based service resolution
                        .setQueryTimeout(1000));
        return Vertx.vertx(options);
    }

    /**
     * Creates a new Authentication Server instance and exposes it as a Spring Bean.
     * 创建一个新的Authentication Server实例并将其公开为Spring Bean。
     * @return The new instance.
     */
    @Bean(name = BEAN_NAME_SIMPLE_AUTHENTICATION_SERVER)
    @Scope("prototype")
    public SimpleAuthenticationServer simpleAuthenticationServer(){
        return new SimpleAuthenticationServer();
    }

    /**
     * Exposes a factory for Authentication Server instances as a Spring bean.
     * 将Authentication Server实例的工厂公开为Spring bean。
     * @return The factory.
     */
    @Bean
    public ObjectFactoryCreatingFactoryBean authServerFactory() {
        final ObjectFactoryCreatingFactoryBean factory = new ObjectFactoryCreatingFactoryBean();
        factory.setTargetBeanName(BEAN_NAME_SIMPLE_AUTHENTICATION_SERVER);
        return factory;
    }

    /**
     * Exposes properties for configuring the application properties a Spring bean.
     * 公开用于将应用程序属性配置为Spring bean的属性。
     * @return The application configuration properties.
     */
    @Bean
    @ConfigurationProperties(prefix = "hono.app")
    public ApplicationConfigProperties applicationConfigProperties(){
        return new ApplicationConfigProperties();
    }

    /**
     * Exposes this service's AMQP endpoint configuration properties as a Spring bean.
     * 将此服务的AMQP端点配置属性公开为Spring bean。
     * @return The properties.
     */
    @Bean
    @ConfigurationProperties(prefix = "hono.auth.amqp")
    public ServiceConfigProperties amqpProperties() {
        final ServiceConfigProperties props = new ServiceConfigProperties();
        return props;
    }

    /**
     * Exposes this service's AMQP endpoint configuration properties as a Spring bean.
     * 
     * @return The properties.
     */
    @Bean
    @ConfigurationProperties(prefix = "hono.auth.svc")
    public AuthenticationServerConfigProperties serviceProperties() {
        return new AuthenticationServerConfigProperties();
    }

    /**
     * Exposes a factory for JWTs asserting a client's identity as a Spring bean.
     * 
     * @return The bean.
     */
    @Bean
    @Qualifier("signing")
    public AuthTokenHelper authTokenFactory() {
        final ServiceConfigProperties amqpProps = amqpProperties();
        final AuthenticationServerConfigProperties serviceProps = serviceProperties();
        if (!serviceProps.getSigning().isAppropriateForCreating() && amqpProps.getKeyPath() != null) {
            // fall back to TLS configuration
            serviceProps.getSigning().setKeyPath(amqpProps.getKeyPath());
        }
        return AuthTokenHelperImpl.forSigning(vertx(), serviceProps.getSigning());
    }

    /**
     * Creates a helper for validating JWTs asserting a client's identity and authorities.
     * <p>
     * An instance of this bean is required for the {@code HonoSaslAuthenticationFactory}.
     * 
     * @return The bean.
     */
    @Bean
    @Qualifier(AuthenticationConstants.QUALIFIER_AUTHENTICATION)
    public AuthTokenHelper tokenValidator() {
        final ServiceConfigProperties amqpProps = amqpProperties();
        final AuthenticationServerConfigProperties serviceProps = serviceProperties();
        if (!serviceProps.getValidation().isAppropriateForValidating() && amqpProps.getCertPath() != null) {
            // fall back to TLS configuration
            serviceProps.getValidation().setCertPath(amqpProps.getCertPath());
        }
        return AuthTokenHelperImpl.forValidating(vertx(), serviceProps.getValidation());
    }

    /**
     * Customizer for meter registry.
     * 用于仪表注册的定制器。
     * @return The new meter registry customizer.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTags() {

        return r -> r.config().commonTags(
                MetricsTags.forService(MetricsTags.VALUE_SERVICE_AUTH));

    }
}