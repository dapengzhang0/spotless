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
package com.diffplug.spotless.extra.config;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.diffplug.spotless.ResourceHarness;
import com.diffplug.spotless.SerializableEqualityTester;
import com.diffplug.spotless.TestProvisioner;
import com.diffplug.spotless.java.ImportOrderStep;
import com.diffplug.spotless.kotlin.KtLintStep;

public class EclipseConfigurationTest extends ResourceHarness {

	private static final String NAME = "test configuration";
	private static final String VERSION_LOW = "0.0.1";
	private static final String VERSION_IN_BETWEEN = "0.1.0";
	private static final String VERSION_HIGH = "1.0.0";
	private static final String VERSION_TOO_HIGH = "10.0.0";
	private static final String VERSIONS[] = {VERSION_LOW, VERSION_HIGH, VERSION_IN_BETWEEN};
	private static final String DEPENDENCY_OLD = "com.diffplug.spotless:spotless-lib:1.0.0";
	private static final String DEPENDENCY_NEW = "com.diffplug.spotless:spotless-lib:1.12.0";
	private static final String TEST_FILES_FOLDER = "extra/config/";
	private static final String PREFERENCES_FILE = TEST_FILES_FOLDER + "preferences.properties";
	private static final String DEPENDENCY_OLD_FILE = TEST_FILES_FOLDER + "dependencies_old.txt";

	private EclipseConfiguration testConfig;

	@Before
	public void initTestConfig() throws IOException {
		testConfig = new EclipseConfiguration(NAME, TestProvisioner.mavenCentral(), VERSIONS);
	}

	@Test
	public void testDefaultVersionIsLatest() {
		assertThat(testConfig.get().compareVersionTo(VERSION_HIGH)).isEqualTo(0);
	}

	@Test
	public void testSetVersion() {
		testConfig.setVersion(VERSION_LOW);
		assertThat(testConfig.get().compareVersionTo(VERSION_LOW)).isEqualTo(0);
	}

	@Test
	public void testSetUnsupportedVersion() {
		assertThatExceptionOfType(UserArgumentException.class).isThrownBy(() -> testConfig.setVersion(VERSION_TOO_HIGH));
		assertThat(testConfig.get().compareVersionTo(VERSION_HIGH)).isEqualTo(0);
	}

	@Test
	public void testSetUrlInsteadOfVersion() throws IOException {
		URL userDepURL = createTestFile(DEPENDENCY_OLD_FILE).toURI().toURL();
		testConfig.setVersion(userDepURL.toString());
		//Version is not altered in this case
		assertThat(testConfig.get().compareVersionTo(VERSION_HIGH)).isEqualTo(0);
		//But the old version as specified by the user URL is loaded
		assertThatExceptionOfType(UserArgumentException.class).isThrownBy(() -> testConfig.get().loadClass(KtLintStep.class.getCanonicalName()));
		testConfig.get().loadClass(ImportOrderStep.class.getCanonicalName());
	}

	@Test
	public void testSetMalformedUrl() {
		assertThatExceptionOfType(UserArgumentException.class).isThrownBy(() -> testConfig.setVersion("invalidProtocol://some.domain/some.properties"));
		assertThat(testConfig.get().compareVersionTo(VERSION_HIGH)).isEqualTo(0);
	}

	@Test
	public void testVersionComparison() {
		assertThat(testConfig.get().compareVersionTo(VERSION_HIGH)).isEqualTo(0);
		assertThat(testConfig.get().compareVersionTo(VERSION_TOO_HIGH)).isEqualTo(-1);
		assertThat(testConfig.get().compareVersionTo(VERSION_LOW)).isEqualTo(1);
	}

	@Test
	public void testVersionComparisonForInvalidValue() {
		assertThatExceptionOfType(UserArgumentException.class).isThrownBy(() -> testConfig.get().compareVersionTo(null));
	}

	@Test
	public void testPreferences() throws IOException {
		File propFile = createTestFile(PREFERENCES_FILE);
		testConfig.setPreferences(Arrays.asList(propFile));
		Properties prop = testConfig.get().getPreferences();
		assertThat(prop).containsEntry("A", "B");
	}

	@Test
	public void testInvalidPreferences() throws IOException {
		File doesNotExist = new File("doesNotExist");
		testConfig.setPreferences(Arrays.asList(doesNotExist));
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> testConfig.get().getPreferences());
	}

	@Test
	public void testDefaultDependencyResolution() {
		testConfig.get().loadClass(KtLintStep.class.getCanonicalName());
		testConfig.get().loadClass(ImportOrderStep.class.getCanonicalName());
		testConfig.setVersion(VERSION_LOW);
		assertThatExceptionOfType(UserArgumentException.class).isThrownBy(() -> testConfig.get().loadClass(KtLintStep.class.getCanonicalName()));
		testConfig.get().loadClass(ImportOrderStep.class.getCanonicalName());
	}

	@Test
	public void testUserDependencyResolution() {
		testConfig.setDepenencies(DEPENDENCY_OLD);
		assertThatExceptionOfType(UserArgumentException.class).isThrownBy(() -> testConfig.get().loadClass(KtLintStep.class.getCanonicalName()));
		testConfig.get().loadClass(ImportOrderStep.class.getCanonicalName());
	}

	@Test
	public void equality() throws IOException {
		List<File> preferencesFile = createTestFiles(PREFERENCES_FILE);
		new SerializableEqualityTester() {
			List<File> settingsFiles;
			String version;
			String dependency;

			@Override
			protected void setupTest(API api) {
				version = VERSION_LOW;
				settingsFiles = new ArrayList<File>(0);
				dependency = DEPENDENCY_NEW;
				api.areDifferentThan();

				version = VERSION_HIGH;
				settingsFiles = new ArrayList<File>(0);
				dependency = DEPENDENCY_NEW;
				api.areDifferentThan();

				version = VERSION_HIGH;
				settingsFiles = preferencesFile;
				dependency = DEPENDENCY_NEW;
				api.areDifferentThan();

				version = VERSION_HIGH;
				settingsFiles = preferencesFile;
				dependency = DEPENDENCY_OLD;
				api.areDifferentThan();
			}

			@Override
			protected EclipseConfiguration.State create() {
				testConfig.setVersion(version);
				testConfig.setPreferences(settingsFiles);
				testConfig.setDepenencies(dependency);
				return testConfig.get();
			}
		}.testEquals();
	}
}