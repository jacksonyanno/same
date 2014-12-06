package uy.gub.imm.sae.common.factories.ws;

import java.util.List;

import uy.gub.imm.sae.business.api.Recursos;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.UserException;
import uy.gub.imm.sae.common.factories.ws.client.recursos.ApplicationException_Exception;
import uy.gub.imm.sae.common.factories.ws.client.recursos.BusinessException_Exception;
import uy.gub.imm.sae.common.factories.ws.client.recursos.RecursosWS;
import uy.gub.imm.sae.entity.Agenda;
import uy.gub.imm.sae.entity.AgrupacionDato;
import uy.gub.imm.sae.entity.DatoASolicitar;
import uy.gub.imm.sae.entity.DatoDelRecurso;
import uy.gub.imm.sae.entity.Recurso;
import uy.gub.imm.sae.entity.ServicioPorRecurso;
import uy.gub.imm.sae.entity.ValorPosible;

public class RecursosPort implements Recursos{

	private RecursosWS recursos;
	
	public RecursosPort (RecursosWS recursos){
		this.recursos = recursos;
	}
	
	public AgrupacionDato agregarAgrupacionDato(Recurso r, AgrupacionDato a)
			throws UserException, ApplicationException {
		throw new UnsupportedOperationException();
	}

	public DatoASolicitar agregarDatoASolicitar(Recurso r, AgrupacionDato a,
			DatoASolicitar d) throws UserException, ApplicationException,
			BusinessException {
		throw new UnsupportedOperationException();
	}

	public DatoDelRecurso agregarDatoDelRecurso(Recurso r, DatoDelRecurso d)
			throws UserException {
		throw new UnsupportedOperationException();
	}

	public ValorPosible agregarValorPosible(DatoASolicitar d, ValorPosible v)
			throws UserException, ApplicationException {
		throw new UnsupportedOperationException();
	}

	public List<AgrupacionDato> consultarAgrupacionesDatos(Recurso r)
			throws ApplicationException {
		throw new UnsupportedOperationException();
	}

	public List<DatoDelRecurso> consultarDatosDelRecurso(Recurso r)
			throws ApplicationException, BusinessException {
		
		try {
			return this.recursos.consultarDatosDelRecurso(r);
		} catch (ApplicationException_Exception e) {
			throw new ApplicationException(e.getFaultInfo().getCodigoError(),e.getFaultInfo().getMessage());
		} catch (BusinessException_Exception e) {
			throw new BusinessException(e.getFaultInfo().getCodigoError(),e.getFaultInfo().getMessage());
		}
	}

	public List<DatoASolicitar> consultarDatosSolicitar(Recurso r)
			throws ApplicationException {
		throw new UnsupportedOperationException();
	}

	public List<AgrupacionDato> consultarDefCamposTodos(Recurso recurso)
			throws BusinessException {
		throw new UnsupportedOperationException();
	}

	public List<AgrupacionDato> consultarDefinicionDeCampos(Recurso recurso)
			throws BusinessException {

		try {
			return this.recursos.consultarDefinicionDeCampos(recurso);
		} catch (BusinessException_Exception e) {
			throw new BusinessException(e.getFaultInfo().getCodigoError(),e.getFaultInfo().getMessage());
		}
	}

	public Recurso consultarRecurso(Recurso r) throws UserException {
		throw new UnsupportedOperationException();
	}

	public List<ValorPosible> consultarValoresPosibles(DatoASolicitar d)
			throws ApplicationException {
		throw new UnsupportedOperationException();
	}

	public void copiarRecurso(Recurso r, String nombre, String descripcion)
			throws BusinessException, ApplicationException, UserException {
		throw new UnsupportedOperationException();		
	}

	public Recurso crearRecurso(Agenda a, Recurso r) throws UserException,
			ApplicationException, BusinessException {
		throw new UnsupportedOperationException();
	}

	public void eliminarAgrupacionDato(AgrupacionDato a) throws UserException,
			ApplicationException {
		throw new UnsupportedOperationException();
	}

	public void eliminarDatoASolicitar(DatoASolicitar d) throws UserException {
		throw new UnsupportedOperationException();
	}

	public void eliminarDatoDelRecurso(DatoDelRecurso d) throws UserException {
		throw new UnsupportedOperationException();
	}

	public void eliminarRecurso(Recurso r) throws UserException,
			ApplicationException {
		throw new UnsupportedOperationException();
	}

	public void eliminarValorPosible(ValorPosible v) throws UserException {
		throw new UnsupportedOperationException();
	}

	public void modificarAgrupacionDato(AgrupacionDato a) throws UserException {
		throw new UnsupportedOperationException();
	}

	public void modificarDatoASolicitar(DatoASolicitar d) throws UserException,
			ApplicationException {
		throw new UnsupportedOperationException();
	}

	public void modificarDatoDelRecurso(DatoDelRecurso d) throws UserException {
		throw new UnsupportedOperationException();
	}

	public void modificarRecurso(Recurso r) throws UserException,
			BusinessException, ApplicationException {
		throw new UnsupportedOperationException();
	}

	public void modificarValorPosible(ValorPosible v) throws UserException,
			ApplicationException {
		throw new UnsupportedOperationException();
	}

	public Boolean mostrarDatosASolicitarEnLlamador(Recurso r)
			throws BusinessException {
		throw new UnsupportedOperationException();
	}

	public List<ServicioPorRecurso> consultarServicioAutocompletar(Recurso r)
			throws BusinessException {
		throw new UnsupportedOperationException();
	}

	public void copiarRecursoAgenda(Agenda a, Recurso r, String nombre,	
			String descripcion) throws BusinessException, ApplicationException,	UserException {
			throw new UnsupportedOperationException();
	}


}
