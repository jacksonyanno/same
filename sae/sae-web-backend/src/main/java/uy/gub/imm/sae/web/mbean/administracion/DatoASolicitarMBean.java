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
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.model.SelectItem;

import org.richfaces.component.UIDataTable;

import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.enumerados.Tipo;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.ValorPosible;
import uy.gub.imm.sae.web.common.BaseMBean;

public class DatoASolicitarMBean extends BaseMBean {
	public static final String MSG_ID = "pantalla";

	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;

	public SessionMBean sessionMBean;
	private DatoASSessionMBean datoASSessionMBean;

	// Lista de agrupaciones para usar al crear o modificar
	// un datoASolicitar.
	private List<SelectItem> listaAgrupaciones = new ArrayList<SelectItem>();
	// agrupacionDatoId es para cargar la agrupación seleccionada por el usuario
	// en el selectOneListBox.
	// Luego hay que buscar la agrupación con ese ID.
	private Integer agrupacionDatoId;
	// Lista de tipos del dato a Solicitar al crear o modificar
	private List<SelectItem> listaTipos = new ArrayList<SelectItem>();

	private UIDataTable camposDataTableBorrar;
	private UIDataTable camposDataTableConsultar;
	private UIDataTable camposDataTableModificar;

	private DatoASolicitar datoASolicitarNuevo;
	
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
	public UIDataTable getCamposDataTableBorrar() {
		return camposDataTableBorrar;
	}
	public void setCamposDataTableBorrar(UIDataTable camposDataTableBorrar) {
		this.camposDataTableBorrar = camposDataTableBorrar;
	}
	public UIDataTable getCamposDataTableConsultar() {
		return camposDataTableConsultar;
	}
	public void setCamposDataTableConsultar(UIDataTable camposDataTableConsultar) {
		this.camposDataTableConsultar = camposDataTableConsultar;
	}
	public UIDataTable getCamposDataTableModificar() {
		return camposDataTableModificar;
	}
	public void setCamposDataTableModificar(UIDataTable camposDataTableModificar) {
		this.camposDataTableModificar = camposDataTableModificar;
	}
	public void beforePhaseCrear(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("formulario.field.create.title"));
		}
	}
	public void beforePhaseModificarConsultar(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("formulario.field.modify.title"));
		}
	}
	public void beforePhaseConsultar(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("formulario.field.query.title"));
		}
	}
	public void beforePhaseEliminar(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("formulario.field.delete.title"));
		}
	}

	@PostConstruct
	public void init() {
		
		if (recursosEJB == null) recursosEJB = (Recursos)lookupEJB(SAEProfile.getInstance().EJB_RECURSOS_JNDI);
		
		if (sessionMBean.getRecursoMarcado() != null) {
			// Se cargan listas desplegables
			this.cargarListaTipos();
			this.cargarListaAgrupaciones();
		} else {
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
		}
		
		datoASolicitarNuevo = new DatoASolicitar();
	}

	
	
	
	/*
	 * MODIFICACION
	 * 
	 */

	public void seleccionarDato(ActionEvent event) {

		DatoASolicitar d = (DatoASolicitar) this.getCamposDataTableModificar().getRowData();
		
		if (d != null) {
			datoASSessionMBean.setDatoSeleccionado(d);
			this.agrupacionDatoId = d.getAgrupacionDato().getId();
			datoASSessionMBean.setMostrarModifValor(false);
			datoASSessionMBean.setMostrarAgregarValor(false);
		}
		else {
			datoASSessionMBean.setDatoSeleccionado(null);
		}

	}
	
	
	public void modificarDato(ActionEvent event) {

		if (datoASSessionMBean.getDatoSeleccionado() != null) {
			for (AgrupacionDato a : datoASSessionMBean.getAgrupaciones()) {
				if (a.getId().equals(this.getAgrupacionDatoId())) {
					datoASSessionMBean.getDatoSeleccionado().setAgrupacionDato(a);
				}
			}
			
			// Se intenta modificar el dato en la base.
			try {
				recursosEJB.modificarDatoASolicitar(datoASSessionMBean.getDatoSeleccionado());
				datoASSessionMBean.clearDatosASolicitar();
				
				//Hay que traer los valores posibles en caso que halla cambiado el tipo a LIST
				List<ValorPosible> valoresP = recursosEJB.consultarValoresPosibles(datoASSessionMBean.getDatoSeleccionado());
				datoASSessionMBean.getDatoSeleccionado().setValoresPosibles(valoresP);
				
				addInfoMessage(getI18N().getText("formulario.field.modified"), MSG_ID);
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		} else {
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
		}
	}

	public Integer getAgrupacionDatoId() {
		return agrupacionDatoId;
	}

	public void setAgrupacionDatoId(Integer agrupacionDatoId) {
		this.agrupacionDatoId = agrupacionDatoId;
	}
	
	public List<SelectItem> getListaTipos() {
		return listaTipos;
	}

	public void setListaTipos(List<SelectItem> listaTipos) {
		this.listaTipos = listaTipos;
	}

	public List<SelectItem> getListaAgrupaciones() {
		if (this.listaAgrupaciones.isEmpty()) {
			addErrorMessage(getI18N().getText("formulario.field.agrupation"),
					MSG_ID);
		}
		return listaAgrupaciones;
	}
	
	public void setListaAgrupaciones(List<SelectItem> listaAgrupaciones) {
		this.listaAgrupaciones = listaAgrupaciones;
	}
	
	private void cargarListaTipos() {

		this.listaTipos = new ArrayList<SelectItem>();

		for (Tipo t : Tipo.values()) {
			SelectItem s = new SelectItem();
			s.setValue(t);
			s.setLabel(t.getDescripcion(getI18N().getLocale()));
			this.listaTipos.add(s);
		}
	}

	private void cargarListaAgrupaciones() {

		//Si hay recurso selecciondada, se cargan las agrupaciones.
		//En caso contrario se vacía la lista de agrupaciones
		if (sessionMBean.getRecursoMarcado() != null){			
			try {
				List<AgrupacionDato> entidades;
				entidades = recursosEJB.consultarAgrupacionesDatos(sessionMBean.getRecursoMarcado());

				listaAgrupaciones = new ArrayList<SelectItem>();
				for (AgrupacionDato a: entidades) {
					SelectItem s = new SelectItem();
					s.setValue(a.getId());
					s.setLabel(a.getNombre());
					listaAgrupaciones.add(s);
				}
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		}		
	}
	
	

	
	
	/*
	 * ELIMINACION
	 * 
	 */

	public void seleccionarDatoParaEliminar(ActionEvent e) {
		datoASSessionMBean.setDatoSeleccionado((DatoASolicitar) this.getCamposDataTableBorrar().getRowData());
	}

	
	public void eliminarDato(ActionEvent event) {

		DatoASolicitar d = datoASSessionMBean.getDatoSeleccionado();

		if (d != null) {

			try {
				recursosEJB.eliminarDatoASolicitar(d);
				addInfoMessage(getI18N().getText("formulario.field.deleted"), MSG_ID);
				datoASSessionMBean.setDatoSeleccionado(null);
				datoASSessionMBean.clearDatosASolicitar();

			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			} finally {
				datoASSessionMBean.setDatoSeleccionado(null);
			}
		} else {
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
		}
	}

	
	

	/*
	 * CREACION
	 * 
	 */

	public void crearDato(ActionEvent e) {
		
		if (   this.getDatoASolicitarNuevo().getNombre() == null
			|| this.getDatoASolicitarNuevo().getNombre().equals("")) {
			addErrorMessage(getI18N().getText("formulario.field.name.required"), MSG_ID);
		} else {
			try {

				List<AgrupacionDato> entidades;
				entidades = recursosEJB.consultarAgrupacionesDatos(sessionMBean.getRecursoMarcado());
				for (AgrupacionDato a: entidades) {
					if (a.getId().equals(this.getAgrupacionDatoId())) {
						this.datoASolicitarNuevo.setAgrupacionDato(a);
					}
				}

				recursosEJB.agregarDatoASolicitar(
						sessionMBean.getRecursoMarcado(), 
						getDatoASolicitarNuevo().getAgrupacionDato(), getDatoASolicitarNuevo());
			
				datoASSessionMBean.clearDatosASolicitar();
				
				// Se blanquea la info en la página
				datoASolicitarNuevo = new DatoASolicitar();
				
				addInfoMessage(getI18N().getText("formulario.field.created"), MSG_ID);
			} catch (Exception ex) {
				addErrorMessage(ex, MSG_ID);
			}
		}
	}
	
	public DatoASolicitar getDatoASolicitarNuevo() {
		return datoASolicitarNuevo;
	}

	public void setDatoASolicitarNuevo(DatoASolicitar datoASolicitarNuevo) {
		this.datoASolicitarNuevo = datoASolicitarNuevo;
	}

	
	
	

	/*
	 * CONSULTAR
	 * 
	 */
	
	public String consultarDato() throws ApplicationException {

		DatoASolicitar d = (DatoASolicitar) this.getCamposDataTableConsultar().getRowData();

		if (d != null) {
			datoASSessionMBean.setDatoSeleccionado(d);
			if (datoASSessionMBean.getDatoSeleccionado().getTipo() == Tipo.LIST){
				datoASSessionMBean.setMostrarConsultarValor(true);
			}
			else {
				datoASSessionMBean.setMostrarConsultarValor(false);
			}
		} else {
			datoASSessionMBean.setDatoSeleccionado(null);
			datoASSessionMBean.setMostrarConsultarValor(false);
		}
		return "consultarDato";
	}
	
	
}
