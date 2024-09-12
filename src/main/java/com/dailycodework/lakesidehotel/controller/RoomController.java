package com.dailycodework.lakesidehotel.controller;

import com.dailycodework.lakesidehotel.exception.PhotoRetrievalException;
import com.dailycodework.lakesidehotel.model.BookedRoom;
import com.dailycodework.lakesidehotel.model.Room;
import com.dailycodework.lakesidehotel.response.BookingResponse;
import com.dailycodework.lakesidehotel.response.RoomResponse;
import com.dailycodework.lakesidehotel.service.BookingService;
import com.dailycodework.lakesidehotel.service.RoomService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;
    private final BookingService bookingService;

    public RoomController(RoomService roomService,
                          BookingService bookingService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
    }


    @PostMapping("/add/new-room")
    public ResponseEntity<RoomResponse> addNewRoom(@RequestParam("photo") MultipartFile photo,
                                                   @RequestParam("roomType") String roomType,
                                                   @RequestParam("roomPrice") BigDecimal roomPrice) throws SQLException, IOException {

        Room savedRoom = roomService.addNewRoom(photo, roomType, roomPrice);

        RoomResponse response = new RoomResponse(savedRoom.getId(),
                savedRoom.getRoomType(),
                savedRoom.getRoomPrice());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/types")
    public List<String> getRoomTypes() {

        return roomService.getAllRoomTypes();
    }

    @GetMapping("/a;;")
    public ResponseEntity<List<RoomResponse>> getAllRooms() throws SQLException, PhotoRetrievalException {

        List<Room> rooms = roomService.getAllRooms();
        List<RoomResponse> responses = new ArrayList<>();

        for (Room room : rooms) {

            byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getId());
            if (photoBytes != null && photoBytes.length > 0) {
                String base64Photo = Base64.encodeBase64String(photoBytes);
                RoomResponse roomResponse = getRoomResponse(room);
                roomResponse.setPhoto(base64Photo);
                responses.add(roomResponse);
            }
        }

        return ResponseEntity.ok(responses);
    }

    private RoomResponse getRoomResponse(Room room) throws PhotoRetrievalException {

        RoomResponse response = new RoomResponse();
        List<BookedRoom> bookings = getAllBookingsByRoomId(room.getId());
        List<BookingResponse> bookingInfo = bookings
                .stream()
                .map(b -> new BookingResponse(b.getBookingId(),
                        b.getCheckInDate(),
                        b.getCheckOutDate(),
                        b.getBookingConfirmationCode())).toList();

        byte[] photoBytes = null;
        Blob photoBlob = room.getPhoto();

        if (photoBlob != null) {
            try {
                photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
            } catch (SQLException e) {
                throw  new PhotoRetrievalException("Error retrieving photo.");
            }
        }
        return new RoomResponse(room.getId(), room.getRoomType(), room.getRoomPrice(), room.isBooked(), photoBytes, bookingInfo);
    }

    private List<BookedRoom> getAllBookingsByRoomId(Long roomId) {

        return bookingService.getAllBookingsByRoomId(roomId);
    }
}
