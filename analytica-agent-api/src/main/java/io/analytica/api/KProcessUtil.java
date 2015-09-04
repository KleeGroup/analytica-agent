/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.analytica.api;

import java.util.Collection;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author pchretien
 */
public final class KProcessUtil {
	private static final Gson gson = new GsonBuilder().create();

	static void checkNotNull(final Object value, final String msg) {
		if (value == null) {
			throw new NullPointerException(msg);
		}
	}

	static void ckeckRegex(final String s, final Pattern pattern, final String info) {
		if (!pattern.matcher(s).matches()) {
			throw new IllegalArgumentException(info + " " + s + " must match regex :" + pattern.pattern());
		}
	}

	static void ckeckNotEmpty(final Collection collection, final String msg) {
		if (collection == null) {
			throw new NullPointerException(msg);
		}
		if (collection.isEmpty()) {
			throw new NullPointerException(msg);
		}
	}

	public static KProcess fromJson(final String json) {
		return gson.fromJson(json, KProcess.class);
	}

	public static String toJson(final KProcess process) {
		return gson.toJson(process);
	}
}
