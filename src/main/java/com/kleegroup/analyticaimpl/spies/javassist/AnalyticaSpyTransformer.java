package com.kleegroup.analyticaimpl.spies.javassist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.io.Reader;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;

import com.google.gson.Gson;
import com.kleegroup.analyticaimpl.spies.javassist.matcher.CompositeMatcher;
import com.kleegroup.analyticaimpl.spies.javassist.matcher.CtBehaviorMatcher;
import com.kleegroup.analyticaimpl.spies.javassist.matcher.InheritClassMatcher;
import com.kleegroup.analyticaimpl.spies.javassist.matcher.Matcher;
import com.kleegroup.analyticaimpl.spies.javassist.matcher.RegExpMatcher;
import com.kleegroup.analyticaimpl.spies.javassist.matcher.StaticMatcher;

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

	/**
	 * @param agentArgs parametres du javaagent de ligne de commande
	 */
	AnalyticaSpyTransformer(final String agentArgs) {
		//Assertion.notEmpty(agentArgs); //pas Asssertion pour eviter une dépendance
		if (agentArgs == null || agentArgs.isEmpty()) {
			throw new IllegalArgumentException("Usage : -javaAgent:analyticaSpyAgent.jar=analyticaPlugs.json");
		}
		analyticaSpyConf = loadJsonConf(agentArgs);

		excludeMatcher = buildStringMatchers(analyticaSpyConf.getFastExcludedPackages(), false);
		includeMatcher = buildStringMatchers(analyticaSpyConf.getFastIncludedPackages(), true);

		for (final AnalyticaSpyHookPoint hookPoint : analyticaSpyConf.getHookPoints()) {
			classNameHookPointMatchers.put(new RegExpMatcher(hookPoint.getClassName()), hookPoint);
			if (hookPoint.getInherits() != null) {
				classHookPointMatchers.put(hookPoint, new InheritClassMatcher(hookPoint.getInherits()));
			}
			methodHookPointMatchers.put(hookPoint, buildCtBehaviorMatchers(hookPoint.getMethods(), true));
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

	/**
	 * @param className Nom de la classe
	 * @return si cette classe doit être instrumentée
	 */
	public boolean shouldTransform(final String className) {
		final String adaptedclassName = className.replace('/', '.');
		return lookForHookPoint(adaptedclassName) != null;
	}

	private AnalyticaSpyHookPoint lookForHookPoint(final String adaptedclassName) {
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

		final CtClass ctClass = obtainCtClass(adaptedclassName);
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

	/** {@inheritDoc} */
	public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
		final String adaptedclassName = className.replace('/', '.');
		final AnalyticaSpyHookPoint hookPoint = lookForHookPoint(adaptedclassName);
		if (hookPoint == null) {
			return null;
		}

		//Sinon on instrument
		final byte[] newClass = instrumentClass(adaptedclassName, hookPoint);
		return newClass;

	}

	private CtClass obtainCtClass(final String adaptedclassName) {
		final ClassPool pool = ClassPool.getDefault();
		final CtClass cl;
		try {
			cl = pool.get(adaptedclassName);
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

	private byte[] instrumentClass(final String adaptedclassName, final AnalyticaSpyHookPoint hookPoint) {
		System.out.println("Analytica instrument " + adaptedclassName + " with : " + hookPoint);
		final CtClass ctClass = obtainCtClass(adaptedclassName);
		try {
			final Matcher<CtBehavior> methodMatcher = methodHookPointMatchers.get(hookPoint);
			final ClassPool pool = ClassPool.getDefault();
			try {
				final CtClass clThrowable = pool.get("java.lang.Throwable");
				if (!ctClass.isInterface()) {
					//final String agentManagerDef = "private final com.kleegroup.analytica.agent.AgentManager agentManager = kasper.kernel.Home.getContainer().getManager(com.kleegroup.analytica.agent.AgentManager.class);";
					//final CtField agentManagerField = CtField.make(agentManagerDef, cl);
					//cl.addField(agentManagerField);

					final CtBehavior[] methods = ctClass.getDeclaredBehaviors();
					for (int i = 0; i < methods.length; i++) {
						if (methods[i].isEmpty() == false && methodMatcher.isMatch(methods[i])) {
							instrumentMethod(methods[i], clThrowable, hookPoint);
						}
					}
					return ctClass.toBytecode();
				} else {
					throw new InvalidClassException("Can't instrument interfaces");
				}
			} catch (final Exception e) {
				System.err.println("Could not instrument  " + adaptedclassName + ",  exception : " + e.getMessage());
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		} finally {
			ctClass.detach();
		}
	}

	private void instrumentMethod(final CtBehavior method, final CtClass clThrowable, final AnalyticaSpyHookPoint hookPoint) throws CannotCompileException {
		final String processType = hookPoint.getProcessType();
		final StringBuilder sbBefore = new StringBuilder();
		appendSubTypes(sbBefore, hookPoint, method.getName());
		final String agentManagerDef = "final com.kleegroup.analytica.agent.AgentManager agentManager = kasper.kernel.Home.getContainer().getManager(com.kleegroup.analytica.agent.AgentManager.class);";
		sbBefore.append(agentManagerDef);
		sbBefore.append("agentManager.startProcess(\"").append(processType).append("\", subTypes);");
		sbBefore.append("agentManager.setMeasure(\"ME_ERROR_PCT\", 0d);"); //le setMeasure surchargera si besoin
		//sbBefore.append("try {");
		final StringBuilder sbCatch = new StringBuilder();
		//sbCatch.append("} catch (final Throwable th) {");
		sbCatch.append(agentManagerDef);
		sbCatch.append("	agentManager.setMeasure(\"ME_ERROR_PCT\", 100d);");
		sbCatch.append("	agentManager.addMetaData(\"ME_ERROR_HEADER\", String.valueOf($e));");
		sbCatch.append("	throw $e;");
		final StringBuilder sbAfter = new StringBuilder();
		//sbAfter.append("} finally {");
		sbAfter.append(agentManagerDef);
		sbAfter.append("	agentManager.stopProcess();");
		//sbAfter.append("}");

		method.addCatch(sbCatch.toString(), clThrowable);
		method.insertBefore(sbBefore.toString());
		method.insertAfter(sbAfter.toString(), true); //second param : finally	
		System.out.println("Analytica instrument " + method.getLongName());

	}

	private void appendSubTypes(final StringBuilder sbBefore, final AnalyticaSpyHookPoint hookPoint, final String methodName) {
		sbBefore.append("String[] subTypes = new String[" + hookPoint.getSubTypes().size() + "];\n");
		int i = 0;
		for (final String subType : hookPoint.getSubTypes()) {
			sbBefore.append("subTypes[").append(i).append("] = ");
			if (subType.equals("$method")) {
				sbBefore.append("\"");
				sbBefore.append(methodName);
				sbBefore.append("\"");
			} else if (subType.startsWith("$")) {
				sbBefore.append(subType);
			} else {
				sbBefore.append("\"");
				sbBefore.append(subType);
				sbBefore.append("\"");
			}
			sbBefore.append(";\n");
			i++;
		}
	}

	//	private static final Properties loadProperties(final String propertiesFileName) {
	//		final Properties properties = new Properties();
	//		//---------------------------------------------------------------------
	//		try {
	//			final InputStream in = new FileInputStream(new File(propertiesFileName));
	//			try {
	//				properties.load(in);
	//			} finally {
	//				in.close();
	//			}
	//		} catch (final IOException e) {
	//			throw new IllegalArgumentException("Impossible de charger le fichier de configuration : " + propertiesFileName, e);
	//		}
	//		return properties;
	//	}

	private static final AnalyticaSpyConf loadJsonConf(final String configurationFileName) {
		try {
			//System.out.println("root : " + new File(configurationFileName).getAbsolutePath());
			final String confJson = readConf(new File(configurationFileName));
			final AnalyticaSpyConf conf = new Gson().fromJson(confJson, AnalyticaSpyConf.class);
			return conf;
		} catch (final Exception e) {
			throw new IllegalArgumentException("Impossible de charger le fichier de configuration : " + configurationFileName, e);
		}
	}

	private static String readConf(final File confFile) {
		final StringBuilder sb = new StringBuilder();
		try {
			//on lit le fichier
			final InputStream in = new FileInputStream(confFile);
			try {
				final Reader isr = new InputStreamReader(in);
				try {
					final BufferedReader br = new BufferedReader(isr);
					try {
						String currentLine;
						while ((currentLine = br.readLine()) != null) {
							sb.append(currentLine).append('\n');
						}
					} finally {
						br.close();
					}
				} finally {
					isr.close();
				}
			} finally {
				in.close();
			}
		} catch (final IOException e) {
			throw new RuntimeException("Erreur de lecture de la conf", e);
		}
		return sb.toString();
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
