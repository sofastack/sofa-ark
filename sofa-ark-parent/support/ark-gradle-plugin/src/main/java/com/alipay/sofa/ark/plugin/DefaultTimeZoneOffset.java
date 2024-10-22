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

import java.nio.file.attribute.FileTime;
import java.util.TimeZone;

class DefaultTimeZoneOffset {

	static final DefaultTimeZoneOffset INSTANCE = new DefaultTimeZoneOffset(TimeZone.getDefault());

	private final TimeZone defaultTimeZone;

	DefaultTimeZoneOffset(TimeZone defaultTimeZone) {
		this.defaultTimeZone = defaultTimeZone;
	}

	/**
	 * Remove the default offset from the given time.
	 * @param time the time to remove the default offset from
	 * @return the time with the default offset removed
	 */
	FileTime removeFrom(FileTime time) {
		return FileTime.fromMillis(removeFrom(time.toMillis()));
	}

	/**
	 * Remove the default offset from the given time.
	 * @param time the time to remove the default offset from
	 * @return the time with the default offset removed
	 */
	long removeFrom(long time) {
		return time - this.defaultTimeZone.getOffset(time);
	}

}
