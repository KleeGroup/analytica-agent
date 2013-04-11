package com.kleegroup.analyticaimpl.spies.javassist;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import kasper.kernel.exception.KRuntimeException;

import com.kleegroup.analytica.hcube.dimension.WhatDimension;
import com.kleegroup.analyticaimpl.spies.javassist.matcher.CompositeMatcher;
import com.kleegroup.analyticaimpl.spies.javassist.matcher.Matcher;

/**
 * Implémentation de ClassFileTransformer pour instrumenter les méthodes.
 * Necessite d'être placé dans un jar <b>avec</b> les class de Javassit, car il n'y a pas de gestion de classpath dans le javaagent
 * @author npiedeloup
 * @version $Id: MemoryLeakTransformer.java,v 1.1 2011/05/12 10:16:05 prahmoune Exp $
 */
final class AnalyticaSpyTransformer implements ClassFileTransformer {

	private final Matcher agentIncludeMatcher;
	private final Matcher agentExcludeMatcher;

	/**
	 * @param agentArgs parametres du javaagent de ligne de commande
	 */
	AnalyticaSpyTransformer(final String agentArgs) {
		//Assertion.notEmpty(agentArgs); //pas Asssertion pour eviter une dépendance
		if (agentArgs == null || agentArgs.isEmpty()) {
			throw new IllegalArgumentException("Usage : -javaAgent:analyticaSpyAgent.jar=analyticaPlugs.properties");
		}
		final Properties properties = loadProperties(agentArgs);
		//TODO : gérer l'inclusion de méthodes spécifique
		//exemple properties
		//exclude=sun.,java.,javax.
		//java.sql.Statement=executeQuery #Statement est une interface et l'on veut instrumenter les implé !!

		final List<String> includePatterns = extractIncludes(properties);
		final List<String> excludePatterns = extractExcludes(properties);

		agentIncludeMatcher = CompositeMatcher.buildCompositeGlobMatcher(includePatterns);
		agentExcludeMatcher = CompositeMatcher.buildCompositeGlobMatcher(excludePatterns);
	}

	private List<String> extractExcludes(final Properties properties) {
		final List<String> patterns = new ArrayList<String>();
		for (final Object key : properties.keySet()) {
			final boolean keyStartsWithExclude = ((String) key).toLowerCase().startsWith("exclude");
			if (keyStartsWithExclude) {
				for (final String pattern : properties.getProperty((String) key).split(",")) {
					patterns.add(pattern.trim());
				}
			}
		}
		return patterns;
	}

	private List<String> extractIncludes(final Properties properties) {
		final List<String> patterns = new ArrayList<String>();
		for (final Object key : properties.keySet()) {
			final boolean keyStartsWithExclude = ((String) key).toLowerCase().startsWith("exclude");
			final boolean keyStartsWithInclude = ((String) key).toLowerCase().startsWith("include");
			if (keyStartsWithInclude) {
				//Soit on veut les valeurs de properties qui "commencent par" include
				for (final String pattern : properties.getProperty((String) key).split(",")) {
					patterns.add(pattern.trim());
				}
			} else if (!keyStartsWithExclude) {
				//Soit on veut les clés de properties qui "ne commencent pas par" include ou exclude
				patterns.add(((String) key).trim());
			}
		}
		return patterns;
	}

	/** {@inheritDoc} */
	public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
		//Test d'exclusions
		if (isIgnoredClass(className)) {
			// @see ClassFileTransformer : Returning null means that no transformation was done.
			return null;
		}
		//Test de non inclusions
		if (!matchPattern(className)) {
			// @see ClassFileTransformer : Returning null means that no transformation was done.
			return null;
		}

		return instrumentClass(className.replace('/', '.'), classBeingRedefined, classfileBuffer);
	}

	private boolean isIgnoredClass(final String className) {
		return agentExcludeMatcher.isMatch(className.replace('/', '.'));
	}

	private boolean matchPattern(final String className) {
		return agentIncludeMatcher.isMatch(className.replace('/', '.'));
	}

	private byte[] instrumentClass(final String className, final Class classBeingRedefined, final byte[] classfileBuffer) {
		final ClassPool pool = ClassPool.getDefault();
		final CtClass cl;
		final CtClass clThrowable;
		try {
			cl = pool.get(className);
			clThrowable = pool.get("java.lang.Throwable");
		} catch (final NotFoundException e) {
			System.err.println("Could not instrument  " + className + ",  exception : " + e.getMessage());
			throw new KRuntimeException(e);
		}
		try {
			if (!cl.isInterface()) {
				final String agentManagerDef = "private final com.kleegroup.analytica.agent.AgentManager agentManager = kasper.kernel.Home.getContainer().getManager(com.kleegroup.analytica.agent.AgentManager.class);";
				final CtField agentManagerField = CtField.make(agentManagerDef, cl);
				cl.addField(agentManagerField);

				final CtBehavior[] methods = cl.getDeclaredBehaviors();
				for (int i = 0; i < methods.length; i++) {
					if (methods[i].isEmpty() == false) {
						instrumentMethod(methods[i], clThrowable);
					}
				}
				return cl.toBytecode();
			} else {
				throw new InvalidClassException("Can't instrument interfaces");
			}
		} catch (final Exception e) {
			System.err.println("Could not instrument  " + className + ",  exception : " + e.getMessage());
			e.printStackTrace();
			throw new KRuntimeException(e);
		} finally {
			cl.detach();
		}
	}

	private void instrumentMethod(final CtBehavior method, final CtClass clThrowable) throws NotFoundException, CannotCompileException {
		final StringBuilder sbBefore = new StringBuilder();
		sbBefore.append("agentManager.startProcess(\"JAVASSIST\", \"" + method.getDeclaringClass().getName() + WhatDimension.SEPARATOR + method.getName() + "\");");
		//sbBefore.append("try {");
		final StringBuilder sbCatch = new StringBuilder();
		//sbCatch.append("} catch (final Throwable th) {");
		sbCatch.append("	agentManager.setMeasure(\"ME_ERROR_PCT\", 100d);");
		sbCatch.append("	agentManager.addMetaData(\"ME_ERROR_HEADER\", String.valueOf($e));");
		sbCatch.append("	throw $e;");
		final StringBuilder sbAfter = new StringBuilder();
		//sbAfter.append("} finally {");
		sbAfter.append("	agentManager.stopProcess();");
		//sbAfter.append("}");

		method.addCatch(sbCatch.toString(), clThrowable);
		method.insertBefore(sbBefore.toString());
		method.insertAfter(sbAfter.toString(), true); //second param : finally		  
	}

	private static final Properties loadProperties(final String propertiesFileName) {
		final Properties properties = new Properties();
		//---------------------------------------------------------------------
		try {
			final InputStream in = new FileInputStream(new File(propertiesFileName));
			try {
				properties.load(in);
			} finally {
				in.close();
			}
		} catch (final IOException e) {
			throw new IllegalArgumentException("Impossible de charger le fichier de configuration : " + propertiesFileName, e);
		}
		return properties;
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
