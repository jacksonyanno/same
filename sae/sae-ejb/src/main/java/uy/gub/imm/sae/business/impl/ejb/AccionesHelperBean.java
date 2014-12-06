package uy.gub.imm.sae.business.impl.ejb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import uy.gub.imm.sae.business.api.acciones.dto.RecursoDTO;
import uy.gub.imm.sae.business.api.acciones.ejb.EjecutorAccion;
import uy.gub.imm.sae.business.api.acciones.ejb.ErrorAccion;
import uy.gub.imm.sae.business.api.acciones.ejb.ResultadoAccion;
import uy.gub.imm.sae.business.api.acciones.ejb.exception.InvalidParametersException;
import uy.gub.imm.sae.business.api.acciones.ejb.exception.UnexpectedAccionException;
import uy.gub.imm.sae.business.api.dto.ReservaDTO;
import uy.gub.imm.sae.common.enumerados.Evento;
import uy.gub.imm.sae.common.enumerados.Tipo;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.ErrorAccionException;
import uy.gub.imm.sae.entity.Accion;
import uy.gub.imm.sae.entity.AccionPorDato;
import uy.gub.imm.sae.entity.AccionPorRecurso;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.DatoReserva;
import uy.gub.imm.sae.entity.Recurso;

/**
 * Session Bean implementation class AccionesHelperBean
 */
@Stateless
@RolesAllowed({"RA_AE_ADMINISTRADOR","RA_AE_PLANIFICADOR","RA_AE_FCALL_CENTER","RA_AE_FATENCION", "RA_AE_ANONIMO", "RA_AE_LLAMADOR"})
public class AccionesHelperBean implements AccionesHelperLocal{

	//Parametro fijo que se pasa en todas las invocaciones a validaciones.
	private final String PARAMETRO_RECURSO = "RECURSO";
	private final String PARAMETRO_RESERVA = "RESERVA";
	
	@PersistenceContext(unitName = "SAE-EJB")
	private EntityManager em;
	
	public List<AccionPorRecurso> obtenerAccionesPorRecurso(Recurso r, Evento e) {
		
		@SuppressWarnings("unchecked")
		List<AccionPorRecurso> acciones = (List<AccionPorRecurso>) em.createQuery(
		"select axr " +
		"from  AccionPorRecurso axr " +
		"where axr.recurso = :r and " +
		"	   axr.evento = :e and " +
		"      axr.fechaBaja = null " +
		"order by axr.ordenEjecucion asc "
		)
		.setParameter("r", r)
		.setParameter("e", e)
		.getResultList();

		return acciones;
	}

	public void ejecutarAccionesPorEvento(Map<String, DatoReserva> valores, ReservaDTO reserva, Recurso recurso, Evento evento) throws ApplicationException, BusinessException, ErrorAccionException{
		
		List<AccionPorRecurso> acciones = this.obtenerAccionesPorRecurso(recurso, evento);
		
		for(AccionPorRecurso aXr : acciones) {

			Accion a = aXr.getAccion();

			if (a.getFechaBaja() == null) {
			
				List<AccionPorDato> camposDeLaAccion = aXr.getAccionesPorDato();
				
				Map<String, Object> parametros = new HashMap<String, Object>();
				List<String> nombreCampos = new ArrayList<String>();
				
				for (AccionPorDato accionPorDato : camposDeLaAccion) {
					if (accionPorDato.getFechaDesasociacion() == null) {
						String nombreParametro = accionPorDato.getNombreParametro();
						DatoASolicitar campo = accionPorDato.getDatoASolicitar();
						DatoReserva dato = valores.get(campo.getNombre());
						if (dato != null) {
							//TODO parsear el valor de String al tipo que corresponda: Stirng, Integer, Date
							if (campo.getTipo() == Tipo.NUMBER){
								parametros.put(nombreParametro, Integer.valueOf(dato.getValor()));
							}
							else if (campo.getTipo() == Tipo.BOOLEAN) {
								parametros.put(nombreParametro, Boolean.valueOf(dato.getValor()));
							}
							else {
								parametros.put(nombreParametro, dato.getValor());
							}
						}
						else {
							parametros.put(nombreParametro, null);
						}
						nombreCampos.add(campo.getNombre());
					}
				}
			
				
				parametros.put(PARAMETRO_RECURSO, copiarRecurso(aXr.getRecurso()));
				parametros.put(PARAMETRO_RESERVA, reserva);
				
				EjecutorAccion ejecutor;
				ejecutor = this.getEjecutor(a.getHost(), a.getServicio());
				
				try {
					//Ejecuto la accion
					ResultadoAccion resultado =  ejecutor.ejecutar(a.getNombre(), parametros);
					
					//Hay errores
					if (resultado.getErrores().size() > 0) {
						List<String> mensajes = new ArrayList<String>();
						List<String> codigosErrorMensajes = new ArrayList<String>();
						for (ErrorAccion error : resultado.getErrores()) {
							mensajes.add(error.getMensaje());
							codigosErrorMensajes.add(error.getCodigo());
						}
						throw new ErrorAccionException("-1", mensajes, codigosErrorMensajes, a.getNombre());
					}
				} catch (UnexpectedAccionException e) {
					throw new ApplicationException(e);
				} catch (InvalidParametersException e) {
					List<String> mensajes = new ArrayList<String>();
					mensajes.add(e.getMessage());
					throw new ErrorAccionException("-1", mensajes);
				}
			}
		}
	}
	
	private EjecutorAccion getEjecutor(String host, String jndiName) throws ApplicationException {

		Object ejb = null;
		try {
			InitialContext ctx; 
			if (host != null) {
				//PARCHE PARA QUE FUNQUE EN JBOSS7 en realidad quitamos la posibilidad de acceder a un ejb remoto hasta tener una solucion correcta
				//Properties props = new Properties();
				//props.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
				//props.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
				//props.put("java.naming.provider.url", host);
				//ctx = new InitialContext(props);
				//props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
				ctx = new InitialContext();
			} else {
				ctx = new InitialContext();
			}
			
			ejb = ctx.lookup(jndiName);
		} catch (NamingException e) {
			throw new ApplicationException("-1", "No se pudo acceder a un EJB de tipo EjecutorAccion (jndiName: "+jndiName+")", e);
	    }
		
		EjecutorAccion ejecutor = null;
		if (ejb instanceof EjecutorAccion) {
			ejecutor = (EjecutorAccion) ejb;
		}
		else {
			throw new ApplicationException("-1", "Se esperaba un EJB de tipo EjecutorAccion y se encontró uno del tipo " + ejb.getClass());
		}
		
		return ejecutor;
	}
	
	private RecursoDTO copiarRecurso(Recurso recurso) {
		
		RecursoDTO recursoDTO = new RecursoDTO();
		
		recursoDTO.setId(recurso.getId());
		recursoDTO.setNombre(recurso.getNombre());
		recursoDTO.setDescripcion(recurso.getDescripcion());
		recursoDTO.setCantDiasAGenerar(recurso.getCantDiasAGenerar());
		recursoDTO.setFechaBaja(recurso.getFechaBaja());
		recursoDTO.setFechaFin(recurso.getFechaFin());
		recursoDTO.setFechaFinDisp(recurso.getFechaFinDisp());
		recursoDTO.setFechaInicio(recurso.getFechaInicio());
		recursoDTO.setFechaInicioDisp(recurso.getFechaInicioDisp());
		recursoDTO.setMostrarNumeroEnLlamador(recurso.getMostrarNumeroEnLlamador());
		recursoDTO.setReservaMultiple(recurso.getReservaMultiple());
		recursoDTO.setVentanaCuposMinimos(recurso.getVentanaCuposMinimos());
		recursoDTO.setDiasInicioVentanaIntranet(recurso.getDiasInicioVentanaIntranet());
		recursoDTO.setDiasVentanaIntranet(recurso.getDiasVentanaIntranet());
		recursoDTO.setDiasInicioVentanaInternet(recurso.getDiasInicioVentanaInternet());
		recursoDTO.setDiasVentanaInternet(recurso.getDiasVentanaInternet());
		recursoDTO.setSerie(recurso.getSerie());
		recursoDTO.setAgendaDescripcion(recurso.getAgenda().getDescripcion());
		
		return recursoDTO;
	}

}
