package ru.RSOI.Payment.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.RSOI.Payment.Error.EBadRequestError;
import ru.RSOI.Payment.Error.ENotFoundError;
import ru.RSOI.Payment.Model.MPayment;
import ru.RSOI.Payment.Repo.RPayment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sys/payment")
public class CPayment {

    private final RPayment paymentRepo;

    public CPayment(RPayment paymentRepo)
    {
        this.paymentRepo = paymentRepo;
    }

    @GetMapping("")
    public List<MPayment> getAll()
    {
        return paymentRepo.findAll();
    }

    @GetMapping("/{payment_uid}")
    public MPayment getPayments(@PathVariable String payment_uid)
    {
        UUID uid = UUID.fromString(payment_uid);
        return findPayment(uid);
    }

    @PostMapping("/{price}")
    public MPayment createPayment(@PathVariable int price)
    {
        MPayment payment = new MPayment();
        payment.v2_status = "PAID";
        payment.v3_price = price;
        paymentRepo.save(payment);
        return payment;
    }

    @DeleteMapping("/{payment_uid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelPayment(@PathVariable String payment_uid)
    {
        UUID uid = UUID.fromString(payment_uid);
        MPayment payment = findPayment(uid);
        paymentRepo.deleteById(payment.getId());
        payment.v2_status = "CANCELED";
        paymentRepo.save(payment);
    }

    private MPayment findPayment(UUID payment_uid)
    {
        List<MPayment> payment = paymentRepo.findPayment(payment_uid);

        if (payment.size() == 0)
        {
            ENotFoundError error = new ENotFoundError("payment not found!");
            throw error;
        }

        return payment.get(0);
    }
}
