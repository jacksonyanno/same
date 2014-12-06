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

import org.ajax4jsf.util.base64.Base64;
import org.richfaces.component.UIDataTable;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import uy.gub.imm.sae.business.api.AgendaGeneral;
import uy.gub.imm.sae.business.api.Agendas;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.web.common.BaseMBean;



public class AgendaMBean extends BaseMBean {

	public static final int GROUP_DESCRIPTION_MAX_SIZE = 50;
	public static final int GROUP_NAME_MAX_SIZE = 32;

	@EJB(name="ejb/AgendaGeneralBean")
	private AgendaGeneral generalEJB;
	
	@EJB(name="ejb/AgendasBean")
	private Agendas agendasEJB;
	
	private SessionMBean sessionMBean;
	private AgendaSessionMBean agendaSessionMBean;
	private Agenda agendaNueva;

	//private RowList<Agenda> agendasSeleccion;
	private UIDataTable agendasDataTableModificar;
	private UIDataTable agendasDataTableEliminar;


	@PostConstruct
	public void init() {
		if (generalEJB  == null) generalEJB = (AgendaGeneral)lookupEJB(SAEProfile.getInstance().EJB_AGENDA_GENERAL_JNDI);
		if (agendasEJB == null)  agendasEJB = (Agendas)lookupEJB(SAEProfile.getInstance().EJB_AGENDAS_JNDI);
	}
	
	public SessionMBean getSessionMBean() {
		return sessionMBean;
	}
	public void setSessionMBean(SessionMBean sessionMBean) {
		this.sessionMBean = sessionMBean;
	}


	//Lista de agendas para seleccionar en la eliminacion/modificacion.
	/*public RowList<Agenda> getAgendasSeleccion() {
		if (agendasSeleccion == null) {
			try {
				List<Agenda> entidades;
				entidades = generalEJB.consultarAgendas();
				agendasSeleccion = new RowList<Agenda>(entidades);
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		}
		return agendasSeleccion;
	}*/		

	
	public Agenda getAgendaNueva() {

		if (agendaNueva == null) {
			agendaNueva = new Agenda();
		}
		return agendaNueva;
	}

	
	//Agenda seleccionada para eliminacion/modificacion
	public Agenda getAgendaSeleccionada() {
		return sessionMBean.getAgendaSeleccionada();
	}

	
	public UIDataTable getAgendasDataTableModificar() {
		return agendasDataTableModificar;
	}

	public void setAgendasDataTableModificar(UIDataTable agendasDataTableModificar) {
		this.agendasDataTableModificar = agendasDataTableModificar;
	}
	

	public UIDataTable getAgendasDataTableEliminar() {
		return agendasDataTableEliminar;
	}

	public void setAgendasDataTableEliminar(UIDataTable agendasDataTableEliminar) {
		this.agendasDataTableEliminar = agendasDataTableEliminar;
	}

	public String crear() {
		
		if (sessionMBean.getCreandoGrupo()) {
			if(getAgendaNueva().getDescripcion() == null || getAgendaNueva().getDescripcion().equals("")){
				addErrorMessage(getI18N().getText("entity.agenda.message.validation.name_required"), MSG_ID);
			}
			else {
				try {

					getAgendaNueva().setDescripcion(getAgendaNueva().getDescripcion().trim());
					
					//Genero el nombre
					String nombre = getAgendaNueva().getDescripcion().replace(" ", "_");
					if (nombre.length() > GROUP_NAME_MAX_SIZE) {
						nombre = nombre.substring(0,  GROUP_NAME_MAX_SIZE);
					}
					getAgendaNueva().setNombre(nombre);
					
					agendasEJB.crearAgenda(getAgendaNueva());
					sessionMBean.getAgendaSelectionMBean().refreshAgendasDataModel();
					agendaNueva = null;
					addInfoMessage(getI18N().getText("message.change_saved"), MSG_ID);
					sessionMBean.setCreandoGrupo(false);
				} catch (Exception ex) {
					addErrorMessage(ex, MSG_ID);
				}
			}
		}
		
		return null;
	}
	
	public String cambioACreandoGrupo() {
		
		sessionMBean.setCreandoGrupo(true);
		
		return null;
	}

	public String cancelarCreandoGrupo() {
		
		sessionMBean.setCreandoGrupo(false);
		agendaNueva = null;
		
		return null;
	}

	public void copiar(ActionEvent e) {
		
		if(getAgendaNueva().getNombre() == null || getAgendaNueva().getNombre().equals("")){
			addErrorMessage("El nombre de la agenda es obligatorio", MSG_ID);
		}
		else {
			try {
				agendasEJB.copiarAgenda(getAgendaSeleccionada(), getAgendaNueva());
				
				sessionMBean.getAgendaSelectionMBean().refreshAgendasDataModel();
				sessionMBean.setAgendaSeleccionada(null);
				//agendasSeleccion = null;
				agendaNueva = null;			
				
				sessionMBean.getAgendaSelectionMBean().refreshAgendasDataModel();
				
				addInfoMessage("Agenda copiada correctamente.", MSG_ID);
				
			} catch (Exception ex) {
				addErrorMessage(ex, MSG_ID);
			}
		}
	}

	/*@SuppressWarnings("unchecked")
	public void eliminar(ActionEvent event) {
		
		Agenda a = ((Row<Agenda>) this.getAgendasDataTableEliminar().getRowData()).getData();

		if (a != null) {
 			try {
 				agendasEJB.eliminarAgenda(a);
				sessionMBean.cargarAgendas();
 				agendasSeleccion = null;
				addInfoMessage(getI18N().getText("message.change_saved"), MSG_ID);
			} catch (Exception e) {
 				addErrorMessage(e, MSG_ID);
 			}
		}
		else {
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
		}
		
	}*/
	public String eliminar(Agenda a) {
		
		if (a != null) {
 			try {
 				agendasEJB.eliminarAgenda(a);
				sessionMBean.getAgendaSelectionMBean().refreshAgendasDataModel();
 				//agendasSeleccion = null;
				addInfoMessage(getI18N().getText("message.change_saved"), MSG_ID);
			} catch (Exception e) {
 				addErrorMessage(e, MSG_ID);
 			} finally {
 				sessionMBean.setAgendaSeleccionada(null);
 			}
 			
		}
		else {
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
		}
		
		return null;
	}
	

	/*@SuppressWarnings("unchecked")
	public String modificar() {

		Agenda a = ((Row<Agenda>) this.getAgendasDataTableModificar().getRowData()).getData();
		
		if (a != null) {
			sessionMBean.setAgendaSeleccionada(a);
			return "modificar";
		}
		else {
			sessionMBean.setAgendaSeleccionada(null);
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
			return null;
		}
	}*/
	public String modificar(Agenda a) {

		if (a != null) {
			sessionMBean.setAgendaSeleccionada(a);
			return "/administracion/agenda/modificar.xhtml";
		}
		else {
			sessionMBean.setAgendaSeleccionada(null);
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
			return null;
		}
	}
	

	public String modificarTextos(Agenda a) {

		if (a != null) {
			sessionMBean.setAgendaSeleccionada(a);
			
			return "/administracion/agenda/modificarTextos.xhtml";
		}
		else {
			sessionMBean.setAgendaSeleccionada(null);
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
			return null;
		}
		
	}

	/*@SuppressWarnings("unchecked")
	public String copiar() {

		Agenda a = ((Row<Agenda>) this.getAgendasDataTableModificar().getRowData()).getData();
		
		if (a != null) {
			sessionMBean.setAgendaSeleccionada(a);
			return "copiar";
		}
		else {
			sessionMBean.setAgendaSeleccionada(null);
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
			return null;
		}
	}*/
	public String copiar(Agenda a) {

		if (a != null) {
			sessionMBean.setAgendaSeleccionada(a);
			return "/administracion/agenda/copiar.xhtml";
		}
		else {
			sessionMBean.setAgendaSeleccionada(null);
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
			return null;
		}
	}
	
	
	public String guardar() {
		if (sessionMBean.getAgendaSeleccionada() != null) {
 			try {
 				
 				//Actualizo logo
 				if (agendaSessionMBean.getAgendaLogo() != null) {
 					sessionMBean.getAgendaSeleccionada().setLogo(agendaSessionMBean.getAgendaLogo());
 				}
 				
 				agendasEJB.modificarAgenda(sessionMBean.getAgendaSeleccionada());
				sessionMBean.getAgendaSelectionMBean().refreshAgendasDataModel();
 				//agendasSeleccion = null;
 				sessionMBean.setAgendaSeleccionada(null);
 				agendaSessionMBean.setAgendaLogo(null);
				addInfoMessage(getI18N().getText("message.change_saved"), MSG_ID);
				return "guardar";
 			} catch (Exception e) {
 				addErrorMessage(e, MSG_ID);
 			}
		}
		else {
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
		}
		
		return null;
	}
	public AgendaSessionMBean getAgendaSessionMBean() {
		return agendaSessionMBean;
	}
	public void setAgendaSessionMBean(AgendaSessionMBean agendaSessionMBean) {
		this.agendaSessionMBean = agendaSessionMBean;
	}

	//setearlo en <f:view beforePhase de la pagina.
	public void beforePhaseCrear(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("agendas.create.title"));
		}
	}

	public void beforePhaseModificarConsultar(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("agendas.update.title"));
		}
	}
	
	public void beforePhaseModificar(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			//sessionMBean.setPantallaTitulo("Modificar agenda");
		}
	}
	
	public void beforePhaseCopiar(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("agendas.copy.title"));
		}
	}


	public void beforePhaseEliminar(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("agendas.delete.title"));
		}
	}

	public void beforePhaseConsultar(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("agendas.query.title"));
		}
	}
	
	
	public void fileUploadListener(UploadEvent event) throws Exception{
	        UploadItem item = event.getUploadItem();
	        
	        String mime = null;
	        int extDot = item.getFileName().lastIndexOf('.');
	        if(extDot > 0){
	            String extension = item.getFileName().substring(extDot +1);
	            if("bmp".equals(extension)){
	                mime="image/bmp";
	            } else if("jpg".equals(extension)){
	                mime="image/jpeg";
	            } else if("gif".equals(extension)){
	                mime="image/gif";
	            } else if("png".equals(extension)){
	                mime="image/png";
	            } else {
	                mime = "image/unknown";
	            }
	        }

	        byte[] header = ("data:"+mime+";base64,").getBytes();
	        
			byte[] binary = Base64.encodeBase64(item.getData());

	        byte[] logo = new byte[header.length + binary.length];

			System.arraycopy(header,0,logo,0,header.length);
			System.arraycopy(binary,0,logo,header.length,binary.length);
			
	        agendaSessionMBean.setAgendaLogo(logo);
	}

	public String getAgendaLogo() {
		byte[] logo = sessionMBean.getAgendaSeleccionada().getLogo();
		if (logo != null && logo.length > 0) return new String(logo);
		logo = agendaSessionMBean.getAgendaLogo();
		if (logo != null && logo.length > 0) return new String(logo);
		return "";
	}
	
	
	public String ver(Agenda a) {

		if (a != null) {
			
			sessionMBean.setAgendaSeleccionada(a);
			try {
				return "/administracion/agenda/ver.xhtml";
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
			return null;
		}
		else {
			sessionMBean.setAgendaSeleccionada(null);
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
			return null;
		}
	}	
}
