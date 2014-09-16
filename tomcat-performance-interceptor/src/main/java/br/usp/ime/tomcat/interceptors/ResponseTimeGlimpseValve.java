/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.usp.ime.tomcat.interceptors;

import java.io.IOException;
import java.util.Timer;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

public class ResponseTimeGlimpseValve extends ValveBase {

	private long rtBuff = 0L;
	private int rtCounter = 0;
	private String glimpseHost;
	Timer uploadCheckerTimer = null;

	public void invoke(final Request request, Response response)
			throws IOException, ServletException {

		if (request == null)
			return;

		long t1 = System.currentTimeMillis();
		getNext().invoke(request, response);
		long t2 = System.currentTimeMillis();

		rtBuff += t2 - t1;
		rtCounter++;

		if ( rtCounter >= 4) {
			// Have at least one?
			long meanTime = 0;
			if (rtCounter > 0)
				meanTime = rtBuff / rtCounter;
			else
				return;

			String requestURI = request.getRequestURI();

			GlimpseProbe.getInstance("tcp://" + glimpseHost + ":61616")
					.reportQoSMetric("response_time", "" + meanTime,
							Utils.getServiceInstanceId(requestURI),
							request.getServerName());
			rtCounter = 0;
			rtBuff = 0;
		}
			
	}

	public String getGlimpseHost() {
		return glimpseHost;
	}

	public void setGlimpseHost(String glimpseHost) {
		this.glimpseHost = glimpseHost;
	}
}
