package com.example.demo;


import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/reservation")
public class ReservationController {
    private final ReservationService reservationService;

    private static final Logger log= LoggerFactory.getLogger(ReservationController.class);

    public ReservationController(ReservationService reservationService){
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservarion(@RequestBody Reservation reservationToCreate){
        log.info("Called createReservarion");
        return ResponseEntity.status(201)
                .body(reservationService.createReservation(reservationToCreate));
       // return reservationService.createReservation(reservationToCreate);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(
            @PathVariable Long id){
        log.info("Called getReservationById: id = " + id);
        try {
            return ResponseEntity.status(200).body(reservationService.getReservationById(id));
        }catch (NoSuchElementException ex) {
            return ResponseEntity.status(404).build();
        }

        //return reservationService.getReservationById(id);
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservation(){
        log.info("Called getAllReservation");
        return ResponseEntity.ok(reservationService.findAllReservation());
        //return reservationService.findAllReservation();
    }

    @PutMapping
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable Long id,
            @RequestBody Reservation reservationToUpdate
    ){
        log.info("Called updateReservation id: " + id);
        var updated = reservationService.updateReservation(id, reservationToUpdate);
        return ResponseEntity.status(200).body(updated);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id){
        log.info("Called deleteReservation id : " + id);
        try {
            reservationService.deleteReservation(id);
            return ResponseEntity.ok().build();
        }catch (NoSuchElementException ex){
            return ResponseEntity.status(404).build();
        }

    }
    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(
            @PathVariable("id") Long id
    ){
        log.info("Called approveReservation id = " + id);
        var reservation = reservationService.approveReservation(id);
        return ResponseEntity.ok(reservation);
    }

}
