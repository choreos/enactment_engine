package br.usp.ime.tomcat.interceptors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static String getServiceInstanceId(String requestURI) {
	String regex = "((/)([0-9a-zA-Z[-_]]+))";
	Pattern pattern = Pattern.compile(regex);

	Matcher matcher = pattern.matcher(requestURI);

	System.out.println(matcher.groupCount());

	if (matcher.find()) {
	    for (int i = 1; i <= matcher.groupCount(); i++)
		System.out.println(matcher.group(i));
	    return matcher.group(3);
	}
	return requestURI;
    }
}
