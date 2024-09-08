package com.dailycodework.lakesidehotel.response;

import com.dailycodework.lakesidehotel.model.Room;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse{

    private Long id;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String guestFullName;
    private String guestEmail;
    private int NumberOfChildren;
    private int NumberOfAdults;
    private int totalNumberOfGuest;
    private String bookingConfirmationCode;
    private Room room;

    public BookingResponse(Long id,
                           LocalDate checkInDate,
                           LocalDate checkOutDate,
                           String bookingConfirmationCode) {

        this.id = id;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingConfirmationCode = bookingConfirmationCode;
    }
}
