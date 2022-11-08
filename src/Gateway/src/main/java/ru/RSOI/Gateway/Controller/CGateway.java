package ru.RSOI.Gateway.Controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.*;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import ru.RSOI.Gateway.Error.EBadRequestError;
import ru.RSOI.Gateway.Error.ENotFoundError;
import ru.RSOI.Gateway.Model.*;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1")
public class CGateway {

    public static final String CarsService    = "http://cars:8070/api/v1/sys/cars";
    public static final String RentService    = "http://rental:8060/api/v1/sys/rental";
    public static final String PaymentService = "http://payment:8050/api/v1/sys/payment";

    @GetMapping("/cars")
    public MCarsPage getAvailableCars(@RequestParam int page, @RequestParam int size,
                                      @RequestParam(defaultValue = "false") boolean showAll)
    {
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
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (RestClientException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

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

    @GetMapping("/rental")
    public List<MRentInfo> getAllUserRents(@RequestHeader(value = "X-User-Name") String username)
    {
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
        MCarInfo car = requestAvailableCar(carUid);

        Date dateFromVal = Date.valueOf(dateFrom);
        Date dateToVal = Date.valueOf(dateTo);
        long diffInMillies = Math.abs(dateToVal.getTime() - dateFromVal.getTime());
        int diff = (int)TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        MRentPaymentInfo paymentInfo = createPayment(diff * car.price);
        MRentInfo rentInfo = createRent(username, carUid, paymentInfo.paymentUid.toString(), dateFrom, dateTo);

        return new MRentSuccess(rentInfo.rentalUid, rentInfo.status, rentInfo.car.carUid,
                rentInfo.dateFrom, rentInfo.dateTo, paymentInfo);
    }

    @GetMapping("/rental/{rentalUid}")
    public MRentInfo getUserRent(@PathVariable String rentalUid, @RequestHeader(value = "X-User-Name") String username)
    {
        return getUserRentByUid(username, rentalUid);
    }

    @DeleteMapping("/rental/{rentalUid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelUserRent(@PathVariable String rentalUid, @RequestHeader(value = "X-User-Name") String username)
    {
        MRentInfo rentInfo = getUserRentByUid(username, rentalUid);
        if (rentInfo.status.equals("IN_PROGRESS"))
        {
            setCarAvailable(rentInfo.car.carUid.toString(), true);
            cancelRent(username, rentalUid);
            cancelPayment(rentInfo.payment.paymentUid.toString());
        }
    }

    @PostMapping("/rental/{rentalUid}/finish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finishUserRent(@PathVariable String rentalUid, @RequestHeader(value = "X-User-Name") String username)
    {
        MRentInfo rentInfo = getUserRentByUid(username, rentalUid);
        if (rentInfo.status.equals("IN_PROGRESS"))
        {
            setCarAvailable(rentInfo.car.carUid.toString(), true);
            finishRent(username, rentalUid);
        }
    }

    private MRentCarInfo getRentCarInfo(String carUid)
    {
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
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (RestClientException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }


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
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (RestClientException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

        JSONObject obj = new JSONObject(response.getBody());

        try
        {
            UUID paymentUidVal = UUID.fromString(paymentUid);
            String status = obj.getString("v2_status");
            int price = obj.getInt("v3_price");

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
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (RestClientException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

        JSONObject obj = new JSONObject(response.getBody());
        return parseCarInfo(obj);
    }

    List<MRentInfo> getAllUserRentsList(String username)
    {
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
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (RestClientException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

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
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (RestClientException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

        JSONObject obj = new JSONObject(response.getBody());

        UUID paymentUid = UUID.fromString(obj.getString("v1_payment_uid"));
        String status = obj.getString("v2_status");
        return new MRentPaymentInfo(paymentUid, status, price);
    }

    MRentInfo createRent(String username, String carUid, String paymentUid, String dateFrom, String dateTo)
    {
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
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (RestClientException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

        return getRentInfoFromJSON(new JSONObject(response.getBody()));
    }

    void setCarAvailable(String carUid, boolean isSetAvailable)
    {
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
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (RestClientException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }

    }

    void cancelRent(String username, String rentalUid)
    {
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
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (RestClientException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }
    }

    void finishRent(String username, String rentalUid)
    {
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
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (RestClientException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }
    }

    void cancelPayment(String paymentUid)
    {
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
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        catch (RestClientException e)
        {
            System.out.println(e);
            throw new EBadRequestError(e.toString(), new ArrayList<>());
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND)
        {
            throw new ENotFoundError(response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            throw new EBadRequestError(response.getBody(), new ArrayList<>());
        }
    }
}
