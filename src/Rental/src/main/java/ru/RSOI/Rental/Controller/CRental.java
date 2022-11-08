package ru.RSOI.Rental.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.RSOI.Rental.Error.EBadRequestError;
import ru.RSOI.Rental.Error.ENotFoundError;
import ru.RSOI.Rental.Model.MRental;
import ru.RSOI.Rental.Repo.RRental;

import java.sql.Timestamp;
import java.util.*;

@RestController
@RequestMapping("/api/v1/sys/rental")
public class CRental {

    private final RRental rentRepo;

    public CRental(RRental rentRepo)
    {
        this.rentRepo = rentRepo;
    }

    @GetMapping("")
    public List<MRental> getUserRents(@RequestHeader(value = "X-User-Name") String username)
    {
        return rentRepo.findUserRents(username);
    }

    @GetMapping("/{rentalUid}")
    public MRental getRentByUUID(@PathVariable String rentalUid, @RequestHeader(value = "X-User-Name") String username)
    {
        UUID uid = UUID.fromString(rentalUid);
        return findRentWithChecks(uid, username);
    }

    @PostMapping("")
    public MRental tryRenting(@RequestHeader(value = "X-User-Name") String username, @RequestBody Map<String, String> values)
    {
        UUID carUid;
        UUID paymentUid;
        Timestamp dateFrom;
        Timestamp dateTo;

        if (!values.containsKey("carUid") || !values.containsKey("paymentUid")
                || !values.containsKey("dateFrom") || !values.containsKey("dateTo"))
        {
            EBadRequestError error =
                    new EBadRequestError("Invalid parameters passed to tryRenting!", new ArrayList<>());
            throw error;
        }

        try
        {
            carUid = UUID.fromString(values.get("carUid"));
        }
        catch (IllegalArgumentException e)
        {
            EBadRequestError error =
                    new EBadRequestError("Invalid car uuid passed to tryRenting!", new ArrayList<>());
            throw error;
        }
        try
        {
            paymentUid = UUID.fromString(values.get("paymentUid"));
        }
        catch (IllegalArgumentException e)
        {
            EBadRequestError error =
                    new EBadRequestError("Invalid payment uuid passed to tryRenting!", new ArrayList<>());
            throw error;
        }
        try
        {
            dateFrom = Timestamp.valueOf(values.get("dateFrom") + " 12:00:00");
        }
        catch (IllegalArgumentException e)
        {
            EBadRequestError error =
                    new EBadRequestError("Invalid dateFrom passed to tryRenting!", new ArrayList<>());
            throw error;
        }
        try
        {
            dateTo = Timestamp.valueOf(values.get("dateTo") + " 12:00:00");
        }
        catch (IllegalArgumentException e)
        {
            EBadRequestError error =
                    new EBadRequestError("Invalid dateTo passed to tryRenting!", new ArrayList<>());
            throw error;
        }

        /*
        List<MRental> carRents = rentRepo.findCarRents(carUid, "IN_PROGRESS");
        boolean isFree = true;
        for (MRental rent : carRents)
        {
            boolean starts_after = (dateFrom.compareTo(rent.v6_date_to) >= 0);
            boolean ends_before = (dateTo.compareTo(rent.v5_date_from) <= 0);
            if (!starts_after && !ends_before)
            {
                isFree = false;
                break;
            }
        }
        if(!isFree)
        {
            EBadRequestError error = new EBadRequestError("Rent date overlaps with another", new ArrayList<>());
            throw error;
        }
         */

        MRental newRent = new MRental();
        newRent.v2_username = username;
        newRent.v3_payment_uid = paymentUid;
        newRent.v4_car_uid = carUid;
        newRent.v5_date_from = dateFrom;
        newRent.v6_date_to = dateTo;
        newRent.v7_status = "IN_PROGRESS";

        rentRepo.save(newRent);
        return newRent;
    }

    @PostMapping("/{rentalUid}/finish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finish(@PathVariable String rentalUid, @RequestHeader(value = "X-User-Name") String username)
    {
        UUID uid = UUID.fromString(rentalUid);
        MRental foundRent = findInProgressRentWithChecks(uid, username);
        foundRent.v7_status = "FINISHED";
        rentRepo.deleteById(foundRent.getId());
        rentRepo.save(foundRent);
    }

    @DeleteMapping("/{rentalUid}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelRent(@PathVariable String rentalUid, @RequestHeader(value = "X-User-Name") String username)
    {
        UUID uid = UUID.fromString(rentalUid);
        MRental foundRent = findInProgressRentWithChecks(uid, username);
        foundRent.v7_status = "CANCELED";
        rentRepo.deleteById(foundRent.getId());
        rentRepo.save(foundRent);
    }

    MRental findRentWithChecks(UUID rentalUid, String username)
    {
        List<MRental> rent = rentRepo.findRentByUUID(rentalUid);
        if (rent.size() == 0)
        {
            ENotFoundError error = new ENotFoundError("Rent not found by uuid!");
            throw error;
        }

        MRental foundRent = rent.get(0);
        if (!foundRent.v2_username.equals(username))
        {
            ENotFoundError error = new ENotFoundError("Rent not found!");
            throw error;
        }

        return foundRent;
    }

    MRental findInProgressRentWithChecks(UUID rentalUid, String username)
    {
        List<MRental> rent = rentRepo.findInProgressRentByUUID(rentalUid);
        if (rent.size() == 0)
        {
            ENotFoundError error = new ENotFoundError("Rent not found by uuid!");
            throw error;
        }

        MRental foundRent = rent.get(0);
        if (!foundRent.v2_username.equals(username))
        {
            ENotFoundError error = new ENotFoundError("Rent not found!");
            throw error;
        }

        return foundRent;
    }

}
