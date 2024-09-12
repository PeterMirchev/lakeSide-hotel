package com.dailycodework.lakesidehotel.service;


import com.dailycodework.lakesidehotel.exception.InvalidBookingRequestException;
import com.dailycodework.lakesidehotel.model.BookedRoom;
import com.dailycodework.lakesidehotel.model.Room;
import com.dailycodework.lakesidehotel.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingServiceImpl implements BookingService{

    private final BookingRepository bookingRepository;
    private final RoomService roomService;
    public BookingServiceImpl(BookingRepository bookingRepository,
                              RoomService roomService) {
        this.bookingRepository = bookingRepository;
        this.roomService = roomService;
    }

    @Override
    public List<BookedRoom> getAllBookings() {

        return bookingRepository.findAll();
    }


    public void cancelBooking(Long bookingId) {
        if (bookingRepository.existsById(bookingId)) {
            bookingRepository.deleteById(bookingId);
        } else {
            throw new InvalidBookingRequestException("Booking with ID " + bookingId + " does not exist.");
        }
    }

    public List<BookedRoom> getAllBookingsByRoomId(Long roomId) {

        return bookingRepository.findByRoomId(roomId);
    }

    @Override
    public String saveBooking(Long roomId, BookedRoom bookingRequest) {

        // Check for null bookingRequest or dates
        if (bookingRequest == null || bookingRequest.getCheckInDate() == null || bookingRequest.getCheckOutDate() == null) {
            throw new InvalidBookingRequestException("Invalid booking request data!");
        }

        // Check if check-out is before check-in
        if (bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())) {
            throw new InvalidBookingRequestException("Check-in date must come before check-out date!");
        }

        // Check if the room exists
        Room room = roomService.getRoomById(roomId).orElseThrow(() ->
                new InvalidBookingRequestException("Room with the given ID does not exist!"));

        // Get existing bookings and check availability
        List<BookedRoom> existingBookings = room.getBookings();
        boolean roomIsAvailable = roomIsAvailable(bookingRequest, existingBookings);

        // Save the booking if available
        if (roomIsAvailable) {
            room.addBooking(bookingRequest);
            bookingRepository.save(bookingRequest);
        } else {
            throw new InvalidBookingRequestException("Sorry, this room is not available for the selected date.");
        }

        return bookingRequest.getBookingConfirmationCode();
    }

    @Override
    public BookedRoom findByBookingConfirmationCode(String confirmationCode) {

        return bookingRepository.findByBookingConfirmationCode(confirmationCode);
    }

    private boolean roomIsAvailable(BookedRoom bookingRequest, List<BookedRoom> existingBookings) {

       /* return existingBookings.stream()
                .noneMatch(existingBooking ->
                        bookingRequest.getCheckInDate().equals(existingBooking.getCheckInDate())
                || bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckOutDate())
                || (bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckInDate())
                        && bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckOutDate()))
                || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())
                        && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckOutDate()))
                || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())
                && bookingRequest.getCheckOutDate().isAfter(existingBooking.getCheckOutDate())) //65
                || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                        && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckInDate()))
                || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                        && bookingRequest.getCheckOutDate().equals(bookingRequest.getCheckOutDate()))
                );*/
        return existingBookings.stream()
                .noneMatch(existingBooking ->
                        // Booking request check-in is before an existing check-out and check-out is after an existing check-in
                        !bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckInDate()) &&
                                !bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckOutDate())
                );
    }
}
