package org.openmrs.module.ips.rest.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DatatypeService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ClobDatatypeStorage;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.response.IllegalRequestException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@RequestMapping(value = "/{uuid}/patientsummary", method = RequestMethod.GET)
	public List<String> getFile(@PathVariable("uuid") String uuid,
			@RequestParam(required = false, defaultValue = "RAW_VIEW") String view, HttpServletResponse response)
			throws Exception {

		List<String> ipStrings = new ArrayList<>();
		List<Person> whom = new ArrayList<>();
		Person p = personService.getPersonByUuid(uuid);
		whom.add(p);
		String ipsconcept = Context.getAdministrationService().getGlobalProperty("ips.concept",
				"11557d80-af43-4788-b30c-69bc60489814");
		Concept c = conceptService.getConceptByUuid(ipsconcept);
		List<Concept> concepts = new ArrayList<>();
		concepts.add(c);
		List<Obs> obsz = obsService.getObservations(whom, null, concepts, null, null, null, null, 5, null, null, null,
				false);

		for (Obs eachobs : obsz) {
			if (eachobs.getValueComplex() == null) {
				throw new IllegalRequestException("It is not a complex obs, thus have no data.");
			}

			String eachuuid = eachobs.getValueComplex();
			ClobDatatypeStorage clobData = datatypeService.getClobDatatypeStorageByUuid(eachuuid);
			String clobValue = clobData.getValue();
			ipStrings.add(clobValue);
		}

		return ipStrings;

	}
	
	@RequestMapping(value = "/{uuid}/patientsummary", method = RequestMethod.PUT)
	public ResponseEntity<String> updateFile(@PathVariable("uuid") String uuid, @RequestBody byte[] updatedData,
	        @RequestParam(required = false, defaultValue = "RAW_VIEW") String view) throws Exception {
		
		//String url = "https://dd25f6cb-a999-43b0-b07a-b426bd9d0dc3.mock.pstmn.io/Patient/" + uuid + "/$summary";
		//To do: pass in the patient ID
		String url = Context.getAdministrationService().getGlobalProperty("ips.url",
		    "https://dd25f6cb-a999-43b0-b07a-b426bd9d0dc3.mock.pstmn.io/Patient/")
		        + 1234 + "/$summary";
		
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		
		UUID randomUuid = UUID.randomUUID();
		
		// create clob
		ClobDatatypeStorage clobData = new ClobDatatypeStorage();
		clobData.setUuid(randomUuid.toString());
		clobData.setValue(response.getBody());
		datatypeService.saveClobDatatypeStorage(clobData);
		// create obs
		
		Obs obs = new Obs();
		String ipsconcept = Context.getAdministrationService().getGlobalProperty("ips.concept",
		    "11557d80-af43-4788-b30c-69bc60489814");
		obs.setConcept(conceptService.getConceptByUuid(ipsconcept));
		
		obs.setValueComplex(randomUuid.toString());
		obs.setObsDatetime(new Date());
		obs.setPerson(personService.getPersonByUuid(uuid));
		obs = obsService.saveObs(obs, "url");
		
		return response;
		
	}
	
}
