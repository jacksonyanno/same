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

import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.entity.TextoRecurso;
import uy.gub.imm.sae.web.common.BaseMBean;

public class TextoRecursoMBean  extends BaseMBean {

	public static final String MSG_ID = "pantalla";


	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;
	private SessionMBean sessionMBean;
	private TextoRecurso textoRec = new TextoRecurso();
	
	
	public Recursos getRecursosEJB() {
		return recursosEJB;
	}
	public void setRecursosEJB(Recursos recursosEJB) {
		this.recursosEJB = recursosEJB;
	}

	public TextoRecurso getTextoRec() {
		return textoRec;
	}
	public void setTextoRec(TextoRecurso textoRec) {
		this.textoRec = textoRec;
	}
	public SessionMBean getSessionMBean() {
		return sessionMBean;
	}
	public void setSessionMBean(SessionMBean sessionMBean) {
		this.sessionMBean = sessionMBean;
	}


	@PostConstruct
	public void initTextoRec(){
		
		if (recursosEJB == null) recursosEJB = (Recursos)lookupEJB(SAEProfile.getInstance().EJB_RECURSOS_JNDI);

		//Se controla que se haya seleccionado un recurso para trabajar con los textos
		if (sessionMBean.getRecursoSeleccionado() == null){
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
		}
		else 
		{
			textoRec.setRecurso(sessionMBean.getRecursoSeleccionado());
			textoRec.setTextoPaso2(sessionMBean.getRecursoSeleccionado().getTextoRecurso().getTextoPaso2());
			textoRec.setTextoPaso3(sessionMBean.getRecursoSeleccionado().getTextoRecurso().getTextoPaso3());
			textoRec.setTituloCiudadanoEnLlamador(sessionMBean.getRecursoSeleccionado().getTextoRecurso().getTituloCiudadanoEnLlamador());
			textoRec.setTituloPuestoEnLlamador(sessionMBean.getRecursoSeleccionado().getTextoRecurso().getTituloPuestoEnLlamador());
			textoRec.setTicketEtiquetaUno(sessionMBean.getRecursoSeleccionado().getTextoRecurso().getTicketEtiquetaUno());
			textoRec.setTicketEtiquetaDos(sessionMBean.getRecursoSeleccionado().getTextoRecurso().getTicketEtiquetaDos());
			textoRec.setValorEtiquetaUno(sessionMBean.getRecursoSeleccionado().getTextoRecurso().getValorEtiquetaUno());
			textoRec.setValorEtiquetaDos(sessionMBean.getRecursoSeleccionado().getTextoRecurso().getValorEtiquetaDos());
		}
	}


	public void guardar(ActionEvent e) {

		if (sessionMBean.getRecursoSeleccionado() != null) {
			try {
				sessionMBean.getRecursoSeleccionado().getTextoRecurso().setTextoPaso2(textoRec.getTextoPaso2());
				sessionMBean.getRecursoSeleccionado().getTextoRecurso().setTextoPaso3(textoRec.getTextoPaso3());
				sessionMBean.getRecursoSeleccionado().getTextoRecurso().setTituloCiudadanoEnLlamador(textoRec.getTituloCiudadanoEnLlamador());
				sessionMBean.getRecursoSeleccionado().getTextoRecurso().setTituloPuestoEnLlamador(textoRec.getTituloPuestoEnLlamador());
				sessionMBean.getRecursoSeleccionado().getTextoRecurso().setTicketEtiquetaUno(textoRec.getTicketEtiquetaUno());
				sessionMBean.getRecursoSeleccionado().getTextoRecurso().setTicketEtiquetaDos(textoRec.getTicketEtiquetaDos());
				sessionMBean.getRecursoSeleccionado().getTextoRecurso().setValorEtiquetaUno(textoRec.getValorEtiquetaUno());
				sessionMBean.getRecursoSeleccionado().getTextoRecurso().setValorEtiquetaDos(textoRec.getValorEtiquetaDos());
				
				recursosEJB.modificarRecurso(sessionMBean.getRecursoSeleccionado());
				addInfoMessage(getI18N().getText("recursos.modify"), MSG_ID); 
			} catch (Exception ex) {
				addErrorMessage(ex, MSG_ID);
			}
		}
		else {
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
		}

	}


	public void cancelar(ActionEvent event) {
		if (sessionMBean.getRecursoSeleccionado() == null){
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
		}
		else 
		{
			textoRec.setRecurso(sessionMBean.getRecursoSeleccionado());
			textoRec.setTextoPaso2(sessionMBean.getRecursoSeleccionado().getTextoRecurso().getTextoPaso2());
			textoRec.setTextoPaso3(sessionMBean.getRecursoSeleccionado().getTextoRecurso().getTextoPaso3());
			textoRec.setTituloCiudadanoEnLlamador(sessionMBean.getRecursoSeleccionado().getTextoRecurso().getTituloCiudadanoEnLlamador());
			textoRec.setTituloPuestoEnLlamador(sessionMBean.getRecursoSeleccionado().getTextoRecurso().getTituloPuestoEnLlamador());
			textoRec.setTicketEtiquetaUno(sessionMBean.getRecursoSeleccionado().getTextoRecurso().getTicketEtiquetaUno());
			textoRec.setTicketEtiquetaDos(sessionMBean.getRecursoSeleccionado().getTextoRecurso().getTicketEtiquetaDos());
			textoRec.setValorEtiquetaUno(sessionMBean.getRecursoSeleccionado().getTextoRecurso().getValorEtiquetaUno());
			textoRec.setValorEtiquetaDos(sessionMBean.getRecursoSeleccionado().getTextoRecurso().getValorEtiquetaDos());
		}
	}




	public void beforePhaseModifTexto(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("recursos.texts_update.title"));
		}
	}

}
