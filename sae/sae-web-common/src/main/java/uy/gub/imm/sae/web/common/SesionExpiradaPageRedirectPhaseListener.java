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

package uy.gub.imm.sae.web.common;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class SesionExpiradaPageRedirectPhaseListener implements javax.faces.event.PhaseListener {

	private static final long serialVersionUID = 1L;

	private static String SESION_EXPIRADA_OUTCOME = "sesion_expirada";
	
	private Logger logger = Logger.getLogger(SesionExpiradaPageRedirectPhaseListener.class);
	
	public SesionExpiradaPageRedirectPhaseListener() {
	}

	public void afterPhase(PhaseEvent event) {

		/*
		FacesContext fc = event.getFacesContext();
		if (! fc.getResponseComplete()) {
			try {
				ServletContext servletCtx = (ServletContext) fc.getExternalContext().getContext();
				fc.getExternalContext().redirect(servletCtx.getContextPath() + EXPIRED_SESSION_PAGE);
				fc.responseComplete();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}*/
		
		event.getFacesContext().getApplication().getNavigationHandler().handleNavigation(event.getFacesContext(), null, SESION_EXPIRADA_OUTCOME);
		event.getFacesContext().responseComplete();
	}

	public void beforePhase(PhaseEvent event) {

	}

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

}
