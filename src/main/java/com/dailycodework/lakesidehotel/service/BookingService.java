package com.dailycodework.lakesidehotel.service;

import com.dailycodework.lakesidehotel.model.BookedRoom;

import java.util.List;

public interface BookingService {
    void cancelBooking(Long bookingId);

    String saveBooking(Long roomId, BookedRoom bookingRequest);

    BookedRoom findByBookingConfirmationCode(String confirmationCode);

    List<BookedRoom> getAllBookings();
}
