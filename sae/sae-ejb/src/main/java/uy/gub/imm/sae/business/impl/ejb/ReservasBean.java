package uy.gub.imm.sae.business.impl.ejb;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;

import uy.gub.imm.sae.business.api.Reservas;
import uy.gub.imm.sae.business.api.dto.ReservaDTO;
import uy.gub.imm.sae.common.enumerados.Estado;
import uy.gub.imm.sae.common.exception.AccesoMultipleException;
import uy.gub.imm.sae.entity.Disponibilidad;
import uy.gub.imm.sae.entity.Reserva;

@Stateless
@RolesAllowed({"RA_AE_FCALL_CENTER","RA_AE_PLANIFICADOR", "RA_AE_ADMINISTRADOR","RA_AE_ANONIMO","RA_AE_FATENCION"})
public class ReservasBean implements Reservas {

	@PersistenceContext(unitName = "SAE-EJB")
	private EntityManager em;
	
	@SuppressWarnings("unchecked")
	public void modificarEstadoReserva(ReservaDTO reserva) throws AccesoMultipleException {

		List<Disponibilidad>  disponibilidades = (List<Disponibilidad>) em.createQuery("select d from  Disponibilidad d join d.reservas reserva where reserva.id = :r").setParameter("r", reserva.getId().intValue()).getResultList();;
		
		Disponibilidad disponibilidad = disponibilidades.get(0);
		
		disponibilidad = em.find(Disponibilidad.class, disponibilidad.getId());
		
		try {
			
			disponibilidad.setNumerador(disponibilidad.getNumerador()+1);
			em.flush();
		} catch(OptimisticLockException e){

			throw new AccesoMultipleException("-1","ACCESO MULTIPLE A DISPONIBILIDAD EN CONFIRMAR RESERVA (id = "+reserva.getId()+")");
		}
		
		Reserva r = em.find(Reserva.class, reserva.getId());
		r.setEstado(Estado.U);
		r.setOrigen(reserva.getOrigen());
		
		em.flush();
		
	}
}
