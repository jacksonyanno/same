/*
 * SAME - Sistema de Gestion de Turnos por Internet
 * SAME is a fork of SAE - Sistema de Agenda Electronica
 * 
 * Copyright (C) 2013, 2014  SAGANT - Codestra S.R.L.
 * Copyright (C) 2013, 2014  Alvaro Rettich <alvaro@sagant.com>
 * Copyright (C) 2013, 2014  Carlos Gutierrez <carlos@sagant.com>
 * Copyright (C) 2013, 2014  Victor Dumas <victor@sagant.com>
 *
 * This file is part of SAME.
 *
 * SAME is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sagant.sae.utils;

import java.util.Properties;


import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

//import org.jboss.ejb.client.ContextSelector;
//import org.jboss.ejb.client.EJBClientConfiguration;
//import org.jboss.ejb.client.EJBClientContext;
//import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
//import org.jboss.ejb.client.StatelessEJBLocator;
//import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;

//import uy.gub.imm.sae.business.ejb.facade.AgendarReservas;
//import uy.gub.imm.sae.business.ejb.facade.AgendarReservasRemote;
//import uy.gub.imm.sae.business.ejb.facade.DepurarReservas;
//import uy.gub.imm.sae.business.ejb.facade.Recursos;
//import uy.gub.imm.sae.common.SAEProfile;
//import uy.gub.imm.sae.common.factories.BusinessLocator;
import uy.gub.imm.sae.common.factories.ejb.LookupAnonimoLocalBusinessLocator;
import uy.gub.imm.sae.common.exception.ApplicationException;

public class LookupAnonimoLocalBusinessLocatorJboss7 extends LookupAnonimoLocalBusinessLocator {

//	private static final String LOCATOR_CONTEXTO_NO_AUTENTICADO_BACKEND_USER_NAME_KEY = "locator.contexto.no.autenticado.backend.user.name";
//	private static final String LOCATOR_CONTEXTO_NO_AUTENTICADO_BACKEND_USER_PASSWORD_KEY = "locator.contexto.no.autenticado.backend.user.password";

	
	/*
	protected String getUsuarioAnonimo() throws ApplicationException {

		String user = SAEProfile
				.getInstance()
				.getProperties()
				.getProperty(
						LOCATOR_CONTEXTO_NO_AUTENTICADO_BACKEND_USER_NAME_KEY);
		if (user == null) {
			throw new ApplicationException(
					"-1",
					"El nombre del usuario anonimo no está configurado: "
							+ LOCATOR_CONTEXTO_NO_AUTENTICADO_BACKEND_USER_NAME_KEY
							+ " es null");
		}
		return user;
	}

	protected String getPasswordUsuarioAnonimo(String usuario)
			throws ApplicationException {

		String password = SAEProfile
				.getInstance()
				.getProperties()
				.getProperty(
						LOCATOR_CONTEXTO_NO_AUTENTICADO_BACKEND_USER_PASSWORD_KEY);
		if (password == null) {
			throw new ApplicationException(
					"-1",
					"La password del usuario anonimo no está configurada: "
							+ LOCATOR_CONTEXTO_NO_AUTENTICADO_BACKEND_USER_PASSWORD_KEY
							+ " es null");
		}
		return password;

	}*/

	public LookupAnonimoLocalBusinessLocatorJboss7() throws ApplicationException {

		//String usuario_anonimo = getUsuarioAnonimo();

		/**
		 * Este codigo parace andar con el anonimo desde la reserva web pero no
		 * funciona desde el anonimo del StartapcontextListener del web del
		 * backend para inicar el timer de depurar reservas
		 * 
		 * SecurityClient client = SecurityClientFactory.getSecurityClient();
		 * client.setSimple(usuario_anonimo,
		 * getPasswordUsuarioAnonimo(usuario_anonimo)); client.login();
		 * Properties prop = new Properties();
		 * prop.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		 * return new InitialContext(prop);
		 */

		/**
		 * Fuente:
		 * http://www.mastertheboss.com/jboss-as-7/jboss-as-7-ejb-remote-
		 * clients-without-configuration-file
		 */
		/*
		Properties prop = new Properties();
		prop.put("endpoint.name", "client-endpoint");
		prop.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
		prop.put("remote.connections", "default");
		prop.put("remote.connection.default.port", "4447");
		prop.put("remote.connection.default.host", "localhost");
		prop.put("remote.connection.default.username", usuario_anonimo);
		prop.put("remote.connection.default.password", getPasswordUsuarioAnonimo(usuario_anonimo));
		//prop.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
		
		EJBClientConfiguration cc = new PropertiesBasedEJBClientConfiguration(prop);
		final ContextSelector<EJBClientContext> ejbClientContextSelector = new ConfigBasedEJBClientContextSelector(cc);
		// Now let's setup the EJBClientContext to use this selector
		@SuppressWarnings("unused")
		final ContextSelector<EJBClientContext> previousSelector = EJBClientContext.setSelector(ejbClientContextSelector);
	*/
	}
	
	
	
	@Override
	protected Context getContext() throws ApplicationException {
		
		String usuario_anonimo = getUsuarioAnonimo();

		SecurityClient client;
		try {
			client = SecurityClientFactory.getSecurityClient();
			client.setSimple(usuario_anonimo,
			getPasswordUsuarioAnonimo(usuario_anonimo)); client.login();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 
		Properties prop = new Properties();
//		prop.put("endpoint.name", "client-endpoint");
//		prop.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
//		prop.put("remote.connections", "default");
//		prop.put("remote.connection.default.port", "4447");
//		prop.put("remote.connection.default.host", "localhost");
//		prop.put("remote.connection.default.username", usuario_anonimo);
//		prop.put("remote.connection.default.password", getPasswordUsuarioAnonimo(usuario_anonimo));
		//prop.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
		prop.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		
//		Dependencies: org.jboss.xnio

		try {
			return new InitialContext(prop);
		} catch (NamingException e) {
			throw new ApplicationException(e);
		}
	}


/*
	@Override
	public AgendarReservas getAgendarReservas() throws ApplicationException {
		
		StatelessEJBLocator<AgendarReservasRemote> locator = new StatelessEJBLocator<AgendarReservasRemote>(
				AgendarReservasRemote.class, "ear", "module", "CalculatorRemoteBean", "");
		AgendarReservasRemote ejb = org.jboss.ejb.client.EJBClient.createProxy(locator);
		
		return ejb;
	}

	@Override
	public Recursos getRecursos() throws ApplicationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DepurarReservas getDepurarReservas() throws ApplicationException {
		// TODO Auto-generated method stub
		return null;
	}
*/
}
