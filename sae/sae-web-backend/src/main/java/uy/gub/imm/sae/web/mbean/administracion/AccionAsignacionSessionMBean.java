package uy.gub.imm.sae.web.mbean.administracion;

import java.util.List;

import javax.annotation.PostConstruct;

import uy.gub.imm.sae.entity.Accion;
import uy.gub.imm.sae.entity.AccionPorRecurso;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.ParametroAccion;
import uy.gub.imm.sae.web.common.RemovableFromSession;
import uy.gub.imm.sae.web.common.SessionCleanerMBean;

public class AccionAsignacionSessionMBean extends SessionCleanerMBean implements RemovableFromSession {

	private Boolean modoCreacion;
	private Boolean modoEdicion;
	
	private Integer accionesDelRecursoTablePageIndex;
	private Integer accionesPorDatoTablePageIndex;

 	private List<Accion> acciones;
 	
	private AccionPorRecurso accionDelRecurso;
	private List<DatoASolicitar> datosASolicitarDelRecurso;
	private List<DatoASolicitar> datosASolicitarDelRecursoCopia;
	private List<String> nombresParametrosAccion;
	private List<ParametroAccion> parametrosAccion;
	
	//Esta coleccion deben estar sincronizadas con la respectiva de accionDelRecurso, es responsabilidad del ManagedBean que las maneja.
	//private Map<Integer, AccionPorDato> asignacionesMap;
	
	
	@PostConstruct
	public void init() {
		modoCreacion = false;
		modoEdicion = false;
		
		accionesDelRecursoTablePageIndex = 1;
		accionesPorDatoTablePageIndex = 1;
	}
	
	public Integer getAccionesDelRecursoTablePageIndex() {
		return accionesDelRecursoTablePageIndex;
	}

	public void setAccionesDelRecursoTablePageIndex(
			Integer accionesDelRecursoTablePageIndex) {
		this.accionesDelRecursoTablePageIndex = accionesDelRecursoTablePageIndex;
	}

	public Integer getAccionesPorDatoTablePageIndex() {
		return accionesPorDatoTablePageIndex;
	}

	public void setAccionesPorDatoTablePageIndex(
			Integer accionesPorDatoTablePageIndex) {
		this.accionesPorDatoTablePageIndex = accionesPorDatoTablePageIndex;
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

	public AccionPorRecurso getAccionDelRecurso() {
		return accionDelRecurso;
	}

	public void setAccionDelRecurso(AccionPorRecurso accionDelRecurso) {
		this.accionDelRecurso = accionDelRecurso;
	}

	public List<DatoASolicitar> getDatosASolicitarDelRecurso() {
		return datosASolicitarDelRecurso;
	}

	public void setDatosASolicitarDelRecurso(
			List<DatoASolicitar> datosASolicitarDelRecurso) {
		this.datosASolicitarDelRecurso = datosASolicitarDelRecurso;
	}

	public List<Accion> getAcciones() {
		return acciones;
	}
	
	
	public List<DatoASolicitar> getDatosASolicitarDelRecursoCopia() {
		return datosASolicitarDelRecursoCopia;
	}

	public void setDatosASolicitarDelRecursoCopia(
			List<DatoASolicitar> datosASolicitarDelRecursoCopia) {
		this.datosASolicitarDelRecursoCopia = datosASolicitarDelRecursoCopia;
	}

	public List<String> getNombresParametrosAccion() {
		return nombresParametrosAccion;
	}

	public void setNombresParametrosAccion(
			List<String> nombresParametrosAccion) {
		this.nombresParametrosAccion = nombresParametrosAccion;
	}

	public List<ParametroAccion> getParametrosAccion() {
		return parametrosAccion;
	}

	public void setParametrosAccion(
			List<ParametroAccion> parametrosAccion) {
		this.parametrosAccion = parametrosAccion;
	}

	public void setAcciones(List<Accion> acciones) {
		this.acciones = acciones;
	}

/*
	public Map<Integer, AccionPorDato> getAsignacionesMap() {
		return asignacionesMap;
	}

	public void setAsignacionesMap(Map<Integer, AccionPorDato> asignacionesMap) {
		this.asignacionesMap = asignacionesMap;
	}
*/

}
