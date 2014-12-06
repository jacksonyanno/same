/*
 * SAE - Sistema de Agenda Electrónica
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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;

import uy.gub.imm.sae.business.api.AgendaGeneral;
import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.DatoDelRecurso;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.web.common.RowList;
import uy.gub.imm.sae.web.common.RowListMultipleSelect;
import uy.gub.imm.sae.web.common.RowMultipleSelect;
import uy.gub.imm.sae.web.common.SessionCleanerMBean;

import com.sagant.same.web.mbean.administracion.AgendaSelectionMBean;


public class SessionMBean extends SessionCleanerMBean {
	
		
	@EJB(name="ejb/AgendaGeneralBean")
	private AgendaGeneral generalEJB;
	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;

	private AgendaSelectionMBean agendaSelectionMBean;
	
	
	//Pagina que se debe desplegar en la sección "pantalla" de la pagina principal
	private String viewId;
	private String pantallaTitulo;
	
	//Lista de Agenda y recurso seleccionados para trabajar sobre el resto de los elementos.
//	private RowList<Agenda> agendas;
//	private RowList<Recurso> recursos;
	private RowList<DatoDelRecurso> datosDelRecurso;
//	private RowList<AgrupacionDato> agrupacionesDatos;
	/** Lista de recursos con soporte para multiple selección para construir el llamador */
	private RowListMultipleSelect<Recurso> recursosMultipleSelect;

	
	//Agenda/Recurso seleccionados para modificacion
	// es necesario pues al navegar de la pagina modificarConsultar a modificar se pierde
	// la row seleccionada en la tabla de agendas/recursos :(
	private Agenda agendaSeleccionada;
	private Recurso recursoSeleccionado;
	private DatoDelRecurso datoDelRecursoSeleccionado;
//	private AgrupacionDato agrupacionDatoSeleccionada;
	//private CupoPorDia cupoPorDiaSeleccionado;	

	//Booleana para saber si se despliega la tabla de Dato del Recurso
	private Boolean mostrarDato = true;
	//Booleana para saber si se despliega la tabla para agregar Dato del Recurso
	private Boolean mostrarAgregarDato = false;

	//Booleana para saber si se despliega la tabla de modificación de Agrupaciones
	private Boolean mostrarAgrupacion = false;
	//Booleana para saber si se despliega la tabla para agregar Agrupaciones
	private Boolean mostrarAgregarAgrupacion = false;
	
	//Booleana para saber si se utiliza llamador o no
	private Boolean mostrarLlamador = true;
	
	//private DatosUrlToForwardReserva datosUrlToForwardReserva = new DatosUrlToForwardReserva();
	
	
	private int pagina;
	
	private Map<String, DatoASolicitar> datosASolicitar;
	
	//Numero de puesto asignado al usuario en el momento de atender reservas con el modulo Llamador
	private Integer puesto = 0;
	
	
	//Estado que indica si estoy creando un grupo nuevo en la pantalla de mantenimento de grupos/agendas
	private Boolean creandoGrupo;
	
	

	public Boolean getCreandoGrupo() {
		return creandoGrupo;
	}
	public void setCreandoGrupo(Boolean creandoGrupo) {
		this.creandoGrupo = creandoGrupo;
	}
	
	public int getPagina() {
		return pagina;
	}
	public void setPagina(int pagina) {
		this.pagina = pagina;
	}

	public String getViewId() {
		if (viewId == null) {
			viewId = "/administracion/inicio.xhtml";
		}
		return viewId;
	}
	public void setViewId(String viewId) {
		this.viewId = viewId;
	}
	
	public String getPantallaTitulo() {
		return pantallaTitulo;
	}
	
	public void setPantallaTitulo(String pantallaTitulo) {
		this.pantallaTitulo = pantallaTitulo;
	}

	public void beforePhaseInicio(PhaseEvent event) {

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			setPantallaTitulo("Inicio");
		}
	}

	public void beforePhaseSeleccionAgendaRecurso(PhaseEvent event) {
		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {					
			setPantallaTitulo(getI18N().getText("menu.agenda_recurso.title"));			
		}
	}
	
	
	public RowList<Agenda> getAgendas() {
		return new RowList<Agenda>();
		//return agendas;
	}

	
	//Agenda seleccionada en pantalla de selección de agendas y recursos
	public Agenda getAgendaMarcada() {
		
		Recurso recurso = agendaSelectionMBean.getSelected();
		if (recurso != null) {
			return recurso.getAgenda();
		}
		else {
			return null;
		}

		/*if (agendas != null && agendas.getSelectedRow() != null){
			return agendas.getSelectedRow().getData();
		}
		else {
			return null;
		}*/
	}

	public void desmarcarAgenda(){
		
		agendaSelectionMBean.select(null);

		//agendas.setSelectedRow(null);
	}

	
	public RowList<Recurso> getRecursos() {
		return new RowList<Recurso>();
		//return recursos;
	}
	
	/** Retorno el rowlist con soporte para seleccion multiple siempre con el recurso marcado con el que se está trabajando.
	 * El recurso marcado es el que utiliza como prioritario para armar el llamador. 
	 */
	public RowListMultipleSelect<Recurso> getRecursosMultipleSelect() {
		
		
		if (getAgendaSelectionMBean().getSelected() == null) {
			
			return new RowListMultipleSelect<Recurso>();			
		}
		else if (recursosMultipleSelect == null) {

			recursosMultipleSelect = new RowListMultipleSelect<Recurso>(getAgendaSelectionMBean().getSelected().getAgenda().getRecursos());
			

		} else {
			if (recursosMultipleSelect.size() != getAgendaSelectionMBean().getSelected().getAgenda().getRecursos().size()) {
				recursosMultipleSelect = new RowListMultipleSelect<Recurso>(getAgendaSelectionMBean().getSelected().getAgenda().getRecursos());
			}
			else {
				boolean reset = false;
				for (RowMultipleSelect<Recurso> row : recursosMultipleSelect ) {
					if (! getAgendaSelectionMBean().getSelected().getAgenda().getRecursos().contains(row.getData())) {
						reset = true;
					}
				}
				if (reset) {
					recursosMultipleSelect = new RowListMultipleSelect<Recurso>(getAgendaSelectionMBean().getSelected().getAgenda().getRecursos());
				}
			}
				
		}

		int selectedIndex = recursosMultipleSelect.indexOf(new RowMultipleSelect<Recurso>(agendaSelectionMBean.getSelected(), null));
		//Por construcción nunca debería devolver -1 pero por programacion preventiva, lo chequeo y retorno vacio
		if (selectedIndex == -1) {return new RowListMultipleSelect<Recurso>();}

		recursosMultipleSelect.get(selectedIndex).setSelected(true);

		return recursosMultipleSelect;
		
	}
	
	public void setRecursosMultipleSelect(RowListMultipleSelect<Recurso> recursosMultipleSelect) {
		this.recursosMultipleSelect = recursosMultipleSelect;
	}
	
	//Recurso seleccionado en pantalla de selección de agendas y recursos	
	public Recurso getRecursoMarcado() {
		
		return agendaSelectionMBean.getSelected();

		/*if (recursos != null && recursos.getSelectedRow() != null){
			return recursos.getSelectedRow().getData();
		}
		else {
			return null;
		}*/
	}
	
	/** Retorna los recursos marcados para el llamador */
	/*public List<Recurso> getRecursosMarcados() {
		List<Recurso> recursos = null;
		if (recursosMultipleSelect != null ){
			recursos = new ArrayList<Recurso>();
			for (RowMultipleSelect<Recurso> row : recursosMultipleSelect.getSelectedRows()) {
				recursos.add(row.getData());
			}
		}
		return recursos;
	}*/
	
	public void desmarcarRecurso(){
		
		agendaSelectionMBean.select(null);
		//recursos.setSelectedRow(null);
	}

	public RowList<DatoDelRecurso> getDatosDelRecurso() {
		cargarDatosDelRecurso();
		return datosDelRecurso;
	}

	public void setDatosDelRecurso(RowList<DatoDelRecurso> datosDelRecurso) {
		this.datosDelRecurso = datosDelRecurso;
	}
	
	public Agenda getAgendaSeleccionada() {
		return agendaSeleccionada;
	}
	public void setAgendaSeleccionada(Agenda agenda) {
		this.agendaSeleccionada = agenda;
	}
	
	public Recurso getRecursoSeleccionado() {
		return recursoSeleccionado;
	}
	
	public void setRecursoSeleccionado(Recurso recurso) {
		this.recursoSeleccionado = recurso;
		if (recurso != null) {
			//this.desmarcarRecurso();
		}
	}

	public void desseleccionarRecurso(ActionEvent a){
		removeMBeansFromSession();
		this.setRecursoSeleccionado(null);
	}
	
	public DatoDelRecurso getDatoDelRecursoSeleccionado() {
		return datoDelRecursoSeleccionado;
	}
	public void setDatoDelRecursoSeleccionado(
			DatoDelRecurso datoDelRecursoSeleccionado) {
		this.datoDelRecursoSeleccionado = datoDelRecursoSeleccionado;
	}

	public Boolean getMostrarAgregarDato() {
		return mostrarAgregarDato;
	}

	public void setMostrarAgregarDato(Boolean mostrarAgregarDato) {
		this.mostrarAgregarDato = mostrarAgregarDato;
	}
	
	//Es llamado cada vez que se marca/desmarca en la tabla de agendas del panel modal.
	/*public void seleccionarAgenda(ActionEvent event) {
		removeMBeansFromSession();
		cargarRecursos();
	}*/
	
	@PostConstruct
	public void init() {
		
		
		//agendaSelectionMBean = (AgendaSelectionMBean)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("agendaSelectionMBean");


		if (generalEJB  == null) generalEJB  = (AgendaGeneral)lookupEJB(SAEProfile.getInstance().EJB_AGENDA_GENERAL_JNDI);
		if (recursosEJB == null) recursosEJB = (Recursos)lookupEJB(SAEProfile.getInstance().EJB_RECURSOS_JNDI);

		
		//List<Agenda> entidades;
		//try {
			//entidades = generalEJB.consultarAgendas();
			//agendas = new RowList<Agenda>(entidades);
			//if (recursos != null) {
			//	recursos.clear();
			//}
		//} catch (Exception e) {
		//	addErrorMessage(e, MSG_ID);
		//}
	}
	
	
	//Si hay agenda selecciondada, se cargan los recursos asociados.
	//En caso contrario se vacía la lista de recursos
	/*public void cargarRecursos() {
		
		if (getAgendaMarcada() != null){
			try {
				List<Recurso> entidades;
				entidades = generalEJB.consultarRecursos(getAgendaMarcada());
				//recursos = new RowList<Recurso>(entidades);
				recursosMultipleSelect = new RowListMultipleSelect<Recurso>(entidades);
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		}
		else {
			if (recursosMultipleSelect != null) {
				//recursos.clear();
				recursosMultipleSelect.clear();
			}
		}
	}*/	

	
	public AgendaSelectionMBean getAgendaSelectionMBean() {
		return this.agendaSelectionMBean;
	}
	public void setAgendaSelectionMBean(AgendaSelectionMBean agendaSelectionMBean) {
		this.agendaSelectionMBean = agendaSelectionMBean;
	}

	//Si hay recurso selecciondada, se cargan los datos del recurso asociados.
	//En caso contrario se vacía la lista de datosDelRecurso
	public void cargarDatosDelRecurso() {
		
		if (this.getRecursoSeleccionado() != null){			
			try {
				List<DatoDelRecurso> entidades;
				entidades = recursosEJB.consultarDatosDelRecurso(this.getRecursoSeleccionado());
				datosDelRecurso = new RowList<DatoDelRecurso>(entidades);
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		}
		else {
			if (datosDelRecurso != null) {
				datosDelRecurso.clear();
			}
		}
	}	

	public Boolean getMostrarDato() {

		if (this.getDatoDelRecursoSeleccionado() != null ) {
			mostrarDato = true;
		}
		else {
			mostrarDato = false;
		}
		return mostrarDato;
	}


	public void setMostrarDato(Boolean mostrarDato) {
		this.mostrarDato = mostrarDato;
	}

/*	
	public RowList<AgrupacionDato> getAgrupacionesDatos() {
		cargarAgrupaciones();
		return agrupacionesDatos;
	}
	*/
	
/*
	public AgrupacionDato getAgrupacionDatoSeleccionada() {
		return agrupacionDatoSeleccionada;
	}
	
	public void setAgrupacionDatoSeleccionada(
			AgrupacionDato agrupacionDatoSeleccionada) {
		this.agrupacionDatoSeleccionada = agrupacionDatoSeleccionada;
	}	
*/
	public Boolean getMostrarAgrupacion() {
			
	/*	if (this.getAgrupacionDatoSeleccionada() != null) {
			mostrarAgrupacion = true;
		}
		else {
			mostrarAgrupacion = false;
		}
	*/	
		return mostrarAgrupacion;
	}


	public void setMostrarAgrupacion(Boolean mostrarAgrupacion) {
		this.mostrarAgrupacion = mostrarAgrupacion;
	}

	public Boolean getMostrarAgregarAgrupacion() {
		return mostrarAgregarAgrupacion;
	}
	
	public void setMostrarAgregarAgrupacion(Boolean mostrarAgregarAgrupacion) {
		this.mostrarAgregarAgrupacion = mostrarAgregarAgrupacion;
	}

	
	/*
	public CupoPorDia getCupoPorDiaSeleccionado() {
		return cupoPorDiaSeleccionado;
	}
	
	public void setCupoPorDiaSeleccionado(CupoPorDia cupoPorDiaSeleccionado) {
		this.cupoPorDiaSeleccionado = cupoPorDiaSeleccionado;
	}
	*/

	/*public DatosUrlToForwardReserva getDatosUrlToForwardReserva() {
		return datosUrlToForwardReserva;
	}
	
	public void setDatosUrlToForwardReserva(DatosUrlToForwardReserva datosUrlToForwardReserva) {
		this.datosUrlToForwardReserva = datosUrlToForwardReserva;
	}*/
	
	// genera la url a la que se redirecciona para reservar
	/*
	public void evaluarForwardToReservar(ActionEvent e) {
		if(getAgendaMarcada()!=null){
			// hay agenda marcada
			datosUrlToForwardReserva.setSePuedeHacerForwardAgendar(true);
			datosUrlToForwardReserva.setUrlReservaAgendaToForward(URL_BASE_TO_FORWARD_RESERVA + getAgendaMarcada().getNombre());
		} else {
			datosUrlToForwardReserva.setSePuedeHacerForwardAgendar(false);
			datosUrlToForwardReserva.setUrlReservaAgendaToForward("#");
			addErrorMessage("Debe tener una agenda seleccionada", MSG_ID);
		}
	}
	*/
	
	/*public String getUrlAgendarReservas() {
		
		String urlAgendarReserva = "#";
		
		if (getAgendaMarcada() != null && getRecursoMarcado() != null) {
			
			FacesContext ctx = FacesContext.getCurrentInstance();
			HttpServletRequest request = (HttpServletRequest)ctx.getExternalContext().getRequest();
			String urlRetorno = "#";
			
			try {
				urlRetorno = URLEncoder.encode(request.getRequestURL().toString(), "utf-8");
				
				urlAgendarReserva = request.getContextPath() + URL_BASE_TO_FORWARD_RESERVA + URLEncoder.encode(getAgendaMarcada().getNombre(),"utf-8") +
									"&recurso=" + URLEncoder.encode(getRecursoMarcado().getNombre(),"utf-8") +
									"&pagina_retorno=" + urlRetorno;
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
		}
		
		return urlAgendarReserva;
	}*/

	/*public class DatosUrlToForwardReserva {
		private String urlReservaAgendaToForward;
		private boolean sePuedeHacerForwardAgendar;
		
		public DatosUrlToForwardReserva(){
			urlReservaAgendaToForward = "#";
			sePuedeHacerForwardAgendar = false;
		}
		
		public String getUrlReservaAgendaToForward() {
			return urlReservaAgendaToForward;
		}
		
		public void setUrlReservaAgendaToForward(String urlReservaAgendaToForward) {
			this.urlReservaAgendaToForward = urlReservaAgendaToForward;
		}
		
		public boolean isSePuedeHacerForwardAgendar() {
			return sePuedeHacerForwardAgendar;
		}
		
		public void setSePuedeHacerForwardAgendar(boolean sePuedeHacerForwardAgendar) {
			this.sePuedeHacerForwardAgendar = sePuedeHacerForwardAgendar;
		}
	}*/

	public Map<String, DatoASolicitar> getDatosASolicitar() {
		return datosASolicitar;
	}
	public void setDatosASolicitar(Map<String, DatoASolicitar> datosASolicitar) {
		this.datosASolicitar = datosASolicitar;
	}
	public Integer getPuesto() {
		return puesto;
	}
	public void setPuesto(Integer puesto) {
		this.puesto = puesto;
	}

	public Boolean getMostrarLlamador() {
		
		Recurso recurso = agendaSelectionMBean.getSelected();
		if (recurso != null){
			mostrarLlamador = recurso.getUsarLlamador();
		}else {
			mostrarLlamador = true;
		}
		
		/*if (recursos != null && recursos.getSelectedRow() != null){
			mostrarLlamador = recursos.getSelectedRow().getData().getUsarLlamador();
		}else {
			mostrarLlamador = true;
		}*/
		return mostrarLlamador;
	}
	
	public void setMostrarLlamador(Boolean mostrarLlamador) {
		this.mostrarLlamador = mostrarLlamador;
	}


	public String gotToBookingPage() {

		if (agendaSelectionMBean.getSelected() == null) {
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
		}
		else {
			
			FacesContext ctx = FacesContext.getCurrentInstance();
			try {
				ctx.getExternalContext().redirect(getBooking4AgendaURL(agendaSelectionMBean.getSelected()));
			} catch (IOException e) {
				addErrorMessage(e, MSG_ID);
				
				//TODO manejar logger
				e.printStackTrace();
			}
		}

		return null;
	}

}


