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

package io.analytica.agent.impl;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class AnalyticaConfigurationStore {
	public static final String DEFAULT_APP_NAME="DummyApplication";
	public static final String DEFAULT_LOCATION="DummyLocation";
	public static final String DEFAULT_CONNECTOR_TYPE="DummyConnector";
	public static final String DEFAULT_CONNECTOR_FILE_FILE_NAME="DummyFileName";
	public static final String DEFAULT_CONNECTOR_REMOTE_SERVER_URL="DummyServerUrl";
	public static final int DEFAULT_CONNECTOR_REMOTE_SEND_PAQUET_SIZE=1;
	public static final int DEFAULT_CONNECTOR_REMOTE_SEND_PAQUET_FREQUENCY_SECONDS=1;
	public static final int DEFAULT_ANALYTICA_SPY_SESSION_FREQUENCY_SECONDS=Integer.MAX_VALUE;
	private static AnalyticaConfigurationStore INSTANCE = new AnalyticaConfigurationStore();
	
	private final Map<AnalyticaConfigurationType,Object> configurations;
	
	private AnalyticaConfigurationStore(){
		configurations=new HashMap<AnalyticaConfigurationType,Object>();
		configurations.put(AnalyticaConfigurationType.ANALYTICA_APP_NAME, DEFAULT_APP_NAME);
		configurations.put(AnalyticaConfigurationType.ANALYTICA_LOCATION, DEFAULT_LOCATION);
		configurations.put(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_TYPE, DEFAULT_CONNECTOR_TYPE);
		configurations.put(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_FILE_FILE_NAME, DEFAULT_CONNECTOR_FILE_FILE_NAME);
		configurations.put(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_REMOTE_SERVER_URL, DEFAULT_CONNECTOR_REMOTE_SERVER_URL);
		configurations.put(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_REMOTE_SEND_PAQUET_SIZE, DEFAULT_CONNECTOR_REMOTE_SEND_PAQUET_SIZE);
		configurations.put(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_REMOTE_SEND_PAQUET_FREQUENCY_SECONDS, DEFAULT_APP_NAME);
		configurations.put(AnalyticaConfigurationType.ANALYTICA_SPY_SESSION_FREQUENCY_SECONDS, DEFAULT_ANALYTICA_SPY_SESSION_FREQUENCY_SECONDS);
		
		try {

			Context initCtx = new InitialContext();
			Context context = (Context) initCtx.lookup("java:comp/env");
			try {
				configurations.put(AnalyticaConfigurationType.ANALYTICA_APP_NAME, 
						 context.lookup(AnalyticaConfigurationType.ANALYTICA_APP_NAME.toString()));
			} catch (final NamingException e) {
				//
			}
			
			try {
			configurations.put(AnalyticaConfigurationType.ANALYTICA_LOCATION, 
					 context.lookup(AnalyticaConfigurationType.ANALYTICA_LOCATION.toString()));
			} catch (final NamingException e) {
				//
			}
			try {
			configurations.put(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_TYPE, 
					 context.lookup(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_TYPE.toString()));
			} catch (final NamingException e) {
				//	
			}
		
			try {
			configurations.put(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_FILE_FILE_NAME, 
					 context.lookup(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_FILE_FILE_NAME.toString()));
			} catch (final NamingException e) {
				//
			}
			
			try {
			configurations.put(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_REMOTE_SERVER_URL, 
					 context.lookup(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_REMOTE_SERVER_URL.toString()));
			} catch (final NamingException e) {
				//
			}
			
			try {
			configurations.put(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_REMOTE_SEND_PAQUET_SIZE, 
					 context.lookup(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_REMOTE_SEND_PAQUET_SIZE.toString()));
			} catch (final NamingException e) {
				//
			}
			
			try {
			configurations.put(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_REMOTE_SEND_PAQUET_FREQUENCY_SECONDS, 
					 context.lookup(AnalyticaConfigurationType.ANALYTICA_CONNECTOR_REMOTE_SEND_PAQUET_FREQUENCY_SECONDS.toString()));
			} catch (final NamingException e) {
				//
			}
			
			try {
			configurations.put(AnalyticaConfigurationType.ANALYTICA_SPY_SESSION_FREQUENCY_SECONDS, 
					 context.lookup(AnalyticaConfigurationType.ANALYTICA_SPY_SESSION_FREQUENCY_SECONDS.toString()));
			} catch (final NamingException e) {
				//
			}
		} catch (final NamingException e) {
			//
		}
	}
	
	public static AnalyticaConfigurationStore getInstance(){
		return INSTANCE;
	}
	
	public synchronized Object getConfiguration (AnalyticaConfigurationType configurationType){
		return this.configurations.get(configurationType);
	}
}
