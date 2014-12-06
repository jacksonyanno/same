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


import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;

import uy.gub.imm.sae.business.api.Consultas;
import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.entity.Reserva;
import uy.gub.imm.sae.web.common.FormularioDinReservaClient;
import uy.gub.imm.sae.web.common.SessionCleanerMBean;

public class ConsultaMBean extends SessionCleanerMBean {
	
	public static final String MSG_ID = "pantalla";
	
	
	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;
	
	@EJB(name="ejb/ConsultasBean")
	private Consultas consultaEJB;
	
	private UIComponent campos;
	
	
	private SessionMBean sessionMBean;
	private ConsultaSessionMBean consultaSessionMBean;
	private Integer idReserva;
	private Integer dataScrollerPage;
	
	/* Pagina de consulta por Numero */
	private Date fechaHoraReserva;
	private Integer numeroReserva;
	private Reserva reservaConsultada;
	
	
	public ConsultaMBean(){
		
	}
	
	
	@PostConstruct
	public void initAgendaRecurso(){
			
		if (recursosEJB == null) recursosEJB = (Recursos)lookupEJB(SAEProfile.getInstance().EJB_RECURSOS_JNDI);
		if (consultaEJB  == null) consultaEJB = (Consultas)lookupEJB(SAEProfile.getInstance().EJB_CONSULTAS_JNDI);
		
		//Se controla que se haya Marcado una agenda para trabajar con los recursos
		if (sessionMBean.getAgendaMarcada() == null){
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
		}
		
		//Se controla que se haya Marcado un recurso
		if (sessionMBean.getRecursoMarcado() == null){
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
		}
		
		
	}
	
	public void buscarReservaId(ActionEvent event){
		boolean huboError=false;
		Reserva reservaAux ;
		
		campos.getChildren().clear();
		// limpio campos en los que guardo mis datos de Session
		consultaSessionMBean.setReserva(null);
		consultaSessionMBean.setDisponibilidad(null);
		
		if (sessionMBean.getAgendaMarcada() == null && !huboError){
			huboError = true;
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
		}
		
		if (sessionMBean.getRecursoMarcado() == null && !huboError){
			huboError = true;
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
		}
		
		if (!huboError){
					
			// Voy a negocio a buscar la reserva
			try {
				reservaAux = consultaEJB.consultarReservaId(idReserva,
						sessionMBean.getRecursoMarcado().getId());
				
				if (reservaAux== null){
					addErrorMessage(getI18N().getText("query.not_found"), MSG_ID);
				}
				else {
					this.consultaSessionMBean.setReserva(reservaAux);
					this.consultaSessionMBean.setDisponibilidad(reservaAux.getDisponibilidades().get(0));
					List<AgrupacionDato> agrupaciones = recursosEJB.consultarDefinicionDeCampos(sessionMBean.getRecursoMarcado());
					FormularioDinReservaClient.armarFormularioLecturaDinamico(sessionMBean.getRecursoMarcado(), this.consultaSessionMBean.getReserva(), this.campos, agrupaciones);
				}	
			
			} catch (ApplicationException ae) {
				addErrorMessage(ae.getMessage(), MSG_ID);
			} catch (BusinessException be) {
				addErrorMessage(be.getMessage(), MSG_ID);
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
	
		}	
	}

	
		public SessionMBean getSessionMBean() {
		return sessionMBean;
	}

	public void setSessionMBean(SessionMBean sessionMBean) {
		this.sessionMBean = sessionMBean;
	}

	
	public Integer getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(Integer idReserva) {
		this.idReserva = idReserva;
	}




	public Integer getDataScrollerPage() {
		return dataScrollerPage;
	}




	public void setDataScrollerPage(Integer dataScrollerPage) {
		this.dataScrollerPage = dataScrollerPage;
	}




	public ConsultaSessionMBean getConsultaSessionMBean() {
		return consultaSessionMBean;
	}




	public void setConsultaSessionMBean(ConsultaSessionMBean consultaSessionMBean) {
		this.consultaSessionMBean = consultaSessionMBean;
	}

	
	
	public void beforePhaseConsultarReservaId(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("query.reserva_by_id"));
		}
	}

	public void beforePhaseConsultarReservaNumero(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("query.reserva_by_number"));
		}
	}

	public void beforePhaseConsultarReservaPeriodo(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("query.reserva_by_period"));
		}
	}

	public void beforePhaseConsultarAsistenciaPeriodo(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("query.asistencia_by_period"));
		}
	}


	public UIComponent getCampos() {
		return campos;
	}


	public void setCampos(UIComponent campos) {
		this.campos = campos;
	}
	
	
	/**
	 * Pagina de consulta por Numero
	 *  
	 */

	public Date getFechaHoraReserva() {
		return fechaHoraReserva;
	}


	public void setFechaHoraReserva(Date fechaHoraReserva) {
		this.fechaHoraReserva = fechaHoraReserva;
	}


	public Integer getNumeroReserva() {
		return numeroReserva;
	}


	public void setNumeroReserva(Integer numeroReserva) {
		this.numeroReserva = numeroReserva;
	}


	public Reserva getReservaConsultada() {
		return reservaConsultada;
	}

	
	public void buscarReservaPorNumero(ActionEvent event){
		
		boolean huboError=false;
		
		campos.getChildren().clear();
		// limpio campos en los que guardo mis datos de Session
//		consultaSessionMBean.setReserva(null);
	//	consultaSessionMBean.setDisponibilidad(null);
		
		if (sessionMBean.getAgendaMarcada() == null && !huboError){
			huboError = true;
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
		}
		
		if (sessionMBean.getRecursoMarcado() == null && !huboError){
			huboError = true;
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
		}
		
		if ((fechaHoraReserva == null || numeroReserva == null) && !huboError){
			huboError = true;
			addErrorMessage(getI18N().getText("query.indicate_day_hour_number"), MSG_ID);
		}
		
		if (!huboError){
					
			// Voy a negocio a buscar la reserva
			try {
				reservaConsultada = consultaEJB.consultarReservaPorNumero(sessionMBean.getRecursoMarcado(), fechaHoraReserva, numeroReserva);
				
				List<AgrupacionDato> agrupaciones = recursosEJB.consultarDefinicionDeCampos(sessionMBean.getRecursoMarcado());
				FormularioDinReservaClient.armarFormularioLecturaDinamico(sessionMBean.getRecursoMarcado(), reservaConsultada, this.campos, agrupaciones);
			
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
	
		}	
		
	}

	
	
}
