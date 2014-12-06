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

package uy.gub.imm.sae.web.mbean.administracion;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;

import uy.gub.imm.sae.business.api.Agendas;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.entity.TextoAgenda;
import uy.gub.imm.sae.web.common.BaseMBean;

public class TextoAgendaMBean  extends BaseMBean{

	public static final String MSG_ID = "pantalla";

	
	@EJB(name="ejb/AgendasBean")
	private Agendas agendasEJB;

	public SessionMBean sessionMBean;
	public TextoAgenda textoAux = new TextoAgenda();
	
	
	public TextoAgenda getTextoAux() {
		return textoAux;
	}
	public void setTextoAux(TextoAgenda textoAux) {
		this.textoAux = textoAux;
	}
	public SessionMBean getSessionMBean() {
		return sessionMBean;
	}
	public void setSessionMBean(SessionMBean sessionMBean) {
		this.sessionMBean = sessionMBean;
	}

	@PostConstruct
	public void initTextoAg(){
		
		if (agendasEJB == null) agendasEJB = (Agendas)lookupEJB(SAEProfile.getInstance().EJB_AGENDAS_JNDI);

		
		//Se controla que se haya Marcado una agenda para trabajar con los textos
		if (sessionMBean.getAgendaSeleccionada() == null){
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
		}
		else
		{
			textoAux.setAgenda(sessionMBean.getAgendaSeleccionada());
			textoAux.setTextoPaso1(sessionMBean.getAgendaSeleccionada().getTextoAgenda().getTextoPaso1());
			textoAux.setTextoPaso2(sessionMBean.getAgendaSeleccionada().getTextoAgenda().getTextoPaso2());
			textoAux.setTextoPaso3(sessionMBean.getAgendaSeleccionada().getTextoAgenda().getTextoPaso3());
			textoAux.setTextoSelecRecurso(sessionMBean.getAgendaSeleccionada().getTextoAgenda().getTextoSelecRecurso());
			textoAux.setTextoTicketConf(sessionMBean.getAgendaSeleccionada().getTextoAgenda().getTextoTicketConf());
		}
	}
	/**************************************************************************/
	/*                   Action  de Texto Agenda  (navegación)                 */	
	/**************************************************************************/	
	
	
	public void guardar(ActionEvent e) {
		
		if (sessionMBean.getAgendaSeleccionada() != null) {
 			try {
 				sessionMBean.getAgendaSeleccionada().getTextoAgenda().setTextoPaso1(textoAux.getTextoPaso1());
 				sessionMBean.getAgendaSeleccionada().getTextoAgenda().setTextoPaso2(textoAux.getTextoPaso2());
 				sessionMBean.getAgendaSeleccionada().getTextoAgenda().setTextoPaso3(textoAux.getTextoPaso3());
 				sessionMBean.getAgendaSeleccionada().getTextoAgenda().setTextoSelecRecurso(textoAux.getTextoSelecRecurso());
 				sessionMBean.getAgendaSeleccionada().getTextoAgenda().setTextoTicketConf(textoAux.getTextoTicketConf());
				agendasEJB.modificarAgenda(sessionMBean.getAgendaSeleccionada());
				addInfoMessage(getI18N().getText("agendas.modified"), MSG_ID); 
 			} catch (Exception ex) {
 				addErrorMessage(ex, MSG_ID);
 			}
		}
		else {
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
		}
		
	}
	
	public void cancelar(ActionEvent e) {
		if (sessionMBean.getAgendaSeleccionada() == null){
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
		}
		else
		{
			textoAux.setTextoPaso1(sessionMBean.getAgendaSeleccionada().getTextoAgenda().getTextoPaso1());
			textoAux.setTextoPaso2(sessionMBean.getAgendaSeleccionada().getTextoAgenda().getTextoPaso2());
			textoAux.setTextoPaso3(sessionMBean.getAgendaSeleccionada().getTextoAgenda().getTextoPaso3());
			textoAux.setTextoSelecRecurso(sessionMBean.getAgendaSeleccionada().getTextoAgenda().getTextoSelecRecurso());
			textoAux.setTextoTicketConf(sessionMBean.getAgendaSeleccionada().getTextoAgenda().getTextoTicketConf());
		}
	}

	
	public Agendas getAgendasEJB() {
		return agendasEJB;
	}
	public void setAgendasEJB(Agendas agendasEJB) {
		this.agendasEJB = agendasEJB;
	}
	
	public void beforePhaseModifTexto(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("agendas.texts_update.title"));
		}
	}

}
