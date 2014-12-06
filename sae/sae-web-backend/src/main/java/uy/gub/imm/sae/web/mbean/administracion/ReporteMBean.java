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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import uy.gub.imm.opencsv.ext.entity.CommonLabelValueImpl;
import uy.gub.imm.opencsv.ext.entity.LabelValue;
import uy.gub.imm.opencsv.ext.entity.TableCellValue;
import uy.gub.imm.opencsv.ext.file.StandardCSVFile;
import uy.gub.imm.opencsv.ext.printer.CSVWebFilePrinter;
import uy.gub.imm.sae.business.api.Consultas;
import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.business.api.dto.ReservaDTO;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.Utiles;
import uy.gub.imm.sae.common.VentanaDeTiempo;
import uy.gub.imm.sae.common.enumerados.Estado;
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;
import uy.gub.imm.sae.web.common.BaseMBean;
import uy.gub.imm.sae.web.common.reporte.Columna;
import uy.gub.imm.sae.web.common.reporte.ReporteProvider;

public class ReporteMBean extends BaseMBean {

	
	@EJB(name="ejb/ConsultasBean")
	private Consultas consultaEJB;
	
	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;
	
	private Date fechaDesde;
	private Date fechaHasta;
	private Estado estado;
	private Boolean unaPaginaPorHora;
	private String estadoDescripcion;
	private List<SelectItem> estadosReserva =  new ArrayList<SelectItem>();
	private Estado estadoReservaSeleccionado;
	
	private SessionMBean sessionMBean;

	public static final String MSG_ID = "pantalla";

	@PostConstruct
	public void cargarDatos(){
		
		if (consultaEJB  == null) consultaEJB = (Consultas)lookupEJB(SAEProfile.getInstance().EJB_CONSULTAS_JNDI);
		if (recursosEJB == null) recursosEJB = (Recursos)lookupEJB(SAEProfile.getInstance().EJB_RECURSOS_JNDI);
		
		cargarListaEstados();
	}
	
	
	private void cargarListaEstados(){
		/*Se carga la lista de SelectItem menos el estado
		 * Pendiente que es un estado interno
		 */
		estadosReserva =  new ArrayList<SelectItem>();
	    Reserva r = new Reserva();
		
		for(Estado e: Estado.values()){
			if (! e.equals(Estado.P)){
				SelectItem s = new SelectItem();
				s.setValue(e);
				s.setLabel(r.getEstadoDescripcion(e));
				estadosReserva.add(s);
			}
		}
	}

/*	
	public void reporteReservaFecha(ActionEvent e) {
		
		boolean error = false;
		
		Agenda agendaMarcada = sessionMBean.getAgendaMarcada();
		Recurso recursoMarcado = sessionMBean.getRecursoMarcado();
		
		if (sessionMBean.getAgendaMarcada() == null){
			error = true;
			addErrorMessage("Debe seleccionar una agenda.", MSG_ID);
		}
		
		if (sessionMBean.getRecursoMarcado() == null){
			error = true;
			addErrorMessage("Debe seleccionar un recurso.", MSG_ID);
		}
	
		if (fechaDesde == null && !error){
			error = true;
			addErrorMessage("Debe ingresar la Fecha Desde.", MSG_ID);
		}
		
		if (fechaHasta == null && !error){
			error = true;
			addErrorMessage("Debe ingresar la Fecha Hasta.", MSG_ID);
		}
		
		if (estadoReservaSeleccionado == null && !error){
			error = true;
			addErrorMessage("Debe ingresar el Estado.", MSG_ID);
		}
		if (!error){
			
			String reportFile = "/uy/gub/imm/sae/web/reporte/ReservaPeriodoEstado.jrxml";
		
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("ID_AGENDA", agendaMarcada.getId());
			params.put("ID_RECURSO", recursoMarcado.getId());
			params.put("FECHA_DESDE", fechaDesde);
			params.put("FECHA_HASTA", fechaHasta);
			params.put("ESTADO", estadoReservaSeleccionado.toString());
			params.put("NOMBRE_AGENDA", agendaMarcada.getNombre());
			params.put("NOMBRE_RECURSO", recursoMarcado.getNombre());
			
			desplegar_reporte_pdf(reportFile, params);			 
		}
	}
*/	
	public void reporteReservaFecha(ActionEvent e) {
		
		boolean error = false;
		
		Agenda agendaMarcada = sessionMBean.getAgendaMarcada();
		Recurso recursoMarcado = sessionMBean.getRecursoMarcado();
		
		if (sessionMBean.getAgendaMarcada() == null){
			error = true;
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
		}
		
		if (sessionMBean.getRecursoMarcado() == null){
			error = true;
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
		}
	
		if (fechaDesde == null && !error){
			error = true;
			addErrorMessage(getI18N().getText("query.report.date_from_required"), MSG_ID);
		}
		
		if (fechaHasta == null && !error){
			error = true;
			addErrorMessage(getI18N().getText("query.report.date_to_required"), MSG_ID);
		}
		
		if (estadoReservaSeleccionado == null && !error){
			error = true;
			addErrorMessage(getI18N().getText("query.report.state_required"), MSG_ID);
		}

		if (!error){
			
			VentanaDeTiempo periodo = new VentanaDeTiempo();
			periodo.setFechaInicial(fechaDesde);
			periodo.setFechaFinal(fechaHasta);
			
			InputStream inputStream = null;
			
			try {
				
				//Diseño basico del reporte al que se le agregará la definición de los campos dinamicos.
				String archivoJrxml = null;				
				if (unaPaginaPorHora) {
					archivoJrxml = "/uy/gub/imm/sae/web/reporte/ReservaPeriodoEstadoHoraPlanilla.jrxml";				
				}
				else {
					archivoJrxml = "/uy/gub/imm/sae/web/reporte/ReservaPeriodoEstadoPlanilla.jrxml";				
				}
				
				//Definicion de los campos dinamicos del reporte
				List<Columna> defColumnas = new ArrayList<Columna>();
				List<AgrupacionDato> agrupaciones = recursosEJB.consultarDefinicionDeCampos(recursoMarcado);
				for(AgrupacionDato grupo: agrupaciones) {
					for(DatoASolicitar campo: grupo.getDatosASolicitar()) {
						if (campo.getIncluirEnReporte()) {
							Columna col = new Columna();
							col.setId(campo.getNombre());
							col.setNombre(campo.getEtiqueta());
							col.setClase(String.class);
							col.setAncho(campo.getAnchoDespliegue());
							defColumnas.add(col);
						}
					}
				}
				
				//Nombre del atributo en el que se espera encontrar un Map con los campos dinamicos
				//En este caso el objeto de iteracion es ReservaDTO y el atributo ReservaDTO.getDatos es el
				//Map que contendra las parejas <nombreCampo, valor> para cada campo dinamico del reporte.
				String atributoCamposDinamicos = "datos";
				
				//Armo los parametros esperados por el reporte de reservas por fecha y hora
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("ID_AGENDA", agendaMarcada.getId());
				params.put("ID_RECURSO", recursoMarcado.getId());
				params.put("FECHA_DESDE", fechaDesde);
				params.put("FECHA_HASTA", fechaHasta);
				params.put("ESTADO", estadoReservaSeleccionado);
				params.put("NOMBRE_AGENDA", agendaMarcada.getDescripcion());
				params.put("NOMBRE_RECURSO", recursoMarcado.getDescripcion());
				
				//Datos a desplegar en el reporte, en este casos las reservas por fecha y hora
				List<ReservaDTO> reservas = consultaEJB.consultarReservasPorPeriodoEstado(recursoMarcado, periodo, estadoReservaSeleccionado);

				FacesContext ctx = FacesContext.getCurrentInstance();
				inputStream = this.getClass().getResourceAsStream(archivoJrxml);
				HttpServletResponse response = (HttpServletResponse) ctx.getExternalContext().getResponse();

				byte[] pdf = ReporteProvider.generarReporteDinamico(inputStream, defColumnas, atributoCamposDinamicos, params, reservas);
				ReporteProvider.exportarReporteComoPdf(response,pdf);

				ctx.responseComplete();
				
			} catch (Exception e1) {
				addErrorMessage(e1);
			} finally{
				try {
					if (inputStream != null) inputStream.close();
				} catch (IOException e1) {
				}
			}

		}
	}
	
	public void reporteAsistenciaFecha(ActionEvent e) {
		
		boolean error = false;
		
		Agenda agendaMarcada = sessionMBean.getAgendaMarcada();
		Recurso recursoMarcado = sessionMBean.getRecursoMarcado();
		
		if (sessionMBean.getAgendaMarcada() == null){
			error = true;
			addErrorMessage(getI18N().getText("message.agenda_must_be_selected"), MSG_ID);
		}
		
		if (sessionMBean.getRecursoMarcado() == null){
			error = true;
			addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
		}
	
		if (fechaDesde == null && !error){
			error = true;
			addErrorMessage(getI18N().getText("query.report.date_from_required"), MSG_ID);
		}
		
		if (fechaHasta == null && !error){
			error = true;
			addErrorMessage(getI18N().getText("query.report.date_to_required"), MSG_ID);
		}
		
		estadoReservaSeleccionado=Estado.U;

		if (!error){
			
			VentanaDeTiempo periodo = new VentanaDeTiempo();
			periodo.setFechaInicial(fechaDesde);
			periodo.setFechaFinal(fechaHasta);
			
			InputStream inputStream = null;
			try {
				
				//Definicion de los campos dinamicos del reporte
				//List<Columna> defColumnas = new ArrayList<Columna>();
				
				// TODO: Se debe ver bien que campos se cargan en la planilla.
				List<AgrupacionDato> agrupaciones = recursosEJB.consultarDefinicionDeCampos(recursoMarcado);

				String[] defColPlanilla = armarCabezales(agrupaciones) ;
				//Nombre del atributo en el que se espera encontrar un Map con los campos dinamicos
				//En este caso el objeto de iteracion es ReservaDTO y el atributo ReservaDTO.getDatos es el
				//Map que contendra las parejas <nombreCampo, valor> para cada campo dinamico del reporte.
				//String atributoCamposDinamicos = "datos";
				
				//Datos a desplegar en el reporte, en este casos las reservas por fecha y hora
				List<ReservaDTO> reservas = consultaEJB.consultarReservasUsadasPeriodo(recursoMarcado, periodo);
				// Aqu� se debe armar la lista de listas de valores, para pasarle al archivo
				
				List<List<TableCellValue>> contenido = armarContenido(reservas, agrupaciones); 

				//TODO: transformar(reservas);
//				StandardCSVFile fileCSV = null;
	               LabelValue[] filtros = {new CommonLabelValueImpl("Agenda: ",agendaMarcada.getDescripcion() ), 
	            		   new CommonLabelValueImpl(getI18N().getText("query.report.commonlabel.resource"),recursoMarcado.getDescripcion()),
	            		   new CommonLabelValueImpl(getI18N().getText("query.report.commonlabel.date_from"),Utiles.date2string(fechaDesde, Utiles.DIA)),
	            		   new CommonLabelValueImpl(getI18N().getText("query.report.commonlabel.date_to"), Utiles.date2string(fechaHasta, Utiles.DIA))
	               };
	             
	            StandardCSVFile fileCSV = new StandardCSVFile(filtros, defColPlanilla, contenido); 
	                CSVWebFilePrinter printer = new CSVWebFilePrinter(fileCSV, "ReporteAsistencia");
	                printer.print(); 
				
			} catch (Exception e1) {
				addErrorMessage(e1);
			} finally{
				try {
					if (inputStream != null) inputStream.close();
				} catch (IOException e1) {
				}
			}

		}
	}
	
	

	public Date getFechaDesde() {
		return fechaDesde;
	}
	public void setFechaDesde(Date fechaDesde) {
		this.fechaDesde = fechaDesde;
	}
	public Date getFechaHasta() {
		return fechaHasta;
	}
	public void setFechaHasta(Date fechaHasta) {
		this.fechaHasta = fechaHasta;
	}

	public SessionMBean getSessionMBean() {
		return sessionMBean;
	}

	public void setSessionMBean(SessionMBean sessionMBean) {
		this.sessionMBean = sessionMBean;
	}

	public Estado getEstado() {
		return estado;
	}


	public void setEstado(Estado estado) {
		this.estado = estado;
	}
	public String getEstadoDescripcion() {
		return estadoDescripcion;
	}

	public Boolean getUnaPaginaPorHora() {
		return unaPaginaPorHora;
	}


	public void setUnaPaginaPorHora(Boolean unaPaginaPorHora) {
		this.unaPaginaPorHora = unaPaginaPorHora;
	}


	public void setEstadoDescripcion(String estadoDescripcion) {
		this.estadoDescripcion = estadoDescripcion;
	}

	public List<SelectItem> getEstadosReserva() {
		return estadosReserva;
	}

	public void setEstadosReserva(List<SelectItem> estadosReserva) {
		this.estadosReserva = estadosReserva;
	}


	public Estado getEstadoReservaSeleccionado() {
		return estadoReservaSeleccionado;
	}


	public void setEstadoReservaSeleccionado(Estado estadoReservaSeleccionado) {
		this.estadoReservaSeleccionado = estadoReservaSeleccionado;
	}

	// Arma la lista de etiquetas para encabezar la planilla excel
	private String[] armarCabezales(List<AgrupacionDato> datos){
		String[] cabezales = {"Fecha","Hora","Numero"};
		
		for(AgrupacionDato grupo: datos) {
			for(DatoASolicitar campo: grupo.getDatosASolicitar()) {
				if (campo.getIncluirEnReporte()) {
					String[] aux = new String[cabezales.length+1];
					aux[cabezales.length] = campo.getEtiqueta();
					for (int i=0;i<cabezales.length; i++){
						aux[i]=cabezales[i];
					}
					cabezales = aux;
				}
			}
		}
		String[] aux = new String[cabezales.length+2];
		int i=0;
		for (i=0;i<cabezales.length; i++){
			aux[i]=cabezales[i];
		}
		aux[i]= "Puesto";
		aux[i+1]= "Asistio";
		cabezales = aux;
		return cabezales;
	}

	// Arma el contenido de la planilla excel
	private List<List<TableCellValue>> armarContenido(List<ReservaDTO> reservas, List<AgrupacionDato> agrupaciones) {
		List<List<TableCellValue>> resultado = new ArrayList<List<TableCellValue>>();

		for (ReservaDTO reserva:reservas) {
			List<TableCellValue> filaDatos = new ArrayList<TableCellValue>();
			filaDatos.add(new TableCellValue(Utiles.date2string(reserva.getFecha(), Utiles.DIA)));
			filaDatos.add(new TableCellValue(Utiles.date2string(reserva.getHoraInicio(), Utiles.HORA)));
			filaDatos.add(new TableCellValue(reserva.getNumero()));
			for(AgrupacionDato grupo: agrupaciones) {
			for(DatoASolicitar campo: grupo.getDatosASolicitar()) {
				if (campo.getIncluirEnReporte()) {
					String clave = campo.getNombre();
					TableCellValue valor;
					if (reserva.getDatos().containsKey(clave)){
						valor = new TableCellValue(reserva.getDatos().get(clave).toString());
					}
					else {
						valor = new TableCellValue("");
					}
					filaDatos.add(valor);
				}
				}
			}
			filaDatos.add(new TableCellValue(reserva.getPuestoLlamada()));
			filaDatos.add(new TableCellValue(reserva.getAsistio()));
			resultado.add(filaDatos);
		}
		
		return resultado;
	}


	
	
}
