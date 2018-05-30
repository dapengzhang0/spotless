/*
 * Copyright 2016 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.spotless.extra.java;

import java.lang.reflect.Method;
import java.util.Properties;

import com.diffplug.spotless.FormatterFunc;
import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.Provisioner;
import com.diffplug.spotless.ThrowingEx;
import com.diffplug.spotless.extra.config.EclipseConfiguration;
import com.diffplug.spotless.extra.config.EclipseConfiguration.State;

/** Formatter step which calls out to the Eclipse JDT formatter. */
public final class EclipseJdtFormatterStep implements ThrowingEx.Function<EclipseConfiguration.State, FormatterFunc> {

	static final String NAME = "eclipse jdt formatter";
	static final String FORMATTER_CLASS = "com.diffplug.gradle.spotless.java.eclipse.EclipseFormatterStepImpl";
	static final String FORMATTER_METHOD = "format";
	static final String VERSIONS[] = {"4.6.1", "4.6.3", "4.7.0", "4.7.1", "4.7.2"};

	/** Constructs a formatter step adapted for a certain Eclipse JDT formatter version */
	private EclipseJdtFormatterStep() {
		//Currently all supported versions behaves equally. No adaptation required.
	}

	/** Provides default configuration */
	public static EclipseConfiguration createConfig(Provisioner provisioner) {
		return new EclipseConfiguration(NAME, provisioner, VERSIONS);
	}

	/** Creates a formatter step for the given configuration state. */
	public static FormatterStep createStep(EclipseConfiguration config) {
		return FormatterStep.createLazy(NAME, config, new EclipseJdtFormatterStep());
	}

	@Override
	public FormatterFunc apply(State state) throws Exception {
		Class<?> formatterClazz = state.loadClass(FORMATTER_CLASS);
		Object formatter = formatterClazz.getConstructor(Properties.class).newInstance(state.getPreferences());
		Method method = formatterClazz.getMethod(FORMATTER_METHOD, String.class);
		return input -> (String) method.invoke(formatter, input);
	}

}