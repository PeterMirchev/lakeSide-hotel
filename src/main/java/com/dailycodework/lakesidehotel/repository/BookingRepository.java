package com.dailycodework.lakesidehotel.repository;

import com.dailycodework.lakesidehotel.model.BookedRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<BookedRoom, Long> {
    List<BookedRoom> findByRoomId(Long roomId);

    @Query("""
        SELECT b FROM BookedRoom b
        WHERE b.bookingConfirmationCode = :confirmationCode
        """)
    BookedRoom findByBookingConfirmationCode(@Param("confirmationCode")String confirmationCode);
}
