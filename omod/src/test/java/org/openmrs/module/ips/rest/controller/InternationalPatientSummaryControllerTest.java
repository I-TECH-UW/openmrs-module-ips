package org.openmrs.module.ips.rest.controller;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

public class InternationalPatientSummaryControllerTest extends RestControllerTestUtils {
	
	@Test
	public void shouldReturnIpsWhenGivenPatientUUID() throws Exception {
		String requestURI = "/rest/" + RestConstants.VERSION_1 + "/patient/1234/patientsummary";
		MockHttpServletRequest request = new MockHttpServletRequest(RequestMethod.GET.toString(), requestURI);
		SimpleObject result = deserialize(handle(request));
		Assert.assertEquals("344", PropertyUtils.getProperty(result, "id"));
		
	}
}
