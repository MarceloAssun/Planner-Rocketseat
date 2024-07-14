package com.planner.planner.trip;

import com.planner.planner.activity.*;
import com.planner.planner.link.*;
import com.planner.planner.participant.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private TripReposirotry reposirotry;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private LinkService linkService;


    //-------TRIPS

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id){
        Optional<Trip> trip = this.reposirotry.findById(id);

        return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/confirm")
    public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id){
        Optional<Trip> trip = this.reposirotry.findById(id);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();
            rawTrip.setIsConfirmed(true);

            this.reposirotry.save(rawTrip);
            this.participantService.triggerConfirmationEmailToParticipants(id);

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<TripCreatResponse> creatTrip(@RequestBody TripRequestPayload payload){
        Trip newTrip = new Trip(payload);

        this.reposirotry.save(newTrip);

        this.participantService.registerParticipantsToEvents(payload.emails_to_invite(), newTrip);

        return ResponseEntity.ok(new TripCreatResponse(newTrip.getId()));

    }

    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestPayload payload){
        Optional<Trip> trip = this.reposirotry.findById(id);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();
            rawTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setDestination(payload.destination());

            this.reposirotry.save(rawTrip);
            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }


    //-------PARTICIPANTS

    @GetMapping("/{id}/participants")
    public ResponseEntity <List<ParticipantData>> getAllParticipants(@PathVariable UUID id){

        List<ParticipantData> participantsList = this.participantService.getAllParticipantsFromTrip(id);

        return ResponseEntity.ok(participantsList);
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<ParticipantCreateResponse> inviteParticipant(@PathVariable UUID id, @RequestBody ParticipantRequestPayload payload){
        Optional<Trip> trip = this.reposirotry.findById(id);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();


            ParticipantCreateResponse  participantCreateResponse =  this.participantService.registerParticipantToEvent(payload.email(),rawTrip);

            if (rawTrip.getIsConfirmed()) this.participantService.triggerConfirmationEmailToParticipant(payload.email());

            return ResponseEntity.ok(participantCreateResponse);
        }

        return ResponseEntity.notFound().build();
    }


    //-------ACTIVITIES

    @GetMapping("/{id}/activities")
    public ResponseEntity <List<ActivityData>> getAllActivities(@PathVariable UUID id){

        List<ActivityData> activityDataList = this.activityService.getAllActivitiesFromId(id);

        return ResponseEntity.ok(activityDataList);
    }

    @PostMapping("/{id}/activities")
    public ResponseEntity<ActivityResponse> registerActivity(@PathVariable UUID id, @RequestBody ActivityRequestPayload payload){
        Optional<Trip> trip = this.reposirotry.findById(id);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();


            ActivityResponse activityResponse = this.activityService.registerActivity(payload, rawTrip);


            return ResponseEntity.ok(activityResponse);
        }

        return ResponseEntity.notFound().build();
    }


    //-------LINKS

    @PostMapping("/{id}/links")
    public ResponseEntity<LinkResponse> registerLink(@PathVariable UUID id, @RequestBody LinkRequestPayload payload){
        Optional<Trip> trip = this.reposirotry.findById(id);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();


            LinkResponse linkResponse = this.linkService.registerLink(payload, rawTrip);


            return ResponseEntity.ok(linkResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/links")
    public ResponseEntity <List<LinkData>> getAllLinks(@PathVariable UUID id){

        List<LinkData> linkDataList = this.linkService.getAllLinksFromTrip(id);

        return ResponseEntity.ok(linkDataList);
    }

}


