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

import javax.servlet.http.HttpServletRequest;

/**
 * Util class for HttpSessions.
 * */
public class HttpSessionsUtil {
	
	/**
	 * This method allows to retrieve the browser name and version from the HttpServletRequest
	 * Here's an example of the return value: Firefox_39
	 * */
	public static final  String  getBrowserInfo(HttpServletRequest request)
	  {
		
	    String browsername = "Unkown";
	    String browserversion = "Unkown";
	    String browser = request.getHeader("User-Agent");
	    if (browser.contains("MSIE"))
	    {
	      String subsString = browser.substring(browser.indexOf("MSIE"));
	      String info[] = (subsString.split(";")[0]).split(" ");
	      browsername = info[0];
	      browserversion = info[1];
	    } else if (browser.contains("Firefox"))
	    {

	      String subsString = browser.substring(browser.indexOf("Firefox"));
	      String info[] = (subsString.split(" ")[0]).split("/");
	      browsername = info[0];
	      browserversion = info[1];
	    } else if (browser.contains("Chrome"))
	    {

	      String subsString = browser.substring(browser.indexOf("Chrome"));
	      String info[] = (subsString.split(" ")[0]).split("/");
	      browsername = info[0];
	      browserversion = info[1];
	    } else if (browser.contains("Opera"))
	    {

	      String subsString = browser.substring(browser.indexOf("Opera"));
	      String info[] = (subsString.split(" ")[0]).split("/");
	      browsername = info[0];
	      browserversion = info[1];
	    } else if (browser.contains("Safari"))
	    {

	      String subsString = browser.substring(browser.indexOf("Safari"));
	      String info[] = (subsString.split(" ")[0]).split("/");
	      browsername = info[0];
	      browserversion = info[1];
	    }
	    return browsername;
	  }

}
