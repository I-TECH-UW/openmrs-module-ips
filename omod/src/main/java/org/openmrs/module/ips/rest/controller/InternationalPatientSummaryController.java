package org.openmrs.module.ips.rest.controller;

import javax.servlet.http.HttpServletResponse;

import org.openmrs.module.ips.api.InternationalPatientSummaryService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/patient")
public class InternationalPatientSummaryController extends BaseRestController {
	
	@Autowired
	InternationalPatientSummaryService internationalPatientSummaService;
	
	@RequestMapping(value = "/{uuid}/patientsummary", method = RequestMethod.GET)
	@ResponseBody
	public String getPatientSummary(@PathVariable("uuid") String uuid,
	        @RequestParam(required = false, defaultValue = "RAW_VIEW") String view, HttpServletResponse response)
	        throws Exception {
		return internationalPatientSummaService.getIPS(uuid);
		
	}
	
	@RequestMapping(value = "/{uuid}/patientsummary", method = RequestMethod.PUT)
	@ResponseBody
	public String updatePatientSummary(@PathVariable("uuid") String uuid, @RequestBody String requestBody,
	        @RequestParam(required = false, defaultValue = "RAW_VIEW") String view) throws Exception {
		internationalPatientSummaService.addPatientIPS(uuid);
		return internationalPatientSummaService.getIPS(uuid);
		
	}
}
