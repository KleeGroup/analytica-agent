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

import io.analytica.spies.imp.javassist.matcher.CompositeMatcher;
import io.analytica.spies.imp.javassist.matcher.CtBehaviorMatcher;
import io.analytica.spies.imp.javassist.matcher.InheritClassMatcher;
import io.analytica.spies.imp.javassist.matcher.Matcher;
import io.analytica.spies.imp.javassist.matcher.RegExpMatcher;
import io.analytica.spies.imp.javassist.matcher.StaticMatcher;
import io.analytica.spies.impl.JsonConfReader;

import java.io.InvalidClassException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.InvalidParameterException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.MethodInfo;
import javassist.runtime.Desc;

/**
 * Implémentation de ClassFileTransformer pour instrumenter les méthodes.
 * Necessite d'être placé dans un jar <b>avec</b> les class de Javassist, car il n'y a pas de gestion de classpath dans le javaagent
 * @author npiedeloup
 * @version $Id: MemoryLeakTransformer.java,v 1.1 2011/05/12 10:16:05 prahmoune Exp $
 */
final class AnalyticaSpyTransformer implements ClassFileTransformer {

	private final AnalyticaSpyConf analyticaSpyConf;
	private final Matcher<String> excludeMatcher;
	private final Matcher<String> includeMatcher;
	private final Map<Matcher<String>, AnalyticaSpyHookPoint> classNameHookPointMatchers = new LinkedHashMap<Matcher<String>, AnalyticaSpyHookPoint>();
	private final Map<AnalyticaSpyHookPoint, Matcher<CtClass>> classHookPointMatchers = new LinkedHashMap<AnalyticaSpyHookPoint, Matcher<CtClass>>();
	private final Map<AnalyticaSpyHookPoint, Matcher<CtBehavior>> methodHookPointMatchers = new LinkedHashMap<AnalyticaSpyHookPoint, Matcher<CtBehavior>>();

	private final Map<String, CtClass> localVariables = new HashMap<String, CtClass>();
	private final List<String> methodBefore;
	private final List<String> methodAfter;
	private final Map<CtClass, List<String>> methodCatchs = new HashMap<CtClass, List<String>>();
	private final List<String> methodFinally;

	private final List<ClassLoader> registeredClassLoader = new ArrayList<ClassLoader>();
	private final ClassPool classPool = ClassPool.getDefault();

	/**
	 * @param agentArgs parametres du javaagent de ligne de commande
	 */
	AnalyticaSpyTransformer(final String agentArgs) {
		//Assertion.notEmpty(agentArgs); //pas Asssertion pour eviter une dépendance
		if (agentArgs == null || agentArgs.length() == 0) {
			throw new IllegalArgumentException("Usage : -javaagent:analyticaAgent.jar=analyticaPlugs.json");
		}
		analyticaSpyConf = JsonConfReader.loadJsonConf(agentArgs, AnalyticaSpyConf.class);
		System.out.println("Analytica start conf : " + analyticaSpyConf.toJson());

		Container.initCollector(analyticaSpyConf);

		excludeMatcher = buildStringMatchers(analyticaSpyConf.getFastExcludedPackages(), false);
		includeMatcher = buildStringMatchers(analyticaSpyConf.getFastIncludedPackages(), true);

		methodBefore = analyticaSpyConf.getMethodBefore();
		methodAfter = analyticaSpyConf.getMethodAfter();
		methodFinally = analyticaSpyConf.getMethodFinally();

		for (final AnalyticaSpyHookPoint hookPoint : analyticaSpyConf.getHookPoints()) {
			classNameHookPointMatchers.put(new RegExpMatcher(hookPoint.getClassName()), hookPoint);
			if (hookPoint.getInherits() != null) {
				classHookPointMatchers.put(hookPoint, new InheritClassMatcher(hookPoint.getInherits()));
			}
			methodHookPointMatchers.put(hookPoint, buildCtBehaviorMatchers(hookPoint.getMethods(), true));
		}

		Desc.useContextClassLoader = true;
	}

	/** {@inheritDoc} */
	public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
		try {
			final String adaptedclassName = className.replace('/', '.');
			final AnalyticaSpyHookPoint hookPoint = lookForHookPoint(loader, adaptedclassName);
			if (hookPoint == null) {
				return null;
			}

			//Sinon on instrument
			final byte[] newClass = instrumentClass(loader, adaptedclassName, hookPoint);
			return newClass;
		} catch (final Throwable th) {
			System.err.println("Could not instrument  " + className + ",  exception : " + th.getMessage());
			return null;
		}
	}

	/**
	 * @param loader Class loader
	 * @param className Nom de la classe
	 * @return si cette classe doit être instrumentée
	 */
	boolean shouldTransform(final ClassLoader loader, final String className) {
		final String adaptedclassName = className.replace('/', '.');
		return lookForHookPoint(loader, adaptedclassName) != null;
	}

	private void registerClassLoader(final ClassLoader loader) {
		if (!registeredClassLoader.contains(loader)) {
			classPool.appendClassPath(new javassist.LoaderClassPath(loader));
			registeredClassLoader.add(loader);
		}
	}

	private static Matcher<String> buildStringMatchers(final List<String> patterns, final boolean ifEmpty) {
		if (patterns.isEmpty()) {
			return new StaticMatcher<String>(ifEmpty);
		}
		final List<Matcher<String>> stringMatchers = new ArrayList<Matcher<String>>();
		for (final String pattern : patterns) {
			stringMatchers.add(new RegExpMatcher(pattern));
		}
		return new CompositeMatcher<String>(stringMatchers);
	}

	private static Matcher<CtBehavior> buildCtBehaviorMatchers(final List<String> patterns, final boolean ifEmpty) {
		if (patterns.isEmpty()) {
			return new StaticMatcher<CtBehavior>(ifEmpty);
		}
		final List<Matcher<CtBehavior>> ctBehaviorMatchers = new ArrayList<Matcher<CtBehavior>>();
		for (final String pattern : patterns) {
			ctBehaviorMatchers.add(new CtBehaviorMatcher(pattern));
		}
		return new CompositeMatcher<CtBehavior>(ctBehaviorMatchers);
	}

	private AnalyticaSpyHookPoint lookForHookPoint(final ClassLoader loader, final String adaptedclassName) {
		//Test d'exclusions et d'inclusion rapide
		if (excludeMatcher.isMatch(adaptedclassName) || !includeMatcher.isMatch(adaptedclassName)) {
			// @see ClassFileTransformer : Returning null means that no transformation was done.
			//System.out.println("Analytica exclude " + excludeMatcher.isMatch(adaptedclassName) + ", include " + !includeMatcher.isMatch(adaptedclassName) + " ");
			return null;
		}

		//Test de d'inclusions par le nom de class (pour ne créer la CtClass qu'au besoin)
		final List<AnalyticaSpyHookPoint> hookPoints = getHookPointByClassName(adaptedclassName);
		if (hookPoints.isEmpty()) {
			// @see ClassFileTransformer : Returning null means that no transformation was done.
			//System.out.println("Analytica noHookPoint " + adaptedclassName + " ");
			return null;
		}

		final CtClass ctClass = obtainCtClass(loader, adaptedclassName);
		if (ctClass.isInterface()) {
			return null; //pas d'instrumentation des interfaces
		}
		//Test de l'héritage si présent
		for (final AnalyticaSpyHookPoint hookPoint : hookPoints) {
			final Matcher<CtClass> classMatcher = classHookPointMatchers.get(hookPoint);
			if (classMatcher == null || classMatcher.isMatch(ctClass)) {
				return hookPoint;
			}
		}
		// @see ClassFileTransformer : Returning null means that no transformation was done.
		return null;
	}

	private CtClass obtainCtClass(final ClassLoader loader, final String adaptedclassName) {
		registerClassLoader(loader);
		final CtClass cl;
		try {
			cl = classPool.get(adaptedclassName);
		} catch (final NotFoundException e) {
			System.err.println("Could not instrument  " + adaptedclassName + ",  exception : " + e.getMessage());
			throw new RuntimeException(e);
		}
		return cl;
	}

	private List<AnalyticaSpyHookPoint> getHookPointByClassName(final String adaptedclassName) {
		final List<AnalyticaSpyHookPoint> hookPoints = new ArrayList<AnalyticaSpyHookPoint>();
		for (final Map.Entry<Matcher<String>, AnalyticaSpyHookPoint> entry : classNameHookPointMatchers.entrySet()) {
			if (entry.getKey().isMatch(adaptedclassName)) {
				hookPoints.add(entry.getValue());
			}
		}
		return hookPoints;
	}

	private enum MethodType {
		INIT, CLINIT, METHOD, // présent dans MethodInfo
		ABSTRACT, FINAL, NATIVE, PRIVATE, PROTECTED, PUBLIC, STATIC, SYNCHRONIZED // quelques javassist.Modifier
	}

	private byte[] instrumentClass(final ClassLoader loader, final String adaptedclassName, final AnalyticaSpyHookPoint hookPoint) {
		System.out.println("Analytica instrument " + adaptedclassName + " with : " + hookPoint);
		final CtClass ctClass = obtainCtClass(loader, adaptedclassName);
		try {
			final Matcher<CtBehavior> methodMatcher = methodHookPointMatchers.get(hookPoint);
			try {
				populateLocalVariables(analyticaSpyConf.getLocalVariables());
				populateMethodCatchs(analyticaSpyConf.getMethodCatchs());

				if (!ctClass.isInterface()) { // on ne peut pas instrumenter les interfaces
					//final String agentManagerDef = "private final io.analytica.agent.AgentManager agentManager = Home.getContainer().getManager(io.analytica.agent.AgentManager.class);";
					//final CtField agentManagerField = CtField.make(agentManagerDef, cl);
					//cl.addField(agentManagerField);

					final CtBehavior[] methods = ctClass.getDeclaredBehaviors();
					for (final CtBehavior method : methods) {
						if (method.isEmpty() == false && methodMatcher.isMatch(method)) {
							if (isAttributsAccepted(hookPoint.getMethodTypes(), method)) {
								try {
									instrumentMethod(method, hookPoint);
								} catch (final Exception e) {
									System.err.println("Can't instrument " + adaptedclassName + "." + method.getName() + ",  exception " + e.getClass().getName() + " : " + e.getMessage());
									e.printStackTrace();
									throw new RuntimeException(e);
								}
							}
						}
					}
					return ctClass.toBytecode();
				}
				throw new InvalidClassException("Can't instrument interfaces");
			} catch (final Exception e) {
				System.err.println("Can't instrument " + adaptedclassName + ",  exception " + e.getClass().getName() + " : " + e.getMessage());
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		} finally {
			ctClass.detach();
		}
	}

	private boolean isAttributsAccepted(final List<String> methodTypesStr, final CtBehavior method) {
		final MethodInfo methodInfo = method.getMethodInfo();
		final int modifiers = method.getModifiers();
		boolean attributesAccepted = true;
		for (final String attrStr : methodTypesStr) {
			final boolean isNot = attrStr.startsWith("!");
			final MethodType attr = MethodType.valueOf((isNot ? attrStr.substring(1) : attrStr).toUpperCase());
			switch (attr) {
				case INIT:
					attributesAccepted = attributesAccepted && applyINot(isNot, methodInfo.isConstructor());
					break;
				case CLINIT:
					attributesAccepted = attributesAccepted && applyINot(isNot, methodInfo.isStaticInitializer());
					break;
				case METHOD:
					attributesAccepted = attributesAccepted && applyINot(isNot, methodInfo.isMethod());
					break;
				case ABSTRACT:
					attributesAccepted = attributesAccepted && applyINot(isNot, Modifier.isAbstract(modifiers));
					break;
				case FINAL:
					attributesAccepted = attributesAccepted && applyINot(isNot, Modifier.isFinal(modifiers));
					break;
				case NATIVE:
					attributesAccepted = attributesAccepted && applyINot(isNot, Modifier.isNative(modifiers));
					break;
				case PRIVATE:
					attributesAccepted = attributesAccepted && applyINot(isNot, Modifier.isPrivate(modifiers));
					break;
				case PROTECTED:
					attributesAccepted = attributesAccepted && applyINot(isNot, Modifier.isProtected(modifiers));
					break;
				case PUBLIC:
					attributesAccepted = attributesAccepted && applyINot(isNot, Modifier.isPublic(modifiers));
					break;
				case STATIC:
					attributesAccepted = attributesAccepted && applyINot(isNot, Modifier.isStatic(modifiers));
					break;
				case SYNCHRONIZED:
					attributesAccepted = attributesAccepted && applyINot(isNot, Modifier.isSynchronized(modifiers));
					break;
				default:
					throw new InvalidParameterException("Type d'attributs non géré : " + attrStr);
			}
			if (!attributesAccepted) {
				break; //inutil de continuer
			}
		}
		return attributesAccepted;
	}

	private boolean applyINot(final boolean isNot, final boolean test) {
		return isNot ? !test : test;
	}

	private void populateLocalVariables(final Map<String, String> localVariablesConf) throws NotFoundException {
		for (final Map.Entry<String, String> entry : localVariablesConf.entrySet()) {
			localVariables.put(entry.getKey(), classPool.get(entry.getValue()));
		}
	}

	private void populateMethodCatchs(final Map<String, List<String>> methodCatchsConf) throws NotFoundException {
		for (final Map.Entry<String, List<String>> entry : methodCatchsConf.entrySet()) {
			methodCatchs.put(classPool.get(entry.getKey()), entry.getValue());
		}
	}

	private void instrumentMethod(final CtBehavior method, final AnalyticaSpyHookPoint hookPoint) throws CannotCompileException {
		//method.addLocalVariable("processType", classPool.get("String"));
		//method.addLocalVariable("subTypes", classPool.get("java.lang.String[]"));
		for (final Map.Entry<String, CtClass> entry : localVariables.entrySet()) {
			method.addLocalVariable(entry.getKey(), entry.getValue());
			//System.out.println("addLocalVariable " + entry.getKey() + " : " + entry.getValue().getName());
		}

		final String className = method.getDeclaringClass().getName();
		final String methodName = method.getName();
		final StringBuilder sbBefore = new StringBuilder();
		appendPredefinedVariable(sbBefore, hookPoint, className, methodName);
		for (final String line : methodBefore) {
			if (line.length() != 0) {
				sbBefore.append(line).append("\n");
			}
		}
		if (sbBefore.length() > 0) {
			method.insertBefore(replaceAlias(sbBefore.toString(), className, methodName));
		}
		//System.out.println("sbBefore " + sbBefore.toString());

		final StringBuilder sbAfter = new StringBuilder();
		for (final String line : methodAfter) {
			if (line.length() != 0) {
				sbAfter.append(line).append("\n");
			}
		}
		if (sbAfter.length() > 0) {
			method.insertAfter(replaceAlias(sbAfter.toString(), className, methodName), false);
		}
		//System.out.println("sbAfter " + sbAfter.toString());

		for (final Map.Entry<CtClass, List<String>> entry : methodCatchs.entrySet()) {
			final StringBuilder sbCatch = new StringBuilder();
			for (final String line : entry.getValue()) {
				if (line.length() != 0) {
					sbCatch.append(line).append("\n");
				}
			}
			method.addCatch(replaceAlias(sbCatch.toString(), className, methodName), entry.getKey());
			//System.out.println("addCatch " + entry.getKey().getName() + " : " + entry.getValue());
		}

		final StringBuilder sbFinally = new StringBuilder();
		for (final String line : methodFinally) {
			if (line.length() != 0) {
				sbFinally.append(line).append("\n");
			}
		}
		if (sbFinally.length() > 0) {
			method.insertAfter(replaceAlias(sbFinally.toString(), className, methodName), true);
		}
		//System.out.println("sbFinally " + sbFinally.toString());

		System.out.println("Analytica instrument " + method.getLongName());
	}

	private void appendPredefinedVariable(final StringBuilder buffer, final AnalyticaSpyHookPoint hookPoint, final String className, final String methodName) {
		buffer.append("final String processType = \"" + hookPoint.getProcessType() + "\";\n");
		buffer.append("final String[] subTypes = new String[" + hookPoint.getSubTypes().size() + "];\n");
		int i = 0;
		for (final String subType : hookPoint.getSubTypes()) {
			buffer.append("subTypes[").append(i).append("] = ");
			final String updatedSubType = replaceAlias(subType, className, methodName);
			if (subType.contains("$")) {
				buffer.append(updatedSubType);
			} else {
				buffer.append("\"");
				buffer.append(subType);
				buffer.append("\"");
			}
			buffer.append(";\n");
			i++;
		}
	}

	private String replaceAlias(final String toReplace, final String className, final String methodName) {
		String updated = toReplace.replaceAll("\\$methodName", "\"" + methodName + "\"");
		updated = updated.replaceAll("\\$className", "\"" + className + "\"");
		//updated = updated.replaceAll("\\$collector", "io.analytica.agent.impl.KProcessCollector processCollector = io.analytica.spies.imp.javassist.Container.getProcessCollector();");
		updated = updated.replaceAll("\\$collector", "io.analytica.spies.imp.javassist.Container.getProcessCollector()");
		return updated;
	}
	//
	//	private void addProfilingInformation(final CtClass clas, final CtMethod mold) throws NotFoundException, CannotCompileException {
	//		// get the method information (throws exception if method with
	//		// given name is not declared directly by this class, returns
	//		// arbitrary choice if more than one with the given name)
	//		final String mname = mold.getName();
	//		final String longName = mold.getLongName();
	//
	//		// rename old method to synthetic name, then duplicate the
	//		// method with original name for use as interceptor
	//		final String nname = mname + "$impl";
	//
	//		mold.setName(nname);
	//		final CtMethod mnew = CtNewMethod.copy(mold, mname, clas, null);
	//
	//		// start the body text generation by saving the start time
	//		// to a local variable, then call the timed method; the
	//		// actual code generated needs to depend on whether the
	//		// timed method returns a value
	//		final String type = mold.getReturnType().getName();
	//		final StringBuffer body = new StringBuffer();
	//		body.append("{\n");
	//		body.append("int currentStackDepth = Thread.currentThread().getStackTrace().length;\n");
	//		body.append("StringBuffer buf = new StringBuffer();");
	//		body.append("for (int zs = 0; zs < currentStackDepth; zs++) {").append("buf.append(\"   \");\n").append("}");
	//
	//		body.append(" System.out.println(buf.toString() + \"-> Enter Method:" + longName + "\");\n");
	//		body.append(" long startTime = System.nanoTime();\n");
	//		body.append("try {\n");
	//
	//		if (!"void".equals(type)) {
	//			body.append(type + " result = ");
	//		}
	//
	//		body.append(nname + "($$);\n");
	//
	//		if (!"void".equals(type)) {
	//			body.append("return result;\n");
	//		}
	//
	//		body.append("} finally {");
	//		// finish body text generation with call to print the timing
	//		// information, and return saved value (if not void)
	//		body.append("long endTime = System.nanoTime();\n");
	//		body.append("long delta = endTime - startTime;\n");
	//
	//		body.append("System.out.println(buf.toString() + \"<- Exit Method:" + longName + " completed in \" + delta + \" nano secs\");\n");
	//
	//		body.append(" }\n");
	//		body.append("}");
	//
	//		// replace the body of the interceptor method with generated
	//		// code block and add it to class
	//		mnew.setBody(body.toString());
	//		clas.addMethod(mnew);
	//	}

}
