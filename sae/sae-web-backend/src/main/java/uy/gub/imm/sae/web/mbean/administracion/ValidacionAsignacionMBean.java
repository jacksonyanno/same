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

import org.richfaces.component.html.HtmlDataTable;

import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.business.api.Validaciones;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.ParametroValidacion;
import uy.gub.imm.sae.entity.Validacion;
import uy.gub.imm.sae.entity.ValidacionPorDato;
import uy.gub.imm.sae.entity.ValidacionPorRecurso;
import uy.gub.imm.sae.web.common.BaseMBean;


public class ValidacionAsignacionMBean extends BaseMBean{

	public static final String MSG_ID = "pantalla";
	
	@EJB(name="ejb/ValidacionesBean")
	private Validaciones validacionEJB;

	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;
	
	private SessionMBean sessionMBean;
	private ValidacionAsignacionSessionMBean validacionAsignacionSessionMBean;

	
	private List<ValidacionPorRecurso> validacionesDelRecurso;
	private HtmlDataTable validacionesDelRecursoTable;
	
	private List<SelectItem> validacionesItems;

	private HtmlDataTable validacionesPorDatoTable;
	
	@PostConstruct
	public void init() {
		if (recursosEJB == null) recursosEJB = (Recursos)lookupEJB(SAEProfile.getInstance().EJB_RECURSOS_JNDI);
		if (validacionEJB == null) validacionEJB = (Validaciones)lookupEJB(SAEProfile.getInstance().EJB_VALIDACIONES_JNDI);

	}
	
	public void beforePhase(PhaseEvent event){

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("acciones.validar.assign.title"));

			if (sessionMBean.getRecursoMarcado() == null) {
				addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
			}
		}
		
		
	}
	
	public SessionMBean getSessionMBean() {
		return sessionMBean;
	}
	public void setSessionMBean(SessionMBean sessionMBean) {
		this.sessionMBean = sessionMBean;
	}

	public ValidacionAsignacionSessionMBean getValidacionAsignacionSessionMBean() {
		return validacionAsignacionSessionMBean;
	}
	public void setValidacionAsignacionSessionMBean(
			ValidacionAsignacionSessionMBean validacionAsignacionSessionMBean) {
		this.validacionAsignacionSessionMBean = validacionAsignacionSessionMBean;
	}

	public List<ValidacionPorRecurso> getValidacionesDelRecurso() {
		
		if (validacionesDelRecurso == null) {
		
			validacionesDelRecurso = new ArrayList<ValidacionPorRecurso>();
			
			try {
				if (sessionMBean.getRecursoMarcado() != null) {
					validacionesDelRecurso = validacionEJB.obtenerValidacionesDelRecurso(sessionMBean.getRecursoMarcado());
				}
			} catch(Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		}
		
		return validacionesDelRecurso;
	}
	public void setValidacionesDelRecurso(List<ValidacionPorRecurso> validacionesDelRecurso) {
		this.validacionesDelRecurso = validacionesDelRecurso;
	}

	public HtmlDataTable getValidacionesDelRecursoTable() {
		return validacionesDelRecursoTable;
	}
	public void setValidacionesDelRecursoTable(HtmlDataTable validacionesDelRecursoTable) {
		this.validacionesDelRecursoTable = validacionesDelRecursoTable;
	}
	
	
	public Boolean getHayAlgunaValidacionPorDato() {
		Boolean existeAlguna = false;
		try {
			
			ValidacionPorRecurso vxr = validacionAsignacionSessionMBean.getValidacionDelRecurso();
			if (vxr != null && vxr.getId() != null) {
				existeAlguna = validacionEJB.obtenerAsociacionesValidacionPorDato(vxr).size() > 0;
			}
						   
		} catch (ApplicationException e) {
			addErrorMessage(e, MSG_ID);
		}
									
		return existeAlguna;
	}
	
	public List<SelectItem> getValidacionesItems() {
		
		if (validacionesItems == null) {
			
			validacionesItems = new ArrayList<SelectItem>();
	
			try {
				for (Validacion v: validacionAsignacionSessionMBean.getValidaciones()) {
					validacionesItems.add(new SelectItem(v, v.getNombre()));
				}
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		}
		
		return validacionesItems;
	}
	
	public HtmlDataTable getValidacionesPorDatoTable() {
		return validacionesPorDatoTable;
	}
	public void setValidacionesPorDatoTable(HtmlDataTable validacionesPorDatoTable) {
		this.validacionesPorDatoTable = validacionesPorDatoTable;
	}
	
	public List<SelectItem> getParametrosDeLaValidacionItems() {
		
		List<SelectItem> parametrosDeLaValidacionItems = new ArrayList<SelectItem>();

		if (validacionAsignacionSessionMBean.getParametrosValidacion() != null) {

			for (String nombreParametro: validacionAsignacionSessionMBean.getNombresParametrosValidacion()) {
				parametrosDeLaValidacionItems.add(new SelectItem(nombreParametro, nombreParametro));
			}
		}
		return parametrosDeLaValidacionItems;
	}
	
	private void cargarParametrosDeLaValidacion() {
		
		if (sessionMBean.getRecursoMarcado() != null) {

			List<String> nombreParametros = new ArrayList<String>();
			List<ParametroValidacion> parametros = new ArrayList<ParametroValidacion>();
			
			try {

				Validacion v = validacionAsignacionSessionMBean.getValidacionDelRecurso().getValidacion();
				parametros = validacionEJB.consultarParametrosDeLaValidacion(v);
				for (ParametroValidacion p : parametros) {
					nombreParametros.add(p.getNombre());
				}

			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}

			validacionAsignacionSessionMBean.setNombresParametrosValidacion(nombreParametros);
			validacionAsignacionSessionMBean.setParametrosValidacion(parametros);

			refrescarListaParametros(null);
		}
	}
	
	public void cambioValidacionDelRecurso(ActionEvent event) {
		
		cargarParametrosDeLaValidacion();
	}
	
	public void refrescarListaParametros(ActionEvent event) {

		List<ParametroValidacion> copia = validacionAsignacionSessionMBean.getParametrosValidacion();
		validacionAsignacionSessionMBean.setNombresParametrosValidacion(new ArrayList<String>());
		for (ParametroValidacion p : copia) {
			validacionAsignacionSessionMBean.getNombresParametrosValidacion().add(p.getNombre());
		}
		
		for ( ValidacionPorDato vxd : validacionAsignacionSessionMBean.getValidacionDelRecurso().getValidacionesPorDato()) {
			validacionAsignacionSessionMBean.getNombresParametrosValidacion().remove(vxd.getNombreParametro());
		}
	}

	
	
	public List<SelectItem> getDatosASolicitarItems() {
		
		List<SelectItem> datosASolicitarItems = new ArrayList<SelectItem>();
		
		for (DatoASolicitar d : validacionAsignacionSessionMBean.getDatosASolicitarDelRecurso()) {
				datosASolicitarItems.add(new SelectItem(d, d.getNombre()));
		}
		
		return datosASolicitarItems;
	}
	
	private void cargarDatosASolicitar() {
		
		if (sessionMBean.getRecursoMarcado() != null) {

			List<DatoASolicitar> campos = new ArrayList<DatoASolicitar>();
			
			try {
				campos = recursosEJB.consultarDatosSolicitar(sessionMBean.getRecursoMarcado());

			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}

			validacionAsignacionSessionMBean.setDatosASolicitarDelRecurso(campos);
			validacionAsignacionSessionMBean.setDatosASolicitarDelRecursoCopia(new ArrayList<DatoASolicitar>(campos));
			
			refrescarListaDatosASolicitar(null);
		}
	}
	
	public void refrescarListaDatosASolicitar(ActionEvent event) {

		List<DatoASolicitar> copia = validacionAsignacionSessionMBean.getDatosASolicitarDelRecursoCopia();
		validacionAsignacionSessionMBean.setDatosASolicitarDelRecurso(new ArrayList<DatoASolicitar>(copia));

		for ( ValidacionPorDato vxd : validacionAsignacionSessionMBean.getValidacionDelRecurso().getValidacionesPorDato()) {
			validacionAsignacionSessionMBean.getDatosASolicitarDelRecurso().remove(vxd.getDatoASolicitar());
		}
	}
	

	public void eliminar(ActionEvent event) {
		ValidacionPorRecurso vxr = (ValidacionPorRecurso)getValidacionesDelRecursoTable().getRowData();
		try {
			
			validacionEJB.eliminarValidacionPorRecurso(vxr);
			
			this.setValidacionesDelRecurso(null);
			validacionAsignacionSessionMBean.setModoCreacion(false);
			validacionAsignacionSessionMBean.setModoEdicion(false);
			validacionAsignacionSessionMBean.setValidacionDelRecurso(null);
		}
		catch (Exception e) {
			addErrorMessage(e,MSG_ID);
		}
	}	
	

	public void editar(ActionEvent event) {
		
		ValidacionPorRecurso vxr = (ValidacionPorRecurso)getValidacionesDelRecursoTable().getRowData();
		
		try {

			List<Validacion> validaciones = validacionEJB.consultarValidaciones();
			List<ParametroValidacion> parametros = validacionEJB.consultarParametrosDeLaValidacion(vxr.getValidacion());
			List<ValidacionPorDato> asignaciones = validacionEJB.obtenerAsociacionesValidacionPorDato(vxr);
			
			vxr.setValidacionesPorDato(asignaciones);

		/*	Map<Integer, ValidacionPorDato> asignacionesMap = new HashMap<Integer, ValidacionPorDato>();
			for (ValidacionPorDato vxd : asignaciones) {
				asignacionesMap.put(vxd.getId(), vxd);
			}
		*/	
			validacionAsignacionSessionMBean.setValidaciones(validaciones);
			validacionAsignacionSessionMBean.setValidacionDelRecurso(vxr);
			validacionAsignacionSessionMBean.setParametrosValidacion(parametros);


		//	validacionAsignacionSessionMBean.setAsignacionesMap(asignacionesMap);
			
			validacionAsignacionSessionMBean.setModoEdicion(true);
			validacionAsignacionSessionMBean.setModoCreacion(false);
			
		} catch (Exception e) {
			addErrorMessage(e, MSG_ID);
		}
		
		cargarDatosASolicitar();
		cargarParametrosDeLaValidacion();
	}
	
	public void guardarEdicion(ActionEvent event) {

		try {
			
			validacionEJB.modificarValidacionPorRecurso(validacionAsignacionSessionMBean.getValidacionDelRecurso());
			
			addInfoMessage(getI18N().getText("message.change_saved"), MSG_ID);
			this.setValidacionesDelRecurso(null);
			validacionAsignacionSessionMBean.setModoEdicion(false);
			validacionAsignacionSessionMBean.setValidacionDelRecurso(null);
			
		} catch (Exception e) {
			addErrorMessage(e , MSG_ID);
		}
		
	}
	
	public void cancelarEdicion(ActionEvent e) {
		validacionAsignacionSessionMBean.setModoEdicion(false);
		validacionAsignacionSessionMBean.setValidacionDelRecurso(null);
	}	
	

	public void crear(ActionEvent event) {

		ValidacionPorRecurso vxr = new ValidacionPorRecurso();
		vxr.setRecurso(sessionMBean.getRecursoMarcado());
		
		try {

			List<Validacion> validaciones = validacionEJB.consultarValidaciones();
			List<ParametroValidacion> parametros = new ArrayList<ParametroValidacion>();
			List<ValidacionPorDato> asignaciones = new ArrayList<ValidacionPorDato>();
			
			vxr.setValidacionesPorDato(asignaciones);
		//	Map<Integer, ValidacionPorDato> asignacionesMap = new HashMap<Integer, ValidacionPorDato>();
			
			validacionAsignacionSessionMBean.setValidaciones(validaciones);
			validacionAsignacionSessionMBean.setValidacionDelRecurso(vxr);
			validacionAsignacionSessionMBean.setParametrosValidacion(parametros);


		//	validacionAsignacionSessionMBean.setAsignacionesMap(asignacionesMap);
			
			validacionAsignacionSessionMBean.setModoEdicion(false);
			validacionAsignacionSessionMBean.setModoCreacion(true);
			
		} catch (Exception e) {
			addErrorMessage(e, MSG_ID);
		}
		
		cargarDatosASolicitar();
		cargarParametrosDeLaValidacion();
	}

	public void guardarCreacion(ActionEvent event) {

		try {
			
			validacionEJB.crearValidacionPorRecurso(validacionAsignacionSessionMBean.getValidacionDelRecurso());

			addInfoMessage(getI18N().getText("message.change_saved"), MSG_ID);
			this.setValidacionesDelRecurso(null);
			validacionAsignacionSessionMBean.setModoCreacion(false);
			validacionAsignacionSessionMBean.setValidacionDelRecurso(null);
			
		} catch (Exception e) {
			addErrorMessage(e , MSG_ID);
		}
		
	}
	
	public void cancelarCreacion(ActionEvent e) {
		validacionAsignacionSessionMBean.setModoCreacion(false);
		validacionAsignacionSessionMBean.setValidacionDelRecurso(null);
	}	
	
	public void eliminarValidacionPorDato (ActionEvent event) {
		
		ValidacionPorDato vxd = (ValidacionPorDato)validacionesPorDatoTable.getRowData();
		
		validacionAsignacionSessionMBean.getValidacionDelRecurso().getValidacionesPorDato().remove(vxd);
	
		refrescarListaDatosASolicitar(null);
		refrescarListaParametros(null);
	}

	public void crearValidacionPorDato (ActionEvent event) {
		
		ValidacionPorDato vxd = new ValidacionPorDato();
		validacionAsignacionSessionMBean.getValidacionDelRecurso().getValidacionesPorDato().add(vxd);
	}
	

}
