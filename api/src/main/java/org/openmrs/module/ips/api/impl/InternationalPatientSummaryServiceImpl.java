/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ips.api.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DatatypeService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ClobDatatypeStorage;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ips.InternationalPatientSummaryConstants;
import org.openmrs.module.ips.api.InternationalPatientSummaryService;
import org.openmrs.module.ips.api.dao.InternationalPatientSummaryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class InternationalPatientSummaryServiceImpl extends BaseOpenmrsService implements InternationalPatientSummaryService {
	
	@Autowired
	ObsService obsService;
	
	@Autowired
	ConceptService conceptService;
	
	@Autowired
	PersonService personService;
	
	@Autowired
	private DatatypeService datatypeService;
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService administrationService;
	
	InternationalPatientSummaryDao dao;
	
	UserService userService;
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setDao(InternationalPatientSummaryDao dao) {
		this.dao = dao;
	}
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	@Override
	public String getIPS(String uuid) throws Exception {
		
		Obs obs = getIPSObs(uuid);
		
		if (obs != null && obs.getValueComplex() == null) {
			throw new Exception("Obs is doesn't contain complex value");
		} else if (obs != null && obs.getValueComplex() != null) {
			String ipsClobUuid = obs.getValueComplex();
			ClobDatatypeStorage clobData = datatypeService.getClobDatatypeStorageByUuid(ipsClobUuid);
			return clobData != null ? clobData.getValue() : null;
		}
		
		return null;
	}
	
	@Override
	public void addPatientIPS(String uuid) throws Exception {

		String url = administrationService.getGlobalProperty(InternationalPatientSummaryConstants.IPS_URL_STRING);
		String preferredID = administrationService
				.getGlobalProperty(InternationalPatientSummaryConstants.IPS_PREFERRED_IDENTIFIER_TYPE_UUID);
		Patient p = Context.getPatientService().getPatientByUuid(uuid);

		PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierTypeByUuid(preferredID);

		List<PatientIdentifier> pis = Context.getPatientService().getPatientIdentifiers(null, Collections.singletonList(pit), null, Collections.singletonList(p),
				null);

		String patientID = !pis.isEmpty() ? pis.get(pis.size() - 1).getIdentifier() : null;

		RestTemplate restTemplate = new RestTemplate();

		org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String requestBody = String.format(
				"{ \"resourceType\": \"Parameters\", " +
						"\"parameter\": [{ " +
						"\"name\": \"identifier\", " +
						"\"valueIdentifier\": { " +
						"\"value\": \"%s\" " +
						"} }] }",
				patientID);

		HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

		Obs obs = getIPSObs(uuid);

		String clobUuid;
		ClobDatatypeStorage clobData;

		if (obs != null && obs.getValueComplex() != null) {
			clobUuid = obs.getValueComplex();
			clobData = datatypeService.getClobDatatypeStorageByUuid(clobUuid) != null
					? datatypeService.getClobDatatypeStorageByUuid(clobUuid)
					: new ClobDatatypeStorage();
		} else {
			clobUuid = UUID.randomUUID().toString();
			clobData = new ClobDatatypeStorage();
			clobData.setUuid(clobUuid);
		}

		clobData.setValue(response.getBody());
		datatypeService.saveClobDatatypeStorage(clobData);

		if (obs == null) {
			obs = new Obs();
			String ipsconcept = administrationService.getGlobalProperty(InternationalPatientSummaryConstants.IPS_CONCEPT);
			obs.setConcept(conceptService.getConceptByReference(ipsconcept));
			obs.setValueComplex(clobUuid);
			obs.setObsDatetime(new Date());
			obs.setPerson(personService.getPersonByUuid(uuid));
			obsService.saveObs(obs, "Create IPS");
		} else {
			obs.setObsDatetime(new Date());
			obsService.saveObs(obs, "Create IPS");
		}
	}
	
	private Obs getIPSObs(String uuid) {
		Person p = personService.getPersonByUuid(uuid);
		String ipsconcept = administrationService.getGlobalProperty(InternationalPatientSummaryConstants.IPS_CONCEPT);
		Concept c = conceptService.getConceptByReference(ipsconcept);
		List<Obs> observations = obsService.getObservationsByPersonAndConcept(p, c);
		Obs obs = !observations.isEmpty() ? observations.get(observations.size() - 1) : null;
		return obs;
	}
}
