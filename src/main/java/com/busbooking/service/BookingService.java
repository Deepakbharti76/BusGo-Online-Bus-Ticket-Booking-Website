package com.busbooking.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.busbooking.model.BookingDTO;
import com.busbooking.model.BookingRequest;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class BookingService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BusService busService;

    /* =====================
       PNR GENERATOR
    ===================== */
    private String generatePNR() {
        return "PNR" + System.currentTimeMillis();
    }

    /* =====================
       FARE CALCULATOR
    ===================== */
    private double calculateFare(int busId, String seatType, String fromCity, String toCity, int seats) {
        int fromStop = busService.getStopOrder(busId, fromCity);
        int toStop   = busService.getStopOrder(busId, toCity);

        if (fromStop == -1 || toStop == -1 || fromStop >= toStop) return -1;

        int usedSegments  = toStop - fromStop;
        int totalSegments = 15;

        try {
            String sql = "SELECT base_fare FROM bus_seat_type WHERE bus_id=? AND seat_type=?";
            Double baseFare = jdbcTemplate.queryForObject(sql, Double.class, busId, seatType);
            if (baseFare == null) return -1;
            double farePerSegment = baseFare / totalSegments;
            return farePerSegment * usedSegments * seats;
        } catch (Exception e) {
            return -1;
        }
    }

    /* =====================
       BOOK TICKET
    ===================== */
    @Transactional
    public Map<String, Object> bookTicket(BookingRequest req) {

        // Validation
        if (req.getPassengerName() == null || req.getPassengerName().isEmpty()) {
            return Map.of("success", false, "message", "Passenger name required");
        }
        if (req.getMobile() == null || req.getMobile().isEmpty()) {
            return Map.of("success", false, "message", "Mobile number required");
        }
        if (req.getSeats() <= 0) {
            return Map.of("success", false, "message", "Invalid seat count");
        }

        try {
            LocalDate.parse(req.getTravelDate());
        } catch (Exception e) {
            return Map.of("success", false, "message", "Invalid date format (YYYY-MM-DD)");
        }

        // Check seat availability
        try {
            String checkSql = "SELECT available_seats FROM bus_seat_type WHERE bus_id=? AND seat_type=?";
            Integer available = jdbcTemplate.queryForObject(checkSql, Integer.class,
                    req.getBusId(), req.getSeatType());

            if (available == null || available < req.getSeats()) {
                return Map.of("success", false, "message", "Not enough seats available. Available: " + (available != null ? available : 0));
            }
        } catch (Exception e) {
            return Map.of("success", false, "message", "Bus/Seat type not found");
        }

        // Calculate fare
        double fare = calculateFare(req.getBusId(), req.getSeatType(),
                req.getFromCity(), req.getToCity(), req.getSeats());

        if (fare <= 0) {
            return Map.of("success", false, "message", "Invalid route or seat type");
        }

        String pnr = generatePNR();

        // Insert booking
        String insertSql = "INSERT INTO booking (bus_id, pnr, passenger_name, contact_number, email, " +
                "from_city, to_city, travel_date, seat_type, seats_booked, fare) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        jdbcTemplate.update(insertSql,
                req.getBusId(), pnr, req.getPassengerName(),
                req.getMobile(), req.getEmail(),
                req.getFromCity(), req.getToCity(),
                java.sql.Date.valueOf(LocalDate.parse(req.getTravelDate())),
                req.getSeatType(), req.getSeats(), fare);

        // Update available seats
        String updateSql = "UPDATE bus_seat_type SET available_seats = available_seats - ? " +
                "WHERE bus_id=? AND seat_type=?";
        jdbcTemplate.update(updateSql, req.getSeats(), req.getBusId(), req.getSeatType());

        return Map.of("success", true, "pnr", pnr, "fare", fare,
                "message", "Ticket booked successfully!");
    }

    /* =====================
       GET BOOKING BY PNR
    ===================== */
    public BookingDTO getBookingByPNR(String pnr) {
        try {
            String sql = "SELECT b.*, bs.bus_name FROM booking b " +
                    "JOIN bus bs ON b.bus_id = bs.bus_id WHERE b.pnr=?";
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                BookingDTO dto = new BookingDTO();
                dto.setBookingId(rs.getInt("booking_id"));
                dto.setBusId(rs.getInt("bus_id"));
                dto.setPnr(rs.getString("pnr"));
                dto.setPassengerName(rs.getString("passenger_name"));
                dto.setContactNumber(rs.getString("contact_number"));
                dto.setEmail(rs.getString("email"));
                dto.setFromCity(rs.getString("from_city"));
                dto.setToCity(rs.getString("to_city"));
                dto.setTravelDate(rs.getString("travel_date"));
                dto.setSeatType(rs.getString("seat_type"));
                dto.setSeatsBooked(rs.getInt("seats_booked"));
                dto.setFare(rs.getDouble("fare"));
                dto.setBusName(rs.getString("bus_name"));
                return dto;
            }, pnr);
        } catch (Exception e) {
            return null;
        }
    }

    /* =====================
       BOOKING HISTORY BY MOBILE
    ===================== */
    public List<BookingDTO> getBookingsByMobile(String mobile) {
        String sql = "SELECT b.*, bs.bus_name FROM booking b " +
                "JOIN bus bs ON b.bus_id = bs.bus_id WHERE b.contact_number=?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            BookingDTO dto = new BookingDTO();
            dto.setBookingId(rs.getInt("booking_id"));
            dto.setBusId(rs.getInt("bus_id"));
            dto.setPnr(rs.getString("pnr"));
            dto.setPassengerName(rs.getString("passenger_name"));
            dto.setContactNumber(rs.getString("contact_number"));
            dto.setEmail(rs.getString("email"));
            dto.setFromCity(rs.getString("from_city"));
            dto.setToCity(rs.getString("to_city"));
            dto.setTravelDate(rs.getString("travel_date"));
            dto.setSeatType(rs.getString("seat_type"));
            dto.setSeatsBooked(rs.getInt("seats_booked"));
            dto.setFare(rs.getDouble("fare"));
            dto.setBusName(rs.getString("bus_name"));
            return dto;
        }, mobile);
    }

    /* =====================
       ALL BOOKINGS (Admin)
    ===================== */
    public List<BookingDTO> getAllBookings() {
        String sql = "SELECT b.*, bs.bus_name FROM booking b " +
                "JOIN bus bs ON b.bus_id = bs.bus_id ORDER BY b.booking_id DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            BookingDTO dto = new BookingDTO();
            dto.setBookingId(rs.getInt("booking_id"));
            dto.setBusId(rs.getInt("bus_id"));
            dto.setPnr(rs.getString("pnr"));
            dto.setPassengerName(rs.getString("passenger_name"));
            dto.setContactNumber(rs.getString("contact_number"));
            dto.setEmail(rs.getString("email"));
            dto.setFromCity(rs.getString("from_city"));
            dto.setToCity(rs.getString("to_city"));
            dto.setTravelDate(rs.getString("travel_date"));
            dto.setSeatType(rs.getString("seat_type"));
            dto.setSeatsBooked(rs.getInt("seats_booked"));
            dto.setFare(rs.getDouble("fare"));
            dto.setBusName(rs.getString("bus_name"));
            return dto;
        });
    }

    /* =====================
       CANCEL TICKET
    ===================== */
    @Transactional
    public Map<String, Object> cancelTicket(int bookingId) {
        try {
            String selectSql = "SELECT bus_id, seat_type, seats_booked FROM booking WHERE booking_id=?";
            Map<String, Object> row = jdbcTemplate.queryForMap(selectSql, bookingId);

            if (row == null) {
                return Map.of("success", false, "message", "Invalid Booking ID");
            }

            int busId = (int) row.get("bus_id");
            String seatType = (String) row.get("seat_type");
            int seats = (int) row.get("seats_booked");

            jdbcTemplate.update("DELETE FROM booking WHERE booking_id=?", bookingId);

            jdbcTemplate.update(
                "UPDATE bus_seat_type SET available_seats = available_seats + ? WHERE bus_id=? AND seat_type=?",
                seats, busId, seatType
            );

            return Map.of("success", true, "message", "Ticket cancelled successfully");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Booking not found or error: " + e.getMessage());
        }
    }

    /* =====================
       GENERATE PDF (bytes)
    ===================== */
    public byte[] generatePDFTicket(String pnr) throws Exception {
        BookingDTO booking = getBookingByPNR(pnr);
        if (booking == null) throw new Exception("Booking not found");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document();
        PdfWriter.getInstance(doc, baos);
        doc.open();

        doc.add(new Paragraph("============= BUS TICKET ============="));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("PNR         : " + booking.getPnr()));
        doc.add(new Paragraph("Bus Name    : " + booking.getBusName()));
        doc.add(new Paragraph("Passenger   : " + booking.getPassengerName()));
        doc.add(new Paragraph("Mobile      : " + booking.getContactNumber()));
        doc.add(new Paragraph("Email       : " + booking.getEmail()));
        doc.add(new Paragraph("Route       : " + booking.getFromCity() + " → " + booking.getToCity()));
        doc.add(new Paragraph("Travel Date : " + booking.getTravelDate()));
        doc.add(new Paragraph("Seat Type   : " + booking.getSeatType()));
        doc.add(new Paragraph("Seats       : " + booking.getSeatsBooked()));
        doc.add(new Paragraph("Fare        : ₹" + booking.getFare()));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("======================================="));
        doc.add(new Paragraph("Have a safe journey! 🚌"));

        doc.close();
        return baos.toByteArray();
    }
}
