package com.dailycodework.lakesidehotel.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookedRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(name = "check_in")
    private LocalDate checkInDate;

    @Column(name = "check_out")
    private LocalDate checkOutDate;

    @Column(name = "guest_full_name")
    private String guestFullName;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "children")
    private int NumberOfChildren;

    @Column(name = "adults")
    private int NumberOfAdults;

    @Column(name = "total_guest")
    private int totalNumberOfGuest;

    @Column(name = "confirmation_code")
    private String bookingConfirmationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;


    public void calculateTotalNumberOfGuests() {

        this.totalNumberOfGuest = this.NumberOfAdults + this.NumberOfChildren;
    }

    public void setNumberOfChildren(int numberOfChildren) {

        NumberOfChildren = numberOfChildren;
        calculateTotalNumberOfGuests();
    }

    public void setNumberOfAdults(int numberOfAdults) {

        NumberOfAdults = numberOfAdults;
        calculateTotalNumberOfGuests();
    }

    public void setBookingConfirmationCode(String bookingConfirmationCode) {

        this.bookingConfirmationCode = bookingConfirmationCode;
    }
}
