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

import uy.gub.imm.sae.business.api.Acciones;
import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.enumerados.Evento;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.entity.Accion;
import uy.gub.imm.sae.entity.AccionPorDato;
import uy.gub.imm.sae.entity.AccionPorRecurso;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.ParametroAccion;
import uy.gub.imm.sae.web.common.BaseMBean;

public class AccionAsignacionMBean extends BaseMBean{

	public static final String MSG_ID = "pantalla";
	
	@EJB(name="ejb/AccionesBean")
	private Acciones accionEJB;

	@EJB(name="ejb/RecursosBean")
	private Recursos recursosEJB;
	
	private SessionMBean sessionMBean;
	private AccionAsignacionSessionMBean accionAsignacionSessionMBean;

	
	private List<AccionPorRecurso> accionesDelRecurso;
	private HtmlDataTable accionesDelRecursoTable;
	
	private List<SelectItem> accionesItems;

	private HtmlDataTable accionesPorDatoTable;

	
	@PostConstruct
	public void init() {
		if (accionEJB  == null) accionEJB  = (Acciones)lookupEJB(SAEProfile.getInstance().EJB_ACCIONES_JNDI);
		if (recursosEJB == null) recursosEJB = (Recursos)lookupEJB(SAEProfile.getInstance().EJB_RECURSOS_JNDI);

	}
	
	public void beforePhase(PhaseEvent event){

		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("acciones.notificar.assign.title"));

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

	public AccionAsignacionSessionMBean getAccionAsignacionSessionMBean() {
		return accionAsignacionSessionMBean;
	}
	public void setAccionAsignacionSessionMBean(
			AccionAsignacionSessionMBean accionAsignacionSessionMBean) {
		this.accionAsignacionSessionMBean = accionAsignacionSessionMBean;
	}

	public List<AccionPorRecurso> getAccionesDelRecurso() {
		
		if (accionesDelRecurso == null) {
		
			accionesDelRecurso = new ArrayList<AccionPorRecurso>();
			
			try {
				if (sessionMBean.getRecursoMarcado() != null) {
					accionesDelRecurso = accionEJB.obtenerAccionesDelRecurso(sessionMBean.getRecursoMarcado());
				}
			} catch(Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		}
		
		return accionesDelRecurso;
	}
	public void setAccionesDelRecurso(List<AccionPorRecurso> accionesDelRecurso) {
		this.accionesDelRecurso = accionesDelRecurso;
	}

	public HtmlDataTable getAccionesDelRecursoTable() {
		return accionesDelRecursoTable;
	}
	public void setAccionesDelRecursoTable(HtmlDataTable accionesDelRecursoTable) {
		this.accionesDelRecursoTable = accionesDelRecursoTable;
	}
	
	
	public Boolean getHayAlgunaAccionPorDato() {
		Boolean existeAlguna = false;
		try {
			
			AccionPorRecurso vxr = accionAsignacionSessionMBean.getAccionDelRecurso();
			if (vxr != null && vxr.getId() != null) {
				existeAlguna = accionEJB.obtenerAsociacionesAccionPorDato(vxr).size() > 0;
			}
						   
		} catch (ApplicationException e) {
			addErrorMessage(e, MSG_ID);
		}
									
		return existeAlguna;
	}
	
	public List<SelectItem> getAccionesItems() {
		
		if (accionesItems == null) {
			
			accionesItems = new ArrayList<SelectItem>();
	
			try {
				for (Accion v: accionAsignacionSessionMBean.getAcciones()) {
					accionesItems.add(new SelectItem(v, v.getNombre()));
				}
			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}
		}
		
		return accionesItems;
	}
	
	public HtmlDataTable getAccionesPorDatoTable() {
		return accionesPorDatoTable;
	}
	public void setAccionesPorDatoTable(HtmlDataTable accionesPorDatoTable) {
		this.accionesPorDatoTable = accionesPorDatoTable;
	}
	
	public List<SelectItem> getParametrosDeLaAccionItems() {
		
		List<SelectItem> parametrosDeLaAccionItems = new ArrayList<SelectItem>();

		if (accionAsignacionSessionMBean.getParametrosAccion() != null) {

			for (String nombreParametro: accionAsignacionSessionMBean.getNombresParametrosAccion()) {
				parametrosDeLaAccionItems.add(new SelectItem(nombreParametro, nombreParametro));
			}
		}
		return parametrosDeLaAccionItems;
	}
	
	private void cargarParametrosDeLaAccion() {
		
		if (sessionMBean.getRecursoMarcado() != null) {

			List<String> nombreParametros = new ArrayList<String>();
			List<ParametroAccion> parametros = new ArrayList<ParametroAccion>();
			
			try {

				Accion v = accionAsignacionSessionMBean.getAccionDelRecurso().getAccion();
				parametros = accionEJB.consultarParametrosDeLaAccion(v);
				for (ParametroAccion p : parametros) {
					nombreParametros.add(p.getNombre());
				}

			} catch (Exception e) {
				addErrorMessage(e, MSG_ID);
			}

			accionAsignacionSessionMBean.setNombresParametrosAccion(nombreParametros);
			accionAsignacionSessionMBean.setParametrosAccion(parametros);

			refrescarListaParametros(null);
		}
	}
	
	public void cambioAccionDelRecurso(ActionEvent event) {
		
		cargarParametrosDeLaAccion();
	}
	
	public void refrescarListaParametros(ActionEvent event) {

		List<ParametroAccion> copia = accionAsignacionSessionMBean.getParametrosAccion();
		accionAsignacionSessionMBean.setNombresParametrosAccion(new ArrayList<String>());
		for (ParametroAccion p : copia) {
			accionAsignacionSessionMBean.getNombresParametrosAccion().add(p.getNombre());
		}
		
		for ( AccionPorDato vxd : accionAsignacionSessionMBean.getAccionDelRecurso().getAccionesPorDato()) {
			accionAsignacionSessionMBean.getNombresParametrosAccion().remove(vxd.getNombreParametro());
		}
	}

	
	
	public List<SelectItem> getDatosASolicitarItems() {
		
		List<SelectItem> datosASolicitarItems = new ArrayList<SelectItem>();
		
		for (DatoASolicitar d : accionAsignacionSessionMBean.getDatosASolicitarDelRecurso()) {
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

			accionAsignacionSessionMBean.setDatosASolicitarDelRecurso(campos);
			accionAsignacionSessionMBean.setDatosASolicitarDelRecursoCopia(new ArrayList<DatoASolicitar>(campos));
			
			refrescarListaDatosASolicitar(null);
		}
	}
	
	public void refrescarListaDatosASolicitar(ActionEvent event) {

		List<DatoASolicitar> copia = accionAsignacionSessionMBean.getDatosASolicitarDelRecursoCopia();
		accionAsignacionSessionMBean.setDatosASolicitarDelRecurso(new ArrayList<DatoASolicitar>(copia));

		for ( AccionPorDato vxd : accionAsignacionSessionMBean.getAccionDelRecurso().getAccionesPorDato()) {
			accionAsignacionSessionMBean.getDatosASolicitarDelRecurso().remove(vxd.getDatoASolicitar());
		}
	}
	

	public void eliminar(ActionEvent event) {
		AccionPorRecurso vxr = (AccionPorRecurso)getAccionesDelRecursoTable().getRowData();
		try {
			
			accionEJB.eliminarAccionPorRecurso(vxr);
			
			this.setAccionesDelRecurso(null);
			accionAsignacionSessionMBean.setModoCreacion(false);
			accionAsignacionSessionMBean.setModoEdicion(false);
			accionAsignacionSessionMBean.setAccionDelRecurso(null);
		}
		catch (Exception e) {
			addErrorMessage(e,MSG_ID);
		}
	}	
	

	public void editar(ActionEvent event) {
		
		AccionPorRecurso vxr = (AccionPorRecurso)getAccionesDelRecursoTable().getRowData();
		
		try {

			List<Accion> acciones = accionEJB.consultarAcciones();
			List<ParametroAccion> parametros = accionEJB.consultarParametrosDeLaAccion(vxr.getAccion());
			List<AccionPorDato> asignaciones = accionEJB.obtenerAsociacionesAccionPorDato(vxr);
			
			vxr.setAccionesPorDato(asignaciones);

		/*	Map<Integer, AccionPorDato> asignacionesMap = new HashMap<Integer, AccionPorDato>();
			for (AccionPorDato vxd : asignaciones) {
				asignacionesMap.put(vxd.getId(), vxd);
			}
		*/	
			accionAsignacionSessionMBean.setAcciones(acciones);
			accionAsignacionSessionMBean.setAccionDelRecurso(vxr);
			accionAsignacionSessionMBean.setParametrosAccion(parametros);


		//	accionAsignacionSessionMBean.setAsignacionesMap(asignacionesMap);
			
			accionAsignacionSessionMBean.setModoEdicion(true);
			accionAsignacionSessionMBean.setModoCreacion(false);
			
		} catch (Exception e) {
			addErrorMessage(e, MSG_ID);
		}
		
		cargarDatosASolicitar();
		cargarParametrosDeLaAccion();
	}
	
	public void guardarEdicion(ActionEvent event) {

		try {
			
			accionEJB.modificarAccionPorRecurso(accionAsignacionSessionMBean.getAccionDelRecurso());
			
			addInfoMessage(getI18N().getText("message.change_saved"), MSG_ID);
			this.setAccionesDelRecurso(null);
			accionAsignacionSessionMBean.setModoEdicion(false);
			accionAsignacionSessionMBean.setAccionDelRecurso(null);
			
		} catch (Exception e) {
			addErrorMessage(e , MSG_ID);
		}
		
	}
	
	public void cancelarEdicion(ActionEvent e) {
		accionAsignacionSessionMBean.setModoEdicion(false);
		accionAsignacionSessionMBean.setAccionDelRecurso(null);
	}	
	

	public void crear(ActionEvent event) {

		AccionPorRecurso vxr = new AccionPorRecurso();
		vxr.setRecurso(sessionMBean.getRecursoMarcado());
		
		try {

			List<Accion> acciones = accionEJB.consultarAcciones();
			List<ParametroAccion> parametros = new ArrayList<ParametroAccion>();
			List<AccionPorDato> asignaciones = new ArrayList<AccionPorDato>();
			
			vxr.setAccionesPorDato(asignaciones);
		//	Map<Integer, AccionPorDato> asignacionesMap = new HashMap<Integer, AccionPorDato>();
			
			accionAsignacionSessionMBean.setAcciones(acciones);
			accionAsignacionSessionMBean.setAccionDelRecurso(vxr);
			accionAsignacionSessionMBean.setParametrosAccion(parametros);


		//	accionAsignacionSessionMBean.setAsignacionesMap(asignacionesMap);
			
			accionAsignacionSessionMBean.setModoEdicion(false);
			accionAsignacionSessionMBean.setModoCreacion(true);
			
		} catch (Exception e) {
			addErrorMessage(e, MSG_ID);
		}
		
		cargarDatosASolicitar();
		cargarParametrosDeLaAccion();
	}

	public void guardarCreacion(ActionEvent event) {

		try {
			
			accionEJB.crearAccionPorRecurso(accionAsignacionSessionMBean.getAccionDelRecurso());

			addInfoMessage(getI18N().getText("message.change_saved"), MSG_ID);
			this.setAccionesDelRecurso(null);
			accionAsignacionSessionMBean.setModoCreacion(false);
			accionAsignacionSessionMBean.setAccionDelRecurso(null);
			
		} catch (Exception e) {
			addErrorMessage(e , MSG_ID);
		}
		
	}
	
	public void cancelarCreacion(ActionEvent e) {
		accionAsignacionSessionMBean.setModoCreacion(false);
		accionAsignacionSessionMBean.setAccionDelRecurso(null);
	}	
	
	public void eliminarAccionPorDato (ActionEvent event) {
		
		AccionPorDato vxd = (AccionPorDato)accionesPorDatoTable.getRowData();
		
		accionAsignacionSessionMBean.getAccionDelRecurso().getAccionesPorDato().remove(vxd);
	
		refrescarListaDatosASolicitar(null);
		refrescarListaParametros(null);
	}

	public void crearAccionPorDato (ActionEvent event) {
		
		AccionPorDato vxd = new AccionPorDato();
		accionAsignacionSessionMBean.getAccionDelRecurso().getAccionesPorDato().add(vxd);
	}
	
	public List<SelectItem> getItemsEvento() {
		
		List<SelectItem> items = new ArrayList<SelectItem>();
		for (Evento e : Evento.values()) {
			items.add(new SelectItem(e, e.getDescripcion()));		
		}		
		return items;
	}

}

