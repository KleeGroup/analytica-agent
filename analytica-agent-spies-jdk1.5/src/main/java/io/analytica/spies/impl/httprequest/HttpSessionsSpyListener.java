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
 */

package io.analytica.spies.impl.httprequest;

import io.analytica.agent.impl.AnalyticaConfigurationStore;
import io.analytica.agent.impl.AnalyticaConfigurationType;
import io.analytica.agent.impl.KProcessCollectorContainer;
import io.analytica.api.KMeasureType;
import io.analytica.api.KProcess;
import io.analytica.api.KProcessBuilder;
import io.analytica.api.KProcessType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * This is the java 5 implementation of the HttpSessionsSpyListener. For java 7 or above use the project analytica-agent-api
 * HttpSessionsSpyListener is a Spy capable of generating data retrievable from the sessions.In comparison to other spy's implementation 
 * you can only store tags, but no metrics. Theses tags are set once, after the session was created. Only the data that doesn't change 
 * during the session's life should be stored.
 * 
 * The default tags that are implemented are :
 * 		- client's web browser (and version)
 * 		
 * You can add your own specific business tags in the method getSpecificTags by extending this class. 
 * Here are some ideas for specific business tags:
 * 		- user's profile
 *		- user's location (or region)
 *		- user's language 
 *
 * The data is stored in the HttpSessionContainer. 
 * Periodically ,The data is transformed in a KProcess object and sent to the Analytica's Server using the KProcessCollectorContainer singleton.
 * The tags are then transformed in metrics. Each metric represents the number of sessions specific to each tag.
 * Here's an example:
 * [{SESSION_ALL:120},{BROWSER_Firefox_:40},{BROWSER_Chrome:40},{BROWSER_MSIE:40},{language_english:120},{region_CA:100},{region_NY:20}]
 * 
 * To Use this class in your application all you need to do is 
 * 		- declare it as a listner in your web.xml file.
 * 				<listener>
 *  				 <listener-class>io.analytica.spies.impl.httprequest.HttpSessionsSpyListener</listener-class>
 *				</listener>
 *		- set up the configuration necessary for KProcessCollectorContainer (if not already done)
 * */

public class HttpSessionsSpyListener implements ServletContextListener,
		HttpSessionListener, ServletRequestListener {
	 private SessionContainer sessionContainer;
	 private final Timer delayedStarter = new Timer("HttpSessionsManager", true);
	 private final SessionDataCollectorTask collectorTask = new SessionDataCollectorTask(); 
	 
	public final void requestInitialized(ServletRequestEvent sre) {
		HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
	    HttpSession session = request.getSession();
	    sessionContainer.addSession(session);
	  
	    for (Map.Entry<String,String> tag : getDefaultTags(request).entrySet()){
	    	 sessionContainer.addTag(session.getId(), tag.getKey(),tag.getValue());
	    }
	    
	    for (Map.Entry<String,String> tag : getSpecificTags(request).entrySet()){
	    	 sessionContainer.addTag(session.getId(), tag.getKey(),tag.getValue());
	    }
	}

	public final void sessionDestroyed(HttpSessionEvent se) {
		sessionContainer.removeSession(se.getSession().getId());

	}

	public final void contextInitialized(ServletContextEvent sce) {
		sessionContainer = new SessionContainer();
		collectorTask.run();
	}

	
	public final Map<String,String> getDefaultTags( HttpServletRequest request){
		Map<String,String> defaultTags = new HashMap<String, String>();
		//browser
		defaultTags.put(KMeasureType.BROWSER.toString(), HttpSessionsUtil.getBrowserInfo(request));
		return defaultTags;
	}
	
	public Map<String,String> getSpecificTags( HttpServletRequest event){
		return Collections.emptyMap();
	}

	public final void contextDestroyed(ServletContextEvent sce) {
		//No implementation
	}
	
	public final void requestDestroyed(ServletRequestEvent sre) {
		//No implementation
	}
	
	public final void sessionCreated(HttpSessionEvent se) {
		//No implementation
	}
	protected final KProcess encodeSessionData(){
		if(sessionContainer.isEmpty()){
			return null;
		}
		
		KProcessBuilder builder = new KProcessBuilder(KProcessCollectorContainer.getInstance().getAppName(), KProcessType.SESSION.toString());
		Map<String, Set<String>> tagsSessions = sessionContainer.getTags();
		for(Map.Entry<String, Set<String>> tags :tagsSessions.entrySet()){
			for(String tag : tags.getValue()){
				builder.incMeasure(tag, 1);
			}
			builder.incMeasure(KMeasureType.SESSION_ALL.toString(), 1);
		}
		builder.withLocation(KProcessCollectorContainer.getInstance().getLocation())
				.withCategory(KProcessType.SESSION.toString());
		return builder.build();
	}
	
	
	protected final class SessionDataCollectorTask extends TimerTask {
		/** {@inheritDoc} */
		@Override
		public void run() {
			if(!sessionContainer.isEmpty()){
				KProcessCollectorContainer.getInstance().add(encodeSessionData());
			}
			delayedStarter.schedule(new SessionDataCollectorTask(), ((Long) AnalyticaConfigurationStore.getInstace().getConfiguration(AnalyticaConfigurationType.ANALYTICA_SPY_SESSION_FREQUENCY_SECONDS))*1000);
		}
	}

}
