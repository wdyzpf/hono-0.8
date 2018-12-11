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

package org.eclipse.hono.deviceregistry;

/**
 * Common configuration properties for file based implementations of the APIs of Hono's device registry as own server.
 * 基于文件的Hono设备注册表API实现的公共配置属性作为自己的服务器。
 * <p>
 * This class is intended to be used as base class for property classes that configure a specific file based API implementation.
 * 此类旨在用作配置基于特定文件的API实现的属性类的基类。
 */
abstract class AbstractFileBasedRegistryConfigProperties {

    private String filename = getDefaultFileName();
    private boolean saveToFile = false;
    private boolean modificationEnabled = true;

    /**
     * Gets the path to the file that the registry should be persisted to periodically.
     * <p>
     *获取应定期保留注册表的文件的路径。
     * @return The path to the file.
     */
    protected abstract String getDefaultFileName();

    /**
     * Checks whether the content of the registry should be persisted to the file system
     * periodically.
     * 检查注册表的内容是否应定期持久保存到文件系统
     * <p>
     * Default value is {@code false}.
     *
     * @return {@code true} if registry content should be persisted.
     */
    public boolean isSaveToFile() {
        return saveToFile;
    }

    /**
     * Sets whether the content of the registry should be persisted to the file system
     * periodically.
     * 设置是否应定期将注册表的内容持久保存到文件系统。
     * <p>
     * Default value is {@code false}.
     *
     * @param enabled {@code true} if registry content should be persisted.
     * @throws IllegalStateException if this registry is already running.
     */
    public void setSaveToFile(final boolean enabled) {
        this.saveToFile = enabled;
    }

    /**
     * Checks whether this registry allows the creation, modification and removal of entries.
     * 检查此注册表是否允许创建，修改和删除条目。
     * <p>
     * If set to {@code false} then methods for creating, updating or deleting an entry should return a <em>403 Forbidden</em> response.
     * 如果设置为{@code false}，则创建，更新或删除条目的方法应返回
     * <p>
     * The default value of this property is {@code true}.
     *
     * @return The flag.
     */
    public boolean isModificationEnabled() {
        return modificationEnabled;
    }

    /**
     * Sets whether this registry allows creation, modification and removal of entries.
     * 设置此注册表是否允许创建，修改和删除条目。
     * <p>
     * If set to {@code false} then for creating, updating or deleting an entry should return a <em>403 Forbidden</em> response.
     * <p>
     * The default value of this property is {@code true}.
     *
     * @param flag The flag.
     */
    public void setModificationEnabled(final boolean flag) {
        modificationEnabled = flag;
    }

    /**
     * Gets the path to the file that the registry should be persisted to
     * periodically.
     * 获取应定期保留注册表的文件的路径。
     * <p>
     *
     * @return The file name.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the path to the file that the registry should be persisted to
     * periodically.
     * 设置应定期保留注册表的文件的路径。
     * <p>
     *
     * @param filename The name of the file to persist to (can be a relative or absolute path).
     */
    public void setFilename(final String filename) {
        this.filename = filename;
    }

}
