package ru.RSOI.Model;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name="cars")
public class MCar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id = -1;

    @Column(name = "car_uid", columnDefinition = "uuid UNIQUE NOT NULL")
    public UUID v1_car_uid = UUID.randomUUID();

    @Column(name = "brand", length = 80, nullable = false)
    public String v2_brand = "";

    @Column(name = "model", length = 80, nullable = false)
    public String v3_model = "";

    @Column(name = "registration_number", length = 20, nullable = false)
    public String v4_registration_number = "";

    @Column(name = "power")
    public int v5_power = 0;

    @Column(name = "price", nullable = false)
    public int v6_price = 0;

    @Column(name = "status", columnDefinition = "VARCHAR(20) CHECK (type IN ('SEDAN', 'SUV', 'MINIVAN', 'ROADSTER'))")
    public String v7_type = "SEDAN";

    @Column(name = "availability", nullable = false)
    public boolean v8_availability = false;

    public MCar()
    {

    }

    public MCar(int id, UUID uuid, String brand, String model, String reg_num,
                int power, int price, String type, boolean isAvailable)
    {
        this.id = id;
        this.v1_car_uid = uuid;
        this.v2_brand = brand;
        this.v3_model = model;
        this.v4_registration_number = reg_num;
        this.v5_power = power;
        this.v6_price = price;
        this.v7_type = type;
        this.v8_availability = isAvailable;
    }

    public int getId() {
        return id;
    }
}
