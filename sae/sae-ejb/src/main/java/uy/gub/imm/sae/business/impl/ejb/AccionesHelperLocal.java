package uy.gub.imm.sae.business.impl.ejb;

import java.util.Map;

import uy.gub.imm.sae.business.api.dto.ReservaDTO;
import uy.gub.imm.sae.common.enumerados.Evento;
import uy.gub.imm.sae.common.exception.ApplicationException;
import uy.gub.imm.sae.common.exception.BusinessException;
import uy.gub.imm.sae.common.exception.ErrorAccionException;
import uy.gub.imm.sae.entity.DatoReserva;
import uy.gub.imm.sae.entity.Recurso;

public interface AccionesHelperLocal {

	public void ejecutarAccionesPorEvento(Map<String, DatoReserva> valores, ReservaDTO reserva, Recurso recurso, Evento evento) throws ApplicationException, BusinessException, ErrorAccionException;
}
