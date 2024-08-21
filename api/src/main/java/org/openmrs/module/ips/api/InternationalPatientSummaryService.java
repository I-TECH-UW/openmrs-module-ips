/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ips.api;

import java.util.List;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.util.PrivilegeConstants;

/**
 * The main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 */
public interface InternationalPatientSummaryService extends OpenmrsService {
	
	@Authorized({ PrivilegeConstants.GET_OBS })
	List<String> getAllPatientIPS(String uuid) throws Exception;
	
	@Authorized({ PrivilegeConstants.ADD_OBS, PrivilegeConstants.GET_OBS })
	void addPatientIPS(String uuid) throws Exception;
	
}
