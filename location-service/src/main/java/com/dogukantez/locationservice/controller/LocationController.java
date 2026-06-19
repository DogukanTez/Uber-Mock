package com.dogukantez.locationservice.controller;

import com.dogukantez.locationservice.dto.DriverLocationRequest;
import com.dogukantez.locationservice.dto.NearByDriverResponse;
import com.dogukantez.locationservice.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/locations")
@Slf4j
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }


    //every 3 seconds, driver posts his-her location
    @PostMapping("/drivers/update")
    public ResponseEntity<String> updateDriverLocation(@RequestBody DriverLocationRequest driverLocationRequest){
        locationService.updateDriverLocation(driverLocationRequest);
        return ResponseEntity.ok("Driver Location updated");
    }

    @GetMapping("/drivers/nearby")
    public ResponseEntity<List<NearByDriverResponse>> getNearbyDrivers (
            @RequestParam double latitude, @RequestParam double longitude, @RequestParam (defaultValue = "5.0") double radius){

        return ResponseEntity.ok(locationService.findNearbyDrivers(latitude, longitude, radius));

    }



    //driver goes offline
    @DeleteMapping("/drivers/{driverID}")
    public ResponseEntity<String> removeDriver(@PathVariable String driverID){
        locationService.removeDriver(driverID);
        return ResponseEntity.ok("Driver removed successfully");
    }







}
