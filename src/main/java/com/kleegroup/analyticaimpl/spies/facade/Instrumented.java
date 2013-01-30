package com.kleegroup.analyticaimpl.spies.facade;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import kasper.kernel.lang.Intercept;

/**
 * Annotation à poser sur les méthodes des implémentations des services.
 * L'utilisation de cette annotation est optionnelle. 
 * Pour la pose systématique d'un intercepteur gérant l'instrumentation, utiliser un
 * aspect automatique.
 * 
 * @author npiedeloup
 * @version $Id: Instrumented.java,v 1.1 2011/05/12 10:16:12 npiedeloup Exp $
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Intercept
public @interface Instrumented {
	// vide
}
