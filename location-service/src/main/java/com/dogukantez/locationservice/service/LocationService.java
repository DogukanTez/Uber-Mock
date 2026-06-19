package com.dogukantez.locationservice.service;

import com.dogukantez.locationservice.dto.DriverLocationRequest;
import com.dogukantez.locationservice.dto.NearByDriverResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LocationService {

    private final RedisTemplate<String,String> redisTemplate;

    //Redis key for all driver locations
    private static final String DRIVERS_GEO_KEY = "drivers:locations";

    public LocationService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public void updateDriverLocation(DriverLocationRequest driverLocationRequest){
        log.info("Updating location for driver: {}", driverLocationRequest.getDriverId());


        Point driverpoint = new Point(
                driverLocationRequest.getLongitude(),
                driverLocationRequest.getLatitude()
        );

        redisTemplate.opsForGeo().add(DRIVERS_GEO_KEY,driverpoint,driverLocationRequest.getDriverId());
        log.info("Location updated for driver: {}", driverLocationRequest.getDriverId());
    }



    public List<NearByDriverResponse> findNearbyDrivers(
            double latitude, double longitude, double radiusInKm) {
        log.info("Finding drivers near lat: {} long: {} withing {}Km",
                latitude, longitude, radiusInKm);

        Circle searchArea = new Circle(
                new Point(longitude, latitude),
                new Distance(radiusInKm, Metrics.KILOMETERS)
        );

        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(
                        DRIVERS_GEO_KEY,
                        searchArea,
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                                .includeCoordinates()
                                .includeDistance()
                                .sortAscending()
                                .limit(10)
                );

        List<NearByDriverResponse> nearbyDrivers = new ArrayList<>();


            if(results != null){
                results.getContent().forEach(result -> {
                    RedisGeoCommands.GeoLocation<String> location = result.getContent();
                    nearbyDrivers.add(new NearByDriverResponse(
                            location.getName(),
                            location.getPoint().getY(),
                            location.getPoint().getX(),
                            result.getDistance().getValue()
                    ));
                });
            }


            log.info("Found {} drivers nearby", nearbyDrivers.size());
            return nearbyDrivers;

    }


    public void removeDriver(String driverId){
        log.info("Removing driver: {}", driverId);
        redisTemplate.opsForGeo().remove(DRIVERS_GEO_KEY, driverId);
    }




}
