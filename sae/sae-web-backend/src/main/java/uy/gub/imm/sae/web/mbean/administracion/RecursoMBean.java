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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;

import org.richfaces.component.UIDataTable;

import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.DatoDelRecurso;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.web.common.BaseMBean;
import uy.gub.imm.sae.web.common.Row;

public class RecursoMBean extends BaseMBean{

	public static final String MSG_ID = "pantalla";
	
	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;
	
	private SessionMBean sessionMBean;
	private Recurso recursoNuevo;
	private DatoDelRecurso datoDelRecursoNuevo;
	private RecursoSessionMBean recursoSessionMBean;
	
	//Tabla asociada en pantalla para poder saber en que recurso se posiciona. 
	private UIDataTable recursosDataTableModificar;
	private UIDataTable recursosDataTableEliminar;
	private UIDataTable recursosDataTableConsultar;
	
	//Tabla asociada tabla en pantalla para poder saber en que recurso se posiciona. 
	private UIDataTable datosDataTable;


	/**************************************************************************/
	/*                           Getters y Setters                            */	
	/**************************************************************************/	
	public SessionMBean getSessionMBean() {
		return sessionMBean;
	}
	
	public void setSessionMBean(SessionMBean sessionMBean) {
		this.sessionMBean = sessionMBean;
	}
	

	public Recurso getRecursoNuevo() {

		if (recursoNuevo == null) {
			recursoNuevo = new Recurso();
		}
		return recursoNuevo;
	}
	
	public void setRecursoNuevo(Recurso r){
		recursoNuevo = r;
	}


	//Recurso seleccionado para eliminacion/modificacion
	public Recurso getRecursoSeleccionado() {
		return sessionMBean.getRecursoSeleccionado();
	}


	
	
	public UIDataTable getRecursosDataTableModificar() {
		return recursosDataTableModificar;
	}

	public void setRecursosDataTableModificar(UIDataTable recursosDataTableModificar) {
		this.recursosDataTableModificar = recursosDataTableModificar;
	}

	public UIDataTable getRecursosDataTableEliminar() {
		return recursosDataTableEliminar;
	}

	public void setRecursosDataTableEliminar(UIDataTable recursosDataTableEliminar) {
		this.recursosDataTableEliminar = recursosDataTableEliminar;
	}

	public UIDataTable getRecursosDataTableConsultar() {
		return recursosDataTableConsultar;
	}

	public void setRecursosDataTableConsultar(UIDataTable recursosDataTableConsultar) {
		this.recursosDataTableConsultar = recursosDataTableConsultar;
	}

	//Tabla de Datos del Recursos	
	public UIDataTable getDatosDataTable() {
		return datosDataTable;
	}

	public void setDatosDataTable(UIDataTable datosDataTable) {
		this.datosDataTable = datosDataTable;
	}

	public DatoDelRecurso getDatoDelRecursoNuevo() {

		if (datoDelRecursoNuevo == null) {
			datoDelRecursoNuevo = new DatoDelRecurso();
			
		}
		return datoDelRecursoNuevo;
	}


	@PostConstruct
	public void initRecurso(){
		
		if (recursosEJB == null) recursosEJB = (Recursos)lookupEJB(SAEProfile.getInstance().EJB_RECURSOS_JNDI);
		
	}

	/**************************************************************************/
	/*                        Action Listener de Recurso                      */	
	/**************************************************************************/	
	
	public String crearEn(Agenda agenda) {
		
		if (agenda != null) {
			sessionMBean.setAgendaSeleccionada(agenda);
			return "/administracion/recurso/crear.xhtml";
		}
		else {
			return null;
		}
	}
	
	public void crear(ActionEvent e) {
		if(getRecursoNuevo().getNombre() == null || getRecursoNuevo().getNombre().equals("")){
			addErrorMessage(getI18N().getText("recursos.name_required"), MSG_ID);
		}
		else if(getRecursoNuevo().getDescripcion() == null || getRecursoNuevo().getDescripcion().equals("")){
			addErrorMessage(getI18N().getText("recursos.description_required"), MSG_ID);
		}
		else {
			try {
				// Se eliminan espacios al principio y final del nombre.
				getRecursoNuevo().setNombre(getRecursoNuevo().getNombre().trim());
				getRecursoNuevo().setDescripcion(getRecursoNuevo().getDescripcion().trim());
				recursosEJB.crearRecurso(sessionMBean.getAgendaSeleccionada(), getRecursoNuevo());
				
				sessionMBean.getAgendaSelectionMBean().refreshAgendasDataModel();
				//sessionMBean.cargarRecursos();
				//sessionMBean.desmarcarRecurso();
				
				//Se blanquea la info en la página
				this.setRecursoNuevo(null);
				addInfoMessage(getI18N().getText("recursos.created"), MSG_ID);
			} catch (Exception ex) {
				addErrorMessage(ex, MSG_ID);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void selecRecursoEliminar(ActionEvent e){
		recursoSessionMBean.setRecursoSeleccionado(((Row<Recurso>) this.getRecursosDataTableEliminar().getRowData()).getData());
	}
	
	
	/*public void eliminar(ActionEvent ev) {
    	//Recurso r = ((Row<Recurso>) this.getRecursosDataTableEliminar().getRowData()).getData();
		
		Recurso r = this.recursoSessionMBean.getRecursoSeleccionado();
		if ( r != null) {
 			try {
 				recursosEJB.eliminarRecurso(r);
 				sessionMBean.cargarRecursos();
 				sessionMBean.desmarcarRecurso();

 				addInfoMessage(getI18N().getText("recursos.deleted"), MSG_ID);
 			} catch (Exception e) {
 				addErrorMessage(e, MSG_ID);
 			} finally {
 				this.recursoSessionMBean.setRecursoSeleccionado(null);		
 			}
 		}
		else {
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
		}
	}*/
	public String eliminar(Recurso r) {

		if ( r != null) {
 			try {
 				recursosEJB.eliminarRecurso(r);
 				sessionMBean.getAgendaSelectionMBean().refreshAgendasDataModel();
 
 				addInfoMessage(getI18N().getText("recursos.deleted"), MSG_ID);
 			} catch (Exception e) {
 				addErrorMessage(e, MSG_ID);
 			} finally {
 				this.recursoSessionMBean.setRecursoSeleccionado(null);		
 			}
 		}
		else {
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
		}
		
		return null;
	}	

	/**************************************************************************/
	/*                       Action  de Recurso  (navegación)                 */	
	/**************************************************************************/	
	
	/*@SuppressWarnings("unchecked")
	public String modificar() {

    	Recurso r = ((Row<Recurso>) this.getRecursosDataTableModificar().getRowData()).getData();
		
		if (r != null) {
			sessionMBean.setRecursoSeleccionado(r);
			
			//Se agrega para que si cambiamos de recurso no queden cargados los datos viejos 
			sessionMBean.setDatoDelRecursoSeleccionado(null);
			sessionMBean.setMostrarAgregarDato(false);

			sessionMBean.cargarDatosDelRecurso();
			return "modificar";
		}
		else {
			sessionMBean.setRecursoSeleccionado(null);
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
			return null;
		}
		
	}*/
	public String modificar(Recurso r) {

		if (r != null) {
			sessionMBean.setRecursoSeleccionado(r);
			
			//Se agrega para que si cambiamos de recurso no queden cargados los datos viejos 
			sessionMBean.setDatoDelRecursoSeleccionado(null);
			sessionMBean.setMostrarAgregarDato(false);

			sessionMBean.cargarDatosDelRecurso();
			return "/administracion/recurso/modificar.xhtml";
		}
		else {
			sessionMBean.setRecursoSeleccionado(null);
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
			return null;
		}
		
	}
	
	public String modificarTextos(Recurso r) {

		if (r != null) {
			sessionMBean.setRecursoSeleccionado(r);
			
			return "/administracion/recurso/modificarTextos.xhtml";
		}
		else {
			sessionMBean.setRecursoSeleccionado(null);
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
			return null;
		}
		
	}

	
	
	/*@SuppressWarnings("unchecked")
	public void copiar(ActionEvent event) {

    	Recurso r = ((Row<Recurso>) this.getRecursosDataTableModificar().getRowData()).getData();
		
		if (r != null) {
			try {
				recursosEJB.copiarRecurso(r, "Copia de " + r.getNombre(), "Copia de " + r.getDescripcion());
				
				sessionMBean.cargarRecursos();
				sessionMBean.desmarcarRecurso();

				addInfoMessage(getI18N().getText("recursos.copied") + r.getNombre(), MSG_ID);
				
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		}
	}*/
	public String copiar(Recurso r) {

		if (r != null) {
			try {
				recursosEJB.copiarRecurso(r, "Copia de " + r.getNombre(), "Copia de " + r.getDescripcion());
				
				sessionMBean.getAgendaSelectionMBean().refreshAgendasDataModel();

				addInfoMessage(getI18N().getText("recursos.copied") + r.getNombre(), MSG_ID);
				
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		}
		
		return null;
	}

	
	public String guardar() {
		if (sessionMBean.getRecursoSeleccionado() != null) {
 			try {
 				// Se hace un trim del nombre del recurso, para evitar que
 				// tenga blancos al principio o al final
 				sessionMBean.getRecursoSeleccionado().setNombre(sessionMBean.getRecursoSeleccionado().getNombre().trim());
 				sessionMBean.getRecursoSeleccionado().setDescripcion(sessionMBean.getRecursoSeleccionado().getDescripcion().trim());
 				
 				//largoListaEspera > 0
 				if (sessionMBean.getRecursoSeleccionado().getLargoListaEspera() <= 0 ){
 					sessionMBean.getRecursoSeleccionado().setLargoListaEspera(null);
 				}
 				
 				recursosEJB.modificarRecurso(sessionMBean.getRecursoSeleccionado());
 				addInfoMessage(getI18N().getText("recursos.modify"), MSG_ID); 				
				sessionMBean.getAgendaSelectionMBean().refreshAgendasDataModel();
 				return "guardar";
 			} catch (Exception e) {
 				addErrorMessage(e, MSG_ID);
 			}
		}
		else {
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
		}
		
		return null;
	}
	

	/**************************************************************************/
	/*               Action Listeners de Datos del Recurso                    */	
	/**************************************************************************/	

	public void agregarDatoDelRecurso(ActionEvent e) {
		if(getDatoDelRecursoNuevo().getEtiqueta() == null || getDatoDelRecursoNuevo().getEtiqueta().equals("")){
			addErrorMessage(getI18N().getText("recursos.info.label_required"), MSG_ID);
		}
		else {
			try {
				recursosEJB.agregarDatoDelRecurso(sessionMBean.getRecursoSeleccionado(), getDatoDelRecursoNuevo());
				addInfoMessage(getI18N().getText("recursos.info.information_created"), MSG_ID);
				
		//		sessionMBean.cargarDatosDelRecurso();
				//Se blanquea la info en la página
				datoDelRecursoNuevo = null;

			} catch (Exception ex) {
				addErrorMessage(ex, MSG_ID);
			}
		}
	}

	public void guardarModifDato(ActionEvent event) {
		if (sessionMBean.getDatoDelRecursoSeleccionado() != null) {			
 			try {
 				recursosEJB.modificarDatoDelRecurso(sessionMBean.getDatoDelRecursoSeleccionado());
 				addInfoMessage(getI18N().getText("recursos.info.information_modified"), MSG_ID); 				
 			} catch (Exception e) {
 				addErrorMessage(e, MSG_ID);
 			}
		}
		else {
			addErrorMessage(getI18N().getText("recursos.info.information_required"), MSG_ID);
		}
	}

	public void cancelarModifDato(ActionEvent event) {
//		sessionMBean.cargarDatosDelRecurso();
		sessionMBean.setDatoDelRecursoSeleccionado(null);
	}


	public void cancelarAgregarDato(ActionEvent event) {
		sessionMBean.setMostrarAgregarDato(false);
		sessionMBean.cargarDatosDelRecurso();
		
	}

	
	@SuppressWarnings("unchecked")
	public void seleccionarDato(ActionEvent e) {
		DatoDelRecurso d = ((Row<DatoDelRecurso>) this.getDatosDataTable().getRowData()).getData();
    	sessionMBean.setMostrarAgregarDato(false);
//		if (d != null) {
		sessionMBean.setDatoDelRecursoSeleccionado(d); 
/*		}
		else {
			sessionMBean.setDatoDelRecursoSeleccionado(null);
		}*/
	}

	public void mostrarDatoDelRecurso(ActionEvent e) {
		sessionMBean.setMostrarAgregarDato(true);
		sessionMBean.setMostrarDato(false);
		sessionMBean.setDatoDelRecursoSeleccionado(null);
	}
	
	@SuppressWarnings("unchecked")
	public void selecDatoEliminar(ActionEvent e){
		this.getRecursoSessionMBean().setDatoSolicSeleccionado(((Row<DatoDelRecurso>) this.getDatosDataTable().getRowData()).getData());
	}

	public void eliminarDatoDelRecurso(ActionEvent event) {
    //	DatoDelRecurso d = ((Row<DatoDelRecurso>) this.getDatosDataTable().getRowData()).getData();
    	
		DatoDelRecurso d = this.getRecursoSessionMBean().getDatoSolicSeleccionado();
		
		if (d != null) {
 			try {
 				recursosEJB.eliminarDatoDelRecurso(d); 
 	//			sessionMBean.cargarDatosDelRecurso();
 				sessionMBean.setMostrarAgregarDato(false);
 				sessionMBean.setMostrarDato(false);
 				sessionMBean.setDatoDelRecursoSeleccionado(null);
 				addInfoMessage(getI18N().getText("recursos.info.information_deleted"), MSG_ID);
 			} catch (Exception e) {
 				addErrorMessage(e, MSG_ID);
 			} finally {
 				this.getRecursoSessionMBean().setDatoSolicSeleccionado(null);
 			}
		}
		else {
			addErrorMessage(getI18N().getText("recursos.info.information_required"), MSG_ID);
		}
	}
	
	/**************************************************************************/
	/*               Action  de Datos del Recurso  (navegación)               */	
	/**************************************************************************/	

	
	/*@SuppressWarnings("unchecked")
	public String consultarDatos() throws ApplicationException, BusinessException {
        //Se busca posición que se quiere consultar
    	
    	Recurso r = ((Row<Recurso>) this.getRecursosDataTableConsultar().getRowData()).getData();
    	
		if (r != null) {
	        //La siguiente línea no está desplegando el recurso
			sessionMBean.setRecursoSeleccionado(r);
			List<DatoDelRecurso> datosDelRecurso= recursosEJB.consultarDatosDelRecurso(r);
			sessionMBean.getRecursoSeleccionado().setDatoDelRecurso(datosDelRecurso);
			return "consultarDatos";
		}
		else {
			sessionMBean.setRecursoSeleccionado(null);
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
			return null;
		}
	}*/
	public String ver(Recurso r) {

		if (r != null) {
			
			sessionMBean.setRecursoSeleccionado(r);
			try {
				List<DatoDelRecurso> datosDelRecurso= recursosEJB.consultarDatosDelRecurso(r);
				sessionMBean.getRecursoSeleccionado().setDatoDelRecurso(datosDelRecurso);
				
				return "/administracion/recurso/ver.xhtml";
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
			return null;
		}
		else {
			sessionMBean.setRecursoSeleccionado(null);
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
			return null;
		}
	}

	public String consultarRecursos() throws ApplicationException {

        if (sessionMBean.getAgendaMarcada() != null){
			return "consultarRecursos";
		}
		else {
			//sessionMBean.setAgendaSeleccionada(null);
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
			return null;
		}
	}

	//Navega a pantalla modificarConsultar para los recursos
	public String volverModificarConsultar() throws ApplicationException {

        if (sessionMBean.getAgendaMarcada() != null){
			return "volverModificarConsultar";
		}
		else {
			//sessionMBean.setAgendaSeleccionada(null);
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
			return null;
		}
	}

	public RecursoSessionMBean getRecursoSessionMBean() {
		return recursoSessionMBean;
	}

	public void setRecursoSessionMBean(RecursoSessionMBean recursoSessionMBean) {
		this.recursoSessionMBean = recursoSessionMBean;
	}

	
	public void beforePhaseCrear(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("recursos.create.title"));
		}
	}
	public void beforePhaseEliminar(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("recursos.delete.title"));
		}
	}	
	public void beforePhaseConsultar(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("recursos.query.title"));
		}
	}
	public void beforePhaseModificarConsultar(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("recursos.update.title"));
		}
	}
	
}
