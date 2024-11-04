/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.ark.plugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.provider.Property;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

public interface BootArchive extends Task {

	/**
	 * Returns the fully-qualified name of the application's main class.
	 * @return the fully-qualified name of the application's main class
	 * @since 2.4.0
	 */
	@Input
	Property<String> getMainClass();

	/**
	 * Adds Ant-style patterns that identify files that must be unpacked from the archive
	 * when it is launched.
	 * @param patterns the patterns
	 */
	void requiresUnpack(String... patterns);

	/**
	 * Adds a spec that identifies files that must be unpacked from the archive when it is
	 * launched.
	 * @param spec the spec
	 */
	void requiresUnpack(Spec<FileTreeElement> spec);


	/**
	 * Returns the classpath that will be included in the archive.
	 * @return the classpath
	 */
	@Optional
	@Classpath
	FileCollection getClasspath();

	/**
	 * Adds files to the classpath to include in the archive. The given {@code classpath}
	 * is evaluated as per {@link Project#files(Object...)}.
	 * @param classpath the additions to the classpath
	 */
	void classpath(Object... classpath);

	/**
	 * Sets the classpath to include in the archive. The given {@code classpath} is
	 * evaluated as per {@link Project#files(Object...)}.
	 * @param classpath the classpath
	 * @since 2.0.7
	 */
	void setClasspath(Object classpath);

	/**
	 * Sets the classpath to include in the archive.
	 * @param classpath the classpath
	 * @since 2.0.7
	 */
	void setClasspath(FileCollection classpath);

}
