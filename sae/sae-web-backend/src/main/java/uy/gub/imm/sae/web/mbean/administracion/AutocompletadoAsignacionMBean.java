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

import uy.gub.imm.sae.business.api.Autocompletados;
import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.ParametrosAutocompletar;
import uy.gub.imm.sae.entity.ServicioAutocompletar;
import uy.gub.imm.sae.entity.ServicioAutocompletarPorDato;
import uy.gub.imm.sae.entity.ServicioPorRecurso;
import uy.gub.imm.sae.web.common.BaseMBean;


public class AutocompletadoAsignacionMBean extends BaseMBean{

	public static final String MSG_ID = "pantalla";
	
	@EJB(name="ejb/AutocompletadosBean")
	private Autocompletados autocompletadoEJB;

	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;
	
	private SessionMBean sessionMBean;
	private AutocompletadoAsignacionSessionMBean autocompletadoAsignacionSessionMBean;

	
	private List<ServicioPorRecurso> autocompletadosDelRecurso;
	private HtmlDataTable autocompletadosDelRecursoTable;
	
	private List<SelectItem> autocompletadosItems;

	private HtmlDataTable autocompletadosPorDatoTable;
	
	@PostConstruct
	public void init() {
		if (recursosEJB == null) recursosEJB = (Recursos)lookupEJB(SAEProfile.getInstance().EJB_RECURSOS_JNDI);
		if (autocompletadoEJB  == null) autocompletadoEJB = (Autocompletados)lookupEJB(SAEProfile.getInstance().EJB_AUTOCOMPLETADOS_JNDI);

	}
	public void beforePhase(PhaseEvent event){

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("acciones.autocompletar.assign.title"));

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

	public AutocompletadoAsignacionSessionMBean getAutocompletadoAsignacionSessionMBean() {
		return autocompletadoAsignacionSessionMBean;
	}
	public void setAutocompletadoAsignacionSessionMBean(
			AutocompletadoAsignacionSessionMBean autocompletadoAsignacionSessionMBean) {
		this.autocompletadoAsignacionSessionMBean = autocompletadoAsignacionSessionMBean;
	}

	public List<ServicioPorRecurso> getAutocompletadosDelRecurso() {
		
		if (autocompletadosDelRecurso == null) {
		
			autocompletadosDelRecurso = new ArrayList<ServicioPorRecurso>();
			
			try {
				if (sessionMBean.getRecursoMarcado() != null) {
					autocompletadosDelRecurso = autocompletadoEJB.obtenerAutocompletadosDelRecurso(sessionMBean.getRecursoMarcado());
				}
			} catch(Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		}
		
		return autocompletadosDelRecurso;
	}
	public void setAutocompletadosDelRecurso(List<ServicioPorRecurso> autocompletadosDelRecurso) {
		this.autocompletadosDelRecurso = autocompletadosDelRecurso;
	}

	public HtmlDataTable getAutocompletadosDelRecursoTable() {
		return autocompletadosDelRecursoTable;
	}
	public void setAutocompletadosDelRecursoTable(HtmlDataTable autocompletadosDelRecursoTable) {
		this.autocompletadosDelRecursoTable = autocompletadosDelRecursoTable;
	}
	
	
	public Boolean getHayAlgunaAutocompletadoPorDato() {
		Boolean existeAlguna = false;
		try {
			
			ServicioPorRecurso vxr = autocompletadoAsignacionSessionMBean.getAutocompletadoDelRecurso();
			if (vxr != null && vxr.getId() != null) {
				existeAlguna = autocompletadoEJB.obtenerAsociacionesAutocompletadoPorDato(vxr).size() > 0;
			}
						   
		} catch (ApplicationException e) {
			addErrorMessage(e, MSG_ID);
		}
									
		return existeAlguna;
	}
	
	public List<SelectItem> getAutocompletadosItems() {
		
		if (autocompletadosItems == null) {
			
			autocompletadosItems = new ArrayList<SelectItem>();
	
			try {
				for (ServicioAutocompletar s: autocompletadoAsignacionSessionMBean.getAutocompletados()) {
					autocompletadosItems.add(new SelectItem(s, s.getNombre()));
				}
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		}
		
		return autocompletadosItems;
	}
	
	public HtmlDataTable getAutocompletadosPorDatoTable() {
		return autocompletadosPorDatoTable;
	}
	public void setAutocompletadosPorDatoTable(HtmlDataTable autocompletadosPorDatoTable) {
		this.autocompletadosPorDatoTable = autocompletadosPorDatoTable;
	}
	
	public List<SelectItem> getParametrosDeLaAutocompletadoItems() {
		
		List<SelectItem> parametrosDeLaAutocompletadoItems = new ArrayList<SelectItem>();

		if (autocompletadoAsignacionSessionMBean.getParametrosAutocompletar() != null) {

			for (String nombreParametro: autocompletadoAsignacionSessionMBean.getNombresParametrosAutocompletar()) {
				parametrosDeLaAutocompletadoItems.add(new SelectItem(nombreParametro, nombreParametro));
			}
		}
		return parametrosDeLaAutocompletadoItems;
	}
	
	private void cargarParametrosDeLaAutocompletado() {
		
		if (sessionMBean.getRecursoMarcado() != null) {

			List<String> nombreParametros = new ArrayList<String>();
			List<ParametrosAutocompletar> parametros = new ArrayList<ParametrosAutocompletar>();
			
			try {

				ServicioAutocompletar s = autocompletadoAsignacionSessionMBean.getAutocompletadoDelRecurso().getAutocompletado();
				parametros = autocompletadoEJB.consultarParametrosDelAutoCompletado(s);
				for (ParametrosAutocompletar p : parametros) {
					nombreParametros.add(p.getNombre());
				}

			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}

			autocompletadoAsignacionSessionMBean.setNombresParametrosAutocompletar(nombreParametros);
			autocompletadoAsignacionSessionMBean.setParametrosAutocompletar(parametros);

			refrescarListaParametros(null);
		}
	}
	
	public void cambioAutocompletadoDelRecurso(ActionEvent event) {
		
		cargarParametrosDeLaAutocompletado();
	}
	
	public void refrescarListaParametros(ActionEvent event) {

		List<ParametrosAutocompletar> copia = autocompletadoAsignacionSessionMBean.getParametrosAutocompletar();
		autocompletadoAsignacionSessionMBean.setNombresParametrosAutocompletar(new ArrayList<String>());
		for (ParametrosAutocompletar p : copia) {
			autocompletadoAsignacionSessionMBean.getNombresParametrosAutocompletar().add(p.getNombre());
		}
		
		for ( ServicioAutocompletarPorDato sxd : autocompletadoAsignacionSessionMBean.getAutocompletadoDelRecurso().getAutocompletadosPorDato()) {
			autocompletadoAsignacionSessionMBean.getNombresParametrosAutocompletar().remove(sxd.getNombreParametro());
		}
	}

	
	
	public List<SelectItem> getDatosASolicitarItems() {
		
		List<SelectItem> datosASolicitarItems = new ArrayList<SelectItem>();
		
		for (DatoASolicitar d : autocompletadoAsignacionSessionMBean.getDatosASolicitarDelRecurso()) {
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

			autocompletadoAsignacionSessionMBean.setDatosASolicitarDelRecurso(campos);
			autocompletadoAsignacionSessionMBean.setDatosASolicitarDelRecursoCopia(new ArrayList<DatoASolicitar>(campos));
			
			refrescarListaDatosASolicitar(null);
		}
	}
	
	public void refrescarListaDatosASolicitar(ActionEvent event) {

		List<DatoASolicitar> copia = autocompletadoAsignacionSessionMBean.getDatosASolicitarDelRecursoCopia();
		autocompletadoAsignacionSessionMBean.setDatosASolicitarDelRecurso(new ArrayList<DatoASolicitar>(copia));

		for ( ServicioAutocompletarPorDato sxd : autocompletadoAsignacionSessionMBean.getAutocompletadoDelRecurso().getAutocompletadosPorDato()) {
			autocompletadoAsignacionSessionMBean.getDatosASolicitarDelRecurso().remove(sxd.getDatoASolicitar());
		}
	}
	

	public void eliminar(ActionEvent event) {
		ServicioPorRecurso vxr = (ServicioPorRecurso)getAutocompletadosDelRecursoTable().getRowData();
		try {
			
			autocompletadoEJB.eliminarAutocompletadoPorRecurso(vxr);
			
			this.setAutocompletadosDelRecurso(null);
			autocompletadoAsignacionSessionMBean.setModoCreacion(false);
			autocompletadoAsignacionSessionMBean.setModoEdicion(false);
			autocompletadoAsignacionSessionMBean.setAutocompletadoDelRecurso(null);
		}
		catch (Exception e) {
			addErrorMessage(e,MSG_ID);
		}
	}	
	

	public void editar(ActionEvent event) {
		
		ServicioPorRecurso sxr = (ServicioPorRecurso)getAutocompletadosDelRecursoTable().getRowData();
		
		try {

			List<ServicioAutocompletar> autocompletados = autocompletadoEJB.consultarAutoCompletados();
			List<ParametrosAutocompletar> parametros = autocompletadoEJB.consultarParametrosDelAutoCompletado(sxr.getAutocompletado());
			List<ServicioAutocompletarPorDato> asignaciones = autocompletadoEJB .obtenerAsociacionesAutocompletadoPorDato(sxr);
			
			sxr.setAutocompletadosPorDato(asignaciones);

			autocompletadoAsignacionSessionMBean.setAutocompletados(autocompletados);
			autocompletadoAsignacionSessionMBean.setAutocompletadoDelRecurso(sxr);
			autocompletadoAsignacionSessionMBean.setParametrosAutocompletar(parametros);
			
			autocompletadoAsignacionSessionMBean.setModoEdicion(true);
			autocompletadoAsignacionSessionMBean.setModoCreacion(false);
			
		} catch (Exception e) {
			addErrorMessage(e, MSG_ID);
		}
		
		cargarDatosASolicitar();
		cargarParametrosDeLaAutocompletado();
	}
	
	public void guardarEdicion(ActionEvent event) {

		try {
			
			autocompletadoEJB.modificarAutocompletadoPorRecurso(autocompletadoAsignacionSessionMBean.getAutocompletadoDelRecurso());
			
			addInfoMessage(getI18N().getText("message.change_saved"), MSG_ID);
			this.setAutocompletadosDelRecurso(null);
			autocompletadoAsignacionSessionMBean.setModoEdicion(false);
			autocompletadoAsignacionSessionMBean.setAutocompletadoDelRecurso(null);
			
		} catch (Exception e) {
			addErrorMessage(e , MSG_ID);
		}
		
	}
	
	public void cancelarEdicion(ActionEvent e) {
		autocompletadoAsignacionSessionMBean.setModoEdicion(false);
		autocompletadoAsignacionSessionMBean.setAutocompletadoDelRecurso(null);
	}	
	

	public void crear(ActionEvent event) {

		ServicioPorRecurso sxr = new ServicioPorRecurso();
		sxr.setRecurso(sessionMBean.getRecursoMarcado());
		
		try {

			List<ServicioAutocompletar> autocompletados = autocompletadoEJB.consultarAutoCompletados();
			List<ParametrosAutocompletar> parametros = new ArrayList<ParametrosAutocompletar>();
			List<ServicioAutocompletarPorDato> asignaciones = new ArrayList<ServicioAutocompletarPorDato>();
			
			sxr.setAutocompletadosPorDato(asignaciones);
			
			autocompletadoAsignacionSessionMBean.setAutocompletados(autocompletados);
			autocompletadoAsignacionSessionMBean.setAutocompletadoDelRecurso(sxr);
			autocompletadoAsignacionSessionMBean.setParametrosAutocompletar(parametros);
			
			autocompletadoAsignacionSessionMBean.setModoEdicion(false);
			autocompletadoAsignacionSessionMBean.setModoCreacion(true);
			
		} catch (Exception e) {
			addErrorMessage(e, MSG_ID);
		}
		
		cargarDatosASolicitar();
		cargarParametrosDeLaAutocompletado();
	}

	public void guardarCreacion(ActionEvent event) {

		try {
			
			autocompletadoEJB.crearAutocompletadoPorRecurso(autocompletadoAsignacionSessionMBean.getAutocompletadoDelRecurso());

			addInfoMessage(getI18N().getText("message.change_saved"), MSG_ID);
			this.setAutocompletadosDelRecurso(null);
			autocompletadoAsignacionSessionMBean.setModoCreacion(false);
			autocompletadoAsignacionSessionMBean.setAutocompletadoDelRecurso(null);
			
		} catch (Exception e) {
			addErrorMessage(e , MSG_ID);
		}
		
	}
	
	public void cancelarCreacion(ActionEvent e) {
		autocompletadoAsignacionSessionMBean.setModoCreacion(false);
		autocompletadoAsignacionSessionMBean.setAutocompletadoDelRecurso(null);
	}	
	
	public void eliminarAutocompletadoPorDato (ActionEvent event) {
		
		ServicioAutocompletarPorDato sxd = (ServicioAutocompletarPorDato)autocompletadosPorDatoTable.getRowData();
		
		autocompletadoAsignacionSessionMBean.getAutocompletadoDelRecurso().getAutocompletadosPorDato().remove(sxd);
	
		refrescarListaDatosASolicitar(null);
		refrescarListaParametros(null);
	}

	public void crearAutocompletadoPorDato (ActionEvent event) {
		
		ServicioAutocompletarPorDato sxd = new ServicioAutocompletarPorDato();
		autocompletadoAsignacionSessionMBean.getAutocompletadoDelRecurso().getAutocompletadosPorDato().add(sxd);
	}

}
