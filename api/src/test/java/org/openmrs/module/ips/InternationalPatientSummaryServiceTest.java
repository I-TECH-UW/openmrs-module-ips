/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ips;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DatatypeService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;
import org.openmrs.api.db.ClobDatatypeStorage;
import org.openmrs.module.ips.api.impl.InternationalPatientSummaryServiceImpl;

/**
 * This is a unit test, which verifies logic in InternationalPatientSummaryService. It doesn't
 * extend BaseModuleContextSensitiveTest, thus it is run without the in-memory DB and Spring
 * context.
 */
public class InternationalPatientSummaryServiceTest {
	
	@InjectMocks
	InternationalPatientSummaryServiceImpl basicModuleService;
	
	@Mock
	PersonService personService;
	
	@Mock
	ObsService obsService;
	
	@Mock
	ConceptService conceptService;
	
	@Mock
	private DatatypeService datatypeService;
	
	@Mock
	private AdministrationService administrationService;
	
	@Before
	public void setupMocks() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void getAllPatientIPS_shouldGetAllPatientIPS() throws Exception {

		Person person = new Person();
		person.setUuid("94dc952d-0803-41b6-aba9-3b85f96f27bf");
		when(personService.getPersonByUuid("94dc952d-0803-41b6-aba9-3b85f96f27bf")).thenReturn(person);

		List<Person> personList = new ArrayList<>();
		personList.add(person);
		when(administrationService.getGlobalProperty(InternationalPatientSummaryConstants.IPS_CONCEPT)).thenReturn("e8a59cac-04fd-42ac-9af1-85928b45c146");
		Concept c = new Concept();
		c.setUuid("e8a59cac-04fd-42ac-9af1-85928b45c146");
		c.isComplex();

		when(conceptService.getConceptByReference("e8a59cac-04fd-42ac-9af1-85928b45c146")).thenReturn(c);

		List<Concept> conceptList = new ArrayList<>();
		conceptList.add(c);

		Obs obs = new Obs();
		obs.setConcept(c);
		obs.setPerson(person);
		obs.setValueComplex("e8a59cac-04fd-42ac-9af1-85928b45c146");
		List<Obs> obsList = new ArrayList<>();
		obsList.add(obs);
		when(obsService.getObservations(personList, null, conceptList, null, null, null, null, 10, null, null, null,
				false)).thenReturn(obsList);

		ClobDatatypeStorage clobData = new ClobDatatypeStorage();
		clobData.setUuid("e8a59cac-04fd-42ac-9af1-85928b45c146");
		clobData.setValue("{'id':'patient'}");
		when(datatypeService.getClobDatatypeStorageByUuid("e8a59cac-04fd-42ac-9af1-85928b45c146")).thenReturn(clobData);

		List<String> output = basicModuleService.getAllPatientIPS("94dc952d-0803-41b6-aba9-3b85f96f27bf");

		assertThat(output.get(0), equalTo("{'id':'patient'}"));

	}
}
