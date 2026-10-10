package com.healthharbor.appointment.repository;

import com.healthharbor.appointment.entity.Appointment;
import com.healthharbor.appointment.entity.Appointment.AppointmentStatus;
import com.healthharbor.common.crud.CrudRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface AppointmentRepository extends CrudRepository<Appointment> {

    boolean existsByWorkerIdAndAppointmentDateAndAppointmentTimeAndStatusInAndIdNot(
            Long workerId, LocalDate date, LocalTime time, Collection<AppointmentStatus> statuses, Long id);

    long countByAppointmentDate(LocalDate date);

    long countByAppointmentDateBetweenAndStatus(LocalDate from, LocalDate to, AppointmentStatus status);

    List<Appointment> findByStatusInAndAppointmentDateLessThanEqualOrderByAppointmentDateAscAppointmentTimeAsc(
            Collection<AppointmentStatus> statuses, LocalDate date);

    @Query("select a.status, count(a) from Appointment a group by a.status")
    List<Object[]> countByStatus();

    @Query("select a.type, count(a) from Appointment a group by a.type")
    List<Object[]> countByType();

    @Query("select a.healthCentreName, count(a) from Appointment a group by a.healthCentreName")
    List<Object[]> countByCentre();

    @Query("select a.appointmentDate, count(a) from Appointment a where a.appointmentDate between :from and :to group by a.appointmentDate order by a.appointmentDate")
    List<Object[]> dailyCounts(LocalDate from, LocalDate to);
}
