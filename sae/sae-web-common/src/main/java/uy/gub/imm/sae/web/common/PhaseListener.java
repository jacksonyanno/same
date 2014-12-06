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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class PhaseListener implements javax.faces.event.PhaseListener {

	private static final long serialVersionUID = 256452900505269628L;

	public PhaseListener() {
		// TODO Auto-generated constructor stub
	}

	public void afterPhase(PhaseEvent event) {

	
/*		FacesContext fc = event.getFacesContext();
		
		if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
			if (fc.getViewRoot() == null) {
				//No se pudo crear la vista, seguramente vencio la sesion.
				ExternalContext ec = event.getFacesContext().getExternalContext();
				try {
					ec.redirect("/SAE-Internet-WEB/index.xhtml");
					fc.responseComplete();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}*/
		
		if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
			HttpSession sesion = (HttpSession) event.getFacesContext().getExternalContext().getSession(false);
			if (sesion == null) {
				event.getFacesContext().getApplication().getNavigationHandler().handleNavigation(event.getFacesContext(), "", "agendaInvalida");
			}
		}
	}

	public void beforePhase(PhaseEvent event) {
		
		
		
	}
	public void beforePhase2(PhaseEvent pe) {

		FacesContext fc = pe.getFacesContext();
		ExternalContext ec = fc.getExternalContext();
	    HttpSession session = (HttpSession)ec.getSession(false);
		if(session == null) {
			// session already timed out and destroyed. redirect to home page.
			// this will cause a login.
			
			String contextRoot;
			String contextPath = ((HttpServletRequest)ec.getRequest()).getContextPath();
			int i = contextPath.indexOf("/", 1);
			if (i != -1) {
				contextRoot = contextPath.substring(0, i-1);
			}
			else {
				contextRoot = contextPath;
			}
			String homePage = contextRoot + "/index.xhtml";
			
			if (!homePage.equals(contextPath)) {
				try {
					ec.redirect(homePage);
				} catch(Exception e) {
					e.printStackTrace();
					// this should never happen
				}
			}
		}
	}

	public PhaseId getPhaseId() {
		
		return PhaseId.ANY_PHASE;
	}

}
