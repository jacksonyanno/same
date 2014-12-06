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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;

import org.richfaces.component.UIDataTable;

import uy.gub.imm.sae.business.api.AgendarReservas;
import uy.gub.imm.sae.business.api.Consultas;
import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.enumerados.Estado;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.DatoReserva;
import uy.gub.imm.sae.entity.Reserva;
import uy.gub.imm.sae.web.common.BaseMBean;
import uy.gub.imm.sae.web.common.FormularioDinReservaClient;

public class ReservaMBean extends BaseMBean {

	public static final String MSG_ID = "pantalla";
	
	
	@EJB(name="ejb/AgendarReservasBean")
	private AgendarReservas agendarReservasEJB;
	
	@EJB(name="ejb/ConsultasBean")
	private Consultas consultaEJB;

	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;

	private ReservaSessionMBean reservaSessionMBean;
	private SessionMBean sessionMBean;

	private Map<String, DatoASolicitar> datosASolicitar;
	private Map<String, Object> datosFiltroReservaMBean;

	private UIDataTable reservasDataTable;

	private UIComponent filtroConsulta;
	private UIComponent campos;

	@PostConstruct
	public void initAgendaRecurso() {
		
		if (recursosEJB == null) recursosEJB = (Recursos)lookupEJB(SAEProfile.getInstance().EJB_RECURSOS_JNDI);
		if (agendarReservasEJB == null) agendarReservasEJB = (AgendarReservas)lookupEJB(SAEProfile.getInstance().EJB_AGENDAR_RESERVAS_JNDI);
		if (consultaEJB  == null) consultaEJB = (Consultas)lookupEJB(SAEProfile.getInstance().EJB_CONSULTAS_JNDI);

		
		// Se controla que se haya Marcado una agenda para trabajar con los
		// recursos
		if (sessionMBean.getAgendaMarcada() == null) {
			addErrorMessage("Debe tener una agenda seleccionada", MSG_ID);
		}

		// Se controla que se haya Marcado un recurso
		if (sessionMBean.getRecursoMarcado() == null) {
			addErrorMessage("Debe tener un recurso seleccionado", MSG_ID);
		}

		try {
			// guardo en session los datos a solicitar del recurso
			List<DatoASolicitar> listaDatoSolicitar = recursosEJB
					.consultarDatosSolicitar(sessionMBean.getRecursoMarcado());

/*			List<DatoASolicitar> listaDatoSolicitar = sessionMBean.getRecursoMarcado().getDatoASolicitar();
		*/
			Map<String, DatoASolicitar> datoSolicMap = new HashMap<String, DatoASolicitar>();
			for (DatoASolicitar dato : listaDatoSolicitar) {
				datoSolicMap.put(dato.getNombre(), dato);
			}
			setDatosASolicitar(datoSolicMap);

		} 
	catch (ApplicationException e) {
			addErrorMessage(e.getMessage());
		}

	}

	public String volverPagInicio() {

		// Este objeto limpia la session
		//SessionCleaner sc = new SessionCleaner();
		return "volver";
	}

	
	public void buscarReservaDatos(ActionEvent e) {
		boolean huboError = false;
		ArrayList<Reserva> reservas = new ArrayList<Reserva>();

		List<DatoReserva> datos = FormularioDinReservaClient.obtenerDatosReserva(datosFiltroReservaMBean, datosASolicitar);

		if (sessionMBean.getAgendaMarcada() == null && !huboError) {
			huboError = true;
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
		}

		if (sessionMBean.getRecursoMarcado() == null && !huboError) {
			huboError = true;
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
		}

		if (!huboError) {

			// Voy a negocio a buscar las reservas
			reservas = (ArrayList<Reserva>) consultaEJB.consultarReservasParaCancelar(datos, sessionMBean.getRecursoMarcado());
			this.reservaSessionMBean.setListaReservas(reservas);
			if (reservas.isEmpty()) {
				addErrorMessage(
						getI18N().getText("query.not_found"),
						MSG_ID);
			} else {
				this.reservaSessionMBean.setListaReservas(reservas);
			}
		}

	}

	public String verDetalleReserva() {
		int iSelectedPos = getReservasDataTable().getRowIndex();
		Reserva r = this.reservaSessionMBean.getListaReservas().get(
				iSelectedPos);
		this.reservaSessionMBean.setReservaDatos(r);
		this.reservaSessionMBean.setDisponibilidad(r.getDisponibilidades().get(0));
		return "detalleReserva";
	}

	public void cancelarReserva(ActionEvent event) {
		boolean huboError=false;
		
		if (sessionMBean.getAgendaMarcada() == null){
			huboError = true;
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
		}
		
		if (sessionMBean.getRecursoMarcado() == null){
			huboError = true;
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
		}

		if (reservaSessionMBean.getReservaDatos() == null || 
			reservaSessionMBean.getReservaDatos().getId()== null){
			huboError = true;
			addErrorMessage(getI18N().getText("reservas.query_before_cancel"), MSG_ID);
		}

		if (reservaSessionMBean.getReservaDatos().getEstado() != Estado.R){
			huboError = true;
			addErrorMessage(getI18N().getText("reservas.not_cancel_in_state")+reservaSessionMBean.getReservaDatos().getEstadoDescripcion(), MSG_ID);
		}


		if (!huboError){
			try {
				//Reserva reserva = consultaEJB.consultarReservaId(.getIdReserva());
				
				agendarReservasEJB.cancelarReserva(sessionMBean.getRecursoMarcado(), reservaSessionMBean.getReservaDatos());
				Reserva r;
				r = consultaEJB.consultarReservaPorNumero(
						sessionMBean.getRecursoMarcado(), reservaSessionMBean.getDisponibilidad().getHoraInicio(), reservaSessionMBean.getReservaDatos().getNumero());
				reservaSessionMBean.setReservaDatos(r);

				addInfoMessage(getI18N().getText("reservas.cancel_success"), "pantalla");
				
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		}
	}
	
	public Boolean getConfirmarDeshabilitado() {
		
		if (reservaSessionMBean.getReservaDatos() == null || 
			reservaSessionMBean.getReservaDatos().getEstado() == Estado.C ||
			reservaSessionMBean.getReservaDatos().getEstado() == Estado.U) {
			
			return true;
		}
		else {
			return false;
		}
	}
	/** **************************************************************************** */
	
	public void beforePhaseDetalleReserva(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean
					.setPantallaTitulo(getI18N().getText("query.reserva_by_data"));
		}
	}

	public UIComponent getFiltroConsulta() {
		return filtroConsulta;
	}

	public void setFiltroConsulta(UIComponent filtroConsulta) {
		this.filtroConsulta = filtroConsulta;
		try {
		List<AgrupacionDato> agrupaciones = recursosEJB.consultarDefCamposTodos(this.sessionMBean.getRecursoMarcado());
		FormularioDinReservaClient.armarFormularioEdicionDinamico(this.sessionMBean.getRecursoMarcado(), filtroConsulta, agrupaciones);
		} catch (BusinessException be) {
			addErrorMessage(be, MSG_ID);
		} catch (Exception e) {
			addErrorMessage(e);
		}
	}

	public UIDataTable getReservasDataTable() {
		return reservasDataTable;
	}

	public void setReservasDataTable(UIDataTable reservasDataTable) {
		this.reservasDataTable = reservasDataTable;
	}

	public Map<String, DatoASolicitar> getDatosASolicitar() {
		return datosASolicitar;
	}

	public void setDatosASolicitar(Map<String, DatoASolicitar> datosASolicitar) {
		this.datosASolicitar = datosASolicitar;
	}

	public UIComponent getCampos() {
		return campos;
	}

	public void setCampos(UIComponent campos) {
		this.campos = campos;
		try {
			List<AgrupacionDato> agrupaciones = recursosEJB
					.consultarDefinicionDeCampos(sessionMBean
							.getRecursoMarcado());
			FormularioDinReservaClient.armarFormularioLecturaDinamico(
					sessionMBean.getRecursoMarcado(),
					this.reservaSessionMBean.getReservaDatos(),
					this.campos, agrupaciones);
		} catch (BusinessException be) {
			addErrorMessage(be, MSG_ID);
		} catch (Exception e) {
			addErrorMessage(e);
		}

	}

	public Map<String, Object> getDatosFiltroReservaMBean() {
		return datosFiltroReservaMBean;
	}

	public void setDatosFiltroReservaMBean(
			Map<String, Object> datosFiltroReservaMBean) {
		this.datosFiltroReservaMBean = datosFiltroReservaMBean;
	}

	public SessionMBean getSessionMBean() {
		return sessionMBean;
	}

	public void setSessionMBean(SessionMBean sessionMBean) {
		this.sessionMBean = sessionMBean;
	}

	public ReservaSessionMBean getReservaSessionMBean() {
		return reservaSessionMBean;
	}
	public void setReservaSessionMBean(ReservaSessionMBean reservaSessionMBean) {
		this.reservaSessionMBean = reservaSessionMBean;
	}

	//setearlo en <f:view beforePhase de la pagina.
	public void beforePhaseCancelar(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("menu.reservas.book_cancel"));
		}
	}
	
	
}
