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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import uy.gub.imm.sae.business.api.AgendarReservas;
import uy.gub.imm.sae.business.api.DepurarReservas;
import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.factories.BusinessLocator;

public class LookupLocalBusinessLocator implements BusinessLocator {

	protected Context getContext() throws ApplicationException {
		try {
			return new InitialContext();
		} catch (NamingException e) {
			throw new ApplicationException("-1", "Imposible crear InitialContext", e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T lookup(Class<T> clazz, String name) throws ApplicationException {
		
		try {
			Context ctx = getContext();
			Object object = ctx.lookup(name);
			
			return ((T)object);
			
	    } catch (ApplicationException e) {
	    	throw e;
	    } catch (NamingException e) {
	        throw new ApplicationException("-1", String.format("Imposible encontrar el EJB para %s", clazz.getName()), e);
	    } catch (Exception e) {
	    	throw new ApplicationException("-1", String.format("Imposible obtener contexto para invocar EJB %s", clazz.getName()), e);
		}
	}
	
	public AgendarReservas getAgendarReservas() throws ApplicationException {
		return lookup(AgendarReservas.class, SAEProfile.getInstance().EJB_AGENDAR_RESERVAS_JNDI);
	}

	public DepurarReservas getDepurarReservas() throws ApplicationException {
		return lookup(DepurarReservas.class, SAEProfile.getInstance().EJB_DEPURAR_RESERVAS_JNDI);
	}

	public Recursos getRecursos() throws ApplicationException {
		return lookup(Recursos.class, SAEProfile.getInstance().EJB_RECURSOS_JNDI);
	}

}
