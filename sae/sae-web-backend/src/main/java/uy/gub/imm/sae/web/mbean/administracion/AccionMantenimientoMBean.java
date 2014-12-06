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
import org.richfaces.component.html.HtmlDatascroller;

import uy.gub.imm.sae.business.api.Acciones;
import uy.gub.imm.sae.common.SAEProfile;
import uy.gub.imm.sae.common.enumerados.Tipo;
import uy.gub.imm.sae.entity.Accion;
import uy.gub.imm.sae.entity.ParametroAccion;
import uy.gub.imm.sae.web.common.BaseMBean;


public class AccionMantenimientoMBean extends BaseMBean {

	public static final String MSG_ID = "pantalla";
		
	@EJB(name="ejb/AccionesBean")
	private Acciones accionEJB;

	private SessionMBean sessionMBean;
	private AccionMantenimientoSessionMBean accionMantenimientoSessionMBean;

	
	private List<Accion> acciones;
	private HtmlDataTable accionesTable;
	
	private HtmlDataTable parametrosTable;
	private HtmlDatascroller parametrosDataScroller;

	@PostConstruct
	public void init() {
		if (accionEJB  == null) accionEJB  = (Acciones)lookupEJB(SAEProfile.getInstance().EJB_ACCIONES_JNDI);
	}
	public void beforePhase(PhaseEvent event){
		if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
			sessionMBean.setPantallaTitulo(getI18N().getText("acciones.notificar.update.title"));
		}
		
	}
	
	public SessionMBean getSessionMBean() {
		return sessionMBean;
	}


	public void setSessionMBean(SessionMBean sessionMBean) {
		this.sessionMBean = sessionMBean;
	}



	public AccionMantenimientoSessionMBean getAccionMantenimientoSessionMBean() {
		return accionMantenimientoSessionMBean;
	}


	public void setAccionMantenimientoSessionMBean(
			AccionMantenimientoSessionMBean accionMantenimientoSessionMBean) {
		this.accionMantenimientoSessionMBean = accionMantenimientoSessionMBean;
	}


	public List<Accion> getAcciones() {
		
		acciones = new ArrayList<Accion>();
		
		try {
			acciones = accionEJB.consultarAcciones();
		} catch(Exception e) {
			addErrorMessage(e,MSG_ID);
		}
		
		return acciones;
	}


	public void setAcciones(List<Accion> acciones) {
		this.acciones = acciones;
	}

	public HtmlDataTable getAccionesTable() {
		return accionesTable;
	}

	public void setAccionesTable(HtmlDataTable accionesTable) {
		this.accionesTable = accionesTable;
	}

	public void eliminarAccion(ActionEvent event) {
		Accion accion = (Accion)getAccionesTable().getRowData();
		try {
			accionEJB.eliminarAccion(accion);
			accionMantenimientoSessionMBean.setModoCreacion(false);
			accionMantenimientoSessionMBean.setModoEdicion(false);
			accionMantenimientoSessionMBean.setAccion(null);
		}
		catch (Exception e) {
			addErrorMessage(e,MSG_ID);
		}
	}
	
	
	public HtmlDataTable getParametrosTable() {
		return parametrosTable;
	}

	public void setParametrosTable(HtmlDataTable parametrosTable) {
		this.parametrosTable = parametrosTable;
	}
	
	public HtmlDatascroller getParametrosDataScroller() {
		return parametrosDataScroller;
	}

	public void setParametrosDataScroller(HtmlDatascroller parametrosDataScroller) {
		this.parametrosDataScroller = parametrosDataScroller;
	}

	public List<SelectItem> getTiposDeDato() {
		
		List<SelectItem> items = new ArrayList<SelectItem>();
		for (Tipo t : Tipo.values()) {
			items.add(new SelectItem(t, t.getDescripcion(getI18N().getLocale())));
		}
		
		return items;
	}

	public void editar(ActionEvent event) {

		Accion v = (Accion)getAccionesTable().getRowData();
		
		accionMantenimientoSessionMBean.setModoEdicion(true);
		accionMantenimientoSessionMBean.setModoCreacion(false);
		
		List<ParametroAccion> parametros = new ArrayList<ParametroAccion>();
		try {
			parametros = accionEJB.consultarParametrosDeLaAccion(v);
		} catch (Exception e) {
			addErrorMessage(e, MSG_ID);
		}

		accionMantenimientoSessionMBean.setAccion(v);
		accionMantenimientoSessionMBean.getAccion().setParametrosAccion(parametros);
		
	}
	
	public void guardarEdicion(ActionEvent event) {

		try {
			
			//Modifica la accion y sus parametros
			accionEJB.modificarAccion(accionMantenimientoSessionMBean.getAccion());
			
			addInfoMessage(getI18N().getText("message.change_saved"), MSG_ID);
			accionMantenimientoSessionMBean.setModoEdicion(false);
			accionMantenimientoSessionMBean.setAccion(null);
			
		} catch (Exception e) {
			addErrorMessage(e , MSG_ID);
		}
		
	}
	
	public void cancelarEdicion(ActionEvent e) {
		accionMantenimientoSessionMBean.setModoEdicion(false);
		accionMantenimientoSessionMBean.setAccion(null);
	}

	public void crear(ActionEvent event) {
		
		Accion v = new Accion();
		
		accionMantenimientoSessionMBean.setModoEdicion(false);
		accionMantenimientoSessionMBean.setModoCreacion(true);
		
		accionMantenimientoSessionMBean.setAccion(v);
	}

	public void guardarCreacion(ActionEvent event) {

		try {
			
			//Crea la accion y sus parametros
			accionEJB.crearAccion(accionMantenimientoSessionMBean.getAccion());

			addInfoMessage(getI18N().getText("message.change_saved"), MSG_ID);
			accionMantenimientoSessionMBean.setModoCreacion(false);
			accionMantenimientoSessionMBean.setAccion(null);
			
		} catch (Exception e) {
			addErrorMessage(e , MSG_ID);
		}
		
	}
	
	public void cancelarCreacion(ActionEvent e) {
		accionMantenimientoSessionMBean.setModoCreacion(false);
		accionMantenimientoSessionMBean.setAccion(null);
	}
	
	
	public void crearParametro(ActionEvent event) {
		accionMantenimientoSessionMBean.getAccion().getParametrosAccion().add(new ParametroAccion());
		int size = accionMantenimientoSessionMBean.getAccion().getParametrosAccion().size();
		accionMantenimientoSessionMBean.setParametrosTablePageIndex(Double.valueOf(size / parametrosTable.getRows()).intValue()+1);
		parametrosDataScroller.setPage(Double.valueOf(size / parametrosTable.getRows()).intValue()+1);
	}
	
	public void eliminarParametro(ActionEvent event) {
		
		int i = getParametrosTable().getRowIndex();
		accionMantenimientoSessionMBean.getAccion().getParametrosAccion().remove(i);
	}
	
}

