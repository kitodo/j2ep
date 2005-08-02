/*
 * Copyright 2005 Anders Nyman.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.j2ep.servers;

import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * A wrapper for requests that will create it's own set
 * of headers. The headers are the same except for 
 * cookies with a JSESSIONID that has a mark for a specific
 * server. More information about this can be found in the 
 * ClusterServer
 * 
 * @author Anders Nyman
 * @see ClusterServer
 */
public class SessionRewritingRequestWrapper extends HttpServletRequestWrapper {
    
    /** 
     * The cookies for this request.
     */
    private Vector cookies;
    
    /** 
     * Regex to find session in cookies.
     */
    private static Pattern sessionPattern = Pattern.compile("(JSESSIONID=[^\\.\\s;]+)(\\.[^;\\s]+)", Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);
    
    /**
     * Constructor, will check all cookies if they include
     * JSESSIONID. If they do any extra information about
     * which server this session was created for is removed.
     * 
     * @param request The request we wrap.
     */
    public SessionRewritingRequestWrapper(HttpServletRequest request) {
        super(request);
        cookies = new Vector();
        
        Enumeration reqCookies = request.getHeaders("Cookie");
        while (reqCookies.hasMoreElements()) {
            String value = (String) reqCookies.nextElement();
            Matcher matcher = sessionPattern.matcher(value);
            matcher.replaceAll("$1");
            cookies.add(value);
        }
    }

    /**
     * Will return the default request's header unless we are requesting
     * a cookie. If it's a cookie we want we will use our own.
     * 
     * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
     */
    public String getHeader(String name) {
        if (name.toLowerCase().equals("cookie")) {
            return (String) cookies.firstElement();
        } else {
            return super.getHeader(name);
        }
    }
    
    /**
     * Will return the default request's headers unless we are requesting
     * a cookie. If it's a cookie we want we will use our own vector.
     * 
     * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
     */
    public Enumeration getHeaders(String name) {
        if (name.toLowerCase().equals("cookie")) {
            return cookies.elements();
        } else {
            return super.getHeaders(name);
        }
    }   
    
}