package org.openmrs.module.ips.rest.controller;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DatatypeService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ClobDatatypeStorage;
import org.openmrs.module.ips.api.InternationalPatientSummaryService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/patient")
public class InternationalPatientSummaryController extends BaseRestController {
	
	@Autowired
	ObsService obsService;
	
	@Autowired
	ConceptService conceptService;
	
	@Autowired
	PersonService personService;
	
	@Autowired
	private DatatypeService datatypeService;
	
	@Autowired
	InternationalPatientSummaryService internationalPatientSummaService;
	
	@RequestMapping(value = "/{uuid}/patientsummary", method = RequestMethod.GET)
	public List<String> getFile(@PathVariable("uuid") String uuid,
	        @RequestParam(required = false, defaultValue = "RAW_VIEW") String view, HttpServletResponse response)
	        throws Exception {
		
		return internationalPatientSummaService.getAllPatientIPS(uuid);
		
	}
	
	@RequestMapping(value = "/{uuid}/patientsummary", method = RequestMethod.PUT)
	public List<String> updateFile(@PathVariable("uuid") String uuid, @RequestBody String requestBody,
			@RequestParam(required = false, defaultValue = "RAW_VIEW") String view) throws Exception {

		String url = Context.getAdministrationService().getGlobalProperty("ips.url");
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

		UUID randomUuid = UUID.randomUUID();

		ClobDatatypeStorage clobData = new ClobDatatypeStorage();
		clobData.setUuid(randomUuid.toString());
		clobData.setValue(response.getBody());
		datatypeService.saveClobDatatypeStorage(clobData);

		Obs obs = new Obs();
		String ipsconcept = Context.getAdministrationService().getGlobalProperty("ips.concept",
				"11557d80-af43-4788-b30c-69bc60489814");
		obs.setConcept(conceptService.getConceptByUuid(ipsconcept));
		obs.setValueComplex(randomUuid.toString());
		obs.setObsDatetime(new Date());
		obs.setPerson(personService.getPersonByUuid(uuid));
		obsService.saveObs(obs, "ipsObs");

		return internationalPatientSummaService.getAllPatientIPS(uuid);

	}
}
