package ru.RSOI.Payment.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.RSOI.Payment.Model.MPayment;

import java.util.List;
import java.util.UUID;

@Repository
public interface RPayment extends JpaRepository<MPayment, Integer> {

    @Query("SELECT p FROM MPayment p where p.v1_payment_uid = ?1")
    public List<MPayment> findPayment(UUID payment_uid);

}
