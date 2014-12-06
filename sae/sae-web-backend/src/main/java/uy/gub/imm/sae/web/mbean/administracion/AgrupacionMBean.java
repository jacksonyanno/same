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

import org.richfaces.component.UIDataTable;

import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.web.common.BaseMBean;

public class AgrupacionMBean extends BaseMBean {
	public static final String MSG_ID = "pantalla";

	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;

	public SessionMBean sessionMBean;
	private DatoASSessionMBean datoASSessionMBean;
	
	private UIDataTable agrupacionesDataTable;

	private AgrupacionDato agrupacionDatoNuevo;



	@PostConstruct
	public void initRecurso(){

		if (recursosEJB == null) recursosEJB = (Recursos)lookupEJB(SAEProfile.getInstance().EJB_RECURSOS_JNDI);
		
		//Se controla que se haya Marcado una agenda y recurso
		if (sessionMBean.getAgendaMarcada() == null){
			addErrorMessage("Debe tener una agenda y recurso seleccionados", MSG_ID);
		}
		if (sessionMBean.getRecursoMarcado() == null) {
			addErrorMessage("Debe tener un recurso seleccionado", MSG_ID);
		}
	}

	public SessionMBean getSessionMBean() {
		return sessionMBean;
	}
	public void setSessionMBean(SessionMBean sessionMBean) {
		this.sessionMBean = sessionMBean;
	}
	public DatoASSessionMBean getDatoASSessionMBean() {
		return datoASSessionMBean;
	}

	public void setDatoASSessionMBean(DatoASSessionMBean datoASSessionMBean) {
		this.datoASSessionMBean = datoASSessionMBean;
	}
	public UIDataTable getAgrupacionesDataTable() {
		return agrupacionesDataTable;
	}
	public void setAgrupacionesDataTable(UIDataTable agrupacionesDataTable) {
		this.agrupacionesDataTable = agrupacionesDataTable;
	}
	public void beforePhaseModificarAgrupaciones(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("formulario.groups.title"));
		}
	}



	
	/*
	 * MODIFICACION
	 * 
	 */
	
	public void seleccionarAgrupacion(ActionEvent e) {
		
		sessionMBean.setMostrarAgrupacion(true);
		sessionMBean.setMostrarAgregarAgrupacion(false);
		
		AgrupacionDato a = (AgrupacionDato) this.getAgrupacionesDataTable().getRowData();

		if (a != null) {
			datoASSessionMBean.setAgrupacionSeleccionada(a);
		} else {
			datoASSessionMBean.setAgrupacionSeleccionada(null);
		}
	}

	public void modificarAgrupacion(ActionEvent event) {

		if (datoASSessionMBean.getAgrupacionSeleccionada() != null) {
			try {
				recursosEJB.modificarAgrupacionDato(datoASSessionMBean.getAgrupacionSeleccionada());
				datoASSessionMBean.clearAgrupaciones();
				addInfoMessage(getI18N().getText("formulario.groups.message.saved_ok"),	MSG_ID);
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		} else {
			addErrorMessage(getI18N().getText("formulario.groups.message.group_must_be_selected"),	MSG_ID);
		}
	}
	
	public void cancelarModificarAgrupacion(ActionEvent event) {
		sessionMBean.setMostrarAgrupacion(false);
	}
	
	
	
	
	/*
	 * ELIMINACION
	 * 
	 */

	public void seleccionarAgrupacionParaEliminar(ActionEvent e) {
		datoASSessionMBean.setAgrupacionSeleccionada(
				(AgrupacionDato) this.getAgrupacionesDataTable().getRowData()
		);
	}

	public void eliminarAgrupacion(ActionEvent event) {

		AgrupacionDato a = datoASSessionMBean.getAgrupacionSeleccionada();
			
		if (a != null) {
			try {
				recursosEJB.eliminarAgrupacionDato(a);
				datoASSessionMBean.clearAgrupaciones();
				datoASSessionMBean.setAgrupacionSeleccionada(null);
				sessionMBean.setMostrarAgrupacion(false);
				sessionMBean.setMostrarAgregarAgrupacion(false);
				addInfoMessage(getI18N().getText("formulario.groups.message.deleted_ok"),MSG_ID);
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		} else {
			addErrorMessage(getI18N().getText("formulario.groups.message.group_must_be_selected"),	MSG_ID);
		}
	}

	
	
	
	/*
	 * CREACION
	 * 
	 */
	
	public void mostrarCrearAgrupacion(ActionEvent e) {

		sessionMBean.setMostrarAgregarAgrupacion(true);
		sessionMBean.setMostrarAgrupacion(false);
	}

	public void crearAgrupacion(ActionEvent e) {

		boolean huboError = false;
		
		if (sessionMBean.getAgendaMarcada() == null){
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
			huboError = true;
			
		}
		if (sessionMBean.getRecursoMarcado() == null) {
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
			huboError = true;
		}

		if (getAgrupacionDatoNuevo().getNombre() == null || getAgrupacionDatoNuevo().getNombre().equals("")) {
			addErrorMessage("El nombre de la agrupacion del dato del recurso es obligatorio", MSG_ID);
			huboError = true;
		}
		
		if (!huboError){
		
			try {
				recursosEJB.agregarAgrupacionDato(sessionMBean.getRecursoMarcado(), getAgrupacionDatoNuevo());
				
				datoASSessionMBean.clearAgrupaciones();
				
				// Se blanquea la info en la página
				setAgrupacionDatoNuevo(null);
				
				addInfoMessage("Agrupacion del Recurso creada correctamente.", MSG_ID);
			} catch (Exception ex) {
				addErrorMessage(ex, MSG_ID);
			}
		}
	
	}
	
	public void cancelarCrearAgrupacion(ActionEvent event) {
		sessionMBean.setMostrarAgregarAgrupacion(false);
	}

	public AgrupacionDato getAgrupacionDatoNuevo() {

		if (agrupacionDatoNuevo == null) {
			agrupacionDatoNuevo = new AgrupacionDato();
		}
		return agrupacionDatoNuevo;
	}

	public void setAgrupacionDatoNuevo(AgrupacionDato agrupacionDatoNuevo) {
		this.agrupacionDatoNuevo = agrupacionDatoNuevo;
	}
	
}
