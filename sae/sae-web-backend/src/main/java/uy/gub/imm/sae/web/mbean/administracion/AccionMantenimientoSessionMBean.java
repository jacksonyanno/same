package uy.gub.imm.sae.web.mbean.administracion;

import javax.annotation.PostConstruct;

import uy.gub.imm.sae.entity.Accion;
import uy.gub.imm.sae.web.common.RemovableFromSession;
import uy.gub.imm.sae.web.common.SessionCleanerMBean;


public class AccionMantenimientoSessionMBean extends SessionCleanerMBean implements RemovableFromSession {

	private Integer accionesTablePageIndex;
	private Boolean modoCreacion;
	private Boolean modoEdicion;
	private Accion accion;

	private Integer parametrosTablePageIndex;
	
	
	@PostConstruct
	public void init() {
		modoCreacion = false;
		modoEdicion = false;
	}

	public Integer getAccionesTablePageIndex() {
		return accionesTablePageIndex;
	}

	public void setAccionesTablePageIndex(Integer accionesTablePageIndex) {
		this.accionesTablePageIndex = accionesTablePageIndex;
	}

	
	public Boolean getModoCreacion() {
		return modoCreacion;
	}

	public void setModoCreacion(Boolean modoCreacion) {
		this.modoCreacion = modoCreacion;
		
	}

	public Boolean getModoEdicion() {
		return modoEdicion;
	}

	public void setModoEdicion(Boolean modoEdicion) {
		this.modoEdicion = modoEdicion;
	}

	public Accion getAccion() {
		return accion;
	}

	public void setAccion(Accion accion) {
		this.accion = accion;
	}

	public Integer getParametrosTablePageIndex() {
		return parametrosTablePageIndex;
	}

	public void setParametrosTablePageIndex(Integer parametrosTablePageIndex) {
		this.parametrosTablePageIndex = parametrosTablePageIndex;
	}

	
}

