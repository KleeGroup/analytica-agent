/**
 * Kasper-kernel - v6 - Simple Java Framework
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidière - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package kasperimpl.resource.plugins.java;

import java.net.URL;

import kasper.kernel.lang.Option;
import kasper.kernel.util.Assertion;
import kasperimpl.resource.ResourceResolverPlugin;

/**
 * Résolution des URL liées au classPath.
 * 
 * @author prahmoune
 * @version $Id: ClassPathResourceResolverPlugin.java,v 1.3 2013/01/28 11:53:27 npiedeloup Exp $ 
 */
public final class ClassPathResourceResolverPlugin implements ResourceResolverPlugin {

	/** {@inheritDoc} */
	public Option<URL> resolve(final String resource) {
		Assertion.notNull(resource);
		// ---------------------------------------------------------------------
		final URL url = getClass().getResource(resource);
		return Option.option(url);
	}
}
