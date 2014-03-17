/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidière - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you permission to link this library
 * with independent modules to produce an executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the license of that module.
 * An independent module is a module which is not derived from or based on this library.
 * If you modify this library, you may extend this exception to your version of the library,
 * but you are not obliged to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 */
package io.analytica.spies.imp.javassist;

import java.util.Collections;
import java.util.List;

/**
 * Paramétrage d'un point d'encrage de l'agent.
 * @author npiedeloup
 * @version $Id: $
 */
public class AnalyticaSpyHookPoint {

	private final String className;
	private final String inherits;
	private final List<String> methods;
	private final List<String> methodTypes;
	private final String processType;
	private final List<String> subTypes;

	/**
	 * Constructeur.
	 * @param className pattern de className
	 * @param inherits pattern d'une interface ou d'une superclass
	 * @param methods Liste de point d'accroche de l'agent
	 * @param methodTypes Type des points d'accroches  (! pour not): INIT, CLINIT, METHOD, ABSTRACT, FINAL, NATIVE, PRIVATE, PROTECTED, PUBLIC, STATIC, SYNCHRONIZED
	 * @param processType Type du process
	 * @param subTypes Liste des sous-catégories
	 */
	public AnalyticaSpyHookPoint(final String className, final String inherits, final List<String> methods, final List<String> methodTypes, final String processType, final List<String> subTypes) {
		this.className = className;
		this.inherits = inherits;
		this.methods = methods;
		this.methodTypes = methodTypes;
		this.processType = processType;
		this.subTypes = subTypes;
	}

	/**
	 * @return Nom de la class
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return Nom de l'héritage (nullable)
	 */
	public String getInherits() {
		return inherits;
	}

	/**
	 * @return Patterns de lecture du log
	 */
	public List<String> getMethods() {
		return methods;
	}

	/**
	 * @return Type de methods acceptés (! pour not): INIT, CLINIT, METHOD, ABSTRACT, FINAL, NATIVE, PRIVATE, PROTECTED, PUBLIC, STATIC, SYNCHRONIZED
	 */
	public List<String> getMethodTypes() {
		return methodTypes != null ? methodTypes : Collections.<String> emptyList();//Gson peut laisser des listes null
	}

	/**
	 * @return Nom du type de process
	 */
	public String getProcessType() {
		return processType;
	}

	/**
	 * @return Liste des sous-catégories : en syntaxe javassist
	 */
	public List<String> getSubTypes() {
		return subTypes != null ? subTypes : Collections.<String> emptyList();//Gson peut laisser des listes null
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("{ \"className\" : \"" + className + "\",\n");
		sb.append(" \"inherits\" : \"" + inherits + "\",\n");
		sb.append(" \"methods\" : " + methods + ",\n");
		sb.append(" \"methodsAttributes\" : \"" + methodTypes + "\",\n");
		sb.append(" \"processType\" : \"" + processType + "\",\n");
		sb.append(" \"subTypes\" : " + subTypes + " }\n");

		return sb.toString();
	}
}
