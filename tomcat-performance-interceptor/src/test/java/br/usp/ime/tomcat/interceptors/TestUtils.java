package br.usp.ime.tomcat.interceptors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestUtils {

    @Test
    public void test() {
	String uri = "/11111111-1111-1111-1111-111111111111/airline";
	String uri1 = "/12345678-1234-5678-9101-121314151617/airline";
	String uri2 = "/airline/airline";
	String uri3 = "/service1";
	String uri4 = "/context1/service1/instance2";

	assertEquals("11111111-1111-1111-1111-111111111111", Utils.getServiceInstanceId(uri));
	assertEquals("12345678-1234-5678-9101-121314151617", Utils.getServiceInstanceId(uri1));
	assertEquals("airline", Utils.getServiceInstanceId(uri2));
	assertEquals("service1", Utils.getServiceInstanceId(uri3));
	assertEquals("context1", Utils.getServiceInstanceId(uri4));

    }
}
