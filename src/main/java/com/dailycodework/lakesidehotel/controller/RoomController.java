package com.dailycodework.lakesidehotel.controller;

import com.dailycodework.lakesidehotel.exception.PhotoRetrievalException;
import com.dailycodework.lakesidehotel.exception.ResourceNotFoundException;
import com.dailycodework.lakesidehotel.model.BookedRoom;
import com.dailycodework.lakesidehotel.model.Room;
import com.dailycodework.lakesidehotel.response.RoomResponse;
import com.dailycodework.lakesidehotel.service.BookingServiceImpl;
import com.dailycodework.lakesidehotel.service.RoomService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;
    private final BookingServiceImpl bookingService;

    public RoomController(RoomService roomService,
                          BookingServiceImpl bookingService) {
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

    @GetMapping("/all-rooms")
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

    @DeleteMapping("/delete/room/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable(name = "roomId") Long roomId) {

        roomService.deleteRoom(roomId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/count-all-rooms")
    public ResponseEntity<Long> countRooms() {

        Long rooms = roomService.count();
        return ResponseEntity.ok(rooms);
    }

    @PutMapping("/update/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable(name = "roomId") Long roomId,
                                                   @RequestParam(required = false) String roomType,
                                                   @RequestParam(required = false) BigDecimal roomPrice,
                                                   @RequestParam(required = false) MultipartFile photo) throws SQLException, IOException, PhotoRetrievalException {

        byte[] photoBytes = photo != null && !photo.isEmpty() ?
                photo.getBytes() : roomService.getRoomPhotoByRoomId(roomId);

        Blob photoBlob = photoBytes != null && photoBytes.length > 0 ?
                new SerialBlob(photoBytes) : null;

        Room room = roomService.updateRoom(roomId, roomType, roomPrice, photoBytes);
        room.setPhoto(photoBlob);

        RoomResponse roomResponse = getRoomResponse(room);

        return ResponseEntity.ok(roomResponse);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<Optional<RoomResponse>> getRoomById(@PathVariable(name = "roomId") Long roomId) {

        Optional<Room> room = Optional.ofNullable(roomService.getRoomById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid room ID")));

        RoomResponse res = mapToRoomResponse(room);
        return ResponseEntity.ok(Optional.of(res));

       /* return room.map(r -> {w
            RoomResponse response = null;
            try {
                response = getRoomResponse(r);
            } catch (PhotoRetrievalException e) {
                throw new RuntimeException(e);
            }
            return ResponseEntity.ok(Optional.of(response));
        }).orElseThrow(() -> new ResourceNotFoundException("Invalid room ID"));*/
    }

    private RoomResponse mapToRoomResponse(Optional<Room> room) {

        try {
            return getRoomResponse(room.get());
        } catch (PhotoRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    private RoomResponse getRoomResponse(Room room) throws PhotoRetrievalException {

        List<BookedRoom> bookings = getAllBookingsByRoomId(room.getId());
      /*  List<BookingResponse> bookingInfo = bookings
                .stream()
                .map(b -> new BookingResponse(b.getBookingId(),
                        b.getCheckInDate(),
                        b.getCheckOutDate(),
                        b.getBookingConfirmationCode())).toList();*/

        byte[] photoBytes = null;
        Blob photoBlob = room.getPhoto();

        if (photoBlob != null) {
            try {
                photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
            } catch (SQLException e) {
                throw  new PhotoRetrievalException("Error retrieving photo.");
            }
        }
        return new RoomResponse(room.getId(), room.getRoomType(), room.getRoomPrice(), room.isBooked(), photoBytes);
    }

    private List<BookedRoom> getAllBookingsByRoomId(Long roomId) {

        return bookingService.getAllBookingsByRoomId(roomId);
    }
}
