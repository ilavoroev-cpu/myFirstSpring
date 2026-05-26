package com.example.demo;


import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {
    private final ReservationRepository repository;
    private final Map<Long, Reservation> reservationMap;
    public ReservationService(ReservationRepository repository){
        this.repository = repository;
        reservationMap = new HashMap<>();
    }


    public Reservation getReservationById(Long id) {

        ReservationEntity it = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Not found reservation by id = " + id));

        return new Reservation(it.getId(), it.getUserId(), it.getRoomId(),it.getStartDate(),it.getEndDate(),it.getStatus());

    }

    public List<Reservation> findAllReservation() {
        List<ReservationEntity>allEntities = repository.findAll();
        return allEntities.stream().map(it ->
           new Reservation(it.getId(), it.getUserId(), it.getRoomId(),it.getStartDate(),it.getEndDate(),it.getStatus())
        ).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if (reservationToCreate.id() != null){
            throw new IllegalArgumentException("Id should be empty");
        }
        if (reservationToCreate.status() != null){
            throw new IllegalArgumentException("Status should be empty");
        }

        var newReservation = new ReservationEntity();
        newReservation.setUserId(reservationToCreate.userId());
        newReservation.setRoomId(reservationToCreate.roomId());
        newReservation.setStartDate(reservationToCreate.startDate());
        newReservation.setEndDate(reservationToCreate.endDate());
        newReservation.setStatus(ReservationStatus.PENDING);


        var savedEntity = repository.save(newReservation);


        return new Reservation(
                savedEntity.getId(),
                savedEntity.getUserId(),
                savedEntity.getRoomId(),
                savedEntity.getStartDate(),
                savedEntity.getEndDate(),
                savedEntity.getStatus()
        );
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        var searchReservation = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Not found with id: " + id));

        if (searchReservation.getStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot modify");
        }





        searchReservation.setUserId(reservationToUpdate.userId());
        searchReservation.setRoomId(reservationToUpdate.roomId());
        searchReservation.setStartDate(reservationToUpdate.startDate());
        searchReservation.setEndDate(reservationToUpdate.endDate());
        searchReservation.setStatus(reservationToUpdate.status());

        repository.save(searchReservation);

        return new Reservation(
                id,
                searchReservation.getUserId(),
                searchReservation.getRoomId(),
                searchReservation.getStartDate(),
                searchReservation.getEndDate(),
                searchReservation.getStatus()
        );
    }

    public void deleteReservation(Long id) {
        var reservationDelete = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Not found with id: " + id));
        repository.delete(reservationDelete);
    }


    public Reservation approveReservation(Long id) {
        var reservationEntity = repository.findById(id).orElseThrow(()-> new NoSuchElementException("Not found with id: " + id));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot approve reservation with status: " + reservationEntity.getStatus()
            );
        }

        var isConflict = isReservationConflict(reservationEntity);
        if (isConflict){
            throw new IllegalArgumentException("Conflict");
        }


        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);
        return new Reservation(reservationEntity.getId(),
                reservationEntity.getUserId(),
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate(),
                ReservationStatus.APPROVED);
    }


    private boolean isReservationConflict(ReservationEntity reservation){
        for(ReservationEntity existingReservation: repository.findAll().stream().toList()){
            if (reservation.getId().equals(existingReservation.getId())){
                continue;
            }
            if (!reservation.getRoomId().equals(existingReservation.getRoomId())){
                continue;
            }
            if(!existingReservation.getStatus().equals(ReservationStatus.APPROVED)){
                continue;
            }
            if (reservation.getStartDate().isBefore(existingReservation.getEndDate()) &&
                    existingReservation.getStartDate().isBefore(reservation.getEndDate())){
                return true;
            }
        }
        return false;

    }
}
