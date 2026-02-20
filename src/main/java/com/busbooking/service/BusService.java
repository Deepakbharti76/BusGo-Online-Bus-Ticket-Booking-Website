package com.busbooking.service;

import com.busbooking.model.BusDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BusService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Hindi → English city normalization
    private static final Map<String, String> CITY_MAP = new HashMap<>();
    static {
        CITY_MAP.put("दिल्ली", "Delhi");
        CITY_MAP.put("दरभंगा", "Darbhanga");
        CITY_MAP.put("पटना", "Patna");
        CITY_MAP.put("मुजफ्फरपुर", "Muzaffarpur");
        CITY_MAP.put("लखनऊ", "Lucknow");
        CITY_MAP.put("झंझारपुर", "Jhanjharpur");
        CITY_MAP.put("लुधियाना", "Ludhiana");
        CITY_MAP.put("सोनीपत", "Sonipat");
    }

    public String normalizeCity(String city) {
        city = city.trim();
        return CITY_MAP.getOrDefault(city, city);
    }

    /* =====================
       ALL BUSES
    ===================== */
    public List<BusDTO> getAllBuses() {
        String sql = "SELECT bus_id, bus_name, source, destination, available_seats FROM bus";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            BusDTO bus = new BusDTO();
            bus.setBusId(rs.getInt("bus_id"));
            bus.setBusName(rs.getString("bus_name"));
            bus.setSource(rs.getString("source"));
            bus.setDestination(rs.getString("destination"));
            bus.setAvailableSeats(rs.getInt("available_seats"));
            return bus;
        });
    }

    /* =====================
       SEARCH BUS BY ROUTE
    ===================== */
    public List<Map<String, Object>> searchBuses(String fromCity, String toCity) {
        fromCity = normalizeCity(fromCity);
        toCity = normalizeCity(toCity);

        String sql = "SELECT DISTINCT b.bus_id, b.bus_name, b.source, b.destination, b.available_seats " +
                     "FROM bus b " +
                     "JOIN bus_route r1 ON b.bus_id = r1.bus_id " +
                     "JOIN bus_route r2 ON b.bus_id = r2.bus_id " +
                     "WHERE LOWER(r1.city_en) = LOWER(?) " +
                     "AND LOWER(r2.city_en) = LOWER(?) " +
                     "AND r1.stop_order < r2.stop_order";

        final String fc = fromCity, tc = toCity;
        return jdbcTemplate.queryForList(sql, fc, tc);
    }

    /* =====================
       GET SEAT TYPES FOR BUS
    ===================== */
    public List<Map<String, Object>> getSeatTypes(int busId) {
        String sql = "SELECT seat_type, available_seats, base_fare FROM bus_seat_type WHERE bus_id = ?";
        return jdbcTemplate.queryForList(sql, busId);
    }

    /* =====================
       GET STOP ORDER
    ===================== */
    public int getStopOrder(int busId, String city) {
        city = normalizeCity(city);
        try {
            String sql = "SELECT stop_order FROM bus_route WHERE bus_id=? AND LOWER(city_en)=LOWER(?)";
            return jdbcTemplate.queryForObject(sql, Integer.class, busId, city);
        } catch (Exception e) {
            return -1;
        }
    }
}