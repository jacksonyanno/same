/*
 * SAME - Sistema de Gestion de Turnos por Internet
 * SAME is a fork of SAE - Sistema de Agenda Electronica
 * 
 * Copyright (C) 2009  IMM - Intendencia Municipal de Montevideo
 * Copyright (C) 2013, 2014  SAGANT - Codestra S.R.L.
 * Copyright (C) 2013, 2014  Alvaro Rettich <alvaro@sagant.com>
 * Copyright (C) 2013, 2014  Carlos Gutierrez <carlos@sagant.com>
 * Copyright (C) 2013, 2014  Victor Dumas <victor@sagant.com>
 *
 * This file is part of SAME.

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

package uy.gub.imm.sae.common.factories.ejb;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.exception.ApplicationException;

public class LookupAnonimoLocalBusinessLocator extends LookupLocalBusinessLocator {

	private static final String LOCATOR_CONTEXTO_NO_AUTENTICADO_USER_NAME_KEY="locator.contexto.no.autenticado.user.name";
	private static final String LOCATOR_CONTEXTO_NO_AUTENTICADO_USER_PASSWORD_KEY="locator.contexto.no.autenticado.user.password";
	
	protected String getUsuarioAnonimo() throws ApplicationException{
		
		String user = SAEProfile.getInstance().getProperties().getProperty(LOCATOR_CONTEXTO_NO_AUTENTICADO_USER_NAME_KEY); 
		if (user == null) {
			throw new ApplicationException("-1", 
					"El nombre del usuario anonimo no está configurado: " + 
					LOCATOR_CONTEXTO_NO_AUTENTICADO_USER_NAME_KEY + " es null");
		}
		return user;
	}
	
	protected String getPasswordUsuarioAnonimo(String usuario) throws ApplicationException{

		String password = SAEProfile.getInstance().getProperties().getProperty(LOCATOR_CONTEXTO_NO_AUTENTICADO_USER_PASSWORD_KEY); 
		if (password == null) {
			throw new ApplicationException("-1", 
					"La password del usuario anonimo no está configurada: " + 
					LOCATOR_CONTEXTO_NO_AUTENTICADO_USER_PASSWORD_KEY + " es null");
		}
		return password;

	}
	
	@Override
	protected Context getContext() throws ApplicationException {

		String usuario_anonimo = getUsuarioAnonimo();
		
		try {
			Properties prop = new Properties();
			prop.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.security.jndi.JndiLoginInitialContextFactory");
			prop.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
			prop.put(Context.SECURITY_PRINCIPAL, usuario_anonimo);
			prop.put(Context.SECURITY_CREDENTIALS, getPasswordUsuarioAnonimo(usuario_anonimo));			
			return new InitialContext(prop);
			
	    } catch (NamingException e) {
	    	throw new ApplicationException("-1", "Imposible crear contexto para usuario anonimo", e);
	    } catch (Exception e) {
			throw new ApplicationException("-1", "Imposible crear contexto para usuario anonimo", e);
		}
	}
}
