package com.kleegroup.analyticaimpl.spies.javassist;

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
	private final String processType;
	private final List<String> subTypes;

	/**
	 * Constructeur.
	 * @param className pattern de className
	 * @param inherits pattern d'une interface ou d'une superclass
	 * @param methods Liste de point d'accroche de l'agent
	 * @param processType Type du process
	 * @param subTypes Liste des sous-catégories
	 */
	public AnalyticaSpyHookPoint(final String className, final String inherits, final List<String> methods, final String processType, final List<String> subTypes) {
		this.className = className;
		this.inherits = inherits;
		this.methods = methods;
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
	 * @return Nom du type de process
	 */
	public String getProcessType() {
		return processType;
	}

	/**
	 * @return Liste des sous-catégories : en syntaxe javassist
	 */
	public List<String> getSubTypes() {
		return subTypes;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("{ \"className\" : \"" + className + "\",\n");
		sb.append(" \"inherits\" : \"" + inherits + "\",\n");
		sb.append(" \"methods\" : " + methods + ",\n");
		sb.append(" \"processType\" : \"" + processType + "\",\n");
		sb.append(" \"subTypes\" : " + subTypes + " }\n");

		return sb.toString();
	}
}
