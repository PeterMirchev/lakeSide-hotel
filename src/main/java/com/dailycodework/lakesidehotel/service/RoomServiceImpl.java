package com.dailycodework.lakesidehotel.service;

import com.dailycodework.lakesidehotel.exception.InternalServerException;
import com.dailycodework.lakesidehotel.exception.ResourceNotFoundException;
import com.dailycodework.lakesidehotel.model.Room;
import com.dailycodework.lakesidehotel.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository repository) {

        this.roomRepository = repository;
    }

    @Override
    public Room addNewRoom(MultipartFile photo,
                           String roomType,
                           BigDecimal roomPrice) throws IOException, SQLException {

        Room room = new Room();
        room.setRoomType(roomType);
        room.setRoomPrice(roomPrice);

        if(!photo.isEmpty()) {

            byte[] photoByes = photo.getBytes();
            Blob photoBlob = new SerialBlob(photoByes);
            room.setPhoto(photoBlob);
        }
        return roomRepository.save(room);
    }

    @Override
    public List<String> getAllRoomTypes() {

        return roomRepository.findDistinctRoomTypes();
    }

    @Override
    public List<Room> getAllRooms() {

        return roomRepository.findAll();
    }

    @Override
    public byte[] getRoomPhotoByRoomId(Long roomId) throws SQLException {

        Optional<Room> room = Optional.ofNullable(roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid room ID.")));

        Blob photoBlob = room.get().getPhoto();

        if (photoBlob != null) {
            return photoBlob.getBytes(1, (int) photoBlob.length());
        }

        return null;
    }

    @Override
    public void deleteRoom(Long roomId) {

        Optional.ofNullable(roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid room ID.")));

        roomRepository.deleteById(roomId);
    }

    @Override
    public Long count() {

        return roomRepository.count();
    }

    @Override
    public Room updateRoom(Long roomId,
                           String roomType,
                           BigDecimal roomPrice,
                           byte[] photoBytes) {

        Optional<Room> room = Optional.ofNullable(roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid room ID.")));

        if (roomType != null) room.get().setRoomType(roomType);
        if (roomPrice != null) room.get().setRoomPrice(roomPrice);
        if (photoBytes != null && photoBytes.length > 0) {

            try {
                room.get().setPhoto(new SerialBlob(photoBytes));
            } catch (SQLException s) {
                throw new InternalServerException("Error updating room.");
            }
        }

        return roomRepository.save(room.get());
    }
}
