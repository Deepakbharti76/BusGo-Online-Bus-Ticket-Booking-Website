package com.busbooking.controller;

import com.busbooking.model.BookingDTO;
import com.busbooking.model.BookingRequest;
import com.busbooking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // POST /api/bookings - book a ticket
    @PostMapping
    public ResponseEntity<Map<String, Object>> bookTicket(@RequestBody BookingRequest req) {
        Map<String, Object> result = bookingService.bookTicket(req);
        return ResponseEntity.ok(result);
    }

    // GET /api/bookings/pnr/{pnr} - get booking by PNR
    @GetMapping("/pnr/{pnr}")
    public ResponseEntity<?> getByPNR(@PathVariable String pnr) {
        BookingDTO dto = bookingService.getBookingByPNR(pnr);
        if (dto == null) return ResponseEntity.status(404).body(Map.of("message", "Booking not found"));
        return ResponseEntity.ok(dto);
    }

    // GET /api/bookings/history?mobile=9999999999 - history by mobile
    @GetMapping("/history")
    public ResponseEntity<List<BookingDTO>> getHistory(@RequestParam String mobile) {
        return ResponseEntity.ok(bookingService.getBookingsByMobile(mobile));
    }

    // GET /api/bookings/all - all bookings (admin)
    @GetMapping("/all")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // DELETE /api/bookings/{bookingId} - cancel ticket
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Map<String, Object>> cancelTicket(@PathVariable int bookingId) {
        return ResponseEntity.ok(bookingService.cancelTicket(bookingId));
    }

    // GET /api/bookings/pdf/{pnr} - download PDF ticket
    @GetMapping("/pdf/{pnr}")
    public ResponseEntity<byte[]> downloadPDF(@PathVariable String pnr) {
        try {
            byte[] pdf = bookingService.generatePDFTicket(pnr);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Ticket_" + pnr + ".pdf");
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }
}