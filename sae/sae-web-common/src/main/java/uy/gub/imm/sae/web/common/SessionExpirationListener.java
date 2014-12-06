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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SessionExpirationListener implements PhaseListener{ 
	
	private static final long serialVersionUID = 1L;

	public void beforePhase(PhaseEvent event) { 
		
		FacesContext facesCtx = event .getFacesContext(); 
		ExternalContext extCtx = facesCtx .getExternalContext(); 
		HttpSession session = (HttpSession)extCtx .getSession(false); 
		
		boolean sessionExpired = session == null; 
		boolean postback = !extCtx .getRequestParameterMap().isEmpty(); 
		boolean timedout = postback && sessionExpired; 
		
		if(timedout) {
			try {
				// en caso de sesion expirada, redirigir a login.jsp
				
				extCtx.redirect(extCtx.getRequestContextPath() + "");
				//((HttpServletResponse)extCtx.getResponse()).sendRedirect("http://www.google.com");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
		
	} 
	
	public void afterPhase(PhaseEvent event) {} 
	
	public PhaseId getPhaseId() { 
		return PhaseId.RESTORE_VIEW; 
	} 	
}
