package ru.RSOI.Payment.Model;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name="payment")
public class MPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id = -1;

    @Column(name = "payment_uid", columnDefinition = "uuid NOT NULL")
    public UUID v1_payment_uid = UUID.randomUUID();

    @Column(name = "status", columnDefinition = "VARCHAR(20) NOT NULL CHECK (status IN ('PAID', 'CANCELED'))")
    public String v2_status = "CANCELED";

    @Column(name = "price", nullable = false)
    public int v3_price = 0;

    public MPayment()
    {

    }

    public MPayment(int id, UUID payment_uid, String status, int price)
    {
        this.id = id;
        this.v1_payment_uid = payment_uid;
        this.v2_status = status;
        this.v3_price = price;
    }

    public int getId() {
        return id;
    }
}
