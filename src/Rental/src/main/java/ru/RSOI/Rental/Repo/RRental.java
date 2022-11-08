package ru.RSOI.Rental.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.RSOI.Rental.Model.MRental;

import java.util.List;
import java.util.UUID;

@Repository
public interface RRental extends JpaRepository<MRental, Integer> {

    @Query("SELECT r FROM MRental r where r.v2_username = ?1")
    public List<MRental> findUserRents(String username);

    @Query("SELECT r FROM MRental r where (r.v4_car_uid = ?1 AND r.v7_status = ?2)")
    public List<MRental> findCarRents(UUID car_uid, String status);

    @Query("SELECT r FROM MRental r where r.v1_rental_uid = ?1")
    public List<MRental> findRentByUUID(UUID rental_uid);

    @Query("SELECT r FROM MRental r where (r.v1_rental_uid = ?1 AND r.v7_status = 'IN_PROGRESS')")
    public List<MRental> findInProgressRentByUUID(UUID rental_uid);

}
