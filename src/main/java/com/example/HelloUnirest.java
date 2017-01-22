/**
Copyright IBM Corp. 2017

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.example;

import com.google.gson.Gson;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * Demonstrates how to use the Unirest library with GSON deserialization.
 *
 * @author Andy Dingsor 2017-0120
 */
public class HelloUnirest {
	private static final Logger logger = Logger.getLogger(HelloUnirest.class);

    /**
     * Helper method logs an error message and exits the JVM.
     */
    private static void throwUp(String message) throws Exception {
        logger.log(Level.ERROR, message);
        throw new Exception(message);
    }

    /**
     * Helper method checks the http status code in a response.
     */
    private static void verifyStatusCode(int expected, HttpResponse httpResponse) throws Exception {
        int actual = httpResponse.getStatus();
        if (expected == actual) {
            logger.log(Level.INFO, "Received expected status code. statusCode=" + actual + ".");
        } else {
            throwUp("Did not receive expected statusCode. Expected=" + expected + ". Actual=" + actual + ".");
        }
    }

    /**
     * Bean class represents a response from http://httpbin.org/ip
     */
    public static class IPBean {
        private String origin;
        public IPBean() {}
        public String getOrigin() { return origin; }
        public String toString() { return "IPBean: origin=" + origin; }
    }

    /**
     * The test starts here.
     */
    public static void main(String[] args) throws Exception {
        logger.log(Level.INFO, "Entry.");

        // Define a class to serialize and deserialize objects.
        // Reference http://unirest.io/java.html -> "setObjectMapper".
        // Note this is a one-time step per JVM and applies globally.
        Unirest.setObjectMapper(new ObjectMapper() {
            private Gson gson = new Gson();

            public <T> T readValue(String string, Class<T> beanClass) {
                try {
                    return gson.fromJson(string, beanClass);
                }
                catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
            public String writeValue(Object object) {
                try {
                    return gson.toJson(object);
                }
                catch(Exception exception) {
                    throw new RuntimeException(exception);
                }
            }
        });

        // Issue a GET request to well-known test site 'httpbin.org'
        HttpResponse<IPBean> httpResponse =
            Unirest.get("http://httpbin.org/ip").asObject(IPBean.class);

        // Verify status code.
        verifyStatusCode(200, httpResponse);

        // Verify response contents.
        IPBean ipBean = (IPBean)httpResponse.getBody();
        logger.log(Level.INFO, ipBean);

        String localHost = InetAddress.getLocalHost().getHostAddress();
        String origin = ipBean.getOrigin();
        if (-1 != origin.indexOf(localHost)) {
            logger.log(Level.INFO, "Local host '" + localHost + "' is included in the response.");
        } else {
            throwUp("Local host is not included in response. localHost=" + localHost + " origin=" + origin);
        }

        logger.log(Level.INFO, "Exit.");
    }
}
