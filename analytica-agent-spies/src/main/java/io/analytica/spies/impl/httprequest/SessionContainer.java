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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

public class SessionContainer {
	
	private final Map<String,HttpSession> sessions =  new ConcurrentHashMap<String,HttpSession>();
	private final Map<String, Set<String>>sessionTags = new ConcurrentHashMap<String, Set<String>>();
	
	public void addSession(HttpSession session){
		sessions.put(session.getId(), session);
		sessionTags.put(session.getId(), new HashSet<String>());
	}

	public void removeSession(final String sessionId){
		sessions.remove(sessionId);
		sessionTags.remove(sessionId);
	}
	
	public void addTag(final String sessionId, final String tagName, final String tagValue){
		if(sessionTags.containsKey(sessionId)){
			sessionTags.get(sessionId).add(getCompleteTagName(tagName, tagValue));
		}
	}
	private String getCompleteTagName(final String tagName, final String tagValue){
		return tagName+"_"+tagValue;
	}
	
	public boolean isEmpty(){
		return sessions.isEmpty();
	}
	public Map<String,Set<String>> getTags (){
		Map<String, Set<String>>clonedSessionsMetrics = new HashMap<String, Set<String>>();
		clonedSessionsMetrics.putAll(sessionTags);
		return clonedSessionsMetrics;
	}
}
