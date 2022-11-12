package ru.RSOI.Gateway.Controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.*;
import org.springframework.web.util.UriComponentsBuilder;
import ru.RSOI.Gateway.Error.EBadRequestError;
import ru.RSOI.Gateway.Error.ECarsErrorBase;
import ru.RSOI.Gateway.Error.EInternalServerError;
import ru.RSOI.Gateway.Error.ENotFoundError;
import ru.RSOI.Gateway.FaultTolerance.FTCircuitBreaker;
import ru.RSOI.Gateway.FaultTolerance.FTDelayedCommand;
import ru.RSOI.Gateway.Model.*;

import java.sql.Date;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1")
public class CGateway {

    public static final String CarsService    = "http://localhost:8070/api/v1/sys/cars";
    public static final String RentService    = "http://localhost:8060/api/v1/sys/rental";
    public static final String PaymentService = "http://localhost:8050/api/v1/sys/payment";

    public static final String CarsManager    = "http://localhost:8070/manage/health";
    public static final String RentManager    = "http://localhost:8060/manage/health";
    public static final String PaymentManager = "http://localhost:8050/manage/health";

    private FTCircuitBreaker CarsCircuitBreaker;
    private FTCircuitBreaker RentCircuitBreaker;
    private FTCircuitBreaker PaymentCircuitBreaker;

    private Queue<FTDelayedCommand> DelayedCommands;

    public CGateway()
    {
        this.CarsCircuitBreaker = new FTCircuitBreaker();
        this.CarsCircuitBreaker.SetRetryTimerTask(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Cars retry");
                CarsCircuitBreaker.SetState(FTCircuitBreaker.State.HalfOpen);

                String url = UriComponentsBuilder.fromHttpUrl(CarsManager)
                        .toUriString();

                HttpHeaders headers = new HttpHeaders();
                HttpEntity<?> entity = new HttpEntity<>(headers);

                RestOperations restOperations = new RestTemplate();
                ResponseEntity<String> response;
                try {
                    response  = restOperations.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            String.class
                    );
                }
                catch (HttpClientErrorException e)
                {
                    System.out.println(e);
                    throw new EBadRequestError(e.toString(), new ArrayList<>());
                }
                catch (HttpServerErrorException e)
                {
                    System.out.println(e);
                    CarsCircuitBreaker.OnFail(); // Microservice still not working
                    throw new EBadRequestError(e.toString(), new ArrayList<>());
                }
                if (response.getStatusCode() == HttpStatus.OK)
                {
                    CarsCircuitBreaker.OnSuccess();
                    processDelayedCommands();
                }
                else
                {
                    CarsCircuitBreaker.OnFail();
                }
            }
        });

        this.RentCircuitBreaker = new FTCircuitBreaker();
        this.RentCircuitBreaker.SetRetryTimerTask(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Rents retry");
                RentCircuitBreaker.SetState(FTCircuitBreaker.State.HalfOpen);

                String url = UriComponentsBuilder.fromHttpUrl(RentManager)
                        .toUriString();

                HttpHeaders headers = new HttpHeaders();
                HttpEntity<?> entity = new HttpEntity<>(headers);

                RestOperations restOperations = new RestTemplate();
                ResponseEntity<String> response;
                try {
                    response  = restOperations.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            String.class
                    );
                }
                catch (HttpClientErrorException e)
                {
                    System.out.println(e);
                    throw new EBadRequestError(e.toString(), new ArrayList<>());
                }
                catch (HttpServerErrorException e)
                {
                    System.out.println(e);
                    RentCircuitBreaker.OnFail(); // Microservice still not working
                    throw new EBadRequestError(e.toString(), new ArrayList<>());
                }
                if (response.getStatusCode() == HttpStatus.OK)
                {
                    RentCircuitBreaker.OnSuccess();
                    processDelayedCommands();
                }
                else
                {
                    RentCircuitBreaker.OnFail();
                }
            }
        });

        this.PaymentCircuitBreaker = new FTCircuitBreaker();
        this.PaymentCircuitBreaker.SetRetryTimerTask(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Pays retry");
                PaymentCircuitBreaker.SetState(FTCircuitBreaker.State.HalfOpen);

                String url = UriComponentsBuilder.fromHttpUrl(PaymentManager)
                        .toUriString();

                HttpHeaders headers = new HttpHeaders();
                HttpEntity<?> entity = new HttpEntity<>(headers);

                RestOperations restOperations = new RestTemplate();
                ResponseEntity<String> response;
                try {
                    response  = restOperations.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            String.class
                    );
                }
                catch (HttpClientErrorException e)
                {
                    System.out.println(e);
                    throw new EBadRequestError(e.toString(), new ArrayList<>());
                }
                catch (HttpServerErrorException e)
                {
                    System.out.println(e);
                    PaymentCircuitBreaker.OnFail(); // Microservice still not working
                    throw new EBadRequestError(e.toString(), new ArrayList<>());
                }
                if (response.getStatusCode() == HttpStatus.OK)
                {
                    PaymentCircuitBreaker.OnSuccess();
                    processDelayedCommands();
                }
                else
                {
                    PaymentCircuitBreaker.OnFail();
                }
            }
        });

        this.DelayedCommands = new ArrayDeque<>();
    }

    @GetMapping("/cars")
    public MCarsPage getAvailableCars(@RequestParam int page, @RequestParam int size,
                                      @RequestParam(defaultValue = "false") boolean showAll)
    {
        if (CarsCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
        {
            // Cars not available now! Fallback
            throw new EInternalServerError("Cars service not available! 1");
        }

        return getCarsPage(page, size, showAll);
    }

    @GetMapping("/rental")
    public List<MRentInfo> getAllUserRents(@RequestHeader(value = "X-User-Name") String username)
    {
        if (RentCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
        {
            throw new EInternalServerError("Rent service not available! 2");
        }

        return getAllUserRentsList(username);
    }

    @PostMapping("/rental")
    public MRentSuccess tryRenting(@RequestHeader(value = "X-User-Name") String username,
                                   @RequestBody Map<String, String> values)
    {
        if (!values.containsKey("carUid") || !values.containsKey("dateFrom") || !values.containsKey("dateTo"))
        {
            throw new EBadRequestError("Not all variables in request!", new ArrayList<>());
        }

        String carUid = values.get("carUid");
        String dateFrom = values.get("dateFrom");
        String dateTo = values.get("dateTo");

        // Request car
        if (CarsCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
        {
            throw new EInternalServerError("Cars service not available! 3");
        }
        MCarInfo car = requestAvailableCar(carUid);
        if (car.available)
        {
            setCarAvailable(carUid, false);
        }
        else
        {
            throw new EBadRequestError("Tried to request reserved car!", new ArrayList<>());
        }

        Date dateFromVal = Date.valueOf(dateFrom);
        Date dateToVal = Date.valueOf(dateTo);
        long diffInMillies = Math.abs(dateToVal.getTime() - dateFromVal.getTime());
        int diff = (int)TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        // Create payment
        if (PaymentCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
        {
            setCarAvailable(carUid, true);
            throw new EInternalServerError("Payment service not available! 4");
        }
        MRentPaymentInfo paymentInfo;
        try {
            paymentInfo = createPayment(diff * car.price);
        }
        catch (ECarsErrorBase e)
        {
            setCarAvailable(carUid, true);
            throw new EInternalServerError(e.toString());
        }

        // Create rent
        if (RentCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
        {
            setCarAvailable(carUid, true);
            cancelPayment(paymentInfo.paymentUid.toString());
            throw new EInternalServerError("Rents service not available! 5");
        }
        MRentInfo rentInfo;
        try {
            rentInfo = createRent(username, carUid, paymentInfo.paymentUid.toString(), dateFrom, dateTo);
        }
        catch (ECarsErrorBase e)
        {
            setCarAvailable(carUid, true);
            cancelPayment(paymentInfo.paymentUid.toString());
            throw new EInternalServerError(e.toString());
        }

        return new MRentSuccess(rentInfo.rentalUid, rentInfo.status, rentInfo.car.carUid,
                rentInfo.dateFrom, rentInfo.dateTo, paymentInfo);
    }

    @GetMapping("/rental/{rentalUid}")
    public MRentInfo getUserRent(@PathVariable String rentalUid, @RequestHeader(value = "X-User-Name") String username)
    {
        if (RentCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
        {
            throw new EInternalServerError("Rent service not available! 6");
        }

        return getUserRentByUid(username, rentalUid);
    }

    @DeleteMapping("/rental/{rentalUid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelUserRent(@PathVariable String rentalUid, @RequestHeader(value = "X-User-Name") String username)
    {
        MRentInfo rentInfo;
        try {
            rentInfo = getUserRentByUid(username, rentalUid);
        }
        catch (ECarsErrorBase e)
        {
            throw new EInternalServerError(e.toString());
        }

        if (rentInfo.status.equals("IN_PROGRESS"))
        {
            try {
                setCarAvailable(rentInfo.car.carUid.toString(), true);
            }
            catch (EInternalServerError e)
            {
                DelayedCommands.add(new FTDelayedCommand(FTDelayedCommand.Type.CarUncheck, rentInfo.car.carUid, null));
                DelayedCommands.add(new FTDelayedCommand(FTDelayedCommand.Type.RentCancel, rentInfo.rentalUid, username));
                DelayedCommands.add(new FTDelayedCommand(FTDelayedCommand.Type.PaymentCancel, rentInfo.payment.paymentUid, null));
                return;
            }

            try{
                cancelRent(username, rentalUid);
            }
            catch (EInternalServerError e)
            {
                DelayedCommands.add(new FTDelayedCommand(FTDelayedCommand.Type.RentCancel, rentInfo.rentalUid, username));
                DelayedCommands.add(new FTDelayedCommand(FTDelayedCommand.Type.PaymentCancel, rentInfo.payment.paymentUid, null));
                return;
            }

            try {
                cancelPayment(rentInfo.payment.paymentUid.toString());
            }
            catch (EInternalServerError e)
            {
                DelayedCommands.add(new FTDelayedCommand(FTDelayedCommand.Type.PaymentCancel, rentInfo.payment.paymentUid, null));
                return;
            }
        }
    }

    @PostMapping("/rental/{rentalUid}/finish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finishUserRent(@PathVariable String rentalUid, @RequestHeader(value = "X-User-Name") String username)
    {
        MRentInfo rentInfo;
        try {
            rentInfo = getUserRentByUid(username, rentalUid);
        }
        catch (ECarsErrorBase e)
        {
            throw new EInternalServerError(e.toString());
        }

        if (rentInfo.status.equals("IN_PROGRESS"))
        {
            try {
                setCarAvailable(rentInfo.car.carUid.toString(), true);
            }
            catch (EInternalServerError e)
            {
                DelayedCommands.add(new FTDelayedCommand(FTDelayedCommand.Type.CarUncheck, rentInfo.car.carUid, null));
                DelayedCommands.add(new FTDelayedCommand(FTDelayedCommand.Type.RentFinish, rentInfo.rentalUid, username));
                return;
            }

            try{
                finishRent(username, rentalUid);
            }
            catch (EInternalServerError e)
            {
                DelayedCommands.add(new FTDelayedCommand(FTDelayedCommand.Type.RentFinish, rentInfo.rentalUid, username));
                return;
            }
        }
    }

    MCarsPage getCarsPage(int page, int size, boolean showAll)
    {
        if(CarsCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
        {
            throw new EInternalServerError("Cars service not available! 7");
        }

        String url = UriComponentsBuilder.fromHttpUrl(CarsService)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("showAll", showAll)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestOperations restOperations = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response  = restOperations.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
        }
        catch (HttpClientErrorException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (HttpServerErrorException e)
        {
            System.out.println(e);
            CarsCircuitBreaker.OnFail();
            // Cars not available now! Fallback
            throw new EInternalServerError("Cars service not available! 8 " + e.toString());
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

        CarsCircuitBreaker.OnSuccess();

        JSONObject obj = new JSONObject(response.getBody());

        int totalElements = obj.getInt("totalElements");

        JSONArray jsonCars = obj.getJSONArray("content");
        int numCars = jsonCars.length();
        ArrayList<MCarInfo> carsInfo = new ArrayList<>(numCars);
        for (int i = 0; i < numCars; ++i)
        {
            JSONObject jsonCar = jsonCars.getJSONObject(i);
            MCarInfo carInfo = parseCarInfo(jsonCar);
            carsInfo.add(carInfo);
        }

        MCarsPage carsPage = new MCarsPage(page, size, totalElements, carsInfo);
        return carsPage;
    }

    private MRentCarInfo getRentCarInfo(String carUid)
    {
        if (CarsCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
        {
            // Fallback
            try {
                return new MRentCarInfo(UUID.fromString(carUid), null, null, null);
            }
            catch (IllegalArgumentException UidE)
            {
                throw new EBadRequestError(UidE.toString(), new ArrayList<>());
            }
        }

        String url = UriComponentsBuilder.fromHttpUrl(CarsService + "/" + carUid)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestOperations restOperations = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response  = restOperations.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
        }
        catch (HttpClientErrorException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (HttpServerErrorException e)
        {
            System.out.println(e);
            CarsCircuitBreaker.OnFail();
            try {
                // Cars not available now! Fallback
                return new MRentCarInfo(UUID.fromString(carUid), null, null, null);
            }
            catch (IllegalArgumentException UidE)
            {
                throw new EBadRequestError(UidE.toString(), new ArrayList<>());
            }
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

        CarsCircuitBreaker.OnSuccess();

        JSONObject obj = new JSONObject(response.getBody());

        try
        {
            UUID carUidVal = UUID.fromString(carUid);
            String brand = obj.getString("v2_brand");
            String model = obj.getString("v3_model");
            String registrationNumber = obj.getString("v4_registration_number");
            return new MRentCarInfo(carUidVal, brand, model, registrationNumber);
        }
        catch (IllegalArgumentException e)
        {
            throw new EBadRequestError("Invalid values passed", new ArrayList<>());
        }

    }

    private MRentPaymentInfo getRentPaymentInfo(String paymentUid)
    {
        if (PaymentCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
        {
            // Fallback
            try {
                return new MRentPaymentInfo(UUID.fromString(paymentUid), null, null);
            }
            catch (IllegalArgumentException UidE)
            {
                throw new EBadRequestError(UidE.toString(), new ArrayList<>());
            }
        }

        String url = UriComponentsBuilder.fromHttpUrl(PaymentService + "/" + paymentUid)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestOperations restOperations = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response  = restOperations.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
        }
        catch (HttpClientErrorException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (HttpServerErrorException e)
        {
            System.out.println(e);
            PaymentCircuitBreaker.OnFail();
            // Fallback
            try {
                return new MRentPaymentInfo(UUID.fromString(paymentUid), null, null);
            }
            catch (IllegalArgumentException UidE)
            {
                throw new EBadRequestError(UidE.toString(), new ArrayList<>());
            }
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

        PaymentCircuitBreaker.OnSuccess();

        JSONObject obj = new JSONObject(response.getBody());

        try
        {
            UUID paymentUidVal = UUID.fromString(paymentUid);
            String status = obj.getString("v2_status");
            Integer price = Integer.valueOf(obj.getInt("v3_price"));

            return new MRentPaymentInfo(paymentUidVal, status, price);
        }
        catch (IllegalArgumentException e)
        {
            throw new EBadRequestError("Invalid values passed", new ArrayList<>());
        }
    }

    private MRentInfo getRentInfoFromJSON(JSONObject obj)
    {
        UUID rentalUid = UUID.fromString(obj.getString("v1_rental_uid"));
        String status = obj.getString("v7_status");
        String dateFrom = obj.getString("v5_date_from");
        String dateTo = obj.getString("v6_date_to");

        int ind1 = dateFrom.indexOf('T');
        if (ind1 != -1)
        {
            dateFrom = dateFrom.substring(0, ind1);
        }
        int ind2 = dateTo.indexOf('T');
        if (ind2 != -1)
        {
            dateTo = dateTo.substring(0, ind2);
        }

        MRentCarInfo rentCarInfo = getRentCarInfo(obj.getString("v4_car_uid"));
        MRentPaymentInfo rentPaymentInfo = getRentPaymentInfo(obj.getString("v3_payment_uid"));

        return new MRentInfo(rentalUid, status, dateFrom, dateTo, rentCarInfo, rentPaymentInfo);
    }

    private MCarInfo parseCarInfo(JSONObject obj)
    {
        UUID carUid = UUID.fromString(obj.getString("v1_car_uid"));
        String brand = obj.getString("v2_brand");
        String model = obj.getString("v3_model");
        String registrationNumber = obj.getString("v4_registration_number");
        int power = obj.getInt("v5_power");
        String type = obj.getString("v7_type");
        int price = obj.getInt("v6_price");
        boolean available = obj.getBoolean("v8_availability");

        return new MCarInfo(carUid, brand, model, registrationNumber, power, type, price, available);
    }
    
    private MCarInfo requestAvailableCar(String carUid)
    {
        if (CarsCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
        {
            throw new EInternalServerError("Cars service not available! 9");
        }

        String url = UriComponentsBuilder.fromHttpUrl(CarsService + "/request/" + carUid)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestOperations restOperations = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response  = restOperations.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );
        }
        catch (HttpClientErrorException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (HttpServerErrorException e)
        {
            System.out.println(e);
            CarsCircuitBreaker.OnFail();
            throw new EInternalServerError("Cars service not available! 10");
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

        CarsCircuitBreaker.OnSuccess();

        JSONObject obj = new JSONObject(response.getBody());
        return parseCarInfo(obj);
    }

    List<MRentInfo> getAllUserRentsList(String username)
    {
        if (RentCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
        {
            throw new EInternalServerError("Rents service not available! 11");
        }

        String url = UriComponentsBuilder.fromHttpUrl(RentService)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set("X-User-Name", username);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestOperations restOperations = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response  = restOperations.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
        }
        catch (HttpClientErrorException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (HttpServerErrorException e)
        {
            System.out.println(e);
            RentCircuitBreaker.OnFail();
            throw new EInternalServerError("Rent service not available! 12 " + e.toString());
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

        RentCircuitBreaker.OnSuccess();

        JSONArray rents = new JSONArray(response.getBody());
        int numRents = rents.length();
        ArrayList<MRentInfo> rentsInfo = new ArrayList<>(numRents);
        for (int i = 0; i < numRents; ++i)
        {
            JSONObject rentInfo = rents.getJSONObject(i);
            rentsInfo.add(getRentInfoFromJSON(rentInfo));
        }

        return rentsInfo;
    }

    MRentInfo getUserRentByUid(String username, String rentalUid)
    {
        List<MRentInfo> allUserRents = getAllUserRents(username);
        for (MRentInfo rentInfo : allUserRents)
        {
            if (rentalUid.equals(rentInfo.rentalUid.toString()))
            {
                return rentInfo;
            }
        }
        throw new ENotFoundError("Rent not found!");
    }

    MRentPaymentInfo createPayment(int price)
    {
        if (PaymentCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
        {
            throw new EInternalServerError("Payments service not available! 13");
        }

        String url = UriComponentsBuilder.fromHttpUrl(PaymentService + "/" + Integer.toString(price))
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestOperations restOperations = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response  = restOperations.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
        }
        catch (HttpClientErrorException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (HttpServerErrorException e)
        {
            System.out.println(e);
            PaymentCircuitBreaker.OnFail();
            throw new EInternalServerError(e.toString());
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

        PaymentCircuitBreaker.OnSuccess();

        JSONObject obj = new JSONObject(response.getBody());

        UUID paymentUid = UUID.fromString(obj.getString("v1_payment_uid"));
        String status = obj.getString("v2_status");
        return new MRentPaymentInfo(paymentUid, status, price);
    }

    MRentInfo createRent(String username, String carUid, String paymentUid, String dateFrom, String dateTo)
    {
        if (RentCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
        {
            throw new EInternalServerError("Rents service not available! 14");
        }

        String url = UriComponentsBuilder.fromHttpUrl(RentService)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set("X-User-Name", username);
        Map<String, String> values = new HashMap<>();
        values.put("carUid", carUid);
        values.put("paymentUid", paymentUid);
        values.put("dateFrom", dateFrom);
        values.put("dateTo", dateTo);
        HttpEntity<?> entity = new HttpEntity<>(values, headers);

        RestOperations restOperations = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response  = restOperations.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
        }
        catch (HttpClientErrorException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (HttpServerErrorException e)
        {
            System.out.println(e);
            RentCircuitBreaker.OnFail();
            throw new EInternalServerError("Rent service not available! 15 " + e.toString());
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

        RentCircuitBreaker.OnSuccess();

        return getRentInfoFromJSON(new JSONObject(response.getBody()));
    }

    void setCarAvailable(String carUid, boolean isSetAvailable)
    {
        if (CarsCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
        {
            throw new EInternalServerError("Cars service not available! 16");
        }

        String url = UriComponentsBuilder.fromHttpUrl(
                CarsService + "/" + carUid + "/" + Boolean.toString(isSetAvailable))
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        // headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestOperations restOperations = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response  = restOperations.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );
        }
        catch (HttpClientErrorException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (HttpServerErrorException e)
        {
            System.out.println(e);
            CarsCircuitBreaker.OnFail();
            throw new EInternalServerError("Cars service not available! 17 " + e.toString());
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

        CarsCircuitBreaker.OnSuccess();

    }

    void cancelRent(String username, String rentalUid)
    {
        if (RentCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
        {
            throw new EInternalServerError("Rents service not available! 18");
        }

        String url = UriComponentsBuilder.fromHttpUrl(RentService + "/" + rentalUid + "/cancel")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set("X-User-Name", username);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestOperations restOperations = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response  = restOperations.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    String.class
            );
        }
        catch (HttpClientErrorException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (HttpServerErrorException e)
        {
            System.out.println(e);
            RentCircuitBreaker.OnFail();
            throw new EInternalServerError("Rents service not available! 19" + e.toString());
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

        RentCircuitBreaker.OnSuccess();
    }

    void finishRent(String username, String rentalUid)
    {
        if (RentCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
        {
            throw new EInternalServerError("Rents service not available! 20");
        }

        String url = UriComponentsBuilder.fromHttpUrl(RentService + "/" + rentalUid + "/finish")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set("X-User-Name", username);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestOperations restOperations = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response  = restOperations.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
        }
        catch (HttpClientErrorException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (HttpServerErrorException e)
        {
            System.out.println(e);
            RentCircuitBreaker.OnFail();
            throw new EInternalServerError("Rents service not available! 21");
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

        RentCircuitBreaker.OnSuccess();
    }

    void cancelPayment(String paymentUid)
    {
        if (PaymentCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
        {
            throw new EInternalServerError("Payment service not available! 22");
        }

        String url = UriComponentsBuilder.fromHttpUrl(PaymentService + "/" + paymentUid)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestOperations restOperations = new RestTemplate();
        ResponseEntity<String> response;
        try {
            response  = restOperations.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    String.class
            );
        }
        catch (HttpClientErrorException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (HttpServerErrorException e)
        {
            System.out.println(e);
            PaymentCircuitBreaker.OnFail();
            throw new EInternalServerError("Payment service not available! 23");
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

        PaymentCircuitBreaker.OnSuccess();
    }

    void processDelayedCommands()
    {
        if (DelayedCommands.isEmpty())
        {
            return;
        }

        while (!DelayedCommands.isEmpty())
        {
            FTDelayedCommand NextCommand = DelayedCommands.element();
            switch (NextCommand.Command)
            {
                case CarUncheck:
                    if (CarsCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
                    {
                        return;
                    }
                    try {
                        setCarAvailable(NextCommand.DataUID.toString(), true);
                    }
                    catch (ECarsErrorBase e)
                    {
                        System.out.println(e);
                        return;
                    }
                    break;

                case RentCancel:
                    if (RentCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
                    {
                        return;
                    }
                    try {
                        cancelRent(NextCommand.Username, NextCommand.DataUID.toString());
                    }
                    catch (ECarsErrorBase e)
                    {
                        System.out.println(e);
                        return;
                    }
                    break;

                case RentFinish:
                    if (RentCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
                    {
                        return;
                    }
                    try {
                        finishRent(NextCommand.Username, NextCommand.DataUID.toString());
                    }
                    catch (ECarsErrorBase e)
                    {
                        System.out.println(e);
                        return;
                    }
                    break;

                case PaymentCancel:
                    if (PaymentCircuitBreaker.GetState() != FTCircuitBreaker.State.Closed)
                    {
                        return;
                    }
                    try {
                        cancelPayment(NextCommand.DataUID.toString());
                    }
                    catch (ECarsErrorBase e)
                    {
                        System.out.println(e);
                        return;
                    }
                    break;
            }
            DelayedCommands.remove();
            System.out.println("Delayed command processed");
        }
    }
}
