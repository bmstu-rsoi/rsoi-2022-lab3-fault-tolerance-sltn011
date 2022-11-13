package ru.RSOI.Gateway.Model;

public class MRentPaymentFilter {
    @Override
    public boolean equals(Object obj) {
        MRentPaymentInfo paymentInfo = (MRentPaymentInfo) obj;
        return paymentInfo.price != null && paymentInfo.status != null;
    }
}
