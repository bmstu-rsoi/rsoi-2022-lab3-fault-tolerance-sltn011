package ru.RSOI.Gateway.Model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;
@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = MRentPaymentFilter.class)
public class MRentPaymentInfo
{
    public UUID paymentUid;
    public String status;
    public Integer price;

    public MRentPaymentInfo(UUID paymentUid, String status, Integer price) {
        this.paymentUid = paymentUid;
        this.status = status;
        this.price = price;
    }
}
