package uy.gub.imm.sae.web.mbean.administracion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.model.SelectItem;

import org.richfaces.component.html.HtmlColumn;
import org.richfaces.component.html.HtmlDataTable;
import org.richfaces.component.html.HtmlSubTable;

import uy.gub.imm.sae.business.api.Llamadas;
import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.business.api.dto.ReservaDTO;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.enumerados.Estado;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.Disponibilidad;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.Reserva;
import uy.gub.imm.sae.web.common.BaseMBean;
import uy.gub.imm.sae.web.common.FormularioDinReservaClient;
import uy.gub.imm.sae.web.common.reporte.Columna;

public class ListaDeEsperaMBean extends BaseMBean {

	public static final String MSG_ID = "pantalla";
	
	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;

	@EJB(name="ejb/LlamadasBean")
	private Llamadas llamadasEJB;

	private ListaDeEsperaSessionMBean listaDeEsperaSessionMBean;
	private SessionMBean sessionMBean;

	
	private List<Columna> definicionColumnas;
	private HtmlColumn columnaHoraListaDeEspera;
	private HtmlDataTable tablaReservas;
	private HtmlSubTable subTablaListaDeEspera;
	
	private UIComponent camposSiguienteReserva;
	private Disponibilidad siguienteReservaDisponibilidad;
	
	private List<SelectItem> itemsEstado;
	
	
	@PostConstruct
	public void init() {
		
		if (recursosEJB == null) recursosEJB = (Recursos)lookupEJB(SAEProfile.getInstance().EJB_RECURSOS_JNDI);
		if (llamadasEJB == null) llamadasEJB = (Llamadas)lookupEJB(SAEProfile.getInstance().EJB_LLAMADAS_JNDI);

		
		if (listaDeEsperaSessionMBean.getAgrupaciones() == null && sessionMBean.getRecursoMarcado() != null) {
			try {
				List<AgrupacionDato> agrupaciones = recursosEJB.consultarDefinicionDeCampos(sessionMBean.getRecursoMarcado());
				listaDeEsperaSessionMBean.setAgrupaciones(agrupaciones);

				if (listaDeEsperaSessionMBean.getHorarios() == null) {
					try {
						refrescarHorariosDeEspera();
					} catch (Exception e) {
						addErrorMessage(e, MSG_ID);
					}
				}
			} catch (BusinessException be) {
				if (be.getCodigoError().equals("AE20084")) {
					addErrorMessage(getI18N().getText("message.recurso_must_be_selected"), MSG_ID);
				}
				else {
					addErrorMessage(be, MSG_ID);
				}
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		}
	}
	
	public ListaDeEsperaSessionMBean getListaDeEsperaSessionMBean() {
		return listaDeEsperaSessionMBean;
	}
	public void setListaDeEsperaSessionMBean(
			ListaDeEsperaSessionMBean listaDeEsperaSessionMBean) {
		this.listaDeEsperaSessionMBean = listaDeEsperaSessionMBean;
	}



	public SessionMBean getSessionMBean() {
		return sessionMBean;
	}
	public void setSessionMBean(SessionMBean sessionMBean) {
		this.sessionMBean = sessionMBean;
	}

	
	/*
	 *ACCIONES Y METODOS PARA LA PAGINA: LISTA DE ESPERA 
	 *
	 */
	
	
	//setearlo en <f:view beforePhase de la pagina.
	public void beforePhaseListaDeEspera(PhaseEvent event) {

		
		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("llamador.lista_espera.title"));
		}
	}
	
	public List<Columna> getDefinicionColumnas() {

		if (definicionColumnas == null) {
			
			definicionColumnas = new ArrayList<Columna>();

			if (sessionMBean.getRecursoMarcado() != null) {
				try {
					//Definicion de los campos dinamicos del reporte
					List<AgrupacionDato> agrupaciones = listaDeEsperaSessionMBean.getAgrupaciones();
					for(AgrupacionDato grupo: agrupaciones) {
						for(DatoASolicitar campo: grupo.getDatosASolicitar()) {
							if (campo.getIncluirEnReporte()) {
								Columna col = new Columna();
								col.setId(campo.getNombre());
								col.setNombre(campo.getEtiqueta());
								col.setClase(String.class);
								col.setAncho(campo.getAnchoDespliegue());
								definicionColumnas.add(col);
							}
						}
					}
		
				} catch (Exception e) {
					addErrorMessage(e, MSG_ID);
				}
			}
		}
		
		return definicionColumnas;
	}
	
	public HtmlColumn getColumnaHoraListaDeEspera() {
		return columnaHoraListaDeEspera;
	}

	public void setColumnaHoraListaDeEspera(HtmlColumn columnaHoraListaDeEspera) {
		this.columnaHoraListaDeEspera = columnaHoraListaDeEspera;
		
		//Seteo el colspan de la columna de horas, para adecuarlo a la cantidad dinamica de campos a desplegar
		columnaHoraListaDeEspera.setColspan(getDefinicionColumnas().size() + 2);
	}

	public HtmlDataTable getTablaReservas() {
		return tablaReservas;
	}
	
	public void setTablaReservas(HtmlDataTable tablaReservas) {
		this.tablaReservas = tablaReservas;
	}
	
	public HtmlSubTable getSubTablaListaDeEspera() {
		return subTablaListaDeEspera;
	}

	public void setSubTablaListaDeEspera(HtmlSubTable subTablaListaDeEspera) {

		this.subTablaListaDeEspera = subTablaListaDeEspera;
	}
	
	public void cambiaSeleccionEstados(ActionEvent event) {
		
		refrescarHorariosDeEspera();
	}

	public void refrescar(ActionEvent event) {
		
		refrescarHorariosDeEspera();
	}

	
	/**
	 * Marca la reserva indicando que el el usuario estuvo presente en la cita
	 */
	public void asistio(ActionEvent event) {
		
		try {

			Reserva r = listaDeEsperaSessionMBean.getSiguienteReserva();
			llamadasEJB.marcarAsistencia(sessionMBean.getRecursoMarcado(), r);
			
			cierroDatosSiguiente();
			
		} catch (Exception e) {
			addErrorMessage(e, MSG_ID);
		}
	}
	
	/**
	 * Marca la reserva indicando que el el usuario estuvo ausente en la cita
	 */
	public void falto(ActionEvent event) {
		
		try {

			Reserva r = listaDeEsperaSessionMBean.getSiguienteReserva();
			llamadasEJB.marcarInasistencia(sessionMBean.getRecursoMarcado(), r);
			
			cierroDatosSiguiente();
			
		} catch (Exception e) {
			addErrorMessage(e, MSG_ID);
		}
	}
	
	private void cierroDatosSiguiente() {
		
		refrescarHorariosDeEspera();
		listaDeEsperaSessionMBean.setMostrarDatosSiguiente(false);
	}

	private void refrescarHorariosDeEspera() {
		
		listaDeEsperaSessionMBean.setHorarios(new ArrayList<Horario>());
		
		
		Recurso recursoMarcado = sessionMBean.getRecursoMarcado();
		if (sessionMBean.getRecursoMarcado() != null) {
			try {
				if (getListaDeEsperaSessionMBean().getEstadosSeleccionado() == null) {
					getListaDeEsperaSessionMBean().setEstadosSeleccionado(new ArrayList<Estado>());
					getListaDeEsperaSessionMBean().getEstadosSeleccionado().add(Estado.R);
				}
				List<Estado> estados = getListaDeEsperaSessionMBean().getEstadosSeleccionado();
	
				List<ReservaDTO> reservas = llamadasEJB.obtenerReservasEnEspera(recursoMarcado, estados);
				
				Horario horario = null;
	
				//Recorro las reservas agrupándolas por horario
				for (ReservaDTO reserva : reservas) {
	
					//Si el horario es nulo o la reserva no tiene el mismo horario -> Creo un nuevo grupo con esta reserva
					//Si no -> la agrego al grupo actual
					if (horario == null || ! reserva.getHoraInicio().equals(horario.getHora())) {
						
						horario = new Horario();
						horario.setHora(reserva.getHoraInicio());
						horario.getListaEspera().add(crearEspera(reserva));
						
						listaDeEsperaSessionMBean.getHorarios().add(horario);
					}
					else {
						horario.getListaEspera().add(crearEspera(reserva));
					}
				}
	
				//Habilito refresco automatico en el caso que el filtro muestre solo las reservadas.
				if (getListaDeEsperaSessionMBean().getEstadosSeleccionado().size() == 1 &&
					getListaDeEsperaSessionMBean().getEstadosSeleccionado().get(0).equals(Estado.R)) {
					
					getListaDeEsperaSessionMBean().setRefrescarListaDeEspera(true);
				}
				else {
					getListaDeEsperaSessionMBean().setRefrescarListaDeEspera(false);
				}
				
			} catch (BusinessException e) {
				addErrorMessage(e, MSG_ID);
			}
		}
	}
	
/*
	public Reserva getSiguienteReserva() {
		return siguienteReserva;
	}
	public void setSiguienteReserva(Reserva siguienteReserva) {
		this.siguienteReserva = siguienteReserva;
	}
	*/
	
	public UIComponent getCamposSiguienteReserva() {
		return camposSiguienteReserva;
	}
	public void setCamposSiguienteReserva(UIComponent camposSiguienteReserva) {
		this.camposSiguienteReserva = camposSiguienteReserva;
	}
	public Disponibilidad getSiguienteReservaDisponibilidad() {
		return siguienteReservaDisponibilidad;
	}
	public void setSiguienteReservaDisponibilidad(Disponibilidad siguienteReservaDisponibilidad) {
		this.siguienteReservaDisponibilidad = siguienteReservaDisponibilidad;
	}
	public Boolean getMostrarDatosSiguiente() {
		return listaDeEsperaSessionMBean.getMostrarDatosSiguiente();
	}
	
	//Llama a la capa de negocio consumiendo la siguiente reserva en la lista de espera y la despliega al usuario.
	public void siguiente(ActionEvent event) {
		
		if (sessionMBean.getRecursoMarcado() != null) {
			Reserva siguienteReserva = null;
			try {
				siguienteReserva = llamadasEJB.siguienteEnEspera(sessionMBean.getRecursoMarcado(), sessionMBean.getPuesto());
				listaDeEsperaSessionMBean.setSiguienteReserva(siguienteReserva);
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
	
			camposSiguienteReserva.getChildren().clear();
			
			if (siguienteReserva != null) {
				try {
					siguienteReservaDisponibilidad = siguienteReserva.getDisponibilidades().get(0);

					List<AgrupacionDato> agrupaciones = listaDeEsperaSessionMBean.getAgrupaciones();
					FormularioDinReservaClient.armarFormularioLecturaDinamico(
							sessionMBean.getRecursoMarcado(), siguienteReserva, camposSiguienteReserva, agrupaciones);
				} catch (Exception e) {
					addErrorMessage(e, MSG_ID);
				}
				
				listaDeEsperaSessionMBean.setMostrarDatosSiguiente(true); //Para que al rerenderizar se muestre el formulario con los datos de la siguiente reserva
			}
/*			else {
				HtmlOutputText noHayEspera = new HtmlOutputText();
				noHayEspera.setValue("No hay personas en espera");
				camposSiguienteReserva.getChildren().add(noHayEspera);
				listaDeEsperaSessionMBean.setMostrarBotonesAtencion(false);
			}*/
			

		}		

	}

	//Llama a la capa de negocio re-consumiendo la reserva indicada y la despliega al usuario.
	public void llamar(ActionEvent event) {
		
		Reserva siguienteReserva = null;
		try {
			 
			Espera espera = (Espera)getSubTablaListaDeEspera().getRowData();
			if (espera != null) {
				Reserva r = new Reserva();
				r.setId(espera.getReserva().getId());
				siguienteReserva = llamadasEJB.volverALlamar(sessionMBean.getRecursoMarcado(), sessionMBean.getPuesto(), r);
				listaDeEsperaSessionMBean.setSiguienteReserva(siguienteReserva);
			}
			
		} catch (Exception e) {
			addErrorMessage(e, MSG_ID);
		}

		camposSiguienteReserva.getChildren().clear();
		
		
		if (siguienteReserva != null) {
			try {
				siguienteReservaDisponibilidad = siguienteReserva.getDisponibilidades().get(0);
				
				List<AgrupacionDato> agrupaciones = listaDeEsperaSessionMBean.getAgrupaciones();
				FormularioDinReservaClient.armarFormularioLecturaDinamico(
						sessionMBean.getRecursoMarcado(), siguienteReserva, camposSiguienteReserva, agrupaciones);
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
			listaDeEsperaSessionMBean.setMostrarDatosSiguiente(true); //Para que al rerenderizar se muestre el formulario con los datos de la siguiente reserva
		}
/*		else {
			HtmlOutputText noHayEspera = new HtmlOutputText();
			noHayEspera.setValue("No hay personas en espera");
			camposSiguienteReserva.getChildren().add(noHayEspera);
		}*/
		

	}
	
	public List<SelectItem> getItemsEstado() {
		
		if (itemsEstado == null) {
			List<Estado> valorItem1 = new ArrayList<Estado>();
			valorItem1.add(Estado.R);
			List<Estado> valorItem2 = new ArrayList<Estado>();
			valorItem2.add(Estado.R);
			valorItem2.add(Estado.U);
			
			itemsEstado = new ArrayList<SelectItem>();
			itemsEstado.add(new SelectItem(valorItem1, getI18N().getText("llamador.lista_espera.item.reservada")));
			itemsEstado.add(new SelectItem(valorItem2, getI18N().getText("llamador.lista_espera.item.reservada_utilizada")));
		}
		
		return itemsEstado;
	}

	
	public String getNombreColumnaPuesto() {
		if (sessionMBean.getRecursoMarcado() != null) {
			return sessionMBean.getRecursoMarcado().getTextoRecurso().getTituloPuestoEnLlamador();
		}
		else {
			return null;
		}
	}
	
	
	private Espera crearEspera (ReservaDTO reserva) {
		
		Espera espera = new Espera();
	
		espera.setReserva(reserva);
		
		List<Columna> cols = getDefinicionColumnas();
		for (Columna columna : cols) {
			Object dato = reserva.getDatos().get(columna.getId());
			//Si el dato es nulo, por ejemplo campo opcional agrego vacio
			espera.getDatos().add((dato == null ? "" : dato.toString()));
		}
		
		return espera;
	}
	
	public class Horario {
		
		private Date hora;
		private List<Espera> listaEspera = new ArrayList<Espera>();
		
		public Date getHora() {
			return hora;
		}
		public void setHora(Date hora) {
			this.hora = hora;
		}
		public List<Espera> getListaEspera() {
			return listaEspera;
		}
		public void setListaEspera(List<Espera> listaEspera) {
			this.listaEspera = listaEspera;
		}
	}

	public class Espera {
		
		private ReservaDTO reserva;
		private List<String> datos = new ArrayList<String>();

		public ReservaDTO getReserva() {
			return reserva;
		}
		public void setReserva(ReservaDTO reserva) {
			this.reserva = reserva;
		}
		public List<String> getDatos() {
			return datos;
		}
		public void setDatos(List<String> datos) {
			this.datos = datos;
		}
	}
	
	
	
}

