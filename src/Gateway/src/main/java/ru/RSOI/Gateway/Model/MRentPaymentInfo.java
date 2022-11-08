package ru.RSOI.Gateway.Model;

import java.util.UUID;

public class MRentPaymentInfo
{
    public UUID paymentUid;
    public String status;
    public int price;

    public MRentPaymentInfo(UUID paymentUid, String status, int price) {
        this.paymentUid = paymentUid;
        this.status = status;
        this.price = price;
    }
}
