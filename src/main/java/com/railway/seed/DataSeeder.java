package com.railway.seed;

import com.railway.model.*;
import com.railway.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final SeatClassRepository seatClassRepository;
    private final TrainRepository trainRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("Data already seeded, skipping...");
            return;
        }

        // 1. Seat Classes
        SeatClass sl = saveClass("SL", "Sleeper");
        SeatClass ac3 = saveClass("3A", "AC 3 Tier");
        SeatClass ac2 = saveClass("2A", "AC 2 Tier");
        SeatClass ac1 = saveClass("1A", "AC First Class");
        SeatClass cc = saveClass("CC", "AC Chair Car");
        SeatClass ec = saveClass("EC", "Executive Chair");

        // 2. Users (Admin + Customer)
        saveUser("admin@railway.com", "admin123", "Admin", "User", Role.ADMIN);
        saveUser("user@railway.com", "user1234", "Demo", "Customer", Role.CUSTOMER);

        // 3. 25+ Major Indian Stations across all railway zones
        Station hwh = saveStation("HWH", "Howrah Junction", "Kolkata");
        Station ndls = saveStation("NDLS", "New Delhi", "Delhi");
        Station dli = saveStation("DLI", "Old Delhi", "Delhi");
        Station mas = saveStation("MAS", "Chennai Central", "Chennai");
        Station bct = saveStation("BCT", "Mumbai Central", "Mumbai");
        Station csmt = saveStation("CSMT", "Chhatrapati Shivaji Maharaj Terminus", "Mumbai");
        Station sbc = saveStation("SBC", "KSR Bengaluru", "Bengaluru");
        Station pnbe = saveStation("PNBE", "Patna Junction", "Patna");
        Station jp = saveStation("JP", "Jaipur Junction", "Jaipur");
        Station lko = saveStation("LKO", "Lucknow Charbagh", "Lucknow");
        Station ghy = saveStation("GHY", "Guwahati", "Guwahati");
        Station adi = saveStation("ADI", "Ahmedabad Junction", "Ahmedabad");
        Station bsb = saveStation("BSB", "Varanasi Junction", "Varanasi");
        Station cnb = saveStation("CNB", "Kanpur Central", "Kanpur");
        Station pune = saveStation("PUNE", "Pune Junction", "Pune");
        Station hyb = saveStation("HYB", "Hyderabad Deccan", "Hyderabad");
        Station agc = saveStation("AGC", "Agra Cantt", "Agra");
        Station cdg = saveStation("CDG", "Chandigarh", "Chandigarh");
        Station asr = saveStation("ASR", "Amritsar Junction", "Amritsar");
        Station bpl = saveStation("BPL", "Bhopal Junction", "Bhopal");
        Station ngp = saveStation("NGP", "Nagpur Junction", "Nagpur");
        Station bbs = saveStation("BBS", "Bhubaneswar", "Bhubaneswar");
        Station rnc = saveStation("RNC", "Ranchi Junction", "Ranchi");
        Station gkp = saveStation("GKP", "Gorakhpur Junction", "Gorakhpur");
        Station mao = saveStation("MAO", "Madgaon", "Goa");

        String daily = "MON,TUE,WED,THU,FRI,SAT,SUN";

        // --- 1. Howrah <-> New Delhi Rajdhani Express ---
        Train t12301 = new Train();
        t12301.setTrainNumber("12301");
        t12301.setName("Howrah - New Delhi Rajdhani Express");
        t12301.setRunningDays(daily);
        t12301.setStops(new HashSet<>(Arrays.asList(
                createStop(t12301, hwh, 1, null, LocalTime.of(16, 50), 0, 0),
                createStop(t12301, pnbe, 2, LocalTime.of(22, 30), LocalTime.of(22, 35), 0, 534),
                createStop(t12301, cnb, 3, LocalTime.of(4, 40), LocalTime.of(4, 45), 1, 1010),
                createStop(t12301, ndls, 4, LocalTime.of(9, 55), null, 1, 1447)
        )));
        t12301.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t12301, sl, 200, 20, 0.45, 150),
                createConfig(t12301, ac3, 150, 15, 0.80, 250),
                createConfig(t12301, ac2, 80, 8, 1.20, 400),
                createConfig(t12301, ac1, 30, 3, 2.00, 600)
        )));
        trainRepository.save(t12301);

        Train t12302 = new Train();
        t12302.setTrainNumber("12302");
        t12302.setName("New Delhi - Howrah Rajdhani Express");
        t12302.setRunningDays(daily);
        t12302.setStops(new HashSet<>(Arrays.asList(
                createStop(t12302, ndls, 1, null, LocalTime.of(16, 55), 0, 0),
                createStop(t12302, cnb, 2, LocalTime.of(21, 35), LocalTime.of(21, 40), 0, 440),
                createStop(t12302, pnbe, 3, LocalTime.of(3, 45), LocalTime.of(3, 55), 1, 998),
                createStop(t12302, hwh, 4, LocalTime.of(9, 55), null, 1, 1447)
        )));
        t12302.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t12302, sl, 200, 20, 0.45, 150),
                createConfig(t12302, ac3, 150, 15, 0.80, 250),
                createConfig(t12302, ac2, 80, 8, 1.20, 400),
                createConfig(t12302, ac1, 30, 3, 2.00, 600)
        )));
        trainRepository.save(t12302);

        // --- 2. Mumbai Central <-> New Delhi Rajdhani Express ---
        Train t12951 = new Train();
        t12951.setTrainNumber("12951");
        t12951.setName("Mumbai Rajdhani Express");
        t12951.setRunningDays(daily);
        t12951.setStops(new HashSet<>(Arrays.asList(
                createStop(t12951, bct, 1, null, LocalTime.of(17, 0), 0, 0),
                createStop(t12951, adi, 2, LocalTime.of(23, 30), LocalTime.of(23, 40), 0, 493),
                createStop(t12951, jp, 3, LocalTime.of(5, 45), LocalTime.of(5, 50), 1, 1092),
                createStop(t12951, ndls, 4, LocalTime.of(8, 35), null, 1, 1384)
        )));
        t12951.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t12951, ac3, 200, 20, 0.85, 300),
                createConfig(t12951, ac2, 120, 12, 1.30, 450),
                createConfig(t12951, ac1, 40, 4, 2.20, 700)
        )));
        trainRepository.save(t12951);

        Train t12952 = new Train();
        t12952.setTrainNumber("12952");
        t12952.setName("New Delhi - Mumbai Rajdhani Express");
        t12952.setRunningDays(daily);
        t12952.setStops(new HashSet<>(Arrays.asList(
                createStop(t12952, ndls, 1, null, LocalTime.of(16, 55), 0, 0),
                createStop(t12952, jp, 2, LocalTime.of(19, 45), LocalTime.of(19, 50), 0, 300),
                createStop(t12952, adi, 3, LocalTime.of(2, 10), LocalTime.of(2, 20), 1, 900),
                createStop(t12952, bct, 4, LocalTime.of(8, 35), null, 1, 1384)
        )));
        t12952.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t12952, ac3, 200, 20, 0.85, 300),
                createConfig(t12952, ac2, 120, 12, 1.30, 450),
                createConfig(t12952, ac1, 40, 4, 2.20, 700)
        )));
        trainRepository.save(t12952);

        // --- 3. New Delhi <-> Varanasi Vande Bharat Express ---
        Train t22436 = new Train();
        t22436.setTrainNumber("22436");
        t22436.setName("Varanasi Vande Bharat Express");
        t22436.setRunningDays(daily);
        t22436.setStops(new HashSet<>(Arrays.asList(
                createStop(t22436, ndls, 1, null, LocalTime.of(6, 0), 0, 0),
                createStop(t22436, cnb, 2, LocalTime.of(10, 8), LocalTime.of(10, 10), 0, 440),
                createStop(t22436, bsb, 3, LocalTime.of(14, 0), null, 0, 759)
        )));
        t22436.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t22436, cc, 200, 20, 1.20, 400),
                createConfig(t22436, ec, 50, 5, 2.40, 900)
        )));
        trainRepository.save(t22436);

        Train t22435 = new Train();
        t22435.setTrainNumber("22435");
        t22435.setName("New Delhi Vande Bharat Express");
        t22435.setRunningDays(daily);
        t22435.setStops(new HashSet<>(Arrays.asList(
                createStop(t22435, bsb, 1, null, LocalTime.of(15, 0), 0, 0),
                createStop(t22435, cnb, 2, LocalTime.of(18, 30), LocalTime.of(18, 32), 0, 319),
                createStop(t22435, ndls, 3, LocalTime.of(23, 0), null, 0, 759)
        )));
        t22435.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t22435, cc, 200, 20, 1.20, 400),
                createConfig(t22435, ec, 50, 5, 2.40, 900)
        )));
        trainRepository.save(t22435);

        // --- 4. New Delhi <-> Lucknow Shatabdi Express ---
        Train t12004 = new Train();
        t12004.setTrainNumber("12004");
        t12004.setName("Lucknow Shatabdi Express");
        t12004.setRunningDays(daily);
        t12004.setStops(new HashSet<>(Arrays.asList(
                createStop(t12004, ndls, 1, null, LocalTime.of(6, 10), 0, 0),
                createStop(t12004, cnb, 2, LocalTime.of(11, 20), LocalTime.of(11, 25), 0, 440),
                createStop(t12004, lko, 3, LocalTime.of(12, 55), null, 0, 512)
        )));
        t12004.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t12004, cc, 220, 20, 1.10, 350),
                createConfig(t12004, ec, 40, 4, 2.20, 800)
        )));
        trainRepository.save(t12004);

        Train t12003 = new Train();
        t12003.setTrainNumber("12003");
        t12003.setName("New Delhi Shatabdi Express");
        t12003.setRunningDays(daily);
        t12003.setStops(new HashSet<>(Arrays.asList(
                createStop(t12003, lko, 1, null, LocalTime.of(15, 30), 0, 0),
                createStop(t12003, cnb, 2, LocalTime.of(16, 50), LocalTime.of(16, 55), 0, 72),
                createStop(t12003, ndls, 3, LocalTime.of(22, 20), null, 0, 512)
        )));
        t12003.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t12003, cc, 220, 20, 1.10, 350),
                createConfig(t12003, ec, 40, 4, 2.20, 800)
        )));
        trainRepository.save(t12003);

        // --- 5. Chennai <-> New Delhi Tamil Nadu Express ---
        Train t12621 = new Train();
        t12621.setTrainNumber("12621");
        t12621.setName("Tamil Nadu Express");
        t12621.setRunningDays(daily);
        t12621.setStops(new HashSet<>(Arrays.asList(
                createStop(t12621, mas, 1, null, LocalTime.of(22, 0), 0, 0),
                createStop(t12621, ngp, 2, LocalTime.of(13, 50), LocalTime.of(13, 55), 1, 1092),
                createStop(t12621, bpl, 3, LocalTime.of(20, 10), LocalTime.of(20, 15), 1, 1482),
                createStop(t12621, agc, 4, LocalTime.of(3, 10), LocalTime.of(3, 15), 2, 1989),
                createStop(t12621, ndls, 5, LocalTime.of(6, 30), null, 2, 2182)
        )));
        t12621.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t12621, sl, 250, 25, 0.40, 100),
                createConfig(t12621, ac3, 180, 18, 0.75, 200),
                createConfig(t12621, ac2, 100, 10, 1.10, 350)
        )));
        trainRepository.save(t12621);

        Train t12622 = new Train();
        t12622.setTrainNumber("12622");
        t12622.setName("New Delhi - Chennai Tamil Nadu Express");
        t12622.setRunningDays(daily);
        t12622.setStops(new HashSet<>(Arrays.asList(
                createStop(t12622, ndls, 1, null, LocalTime.of(21, 5), 0, 0),
                createStop(t12622, agc, 2, LocalTime.of(23, 25), LocalTime.of(23, 30), 0, 193),
                createStop(t12622, bpl, 3, LocalTime.of(6, 45), LocalTime.of(6, 50), 1, 700),
                createStop(t12622, ngp, 4, LocalTime.of(13, 5), LocalTime.of(13, 10), 1, 1090),
                createStop(t12622, mas, 5, LocalTime.of(6, 15), null, 2, 2182)
        )));
        t12622.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t12622, sl, 250, 25, 0.40, 100),
                createConfig(t12622, ac3, 180, 18, 0.75, 200),
                createConfig(t12622, ac2, 100, 10, 1.10, 350)
        )));
        trainRepository.save(t12622);

        // --- 6. Bengaluru <-> New Delhi Rajdhani Express ---
        Train t22691 = new Train();
        t22691.setTrainNumber("22691");
        t22691.setName("Bengaluru Rajdhani Express");
        t22691.setRunningDays(daily);
        t22691.setStops(new HashSet<>(Arrays.asList(
                createStop(t22691, sbc, 1, null, LocalTime.of(20, 0), 0, 0),
                createStop(t22691, hyb, 2, LocalTime.of(7, 0), LocalTime.of(7, 15), 1, 700),
                createStop(t22691, ngp, 3, LocalTime.of(15, 0), LocalTime.of(15, 10), 1, 1200),
                createStop(t22691, bpl, 4, LocalTime.of(21, 30), LocalTime.of(21, 35), 1, 1600),
                createStop(t22691, ndls, 5, LocalTime.of(5, 30), null, 2, 2366)
        )));
        t22691.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t22691, ac3, 160, 16, 0.82, 280),
                createConfig(t22691, ac2, 90, 9, 1.25, 420),
                createConfig(t22691, ac1, 35, 4, 2.10, 650)
        )));
        trainRepository.save(t22691);

        Train t22692 = new Train();
        t22692.setTrainNumber("22692");
        t22692.setName("New Delhi - Bengaluru Rajdhani Express");
        t22692.setRunningDays(daily);
        t22692.setStops(new HashSet<>(Arrays.asList(
                createStop(t22692, ndls, 1, null, LocalTime.of(20, 45), 0, 0),
                createStop(t22692, bpl, 2, LocalTime.of(5, 20), LocalTime.of(5, 25), 1, 766),
                createStop(t22692, ngp, 3, LocalTime.of(11, 20), LocalTime.of(11, 25), 1, 1166),
                createStop(t22692, hyb, 4, LocalTime.of(19, 0), LocalTime.of(19, 15), 1, 1666),
                createStop(t22692, sbc, 5, LocalTime.of(6, 40), null, 2, 2366)
        )));
        t22692.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t22692, ac3, 160, 16, 0.82, 280),
                createConfig(t22692, ac2, 90, 9, 1.25, 420),
                createConfig(t22692, ac1, 35, 4, 2.10, 650)
        )));
        trainRepository.save(t22692);

        // --- 7. Patna <-> New Delhi Rajdhani Express ---
        Train t12309 = new Train();
        t12309.setTrainNumber("12309");
        t12309.setName("Patna Rajdhani Express");
        t12309.setRunningDays(daily);
        t12309.setStops(new HashSet<>(Arrays.asList(
                createStop(t12309, pnbe, 1, null, LocalTime.of(17, 30), 0, 0),
                createStop(t12309, cnb, 2, LocalTime.of(2, 15), LocalTime.of(2, 20), 1, 553),
                createStop(t12309, ndls, 3, LocalTime.of(7, 40), null, 1, 998)
        )));
        t12309.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t12309, sl, 180, 18, 0.42, 120),
                createConfig(t12309, ac3, 120, 12, 0.78, 220),
                createConfig(t12309, ac2, 70, 7, 1.15, 380),
                createConfig(t12309, ac1, 30, 3, 2.00, 600)
        )));
        trainRepository.save(t12309);

        // --- 8. Mumbai <-> Pune Deccan Queen Express ---
        Train t12123 = new Train();
        t12123.setTrainNumber("12123");
        t12123.setName("Deccan Queen Superfast Express");
        t12123.setRunningDays(daily);
        t12123.setStops(new HashSet<>(Arrays.asList(
                createStop(t12123, csmt, 1, null, LocalTime.of(17, 10), 0, 0),
                createStop(t12123, pune, 2, LocalTime.of(20, 25), null, 0, 192)
        )));
        t12123.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t12123, cc, 250, 25, 0.80, 120),
                createConfig(t12123, ec, 60, 6, 1.80, 350)
        )));
        trainRepository.save(t12123);

        Train t12124 = new Train();
        t12124.setTrainNumber("12124");
        t12124.setName("Pune - Mumbai Deccan Queen Express");
        t12124.setRunningDays(daily);
        t12124.setStops(new HashSet<>(Arrays.asList(
                createStop(t12124, pune, 1, null, LocalTime.of(7, 15), 0, 0),
                createStop(t12124, csmt, 2, LocalTime.of(10, 30), null, 0, 192)
        )));
        t12124.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t12124, cc, 250, 25, 0.80, 120),
                createConfig(t12124, ec, 60, 6, 1.80, 350)
        )));
        trainRepository.save(t12124);

        // --- 9. Chennai <-> Bengaluru Shatabdi Express ---
        Train t12027 = new Train();
        t12027.setTrainNumber("12027");
        t12027.setName("Chennai - Bengaluru Shatabdi Express");
        t12027.setRunningDays(daily);
        t12027.setStops(new HashSet<>(Arrays.asList(
                createStop(t12027, mas, 1, null, LocalTime.of(17, 30), 0, 0),
                createStop(t12027, sbc, 2, LocalTime.of(22, 30), null, 0, 362)
        )));
        t12027.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t12027, cc, 240, 24, 0.90, 250),
                createConfig(t12027, ec, 40, 4, 2.00, 650)
        )));
        trainRepository.save(t12027);

        // --- 10. Demo Express (JP <-> LKO) - Tiny capacity for instant testing ---
        Train t19999 = new Train();
        t19999.setTrainNumber("19999");
        t19999.setName("Demo Express");
        t19999.setRunningDays(daily);
        t19999.setStops(new HashSet<>(Arrays.asList(
                createStop(t19999, jp, 1, null, LocalTime.of(6, 0), 0, 0),
                createStop(t19999, adi, 2, LocalTime.of(12, 30), LocalTime.of(12, 45), 0, 600),
                createStop(t19999, lko, 3, LocalTime.of(22, 0), null, 0, 1097)
        )));
        t19999.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t19999, sl, 4, 2, 0.40, 80),
                createConfig(t19999, ac3, 3, 1, 0.75, 180)
        )));
        trainRepository.save(t19999);

        Train t19998 = new Train();
        t19998.setTrainNumber("19998");
        t19998.setName("Demo Express (Return)");
        t19998.setRunningDays(daily);
        t19998.setStops(new HashSet<>(Arrays.asList(
                createStop(t19998, lko, 1, null, LocalTime.of(6, 0), 0, 0),
                createStop(t19998, adi, 2, LocalTime.of(15, 30), LocalTime.of(15, 45), 0, 497),
                createStop(t19998, jp, 3, LocalTime.of(22, 0), null, 0, 1097)
        )));
        t19998.setClassConfigs(new HashSet<>(Arrays.asList(
                createConfig(t19998, sl, 4, 2, 0.40, 80),
                createConfig(t19998, ac3, 3, 1, 0.75, 180)
        )));
        trainRepository.save(t19998);

        log.info("Seed data successfully loaded: 2 users, {} stations, {} bidirectional flagship trains",
                stationRepository.count(), trainRepository.count());
    }

    private SeatClass saveClass(String code, String label) {
        SeatClass sc = new SeatClass();
        sc.setCode(code);
        sc.setLabel(label);
        return seatClassRepository.save(sc);
    }

    private void saveUser(String email, String pass, String first, String last, Role role) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(pass));
        u.setFirstName(first);
        u.setLastName(last);
        u.setRole(role);
        u.setCreatedAt(LocalDateTime.now());
        userRepository.save(u);
    }

    private Station saveStation(String code, String name, String city) {
        Station s = new Station();
        s.setCode(code);
        s.setName(name);
        s.setCity(city);
        return stationRepository.save(s);
    }

    private TrainStop createStop(Train t, Station s, int seq, LocalTime arr, LocalTime dep, int dayOffset, int dist) {
        TrainStop ts = new TrainStop();
        ts.setTrain(t);
        ts.setStation(s);
        ts.setSequenceNumber(seq);
        ts.setArrivalTime(arr);
        ts.setDepartureTime(dep);
        ts.setDayOffset(dayOffset);
        ts.setDistanceFromOriginKm(dist);
        return ts;
    }

    private TrainClassConfig createConfig(Train t, SeatClass sc, int seats, int rac, double fareKm, double base) {
        TrainClassConfig c = new TrainClassConfig();
        c.setTrain(t);
        c.setSeatClass(sc);
        c.setTotalSeats(seats);
        c.setRacQuota(rac);
        c.setFarePerKm(fareKm);
        c.setBaseFare(base);
        return c;
    }
}
