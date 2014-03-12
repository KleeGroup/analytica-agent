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
