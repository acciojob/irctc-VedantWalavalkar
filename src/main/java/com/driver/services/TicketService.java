package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        String route = train.getRoute();
        String[] stations = route.split(",");
        boolean fromStationPresent = false;
        int fromStationIdx = -1;
        boolean toStationPresent = false;
        int toStationIdx = -1;

        for(int i=0 ; i<stations.length ; i++)
        {
            if(bookTicketEntryDto.getFromStation().toString().equalsIgnoreCase(stations[i])) {
                fromStationPresent = true;
                fromStationIdx = i;
            }
            if(bookTicketEntryDto.getToStation().toString().equalsIgnoreCase(stations[i])) {
                toStationPresent = true;
                toStationIdx = i;
            }
        }
        if(!fromStationPresent || !toStationPresent)
            throw new Exception("Invalid stations");

        int bookedSeats = 0;
        List<Ticket> tickets = train.getBookedTickets();
        for(Ticket ticket : tickets)
        {
            int ticketFromStationIdx = -1;
            int ticketToStationIdx = -1;

            for(int i=0; i<stations.length; i++)
            {
                if(ticket.getFromStation().toString().equalsIgnoreCase(stations[i]))
                    ticketFromStationIdx = i;
                else if (ticket.getToStation().toString().equalsIgnoreCase(stations[i]))
                    ticketToStationIdx = i;
            }

            for(int i=fromStationIdx ; i<=toStationIdx ; i++)
            {
                for(int j=ticketFromStationIdx ; j<=ticketToStationIdx ; j++)
                    if(stations[i] == stations[j])
                        bookedSeats += ticket.getPassengersList().size();
            }
        }

        int availableSeats = train.getNoOfSeats() - bookedSeats;
        if(availableSeats < bookTicketEntryDto.getNoOfSeats())
            throw new Exception("Less tickets are available");

        Ticket ticket = new Ticket();
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        int totalFare = (toStationIdx - fromStationIdx) * 300;
        ticket.setTotalFare(totalFare);

        List<Integer> passengerIds = bookTicketEntryDto.getPassengerIds();
        List<Passenger> passengers = new ArrayList<>();
        for(Integer id : passengerIds){
            Passenger passenger = passengerRepository.findById(id).get();
            passengers.add(passenger);
        }
        Passenger bookingPassenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        bookingPassenger.getBookedTickets().add(ticket);

        ticket.setPassengersList(passengers);
        ticket.setTrain(train);
        train.getBookedTickets().add(ticket);

        Ticket savedTicket = ticketRepository.save(ticket);
        trainRepository.save(train);

       return savedTicket.getTicketId();

    }
}
