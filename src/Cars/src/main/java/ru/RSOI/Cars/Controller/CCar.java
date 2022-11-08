package ru.RSOI.Cars.Controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.RSOI.Cars.Model.MCar;
import ru.RSOI.Cars.Repo.RCar;

import ru.RSOI.Cars.Error.*;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v1/sys/cars")
public class CCar {

    private final RCar carRepo;

    public CCar(RCar carRepo)
    {
        this.carRepo = carRepo;
    }

    @GetMapping("")
    public Iterable<MCar> getCars(
            @RequestParam int page, @RequestParam int size, @RequestParam(defaultValue = "false") boolean showAll)
    {
        return getCarsPage(page - 1, size, showAll);
    }

    @GetMapping("/{carUid}")
    public MCar getCar(@PathVariable String carUid)
    {
        return findCar(UUID.fromString(carUid))
                .orElseThrow(() -> new EBadRequestError("Car not found!", new ArrayList<>()));
    }

    @PostMapping("")
    public ResponseEntity<Object> addCar(@RequestBody Map<String, String> values)
    {
        MCar car = new MCar();
        fillValues(car, values);
        int newID = carRepo.save(car).getId();

        String stringLocation = String.format("/api/v1/sys/cars/%d", newID);
        URI location = ServletUriComponentsBuilder.fromUriString(stringLocation).build().toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{carUid}/{isSetAvailable}")
    public MCar updateAvailableCar(@PathVariable String carUid, @PathVariable boolean isSetAvailable)
    {
        UUID carUidVal = UUID.fromString(carUid);
        MCar car = findAnyCar(carUidVal);
        updateAvailability(car, isSetAvailable);
        carRepo.deleteById(car.getId());
        return carRepo.save(car);
    }

    @PutMapping("/request/{carUid}")
    public MCar requestAvailableCar(@PathVariable String carUid)
    {
        MCar car = findAvailableCar(UUID.fromString(carUid));
        updateAvailability(car, false);
        carRepo.deleteById(car.getId());
        return carRepo.save(car);
    }

    private Iterable<MCar> getCarsPage(int page, int size, boolean showAll)
    {
        if (showAll)
        {
            return carRepo.findAll(PageRequest.of(page, size));
        }
        else
        {
            return carRepo.findAvailable(PageRequest.of(page, size));
        }
    }

    private Optional<MCar> findCar(UUID carUid)
    {
        List<MCar> cars = carRepo.findCarByUid(carUid);
        if (cars.size() == 0)
        {
            return Optional.empty();
        }

        return Optional.of(cars.get(0));
    }

    private MCar findAnyCar(UUID carUid)
    {
        Optional<MCar> car = findCar(carUid);
        if (car.isPresent())
        {
            return car.get();
        }
        throw new EBadRequestError("Car not found!", new ArrayList<>());
    }

    private MCar findAvailableCar(UUID carUid)
    {
        Optional<MCar> car = findCar(carUid);
        if (car.isPresent() && car.get().v8_availability == true)
        {
            return car.get();
        }
        throw new EBadRequestError("Car not available!", new ArrayList<>());
    }

    private MCar updateAvailability(MCar car, boolean isSetAvailable)
    {
        car.v8_availability = isSetAvailable;
        return car;
    }

    private MCar fillValues(MCar car, Map<String, String> values)
    {
        if (values.containsKey("brand"))
        {
            car.v2_brand = values.get("brand");
        }
        if (values.containsKey("model"))
        {
            car.v3_model = values.get("model");
        }
        if (values.containsKey("registration_number"))
        {
            car.v4_registration_number = values.get("registration_number");
        }
        if (values.containsKey("power"))
        {
            car.v5_power = Integer.parseInt(values.get("power"));
        }
        if (values.containsKey("price"))
        {
            car.v6_price = Integer.parseInt(values.get("price"));
        }
        if (values.containsKey("type"))
        {
            car.v7_type = values.get("type");
        }
        if (values.containsKey("availability"))
        {
            car.v8_availability = Boolean.parseBoolean(values.get("availability"));
        }

        return car;
    }
}
