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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DatatypeService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;
import org.openmrs.api.UserService;
import org.openmrs.api.db.ClobDatatypeStorage;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ips.api.InternationalPatientSummaryService;
import org.openmrs.module.ips.api.dao.InternationalPatientSummaryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

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
	public List<String> getAllPatientIPS(String uuid) throws Exception {
		List<String> ipStrings = new ArrayList<>();
		List<Person> whom = new ArrayList<>();
		Person p = personService.getPersonByUuid(uuid);
		whom.add(p);
		String ipsconcept = administrationService.getGlobalProperty("ips.concept");
		Concept c = conceptService.getConceptByUuid(ipsconcept);
		List<Concept> concepts = new ArrayList<>();
		concepts.add(c);
		List<Obs> obs = obsService.getObservations(whom, null, concepts, null, null, null, null, 10, null, null, null,
				false);

		for (Obs eachobs : obs) {
			if (eachobs.getValueComplex() == null) {
				throw new Exception("Obs doesn't contain complex value");
			}

			String eachuuid = eachobs.getValueComplex();
			ClobDatatypeStorage clobData = datatypeService.getClobDatatypeStorageByUuid(eachuuid);
			String clobValue = clobData.getValue();
			ipStrings.add(clobValue);
		}
		return ipStrings;
	}
}
