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
package com.diffplug.spotless.maven;

import static java.util.stream.Collectors.toSet;

import java.util.Objects;

import com.diffplug.spotless.Provisioner;

/** Maven integration for Provisioner. */
public class MavenProvisioner {
	private MavenProvisioner() {}

	public static Provisioner create(ArtifactResolver artifactResolver) {
		Objects.requireNonNull(artifactResolver);

		return mavenCoords -> mavenCoords.stream()
				.flatMap(coord -> artifactResolver.resolve(coord).stream())
				.collect(toSet());
	}
}
