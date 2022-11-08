package ru.RSOI.Rental.Model;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name="rental")
public class MRental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id = -1;

    @Column(name = "rental_uid", columnDefinition = "uuid UNIQUE NOT NULL")
    public UUID v1_rental_uid = UUID.randomUUID();

    @Column(name = "username", length = 80, nullable = false)
    public String v2_username = "";

    @Column(name = "payment_uid", columnDefinition = "uuid NOT NULL")
    public UUID v3_payment_uid;

    @Column(name = "car_uid", columnDefinition = "uuid NOT NULL")
    public UUID v4_car_uid;

    @Column(name = "date_from", columnDefinition = "TIMESTAMP WITH TIME ZONE NOT NULL")
    public Timestamp v5_date_from;

    @Column(name = "date_to", columnDefinition = "TIMESTAMP WITH TIME ZONE NOT NULL")
    public Timestamp v6_date_to;

    @Column(name = "status", columnDefinition = "VARCHAR(20) CHECK (status IN ('IN_PROGRESS', 'FINISHED', 'CANCELED'))")
    public String v7_status = "FINISHED";

    public MRental()
    {

    }

    public MRental(int id, UUID rental_uid, String username, UUID payment_uid, UUID car_uid,
                   Timestamp date_from, Timestamp date_to, String status)
    {
        this.id = id;
        this.v1_rental_uid = rental_uid;
        this.v2_username = username;
        this.v3_payment_uid = payment_uid;
        this.v4_car_uid = car_uid;
        this.v5_date_from = date_from;
        this.v6_date_to = date_to;
        this.v7_status = status;
    }

    public int getId() {
        return id;
    }
}
